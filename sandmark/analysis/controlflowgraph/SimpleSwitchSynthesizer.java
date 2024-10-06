package sandmark.analysis.controlflowgraph;



/**
 * A simple code generator using TABLESWITCH instructions.
 * A leaf node produces a RETURN.
 * The resulting code is obviously artificial.
 */
public class SimpleSwitchSynthesizer extends VoidMethodSynthesizer {



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

   genNodes(inslist, handles, graph, root, nodeNumbers);


   inslist.setPositions(true);
   sandmark.program.Method method = makeMethod(inslist,clazz);
   method.removeNOPs();
   return method;
}


/**
 *  Generates the code for each node in turn.
 */
private void genNodes(
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
      java.util.Vector v = new java.util.Vector();
      for (java.util.Iterator j = graph.outEdges(node); j.hasNext(); ) {
         sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge)j.next();
         int dest = ((Integer)nodeNumbers.get(e.sinkNode())).intValue();
         v.add(handles[dest]);
      }
      h = genSwitch(inslist, h, v);
   }
}



/**
 *  Generates a TABLESWITCH instruction given a Vector of InstructionHandles.
 *  If the Vector is empty, generates a RETURN instead.
 */
private org.apache.bcel.generic.InstructionHandle genSwitch(
      org.apache.bcel.generic.InstructionList inslist,
      org.apache.bcel.generic.InstructionHandle h,
      java.util.Vector v) {

   if (v.size() == 0) {
      return inslist.append(h,
         org.apache.bcel.generic.InstructionConstants.RETURN);
   }

   h = inslist.append(h, org.apache.bcel.generic.InstructionConstants.ICONST_0);

   org.apache.bcel.generic.InstructionHandle defjump =
      (org.apache.bcel.generic.InstructionHandle) v.elementAt(0);
   if (v.size() > 1) {
      v.removeElementAt(0);
   }

   org.apache.bcel.generic.InstructionHandle targets[] =
      new org.apache.bcel.generic.InstructionHandle[v.size()];
   v.toArray(targets);

   int[] m = new int[targets.length];
   for (int i = 0; i < targets.length; i++) {
      m[i] = i + 1;
   }

   return inslist.append(h,
      new org.apache.bcel.generic.TABLESWITCH(m, targets, defjump));
}



/**
 * Test driver.
 */
public static void main(String[] args) throws java.lang.Exception {
   new SimpleSwitchSynthesizer().test(args);
}



} // class SimpleSwitchSynthesizer

