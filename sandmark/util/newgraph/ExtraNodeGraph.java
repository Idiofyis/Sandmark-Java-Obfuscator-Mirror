package sandmark.util.newgraph;

class ExtraNodeGraph extends ExtraGraph {
   private NodeWrapper node;

   ExtraNodeGraph(Graph g, java.lang.Object _node) {
      super(g);
      node = new NodeWrapper(this, _node);
   }

   NodeWrapper getWrapper(java.lang.Object n) {
      if (node.node.equals(n))
	 return node;
      else
	 return g.getWrapper(n);
   }

   EdgeWrapper getEdgeWrapper(Edge e) {
      return g.getEdgeWrapper(e);
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      if (n == node)
	 return EMPTY_EDGE;
      else
	 return g._inEdges(n);
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      if (n == node)
	 return EMPTY_EDGE;
      else
	 return g._outEdges(n);
   }

   public boolean hasNode(java.lang.Object n) {
      return node.node.equals(n) || g.hasNode(n);
   }

   public boolean hasEdge(Edge e) {
      return g.hasEdge(e);
   }

   public boolean hasEdge(java.lang.Object from, java.lang.Object to) {
      return g.hasEdge(from, to);
   }

   NodeWrapperIterator _nodes() {
      return new ExtraNodeWrapperIterator(g._nodes(), node);
   }

   EdgeWrapperIterator _edges() {
      return g._edges();
   }

   public int nodeCount() {
      return g.nodeCount() + 1;
   }

   public int edgeCount() {
      return g.edgeCount();
   }

   int _inDegree(NodeWrapper n) {
      return (node == n) ? 0 : g._inDegree(n);
   }

   int _outDegree(NodeWrapper n) {
      return (node == n) ? 0 : g._outDegree(n);
   }

   NodeWrapperIterator extraNodes(int sofar) {
      return new ExtraNodeWrapperIterator(g.extraNodes(sofar+1), node);
   }

   EdgeWrapperIterator extraEdges(int sofar) {
      return g.extraEdges(sofar+1);
   }

   Graph extraBase(int sofar) {
      return g.extraBase(sofar+1);
   }
}
