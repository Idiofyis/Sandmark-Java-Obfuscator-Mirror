package sandmark.util.newgraph;

public class Afs implements java.util.Iterator {

   java.util.ArrayList queue = new java.util.ArrayList();
   Graph graph;
   java.util.Iterator edgeIter = null;
   Path nextPath = null;
   boolean hasNextPath = false;
   java.util.HashMap parent = new java.util.HashMap();
   java.util.HashSet seen = new java.util.HashSet();

/**
 * Perform a breadth-first-search on a graph from a particular 
 * <code>root</code> node, generating all paths.
 * <PRE>
 *    Afs d = new Afs(graph,root);
 *    while (d.hasNext()) {
 *       sandmark.util.newgraph.Path path = 
 *          (sandmark.util.newgraph.Path) d.next();
 *    }
 * </PRE>
 * <P>
 * We generate a set of paths, where <code>path[0]==root</code> and
 * <code>path[path.length-1]</code> is the node we're currently visiting.
 * @param graph the graph
 * @param root  the root node from which we start exploring.
*/
public Afs (
   Graph graph, 
   java.lang.Object root) {
   this.graph = graph;
   queue.add(root);
   edgeIter = graph.outEdges(root);
   parent.put(root,null);
   seen.add(root);
   setDistance(0);
}

public Afs(MutableGraph graph, java.lang.Object root) {
   this(graph.graph(), root);
}

private void nextElement() {
    hasNextPath = false;
    nextPath = null;

    while (true) {
       java.lang.Object head = queue.get(0);
       seen.add(head);

       while (edgeIter.hasNext()) {
          Edge e = (Edge) edgeIter.next();
          java.lang.Object sink = e.sinkNode();
          if (!onPath(sink,head)) {
             setDistance(sink, getDistance(head)+1);
             parent.put(sink,head);
             queue.add(sink);
             nextPath = getPath(sink);
             hasNextPath = true;
             return;
          }
       }
       queue.remove(0);
       seen.remove(head);
       if (queue.isEmpty()) return;
       head = queue.get(0);
       edgeIter = graph.outEdges(head);
    }
}
    
public boolean hasNext()  {
   if (!hasNextPath) nextElement();
   return hasNextPath;
}
    
    
public Object next() {
   if (!hasNextPath) nextElement();
   if (!hasNextPath)
      throw new java.util.NoSuchElementException();
   Path tmp = nextPath;
   nextPath = null;
   hasNextPath = false;
   return tmp;
}
    

public void remove() {
   throw new UnsupportedOperationException
   ("It is impossible to remove from this iterator!");
}

private Path getPath(
   java.lang.Object n) {
   Path path = new Path();
   while (n != null) {
      path.addFirst(n);
      n = parent.get(n);
   }
   return path;
}

/**
 * Return true if <code>node</code> is on the path from <code>root</code>
 * up to the top of the three.
 */
private boolean onPath(
   java.lang.Object node,
   java.lang.Object root) {
   while (root != null) {
      if (node.equals(root)) return true;
      root = parent.get(root);
   }
   return false;
}

/******************************************************************/
private java.util.Hashtable distance;

private int getDistance(java.lang.Object n) {
    return ((java.lang.Integer)distance.get(n)).intValue();
}

private void setDistance(java.lang.Object n, int c) {
    distance.put(n, new java.lang.Integer(c));
}

private void setDistance(int c) {
    distance = new java.util.Hashtable();
    java.util.Iterator nodeIter = graph.nodes();
    while (nodeIter.hasNext()) {
        java.lang.Object n = nodeIter.next();
        setDistance(n, c);
    }
}

}

