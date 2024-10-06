package sandmark.watermark.ct.trace.callforest;

public class Path extends sandmark.util.newgraph.Path implements java.lang.Comparable {

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
      sandmark.watermark.ct.trace.callforest.Node node =
         (sandmark.watermark.ct.trace.callforest.Node) enum.nextElement();
      add(node);
   }
}

/**
 * Generate a string representation of this paths.
 */
public String toString() {
   return super.toString() + ": " + pathWeight();
}

/**
 * Return the weight of this path(sum of the weights of the node in the path).
 */
public int pathWeight() {
   int weight = 0;
   java.util.Iterator iter = iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.trace.callforest.Node node =
         (sandmark.watermark.ct.trace.callforest.Node) iter.next();
      weight += node.getWeight();
   }
   return weight;
}

//calculates the weight of a path in the forest(including the edge weights)
public static int getWeight(sandmark.watermark.ct.trace.callforest.Path path,sandmark.watermark.ct.trace.callforest.Forest forest)
{
	java.util.Vector f=forest.getForest();
	int sum=0;
	sandmark.watermark.ct.trace.callforest.Node last=
            		(sandmark.watermark.ct.trace.callforest.Node)path.lastNode();

	sandmark.watermark.ct.trace.callforest.Node first=
					(sandmark.watermark.ct.trace.callforest.Node)path.firstNode();

	sum+=last.getWeight()+first.getWeight();

	java.util.Iterator iter = path.iterator();
	sandmark.watermark.ct.trace.callforest.Node prev=
		(sandmark.watermark.ct.trace.callforest.Node)iter.next();
 	while (iter.hasNext()) {
 	     sandmark.watermark.ct.trace.callforest.Node curr =
         (sandmark.watermark.ct.trace.callforest.Node) iter.next();
 		 int mark=0;

   		java.util.Enumeration enum = f.elements();
	   while(enum.hasMoreElements()) {
	      sandmark.util.newgraph.MutableGraph graph = 
		 (sandmark.util.newgraph.MutableGraph) enum.nextElement();
	      java.util.Iterator edgeEnum = graph.edges();
	      while (edgeEnum.hasNext()) {
	         sandmark.watermark.ct.trace.callforest.Edge edge =
	            (sandmark.watermark.ct.trace.callforest.Edge) edgeEnum.next();

	         if(edge.sourceNode()==prev && edge.sinkNode()==curr)
	         {
				sum += edge.getWeight();
				mark=1;
		     }
	      }
       }
	 if(mark==0)
	 { System.out.println("couldn't find weight for "+prev+"\ncurrrr="+curr);
		System.exit(0);
 	 }

	  prev=curr;
   }
   return sum;
//return path.pathWeight();
}


/**
 * Return the number of sm$mark()/ENTER-nodes in the path.
 */
public int numberOfMarkNodes() {
   int nodes = 0;
   java.util.Iterator iter = iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.trace.callforest.Node node =
         (sandmark.watermark.ct.trace.callforest.Node) iter.next();
      if (node.isMarkNode() && node.isEnterNode())
         nodes++;
   }
   return nodes;
}


/**
 * Return true if <code>path2</code> is equal to this path.
 */
public boolean equals (java.lang.Object path2) {
   return super.equals(path2);
}

/**
 * Return (-1,0,1), the result of comparing <em>the weight</em> of
 * this path to that of <code>p2</code>.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * <p>
 * Two paths are equal if they contain the same nodes in the same order.
 * <code>p1.compareTo(p2)</code> will return 0, however, if the two paths
 * have the same weight.
 */
public int compareTo(java.lang.Object p2) {
   int w1 = pathWeight();
   int w2 = ((sandmark.watermark.ct.trace.callforest.Path)p2).pathWeight();
   if (w1 == w2)
      return 0;
   else if (w1 < w2)
      return -1;
   else
      return 1;
}

/**********************************************************************/
static sandmark.watermark.ct.trace.callforest.Node mkNode (
   int number,
   int weight) {
   return new sandmark.watermark.ct.trace.callforest.Node (
             number,
             new sandmark.util.StackFrame(
                new sandmark.util.ByteCodeLocation(
                   new sandmark.util.MethodID("a","b","c"),
                   10, 20
                ),
                1
             ),
             weight,
             sandmark.watermark.ct.trace.callforest.Node.ENTER);
}

static void testA() {
     sandmark.watermark.ct.trace.callforest.Node n11 = mkNode(1,11);
     sandmark.watermark.ct.trace.callforest.Node n12 = mkNode(2,12);
     sandmark.watermark.ct.trace.callforest.Node n13 = mkNode(3,13);
     sandmark.watermark.ct.trace.callforest.Node n14 = mkNode(4,14);

     sandmark.watermark.ct.trace.callforest.Node n21 = mkNode(1,21);
     sandmark.watermark.ct.trace.callforest.Node n22 = mkNode(2,22);
     sandmark.watermark.ct.trace.callforest.Node n23 = mkNode(3,23);
     sandmark.watermark.ct.trace.callforest.Node n24 = mkNode(4,24);

     sandmark.watermark.ct.trace.callforest.Path p1 =
         new sandmark.watermark.ct.trace.callforest.Path();
     p1.add(n11);
     p1.add(n12);
     p1.add(n13);
     p1.add(n14);

     System.out.println("Path p1: " + p1.toString());

     sandmark.watermark.ct.trace.callforest.Path p2 =
         new sandmark.watermark.ct.trace.callforest.Path();
     p2.add(n21);
     p2.add(n22);
     p2.add(n23);
     p2.add(n24);

     System.out.println("Path p2: " + p2.toString());


     System.out.println("p1.compareTo(p1)=" + p1.compareTo(p1));
     System.out.println("p1.compareTo(p2)=" + p1.compareTo(p2));
     System.out.println("p2.compareTo(p1)=" + p2.compareTo(p1));
     System.out.println("p2.compareTo(p2)=" + p2.compareTo(p2));

     System.out.println("p1.equals(p1)=" + p1.equals(p1));
     System.out.println("p1.equals(p2)=" + p1.equals(p2));
     System.out.println("p2.equals(p1)=" + p2.equals(p1));
     System.out.println("p2.equals(p2)=" + p2.equals(p2));
   }

  public static void main (String[] args) {
     System.out.println("-----------------------------------");
     testA();
  }
} // class Path


