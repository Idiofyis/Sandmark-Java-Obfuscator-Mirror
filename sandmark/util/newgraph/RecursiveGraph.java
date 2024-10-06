package sandmark.util.newgraph;

abstract class RecursiveGraph extends Graph {
   Graph g;
   private Graph consolidated;

   RecursiveGraph(Graph innerGraph) {
      g = innerGraph;
      consolidated = null;
   }

   public int depth() {
      return g.depth() + 1;
   }
   
   public Graph consolidate() {
      if (consolidated == null)
	 synchronized(this) {
	    if (consolidated == null)
	       consolidated = Graphs.createGraph(nodes(), edges());
	 }
      return consolidated;
   }
}
