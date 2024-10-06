package sandmark.watermark.ct.encode;

/**
 *  This class is responsible for splitting a 
 *  sandmark.util.newgraph.MutableGraph
 *  into an array of subgraphs. The splitting is done in such a
 *  way that each subgraph has a root, a special node from which
 *  all other nodes in the graph can be reached. This allows us
 *  to store only pointers to root nodes in global storage to
 *  prevent the garbage collector from collecting the subgraphs.
 * <P>
 * This class returns two pieces of data:
 * <OL>
 *    <LI> an array of subgraphs, and
 *    <LI> a component graph.
 * </OL>
 * The component graph has the subgraphs as nodes, and
 * the edges indicates the order in which the different
 * subgraphs should be created. I.e. the componentGraph
 * is a dependency graph whose topological order indicates
 * to us how the subgraphs should be ordered.
 * <P>
 * Call like this:
 * <PRE>
 *    sandmark.util.newgraph.MutableGraph graph = ...
 *    sandmark.watermark.ct.encode.Split split = new sandmark.watermark.ct.encode.Split(graph,2);
 *    split.split();
 *    sandmark.util.newgraph.MutableGraph subGraphs[] = split.subGraphs;
 *    sandmark.util.newgraph.MutableGraph componentGraph = 
 *       split.componentGraph;
 * </PRE>
 */

public class Split {

    public static class SplitException extends Exception {
	SplitException() {}
	SplitException(String msg) { super(msg); }
    }

public sandmark.util.newgraph.MutableGraph[] subGraphs = null;
public sandmark.util.newgraph.MutableGraph componentGraph = null;
int components = 0;
sandmark.util.newgraph.MutableGraph graph = null;

/**
 * Create a new Split object.
 * @param graph the graph to be split
 * @param components the number of components to break the graph into.
 */
public Split (
   sandmark.util.newgraph.MutableGraph graph, 
   int components) throws SplitException {
    if(components > graph.nodeCount())
	throw new SplitException
	    ("Can't split a graph with " + graph.nodeCount() + " nodes " +
	     "into " + components + " parts");
    this.components = components;
    this.graph = graph;
}

/**
 * Perform the split. Results are returned in the two 
 * public fields subGraphs and componentGraph.
 */
public void split()  {
   setRoot();
   subGraphs = KunduMisra();
   componentGraph = buildComponentGraph(graph, subGraphs);
   subGraphs = sortSubGraphs(graph, subGraphs, componentGraph);
}

   private void setRoot() {
      sandmark.util.newgraph.Graph g = graph.graph();
      int nodeCount = g.nodeCount();
      java.util.HashSet visited = new java.util.HashSet();
      for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; ) {
	 Object node = nodes.next();
	 if(visited.contains(node))
	    continue;
	 sandmark.util.newgraph.Graph dft = g.depthFirstTree(node);
	 if(dft.nodeCount() == nodeCount) {
	    graph.setRoot(node);
	    return;
	 }
	 for(java.util.Iterator dftNodes = dft.nodes() ; 
	     dftNodes.hasNext() ; )
	    visited.add(dftNodes.next());
      }
      throw new Error("Watermark graph is not weakly connected");
   }
/**
 * This method implements the Kundu-Misra graph splitting algorithm.
 */
private sandmark.util.newgraph.MutableGraph[] KunduMisra() {
   sandmark.util.newgraph.MutableGraph graph = this.graph.copy();
   //graph.process();
   int componentCount = components;
   sandmark.util.newgraph.MutableGraph subGraphs[] = 
      new sandmark.util.newgraph.MutableGraph[componentCount];
   sandmark.util.newgraph.Graph dftree = graph.depthFirstTree(graph.getRoot());
   while(graph.nodeCount() > 0) {
      int nodeCount = graph.nodeCount();
      int componentSize = 0;
      if (componentCount == 0)
          componentSize = nodeCount;
      else
          componentSize = (int)(0.5+((float)nodeCount)/((float)componentCount));
      java.util.Hashtable weights = weighGraph(graph, dftree);
      java.lang.Object root = 
	 findComponentRoot(graph, weights, componentSize);
      sandmark.util.newgraph.MutableGraph subGraph = 
	 extractComponent(graph, dftree, root);
      subGraph.setHeader("Component #"  +  root);
      subGraphs[componentCount-1] = subGraph;

      componentCount--;
   };
   return subGraphs;
}

//======================     Weighting     ===========================
/**
 * Label the tree subgraph of 'graph' such that the weight
 * of leaves is =1 and the weight of an internal node is
 * the sum of the children's weight.
 */
