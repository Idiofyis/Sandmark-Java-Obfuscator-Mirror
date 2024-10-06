package sandmark.metric;

/** This class builds the CFG, determines selection nodes
 *  and calculates the Halstead and Nesting Level complexities.
 *  For each node, first we calculate the Halstead's measure. Then
 *  for each node X which has nodes {p1, p2, .. pn} in its "scope",
 *  the measure(X) = measure(X) + measures(p1+p2+...+pn)
 *  The overall nesting level complexity is the sum of measures of
 *  all nodes in the method CFG.
 */
public class NestingLevelComplexity
{
   java.util.ArrayList myNodes;

   private class ComplexityNode implements Comparable
   {
      sandmark.analysis.controlflowgraph.BasicBlock myBlock;
      double rawComplexity;
      double adjComplexity;
      boolean selection;
      int startLabel;
      int endLabel;

      /* Constructs a new ComplexityNode which is nothing but
       *  a BasicBlock that includes some extra properties given above
       */
      ComplexityNode(sandmark.analysis.controlflowgraph.BasicBlock bb)
      {
         myBlock = bb;
         rawComplexity = 0;
         adjComplexity = 0;
         selection = false;
         startLabel = -1;
         endLabel = -1;
      }

      public int compareTo(Object obj){
         return startLabel-((ComplexityNode)obj).startLabel;
      }
   }

   /*  Builds the cfg for the given method, forms a set of
    *  ComplexityNodes, one for each basic block and marks
    *  the selection nodes
    *  @param mg the BCEL method object
    */
   private void buildCFG(sandmark.program.Method mg)
   {
      myNodes = new java.util.ArrayList();

      if(mg.getInstructionList() == null)
         return;

      mg.getInstructionList().setPositions();
      sandmark.analysis.controlflowgraph.MethodCFG cfg = mg.getCFG(false);

      for (java.util.Iterator bbiter=cfg.basicBlockIterator();bbiter.hasNext();){
         sandmark.analysis.controlflowgraph.BasicBlock bb =
            (sandmark.analysis.controlflowgraph.BasicBlock)bbiter.next();

         java.util.ArrayList bbInstrList = bb.getInstList();
         if (bbInstrList.size()==0)
            continue;

         ComplexityNode cn = new ComplexityNode(bb);
         cn.selection = (cfg.numSuccs(bb) >= 2);
         cn.startLabel = ((org.apache.bcel.generic.InstructionHandle)bbInstrList.get(bbInstrList.size()-1)).getPosition();
         cn.endLabel = bb.getIH().getPosition();

         myNodes.add(cn);
      }

      java.util.Collections.sort(myNodes);
   }

   /* Calculates the Halstead metric for the list of instructions in
    *  each basic block. This method obtains the list of BLOAT instructions
    *  for each basic block using the 'ordered' basic block list of
    *  BCEL instructions.
    *  @param me the BLOAT method object
    */
   private void getRawComplexities(sandmark.program.Method me)
   {
      org.apache.bcel.generic.InstructionList instrlist = me.getInstructionList();
      if(instrlist==null)
         return;

      /*
      HalsteadMethodLengthMeasure hm = (HalsteadMethodLengthMeasure)
         HalsteadMethodLengthMeasure.getInstance();
      int measure = hm.getMeasure(me);
      */

      for (int i=0; i<myNodes.size(); i++){
         ComplexityNode cn = (ComplexityNode)myNodes.get(i);

         HalsteadUtil util = new HalsteadUtil(me);
         java.util.Vector utilvector = util.evalMeasures(cn.myBlock.getInstList().iterator());
         if (utilvector==null)
            cn.rawComplexity = 0;
         else
            cn.rawComplexity = 
               ((Integer)utilvector.get(0)).intValue() + 
               ((Integer)utilvector.get(2)).intValue();
      }
   }

   /* Evaluates the adjusted complexities for each basic block
    *  for selection nodes, adjusted complexity is the sum of raw
    *  complexities of all the nodes that lie in the range of a
    *  selection node, ie. nodes that lie between the selection node
    *  itself and the glb of its immediate successors.
    *  For other nodes, adj complexity = raw complexity
    */
   private void getAdjComplexities()
   {
      for (int i=0; i<myNodes.size(); i++){
         ComplexityNode cn = (ComplexityNode)myNodes.get(i);
         cn.adjComplexity = cn.rawComplexity;
         if (cn.selection == false)
            continue;
         
         // Get immediate successors of the selection node
         java.util.Iterator iter = cn.myBlock.graph().succs(cn.myBlock);
         java.util.List succNodes = new java.util.ArrayList();
         while (iter.hasNext())
            succNodes.add(iter.next());
         
         // Find glb of the succ nodes
         java.util.ArrayList lbList = getLBList(succNodes);
         
         if(lbList.size() == 0)
            continue;
         
         // Build lb graph and get the greatest lower bound
         sandmark.analysis.controlflowgraph.BasicBlock glbNode = getGLB(lbList);
         double complexity = 0;
        if(glbNode!=null) {
            // Find the nodes between the selection node
            // and the greatest lower bound
            sandmark.analysis.controlflowgraph.BasicBlock selNode =
               (sandmark.analysis.controlflowgraph.BasicBlock) cn.myBlock;
            java.util.ArrayList rangeNodes = getRangeNodes(selNode, glbNode);
            
            // Add complexities
            for (int n=0;n<myNodes.size();n++){
               ComplexityNode cNode = (ComplexityNode)myNodes.get(n);
               if (rangeNodes.contains(cNode.myBlock))
                  complexity += cNode.rawComplexity;
            }
         }
         cn.adjComplexity += complexity;
      }
   }

