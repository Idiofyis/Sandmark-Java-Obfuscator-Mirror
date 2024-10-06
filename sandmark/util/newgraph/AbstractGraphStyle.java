package sandmark.util.newgraph;

abstract class AbstractStyle implements Style {
   private class NodeAttributes {
      public int color, shape, style, fontsize;
      public boolean labeled;
      public java.lang.String label;
      public java.lang.String longlabel;
   }

   private class EdgeAttributes {
      public int color, style, fontsize;
      public boolean labeled;
   }

   private java.util.Map nodeAttrs, edgeAttrs;

   AbstractStyle() {
      nodeAttrs = new java.util.HashMap();
      edgeAttrs = new java.util.HashMap();
   }

   void addNode(java.lang.Object node, 
		int color, int shape, int style, int fontsize,
		boolean labeled, java.lang.String label,
		java.lang.String longlabel) {
      NodeAttributes attr = new NodeAttributes();
      attr.color = color;
      attr.shape = shape;
      attr.style = style;
      attr.fontsize = fontsize;
      attr.labeled = labeled;
      attr.label = label;
      attr.longlabel = longlabel;
      nodeAttrs.put(node, attr);
   }

   void addEdge(Edge edge,
		int color, int style, int fontsize,
		boolean labeled) {
      EdgeAttributes attr = new EdgeAttributes();
      attr.color = color;
      attr.style = style;
      attr.fontsize = fontsize;
      attr.labeled = labeled;
      edgeAttrs.put(edge, attr);
   }

   private NodeAttributes getNodeAttr(java.lang.Object node) {
      if (nodeAttrs.containsKey(node))
	 return (NodeAttributes)nodeAttrs.get(node);
      else
	 throw new java.lang.IllegalArgumentException("node not in graph");
   }

   private EdgeAttributes getEdgeAttr(Edge edge) {
      if (edgeAttrs.containsKey(edge))
	 return (EdgeAttributes)edgeAttrs.get(edge);
      else
	 throw new java.lang.IllegalArgumentException("edge not in graph");
   }

   public int getNodeColor(java.lang.Object node) {
      return getNodeAttr(node).color;
   }

   public int getNodeShape(java.lang.Object node) {
      return getNodeAttr(node).shape;
   }

   public int getNodeStyle(java.lang.Object node) {
      return getNodeAttr(node).style;
   }

   public int getNodeFontsize(java.lang.Object node) {
      return getNodeAttr(node).fontsize;
   }

   public boolean isNodeLabeled(java.lang.Object node) {
      return getNodeAttr(node).labeled;
   }

   public java.lang.String getNodeLabel(java.lang.Object node) {
       return getNodeAttr(node).label;
   }

   public java.lang.String getNodeLongLabel(java.lang.Object node) {
       return getNodeAttr(node).longlabel;
   }

   public int getEdgeColor(Edge e) {
      return getEdgeAttr(e).color;
   }

   public int getEdgeStyle(Edge e) {
      return getEdgeAttr(e).style;
   }

   public int getEdgeFontsize(Edge e) {
      return getEdgeAttr(e).fontsize;
   }

   public boolean isEdgeLabeled(Edge e) {
      return getEdgeAttr(e).labeled;
   }
}

class ImmutableGraphStyle extends AbstractStyle {
   ImmutableGraphStyle(GraphStyle s, Graph g) {
      for (java.util.Iterator i = g.nodes(); i.hasNext(); ) {
	 java.lang.Object node = i.next();
	 addNode(node, 
		 s.getNodeColor(g, node), s.getNodeShape(g, node),
		 s.getNodeStyle(g, node), s.getNodeFontsize(g, node),
		 s.isNodeLabeled(g, node), s.getNodeLabel(g, node),
		 s.getNodeLongLabel(g, node));
      }
      for (java.util.Iterator i = g.edges(); i.hasNext(); ) {
	 Edge edge = (Edge)i.next();
	 addEdge(edge,
		 s.getEdgeColor(g, edge), s.getEdgeStyle(g, edge),
		 s.getEdgeFontsize(g, edge), s.isEdgeLabeled(g, edge));
      }
   }
}

class MutableGraphStyle extends AbstractStyle {
   MutableGraphStyle(GraphStyle s, MutableGraph g) {
      for (java.util.Iterator i = g.nodes(); i.hasNext(); ) {
	 java.lang.Object node = i.next();
	 addNode(node, 
		 s.getNodeColor(g, node), s.getNodeShape(g, node),
		 s.getNodeStyle(g, node), s.getNodeFontsize(g, node),
		 s.isNodeLabeled(g, node), s.getNodeLabel(g, node),
		 s.getNodeLongLabel(g, node));
      }
      for (java.util.Iterator i = g.edges(); i.hasNext(); ) {
	 Edge edge = (Edge)i.next();
	 addEdge(edge,
		 s.getEdgeColor(g, edge), s.getEdgeStyle(g, edge),
		 s.getEdgeFontsize(g, edge), s.isEdgeLabeled(g, edge));
      }
   }
}

abstract public class AbstractGraphStyle implements GraphStyle {
   public Style localize(Graph g) {
      return new ImmutableGraphStyle(this, g);
   }

   public Style localize(MutableGraph g) {
      return new MutableGraphStyle(this, g);
   }
}
