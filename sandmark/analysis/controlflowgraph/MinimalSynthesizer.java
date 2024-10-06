package sandmark.analysis.controlflowgraph;



/**
 * A simple code generator designed to minimize output size
 * without reordering nodes.
 * <UL>
 * <LI> A node with no out-edges produces a RETURN instruction.
 * <LI> A node with one out-edge produces a GOTO instruction,
 *      or an ICONST_0/POP pair if fallthrough suffices.
 * <LI> A node with two out-edges, including one fall-through,
 *      produces ICONST_0 and IFNE.
 * <LI> All other cases produce ICONST_0 and TABLESWITCH.
 * </UL>
 *
 * If there is a leaf node (RETURN instruction) reachable from the
 * root node, the generated code will be safely executable.
 * If there is no leaf node, the generated code will loop forever.
 */
public class MinimalSynthesizer extends VoidMethodSynthesizer {


/**
 * Generates a void no-argument method corresponding to the given graph.
 * All graphs are acceptable; no IllegalArgumentException is thrown.
 */
protected sandmark.program.Method generate(
      sandmark.util.newgraph.Graph graph,
      sandmark.program.Class clazz,
      java.lang.Object root,
      java.util.Map nodeNumbers) {

   java.lang.Object[] exits = getExits(graph, nodeNumbers);

   org.apache.bcel.generic.InstructionList inslist =
      new org.apache.bcel.generic.InstructionList();

   org.apache.bcel.generic.InstructionHandle handles[] =
      makeNOPs(inslist, graph, root, nodeNumbers);

   allNodes(inslist, handles, graph, root, exits, nodeNumbers);
   inslist.setPositions(true);


   inslist.setPositions(true);
   sandmark.program.Method method = makeMethod(inslist, clazz);
   method.removeNOPs();
   return method;
}



/**
 * Generates a list, indexed by node number, of preferred jump targets.
 * For each graph node, the corresponding list entry
 * is a node leading (by shortest path) to a return.
 */
private java.lang.Object[] getExits(sandmark.util.newgraph.Graph graph,
				    java.util.Map nodeNumbers) {

   java.util.Vector v = new java.util.Vector();
   java.util.Iterator i = graph.nodes();
   while (i.hasNext()) {
      java.lang.Object node = i.next();
      if (! graph.outEdges(node).hasNext()) {      // if no outgoing edges
         v.add(node);                              // it's a return; add it
      }
   }

   java.lang.Object[] elist =
      new java.lang.Object[nodeNumbers.size()];
   while (v.size() > 0) {                       // multiple passes until done
      v = setIncoming(graph, elist, v, nodeNumbers);
   }
   return elist;
}



/**
 * For each Node in v, checks every incoming edge, and adds the node from v
 * as the preferred target if the source node does not already have one.
 * Returns a new vector of nodes for which targets were assigned.
 */
private java.util.Vector setIncoming(
      sandmark.util.newgraph.Graph graph,
      java.lang.Object[] elist,
      java.util.Vector v,
      java.util.Map nodeNumbers) {

   java.util.Vector vnew = new java.util.Vector();
   java.util.Iterator i = v.iterator();
   while (i.hasNext()) {
      java.lang.Object target = i.next();
      java.util.Iterator j = graph.inEdges(target);
      while (j.hasNext()) {
         sandmark.util.newgraph.Edge edge = 
	    (sandmark.util.newgraph.Edge) j.next();
         java.lang.Object source = edge.sourceNode();
         int n = ((Integer)nodeNumbers.get(source)).intValue();
         if (elist[n] == null) {
            elist[n] = target;
            vnew.add(source);
         }
      }
   }

   return vnew;
}



/**
 *  Generates code for all nodes.
 */
private void allNodes(
      org.apache.bcel.generic.InstructionList inslist,
      org.apache.bcel.generic.InstructionHandle handles[],
      sandmark.util.newgraph.Graph graph,
      java.lang.Object root,
      java.lang.Object[] exits,
      java.util.Map nodeNumbers) {

   java.util.Iterator i = graph.depthFirst(root);
   java.lang.Object thisNode = i.next();
   while (i.hasNext()) {
      java.lang.Object nextNode = i.next();
      genNode(inslist, handles, graph, exits, thisNode, nextNode,
	      nodeNumbers);
      thisNode = nextNode;
   }
   genNode(inslist, handles, graph, exits, thisNode, null, nodeNumbers);
}



/**
 *  Generates code for one node.
 */
private void genNode(
      org.apache.bcel.generic.InstructionList inslist,
      org.apache.bcel.generic.InstructionHandle handles[],
      sandmark.util.newgraph.Graph graph,
      java.lang.Object[] exits,
      java.lang.Object thisNode,
      java.lang.Object nextNode,
      java.util.Map nodeNumbers) {

   int thisNum = ((Integer)nodeNumbers.get(thisNode)).intValue();
   int nextNum = -1;
   if (nextNode != null)
      nextNum = ((Integer)nodeNumbers.get(nextNode)).intValue();
   java.lang.Object prefNode = exits[thisNum];

   int outEdges = 0;
   int dest = 0;
   boolean fallThrough = false;

   for (java.util.Iterator j = graph.outEdges(thisNode); j.hasNext(); ) {
      outEdges++;
      sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge) j.next();
      dest = ((Integer)nodeNumbers.get(e.sinkNode())).intValue();
      fallThrough |= (nextNum == dest);
   }

