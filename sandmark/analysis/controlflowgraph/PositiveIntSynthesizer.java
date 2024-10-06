package sandmark.analysis.controlflowgraph;



/**
 * A PositiveIntSyntheizer generates an integer function that works
 * by maintaining the "hidden" invariant: its one variable always has
 * a nonnegative value.  The function argument is made non-negative
 * using a randomly chosen code fragment.  Basic block code fragments
 * (also randomly chosen) preserve the invariant.
 *
 * Code can be generated to force a positive, negative, zero, or
 * nonzero result, allowing use with an opaque predicate.
 */

public class PositiveIntSynthesizer extends IntFuncSynthesizer {



/**
 * Generates an integer function corresponding to the given graph.
 * All graphs are acceptable.
 *
 * The generated code is guaranteed to return safely, without throwing
 * an exception, provided that the graph contains at least one leaf node.
 * (A leaf node produces a return.  With no return, the code loops forever.)
 */
public final sandmark.program.Method generate(
   sandmark.util.newgraph.Graph g,
   sandmark.program.Class clazz,
   short opcode) {

   java.lang.Object root = g.roots().next();
   return generate(g, clazz, root, getNodeNumbers(g, root), opcode);
}

protected sandmark.program.Method generate(
   sandmark.util.newgraph.Graph g,
   sandmark.program.Class clazz,
   java.lang.Object root,
   java.util.Map nodeNumbers) {

   return generate(g, clazz, root, nodeNumbers, 
		   org.apache.bcel.Constants.NOP);
}

/**
 * Generates an integer function with a constraint on the returned result.
 * The opcode argument is one of the org.apache.bcel.Constants values
 * IFLT, IFLE, IFEQ, IFNE, IFGE, IFGT, or NOP.  The generated code will
 * ensure, for any passed argument, that the result value passes the test
 * (takes the jump) indicated by the opcode.  A NOP argument indicates a
 * don't-care situation where the result is unconstrained.
 */
 protected sandmark.program.Method generate(
      sandmark.util.newgraph.Graph graph,
      sandmark.program.Class clazz,
      java.lang.Object root,
      java.util.Map nodeNumbers,
      short opcode) {

    java.lang.Object nodes[] = new java.lang.Object[graph.nodeCount()];
    for (java.util.Iterator i = graph.nodes(); i.hasNext(); ) {
       java.lang.Object n = i.next();
       nodes[((Integer)nodeNumbers.get(n)).intValue()] = n;
    }
    boolean hamiltonian = true;
    for (int i = 0; hamiltonian && i < nodes.length - 1; i++)
       hamiltonian = graph.hasEdge(nodes[i], nodes[i+1]);
    sandmark.util.newgraph.Graph tree = null;
    if (hamiltonian) {
       tree = sandmark.util.newgraph.Graphs.createGraph(graph.nodes(), null);
       java.util.Iterator edgeIter = graph.edges();
       while (edgeIter.hasNext()) {
	  sandmark.util.newgraph.Edge e = 
	     (sandmark.util.newgraph.Edge)edgeIter.next();
	  int from = ((Integer)nodeNumbers.get(e.sourceNode())).intValue();
	  int to = ((Integer)nodeNumbers.get(e.sinkNode())).intValue();
	  if (from + 1 == to)
	     tree = tree.addEdge(e);
       }
    }
    else
       graph.depthFirstTree(root);
    java.util.BitSet[] elist = getExits(graph, nodeNumbers, tree);

    org.apache.bcel.generic.InstructionList inslist =
       new org.apache.bcel.generic.InstructionList();

    org.apache.bcel.generic.InstructionHandle handles[] =
       makeNOPs(inslist, graph, root, nodeNumbers);

    prologue(inslist);
    allNodes(inslist, handles, opcode, graph, root, elist, nodeNumbers);
    inslist.setPositions(true);
    
    inslist.setPositions(true);
    sandmark.program.Method method = makeFunc(inslist, clazz);
    method.removeNOPs();
    return method;
}



/**
 * Generates a list, indexed by node number, of java.util.BitSet objects
 * identifying acceptable jump targets.  For each graph node, the nonzero
 * bits (also indexed by node number) indicate those jump targets that
 * lead to a return statement without traversing a back edge.
 */
private java.util.BitSet[] getExits(sandmark.util.newgraph.Graph graph,
				    java.util.Map nodeNumbers,
				    sandmark.util.newgraph.Graph dftree) {

   java.util.Vector v = new java.util.Vector();
   java.util.Iterator i = graph.nodes();
   while (i.hasNext()) {
      java.lang.Object node = i.next();
      if (! graph.outEdges(node).hasNext()) {      // if no outgoing edges
         v.add(node);                              // it's a return; add it
      }
   }

   java.util.BitSet[] blist = new java.util.BitSet[nodeNumbers.size()];
   for (int j = 0; j < blist.length; j++) {
      blist[j] = new java.util.BitSet();
   }

   while (v.size() > 0) {               // multiple passes until done
      v = setIncoming(graph, blist, v, nodeNumbers, dftree);
   }
   return blist;
}



/**
 * For each Node in v, checks every incoming edge, and adds the target node
 * as an acceptable destination from the source if the edge is not a BACK edge.
 * Returns a new vector of nodes for which new destinations were assigned.
 */
private java.util.Vector setIncoming(
      sandmark.util.newgraph.Graph graph,
      java.util.BitSet[] blist,
      java.util.Vector v,
      java.util.Map nodeNumbers,
      sandmark.util.newgraph.Graph dftree) {

   java.util.Vector vnew = new java.util.Vector();
   java.util.Iterator i = v.iterator();
   while (i.hasNext()) {
      java.lang.Object target = i.next();
      java.util.Iterator j = graph.inEdges(target);
      int t = ((Integer)nodeNumbers.get(target)).intValue();
      while (j.hasNext()) {
         sandmark.util.newgraph.Edge edge = 
	    (sandmark.util.newgraph.Edge) j.next();
	 if (!dftree.reachable(edge.sinkNode(), edge.sourceNode())) {
            java.lang.Object source = edge.sourceNode();
            int n = ((Integer)nodeNumbers.get(source)).intValue();
            if (! blist[n].get(t)) {
               blist[n].set(t);
               vnew.add(source);
            }
         }
      }
   }

   return vnew;
}



/**
 *  Generates prologue code that ensures that local variable 0 is non-negative.
 *  One of several possible prologues is chosen randomly.
 */
private org.apache.bcel.generic.InstructionHandle prologue(
      org.apache.bcel.generic.InstructionList inslist) {

   CodeContext cx = new CodeContext(inslist);
   cx.insert(org.apache.bcel.generic.InstructionConstants.NOP); // init handle

   genLoad(cx, 0);                      // load n

   switch (randomIn(1, 8)) {            // keep in sync with number of cases

      default:
      case 1:
         // convert to char, which zeros the top 16 bits
         cx.append(org.apache.bcel.generic.InstructionConstants.I2C);
         break;

      case 2:
         // AND a constant with the lower 11 bits
         // (the double random calls bias towards 1-bits in the result)
         genPush(cx, randomIn(1, 0x7FFF) | randomIn(1, 0x7FFF));
         cx.append(org.apache.bcel.generic.InstructionConstants.IAND);
         break;

      case 3:
         // compute a large constant (max 30 bits) and AND it in
         genPush(cx, randomIn(1, 0x7FFF) | randomIn(1, 0x7FFF));
         cx.append(org.apache.bcel.generic.InstructionConstants.DUP);
         cx.append(org.apache.bcel.generic.InstructionConstants.IMUL);
         cx.append(org.apache.bcel.generic.InstructionConstants.IAND);
         break;

      case 4:
         // clear the sign bit by ANDing with (-1 >>> 1)
         cx.append(new org.apache.bcel.generic.ICONST(-1));
         cx.append(new org.apache.bcel.generic.ICONST(1));
         cx.append(org.apache.bcel.generic.InstructionConstants.IUSHR);
         cx.append(org.apache.bcel.generic.InstructionConstants.IAND);
         break;

      case 5:
         // dup, shift right 31, XOR
         // (on a ones-complement architecture this would be an ABS operation)
         cx.append(org.apache.bcel.generic.InstructionConstants.DUP);
         cx.append(new org.apache.bcel.generic.BIPUSH((byte) 31));
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHR);
         cx.append(org.apache.bcel.generic.InstructionConstants.IXOR);
         break;

      case 6:
         // isolate the sign bit and XOR it back in
         cx.append(org.apache.bcel.generic.InstructionConstants.DUP);
         cx.append(new org.apache.bcel.generic.BIPUSH((byte) 1));
         cx.append(new org.apache.bcel.generic.BIPUSH((byte) 31));
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHL);
         cx.append(org.apache.bcel.generic.InstructionConstants.IAND);
         cx.append(org.apache.bcel.generic.InstructionConstants.IXOR);
         break;