   /*  Gets the list of lower bound nodes of the subgraph formed of
    *  of the successor nodes of the selection node.
    *  A 'lower bound' node is one that can be reached by the entire set of nodes.
    *  @param succNodes the list of nodes whose lower bounds is to be obtained
    *  @return lbList the list of lower bound nodes
    */
   private java.util.ArrayList getLBList(java.util.List succNodes)
   {
      boolean lb;
      java.util.ArrayList lbList = new java.util.ArrayList();
      for (int j=0; j<myNodes.size(); j++){
         ComplexityNode cNode = (ComplexityNode) myNodes.get(j);
         sandmark.analysis.controlflowgraph.BasicBlock gNode =
            (sandmark.analysis.controlflowgraph.BasicBlock) cNode.myBlock;
         
         lb = true;
         for (int k=0; k<succNodes.size(); k++){
            sandmark.analysis.controlflowgraph.BasicBlock succNode =
               (sandmark.analysis.controlflowgraph.BasicBlock) succNodes.get(k);
            
            if (!isSuccessor(gNode, succNode)){
               lb = false;
               break;
            }
         }
         
         if (lb)
            lbList.add(gNode);
      }
      return lbList;
   }


   /* Checks whether a given BasicBlock is a successor of another
    * ie. whether there exists some path from startNode to endNode
    * @param endNode
    * @param startNode
    */
   private boolean isSuccessor(sandmark.analysis.controlflowgraph.BasicBlock endNode,
                               sandmark.analysis.controlflowgraph.BasicBlock startNode)
   {
      if (startNode==endNode){
         for (java.util.Iterator edges=startNode.graph().outEdges(startNode);edges.hasNext();){
            sandmark.util.newgraph.Edge edge = (sandmark.util.newgraph.Edge)edges.next();
            if (edge.sinkNode()==startNode)
               return true;
            else if (startNode.graph().reachable(edge.sinkNode(), startNode))
               return true;
         }
         return false;
      }else 
         return startNode.graph().reachable(startNode, endNode);
   }
   
   
   /*  Builds a lower bound graph with the lower bound nodes and returns
    *  the root of that graph as the GLB
    *  @param inList the list of lower bound nodes
    *  @return glb greatest lower bound node
    */
   private sandmark.analysis.controlflowgraph.BasicBlock getGLB(java.util.ArrayList inList)
   {
      if (inList.size() == 1)
         return (sandmark.analysis.controlflowgraph.BasicBlock)inList.get(0);

      // build LB Graph
      sandmark.util.newgraph.MutableGraph lbGraph =
         new sandmark.util.newgraph.MutableGraph();

      // Add edges to the lb Graph
      for (int i=0; i<inList.size(); i++){
         sandmark.analysis.controlflowgraph.BasicBlock srcNode =
            (sandmark.analysis.controlflowgraph.BasicBlock) inList.get(i);
         sandmark.analysis.controlflowgraph.MethodCFG cfg = srcNode.graph();

         for (int j=0; j<inList.size(); j++){
            Object destNode = inList.get(j);
            
            if (cfg.hasEdge(srcNode,destNode))
               lbGraph.addEdge(srcNode, destNode);
         }
      }
      
      // Find the root
      java.util.Iterator rootIter = lbGraph.roots();
      if(rootIter.hasNext())
         return (sandmark.analysis.controlflowgraph.BasicBlock)rootIter.next();
      return null;
   }


   /* Gets the list of nodes between the selection node and the computed
    *  greatest lower bound node.
    *  @param selNode the BasicBlock object - selection node
    *  @param glbNode the BasicBlock object - glb node
    *  @return rangeList list of BasicBlock objects
    */
   private java.util.ArrayList getRangeNodes(sandmark.analysis.controlflowgraph.BasicBlock selNode,
                                             sandmark.analysis.controlflowgraph.BasicBlock glbNode)
   {
      java.util.ArrayList rangeList = new java.util.ArrayList();
      java.util.LinkedList worklist = new java.util.LinkedList();
      worklist.add(selNode);

      sandmark.util.newgraph.MutableGraph cfg = selNode.graph();
      while (!worklist.isEmpty()){
         Object worklistNode = worklist.removeFirst();
         if (worklistNode == glbNode)
            continue;
         for (java.util.Iterator succs=cfg.succs(worklistNode);succs.hasNext();){
            sandmark.analysis.controlflowgraph.BasicBlock succNode =
               (sandmark.analysis.controlflowgraph.BasicBlock)succs.next();

            if ((succNode != null) && (succNode != glbNode) && !rangeList.contains(succNode)) {
               rangeList.add(succNode);
               worklist.addLast(succNode);
            }
         }
      }
      return(rangeList);
   }


   /* Evaluates the Harrison-Magel metric that is based on the Halstead
    *  metrics and the nesting level of program constructs.
    *  @param mg the BCEL method object
    *  @return the calculated metric
    */
   public double evalMeasures(sandmark.program.Method mg)
   {
      // BUILD THE CFG OF THE METHOD AND MARK SELECTION NODES
      buildCFG(mg);

      // GET THE HALSTEAD MEASURES FOR EACH BLOCK (RAW COMPLEXITY)
      getRawComplexities(mg);

      // GET THE ADJUSTED COMPLEXITIES FOR EACH BLOCK
      getAdjComplexities();

      // GET THE TOTAL COMPLEXITY
      double totalComplexity = 0;
      for (int i=0; i<myNodes.size(); i++){
         ComplexityNode cn = (ComplexityNode) myNodes.get(i);
         totalComplexity += cn.adjComplexity;
      }

      return totalComplexity;
   }
}

