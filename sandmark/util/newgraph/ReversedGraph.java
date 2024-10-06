package sandmark.util.newgraph;

class ReversedGraph extends RecursiveGraph {
   private class ReversedEdgeIterator extends EdgeWrapperIterator {
      private EdgeWrapperIterator i;
      private int num;

      public ReversedEdgeIterator(EdgeWrapperIterator ei) {
	 i = ei;
	 num = ei.numEdges();
      }
      
      public EdgeWrapper getNext() {
	 return reverse(i.getNext());
      }

      public int numEdges() {
	 return num;
      }
   }

   private java.util.Map reversedWrappers;
   private java.util.Map wrappers;

   private EdgeWrapper reverse(EdgeWrapper e) {
      if (e == null)
	 return null;
      
      synchronized(reversedWrappers) {
	 EdgeWrapper rval = (EdgeWrapper)reversedWrappers.get(e);
	 if (rval == null) {
	    Edge edge = new EdgeImpl(e.edge.sinkNode(), e.edge.sourceNode());
	    rval = new EdgeWrapper(edge, e.to, e.from);
	    reversedWrappers.put(e, rval);
	    synchronized(wrappers) {
	       wrappers.put(edge, rval);
	    }
	 }
	 return rval;
      }
   }

   ReversedGraph(Graph g) {
      super(g);
      reversedWrappers = new java.util.HashMap();
      wrappers = new java.util.HashMap();
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      return new ReversedEdgeIterator(g._outEdges(n));
   }
   
   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      return new ReversedEdgeIterator(g._inEdges(n));
   }

   public boolean hasNode(java.lang.Object n) {
      return g.hasNode(n);
   }

   public boolean hasEdge(Edge e) {
      synchronized(wrappers) {
	 return wrappers.containsKey(e);
      }
   }

   public boolean hasEdge(java.lang.Object from, java.lang.Object to) {
      return g.hasEdge(to, from);
   }

   boolean reachable(NodeWrapper from, NodeWrapper to) {
      return g.reachable(to, from);
   }

   NodeWrapperIterator _nodes() {
      return g._nodes();
   }
   
   EdgeWrapperIterator _edges() {
      return new ReversedEdgeIterator(g._edges());
   }

   public int nodeCount() {
      return g.nodeCount();
   }
    
   public int edgeCount() {
      return g.edgeCount();
   }
   
   public Graph reverse() {
      return g;
   }

   NodeWrapper getWrapper(java.lang.Object n) {
      return g.getWrapper(n);
   }

   EdgeWrapper getEdgeWrapper(Edge e) {
      synchronized (wrappers) {
	 return (EdgeWrapper)wrappers.get(e);
      }
   }

   int _inDegree(NodeWrapper n) {
      return g._outDegree(n);
   }

   int _outDegree(NodeWrapper n) {
      return g._inDegree(n);
   }
}
