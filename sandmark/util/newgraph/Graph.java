package sandmark.util.newgraph;

/**
 * Immutable directed graphs.  Nodes of these graphs are of type
 * {@link java.lang.Object}.  Nodes are regarded as a set: that is, only one
 * copy of a given node can exist within a given graph.  Whether or not
 * two nodes are the same is determined by 
 * {@link java.lang.Object#equals(java.lang.Object)}.  Adding a node to a graph
 * that already contains that node has no effect.
 * <br><br>
 * Edges are of type {@link Edge}.  Edges are also regarded as a set, so that
 * any given edge can only exist once in a single graph and adding an edge to
 * a graph a second time has no effect.  Whether or not two edges are the same
 * is also determined by {@link java.lang.Object#equals(java.lang.Object)}.
 * <br><br>
 * Since <code>Graphs</code> are immutable, modifier methods like
 * {@link #addNode(java.lang.Object)} or {@link #removeEdge(Edge)} return
 * a new <code>Graph</code> with the appropriate modifications done to it.
 * To add nodes <code>n1</code> and <code>n2</code> to a <code>Graph</code>
 * <code>g</code>, we can use the code:
 * <pre>
     g = g.addNode(n1);
     g = g.addNode(n2);
 * </pre>
 * Or, equivalently, we could use:
 * <pre>
     g = g.addNode(n1).addNode(n2);
 * </pre>
 * Had we also wanted to add an {@link Edge} from <code>n1</code> to
 * <code>n2</code>, we could have done so by creating an instance of the
 * class {@link EdgeImpl}:
 * <pre>
     g = g.addNode(n1).addNode(n2).addEdge(new EdgeImpl(n1, n2));
 * </pre>
 * or by using the method {@link #addEdge(java.lang.Object, java.lang.Object)},
 * which does the same thing:
 * <pre>
     g = g.addNode(n1).addNode(n2).addEdge(n1, n2);
 * </pre>
 * However, since {@link #addEdge(java.lang.Object, java.lang.Object)} adds
 * the source and sink nodes to the graph if necessary, this code would have
 * been sufficient:
 * <pre>
     g = g.addEdge(n1, n2);
 * </pre>
 * <br>
 * To create a <code>Graph</code>, see
 * {@link sandmark.util.newgraph.Graphs#createGraph(java.util.Iterator,java.util.Iterator)}.
 *
 * @author <a href="mailto:ecarter@cs.arizona.edu">Edward Carter</a>
 * @see sandmark.util.newgraph.MutableGraph
 */
public abstract class Graph {
   /**
    * Returns (approximately) the number of levels of indirection in the
    * internal representation of this graph.  The return value of this method
    * should be an indication of how much there is to gain in terms of
    * performance by calling {@link #consolidate()}, with a higher value
    * meaning there is more to gain.
    *
    * @see #consolidate()
    */
   abstract public int depth();   

   /**
    * Returns a copy of this graph with {@link #depth()} equal to zero.
    * The return value of this method is usually of type
    * {@link GraphImpl}.
    *
    * @see #depth()
    */
   abstract public Graph consolidate();
   
   /**
    * Returns a copy of the graph with the given node added.  If the graph
    * already contains the node, <code>this</code> is returned.
    *
    * @param n node to add to the graph
    * @see #hasNode(java.lang.Object)
    */
   public Graph addNode(java.lang.Object n) {
      if (n == null)
	 throw new java.lang.NullPointerException();

      if (hasNode(n))
	 return this;
      else
	 return checkDepth(new ExtraNodeGraph(this, n));
   }

   /**
    * Returns a copy of the graph with all nodes returned by the given
    * {@link java.util.Iterator} added.
    *
    * @param i all nodes returned by {@link java.util.Iterator#next()} will
    *   be included as nodes in the return value
    */
   public Graph addAllNodes(java.util.Iterator i) {
      Graph g = this;
      while (i.hasNext())
	 g = g.addNode(i.next());
      return g;
   }

   private static Graph checkDepth(Graph g) {
      return (g.depth() >= 20) ? (Graph)g.consolidate() : g;
   }

   NodeWrapperIterator extraNodes(int sofar) {
      return EMPTY_NODE;
   }

   EdgeWrapperIterator extraEdges(int sofar) {
      return EMPTY_EDGE;
   }

   Graph extraBase(int sofar) {
      return this;
   }

   Graph extraConsolidate(int sofar) {
      return this;
   }

   NodeWrapperIterator missingNodes(int sofar) {
      return EMPTY_NODE;
   }

   EdgeWrapperIterator missingEdges(int sofar) {
      return EMPTY_EDGE;
   }

