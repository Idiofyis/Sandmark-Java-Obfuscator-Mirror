package sandmark.util.newgraph;

class ImplNodeAttributes extends NodeAttributes {
   public ImplNodeWrapper nw;
   private Status st;

   public ImplNodeAttributes(Status s) {
      st = s;
   }

   protected EdgeSet createEdgeSet() {
      return new StatusEdgeSet(st);
   }
}

class ImplNodeWrapper extends NodeWrapper {
   ImplNodeWrapper(Graph g, java.lang.Object n) {
      super(g, n);
   }

   ImplNodeAttributes data;
}

public class GraphImpl extends Graph {
   private java.util.Map nodes;
   private java.util.Map edges;
   private Status s;

   protected GraphImpl() {
      nodes = new java.util.HashMap();
      edges = new java.util.HashMap();
      s = new Status();
   }

   protected GraphImpl(java.util.Iterator nodeIterator,
		       java.util.Iterator edgeIterator) {
      this();
      if (nodeIterator != null)
	 while (nodeIterator.hasNext())
	    _addNode(nodeIterator.next());
      if (edgeIterator != null)
	 while (edgeIterator.hasNext())
	    _addEdge((Edge)edgeIterator.next());
   }

   private ImplNodeAttributes __addNode(java.lang.Object n) {
      if (n == null)
	 throw new java.lang.NullPointerException();

      ImplNodeAttributes attr = (ImplNodeAttributes)nodes.get(n);

      if (attr == null) {
	 attr = new ImplNodeAttributes(s);
	 attr.nw = new ImplNodeWrapper(this, n);
	 attr.nw.data = attr;
	 nodes.put(n, attr);
      }

      return attr;
   }

   protected final void _addNode(java.lang.Object n) {
      synchronized(s) {
	 s.checkNotAccessed();
	 __addNode(n);
      }
   }

   protected final void _addEdge(Edge e) {
      synchronized(s) {
	 s.checkNotAccessed();
	 
	 if (e == null)
	    throw new java.lang.NullPointerException();

	 ImplNodeAttributes fromAttr = __addNode(e.sourceNode());
	 ImplNodeAttributes toAttr = __addNode(e.sinkNode());
	 EdgeWrapper ew = new EdgeWrapper(e, fromAttr.nw, toAttr.nw);
	 fromAttr.addOutEdge(ew);
	 toAttr.addInEdge(ew);
	 edges.put(e, ew);
      }
   }

   public Graph consolidate() {
      // if the user accesses the consolidated graph, we will call
      // s.setAccessed() anyway, so it's not necessary here
      return this;
   }

   public int depth() {
      // calls to _addNode() and _addEdge() don't change this value, so
      // a call to s.setAccessed() isn't necessary
      return 0;
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      s.setAccessed();
      ImplNodeAttributes attr = ((ImplNodeWrapper)n).data;
      if (attr.inEdges != null)
	 return attr.inEdges.iterator();
      else
	 return EMPTY_EDGE;
   }

   int _inDegree(NodeWrapper n) {
      s.setAccessed();
      ImplNodeAttributes attr = ((ImplNodeWrapper)n).data;
      if (attr.inEdges != null)
	 return attr.inEdges.size();
      else
	 return 0;
   }

   NodeWrapperIterator _preds(NodeWrapper n) {
      ImplNodeAttributes attr = ((ImplNodeWrapper)n).data;
      if (attr.inEdges != null)
	 return attr.inEdges.sourceIterator();
      else
	 return EMPTY_NODE;
   }

   NodeWrapperIterator _succs(NodeWrapper n) {
      ImplNodeAttributes attr = ((ImplNodeWrapper)n).data;
      if (attr.outEdges != null)
	 return attr.outEdges.sinkIterator();
      else
	 return EMPTY_NODE;
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      s.setAccessed();
      ImplNodeAttributes attr = ((ImplNodeWrapper)n).data;
      if (attr.outEdges != null)
	 return attr.outEdges.iterator();
      else
	 return EMPTY_EDGE;
   }

   int _outDegree(NodeWrapper n) {
      s.setAccessed();
      ImplNodeAttributes attr = ((ImplNodeWrapper)n).data;
      if (attr.outEdges != null)
	 return attr.outEdges.size();
      else
	 return 0;
   }

   public boolean hasNode(java.lang.Object n) {
      s.setAccessed();
      return nodes.containsKey(n);
   }

   public boolean hasEdge(Edge e) {
      s.setAccessed();
      return edges.containsKey(e);
   }

   NodeWrapperIterator _nodes() {
      s.setAccessed();
      return new NodeWrapperIterator() {
	    java.util.Iterator i = nodes.values().iterator();

	    public NodeWrapper getNext() {
	       if (i == null)
		  return null;
	       else {
		  NodeWrapper rval = null;
		  if (i.hasNext()) {
		     ImplNodeAttributes attr = (ImplNodeAttributes)i.next();
		     rval = attr.nw;
		  }
		  if (rval == null)
		     i = null;
		  return rval;
	       }
	    }
	 };
   }

   EdgeWrapperIterator _edges() {
      s.setAccessed();
      return new EdgeIteratorWrapper(edges.values().iterator(), edges.size());
   }

   public int nodeCount() {
      s.setAccessed();
      return nodes.size();
   }

   public int edgeCount() {
      s.setAccessed();
      return edges.size();
   }

   NodeWrapper getWrapper(java.lang.Object node) {
      ImplNodeAttributes attr = (ImplNodeAttributes)nodes.get(node);
      if (attr == null)
	 return null;
      else
	 return attr.nw;
   }

   EdgeWrapper getEdgeWrapper(Edge e) {
      return (EdgeWrapper)edges.get(e);
   }
}
