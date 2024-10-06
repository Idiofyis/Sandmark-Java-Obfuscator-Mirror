package sandmark.util.newgraph;

abstract class MissingGraph extends RecursiveGraph {
   MissingGraph(Graph g) {
      super(g);
   }

   public abstract Graph addNode(java.lang.Object n);
   public abstract Graph addEdge(Edge e);

   abstract NodeWrapperIterator missingNodes(int sofar);
   abstract EdgeWrapperIterator missingEdges(int sofar);
   abstract Graph missingBase(int sofar);

   public Graph removeNode(java.lang.Object n) {
      NodeWrapper nw = getWrapper(n);
      if (nw == null)
	 return this;
      else {
	 Graph g = this;
	 EdgeWrapperIterator i = g._outEdges(nw);
	 EdgeWrapper ew = i.getNext();
	 while (ew != null) {
	    g = g.removeEdge(ew.edge);
	    ew = i.getNext();
	 }
	 i = g._inEdges(nw);
	 ew = i.getNext();
	 while (ew != null) {
	    g = g.removeEdge(ew.edge);
	    ew = i.getNext();
	 }
	 return (new MissingNodeGraph(g, nw)).missingConsolidate(0);
      }
   }

   public Graph removeEdge(Edge e) {
      EdgeWrapper ew = getEdgeWrapper(e);
      if (ew == null)
	 return this;
      else
	 return (new MissingEdgeGraph(this, ew)).missingConsolidate(0);
   }

   Graph missingConsolidate(int sofar) {
      return new MissingStuffGraph(missingBase(sofar),
				   missingNodes(sofar), missingEdges(sofar));
   }
}
