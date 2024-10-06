package sandmark.util.newgraph;

public class EditableGraphStyle extends AbstractGraphStyle {
   private class Pair {
      java.lang.Object g;
      java.lang.Object o;

      public Pair(java.lang.Object _g, java.lang.Object _o) {
	 g = _g;
	 o = _o;
      }

      public boolean equals(java.lang.Object _o) {
	 if (o instanceof Pair) {
	    Pair p = (Pair)_o;
	    return g.equals(p.g) && o.equals(p.o);
	 }
	 else
	    return false;
      }
      
      public int hashCode() {
	 return g.hashCode() ^ o.hashCode();
      }
   }

   private class NodeAttributes {
      public int color, shape, style, fontsize;
      public boolean labeled;
      public java.lang.String label;
      public java.lang.String longlabel;

      public static final int COLOR = 0;
      public static final int SHAPE = 1;
      public static final int STYLE = 2;
      public static final int FONTSIZE = 3;
      public static final int LABELED = 4;
      public static final int LABEL = 5;
      public static final int LONGLABEL = 6;
      public static final int NUM_ATTRIBUTES = 7;

      public boolean attributesSet[];

      public NodeAttributes() {
	 attributesSet = new boolean[NUM_ATTRIBUTES];
      }

      public final void setColor(int color) {
	 this.color = color;
	 attributesSet[COLOR] = true;
      }

      public final void setShape(int shape) {
	 this.shape = shape;
	 attributesSet[SHAPE] = true;
      }

      public final void setStyle(int style) {
	 this.style = style;
	 attributesSet[STYLE] = true;
      }

      public final void setFontsize(int fontsize) {
	 this.fontsize = fontsize;
	 attributesSet[FONTSIZE] = true;
      }

      public final void setLabeled(boolean labeled) {
	 this.labeled = labeled;
	 attributesSet[LABELED] = true;
      }

      public final void setLabel(java.lang.String label) {
	 this.label = label;
	 attributesSet[LABEL] = true;
      }

      public final void setLongLabel(java.lang.String label) {
	 this.longlabel = label;
	 attributesSet[LONGLABEL] = true;
      }
   }

   private class EdgeAttributes {
      public int color, style, fontsize;
      public boolean labeled;

      public static final int COLOR = 0;
      public static final int STYLE = 1;
      public static final int FONTSIZE = 2;
      public static final int LABELED = 3;
      public static final int NUM_ATTRIBUTES = 4;

      public boolean attributesSet[];

      public EdgeAttributes() {
	 attributesSet = new boolean[NUM_ATTRIBUTES];
      }

      public final void setColor(int color) {
	 this.color = color;
	 attributesSet[COLOR] = true;
      }

      public final void setStyle(int style) {
	 this.style = style;
	 attributesSet[STYLE] = true;
      }

      public final void setFontsize(int fontsize) {
	 this.fontsize = fontsize;
	 attributesSet[FONTSIZE] = true;
      }

      public final void setLabeled(boolean labeled) {
	 this.labeled = labeled;
	 attributesSet[LABELED] = true;
      }
   }

   private NodeAttributes nodeDefault;
   private EdgeAttributes edgeDefault;

   private java.util.Map nodePairs, edgePairs, graphs, nodes, edges;

   public EditableGraphStyle(int defaultNodeColor, int defaultNodeShape,
			     int defaultNodeStyle, int defaultNodeFontsize,
			     boolean defaultNodeLabeled,
			     int defaultEdgeColor, int defaultEdgeStyle,
			     int defaultEdgeFontsize,
			     boolean defaultEdgeLabeled) {
      nodeDefault = new NodeAttributes();
      nodeDefault.color = defaultNodeColor;
      nodeDefault.shape = defaultNodeShape;
      nodeDefault.style = defaultNodeStyle;
      nodeDefault.fontsize = defaultNodeFontsize;
      nodeDefault.labeled = defaultNodeLabeled;

      edgeDefault = new EdgeAttributes();
      edgeDefault.color = defaultEdgeColor;
      edgeDefault.style = defaultEdgeStyle;
      edgeDefault.fontsize = defaultEdgeFontsize;
      edgeDefault.labeled = defaultEdgeLabeled;

      nodePairs = new java.util.HashMap();
      edgePairs = new java.util.HashMap();
      graphs = new java.util.HashMap();
      nodes = new java.util.HashMap();
      edges = new java.util.HashMap();
   }