   org.apache.bcel.generic.InstructionHandle h = handles[thisNum];
   if (outEdges == 0) {
      inslist.append(h, org.apache.bcel.generic.InstructionConstants.RETURN);
   } else if (outEdges == 1) {
      java.util.Iterator it = nextNode == null ? null :
         graph.inEdges(nextNode);
      if (it != null && it.hasNext()) {
         it.next();
      }
      if (it != null && !it.hasNext()) {
         it = null;
      }
      if (fallThrough && it != null) {
         h = inslist.append(h,
            org.apache.bcel.generic.InstructionConstants.ICONST_0);
         h = inslist.append(h,
            org.apache.bcel.generic.InstructionConstants.POP);
      } else {
         inslist.append(h, new org.apache.bcel.generic.GOTO(handles[dest]));
      }
   } else if (outEdges == 2 && fallThrough) {
      genIfNE(inslist, handles, h, graph, thisNode, nextNode, prefNode,
	      nodeNumbers);
   } else {
      genSwitch(inslist, handles, h, graph, thisNode, prefNode,
		nodeNumbers);
   }
}



/**
 *  Generates a conditional jump.  If the first out-edge of thisNode
 *  is not to nextNode, then the jump reflects that edge.  Otherwise,
 *  the jump reflects the second out-edge of thisNode.
 */
private org.apache.bcel.generic.InstructionHandle genIfNE(
      org.apache.bcel.generic.InstructionList inslist,
      org.apache.bcel.generic.InstructionHandle handles[],
      org.apache.bcel.generic.InstructionHandle h,
      sandmark.util.newgraph.Graph graph,
      java.lang.Object thisNode,
      java.lang.Object nextNode,
      java.lang.Object prefNode,
      java.util.Map nodeNumbers) {

   int dest;
   java.util.Iterator j = graph.outEdges(thisNode);
   sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge) j.next();
   if (e.sinkNode() == nextNode) {
      e = (sandmark.util.newgraph.Edge) j.next();
   }
   dest = ((Integer)nodeNumbers.get(e.sinkNode())).intValue();
   h = inslist.append(h, org.apache.bcel.generic.InstructionConstants.ICONST_0);
   if (prefNode != null 
       && dest == ((Integer)nodeNumbers.get(prefNode)).intValue()) {
      h = inslist.append(h, new org.apache.bcel.generic.IFEQ(handles[dest]));
   } else {
      h = inslist.append(h, new org.apache.bcel.generic.IFNE(handles[dest]));
   }
   return h;
}



/**
 *  Generates a TABLESWITCH instruction.
 */
private org.apache.bcel.generic.InstructionHandle genSwitch(
      org.apache.bcel.generic.InstructionList inslist,
      org.apache.bcel.generic.InstructionHandle handles[],
      org.apache.bcel.generic.InstructionHandle h,
      sandmark.util.newgraph.Graph graph,
      java.lang.Object thisNode,
      java.lang.Object prefNode,
      java.util.Map nodeNumbers) {

   org.apache.bcel.generic.InstructionHandle defjump;
   sandmark.util.newgraph.Edge e;

   java.util.Iterator j = graph.outEdges(thisNode);
   if (prefNode != null) {
      defjump = handles[((Integer)nodeNumbers.get(prefNode)).intValue()];
   } else {
      e = (sandmark.util.newgraph.Edge) j.next();
      defjump = handles[((Integer)nodeNumbers.get(e.sinkNode())).intValue()];
   }

   java.util.Vector v = new java.util.Vector();
   while (j.hasNext()) {
      e = (sandmark.util.newgraph.Edge) j.next();
      java.lang.Object n = e.sinkNode();
      if (n == prefNode) {
         prefNode = null;	// already entered as default
      } else {
         v.add(handles[((Integer)nodeNumbers.get(n)).intValue()]);
      }
   }
   org.apache.bcel.generic.InstructionHandle targets[] =
      new org.apache.bcel.generic.InstructionHandle[v.size()];
   v.toArray(targets);

   int[] m = new int[targets.length];
   for (int i = 0; i < targets.length; i++) {
      m[i] = i + 1;
   }

   h = inslist.append(h, org.apache.bcel.generic.InstructionConstants.ICONST_0);
   return inslist.append(h,
      new org.apache.bcel.generic.TABLESWITCH(m, targets, defjump));
}



/**
 * Test driver; see ControlFlowSynthesizer.test.
 */
public static void main(String[] args) throws java.lang.Exception {
   new MinimalSynthesizer().test(args);
}



} // class MinimalSynthesizer

