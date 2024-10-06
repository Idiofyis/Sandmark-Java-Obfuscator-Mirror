package sandmark.util.newgraph;

class MissingNodeGraph extends MissingGraph {
   private NodeWrapper node;

   MissingNodeGraph(Graph g, NodeWrapper node) {
      super(g);
      this.node = node;
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      if (node == n)
	 return EMPTY_EDGE;
      else
	 return g._inEdges(n);
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      if (node == n)
	 return EMPTY_EDGE;
      else
	 return g._outEdges(n);
   }

   public boolean hasNode(java.lang.Object n) {
      return !node.node.equals(n) && g.hasNode(n);
   }

   public boolean hasEdge(Edge e) {
      return g.hasEdge(e);
   }

   public boolean hasEdge(java.lang.Object from, java.lang.Object to) {
      return g.hasEdge(from, to);
   }

   NodeWrapperIterator _nodes() {
      return new NodeWrapperIterator() {
	    private NodeWrapperIterator i = g._nodes();

	    public NodeWrapper getNext() {
	       if (i == null)
		  return null;
	       else {
		  NodeWrapper rval;
		  do {
		     rval = i.getNext();
		  } while (rval == node);
		  if (rval == null)
		     i = null;
		  return rval;
	       }
	    }
	 };
   }

   EdgeWrapperIterator _edges() {
      return g._edges();
   }

   public int nodeCount() {
      return g.nodeCount() - 1;
   }

   public int edgeCount() {
      return g.edgeCount();
   }

   NodeWrapper getWrapper(java.lang.Object n) {
      if (node.node.equals(n))
	 return null;
      else
	 return g.getWrapper(n);
   }

   EdgeWrapper getEdgeWrapper(Edge e) {
      if (node.node.equals(e.sourceNode()) || node.node.equals(e.sinkNode()))
	 return null;
      else
	 return g.getEdgeWrapper(e);
   }

   int _inDegree(NodeWrapper n) {
      return (node == n) ? 0 : g._inDegree(n);
   }

   int _outDegree(NodeWrapper n) {
      return (node == n) ? 0 : g._outDegree(n);
   }

   public Graph addNode(java.lang.Object n) {
      if (node.node.equals(n))
	 return g;
      else
	 return new MissingNodeGraph(g.addNode(n), node);
   }

   public Graph addEdge(Edge e) {
      if (node.node.equals(e.sourceNode()) || node.node.equals(e.sinkNode()))
	 return g.addEdge(e);
      else
	 return new MissingNodeGraph(g.addEdge(e), node);
   }

   NodeWrapperIterator missingNodes(int sofar) {
      return new ExtraNodeWrapperIterator(g.missingNodes(sofar+1), node);
   }

   EdgeWrapperIterator missingEdges(int sofar) {
      return g.missingEdges(sofar+1);
   }

   Graph missingBase(int sofar) {
      return g.missingBase(sofar+1);
   }
}