   Graph missingBase(int sofar) {
      return this;
   }

   Graph missingConsolidate(int sofar) {
      return this;
   }

   /**
    * Returns a copy of the graph with the given node removed.  If the graph
    * does not contain the given node, <code>this</code> is returned.
    *
    * @param n node to remove from the graph
    * @see #hasNode(java.lang.Object)
    */
   public Graph removeNode(java.lang.Object n) {
      NodeWrapper nw = getWrapper(n);
      if (nw == null)
	 return this;
      else
	 return _removeNode(nw, true);
   }

   abstract NodeWrapper getWrapper(java.lang.Object node);
   abstract EdgeWrapper getEdgeWrapper(Edge e);
   
   public Graph removeAllNodes(java.util.Iterator i) {
      Graph rval = this;
      while (i.hasNext())
	 rval = rval.removeNode(i.next());
      return rval;
   }

   private Graph _removeNode(NodeWrapper nw) {
      return _removeNode(nw, false);
   }

   private Graph _removeNode(NodeWrapper nw, boolean check) {
      Graph g = this;
      EdgeWrapperIterator i = _inEdges(nw);
      EdgeWrapper e = i.getNext();
      while (e != null) {
	 g = new MissingEdgeGraph(g, e);
	 if (check) {
	    g = checkDepth(g);
	    check = false;
	 }
	 e = i.getNext();
      }
      i = _outEdges(nw);
      e = i.getNext();
      while (e != null) {
	 if (e.to != nw) {
	    g = new MissingEdgeGraph(g, e);
	    if (check) {
	       g = checkDepth(g);
	       check = false;
	    }
	 }
	 e = i.getNext();
      }
      g = new MissingNodeGraph(g, nw);
      if (check)
	 g = checkDepth(g);
      return g;
   }
   
   /**
    * Returns a copy of the graph with the given edge added.  If the graph
    * already contains the edge, <code>this</code> is returned.
    *
    * @param e edge to add to the graph
    * @see #hasEdge(Edge)
    */
   public Graph addEdge(Edge e) {
      if (e == null)
	 throw new java.lang.NullPointerException();

      if (hasEdge(e))
	 return this;
      else {
	 Graph g = addNode(e.sourceNode()).addNode(e.sinkNode());
	 return checkDepth(new ExtraEdgeGraph(g, e));
      }
   }

   /**
    * Returns a copy of the graph with an edge from <code>from</code> to
    * <code>to</code> added.  Even if the graph already contains such an
    * edge, another one is added.
    *
    * @param from source node
    * @param to sink node
    * @see #hasEdge(java.lang.Object, java.lang.Object)
    */
   public Graph addEdge(java.lang.Object from, java.lang.Object to) {
      return addEdge(new EdgeImpl(from, to));
   }

   public Graph addAllEdges(java.util.Iterator i) {
      Graph g = this;
      while (i.hasNext())
	 g = g.addEdge((Edge)i.next());
      return g;
   }

   /**
    * Returns a copy of the graph with the given edge removed.  If the graph
    * does not contain that edge, <code>this</code> is returned.
    *
    * @param e edge to remove
    * @see #hasEdge(Edge)
    */
   public Graph removeEdge(Edge e) {
      EdgeWrapper ew = getEdgeWrapper(e);
      if (ew == null)
	 return this;
      else
	 return checkDepth(new MissingEdgeGraph(this, ew));
   }

   public Graph removeAllEdges(java.util.Iterator i) {
      Graph g = this;
      while (i.hasNext()) {
	 g = g.removeEdge((Edge)i.next());
      }
      return g;
   }

   /**
    * Returns a copy of the graph with some edge from <code>from</code> to
    * <code>to</code> removed.  If the graph contains no such edge,
    * <code>this</code> is returned.
    *
    * @param from source node
    * @param to sink node
    * @see #hasEdge(java.lang.Object, java.lang.Object)
    */
   public Graph removeEdge(java.lang.Object from, java.lang.Object to) {
       java.util.Iterator i = outEdges(from);
       while (i.hasNext()) {
	   Edge e = (Edge)i.next();
	   if (to.equals(e.sinkNode()))
	       return removeEdge(e);
       }
       return this;
   }

   /**
    * Returns a copy of the graph with only those nodes that are
    * reachable from <code>root</code>.  If <code>root</code> is not
    * a node of the graph, <code>this</code> is returned.
    */
   public Graph removeUnreachable(java.lang.Object root) {
      if (!hasNode(root))
	 return this;

      int slot = NodeWrapper.lockSlot();

      reachable(getWrapper(root), slot);
      NodeWrapperIterator j = _nodes();
      Graph g = this;
      NodeWrapper nw = j.getNext();
      while (nw != null) {
	 if (nw.getSlot(slot) == 0)
	    g = g._removeNode(nw);
	 nw = j.getNext();
      }
      
      j = g._nodes();
      nw = j.getNext();
      while (nw != null) {
	 nw.setSlot(slot, (byte)0);
	 nw = j.getNext();
      }
      NodeWrapper.unlockSlot(slot);

      return checkDepth(g);
   }