private void labelNodes(
   sandmark.util.newgraph.MutableGraph graph,
   sandmark.util.newgraph.Graph dftree,
   java.lang.Object node, 
   java.util.Hashtable weights) {
   if (weights.containsKey(node))
       return;
   weights.put(node, new java.lang.Integer(1));
   java.util.Iterator iter = graph.outEdges(node);
   while (iter.hasNext()) {
      sandmark.util.newgraph.Edge edge = 
	 (sandmark.util.newgraph.Edge) iter.next();
      if (dftree.hasEdge(edge)) {
	 java.lang.Object child = edge.sinkNode();
	 labelNodes(graph, dftree, child, weights);
	 int newWeight = ((java.lang.Integer)weights.get(node)).intValue() +
	    ((java.lang.Integer)weights.get(child)).intValue();
	 weights.put(node, new Integer(newWeight));
      }
   }
}

private java.util.Hashtable weighGraph(
   sandmark.util.newgraph.MutableGraph graph,
   sandmark.util.newgraph.Graph dftree) {
   java.util.Hashtable weights = new java.util.Hashtable();
   java.lang.Object root = graph.getRoot();
   labelNodes(graph, dftree, root, weights);
   return weights;
}

private void printWeights(
   java.util.Hashtable weights) {
   java.util.Enumeration iter = weights.keys();
   while (iter.hasMoreElements()) {
      java.lang.Object node = iter.nextElement();
      int weight = ((java.lang.Integer)weights.get(node)).intValue();
      System.out.println(node.toString() + " -> " + weight);
   };
    
}

//======================     Component     ===========================
/**
  * Find the root of the component whose weight is closest to
  * 'componentSize'
  */
private java.lang.Object findComponentRoot(
   sandmark.util.newgraph.MutableGraph graph, 
   java.util.Hashtable weights, 
   int componentSize) {
   int bestWeight = 10000;
   java.lang.Object bestNode = null;
   java.util.Enumeration iter = weights.keys();
   while (iter.hasMoreElements()) {
      java.lang.Object node = iter.nextElement();
      int weight = ((java.lang.Integer)weights.get(node)).intValue();
      if (java.lang.Math.abs(componentSize-weight) < 
          java.lang.Math.abs(bestWeight - componentSize)) {
        bestWeight = weight;
        bestNode  = node;
     };
   };
   return bestNode;
}

/**
 * Return the list of nodes reachable through tree edges
 * from 'node'.
 */
private java.util.HashSet findComponent(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.Graph dftree,
   java.lang.Object node) {
    java.util.HashSet component = new java.util.HashSet();
    component.add(node);
    java.util.Iterator iter = graph.outEdges(node);
    while (iter.hasNext()) {
      sandmark.util.newgraph.Edge edge = 
	 (sandmark.util.newgraph.Edge) iter.next();
      if (dftree.hasEdge(edge)) {
	 java.lang.Object child = edge.sinkNode();
	 java.util.HashSet childComponent = findComponent(graph, dftree, 
							  child);
	 component.addAll(childComponent);
      }
   };
   return component;
}

//======================     Extract     ===========================
/**
 * Return the subgraph of 'graph' whose node-set contains 
 * the nodes reachable through tree edges from 'componentRoot'
 */
private  sandmark.util.newgraph.MutableGraph extractComponent(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.Graph dftree,
   java.lang.Object componentRoot) {
   java.util.HashSet component = findComponent(graph, dftree, componentRoot);
   sandmark.util.newgraph.MutableGraph subGraph = graph.copy();
   subGraph.inducedSubgraph(component.iterator());
   graph.removeAllNodes(component.iterator());
   //subGraph.process();
   subGraph.setRoot(componentRoot);
   return subGraph;
}

//======================    SubGraphSort    ===========================

/**
 * Build the dependency-graph/component-graph for the subgraphs.
 * The data element in each node of the dependency graph
 * is the subgraph itself.
 *
 * 'node2component' is a hash table that maps each node
 * of every subgraph into the corresponding node of the
 * dependency-graph. This is built in the Phase 1.
 *
 * In Phase 2 we look at each node of 'graph'
 * and its outgoing edges. If there's an edge 
 * from->to in 'graph' and 'from' is in component 'C1'
 * and 'to' is in component 'C2', then we add an edge
 * C1->C2 to the component-graph if
 *    a) the edge is not present already, and
 *    b) C1 != C2.
 */
