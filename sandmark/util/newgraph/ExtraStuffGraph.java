package sandmark.util.newgraph;

class ExtraNodeAttributes extends NodeAttributes {
   public boolean extra;
   public NodeWrapper nw;
}

class ExtraNodeIterator implements java.util.Iterator {
   private Object n;
   private java.util.Iterator i;

   ExtraNodeIterator(java.util.Iterator i) {
      this.i = i;
      n = getNext();
   }

   private Object getNext() {
      while (i.hasNext()) {
	 ExtraNodeAttributes attr = (ExtraNodeAttributes)i.next();
	 if (attr.extra)
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

class ExtraStuffGraph extends ExtraGraph {
   private java.util.Map edges;
   private java.util.Map nodes;
   private int extraNodes, extraThings;

   ExtraStuffGraph(Graph g, NodeWrapperIterator ni, EdgeWrapperIterator ei) {
      super(g);
      nodes = new java.util.HashMap();

      float loadFactor = 0.625f;
      int capacity = (int)(ei.numEdges() / loadFactor) + 1;
      edges = new java.util.HashMap(capacity, loadFactor);

      NodeWrapper nw = ni.getNext();
      extraNodes = 0;
      while (nw != null) {
	 _addNode(nw, true);
	 extraNodes++;
	 nw = ni.getNext();
      }

      EdgeWrapper ew = ei.getNext();
      while (ew != null) {
	 _addEdge(ew);
	 ew = ei.getNext();
      }

      extraThings = edges.size() + extraNodes;
   }

   private ExtraNodeAttributes _addNode(NodeWrapper nw, boolean extra) {
      ExtraNodeAttributes attr = (ExtraNodeAttributes)nodes.get(nw.node);
      if (attr == null) {
	 attr = new ExtraNodeAttributes();
	 attr.extra = extra;
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
      ExtraNodeAttributes attr = (ExtraNodeAttributes)nodes.get(n.node);
      if (attr != null && attr.inEdges != null) {
	 if (attr.extra)
	    return attr.inEdges.iterator();
	 else
	    return new DoubleEdgeWrapperIterator(attr.inEdges.iterator(),
						 g._inEdges(n));
      }
      else {
	 if (attr != null && attr.extra)
	    return EMPTY_EDGE;
	 else
	    return g._inEdges(n);
      }
   }

   int _inDegree(NodeWrapper n) {
      ExtraNodeAttributes attr = (ExtraNodeAttributes)nodes.get(n.node);
      if (attr != null && attr.inEdges != null) {
	 if (attr.extra)
	    return attr.inEdges.size();
	 else
	    return g._inDegree(n) + attr.inEdges.size();
      }
      else {
	 if (attr != null && attr.extra)
	    return 0;
	 else
	    return g._inDegree(n);
      }
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      ExtraNodeAttributes attr = (ExtraNodeAttributes)nodes.get(n.node);
      if (attr != null && attr.outEdges != null) {
	 if (attr.extra)
	    return attr.outEdges.iterator();
	 else
	    return new DoubleEdgeWrapperIterator(attr.outEdges.iterator(),
						 g._outEdges(n));
      }
      else {
	 if (attr != null && attr.extra)
	    return EMPTY_EDGE;
	 else
	    return g._outEdges(n);
      }
   }

   int _outDegree(NodeWrapper n) {
      ExtraNodeAttributes attr = (ExtraNodeAttributes)nodes.get(n.node);
      if (attr != null && attr.outEdges != null) {
	 if (attr.extra)
	    return attr.outEdges.size();
	 else
	    return g._outDegree(n) + attr.outEdges.size();
      }
      else {
	 if (attr != null && attr.extra)
	    return 0;
	 else
	    return g._outDegree(n);
      }
   }

   public boolean hasNode(java.lang.Object n) {
      return nodes.containsKey(n) || g.hasNode(n);
   }

   public boolean hasEdge(Edge e) {
      return edges.containsKey(e) || g.hasEdge(e);
   }

   NodeWrapperIterator _nodes() {
      java.util.Iterator i = new ExtraNodeIterator(nodes.values().iterator());
      return new DoubleNodeWrapperIterator(g._nodes(), i);
   }

   EdgeWrapperIterator _edges() {
      EdgeWrapperIterator my = 
	 new EdgeIteratorWrapper(edges.values().iterator(), edges.size());
      return new DoubleEdgeWrapperIterator(g._edges(), my);
   }

   public int nodeCount() {
      return g.nodeCount() + extraNodes;
   }

   public int edgeCount() {
      return g.edgeCount() + edges.size();
   }

   NodeWrapper getWrapper(java.lang.Object node) {
      ExtraNodeAttributes attr = (ExtraNodeAttributes)nodes.get(node);
      if (attr == null || !attr.extra) {
	 NodeWrapper rval = g.getWrapper(node);
// 	 if (rval == null)
// 	    throw new NullPointerException("node = " + node
// 					   + ", nodes = " + nodes);
	 return rval;
      }
      else
	 return attr.nw;
   }

   EdgeWrapper getEdgeWrapper(Edge e) {
      EdgeWrapper rval = (EdgeWrapper)edges.get(e);
      if (rval == null)
	 return g.getEdgeWrapper(e);
      else
	 return rval;
   }

   NodeWrapperIterator extraNodes(int sofar) {
      if (sofar < extraThings)
	 return EMPTY_NODE;

      java.util.Iterator i = new ExtraNodeIterator(nodes.values().iterator());
      return new DoubleNodeWrapperIterator(g.extraNodes(sofar+extraThings), i);
   }

   EdgeWrapperIterator extraEdges(int sofar) {
      if (sofar < extraThings)
	 return EMPTY_EDGE;

      EdgeWrapperIterator my = 
	 new EdgeIteratorWrapper(edges.values().iterator(), edges.size());
      return new DoubleEdgeWrapperIterator(g.extraEdges(sofar+extraThings),
					   my);
   }

   Graph extraBase(int sofar) {
      if (sofar < extraThings)
	 return this;
      else
	 return g.extraBase(sofar+extraThings);
   }
}

