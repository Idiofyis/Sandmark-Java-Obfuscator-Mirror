package sandmark.util.newgraph;

class MissingNodeAttributes extends NodeAttributes {
   public boolean missing;
   public NodeWrapper nw;
}

class MissingNodeIterator implements java.util.Iterator {
   private Object n;
   private java.util.Iterator i;

   MissingNodeIterator(java.util.Iterator i) {
      this.i = i;
      n = getNext();
   }

   private Object getNext() {
      while (i.hasNext()) {
	 MissingNodeAttributes attr = (MissingNodeAttributes)i.next();
	 if (attr.missing)
	    return attr.nw;
      }
      return null;
   }

   public boolean hasNext() {
      return n != null;
   }
   
   public Object next() {
      Object rval = n;
      
      if (rval == null)
	 throw new java.util.NoSuchElementException();
      
      n = getNext();
      return rval;
   }
   
   public void remove() {
      throw new UnsupportedOperationException();
   }
}   

class MissingStuffGraph extends MissingGraph {
   private java.util.Map edges;
   private java.util.Map nodes;
   private int missingNodes, missingThings;

   private class MissingNodeWrapperIterator extends NodeWrapperIterator {
      private NodeWrapperIterator i;

      public MissingNodeWrapperIterator(NodeWrapperIterator i) {
	 this.i = i;
      }

      public NodeWrapper getNext() {
	 if (i == null)
	    return null;

	 NodeWrapper rval;
	 MissingNodeAttributes attr;
	 do {
	    rval = i.getNext();
	    if (rval == null)
	       attr = null;
	    else
	       attr = (MissingNodeAttributes)nodes.get(rval.node);
	 } while (attr != null && attr.missing);
	 if (rval == null)
	    i = null;
	 return rval;
      }
   }

   private class MissingEdgeWrapperIterator extends EdgeWrapperIterator {
      private EdgeWrapperIterator i;
      private int num;

      public MissingEdgeWrapperIterator(EdgeWrapperIterator i,
					int numMissing) {
	 this.i = i;
	 num = i.numEdges() - numMissing;
      }

      public EdgeWrapper getNext() {
	 if (i == null)
	    return null;

	 EdgeWrapper rval;
	 do {
	    rval = i.getNext();
	 } while (rval != null && edges.containsKey(rval.edge));
	 if (rval == null)
	    i = null;
	 return rval;
      }

      public int numEdges() {
	 return num;
      }
   }

   private MissingStuffGraph(Graph g, 
			     java.util.Map nodes, java.util.Map edges,
			     int missingNodes, int missingThings) {
      super(g);
      this.nodes = nodes;
      this.edges = edges;
      this.missingNodes = missingNodes;
      this.missingThings = missingThings;
   }

   MissingStuffGraph(Graph g, NodeWrapperIterator ni, EdgeWrapperIterator ei) {
      this(g, new java.util.HashMap(), null, 0, 0);

      float loadFactor = 0.75f;
      int capacity = (int)(ei.numEdges() / loadFactor) + 1;
      edges = new java.util.HashMap(capacity, loadFactor);

      NodeWrapper nw = ni.getNext();
      missingNodes = 0;
      while (nw != null) {
	 _addNode(nw, true);
	 missingNodes++;
	 nw = ni.getNext();
      }

      EdgeWrapper ew = ei.getNext();
      while (ew != null) {
	 _addEdge(ew);
	 ew = ei.getNext();
      }

      missingThings = edges.size() + missingNodes;
   }

   private MissingNodeAttributes _addNode(NodeWrapper nw, boolean missing) {
      MissingNodeAttributes attr = (MissingNodeAttributes)nodes.get(nw.node);
      if (attr == null) {
	 attr = new MissingNodeAttributes();
	 attr.missing = missing;
	 attr.nw = nw;
	 nodes.put(nw.node, attr);
      }
      return attr;
   }