   private void reachable(NodeWrapper n, int slot) {
      if (n.getSlot(slot) == 0) {
	 n.setSlot(slot, (byte)1);
	 EdgeWrapperIterator i = _outEdges(n);
	 EdgeWrapper e = i.getNext();
	 while (e != null) {
	    reachable(e.to, slot);
	    e = i.getNext();
	 }
      }
   }

   public Graph reverse() {
      return checkDepth(new ReversedGraph(this));
   }

   /**
    * Returns an iterator over the edges in the graph pointing to the given
    * node.
    *
    * @param n sink node
    * @see #preds(java.lang.Object)
    */
   public final java.util.Iterator inEdges(java.lang.Object n) {
      NodeWrapper nw = getWrapper(n);
      if (nw == null)
	 return EMPTY_ITER;
      else
	 return _inEdges(nw).iterator();
   }

   abstract EdgeWrapperIterator _inEdges(NodeWrapper n);

   /**
    * Returns an iterator over the edges in the graph pointing from the
    * given node.
    *
    * @param n source node
    * @see #succs(java.lang.Object)
    */
   public final java.util.Iterator outEdges(java.lang.Object n) {
      NodeWrapper nw = getWrapper(n);
      if (nw == null)
	 return EMPTY_ITER;
      else
	 return _outEdges(nw).iterator();
   }

   abstract EdgeWrapperIterator _outEdges(NodeWrapper n);

   /**
    * Returns an iterator over nodes <code>u</code> such that there is an
    * edge in the graph from <code>n</code> to <code>u</code>.
    *
    * @param n source node
    * @see #outEdges(java.lang.Object)
    */
   public java.util.Iterator succs(java.lang.Object n) {
      NodeWrapper nw = getWrapper(n);
      if (nw == null)
	 return EMPTY_ITER;
      else
	 return _succs(nw).iterator();
   }

   NodeWrapperIterator _succs(NodeWrapper nw) {
      return new SuccIterator(nw);
   }

   private class SuccIterator extends NodeWrapperIterator {
      private EdgeWrapperIterator i;
      private NodeWrapper n;
      private int slot;

      public SuccIterator(NodeWrapper n) {
	 this.n = n;
	 i = _outEdges(n);
	 slot = NodeWrapper.lockSlot();
      }

      public NodeWrapper getNext() {
	 if (i == null)
	    return null;
	 else {
	    NodeWrapper rval = null;
	    EdgeWrapper ew;
	    do {
	       ew = i.getNext();
	       if (ew != null && ew.to.getSlot(slot) == 0)
		  rval = ew.to;
	    } while (ew != null && rval == null);
	    if (ew == null)
	       i = null;
	    if (rval == null)
	       unlock();
	    else {
	       synchronized(this) {
		  rval.setSlot(slot, (byte)1);
	       }
	    }
	    return rval;
	 }
      }

      private void unlock() {
	 synchronized(this) {
	    if (slot >= 0) {
	       EdgeWrapperIterator i = _outEdges(n);
	       EdgeWrapper ew = i.getNext();
	       while (ew != null) {
		  ew.to.setSlot(slot, (byte)0);
		  ew = i.getNext();
	       }
	       NodeWrapper.unlockSlot(slot);
	       slot = -1;
	    }
	 }
      }

      protected void finalize() {
	 unlock();
      }
   }

   /**
    * Returns an iterator over nodes <code>u</code> such that there is an
    * edge in the graph from <code>u</code> to <code>n</code>.
    *
    * @param n sink node
    * @see #inEdges(java.lang.Object)
    */
   public java.util.Iterator preds(java.lang.Object n) {
      NodeWrapper nw = getWrapper(n);
      if (nw == null)
	 return EMPTY_ITER;
      else
	 return _preds(nw).iterator();
   }

   NodeWrapperIterator _preds(NodeWrapper nw) {
      return new PredIterator(nw);
   }

   private class PredIterator extends NodeWrapperIterator {
      private EdgeWrapperIterator i;
      private NodeWrapper n;
      private int slot;

      public PredIterator(NodeWrapper n) {
	 this.n = n;
	 i = _inEdges(n);
	 slot = NodeWrapper.lockSlot();
      }

