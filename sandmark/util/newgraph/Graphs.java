package sandmark.util.newgraph;

public class Graphs {
   public static Graph createGraph
      (java.util.Iterator nodeIterator,java.util.Iterator edgeIterator) {
      if (edgeIterator == null || !edgeIterator.hasNext()) {
         if (nodeIterator == null || !nodeIterator.hasNext())
            return new EmptyGraph();
         else
            return new EdgelessGraph(nodeIterator);
      }
      else
         return new GraphImpl(nodeIterator, edgeIterator);
   }

   private static String dotColor(int color) {
      switch (color) {
      case GraphStyle.BLACK:
         return "black";

      default:
         throw new java.lang.RuntimeException("unknown color");
      }
   }

   private static String dotShape(int shape) {
      return "record";
   }

   private static String dotStyle(int style) {
      switch (style) {
      case GraphStyle.SOLID:
         return "solid";

      default:
         throw new java.lang.RuntimeException("unknown style");
      }
   }
   
   public static String toDot(MutableGraph g, GraphStyle style) {
      return toDot(g.graph(), style.localize(g));
   }

   private static String toDot(Graph g, Style style) {
      String s = "digraph sandmark {\n";
      s += "   page=\"8.5,11\";\n";
      s += "   margin=0;\n";
      s += "   ratio=auto;\n";
      s += "   pagedir=TL;\n";
      
      java.util.Iterator i = g.nodes();
      java.util.Map nodeNames = new java.util.HashMap();
      int count = 0;
      while (i.hasNext()) {
         java.lang.Object node = i.next();
         String name = "n" + count;
         count++;
         s += "   " + name + " [";
         if (style.isNodeLabeled(node)) {
            String label = style.getNodeLabel(node);
            label = label.replaceAll("\n","\\\\n");
            label = label.replaceAll("[<>]"," ");
            label = label.replace('{',' ').replace('}',' ').replace(':',' ');
            s += "label=\"" + label + "\",";
         } 
         s += "color=" + dotColor(style.getNodeColor(node)) + ",";
         s += "shape=" + dotShape(style.getNodeShape(node)) + ",";
         s += "fontsize=" + style.getNodeFontsize(node) + "];\n";
         nodeNames.put(node, name);
      }

      i = g.edges();
      while (i.hasNext()) {
         Edge e = (Edge)i.next();
         s += "   " + nodeNames.get(e.sourceNode()) + " -> " 
            + nodeNames.get(e.sinkNode()) + " [";
         s += "style=" + dotStyle(style.getEdgeStyle(e)) + ",";
         if (style.isEdgeLabeled(e))
            s += "label=\"" + e + "\",";
         s += "color=" + dotColor(style.getEdgeColor(e)) + ",";
         s += "fontsize=" + style.getEdgeFontsize(e) + "];\n";
      }

      s += "}\n";

      return s;
   }

   public static String toDot(Graph g, GraphStyle style) {
      return toDot(g, style.localize(g));
   }

   public static void dotInFile(Graph g, GraphStyle style, String filename) {
      try{
         new java.io.PrintStream(new java.io.FileOutputStream(filename)).println(toDot(g, style));
      }
      catch(java.io.IOException ioe){
         System.out.println("Error printing graph to dot file " + filename + ":" + ioe);
      }
   }

   public static void dotInFile(Graph g, Style style, String filename) {
      try{
         new java.io.PrintStream(new java.io.FileOutputStream(filename)).println(toDot(g, style));
      }
      catch(java.io.IOException ioe){
         System.out.println("Error printing graph to dot file " + filename + ":" + ioe);
      }
   }

   public static String toDot(MutableGraph g) {
      return toDot(g, new EditableGraphStyle());
   }

   public static String toDot(Graph g) {
      return toDot(g, new EditableGraphStyle());
   }

   public static void dotInFile(MutableGraph g, String filename) {
      dotInFile(g.graph(), filename);
   }

   public static void dotInFile(Graph g, String filename) {
      dotInFile(g, new EditableGraphStyle(), filename);
   }