      case 7:
         // set the top two bits, then negate
         // (need two bits to ensure that INEG produces a positive value)
         cx.append(new org.apache.bcel.generic.ICONST(3));
         cx.append(new org.apache.bcel.generic.BIPUSH((byte) 30));
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHL);
         cx.append(org.apache.bcel.generic.InstructionConstants.IOR);
         cx.append(org.apache.bcel.generic.InstructionConstants.INEG);
         break;

      case 8:
         // force negative via OR of a constant, then negate
         genPush(cx, -0x8000 | randomIn(1, 0x7FFF) | randomIn(1, 0x7FFF));
         cx.append(org.apache.bcel.generic.InstructionConstants.IOR);
         cx.append(org.apache.bcel.generic.InstructionConstants.INEG);
         break;
   }

   genStore(cx, 0);                     // store new n (now n >= 0)
   return cx.getHandle();
}



/**
 *  Generates code for all nodes.
 */
private void allNodes(
      org.apache.bcel.generic.InstructionList inslist,
      org.apache.bcel.generic.InstructionHandle handles[],
      short opcode,
      sandmark.util.newgraph.Graph graph,
      java.lang.Object root,
      java.util.BitSet[] elist,
      java.util.Map nodeNumbers) {

   java.util.Iterator i = graph.depthFirst(root);
   java.lang.Object thisNode = i.next();
   while (i.hasNext()) {
      java.lang.Object nextNode = i.next();
      genNode(inslist, handles, opcode, graph, elist, thisNode, nextNode,
	      nodeNumbers);
      thisNode = nextNode;
   }
   genNode(inslist, handles, opcode, graph, elist, thisNode, null,
	   nodeNumbers);
}