   public EditableGraphStyle() {
      this(BLACK, BOX, SOLID, 10, true, BLACK, SOLID, 10, false);
   }

   private NodeAttributes getNodeAttributes(java.lang.Object g, 
					    java.lang.Object node,
					    int attribute) {
      NodeAttributes rval = (NodeAttributes)nodePairs.get(new Pair(g, node));
      if (rval == null || !rval.attributesSet[attribute]) {
	 rval = (NodeAttributes)nodes.get(node);
	 if (rval == null || !rval.attributesSet[attribute]) {
	    rval = (NodeAttributes)graphs.get(g);
	    if (rval == null || !rval.attributesSet[attribute]) {
	       rval = nodeDefault;
	       if(node instanceof LabeledNode) {
	          LabeledNode dn = (LabeledNode)node;
	          rval.label = dn.getShortLabel();
	          rval.longlabel = dn.getLongLabel();
	       } else {
	          rval.label = rval.longlabel = node.toString();
	       }
	    }
	 }
      }

      return rval;
   }

   private EdgeAttributes getEdgeAttributes(java.lang.Object g, 
					    Edge e, int attribute) {
      EdgeAttributes rval = (EdgeAttributes)edgePairs.get(new Pair(g, e));
      if (rval == null || !rval.attributesSet[attribute]) {
	 rval = (EdgeAttributes)edges.get(e);
	 if (rval == null || !rval.attributesSet[attribute]) {
	    rval = (EdgeAttributes)graphs.get(g);
	    if (rval == null || !rval.attributesSet[attribute])
	       rval = edgeDefault;
	 }
      }

      return rval;
   }

