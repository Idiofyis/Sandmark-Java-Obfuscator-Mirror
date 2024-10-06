package sandmark.util.newgraph;

class MissingEdgeGraph extends MissingGraph {
   private EdgeWrapper e;

   MissingEdgeGraph(Graph g, EdgeWrapper _e) {
      super(g);
      e = _e;
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      if (n == e.to)
	 return new MissingEdgeWrapperIterator(g._inEdges(n), e);
      else
	 return g._inEdges(n);
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      if (n == e.from)
	 return new MissingEdgeWrapperIterator(g._outEdges(n), e);
      else
	 return g._outEdges(n);
   }

   public boolean hasNode(java.lang.Object n) {
      return g.hasNode(n);
   }

   public boolean hasEdge(Edge _e) {
      return !e.edge.equals(_e) && g.hasEdge(_e);
   }

   NodeWrapperIterator _nodes() {
      return g._nodes();
   }
   
   EdgeWrapperIterator _edges() {
      return new MissingEdgeWrapperIterator(g._edges(), e);
   }

   public int nodeCount() {
      return g.nodeCount();
   }

   public int edgeCount() {
      return g.edgeCount() - 1;
   }

   NodeWrapper getWrapper(java.lang.Object node) {
      return g.getWrapper(node);
   }

   EdgeWrapper getEdgeWrapper(Edge _e) {
      if (e.edge.equals(_e))
	 return null;
      else
	 return g.getEdgeWrapper(_e);
   }

   int _inDegree(NodeWrapper n) {
      if (e.to == n)
	 return g._inDegree(n) - 1;
      else
	 return g._inDegree(n);
   }

   int _outDegree(NodeWrapper n) {
      if (e.from == n)
	 return g._outDegree(n) - 1;
      else
	 return g._outDegree(n);
   }

   public Graph addNode(java.lang.Object n) {
      return new MissingEdgeGraph(g.addNode(n), e);
   }

   public Graph addEdge(Edge edge) {
      if (e.edge.equals(edge))
	 return g;
      else
	 return new MissingEdgeGraph(g.addEdge(edge), e);
   }

   NodeWrapperIterator missingNodes(int sofar) {
      return g.missingNodes(sofar+1);
   }

   EdgeWrapperIterator missingEdges(int sofar) {
      return new ExtraEdgeWrapperIterator(g.missingEdges(sofar+1), e);
   }

   Graph missingBase(int sofar) {
      return g.missingBase(sofar+1);
   }
}