/**
 *  Generates code for one node.
 */
private void genNode(
      org.apache.bcel.generic.InstructionList inslist,
      org.apache.bcel.generic.InstructionHandle handles[],
      short opcode,
      sandmark.util.newgraph.Graph graph,
      java.util.BitSet[] elist,
      java.lang.Object thisNode,
      java.lang.Object nextNode,
      java.util.Map nodeNumbers) {

   int thisNum = ((Integer)nodeNumbers.get(thisNode)).intValue();
   int nextNum = -1;
   if (nextNode != null)
      nextNum = ((Integer)nodeNumbers.get(nextNode)).intValue();
   java.util.BitSet exits = elist[thisNum];

   int outEdges = 0;
   int dest = 0;
   boolean fallThrough = false;

   for (java.util.Iterator j = graph.outEdges(thisNode); j.hasNext(); ) {
      outEdges++;
      sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge) j.next();
      dest = ((Integer)nodeNumbers.get(e.sinkNode())).intValue();
      fallThrough |= (nextNum == dest);
   }

   CodeContext cx = new CodeContext(inslist, handles[thisNum]);

   if (outEdges == 0) {
      genReturn(cx, opcode);
   } else if (outEdges == 1) {
      bblock(cx);
      java.util.Iterator it = nextNode == null ? null :
         graph.inEdges(nextNode);
      if (it != null && it.hasNext()) {
         it.next();
      }
      if (it != null && !it.hasNext()) {
         it = null;
      }
      if (!fallThrough || it == null) {
         cx.append(new org.apache.bcel.generic.GOTO(handles[dest]));
      }
   } else if (outEdges == 2 && fallThrough) {
      bblock(cx);
      genIf(cx, handles, graph, exits, thisNode, nextNode, nodeNumbers);
   } else {
      bblock(cx);
      genSwitch(cx, handles, graph, exits, thisNode, nodeNumbers);
   }
}



/**
 * Generates a simple basic block chosen randomly from among several
 * possibilities.  All sequences preserve the sign bit of a nonnegative
 * local variable 0.
 *
 * Try to do all this without collapsing all input values to zero.
 * For example, that's why we divide only by small values.
 */