   public int getNodeColor(Graph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.COLOR).color;
   }

   public int getNodeColor(MutableGraph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.COLOR).color;
   }

   public int getNodeShape(Graph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.SHAPE).shape;
   }

   public int getNodeShape(MutableGraph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.SHAPE).shape;
   }

   public int getNodeStyle(Graph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.STYLE).style;
   }

   public int getNodeStyle(MutableGraph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.STYLE).style;
   }

   public int getNodeFontsize(Graph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.FONTSIZE).fontsize;
   }

   public int getNodeFontsize(MutableGraph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.FONTSIZE).fontsize;
   }

   public boolean isNodeLabeled(Graph g, java.lang.Object node) {
       return getNodeAttributes(g, node, NodeAttributes.LABELED).labeled;
   }

   public boolean isNodeLabeled(MutableGraph g, java.lang.Object node) {
       return getNodeAttributes(g, node, NodeAttributes.LABELED).labeled;
   }

   public java.lang.String getNodeLabel(Graph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.LABEL).label;
   }

   public java.lang.String getNodeLabel(MutableGraph g,
					java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.LABEL).label;
   }

   public java.lang.String getNodeLongLabel(Graph g, java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.LABEL).longlabel;
   }

   public java.lang.String getNodeLongLabel(MutableGraph g,
					    java.lang.Object node) {
      return getNodeAttributes(g, node, NodeAttributes.LABEL).longlabel;
   }

   public int getEdgeColor(Graph g, Edge e) {
      return getEdgeAttributes(g, e, EdgeAttributes.COLOR).color;
   }

   public int getEdgeColor(MutableGraph g, Edge e) {
      return getEdgeAttributes(g, e, EdgeAttributes.COLOR).color;
   }

   public int getEdgeStyle(Graph g, Edge e) {
      return getEdgeAttributes(g, e, EdgeAttributes.STYLE).style;
   }

   public int getEdgeStyle(MutableGraph g, Edge e) {
      return getEdgeAttributes(g, e, EdgeAttributes.STYLE).style;
   }

   public int getEdgeFontsize(Graph g, Edge e) {
      return getEdgeAttributes(g, e, EdgeAttributes.FONTSIZE).fontsize;
   }

   public int getEdgeFontsize(MutableGraph g, Edge e) {
      return getEdgeAttributes(g, e, EdgeAttributes.FONTSIZE).fontsize;
   }

   public boolean isEdgeLabeled(Graph g, Edge e) {
      return getEdgeAttributes(g, e, EdgeAttributes.LABELED).labeled;
   }

   public boolean isEdgeLabeled(MutableGraph g, Edge e) {
      return getEdgeAttributes(g, e, EdgeAttributes.LABELED).labeled;
   }

   private NodeAttributes createNodeAttributes(java.util.Map map,
					       java.lang.Object key) {
      NodeAttributes attr = (NodeAttributes)map.get(key);
      if (attr == null) {
	 attr = new NodeAttributes();
	 map.put(key, attr);
      }
      return attr;
   }

   private EdgeAttributes createEdgeAttributes(java.util.Map map,
					       java.lang.Object key) {
      EdgeAttributes attr = (EdgeAttributes)map.get(key);
      if (attr == null) {
	 attr = new EdgeAttributes();
	 map.put(key, attr);
      }
      return attr;
   }

   public void setNodeColor(int color) {
      nodeDefault.setColor(color);
   }

   public void setNodeColor(int color, Graph g) {
      createNodeAttributes(graphs, g).setColor(color);
   }

   public void setNodeColor(int color, MutableGraph g) {
      createNodeAttributes(graphs, g).setColor(color);
   }

   public void setNodeColor(int color, java.lang.Object node) {
      createNodeAttributes(nodes, node).setColor(color);
   }

   public void setNodeColor(int color, Graph g, java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setColor(color);
   }

   public void setNodeColor(int color, MutableGraph g, java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setColor(color);
   }

   public void setNodeShape(int shape) {
      nodeDefault.setShape(shape);
   }

   public void setNodeShape(int shape, Graph g) {
      createNodeAttributes(graphs, g).setShape(shape);
   }

   public void setNodeShape(int shape, MutableGraph g) {
      createNodeAttributes(graphs, g).setShape(shape);
   }

   public void setNodeShape(int shape, java.lang.Object node) {
      createNodeAttributes(nodes, node).setShape(shape);
   }

   public void setNodeShape(int shape, Graph g, java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setShape(shape);
   }

   public void setNodeShape(int shape, MutableGraph g, java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setShape(shape);
   }

   public void setNodeStyle(int style) {
      nodeDefault.setStyle(style);
   }

   public void setNodeStyle(int style, Graph g) {
      createNodeAttributes(graphs, g).setStyle(style);
   }

   public void setNodeStyle(int style, MutableGraph g) {
      createNodeAttributes(graphs, g).setStyle(style);
   }

   public void setNodeStyle(int style, java.lang.Object node) {
      createNodeAttributes(nodes, node).setStyle(style);
   }

   public void setNodeStyle(int style, Graph g, java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setStyle(style);
   }

   public void setNodeStyle(int style, MutableGraph g, java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setStyle(style);
   }

   public void setNodeFontsize(int fontsize) {
      nodeDefault.setFontsize(fontsize);
   }

   public void setNodeFontsize(int fontsize, Graph g) {
      createNodeAttributes(graphs, g).setFontsize(fontsize);
   }

   public void setNodeFontsize(int fontsize, MutableGraph g) {
      createNodeAttributes(graphs, g).setFontsize(fontsize);
   }

   public void setNodeFontsize(int fontsize, java.lang.Object node) {
      createNodeAttributes(nodes, node).setFontsize(fontsize);
   }

   public void setNodeFontsize(int fontsize, Graph g, java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setFontsize(fontsize);
   }

   public void setNodeFontsize(int fontsize, MutableGraph g, 
			       java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setFontsize(fontsize);
   }

   public void setNodeLabeled(boolean labeled) {
      nodeDefault.setLabeled(labeled);
   }

   public void setNodeLabeled(boolean labeled, Graph g) {
      createNodeAttributes(graphs, g).setLabeled(labeled);
   }

   public void setNodeLabeled(boolean labeled, MutableGraph g) {
      createNodeAttributes(graphs, g).setLabeled(labeled);
   }

   public void setNodeLabeled(boolean labeled, java.lang.Object node) {
      createNodeAttributes(nodes, node).setLabeled(labeled);
   }

   public void setNodeLabeled(boolean labeled, Graph g, 
			      java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setLabeled(labeled);
   }

   public void setNodeLabeled(boolean labeled, MutableGraph g, 
			      java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setLabeled(labeled);
   }

   public void setNodeLabel(java.lang.String label) {
      nodeDefault.setLabel(label);
   }

   public void setNodeLabel(java.lang.String label, Graph g) {
      createNodeAttributes(graphs, g).setLabel(label);
   }

   public void setNodeLabel(java.lang.String label, MutableGraph g) {
      createNodeAttributes(graphs, g).setLabel(label);
   }

   public void setNodeLabel(java.lang.String label, java.lang.Object node) {
      createNodeAttributes(nodes, node).setLabel(label);
   }

   public void setNodeLabel(java.lang.String label, Graph g, 
			    java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setLabel(label);
   }

   public void setNodeLabel(java.lang.String label, MutableGraph g, 
			    java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setLabel(label);
   }


   public void setNodeLongLabel(java.lang.String label) {
      nodeDefault.setLongLabel(label);
   }

   public void setNodeLongLabel(java.lang.String label, Graph g) {
      createNodeAttributes(graphs, g).setLongLabel(label);
   }

   public void setNodeLongLabel(java.lang.String label, MutableGraph g) {
      createNodeAttributes(graphs, g).setLongLabel(label);
   }

   public void setNodeLongLabel(java.lang.String label,
				java.lang.Object node) {
      createNodeAttributes(nodes, node).setLongLabel(label);
   }

   public void setNodeLongLabel(java.lang.String label, Graph g, 
			    java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setLongLabel(label);
   }

   public void setNodeLongLabel(java.lang.String label, MutableGraph g, 
			    java.lang.Object node) {
      createNodeAttributes(nodePairs, new Pair(g, node)).setLongLabel(label);
   }

   public void setEdgeColor(int color) {
      edgeDefault.setColor(color);
   }

   public void setEdgeColor(int color, Graph g) {
      createEdgeAttributes(graphs, g).setColor(color);
   }

   public void setEdgeColor(int color, MutableGraph g) {
      createEdgeAttributes(graphs, g).setColor(color);
   }

   public void setEdgeColor(int color, Edge e) {
      createEdgeAttributes(edges, e).setColor(color);
   }

   public void setEdgeColor(int color, Graph g, Edge e) {
      createEdgeAttributes(edgePairs, new Pair(g, e)).setColor(color);
   }

   public void setEdgeColor(int color, MutableGraph g, Edge e) {
      createEdgeAttributes(edgePairs, new Pair(g, e)).setColor(color);
   }

   public void setEdgeStyle(int style) {
      edgeDefault.setStyle(style);
   }

   public void setEdgeStyle(int style, Graph g) {
      createEdgeAttributes(graphs, g).setStyle(style);
   }

   public void setEdgeStyle(int style, MutableGraph g) {
      createEdgeAttributes(graphs, g).setStyle(style);
   }

   public void setEdgeStyle(int style, Edge e) {
      createEdgeAttributes(edges, e).setStyle(style);
   }

   public void setEdgeStyle(int style, Graph g, Edge e) {
      createEdgeAttributes(edgePairs, new Pair(g, e)).setStyle(style);
   }

   public void setEdgeStyle(int style, MutableGraph g, Edge e) {
      createEdgeAttributes(edgePairs, new Pair(g, e)).setStyle(style);
   }

   public void setEdgeFontsize(int fontsize) {
      edgeDefault.setFontsize(fontsize);
   }

   public void setEdgeFontsize(int fontsize, Graph g) {
      createEdgeAttributes(graphs, g).setFontsize(fontsize);
   }

   public void setEdgeFontsize(int fontsize, MutableGraph g) {
      createEdgeAttributes(graphs, g).setFontsize(fontsize);
   }

   public void setEdgeFontsize(int fontsize, Edge e) {
      createEdgeAttributes(edges, e).setFontsize(fontsize);
   }

   public void setEdgeFontsize(int fontsize, Graph g, Edge e) {
      createEdgeAttributes(edgePairs, new Pair(g, e)).setFontsize(fontsize);
   }

   public void setEdgeFontsize(int fontsize, MutableGraph g, Edge e) {
      createEdgeAttributes(edgePairs, new Pair(g, e)).setFontsize(fontsize);
   }

   public void setEdgeLabeled(boolean labeled) {
      edgeDefault.setLabeled(labeled);
   }

   public void setEdgeLabeled(boolean labeled, Graph g) {
      createEdgeAttributes(graphs, g).setLabeled(labeled);
   }

   public void setEdgeLabeled(boolean labeled, MutableGraph g) {
      createEdgeAttributes(graphs, g).setLabeled(labeled);
   }

   public void setEdgeLabeled(boolean labeled, Edge e) {
      createEdgeAttributes(edges, e).setLabeled(labeled);
   }

   public void setEdgeLabeled(boolean labeled, Graph g, Edge e) {
      createEdgeAttributes(edgePairs, new Pair(g, e)).setLabeled(labeled);
   }

   public void setEdgeLabeled(boolean labeled, MutableGraph g, Edge e) {
      createEdgeAttributes(edgePairs, new Pair(g, e)).setLabeled(labeled);
   }
}
