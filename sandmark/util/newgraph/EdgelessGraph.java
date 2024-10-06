package sandmark.util.newgraph;

class EdgelessGraph extends Graph {
   private java.util.Map nodes;

   private EdgelessGraph() {
      nodes = new java.util.HashMap();
   }

   EdgelessGraph(java.util.Iterator i) {
      this();
      while (i.hasNext()) {
	 java.lang.Object n = i.next();
	 if (!nodes.containsKey(n))
	    nodes.put(n, new NodeWrapper(this, n));
      }
   }

   public Graph consolidate() {
      return this;
   }

   public int depth() {
      return 0;
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      return EMPTY_EDGE;
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      return EMPTY_EDGE;
   }

   public java.util.Iterator succs(java.lang.Object n) {
      return EMPTY_ITER;
   }

   public java.util.Iterator preds(java.lang.Object n) {
      return EMPTY_ITER;
   }

   public int inDegree(java.lang.Object n) {
      return 0;
   }

   public int outDegree(java.lang.Object n) {
      return 0;
   }

   public int maxInDegree() {
      return 0;
   }

   public int maxOutDegree() {
      return 0;
   }

   public Graph removeEdge(Edge e) {
      return this;
   }

   public Graph removeEdge(java.lang.Object from, java.lang.Object to) {
      return this;
   }

   public boolean hasNode(java.lang.Object n) {
      return nodes.containsKey(n);
   }

   public boolean hasEdge(Edge e) {
      return false;
   }

   public boolean hasEdge(java.lang.Object from, java.lang.Object to) {
      return false;
   }

   NodeWrapperIterator _nodes() {
      return new NodeWrapperIterator() {
	    private java.util.Iterator i = nodes.values().iterator();

	    public NodeWrapper getNext() {
	       if (i == null)
		  return null;
	       else {
		  NodeWrapper rval = null;
		  if (i.hasNext())
		     rval = (NodeWrapper)i.next();
		  if (rval == null)
		     i = null;
		  return rval;
	       }
	    }
	 };
   }

   EdgeWrapperIterator _edges() {
      return EMPTY_EDGE;
   }

   public int nodeCount() {
      return nodes.size();
   }

   public int edgeCount() {
      return 0;
   }

   NodeWrapperIterator _roots() {
      return _nodes();
   }

   NodeWrapperIterator _reverseRoots() {
      return _nodes();
   }

   NodeWrapperIterator _depthFirst(NodeWrapper root) {
      return new SingleNodeWrapperIterator(root);
   }

   NodeWrapperIterator _breadthFirst(NodeWrapper root) {
      return _depthFirst(root);
   }

   public Graph removeUnreachable(java.lang.Object root) {
      return removeNode(root);
   }
   
   public Graph reverse() {
      return this;
   }

   NodeWrapper getWrapper(java.lang.Object node) {
      return (NodeWrapper)nodes.get(node);
   }

   EdgeWrapper getEdgeWrapper(Edge e) {
      return null;
   }

   int _inDegree(NodeWrapper n) {
      return 0;
   }

   int _outDegree(NodeWrapper n) {
      return 0;
   }
}
