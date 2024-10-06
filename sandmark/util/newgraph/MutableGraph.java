package sandmark.util.newgraph;

abstract class MutableIteratorWrapper implements java.util.Iterator {
   protected java.util.Iterator i;
   protected java.lang.Object last;

   public MutableIteratorWrapper(java.util.Iterator i) {
      this.i = i;
      last = null;
   }

   public boolean hasNext() {
      return i.hasNext();
   }

   public java.lang.Object next() {
      last = i.next();
      return last;
   }

   abstract public void remove();
}

public class MutableGraph {
   private Graph g;
   private java.lang.Object root;
   private String header;

   public MutableGraph() {
      this(null, null);
   }

   public MutableGraph(java.util.Iterator nodeIter,
                       java.util.Iterator edgeIter) {
      g = Graphs.createGraph(nodeIter, edgeIter);
   }

   public MutableGraph(Graph g) {
      this.g = g;
   }

   public void consolidate() {
      g = g.consolidate();
      graphChanged();
   }

   public int depth() {
      return g.depth();
   }

   public void addNode(java.lang.Object n) {
      g = g.addNode(n);
      graphChanged();
   }

   public void removeNode(java.lang.Object n) {
      g = g.removeNode(n);
      graphChanged();
   }

   public void removeAllNodes(java.util.Iterator i) {
      g = g.removeAllNodes(i);
      graphChanged();
   }

   private class EdgeIterator extends MutableIteratorWrapper {
      public EdgeIterator(java.util.Iterator i) {
         super(i);
      }

      public void remove() {
         if (last == null)
            throw new java.lang.IllegalStateException();

         removeEdge((Edge)last);
         last = null;
      }
   }

   public java.util.Iterator inEdges(java.lang.Object n) {
      return new EdgeIterator(g.inEdges(n));
   }

   public java.util.Iterator outEdges(java.lang.Object n) {
      return new EdgeIterator(g.outEdges(n));
   }

   private class NodeIterator extends MutableIteratorWrapper {
      public NodeIterator(java.util.Iterator i) {
         super(i);
      }

      public void remove() {
         if (last == null)
            throw new java.lang.IllegalStateException();

         removeNode(last);
         last = null;
      }
   }

   public java.util.Iterator succs(java.lang.Object n) {
      return new NodeIterator(g.succs(n));
   }

   public java.util.Iterator preds(java.lang.Object n) {
      return new NodeIterator(g.preds(n));
   }

   public java.util.Iterator succs(Object n, java.util.Comparator c) {
      return new NodeIterator(g.succs(n,c));
   }

   public java.util.Iterator preds(Object n, java.util.Comparator c) {
      return new NodeIterator(g.preds(n,c));
   }

   public int inDegree(java.lang.Object n) {
      return g.inDegree(n);
   }

   public int outDegree(java.lang.Object n) {
      return g.outDegree(n);
   }

   public int maxInDegree() {
      return g.maxInDegree();
   }

   public int maxOutDegree() {
      return g.maxOutDegree();
   }

   public int numPreds(Object o) {
      return g.numPreds(o);
   }

   public int numSuccs(Object o) {
      return g.numSuccs(o);
   }

   public void addEdge(Edge e) {
      g = g.addEdge(e);
      graphChanged();
   }

   public void addEdge(java.lang.Object from, java.lang.Object to) {
      g = g.addEdge(from, to);
      graphChanged();
   }

   public void removeEdge(Edge e) {
      g = g.removeEdge(e);
      graphChanged();
   }

   public void removeEdge(java.lang.Object from, java.lang.Object to) {
      g = g.removeEdge(from, to);
      graphChanged();
   }

   public boolean hasNode(java.lang.Object n) {
       return g.hasNode(n);
   }

   public boolean hasEdge(Edge e) {
      return g.hasEdge(e);
   }

   public boolean hasEdge(java.lang.Object from, java.lang.Object to) {
      return g.hasEdge(from, to);
   }
   
   public Edge getFirstEdge(java.lang.Object from, java.lang.Object to) {
      return g.getFirstEdge(from,to);
   }

   public java.util.Iterator nodes() {
      return new NodeIterator(g.nodes());
   }

   public java.util.Iterator edges() {
      return new EdgeIterator(g.edges());
   }

   public int nodeCount() {
      return g.nodeCount();
   }

   public int edgeCount() {
      return g.edgeCount();
   }

   public java.util.Iterator roots() {
      return new NodeIterator(g.roots());
   }

   public java.util.Iterator reverseRoots() {
      return new NodeIterator(g.reverseRoots());
   }

   public Graph graph() {
      return g;
   }

   public java.util.Iterator depthFirst(Object root) {
      return g.depthFirst(root);
   }

   public java.util.Iterator postOrder(Object root) {
      return g.postOrder(root);
   }

   public java.util.Iterator breadthFirst(Object root) {
      return g.breadthFirst(root);
   }

   public void removeUnreachable(Object root) {
      g = g.removeUnreachable(root);
      graphChanged();
   }

   public Graph depthFirstTree(Object root) {
      return g.depthFirstTree(root);
   }

   public boolean reachable(java.lang.Object from, java.lang.Object to) {
      return g.reachable(from, to);
   }

   public void inducedSubgraph(java.util.Iterator i) {
      g = g.inducedSubgraph(i);
      graphChanged();
   }

   public java.lang.Object getRoot() {
      if (root == null) {
         java.util.Iterator i = g.roots();
         if (i.hasNext())
            return i.next();
      }

      return root;
   }

   public void setRoot(java.lang.Object root) {
      this.root = root;
   }

   public MutableGraph copy() {
      MutableGraph rval = new MutableGraph(g);
      rval.setRoot(root);
      return rval;
   }

   public String getHeader() {
      return header;
   }

   public void setHeader(String h) {
      header = h;
   }

   public DomTree dominatorTree(java.lang.Object root) {
      return g.dominatorTree(root);
   }

   public void graphChanged() {
       //java.util.Iterator listeners = myListeners.iterator();
       //while(listeners.hasNext()){
       //    sandmark.util.newgraph.GraphListener listener =
       //        (sandmark.util.newgraph.GraphListener)listeners.next();
       //    listener.graphChanged(this);
       //}
   }

    //private java.util.LinkedList myListeners = new java.util.LinkedList();
    public void addGraphListener(sandmark.util.newgraph.GraphListener listener){
        //myListeners.add(listener);
    }
}
