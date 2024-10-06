package sandmark.util.newgraph;

public class Path {
    java.util.Vector path = new java.util.Vector();
    java.util.HashSet nodes = new java.util.HashSet();

/**
 * Construct an empty path.
 */
public Path() {}

/**
 * Construct a copy of the path <code>P</code>.
 */
public Path(sandmark.util.newgraph.Path P) {
   java.util.Enumeration enum = P.elements();
   while (enum.hasMoreElements()) {
      java.lang.Object node = enum.nextElement();
      add(node);
   }
}

/**
 * Add a node last to this path.
 * @param node the node to be added.
 */
public void add(java.lang.Object node) {
   path.add(node);
   nodes.add(node);
}

/**
 * Add a node first to this path.
 * @param node the node to be added.
 */
public void addFirst(java.lang.Object node) {
   path.add(0,node);
   nodes.add(node);
}

/**
 * Return true if <code>node</code> is on this path.
 */
public boolean onPath(java.lang.Object node) {
   return nodes.contains(node);
}

/**
 * Return an array of the nodes on this path.
 */
public java.lang.Object[] getPath() {
   java.lang.Object[] N = new java.lang.Object[path.size()];
   path.toArray(N);
   return N;
}

/**
 * Return the k:th node on this path.
 */
public java.lang.Object get(int k) {
   return path.get(k);
}

/**
 * Return the first node on this path.
 */
public java.lang.Object firstNode() {
   return path.firstElement();
}

/**
 * Return the first last on this path.
 */
public java.lang.Object lastNode() {
   return path.lastElement();
}

/**
 * Return the segment of this path between node <code>first</code>
 * and <code>last</code>.
 * @param first   the first node on the path
 * @param last    the last node on the path
 */
public sandmark.util.newgraph.Path segment(
    java.lang.Object first, 
    java.lang.Object last) {
   sandmark.util.newgraph.Path P = new sandmark.util.newgraph.Path();
   boolean onPath = false;
   java.util.Enumeration enum = elements();
   while (enum.hasMoreElements()) {
      java.lang.Object n = enum.nextElement();
      if (n.equals(first))
      onPath = true;
      if (n.equals(last))
          return P;
      if (onPath)
         P.add(n);
    }
    return P;
}

/**
 * Return a new path consisting of the nodes on the current path
 * followed by the nodes on <code>P</code>.
 * @param P   the path to be added.
 */
public sandmark.util.newgraph.Path concatenate (
   sandmark.util.newgraph.Path P) {
   sandmark.util.newgraph.Path N = new sandmark.util.newgraph.Path(this);
   java.util.Enumeration enum = P.elements();
   while (enum.hasMoreElements()) {
      java.lang.Object node = enum.nextElement();
      N.add(node);
    }
    return N;
}

/**
 * Return the number of nodes on this path.
 */
public int size() {
   return path.size();
}

/**
 * Return an enumerator for the nodes on this path.
 */
public java.util.Enumeration elements() {
   return path.elements();
}

/**
 * Return an iterator for the nodes on this path.
 */
public java.util.Iterator iterator() {
   return new sandmark.util.Enum2Iter(path.elements());
}

/**
 * Return true if <code>path2</code> is equal to this path.
 */
public boolean equals (java.lang.Object path2) {
   if (!(path2 instanceof Path))
      return false;

   sandmark.util.newgraph.Path P = (sandmark.util.newgraph.Path)path2;
   java.util.Iterator iter1 = path.listIterator(0);
   java.util.Iterator iter2 = P.iterator();
   while (iter1.hasNext() && iter2.hasNext()) {
      java.lang.Object n1 = iter1.next();
      java.lang.Object n2 = iter2.next();
      if (!n1.equals(n2)) return false;
   }
   if (iter1.hasNext() || iter2.hasNext())
      return false;
   return true;
}

/**
 * Return a hash value for this path.
 */
public int hashCode() {
   int H = 0;
   java.util.Iterator iter = path.listIterator(0);
   while (iter.hasNext()) {
      java.lang.Object n = iter.next();
      H += n.hashCode();
   }
   return H;
}

/**
 * Generate a string representation of this paths.
 */
public String toString() {
   String S = "";
   java.util.Iterator iter = path.listIterator(0);
   S += "[";
   boolean firstTime = true;
   while (iter.hasNext()) {
      java.lang.Object n = iter.next();
      if (!firstTime) S += ",";
      S += n;
      firstTime = false;
   }
   S += "]";
   return S;
}
}

