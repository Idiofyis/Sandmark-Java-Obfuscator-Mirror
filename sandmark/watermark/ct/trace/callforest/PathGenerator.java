package sandmark.watermark.ct.trace.callforest;

public class PathGenerator implements java.util.Iterator {

java.util.Iterator orderedIterator = null;
java.util.ArrayList orderedPaths = null;

/**
 * This class generates paths between sm$mark()-nodes in the
 * call forest. We generate the paths in order of goodness,
 * with the best one first.
 */
public PathGenerator(
   java.util.Vector forest,
   int maxPaths){
   java.util.HashSet seen = new java.util.HashSet();
   java.util.TreeSet paths = new java.util.TreeSet();
   java.util.Iterator iter = new AllPathsIterator(forest);
   while (iter.hasNext() && (maxPaths>0)) {
      sandmark.watermark.ct.trace.callforest.Path path = 
         (sandmark.watermark.ct.trace.callforest.Path)iter.next();
      if (!seen.contains(path)) {
         paths.add(path);
         seen.add(path);
         maxPaths--;
      }
   }

   orderedPaths = new java.util.ArrayList();
   java.util.Iterator iter2 = paths.iterator();
   while (iter2.hasNext()) {
      sandmark.watermark.ct.trace.callforest.Path path = 
         (sandmark.watermark.ct.trace.callforest.Path)iter2.next();
      orderedPaths.add(0,path);
   }
   orderedIterator = orderedPaths.iterator();
}

public boolean hasNext() {
   return orderedIterator.hasNext();
}

public java.lang.Object next() {
   return orderedIterator.next();
}

public void remove() {}

/**
 * Generate a string representation of all the paths.
 */
public String toString() {
   String S = "";
   java.util.Iterator iter = orderedPaths.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.trace.callforest.Path path = 
         (sandmark.watermark.ct.trace.callforest.Path) iter.next();
      S += path.toString() + "\n";
   }
   return S;
}

}

/************************************************************************/

class AllPathsIterator extends sandmark.util.MultiIter {
java.util.Vector forest = null;

/**
 * This class generates paths between sm$mark()-nodes in the
 * call forest. 
 * <P>
 * <PRE>
 * for every graph g in the forest (in reverse order) do {
 *    for every mark-node n in g do {
 *       for every path p starting in n and ending in another mark-node m do {
 *          yield p;
 *       }
 *    }
 * }
 * </PRE>
 */
public AllPathsIterator(
   java.util.Vector forest){
   this.forest = forest;
}

/**
  * Start enumerator number k. 
  * elmts[0..k-1] hold the current generated values
  * for the first k enumerators.
  */
public java.util.Iterator start(
   int k, 
   java.lang.Object[] elmts) {
   if (k==0)
      return new sandmark.util.Enum2Iter(forest.elements());
   else if (k==1) {
      sandmark.util.newgraph.MutableGraph graph = 
	 (sandmark.util.newgraph.MutableGraph) elmts[0];
      java.util.ArrayList marks = markNodes(graph);
      return marks.listIterator(0);
   } else /* if (k==2) */ {
      sandmark.util.newgraph.MutableGraph graph = 
	 (sandmark.util.newgraph.MutableGraph) elmts[0];
      sandmark.watermark.ct.trace.callforest.Node node = 
         (sandmark.watermark.ct.trace.callforest.Node) elmts[1];
      return new Afs(graph,node);
   }
}

/**
  * Create the object to be returned by the enumerator.
  * Throws an exception if no element could be generated.
  */
public java.lang.Object create(
   java.lang.Object[] elmts) throws java.lang.Exception {
   return elmts[2];
}

/**
 * Return the number of iterators.
 */
public int count() {
   return 3;
}

/**
 * Generate all the mark ENTER-nodes in the graph.
 */
java.util.ArrayList markNodes(sandmark.util.newgraph.MutableGraph graph) {
   java.util.ArrayList m = new java.util.ArrayList();
   java.util.Iterator nodes = graph.nodes();
   while (nodes.hasNext()) {
      sandmark.watermark.ct.trace.callforest.Node node = 
         (sandmark.watermark.ct.trace.callforest.Node) nodes.next();
      if (node.isMarkNode() && node.isEnterNode())
         m.add(node);
   }
   return m;
}

/**
 * Generate a string representation of all the paths.
 */
public String toString() {
   String S = "";
   while (hasNext()) {
      sandmark.watermark.ct.trace.callforest.Path path = 
          (sandmark.watermark.ct.trace.callforest.Path) next();
      S += path.toString() + "\n";
   }
   return S;
}

/**
 * Return the lowest dominating node of <code>markNode1</code>
 * and <code>markNode2</code>.
 */
sandmark.watermark.ct.trace.callforest.Node dominatingNode(
   sandmark.watermark.ct.trace.callforest.Node markNode1,
   sandmark.watermark.ct.trace.callforest.Node markNode2) {
   return null;
}

/******************************************************************/
class Afs extends sandmark.util.newgraph.Afs {

   /**
    * Same as sandmark.util.newgraph.Afs except we trim off parts of
    * paths that follow the last mark()-node, and we return 
    * a sandmark.watermark.ct.trace.callforest.Path rather than
    * a sandmark.util.newgraph.Path.
    */
   public Afs (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.util.newgraph.Node root) {
      super(graph,root);
   }

   public Object next() {
      sandmark.util.newgraph.Path path = (sandmark.util.newgraph.Path) super.next();
      int lastNode = 0;
      for (int i=(path.size()-1); i>=0; i--) {
         sandmark.watermark.ct.trace.callforest.Node node = 
            (sandmark.watermark.ct.trace.callforest.Node) path.get(i);
         if (node.isMarkNode() && node.isEnterNode()) {
            lastNode = i;
            break;
         }
      }
      sandmark.watermark.ct.trace.callforest.Path newPath = 
         new sandmark.watermark.ct.trace.callforest.Path();
      for (int i=0; i<=lastNode; i++) {
         sandmark.watermark.ct.trace.callforest.Node node = 
            (sandmark.watermark.ct.trace.callforest.Node) path.get(i);
         newPath.add(node);
      }
      return newPath;
   }
}
/******************************************************************/

}


