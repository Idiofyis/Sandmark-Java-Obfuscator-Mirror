package sandmark.util.newgraph;

public interface Style {
   abstract public int getNodeColor(java.lang.Object node);
   abstract public int getNodeShape(java.lang.Object node);
   abstract public int getNodeStyle(java.lang.Object node);
   abstract public int getNodeFontsize(java.lang.Object node);
   abstract public boolean isNodeLabeled(java.lang.Object node);
   abstract public java.lang.String getNodeLabel(java.lang.Object node);
   abstract public java.lang.String getNodeLongLabel(java.lang.Object node);

   abstract public int getEdgeColor(Edge e);
   abstract public int getEdgeStyle(Edge e);
   abstract public int getEdgeFontsize(Edge e);
   abstract public boolean isEdgeLabeled(Edge e);
}
