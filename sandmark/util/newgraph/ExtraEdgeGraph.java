package sandmark.util.newgraph;

class ExtraEdgeGraph extends ExtraGraph {
   private EdgeWrapper e;

   ExtraEdgeGraph(Graph g, Edge _e) {
      super(g);
      e = new EdgeWrapper(_e, 
			  g.getWrapper(_e.sourceNode()),
			  g.getWrapper(_e.sinkNode()));
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      if (e.to == n)
	 return new ExtraEdgeWrapperIterator(g._inEdges(n), e);
      else
	 return g._inEdges(n);
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      if (e.from == n)
	 return new ExtraEdgeWrapperIterator(g._outEdges(n), e);
      else
	 return g._outEdges(n);
   }

   public boolean hasNode(java.lang.Object n) {
      return g.hasNode(n);
   }

   public boolean hasEdge(Edge _e) {
      return e.edge.equals(_e) || g.hasEdge(_e);
   }

   public boolean hasEdge(java.lang.Object from, java.lang.Object to) {
      return (e.from.node.equals(from) && e.to.node.equals(to)) 
	 || g.hasEdge(from, to);
   }

   NodeWrapperIterator _nodes() {
      return g._nodes();
   }

   EdgeWrapperIterator _edges() {
      return new ExtraEdgeWrapperIterator(g._edges(), e);
   }

   public int nodeCount() {
      return g.nodeCount();
   }

   public int edgeCount() {
      return g.edgeCount() + 1;
   }

   NodeWrapper getWrapper(java.lang.Object n) {
      return g.getWrapper(n);
   }

   EdgeWrapper getEdgeWrapper(Edge _e) {
      if (e.edge.equals(_e))
	 return e;
      else
	 return g.getEdgeWrapper(_e);
   }

   int _inDegree(NodeWrapper n) {
      if (e.to == n)
	 return g._inDegree(n) + 1;
      else
	 return g._inDegree(n);
   }

   int _outDegree(NodeWrapper n) {
      if (e.from == n)
	 return g._outDegree(n) + 1;
      else
	 return g._outDegree(n);
   }

   NodeWrapperIterator extraNodes(int sofar) {
      return g.extraNodes(sofar+1);
   }

   EdgeWrapperIterator extraEdges(int sofar) {
      return new ExtraEdgeWrapperIterator(g.extraEdges(sofar+1), e);
   }

   Graph extraBase(int sofar) {
      return g.extraBase(sofar+1);
   }
}