      public NodeWrapper getNext() {
	 if (i == null)
	    return null;
	 else {
	    NodeWrapper rval = null;
	    EdgeWrapper ew;
	    do {
	       ew = i.getNext();
	       if (ew != null && ew.from.getSlot(slot) == 0)
		  rval = ew.from;
	    } while (ew != null && rval == null);
	    if (ew == null)
	       i = null;
	    if (rval == null)
	       unlock();
	    else {
	       synchronized(this) {
		  rval.setSlot(slot, (byte)1);
	       }
	    }
	    return rval;
	 }
      }

      private void unlock() {
	 synchronized(this) {
	    if (slot >= 0) {
	       EdgeWrapperIterator i = _inEdges(n);
	       EdgeWrapper ew = i.getNext();
	       while (ew != null) {
		  ew.from.setSlot(slot, (byte)0);
		  ew = i.getNext();
	       }
	       NodeWrapper.unlockSlot(slot);
	       slot = -1;
	    }
	 }
      }

      protected void finalize() {
	 unlock();
      }
   }

   public java.util.Iterator succs(java.lang.Object n,
				   java.util.Comparator c) {
      return sortedIterator(succs(n), c);
   }

   public java.util.Iterator preds(java.lang.Object n,
				   java.util.Comparator c) {
      return sortedIterator(preds(n), c);
   }

   private java.util.Iterator sortedIterator(java.util.Iterator i,
					     java.util.Comparator c) {
      java.lang.Object a[] = new java.lang.Object[10];
      int j = 0;
      while (i.hasNext()) {
	 if (j >= a.length) {
	    java.lang.Object tmp[] = new java.lang.Object[a.length * 2];
	    System.arraycopy(a, 0, tmp, 0, a.length);
	    a = tmp;
	 }
	 a[j++] = i.next();
      }
      java.util.Arrays.sort(a, 0, j, c);
      return new sandmark.util.ArrayIterator(a, 0, j);
   }

   public int inDegree(java.lang.Object n) {
      NodeWrapper nw = getWrapper(n);
      return (nw == null) ? 0 : _inDegree(nw);
   }

   abstract int _inDegree(NodeWrapper n);

   public int outDegree(java.lang.Object n) {
      NodeWrapper nw = getWrapper(n);
      return (nw == null) ? 0 : _outDegree(nw);
   }

   abstract int _outDegree(NodeWrapper n);

   public int maxInDegree() {
      int max = 0;
      java.util.Iterator i = nodes();
      while (i.hasNext()) {
	 int d = inDegree(i.next());
	 if (d > max)
	    max = d;
      }
      return max;
   }

   public int maxOutDegree() {
      int max = 0;
      java.util.Iterator i = nodes();
      while (i.hasNext()) {
	 int d = outDegree(i.next());
	 if (d > max)
	    max = d;
      }
      return max;
   }

   public int numPreds(java.lang.Object n) {
      return itemCount(preds(n));
   }

   public int numSuccs(java.lang.Object n) {
      return itemCount(succs(n));
   }

   private static int itemCount(java.util.Iterator i) {
      int count = 0;
      while (i.hasNext()) {
	 i.next();
	 count++;
      }
      return count;
   }

   /**
    * Returns <code>true</code> iff the graph contains the given node.
    *
    * @param n node to look for
    */
   abstract public boolean hasNode(java.lang.Object n);
   /**
    * Returns <code>true</code> iff the graph contains the given edge.
    *
    * @param e edge to look for
    */
   abstract public boolean hasEdge(Edge e);
   /**
    * Returns <code>true</code> iff the graph contains some edge from
    * <code>from</code> to <code>to</code>.
    *
    * @param from source node
    * @param to sink node
    */
   public boolean hasEdge(java.lang.Object from, java.lang.Object to) {
      java.util.Iterator i = outEdges(from);
      while (i.hasNext())
	 if (to.equals(((Edge)i.next()).sinkNode()))
	    return true;
      return false;
   }
   
   public Edge getFirstEdge(java.lang.Object from, java.lang.Object to) {
      for(java.util.Iterator i = outEdges(from) ; i.hasNext() ; ) {
         Edge e = (Edge)i.next();
         if(e.sinkNode().equals(to))
            return e;
      }
      return null;
   }

   /**
    * Returns an iterator over all nodes in the graph.
    */
   public final java.util.Iterator nodes() {
      return _nodes().iterator();
   }

   abstract NodeWrapperIterator _nodes();
   
   /**
    * Returns an iterator over all the edges in the graph.
    */
   public final java.util.Iterator edges() {
      return _edges().iterator();
   }

   abstract EdgeWrapperIterator _edges();

