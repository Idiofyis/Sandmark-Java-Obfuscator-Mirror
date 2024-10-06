package sandmark.util.newgraph;

class EmptyGraph extends Graph {
   public Graph consolidate() {
      return this;
   }

   public int depth() {
      return 0;
   }

   public Graph removeNode(java.lang.Object n) {
      return this;
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
      return false;
   }

   public boolean hasEdge(Edge e) {
      return false;
   }

   public boolean hasEdge(java.lang.Object from, java.lang.Object to) {
      return false;
   }

   NodeWrapperIterator _nodes() {
      return EMPTY_NODE;
   }

   EdgeWrapperIterator _edges() {
      return EMPTY_EDGE;
   }

   public int nodeCount() {
      return 0;
   }

   public int edgeCount() {
      return 0;
   }

   NodeWrapperIterator _roots() {
      return EMPTY_NODE;
   }

   NodeWrapperIterator _reverseRoots() {
      return EMPTY_NODE;
   }

   public Graph removeUnreachable(java.lang.Object root) {
      return this;
   }

   public Graph reverse() {
      return this;
   }

   NodeWrapper getWrapper(java.lang.Object node) {
      return null;
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