private sandmark.util.newgraph.MutableGraph buildComponentGraph(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph[] subGraphs) {

   /* Phase 1. */
   sandmark.util.newgraph.MutableGraph g = 
      new sandmark.util.newgraph.MutableGraph();
   g.setHeader("Component Graph");
   java.util.Hashtable node2component = new java.util.Hashtable();
   for(int i=0; i<subGraphs.length; i++) {
      sandmark.util.newgraph.MutableGraph s  = subGraphs[i];
      java.lang.Object root = s.getRoot();
      g.addNode(s);
      java.util.Iterator iter1 = s.nodes();
      while (iter1.hasNext()) {
	 java.lang.Object n = iter1.next();
         node2component.put(n, s);
      }
   }

   /* Phase 2. */
   java.util.Iterator iter2 = graph.nodes();
   while (iter2.hasNext()) {
      java.lang.Object fromNode = iter2.next();
      Object fromComponent = node2component.get(fromNode);
      java.util.Iterator iter3 = graph.outEdges(fromNode);
      while (iter3.hasNext()) {
          sandmark.util.newgraph.Edge edge  = 
	     (sandmark.util.newgraph.Edge) iter3.next();
          java.lang.Object toNode = edge.sinkNode();
          Object toComponent = node2component.get(toNode);
	  if(fromComponent != toComponent && 
	     !g.hasEdge(fromComponent,toComponent))
             g.addEdge(fromComponent, toComponent);
      };
   };

   //g.process();
   return g;
}

/**
 * Return the subgraph that contains the global root node.
 */
private sandmark.util.newgraph.MutableGraph rootSubGraph(
    sandmark.util.newgraph.MutableGraph graph, 
    sandmark.util.newgraph.MutableGraph[] subGraphs) {
    for(int i = 0; i<subGraphs.length; i++) 
	if (subGraphs[i].hasNode(graph.getRoot())) 
           return subGraphs[i];
    throw new Error("lost root node");
}

/**
 * Toplogically sort 'componentGraph', returning a list of
 * the subgraphs of 'graph', in the order in which they
 * will eventually be created. 

 * Treat the 'root subgraph' (the subgraph that contains 
 * the root node of 'graph') specially: it should always 
 * be created last. This is a bit of a hack. It would be
 * cleaner to fix the 'componentGraph' to indicate that
 * the root graph is always the last one to be created,
 * but this seems easier to implement.
 */
private sandmark.util.newgraph.MutableGraph[] sortSubGraphs(
    sandmark.util.newgraph.MutableGraph graph, 
    sandmark.util.newgraph.MutableGraph[] subGraphs, 
    sandmark.util.newgraph.MutableGraph componentGraph) {
    sandmark.util.newgraph.MutableGraph s[] = 
       new sandmark.util.newgraph.MutableGraph[componentGraph.nodeCount()];

    sandmark.util.newgraph.MutableGraph rootSubGraph = 
       rootSubGraph(graph, subGraphs);

    int i = 0;
    java.util.Iterator iter = 
       componentGraph.depthFirst(rootSubGraph);
    while (iter.hasNext()) {
         sandmark.util.newgraph.MutableGraph subGraph = 
	     (sandmark.util.newgraph.MutableGraph)iter.next();
	 if (rootSubGraph.getRoot() != subGraph.getRoot()) {
            s[i] = subGraph;
            i++;
         }
    }
    s[s.length-1]=rootSubGraph;
    return s;
}

//======================     Test     ===========================
//   private static void trySplit (
//      sandmark.util.newgraph.MutableGraph g, 
//      int components) {
//      System.out.println("-----------------------------------");
//      System.out.println("g1, split into " + components + " component:");
//      sandmark.watermark.ct.encode.Split split = new sandmark.watermark.ct.encode.Split(g,components);
//      split.split();
//      split.componentGraph.print();
//      for(int i=0; i<components; i++)
// 	 split.subGraphs[i].print();
//   }

//   public static void main (String[] args) {
//      System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//      System.out.println("+++++++++++++++++++++++ Testing Split +++++++++++++++++++++++");
//      System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//      sandmark.util.graph.Graph g1 = new sandmark.util.graph.Graph();
//      sandmark.util.graph.Node n1 = g1.addNode();
//      sandmark.util.graph.Node n2 = g1.addNode();
//      sandmark.util.graph.Node n3 = g1.addNode();
//      sandmark.util.graph.Node n4 = g1.addNode();
//      g1.setEdge(n4,n3,1);
//      g1.setEdge(n4,n2,2);
//      g1.setEdge(n3,n1,1);
//      g1.setEdge(n3,n3,2);
//      g1.setEdge(n2,n4,1);
//      g1.setEdge(n2,n1,2);
//      g1.setHeader("g1, with 4 nodes and 6 edges.");
//      g1.process();
//      g1.print();
//      //     trySplit(g1,1);
//      trySplit(g1,2);
//      trySplit(g1,3);
//      trySplit(g1,4);
//      g1.setHeader("g1, after splitting.");
//      g1.print();
//      System.out.println("-----------------------------------");
//    }
}