private org.apache.bcel.generic.InstructionHandle bblock(CodeContext cx) {

   genLoad(cx, 0);                      // load n

   switch (randomIn(1, 14)) {           // keep in sync with number of cases

      default:
      case 1:
         // set a randomly chosen bit (0 through 14)
         genPush(cx, 1 << randomIn(0, 14));
         cx.append(org.apache.bcel.generic.InstructionConstants.IOR);
         break;

      case 2:
         // set probably *two* randomly chosen bits (0 through 14)
         genPush(cx, (1 << randomIn(0, 14)) | (1 << randomIn(0, 14)));
         cx.append(org.apache.bcel.generic.InstructionConstants.IOR);
         break;

      case 3:
         // set a randomly chosen bit (15 through 30)
         cx.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
         genPush(cx, randomIn(15, 30));
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHL);
         cx.append(org.apache.bcel.generic.InstructionConstants.IOR);
         break;

      case 4:
         // flip a randomly chosen bit (0 through 30)
         cx.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
         genPush(cx, randomIn(15, 30));
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHL);
         cx.append(org.apache.bcel.generic.InstructionConstants.IOR);
         break;

      case 5:
         // clear a randomly chosen bit (0 through 14)
         genPush(cx, ~ (1 << randomIn(0, 14)));
         cx.append(org.apache.bcel.generic.InstructionConstants.IAND);
         break;

      case 6:
         // cast to char
         cx.append(org.apache.bcel.generic.InstructionConstants.I2C);
         break;

      case 7:
         // unsigned shift down by 1 to 4 bits  (divide by 2 | 4 | 8 | 16)
         cx.append(new org.apache.bcel.generic.ICONST(randomIn(1, 4)));
         cx.append(org.apache.bcel.generic.InstructionConstants.IUSHR);
         break;

      case 8:
         // signed shift down by 1 to 4 bits  (divide by 2 | 4 | 8 | 16)
         cx.append(new org.apache.bcel.generic.ICONST(randomIn(1, 4)));
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHR);
         break;

      case 9:
         // divide by a randomly chosen small constant
         genPush(cx, randomIn(3, 15));
         cx.append(org.apache.bcel.generic.InstructionConstants.IDIV);
         break;

      case 10:
         // reduce modulo a randomly chosen constant
         genPush(cx, randomIn(3, 0x7FFF));
         cx.append(org.apache.bcel.generic.InstructionConstants.IREM);
         break;

      case 11:
         // mask off the low N bits, then add in a constant
         int t11 = 1 << randomIn(2, 14);
         genPush(cx, -t11);
         cx.append(org.apache.bcel.generic.InstructionConstants.IAND);
         genPush(cx, randomIn(1, t11 - 1));
         cx.append(org.apache.bcel.generic.InstructionConstants.IADD);
         break;

      case 12:
         // divide by small n, then multiply by n-1
         int k12 = randomIn(3, 15);
         genPush(cx, k12);
         cx.append(org.apache.bcel.generic.InstructionConstants.IDIV);
         genPush(cx, k12 - 1);
         cx.append(org.apache.bcel.generic.InstructionConstants.IMUL);
         break;

      case 13:
         // multiply by 3/4 or 7/8 by subtracting 1/4 or 1/8 of self
         cx.append(org.apache.bcel.generic.InstructionConstants.DUP);
         cx.append(new org.apache.bcel.generic.ICONST(randomIn(2, 3)));
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHR);
         cx.append(org.apache.bcel.generic.InstructionConstants.ISUB);
         break;

      case 14:
         // multiply by a constant, then unsigned shift 1 to ensure positive
         genPush(cx, randomIn(3, 127));
         cx.append(org.apache.bcel.generic.InstructionConstants.IMUL);
         cx.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
         cx.append(org.apache.bcel.generic.InstructionConstants.IUSHR);
         break;
   }

   genStore(cx, 0);                     // store new n (now n >= 0)
   return cx.getHandle();
}



/**
 * Generates a return expression that makes the given opcode take a jump.
 */