   private void _addEdge(EdgeWrapper ew) {
      NodeAttributes fromAttr = _addNode(ew.from, false);
      NodeAttributes toAttr = _addNode(ew.to, false);
      fromAttr.addOutEdge(ew);
      toAttr.addInEdge(ew);
      edges.put(ew.edge, ew);
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      MissingNodeAttributes attr = (MissingNodeAttributes)nodes.get(n.node);
      if (attr == null || attr.inEdges == null)
	 return g._inEdges(n);
      else
	 return new MissingEdgeWrapperIterator(g._inEdges(n), 
					       attr.inEdges.size());
   }

   int _inDegree(NodeWrapper n) {
      MissingNodeAttributes attr = (MissingNodeAttributes)nodes.get(n.node);
      if (attr == null || attr.inEdges == null)
	 return g._inDegree(n);
      else
	 return g._inDegree(n) - attr.inEdges.size();
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      MissingNodeAttributes attr = (MissingNodeAttributes)nodes.get(n.node);
      if (attr == null || attr.outEdges == null)
	 return g._outEdges(n);
      else
	 return new MissingEdgeWrapperIterator(g._outEdges(n), 
					       attr.outEdges.size());
   }

   int _outDegree(NodeWrapper n) {
      MissingNodeAttributes attr = (MissingNodeAttributes)nodes.get(n.node);
      if (attr == null || attr.outEdges == null)
	 return g._outDegree(n);
      else
	 return g._outDegree(n) - attr.outEdges.size();
   }

   public boolean hasNode(java.lang.Object n) {
      return getWrapper(n) != null;
   }

   public boolean hasEdge(Edge e) {
      return !edges.containsKey(e) && g.hasEdge(e);
   }

   NodeWrapperIterator _nodes() {
      return new MissingNodeWrapperIterator(g._nodes());
   }

   EdgeWrapperIterator _edges() {
      return new MissingEdgeWrapperIterator(g._edges(), edges.size());
   }

   public int nodeCount() {
      return g.nodeCount() - missingNodes;
   }

   public int edgeCount() {
      return g.edgeCount() - edges.size();
   }

   NodeWrapper getWrapper(java.lang.Object node) {
      MissingNodeAttributes attr = (MissingNodeAttributes)nodes.get(node);
      if (attr == null)
	 return g.getWrapper(node);
      else if (attr.missing)
	 return null;
      else
	 return attr.nw;
   }

   EdgeWrapper getEdgeWrapper(Edge e) {
      if (edges.containsKey(e))
	 return null;
      else
	 return g.getEdgeWrapper(e);
   }

   public Graph addNode(java.lang.Object n) {
      if (hasNode(n))
	 return this;
      else if (nodes.containsKey(n))
         return new ExtraNodeGraph(this, n);
      else
	 return new MissingStuffGraph(g.addNode(n), nodes, edges,
				      missingNodes, missingThings);
   }

   public Graph addEdge(Edge e) {
      if (hasEdge(e))
	 return this;
      else if(edges.containsKey(e))
         return new ExtraEdgeGraph(this,e);
      else
	 return new MissingStuffGraph(g.addEdge(e), nodes, edges,
				      missingNodes, missingThings);
   }

   NodeWrapperIterator missingNodes(int sofar) {
      if (sofar < missingThings)
	 return EMPTY_NODE;

      java.util.Iterator i = 
	 new MissingNodeIterator(nodes.values().iterator());
      NodeWrapperIterator rval =
	 new DoubleNodeWrapperIterator(g.missingNodes(sofar+missingThings), i);
      return rval;
   }

   EdgeWrapperIterator missingEdges(int sofar) {
      if (sofar < missingThings)
	 return EMPTY_EDGE;

      EdgeWrapperIterator my = 
	 new EdgeIteratorWrapper(edges.values().iterator(), edges.size());
      return new DoubleEdgeWrapperIterator(g.missingEdges(sofar+missingThings),
					   my);
   }

   Graph missingBase(int sofar) {
      if (sofar < missingThings)
	 return this;
      else
	 return g.missingBase(sofar+missingThings);
   }
}

