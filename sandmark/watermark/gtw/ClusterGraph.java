package sandmark.watermark.gtw;

public abstract class ClusterGraph extends sandmark.util.newgraph.MutableGraph {
   public static boolean DEBUG = false;
   int APP = 0;
   int WMARK = 1;
   public void randomlyWalkAddingEdges(java.util.ArrayList appNodes,
				       java.util.ArrayList wmarkNodes,
				       int crossEdgeCount) {
       java.util.Random rnd = sandmark.util.Random.getRandom();
      java.util.ArrayList allNodes = new java.util.ArrayList(appNodes);
      allNodes.addAll(wmarkNodes);
      java.util.Collections.sort(allNodes,new CFGComparator());
      int crossEdgesAdded = 0,edgesAdded = 0;
      Object currentNode = 
	  allNodes.get(((rnd.nextInt() % allNodes.size()) + 
			allNodes.size()) % allNodes.size());
      int currentNodeType = appNodes.contains(currentNode) ? APP : WMARK;
      while(crossEdgesAdded < crossEdgeCount || 
	    containsDisconnectedNode(wmarkNodes)) {
	  if(DEBUG && edgesAdded % 10 == 0)
	      System.out.println
		  ("(edges added,cross edges added,cross edges required = (" +
		   edgesAdded + "," + crossEdgesAdded + "," + 
		   crossEdgeCount + ")");
	  Object nextNode = 
	      allNodes.get(((rnd.nextInt() % allNodes.size()) + 
			    allNodes.size()) % allNodes.size());
          if(nextNode == currentNode)
             continue;
	  if(DEBUG)
	      System.out.println
		  ("nextNode: " + 
		   sandmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName
		   ((sandmark.analysis.controlflowgraph.MethodCFG)nextNode));
	  int nextNodeType = appNodes.contains(nextNode) ? APP : WMARK;
	  if(isLegalEdge(currentNode,currentNodeType,nextNode,nextNodeType)) {
	      if(currentNodeType != nextNodeType)
		  crossEdgesAdded++;
	      edgesAdded++;
	      synthesizeEdge(currentNode,currentNodeType,nextNode,nextNodeType);
	  }
	  currentNode = nextNode;
	  currentNodeType = nextNodeType;
      }
      if(DEBUG && crossEdgesAdded != crossEdgeCount)
	 System.out.println("crossEdgesAdded != crossEdgeCount:  " + 
			    crossEdgesAdded + " != " + crossEdgeCount);
   }
   private boolean containsDisconnectedNode(java.util.ArrayList nodes) {
      for(java.util.Iterator it = nodes.iterator() ; it.hasNext() ; )
	 if(!succs(it.next()).hasNext())
	    return true;
      return false;
   }
   protected abstract void synthesizeEdge(Object node1,int node1Type,
					  Object node2,int node2Type);
   protected abstract boolean isLegalEdge(Object node1,int node1Type,
					  Object node2,int node2Type);
}

class CFGComparator implements java.util.Comparator {
   public int compare(Object o1,Object o2) {
      String o1Name = 
	  sandmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName
	 ((sandmark.analysis.controlflowgraph.MethodCFG)o1);
      String o2Name = 
	  sandmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName
	 ((sandmark.analysis.controlflowgraph.MethodCFG)o2);
      return o1Name.compareTo(o2Name);
   }
}