   public static Graph labelEdges(Graph g, String [] names) {

      for (java.util.Iterator nodeIter = g.nodes(); nodeIter.hasNext(); ) {
         int edgeNum = 0;
         java.lang.Object n = nodeIter.next();
         for (java.util.Iterator i = g.outEdges(n); i.hasNext(); edgeNum++) {
            Edge e = (Edge)i.next();
            int nameIndex = e instanceof sandmark.util.newgraph.TypedEdge ?
                  ((sandmark.util.newgraph.TypedEdge)e).getType() : edgeNum;
                  
            assert nameIndex < names.length : "need more field names";
            
            LabeledEdge newEdge = new LabeledEdge(e.sourceNode(),
                                                  e.sinkNode(),
                                                  names[nameIndex]);
            g = g.removeEdge(e).addEdge(newEdge);
         }
      }

      return g;
   }

   //    static void printSlots(Graph g) {
   //       if (g instanceof AbstractGraph) {
   // 	 System.out.println("----------------------------------------------");
   // 	 AbstractGraph ag = (AbstractGraph)g;
   // 	 for (int slot = 0; slot < 4; slot++) {
   // 	    NodeWrapperIterator i = ag._nodes();
   // 	    NodeWrapper n = i.getNext();
   // 	    while (n != null) {
   // 	       System.out.println("slot value on slot " + slot + " for node "
   // 				  + n.node + ": " + n.getSlot(slot));
   // 	       n = i.getNext();
   // 	    }
   // 	 }
   //       }
   //    }


   /*  this is the old reducibility test. it's slow.
   public static boolean reducible(Graph g, java.lang.Object root,
                                   Graph domtree) {
      return !findBadCycles(g, root, domtree, new java.util.HashSet());
   }

   private static boolean findBadCycles(Graph g, java.lang.Object n, 
                                        Graph domtree,
                                        java.util.Set seen) {
      java.util.Iterator succs = g.succs(n);
      while (succs.hasNext()) {
         java.lang.Object dest = succs.next();
         if (!domtree.reachable(dest, n)) {
            if (seen.contains(dest))
               return true;
            seen.add(n);
            boolean found = findBadCycles(g, dest, domtree, seen);
            seen.remove(n);
            if (found)
               return true;
         }
      }

      return false;
   }
   */


   /** Reducibility test: 
    *  Step 1: remove all backedges
    *  Step 2: test the remaining graph for cycles
    *  the original graph is reducible iff there are no cycles in the remaining graph
    */
   public static boolean reducible(Graph g, Object root, Graph domtree){
      for (java.util.Iterator edges=g.edges();edges.hasNext();){
         Edge edge = (Edge)edges.next();
         if (domtree.reachable(edge.sinkNode(), edge.sourceNode()) && 
             edge.sinkNode()!=edge.sourceNode())
            g = g.removeEdge(edge);
      }
      // removed all backedges, the rest of the graph should be a DAG.
      // hunt for cycles

      java.util.LinkedList path = new java.util.LinkedList();
      path.add(root);
      return !hasCycles(g, path, new java.util.HashSet(), root);
   }

   /** This method only works on a rooted graph!
    */
   private static boolean hasCycles(Graph g, java.util.LinkedList path, 
                                    java.util.HashSet nonCycleNodes, Object currentNode){
      for (java.util.Iterator outedges=g.outEdges(currentNode);outedges.hasNext();){
         Edge edge = (Edge)outedges.next();
         if (path.contains(edge.sinkNode()))
            return true;
      }
      for (java.util.Iterator outedges=g.outEdges(currentNode);outedges.hasNext();){
         Edge edge = (Edge)outedges.next();
         if (nonCycleNodes.contains(edge.sinkNode()))
            continue;
         path.addLast(edge.sinkNode());
         if (hasCycles(g, path, nonCycleNodes, edge.sinkNode()))
            return true;
         nonCycleNodes.add(edge.sinkNode());
         path.removeLast();
      }
      return false;
   }
}
