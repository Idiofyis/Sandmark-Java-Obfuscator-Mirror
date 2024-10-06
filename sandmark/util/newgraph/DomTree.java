package sandmark.util.newgraph;

class DomNodeWrapper extends NodeWrapper {
   DomNodeWrapper(Graph g, java.lang.Object n) {
      super(g, n);
      bucket = new java.util.HashSet();
   }

   NodeWrapper orig;
   DomNodeWrapper label, parent, ancestor, idom;
   TreeNodeWrapper tnw;
   int dfn, sdno;
   java.util.Set bucket;
}

class TreeNodeWrapper extends NodeWrapper {
   TreeNodeWrapper(Graph g, java.lang.Object n) {
      super(g, n);
      down = new EdgeWrapper[4];
   }

   EdgeWrapper up;
   EdgeWrapper down[];
   int downCount;

   void addDown(EdgeWrapper ew) {
      if (downCount >= down.length) {
	 EdgeWrapper tmp[] = new EdgeWrapper[2*down.length];
	 System.arraycopy(down, 0, tmp, 0, downCount);
	 down = tmp;
      }
      down[downCount++] = ew;
   }
}

class EdgeWrapperArrayIterator extends EdgeWrapperIterator {
   private int num;
   private EdgeWrapper a[];
   private int i;

   EdgeWrapperArrayIterator(EdgeWrapper a[], int num) {
      this.a = a;
      this.num = num;
      i = 0;
   }

   public EdgeWrapper getNext() {
      if (a == null)
	 return null;
      else {
	 if (i < a.length && i < num)
	    return a[i++];
	 else {
	    a = null;
	    return null;
	 }
      }
   }

   public int numEdges() {
      return Math.min(num, a.length);
   }
}

public class DomTree extends Graph {
   public java.lang.Object immediateDominator(java.lang.Object n) {
      TreeNodeWrapper tnw = (TreeNodeWrapper)getWrapper(n);
      if (tnw == null || tnw.up == null)
	 return null;
      else
	 return tnw.up.from.node;
   }

   public boolean dominates(java.lang.Object u, java.lang.Object v) {
      return reachable(u, v);
   }

   public java.util.Iterator dominators(java.lang.Object n) {
      return reverse().depthFirst(n);
   }

   public java.util.Iterator dominated(java.lang.Object n) {
      return depthFirst(n);
   }

   private DomNodeWrapper nodes[];
   private DomNodeWrapper n0;
   private int count;

   private java.util.Map wrappers;
   private java.util.Map edges;

   DomTree() {
      wrappers = new java.util.HashMap();
      edges = new java.util.HashMap();
      count = 0;
   }

   DomTree(Graph g, NodeWrapper root) {
      nodes = new DomNodeWrapper[10];
      n0 = new DomNodeWrapper(this, new java.lang.Object());
      n0.sdno = -1;
      n0.ancestor = n0.label = n0;
      count = 0;
      int slot1 = NodeWrapper.lockSlot();
      int slot2 = NodeWrapper.lockSlot();
      int slot3 = NodeWrapper.lockSlot();
      
      dfs(g, root, null, slot1, slot2, slot3);

      for (int i = count - 1; i > 0; i--) {
	 DomNodeWrapper w = nodes[i];

	 NodeWrapperIterator j = g._preds(w.orig);
	 NodeWrapper pred = j.getNext();
	 while (pred != null) {
	    int dfn = getSlots(pred, slot1, slot2, slot3);
	    if (dfn > 0) {
	       DomNodeWrapper u = eval(nodes[dfn-1]);
	       if (u.sdno < w.sdno)
		  w.sdno = u.sdno;
	    }
	    pred = j.getNext();
	 }

	 nodes[w.sdno].bucket.add(w);
	 link(w.parent, w);

	 java.util.Iterator k = w.parent.bucket.iterator();
	 while (k.hasNext()) {
	    DomNodeWrapper v = (DomNodeWrapper)k.next();
	    k.remove();
	    DomNodeWrapper u = eval(v);
	    if (u.sdno < v.sdno)
	       v.idom = u;
	    else
	       v.idom = w.parent;
	 }
      }

      for (int i = 0; i < count; i++)
	 setSlots(nodes[i].orig, slot1, slot2, slot3, 0);
      NodeWrapper.unlockSlot(slot1);
      NodeWrapper.unlockSlot(slot2);
      NodeWrapper.unlockSlot(slot3);
      
      for (int i = 1; i < count; i++) {
	 DomNodeWrapper w = nodes[i];
	 if (w.idom != nodes[w.sdno])
	    w.idom = w.idom.idom;
      }

      n0 = null;

      wrappers = new java.util.HashMap();
      edges = new java.util.HashMap();

      for (int i = 0; i < count; i++) {
	 TreeNodeWrapper tnw = new TreeNodeWrapper(this, nodes[i].node);
	 if (nodes[i].idom != null) {
	    Edge e = new EdgeImpl(nodes[i].idom.node, nodes[i].node);
	    EdgeWrapper ew = 
	       new EdgeWrapper(e, nodes[i].idom.tnw, tnw);
	    edges.put(e, ew);
	    tnw.up = ew;
	    nodes[i].idom.tnw.addDown(ew);
	 }
	 nodes[i].tnw = tnw;
	 wrappers.put(nodes[i].node, tnw);
      }

      nodes = null;
   }