private void genReturn(CodeContext cx, short opcode) {
   switch (opcode) {
      case org.apache.bcel.Constants.IFLT:
         genPush(cx, -1);
         genLoad(cx, 0);
         cx.append(org.apache.bcel.generic.InstructionConstants.ISUB);
         break;
      case org.apache.bcel.Constants.IFLE:
         genLoad(cx, 0);
         cx.append(org.apache.bcel.generic.InstructionConstants.INEG);
         break;
      case org.apache.bcel.Constants.IFEQ:
         genLoad(cx, 0);
         genPush(cx, 31);
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHR);
         break;
      case org.apache.bcel.Constants.IFNE:
         genLoad(cx, 0);
         genPush(cx, randomIn(1, 16));
         cx.append(org.apache.bcel.generic.InstructionConstants.ISHL);
         genPush(cx, (1 << randomIn(0, 14)) | (1 << randomIn(0, 14)));
         cx.append(org.apache.bcel.generic.InstructionConstants.IOR);
         break;
      case org.apache.bcel.Constants.IFGE:
      case org.apache.bcel.Constants.NOP:
         genLoad(cx, 0);
         /* nothing more needed */
         break;
      case org.apache.bcel.Constants.IFGT:
         genPush(cx, (1 << randomIn(0, 14)) | (1 << randomIn(0, 14)));
         genLoad(cx, 0);
         cx.append(org.apache.bcel.generic.InstructionConstants.IOR);
         break;
      default:
         throw new java.lang.IllegalArgumentException("opcode=" + opcode);
   }
   cx.append(org.apache.bcel.generic.InstructionConstants.IRETURN);
}



/**
 *  Generates a conditional jump.  If the first out-edge of thisNode
 *  is not to nextNode, then the jump reflects that edge.  Otherwise,
 *  the jump reflects the second out-edge of thisNode.
 */
private void genIf(
      CodeContext cx,
      org.apache.bcel.generic.InstructionHandle handles[],
      sandmark.util.newgraph.Graph graph,
      java.util.BitSet exits,
      java.lang.Object thisNode,
      java.lang.Object nextNode,
      java.util.Map nodeNumbers) {

   java.util.Iterator j = graph.outEdges(thisNode);
   sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge) j.next();
   if (e.sinkNode() == nextNode) {
      e = (sandmark.util.newgraph.Edge) j.next();
   }
   int dest = ((Integer)nodeNumbers.get(e.sinkNode())).intValue();
   int fall = ((Integer)nodeNumbers.get(nextNode)).intValue();

   genLoad(cx, 0);
   if (exits.get(dest) && ! exits.get(fall)) {
      // ensure jump taken
      cx.append(new org.apache.bcel.generic.IFGE(handles[dest]));
   } else if (exits.get(fall) && ! exits.get(dest)) {
      // ensure jump not taken
      cx.append(new org.apache.bcel.generic.IFLT(handles[dest]));
   } else switch (randomIn(1, 4)) {
      // doesn't matter
      case 1: cx.append(new org.apache.bcel.generic.IFGT(handles[dest])); break;
      case 2: cx.append(new org.apache.bcel.generic.IFLE(handles[dest])); break;
      case 3: cx.append(new org.apache.bcel.generic.IFEQ(handles[dest])); break;
      case 4: cx.append(new org.apache.bcel.generic.IFNE(handles[dest])); break;
   }
}



/**
 *  Generates a TABLESWITCH instruction.
 */
private void genSwitch(
      CodeContext cx,
      org.apache.bcel.generic.InstructionHandle handles[],
      sandmark.util.newgraph.Graph graph,
      java.util.BitSet exits,
      java.lang.Object thisNode,
      java.util.Map nodeNumbers) {

   java.util.Iterator j = graph.outEdges(thisNode);
   org.apache.bcel.generic.InstructionHandle defjump;
   int n = exits.nextSetBit(0);
   if (n > 0) {
      defjump = handles[n];
   } else {
       sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge) j.next();
      defjump = handles[((Integer)nodeNumbers.get(e.sinkNode())).intValue()];
   }

   java.util.Vector v = new java.util.Vector();
   while (j.hasNext()) {
       sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge) j.next();
       org.apache.bcel.generic.InstructionHandle ih =
	   handles[((Integer)nodeNumbers.get(e.sinkNode())).intValue()];
       if(ih != defjump)
	   v.add(ih);
   }
   org.apache.bcel.generic.InstructionHandle targets[] =
      new org.apache.bcel.generic.InstructionHandle[v.size()];
   v.toArray(targets);

   int[] m = new int[targets.length];
   for (int i = 0; i < targets.length; i++) {
      m[i] = i + 1;
   }

   genLoad(cx, 0);
   cx.append(org.apache.bcel.generic.InstructionConstants.INEG);
   cx.append(new org.apache.bcel.generic.TABLESWITCH(m, targets, defjump));
}



/**
 * Test driver; see ControlFlowSynthesizer.test.
 */
public static void main(String[] args) throws java.lang.Exception {
   PositiveIntSynthesizer ifs = new PositiveIntSynthesizer();
   ifs.test(args);
}



} // class PositiveIntSynthesizer