   /**
    * Returns the number of nodes in the graph.
    */
   abstract public int nodeCount();
   /**
    * Returns the number of edges in the graph.
    */
   abstract public int edgeCount();

   /**
    * Returns an iterator over all root nodes in the graph.  A <i>root node</i>
    * is defined as a node with no incoming edges.
    */
   public final java.util.Iterator roots() {
      return _roots().iterator();
   }

   NodeWrapperIterator _roots() {
      return new NodeWrapperIterator() {
	    private NodeWrapperIterator i = _nodes();
	    
	    public NodeWrapper getNext() {
	       if (i == null)
		  return null;
	       else {
		  NodeWrapper nw;
		  do {
		     nw = i.getNext();
		  } while (nw != null && _inDegree(nw) > 0);
		  if (nw == null)
		     i = null;
		  return nw;
	       }
	    }
	 };
   }

   /**
    * Return an iterator over all reverse root nodes in the graph.  A
    * <i>reverse root node</i> is defined as a node with no outgoing edges.
    */
   public final java.util.Iterator reverseRoots() {
      return _reverseRoots().iterator();
   }

   NodeWrapperIterator _reverseRoots() {
      return new NodeWrapperIterator() {
	    private NodeWrapperIterator i = _nodes();
	    
	    public NodeWrapper getNext() {
	       if (i == null)
		  return null;
	       else {
		  NodeWrapper nw;
		  do {
		     nw = i.getNext();
		  } while (nw != null && _outDegree(nw) > 0);
		  if (nw == null)
		     i = null;
		  return nw;
	       }
	    }
	 };
   }

   /**
    * Returns an iterator over nodes in the graph in depth-first order,
    * starting with <code>root</code>.  If <code>root</code> is not a node
    * of the graph, the iterator will return no elements.  Each node
    * of the graph is returned by the iterator at most once.  Nodes
    * not reachable from <code>root</code> are not returned.
    *
    * @param root starting point for depth-first search
    */
   public final java.util.Iterator depthFirst(java.lang.Object root) {
      NodeWrapper nw = getWrapper(root);
      if (nw == null)
	 return EMPTY_ITER;
      else
	 return _depthFirst(nw).iterator();
   }

   NodeWrapperIterator _depthFirst(NodeWrapper root) {
      return new DepthFirstIterator(root);
   }

   private class DepthFirstIterator extends NodeWrapperIterator {
      private NodeWrapper root;
      private java.util.Stack stack;
      private Graph g;
      private int slot;
      private java.util.LinkedList marked;

      public DepthFirstIterator(NodeWrapper root, boolean buildTree) {
	 slot = NodeWrapper.lockSlot();

	 this.root = root;
	 root.setSlot(slot, (byte)1);
	 stack = new java.util.Stack();
	 stack.push(root);
	 if (buildTree)
	    g = Graphs.createGraph(null, null).addNode(root.node);

	 marked = new java.util.LinkedList();
	 marked.add(root);
      }

      public DepthFirstIterator(NodeWrapper root) {
	 this(root, false);
      }

      public NodeWrapper getNext() {
	 if (slot < 0)
	    return null;
	 else if (root == null) {
	    do {
	       NodeWrapper curr = (NodeWrapper)stack.peek();
	       EdgeWrapperIterator i = _outEdges(curr);
	       EdgeWrapper ew = i.getNext();
	       while (ew != null) {
		  NodeWrapper o = ew.to;
		  if (o.getSlot(slot) == 0) {
		     if (g != null)
			g = g.addEdge(ew.edge);
		     synchronized (marked) {
			o.setSlot(slot, (byte)1);
			marked.add(o);
		     }
		     stack.push(o);
		     return o;
		  }
		  ew = i.getNext();
	       }
	       stack.pop();
	    } while (!stack.empty());
	    unlock();
	    return null;
	 }
	 else {
	    NodeWrapper rval = root;
	    root = null;
	    return rval;
	 }
      }

      public Graph tree() {
	 return g;
      }

      private void unlock() {
	 synchronized (marked) {
	    if (slot >= 0) {
	       for (java.util.Iterator i = marked.iterator(); i.hasNext(); )
		  ((NodeWrapper)i.next()).setSlot(slot, (byte)0);
	       NodeWrapper.unlockSlot(slot);
	       slot = -1;
	    }
	 }
      }

      protected void finalize() {
	 unlock();
      }
   }

   public final java.util.Iterator postOrder(java.lang.Object root) {
      NodeWrapper nw = getWrapper(root);
      if (nw == null)
	 return EMPTY_ITER;
      else
	 return _postOrder(nw).iterator();
   }

