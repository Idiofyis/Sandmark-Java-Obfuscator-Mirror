package sandmark.analysis.controlflowgraph;



/**
 * A simple code generator using conditional and unconditional branches.
 * Each graph node yields a NOP instruction followed by zero or more
 * conditional branches and then an unconditional branch.  This is a
 * little too naive because sandmark.analysis.controlflowgraph.MethodCFG
 * counts a series of gotos as multiple basic blocks.  A leaf node
 * of the graph produces a RETURN.
 */
public class SimpleGotoSynthesizer extends VoidMethodSynthesizer {



/**
 * Generates a void no-argument method corresponding to the given graph.
 * All graphs are acceptable; no IllegalArgumentException is thrown.
 */
protected sandmark.program.Method generate(
      sandmark.util.newgraph.Graph graph,
      sandmark.program.Class clazz,
      java.lang.Object root,
      java.util.Map nodeNumbers) {

   org.apache.bcel.generic.InstructionList inslist =
      new org.apache.bcel.generic.InstructionList();

   org.apache.bcel.generic.InstructionHandle handles[] =
      makeNOPs(inslist, graph, root, nodeNumbers);

   addJumps(inslist, handles, graph, root, nodeNumbers);
   inslist.setPositions();

   return makeMethod(inslist, clazz);
}


/**
 *  Generates the jumps from each node.
 */
private void addJumps(
      org.apache.bcel.generic.InstructionList inslist,
      org.apache.bcel.generic.InstructionHandle handles[],
      sandmark.util.newgraph.Graph graph,
      java.lang.Object root,
      java.util.Map nodeNumbers) {

   java.util.Iterator i = graph.depthFirst(root);
   while (i.hasNext()) {
      java.lang.Object node = i.next();
      int self = ((Integer)nodeNumbers.get(node)).intValue();
      org.apache.bcel.generic.InstructionHandle h = handles[self];
      for (java.util.Iterator j = graph.outEdges(node); j.hasNext(); ) {
         sandmark.util.newgraph.Edge e = 
	    (sandmark.util.newgraph.Edge) j.next();
         int dest = ((Integer)nodeNumbers.get(e.sinkNode())).intValue();
         if (j.hasNext()) {
            h = inslist.append(h,
               org.apache.bcel.generic.InstructionConstants.ICONST_0);
            h = inslist.append(h,
               new org.apache.bcel.generic.IFNE(handles[dest]));
         } else {
            h = inslist.append(h,
               new org.apache.bcel.generic.GOTO(handles[dest]));
         }
      }
      if (h == handles[self]) {         // if nothing was generated
         h = inslist.append(h,
            org.apache.bcel.generic.InstructionConstants.RETURN);
      }
   }
}



/**
 * Test driver.
 */
public static void main(String[] args) throws java.lang.Exception {
   new SimpleGotoSynthesizer().test(args);
}



} // class SimpleGotoSynthesizer

