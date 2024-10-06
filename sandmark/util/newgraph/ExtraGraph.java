package sandmark.util.newgraph;

abstract class ExtraGraph extends RecursiveGraph {
   ExtraGraph(Graph g) {
      super(g);
   }

   public Graph addNode(java.lang.Object n) {
      if (n == null)
	 throw new java.lang.NullPointerException();

      if (hasNode(n))
	 return this;
      else
	 return (new ExtraNodeGraph(this, n)).extraConsolidate(0);
   }

   public Graph addEdge(Edge e) {
      if (e == null)
	 throw new java.lang.NullPointerException();

      if (hasEdge(e))
	 return this;
      else {
	 Graph g = addNode(e.sourceNode()).addNode(e.sinkNode());
	 return (new ExtraEdgeGraph(g, e)).extraConsolidate(0);
      }
   }

   Graph extraConsolidate(int sofar) {
      return new ExtraStuffGraph(extraBase(sofar),
				 extraNodes(sofar), extraEdges(sofar));
   }
}