   NodeWrapperIterator _postOrder(NodeWrapper root) {
      return new PostOrderIterator(root);
   }

   private class PostOrderIterator extends NodeWrapperIterator {
      class PathElement {
	 NodeWrapper nw;
	 EdgeWrapperIterator ewi;
	 PathElement(NodeWrapper n,EdgeWrapperIterator i)
	 { nw = n ; ewi = i; }
      }
      private java.util.Stack path;
      private java.util.LinkedList visited;
      private int slot;

      public PostOrderIterator(NodeWrapper root) {
	 slot = NodeWrapper.lockSlot();
	 visited = new java.util.LinkedList();
	 path = new java.util.Stack();
	 buildPath(root);
      }
      
      private void buildPath(NodeWrapper nw) {
	 while(nw != null) {
	    visited.add(nw);
	    nw.setSlot(slot,(byte)1);
	    EdgeWrapperIterator i = _outEdges(nw);
	    path.push(new PathElement(nw,i));
	    EdgeWrapper ew;
	    while((ew = i.getNext()) != null && 
		  ew.to.getSlot(slot) != (byte)0)
	       ;
	    if(ew == null)
	       nw = null;
	    else
	       nw = ew.to;
	 }
      }

      public synchronized NodeWrapper getNext() {
	 if(path.empty()) {
	    unlock();
	    return null;
	 }

	 PathElement pe = (PathElement)path.pop();
	 if(pe.ewi.getNext() != null)
	    throw new Error("visiting parent before visiting some child");

	 if(!path.empty()) {
	    PathElement parent = (PathElement)path.peek();
	    EdgeWrapper ew ;
	    while((ew = parent.ewi.getNext()) != null && 
		  ew.to.getSlot(slot) != (byte)0)
	       ;
	    if(ew != null)
	       buildPath(ew.to);
	 }

	 return pe.nw;
      }
      private synchronized void unlock() {
	 if(slot >= 0) {
	    for (java.util.Iterator i = visited.iterator(); i.hasNext(); )
	       ((NodeWrapper)i.next()).setSlot(slot, (byte)0);
	    NodeWrapper.unlockSlot(slot);
	    slot = -1;
	 }
      }

      protected void finalize() {
	 unlock();
      }
   }

   /**
    * Returns an iterator over nodes in the graph in breadth-first order,
    * starting with <code>root</code>.  If <code>root</code> is not a node
    * of the graph, the iterator will return no elements.  Each node
    * of the graph is returned by the iterator at most once.  Nodes
    * not reachable from <code>root</code> are not returned.
    *
    * @param root starting point for breadth-first search
    */
   public java.util.Iterator breadthFirst(java.lang.Object root) {
      NodeWrapper nw = getWrapper(root);
      if (nw == null)
	 return EMPTY_ITER;
      else
	 return _breadthFirst(nw).iterator();
   }

   NodeWrapperIterator _breadthFirst(NodeWrapper root) {
      return new BreadthFirstIterator(root);
   }

   private class BreadthFirstIterator extends NodeWrapperIterator {
      private java.util.LinkedList nodes, iterators;
      private int slot;
      private java.util.LinkedList marked;

      public BreadthFirstIterator(NodeWrapper root) {
	 slot = NodeWrapper.lockSlot();
	 
	 root.setSlot(slot, (byte)1);
	 nodes = new java.util.LinkedList();
	 nodes.add(root);
	 iterators = new java.util.LinkedList();
	 iterators.add(_outEdges(root));

	 marked = new java.util.LinkedList();
	 marked.add(root);
      }

      public NodeWrapper getNext() {
	 while (nodes.isEmpty() && !iterators.isEmpty()) {
	    EdgeWrapperIterator i = (EdgeWrapperIterator)iterators.removeFirst();
	    EdgeWrapper ew = i.getNext();
	    while (ew != null) {
	       NodeWrapper o = ew.to;
	       if (o.getSlot(slot) == 0) {
		  synchronized (marked) {
		     o.setSlot(slot, (byte)1);
		     marked.add(o);
		  }
		  nodes.add(o);
		  iterators.add(_outEdges(o));
	       }
	       ew = i.getNext();
	    }
	 }
	 if (nodes.isEmpty()) {
	    unlock();
	    return null;
	 }
	 else
	    return (NodeWrapper)nodes.removeFirst();
      }

      private void unlock() {
	 synchronized (marked) {
	    if (slot >= 0) {
	       for (java.util.Iterator i = marked.iterator(); i.hasNext(); )
		  ((NodeWrapper)i.next()).setSlot(slot, (byte)0);
	       NodeWrapper.unlockSlot(slot);
	       slot = -1;
	    }
	 }
      }

