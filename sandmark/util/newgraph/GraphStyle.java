package sandmark.util.newgraph;

public interface GraphStyle {
   // Colors
   public static final int BLACK = 0;

   // Shapes
   public static final int BOX = 0;
   public static final int CIRCLE = 1; 

   // Styles
   public static final int SOLID = 0;

   abstract public int getNodeColor(Graph g, java.lang.Object node);
   abstract public int getNodeShape(Graph g, java.lang.Object node);
   abstract public int getNodeStyle(Graph g, java.lang.Object node);
   abstract public int getNodeFontsize(Graph g, java.lang.Object node);
   abstract public boolean isNodeLabeled(Graph g, java.lang.Object node);
   abstract public java.lang.String getNodeLabel(Graph g,
						 java.lang.Object node);
   abstract public java.lang.String getNodeLongLabel(Graph g,
						     java.lang.Object node);

   abstract public int getNodeColor(MutableGraph g, java.lang.Object node);
   abstract public int getNodeShape(MutableGraph g, java.lang.Object node);
   abstract public int getNodeStyle(MutableGraph g, java.lang.Object node);
   abstract public int getNodeFontsize(MutableGraph g, java.lang.Object node);
   abstract public boolean isNodeLabeled(MutableGraph g, 
					 java.lang.Object node);
   abstract public java.lang.String getNodeLabel(MutableGraph g,
						 java.lang.Object node);
   abstract public java.lang.String getNodeLongLabel(MutableGraph g,
						     java.lang.Object node);
   
   abstract public int getEdgeColor(Graph g, Edge e);
   abstract public int getEdgeStyle(Graph g, Edge e);
   abstract public int getEdgeFontsize(Graph g, Edge e);
   abstract public boolean isEdgeLabeled(Graph g, Edge e);
   
   abstract public int getEdgeColor(MutableGraph g, Edge e);
   abstract public int getEdgeStyle(MutableGraph g, Edge e);
   abstract public int getEdgeFontsize(MutableGraph g, Edge e);
   abstract public boolean isEdgeLabeled(MutableGraph g, Edge e);

   abstract public Style localize(Graph g);
   abstract public Style localize(MutableGraph g);
}