   public Graph consolidate() {
      return this;
   }

   public int depth() {
      return 0;
   }

   EdgeWrapperIterator _inEdges(NodeWrapper n) {
      TreeNodeWrapper tnw = (TreeNodeWrapper)n;
      if (tnw.up == null)
	 return EMPTY_EDGE;
      else
	 return new SingleEdgeWrapperIterator(tnw.up);
   }

   int _inDegree(NodeWrapper n) {
      TreeNodeWrapper tnw = (TreeNodeWrapper)n;
      if (tnw.up == null)
	 return 0;
      else
	 return 1;
   }

   NodeWrapperIterator _preds(NodeWrapper n) {
      TreeNodeWrapper tnw = (TreeNodeWrapper)n;
      if (tnw.up == null)
	 return EMPTY_NODE;
      else
	 return new SingleNodeWrapperIterator(tnw.up.from);
   }

   EdgeWrapperIterator _outEdges(NodeWrapper n) {
      TreeNodeWrapper tnw = (TreeNodeWrapper)n;
      return new EdgeWrapperArrayIterator(tnw.down, tnw.downCount);
   }
   
   int _outDegree(NodeWrapper n) {
      TreeNodeWrapper tnw = (TreeNodeWrapper)n;
      return tnw.downCount;
   }

   public boolean hasNode(java.lang.Object n) {
      return wrappers.containsKey(n);
   }

   public boolean hasEdge(Edge e) {
      return edges.containsKey(e);
   }

   boolean reachable(NodeWrapper from, NodeWrapper to) {
      TreeNodeWrapper curr = (TreeNodeWrapper)to;
      while (curr != null) {
	 if (curr == from)
	    return true;
	 if (curr.up == null)
	    curr = null;
	 else
	    curr = (TreeNodeWrapper)curr.up.from;
      }
      return false;
   }

   NodeWrapperIterator _nodes() {
      return new NodeWrapperIterator() {
	    java.util.Iterator i = wrappers.values().iterator();

	    public NodeWrapper getNext() {
	       if (i == null)
		  return null;
	       else {
		  NodeWrapper rval = null;
		  if (i.hasNext()) {
		     rval = (NodeWrapper)i.next();
		  }
		  if (rval == null)
		     i = null;
		  return rval;
	       }
	    }
	 };
   }

   EdgeWrapperIterator _edges() {
      return new EdgeIteratorWrapper(edges.values().iterator(), edges.size());
   }

   NodeWrapper getWrapper(java.lang.Object node) {
      return (NodeWrapper)wrappers.get(node);
   }

   EdgeWrapper getEdgeWrapper(Edge e) {
      return (EdgeWrapper)edges.get(e);
   }

   public int nodeCount() {
      return count;
   }

   public int edgeCount() {
      return edges.size();
   }

   private void dfs(Graph g, NodeWrapper v, DomNodeWrapper parent,
		    int slot1, int slot2, int slot3) {
      if (count >= nodes.length) {
	 DomNodeWrapper tmp[] = new DomNodeWrapper[2*nodes.length];
	 System.arraycopy(nodes, 0, tmp, 0, count);
	 nodes = tmp;
      }

      DomNodeWrapper nw = new DomNodeWrapper(this, v.node);
      nw.orig = v;
      nw.label = nw;
      nw.sdno = count;
      nw.ancestor = n0;
      nw.dfn = count;
      nw.parent = parent;
      nodes[count++] = nw;
      setSlots(v, slot1, slot2, slot3, count);

      EdgeWrapperIterator i = g._outEdges(v);
      EdgeWrapper e = i.getNext();
      while (e != null) {
	 NodeWrapper succ = e.to;
	 if (!areSlotsSet(succ, slot1, slot2, slot3))
	    dfs(g, succ, nw, slot1, slot2, slot3);
	 e = i.getNext();
      }
   }

   private void compress(DomNodeWrapper v) {
      if (v.ancestor.ancestor != n0) {
	 compress(v.ancestor);
	 if (v.ancestor.label.sdno < v.label.sdno)
	    v.label = v.ancestor.label;
	 v.ancestor = v.ancestor.ancestor;
      }
   }

   private DomNodeWrapper eval(DomNodeWrapper v) {
      if (v.ancestor == n0)
	 return v;
      else {
	 compress(v);
	 return v.label;
      }
   }

   private void link(DomNodeWrapper v, DomNodeWrapper w) {
      w.ancestor = v;
   }
}