      protected void finalize() {
	 unlock();
      }
   }

   public Graph depthFirstTree(java.lang.Object root) {
      NodeWrapper nw = getWrapper(root);
      if (nw == null)
	 return Graphs.createGraph(null, null);

      DepthFirstIterator i = new DepthFirstIterator(nw, true);
      while (i.getNext() != null);
      return i.tree();
   }

   public boolean reachable(java.lang.Object from, java.lang.Object to) {
      NodeWrapper fromw = getWrapper(from);
      if (fromw == null)
	 return false;
      NodeWrapper tow = getWrapper(to);
      if (tow == null)
	 return false;
      if (fromw == tow)
	 return true;
      return reachable(fromw, tow);
   }

   private java.util.Map reachableSets = null;

   synchronized boolean reachable(NodeWrapper from, NodeWrapper to) {
      if (reachableSets == null)
	 reachableSets = new java.util.HashMap();

      java.util.Set s = (java.util.Set)reachableSets.get(from);
      if (s == null) {
	 s = new java.util.HashSet();
	 reachableSets.put(from, s);

	 NodeWrapperIterator i = new DepthFirstIterator(from);
	 NodeWrapper curr = i.getNext();
	 while (curr != null) {
	    s.add(curr);
	    curr = i.getNext();
	 }
      }

      return s.contains(to);
   }

   public Graph union(Graph g) {
      return addAllNodes(g.nodes()).addAllEdges(g.edges());
   }

   public java.util.List acyclicOrder(java.lang.Object root) {
      NodeWrapper nw = getWrapper(root);
      if (nw == null)
	 return null;
      else
	 return convertList(acyclicOrder(nw));
   }

   private java.util.List convertList(java.util.List l) {
      if (l == null)
	 return null;
      java.util.List rval = new java.util.ArrayList();
      for (java.util.Iterator i = l.iterator(); i.hasNext(); ) {
	 NodeWrapper curr = (NodeWrapper)i.next();
	 rval.add(curr.node);
      }
      return rval;
   }

   public java.util.List acyclicHamiltonianPath(java.lang.Object root) {
      NodeWrapper nw = getWrapper(root);
      if (nw == null)
	 return null;
      else
	 return convertList(acyclicHamiltonianPath(nw));
   }

   private java.util.List acyclicOrder(NodeWrapper root) {
      if (_inDegree(root) > 0)
	 return null;

      java.util.List l = new java.util.ArrayList();
      java.util.List rval = null;
      int slot = NodeWrapper.lockSlot();
      root.setSlot(slot, (byte)1);
      
      l.add(root);
      while (l.size() < nodeCount()) {
	 NodeWrapperIterator i = _nodes();
	 NodeWrapper n = i.getNext();
	 boolean found = false;
	 while (n != null && !found) {
	    if (n.getSlot(slot) == 0) {
	       EdgeWrapperIterator ei = _inEdges(n);
	       EdgeWrapper e = ei.getNext();
	       int count = 0;
	       while (count == 0 && e != null) {
		  if (e.from.getSlot(slot) == 0)
		     count++;
		  e = ei.getNext();
	       }
	       if (count == 0)
		  found = true;
	    }
	    if (!found)
	       n = i.getNext();
	 }
	 if (found) {
	    n.setSlot(slot, (byte)1);
	    l.add(n);
	 }
	 else
	    break;
      }

      if (l.size() == nodeCount())
	 rval = l;

      for (java.util.Iterator i = l.iterator(); i.hasNext(); ) {
	 NodeWrapper nw = (NodeWrapper)i.next();
	 nw.setSlot(slot, (byte)0);
      }
      NodeWrapper.unlockSlot(slot);

      return rval;
   }

