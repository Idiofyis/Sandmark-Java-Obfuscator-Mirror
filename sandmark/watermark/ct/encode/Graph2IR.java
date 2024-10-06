package sandmark.watermark.ct.encode;

/**
 *  This class converts a sandmark.util.newgraph.MutableGraph into a program
 *  that builds this graph. The program is made up of intermedite
 *  code statements, taken from sandmark.watermark.ct.encode.ir.*.
 * <P>
 * Call like this:
 * <PRE>
 *    sandmark.util.newgraph.MutableGraph graph = ...
 *    sandmark.watermark.ct.encode.Split split = new sandmark.watermark.ct.encode.Split(graph,2);
 *    split.split();
 *    sandmark.watermark.ct.encode.storage.GlobalStorage storage = ...
 *    sandmark.watermark.ct.encode.ir.Build B = 
 *       sandmark.watermark.ct.encode.Graph2IR(graph, 
 *                                split.subGraphs, 
 *                                split.componentGraph, 
 *                                storage);
 * </PRE>
 * Each subgraph is turned into a Java method that when run, will
 * build that subgraph. A set of fixup methods are also created
 * which should be run to connect the subgraphs in order to
 * create the complete graph.
 */

public class Graph2IR {

public static sandmark.watermark.ct.encode.ir.Build gen(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph[] subGraphs, 
   sandmark.util.newgraph.MutableGraph componentGraph,
   sandmark.watermark.ct.encode.storage.GlobalStorage storage) {
   sandmark.watermark.ct.encode.ir.List creators = genCreators(graph, subGraphs, true);
   sandmark.watermark.ct.encode.ir.List fixups = genFixups(graph, subGraphs);
   sandmark.watermark.ct.encode.ir.List destructors = new sandmark.watermark.ct.encode.ir.List();
   sandmark.watermark.ct.encode.ir.List init = new sandmark.watermark.ct.encode.ir.List();
   sandmark.watermark.ct.encode.ir.Build build = new sandmark.watermark.ct.encode.ir.Build(
      graph, subGraphs, componentGraph, 
      init, creators, fixups, destructors, storage);
   return build;
}

//======================      Links     ===========================
private static sandmark.watermark.ct.encode.ir.List addLinks(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph subGraph,
   sandmark.util.newgraph.Node node,
   java.util.Set createdNodes,
   java.util.Set addedEdges) {
   sandmark.watermark.ct.encode.ir.List ops = new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator iter = subGraph.outEdges(node);
   while (iter.hasNext()) {
      sandmark.util.newgraph.LabeledEdge edge = 
	 (sandmark.util.newgraph.LabeledEdge) iter.next();
      boolean legalEdge = createdNodes.contains(edge.sinkNode());
      if (legalEdge && !addedEdges.contains(edge)) {
	 sandmark.watermark.ct.encode.ir.AddEdge ae = 
	    new sandmark.watermark.ct.encode.ir.AddEdge(graph, subGraph, 
							subGraph, edge, "");
	 ops.cons(ae);
	 addedEdges.add(edge);
      }
   }
   return ops;
}

private static class Path {
   java.util.LinkedList edges=null;
   public Path () {
      edges = new java.util.LinkedList();
   }
   public Path cons(sandmark.util.newgraph.Edge e) {
      Path P = new Path();
      P.edges = (java.util.LinkedList) this.edges.clone();
      P.edges.add(e);
      return P;
   }
   public java.util.Iterator iterator() {
       return edges.listIterator();
   }
}

private static Path findPath(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.Graph dftree,
   sandmark.util.newgraph.Node root, 
   sandmark.util.newgraph.Node node,
   Path currentPath) {
   if (root.equals(node))
      return currentPath;
   java.util.Iterator iter = graph.outEdges(root);
   while (iter.hasNext()) {
      sandmark.util.newgraph.Edge edge  = 
	 (sandmark.util.newgraph.Edge) iter.next();
      if (dftree.hasEdge(edge)) {
	 sandmark.util.newgraph.Node nextNode = 
	    (sandmark.util.newgraph.Node)edge.sinkNode();
	 Path newPath = currentPath.cons(edge);
	 Path nextPath = findPath(graph, dftree, nextNode, node, newPath);
	 if (nextPath != null)
	    return nextPath;
      }
   };
   return null;
}

private static Path findPath(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.Node root, 
   sandmark.util.newgraph.Node node) {
   Path P = new Path();
   Path Q = findPath(graph, graph.depthFirstTree(root), root, node, P);
   return Q;
}

//======================     Creators    ===========================
private static sandmark.watermark.ct.encode.ir.List genCreator_addForwardLinks (
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph subGraph,
   java.util.Set addedEdges) {
   sandmark.watermark.ct.encode.ir.List ops = new sandmark.watermark.ct.encode.ir.List();

   if(subGraph.getRoot() == null)
       throw new Error("woot!G2I");
   java.util.Iterator iter = subGraph.postOrder(subGraph.getRoot());
   java.util.HashSet createdNodes = new java.util.HashSet();
   while (iter.hasNext()) {
      sandmark.util.newgraph.Node node = 
	 (sandmark.util.newgraph.Node) iter.next();
      ops.cons(new sandmark.watermark.ct.encode.ir.CreateNode(graph, subGraph, node));
      createdNodes.add(node);
      sandmark.watermark.ct.encode.ir.List links = 
         addLinks(graph, subGraph, node, createdNodes,addedEdges);
      ops.cons(links);
   };
   return ops;
}

private static sandmark.watermark.ct.encode.ir.List genCreator_addBackwardLinks (
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph subGraph,
   java.util.Set addedEdges) {
    java.util.HashSet createdNodes = new java.util.HashSet();
    for(java.util.Iterator it = subGraph.nodes() ; it.hasNext() ; )
	createdNodes.add(it.next());

   sandmark.watermark.ct.encode.ir.List ops = 
       new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator iter = subGraph.depthFirst(subGraph.getRoot());
   while (iter.hasNext()) {
      sandmark.util.newgraph.Node node = 
	 (sandmark.util.newgraph.Node) iter.next();
      sandmark.watermark.ct.encode.ir.List links = 
         addLinks(graph, subGraph, node, createdNodes,addedEdges);
      ops.cons(links);
   }
   return ops;
}

private static sandmark.watermark.ct.encode.ir.Method genCreator(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph subGraph,
   boolean isCreator) {

   sandmark.watermark.ct.encode.ir.List ops = 
      new sandmark.watermark.ct.encode.ir.List( 
         new sandmark.watermark.ct.encode.ir.PrintGraph(subGraph)
      );
   java.util.HashSet addedEdges = new java.util.HashSet();
   sandmark.watermark.ct.encode.ir.List forward = 
       genCreator_addForwardLinks(graph, subGraph,addedEdges);
   sandmark.watermark.ct.encode.ir.List backward = 
       genCreator_addBackwardLinks(graph, subGraph,addedEdges);
   ops.cons(forward, backward);
   sandmark.watermark.ct.encode.ir.Method method;
   if (isCreator) 
      method = new sandmark.watermark.ct.encode.ir.Create(graph, subGraph, ops);
   else
      method = new sandmark.watermark.ct.encode.ir.Destroy(graph, subGraph, ops);
   return method;
}

private static sandmark.watermark.ct.encode.ir.List genCreators(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph[] components,
   boolean isCreator) {
   sandmark.watermark.ct.encode.ir.List methods = new sandmark.watermark.ct.encode.ir.List();
   for(int i=0; i<components.length; i++) {
      sandmark.watermark.ct.encode.ir.IR m = genCreator(graph, components[i], isCreator);
      methods.cons(m);
   };
   return methods;
}

//======================      Fixups     ===========================
private static sandmark.watermark.ct.encode.ir.List graph2links(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph subGraph, 
   Path path) {
   sandmark.watermark.ct.encode.ir.List ops = new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator iter =  path.iterator();
   while (iter.hasNext()) {
      sandmark.util.newgraph.LabeledEdge edge = 
	 (sandmark.util.newgraph.LabeledEdge) iter.next();
      sandmark.util.newgraph.Node sink =
	 (sandmark.util.newgraph.Node)edge.sinkNode();
      sandmark.watermark.ct.encode.ir.IR n = 
         new sandmark.watermark.ct.encode.ir.FollowLink(graph, subGraph, 
							sink, edge, "");
      ops.cons(n);
   }
   return ops;
}

private static  sandmark.watermark.ct.encode.ir.IR genFixup(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph subGraph1, 
   sandmark.util.newgraph.MutableGraph subGraph2, 
   java.util.HashSet edges) {
   sandmark.util.newgraph.MutableGraph fromGraph =  null;
   sandmark.util.newgraph.MutableGraph toGraph =  null;
   sandmark.util.newgraph.Node root1 = 
      (sandmark.util.newgraph.Node)subGraph1.getRoot();
   sandmark.util.newgraph.Node root2 = 
      (sandmark.util.newgraph.Node)subGraph2.getRoot();
   Path fromPath = null;
   Path toPath = null;
   sandmark.watermark.ct.encode.ir.List ops = new sandmark.watermark.ct.encode.ir.List();
   java.util.Iterator iter = edges.iterator();
   while (iter.hasNext()) {
      sandmark.util.newgraph.LabeledEdge edge = 
	 (sandmark.util.newgraph.LabeledEdge) iter.next();
      sandmark.util.newgraph.Node From = 
	 (sandmark.util.newgraph.Node)edge.sourceNode();
      sandmark.util.newgraph.Node To   = 
	 (sandmark.util.newgraph.Node)edge.sinkNode();
      if (subGraph1.hasNode(From)) {
         fromPath = findPath(subGraph1, root1, From);
         toPath   = findPath(subGraph2, root2, To);
         fromGraph = subGraph1;
         toGraph = subGraph2;
      } else {
         fromPath = findPath(subGraph2, root2, From);
         toPath = findPath(subGraph1, root1, To);
         fromGraph = subGraph2;
         toGraph = subGraph1;
      };
      sandmark.watermark.ct.encode.ir.List fromLinks = 
         graph2links(graph, fromGraph, fromPath);
      sandmark.watermark.ct.encode.ir.List toLinks = 
         graph2links(graph, toGraph, toPath);
      ops.cons(fromLinks, toLinks);
      sandmark.watermark.ct.encode.ir.IR e = 
         new sandmark.watermark.ct.encode.ir.AddEdge(graph, fromGraph, toGraph, edge, "");
      ops.cons(e);
   }

   return new sandmark.watermark.ct.encode.ir.Fixup(graph, subGraph1, subGraph2, ops);
}

private static sandmark.watermark.ct.encode.ir.List genFixups(
   sandmark.util.newgraph.MutableGraph graph, 
   sandmark.util.newgraph.MutableGraph[] subGraphs)  {
   sandmark.watermark.ct.encode.ir.List methods = new sandmark.watermark.ct.encode.ir.List();
   for(int i=0; i<subGraphs.length; i++) {
      sandmark.util.newgraph.MutableGraph f = subGraphs[i];
      for(int j=i+1; j<subGraphs.length; j++) {
         sandmark.util.newgraph.MutableGraph t = subGraphs[j];
	 java.util.HashSet edges = new java.util.HashSet();
	 for (java.util.Iterator ei = graph.edges(); ei.hasNext(); ) {
	    sandmark.util.newgraph.Edge e = 
	       (sandmark.util.newgraph.Edge)ei.next();
	    java.lang.Object from = e.sourceNode();
	    java.lang.Object to = e.sinkNode();
	    if ((f.hasNode(from) && t.hasNode(to))
		|| (f.hasNode(to) && t.hasNode(from)))
	       edges.add(e);
	 }
         if (edges.size() > 0) {
            sandmark.watermark.ct.encode.ir.IR method = genFixup(graph, f, t, edges);
            methods.cons(method);
         }
      }
   }
   return methods;
}

//   public static void main (String[] args) {
//      System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//      System.out.println("+++++++++++++++++++++ Testing Graph2IR ++++++++++++++++++++++");
//      System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//      sandmark.util.graph.Graph graph = new sandmark.util.graph.Graph();
//      sandmark.util.graph.Node n1 = graph.addNode();
//      sandmark.util.graph.Node n2 = graph.addNode();
//      sandmark.util.graph.Node n3 = graph.addNode();
//      sandmark.util.graph.Node n4 = graph.addNode();
//      graph.setEdge(n4,n3,1);
//      graph.setEdge(n4,n2,2);
//      graph.setEdge(n3,n1,1);
//      graph.setEdge(n3,n3,2);
//      graph.setEdge(n2,n4,1);
//      graph.setEdge(n2,n1,2);
//      graph.setHeader("graph, with 4 nodes and 6 edges.");
//      graph.process();
//      sandmark.watermark.ct.encode.Split split = new Split(graph,2);
//      split.split();
//      sandmark.util.graph.Graph subGraphs[] = split.subGraphs;
//      graph.print();
//      subGraphs[0].print();
//      subGraphs[1].print();
//      System.out.println("-----------------------------------");
//      sandmark.watermark.ct.encode.ir.List creators = genCreators(graph, subGraphs, true);
//      System.out.println(creators.toString());
//      System.out.println("-----------------------------------");
//      sandmark.watermark.ct.encode.ir.List fixups = genFixups(graph, subGraphs);
//      System.out.println(fixups.toString());
//      System.out.println("-----------------------------------");
//   }
}