   private java.util.List acyclicHamiltonianPath(NodeWrapper root) {
      java.util.List l = acyclicOrder(root);
      if (l == null)
	 return null;
      if (l.size() < nodeCount())
	 return null;

      NodeWrapper ao[] = new NodeWrapper[l.size()];
      java.util.Iterator li = l.iterator();
      int in = 0;
      while (li.hasNext()) {
	 ao[in++] = (NodeWrapper)li.next();
      }
      int slot1 = NodeWrapper.lockSlot();
      int slot2 = NodeWrapper.lockSlot();
      int slot3 = NodeWrapper.lockSlot();
      for (int i = 0; i < ao.length; i++)
	 setSlots(ao[i], slot1, slot2, slot3, i);

      // find "shortest" paths from the root
      NodeWrapper prevNode[] = new NodeWrapper[nodeCount()];
      int cost[] = new int[nodeCount()];
      int bigCost = cost.length*cost.length;
      prevNode[0] = root;
      cost[0] = 0;
      for (int i = 1; i < cost.length; i++) {
	 int min = bigCost;
	 NodeWrapper minNode = null;
	 for (int j = 0; j < i; j++) {
	    EdgeWrapperIterator ei = _outEdges(ao[j]);
	    boolean found = false;
	    EdgeWrapper e = ei.getNext();
	    while (!found && e != null) {
	       if (e.to == ao[i])
		  found = true;
	       e = ei.getNext();
	    }
	    if (found) {
	       int newCost = cost[j] - 1;
	       if (newCost < min) {
		  min = newCost;
		  minNode = ao[j];
	       }
	    }
	 }
	 cost[i] = min;
	 prevNode[i] = minNode;
      }

      java.util.List rval = null;
      if (cost[cost.length-1] == 1 - nodeCount()) {
	 // put nodes in Hamiltonian path order
	 rval = new java.util.LinkedList();
	 NodeWrapper prev = ao[ao.length - 1];
	 rval.add(prev);
	 while (root != prev) {
	    int index = getSlots(prev, slot1, slot2, slot3);
	    prev = prevNode[index];
	    rval.add(0, prev);
	 }
      }

      for (int i = 0; i < ao.length; i++)
	 setSlots(ao[i], slot1, slot2, slot3, 0);
      NodeWrapper.unlockSlot(slot1);
      NodeWrapper.unlockSlot(slot2);
      NodeWrapper.unlockSlot(slot3);

      return rval;
   }

   public Graph removeMultipleEdges() {
      java.util.Set repeatEdges = new java.util.HashSet();
      for (java.util.Iterator i = nodes(); i.hasNext(); ) {
	 java.util.Set succSet = new java.util.HashSet();
	 for (java.util.Iterator j = outEdges(i.next()); j.hasNext(); ) {
	    Edge e = (Edge)j.next();
	    if (succSet.contains(e.sinkNode()))
	       repeatEdges.add(e);
	    else
	       succSet.add(e.sinkNode());
	 }
      }
      return removeAllEdges(repeatEdges.iterator());
   }

   public Graph inducedSubgraph(java.util.Iterator nodeIter) {
       Graph g = this;
       java.util.Set s = new java.util.HashSet();
       while (nodeIter.hasNext())
	   s.add(nodeIter.next());
       for (java.util.Iterator i = nodes(); i.hasNext(); ) {
	   java.lang.Object n = i.next();
	   if (!s.contains(n))
	       g = g.removeNode(n);
       }
       return g;
   }

   public DomTree dominatorTree(java.lang.Object root) {
      NodeWrapper nw = getWrapper(root);
      if (nw == null)
	 return new DomTree();
      else
	 return new DomTree(this, nw);
   }

   static final NodeWrapperIterator EMPTY_NODE = new NodeWrapperIterator() {
	 public NodeWrapper getNext() {
	    return null;
	 }
      };
   static final EdgeWrapperIterator EMPTY_EDGE = new EdgeWrapperIterator() {
	 public EdgeWrapper getNext() {
	    return null;
	 }

	 public int numEdges() {
	    return 0;
	 }
      };
   static final java.util.Iterator EMPTY_ITER = new java.util.Iterator() {
	 public boolean hasNext() {
	    return false;
	 }

	 public java.lang.Object next() {
	    throw new java.util.NoSuchElementException();
	 }

	 public void remove() {
	    throw new java.lang.UnsupportedOperationException();
	 }
      };

   private static final int MAX_COUNT = 1 << 24;

   static void setSlots(NodeWrapper nw, 
			int slot1, int slot2, int slot3, int val) {
      if (val < 0 || val >= MAX_COUNT)
	 throw new RuntimeException("more than 2^24-1 nodes");

      nw.setSlot(slot1, (byte)val);
      nw.setSlot(slot2, (byte)(val >> 8));
      nw.setSlot(slot3, (byte)(val >> 16));
   }

   static int getSlots(NodeWrapper nw,
		       int slot1, int slot2, int slot3) {
      int part1 = nw.getSlot(slot1);
      part1 &= 255;
      int part2 = nw.getSlot(slot2);
      part2 &= 255;
      int part3 = nw.getSlot(slot3);
      part3 &= 255;

      return part1 | (part2 << 8) | (part3 << 16);
   }

   static boolean areSlotsSet(NodeWrapper nw,
			      int slot1, int slot2, int slot3) {
      if (nw.getSlot(slot1) != 0)
	 return true;
      if (nw.getSlot(slot2) != 0)
	 return true;
      if (nw.getSlot(slot3) != 0)
	 return true;
      return false;
   }
}
