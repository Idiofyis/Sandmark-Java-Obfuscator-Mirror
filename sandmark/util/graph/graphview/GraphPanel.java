// GraphPanel.java

package sandmark.util.graph.graphview;

/**
 * This is a panel for diplaying graphs. It displays the graph according to
 * the type of layout that was passed in the constructor.
 *
 * @author Andrzej
 */
public class GraphPanel extends javax.swing.JPanel {

    /**
     * Constructs a new Panel for drawing the graph.
     *
     * @param g graph to be drawn
     * @param style style containing information about the nodes
     * @param type type of the layout, see {@link GraphLayout} for different
     *             types
     * @param nodeInfo text area where node information can be displayed
     */
    public GraphPanel(sandmark.util.newgraph.Graph g,
                      sandmark.util.newgraph.Style style,
                      int type, javax.swing.JTextArea nodeInfo) {

        myGraph = g.consolidate();
        myGraphStyle = style;
        myNodeInfoArea = nodeInfo;
        myZoomFactor = 1.0;

        setLayout(type, g, style);

        setBackground(java.awt.Color.WHITE);

        // add mouse listener for moving nodes around
        MyMouseListener listener = new MyMouseListener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    /**
     * Sets a new layout, graph and style for this panel.
     *
     * @param type type of the layout (see {@link GraphLayout} for types)
     * @param g graph
     * @param style style of the graph
     */
    public void setLayout(int type, sandmark.util.newgraph.Graph g,
                          sandmark.util.newgraph.Style style) {
        myLayoutType = type;
        myGraph = g;
        myGraphStyle = style;

        if (type == GraphLayout.TREE_LAYOUT)
            myLayout = new TreeLayout(myGraph, myGraphStyle);
        else if (type == GraphLayout.SIMPLE_GRAPH_LAYOUT)
            myLayout = new SimpleGraphLayout(myGraph, myGraphStyle);
        else if (type == GraphLayout.HIERARCHY_TREE_LAYOUT)
            myLayout = new HierarchyTreeLayout(myGraph, myGraphStyle);
        else if (type == GraphLayout.SPRING_EMBEDDER_LAYOUT)
            myLayout = new SpringEmbedderLayout(myGraph, myGraphStyle);
        else if (type == GraphLayout.LAYERED_DRAWING_LAYOUT)
            myLayout = new LayeredDrawingLayout(myGraph, myGraphStyle);

        setPreferredSize(new java.awt.Dimension(myLayout.getMaximumX(),
                                                myLayout.getMaximumY()));
        revalidate();
        repaint();
    }

    // This is a mouse listener allowing the user to drag the nodes of the
    // graph.
    private class MyMouseListener extends javax.swing.event.MouseInputAdapter {
        public void mousePressed(java.awt.event.MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            myDragNode = checkMouseClick(x, y);
            if (myDragNode != null &&
                !(myDragNode.getNode() instanceof DummyNode)) {
                java.lang.Object node = myDragNode.getNode();
                if (myGraphStyle.isNodeLabeled(node)) {
                    java.lang.String nodeLabel =
                        myGraphStyle.getNodeLongLabel(node);
                    nodeLabel = nodeLabel.replaceAll("[<>]"," ");
                    nodeLabel = nodeLabel.trim();
                    myNodeInfoArea.setText("NODE INFORMATION\n" + nodeLabel);
                    myNodeInfoArea.setCaretPosition(0);
                }
            }
            else {
                myNodeInfoArea.setText("NODE INFORMATION");
            }

        }

        public void mouseDragged(java.awt.event.MouseEvent e) {
            if (myDragNode != null) {
                myDragNode.updatePosition(e.getX() - myDX, e.getY() - myDY);
                myDragNode.updateOriginalPositions(myZoomFactor);
                repaint();
            }
        }

        public void mouseReleased(java.awt.event.MouseEvent e) {
            myDragNode = null;
        }
    }

    // checks if the user clicked inside any of the nodes of the graph.
    private sandmark.util.graph.graphview.NodeDisplayInfo checkMouseClick(int x, int y) {
        java.util.Iterator iter = myLayout.nodeIterator();
        while (iter.hasNext()) {
            sandmark.util.graph.graphview.NodeDisplayInfo gn =
                (sandmark.util.graph.graphview.NodeDisplayInfo)iter.next();
            if (gn.contains(x, y)) {
                myDX = x - gn.getX();
                myDY = y - gn.getY();
                return gn;
            }
        }
        return null;
    }

    /**
     * Draws the graph on the panel, using the information from the layout of the
     * graph and style of the graph.
     *
     * @param g the Graphics context in which to paint
     */
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;

        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setFont(new java.awt.Font("monospaced", java.awt.Font.PLAIN, 10));

        // draw nodes
        java.util.Iterator nodes = myLayout.nodeIterator();
        while (nodes.hasNext()) {
            sandmark.util.graph.graphview.NodeDisplayInfo node =
                (sandmark.util.graph.graphview.NodeDisplayInfo)nodes.next();

            if (!(node.getNode() instanceof DummyNode))
                drawNode(g2, node);
        }

        // draw edges
        myDrawnEdges = new java.util.ArrayList();
        java.util.Iterator edges = myGraph.edges();
        while (edges.hasNext()) {
            sandmark.util.newgraph.Edge e =
                (sandmark.util.newgraph.Edge) edges.next();
            sandmark.util.graph.graphview.EdgeDisplayInfo edge =
                myLayout.getEdgeMap(e);
            sandmark.util.graph.graphview.NodeDisplayInfo source =
                myLayout.getNodeMap(e.sourceNode());
            sandmark.util.graph.graphview.NodeDisplayInfo sink =
                myLayout.getNodeMap(e.sinkNode());
            if (!((source.getX() == sink.getX())
                  && (source.getY() == sink.getY())) && !myDrawnEdges.contains(e)) {
                myDrawnEdges.add(e);
                if (myLayoutType == sandmark.util.graph.graphview.GraphLayout.HIERARCHY_TREE_LAYOUT)
                    drawBendEdge(g2, source, sink);
                else {
                    drawEdge(g2, edge, source, sink);
                    // draw other edges between source and sink
                    drawOtherEdges(g2, e.sourceNode(), e.sinkNode());
                }
            }
        }

        // draw self edges
        drawSelfEdges(g2);
    }

    private void drawSelfEdges(java.awt.Graphics2D g2) {
        java.util.Iterator nodes = myGraph.nodes();
        while (nodes.hasNext()) {
            java.lang.Object node = nodes.next();
            java.util.Iterator outEdges = myGraph.outEdges(node);
            NodeDisplayInfo nodeInfo = (NodeDisplayInfo)myLayout.getNodeMap(node);
            int x = nodeInfo.getX();
            int y = nodeInfo.getY();
            int width = nodeInfo.getWidth();
            int height = nodeInfo.getHeight();
            int numSelfEdges = 0;
            while (outEdges.hasNext()) {
                sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge)outEdges.next();
                if (e.sinkNode() == node) {
                    numSelfEdges++;

                    if (nodeInfo.getShapeType() == NodeDisplayInfo.RECTANGLE) {
                        g2.draw(new java.awt.geom.Arc2D.Double(x + width / 2 - (5 + 5 * numSelfEdges) * myZoomFactor,
                                                               y - 10 * myZoomFactor, (10 + 10 * numSelfEdges) * myZoomFactor,
                                                               20 * myZoomFactor, 270, 180, java.awt.geom.Arc2D.OPEN));
                    } else if (nodeInfo.getShapeType() == NodeDisplayInfo.CIRCLE) {
                        g2.draw(new java.awt.geom.Arc2D.Double(x + width / 2 - (5 + 5 * numSelfEdges) * myZoomFactor,
                                                               y - 10 * myZoomFactor, (10 + 10 * numSelfEdges) * myZoomFactor,
                                                               20 * myZoomFactor, 253 + 2 * numSelfEdges, 214 - 4 * numSelfEdges, java.awt.geom.Arc2D.OPEN));
                    }
                }
            }
            if (numSelfEdges > 0) {
                if (nodeInfo.getShapeType() == NodeDisplayInfo.RECTANGLE)
                    drawArrow(g2, (int)(x + width / 2 + (10 + 10 * numSelfEdges) * myZoomFactor), y, x + width / 2, (int)(y - 10 * myZoomFactor));
                else if (nodeInfo.getShapeType() == NodeDisplayInfo.CIRCLE)
                    drawArrow(g2, (int)(x + width / 2 + 10), (int)(y - 10 * myZoomFactor), (int)(x + width / 2 - 4 * myZoomFactor), (int)(y - 10 * myZoomFactor));
            }
        }
    }

    // draws multiple edges between two nodes
    private void drawOtherEdges(java.awt.Graphics2D g2, java.lang.Object source, java.lang.Object sink) {
        java.util.Iterator edges = myGraph.edges();
        int counter = 0;
        int distance = 0;
        int otherDir = 0;
        boolean isOtherDir = false;
        while (edges.hasNext()) {
            sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge)edges.next();
            sandmark.util.graph.graphview.EdgeDisplayInfo edge =
                (sandmark.util.graph.graphview.EdgeDisplayInfo)myLayout.getEdgeMap(e);
            if (((e.sourceNode().equals(source) && e.sinkNode().equals(sink)) ||
                 (e.sourceNode().equals(sink) && e.sinkNode().equals(source))) &&
                edge.getEdgeBends().size() == 0 && !myDrawnEdges.contains(e)) {
                counter++;
                myDrawnEdges.add(e);

                sandmark.util.graph.graphview.NodeDisplayInfo start =
                    (sandmark.util.graph.graphview.NodeDisplayInfo)myLayout.getNodeMap(source);
                sandmark.util.graph.graphview.NodeDisplayInfo end =
                    (sandmark.util.graph.graphview.NodeDisplayInfo)myLayout.getNodeMap(sink);

                if (e.sourceNode().equals(sink)) {
                    NodeDisplayInfo temp = start;
                    start = end;
                    end = temp;
                    isOtherDir = true;
                }
                java.awt.Point edgeStart =
                    computeEdgeStart(start.getX(), start.getY(), end.getX(), end.getY(),
                                     start.getWidth(), start.getHeight(), start.getShapeType());
                java.awt.Point edgeEnd =
                    computeEdgeStart(end.getX(), end.getY(), edgeStart.x, edgeStart.y,
                                     end.getWidth(), end.getHeight(), end.getShapeType());

                // draw curved edge
                int dir = 1;
                if (counter % 2 == 1) {
                    distance += 30;
                    dir = -1;
                }
                java.awt.Point ctrlPoint;
                // so undirected graphs just show up as double sided arrow: (interference graphs)
                if(isOtherDir && otherDir == 0) {
                    ctrlPoint = computeControlPoint(edgeStart, edgeEnd, 0, dir);
                    otherDir ++;
                }
                else ctrlPoint = computeControlPoint(edgeStart, edgeEnd, distance, dir);
                //System.out.println(ctrlPoint);
                g2.draw(new java.awt.geom.QuadCurve2D.Double(edgeStart.x, edgeStart.y, ctrlPoint.x,
                                                             ctrlPoint.y, edgeEnd.x, edgeEnd.y));
                drawArrow(g2, ctrlPoint.x, ctrlPoint.y, edgeEnd.x, edgeEnd.y);
            }
        }
    }

    // computes control point for the curve of an edge between nodes that contain multiple edges
    private java.awt.Point computeControlPoint(java.awt.Point start, java.awt.Point end, int distance, int dir) {
        double x = (end.x - start.x) / 2;
        double y = (end.y - start.y) / 2;
        double tan = 0.0;
        if (x != 0.0)
            tan = y / x;
        else
            tan = y / 0.000001;
        double angle = java.lang.Math.atan(tan);
        double dx = java.lang.Math.sin(angle) * distance;
        double dy = java.lang.Math.cos(angle) * distance;

        if ((tan < 0 && dir == 1) || (tan >= 0 && dir == -1)) {
            dx = -dx;
            dy = -dy;
        }

        return new java.awt.Point((int)(end.x - x + dx), (int)(end.y - y - dy));
    }

    // draws a node in a graph
    private void drawNode(java.awt.Graphics2D g2,
                          sandmark.util.graph.graphview.NodeDisplayInfo node) {
        java.lang.Object n = node.getNode();

        // draw node
        g2.setColor(getStyleColor(myGraphStyle.getNodeColor(n)));
        g2.draw(node.getShape());

        // draw label
        g2.setColor(java.awt.Color.BLACK);
        if (myGraphStyle.isNodeLabeled(n)) {
            java.lang.String nodeLabel = myGraphStyle.getNodeLabel(n);
            int fontSize =
                (int)(myGraphStyle.getNodeFontsize(n) * myZoomFactor);
            g2.setFont(new java.awt.Font("monospaced", java.awt.Font.PLAIN, fontSize));

            // calculate size of label
            java.awt.FontMetrics fm = g2.getFontMetrics(g2.getFont());
            int lineHeight = fm.getHeight();
            int labelWidth = 0;
            int labelHeight = 0;
            java.util.ArrayList label = new java.util.ArrayList();
            int index = 0;
            for (;;) {
                labelHeight += lineHeight;
                int newLine = nodeLabel.indexOf("\n", index);
                if (newLine == -1) {
                    String line = nodeLabel.substring(index);
                    label.add(line);
                    int lineWidth = fm.stringWidth(line);
                    if (lineWidth > labelWidth)
                        labelWidth = lineWidth;
                    break;
                }
                String line = nodeLabel.substring(index, newLine);
                label.add(line);
                int lineWidth = fm.stringWidth(line);
                if (lineWidth > labelWidth)
                    labelWidth = lineWidth;
                index = newLine + 1;
            }

            if (labelWidth <= (node.getWidth() + 20)
                && labelHeight <= (node.getHeight() + 20)
                && (myLayoutType == sandmark.util.graph.graphview.GraphLayout.LAYERED_DRAWING_LAYOUT)) {
               //TODO: support right and left justification in addition to
               //centering.
                int x = node.getX();
                int y = node.getY();
                int startX = x - labelWidth / 2;
                int startY = y - labelHeight / 2 + fontSize;
                for (int i = 0; i < label.size(); i++) {
                    String s = label.get(i).toString();
                    s = s.replaceAll("[<>]"," ");
                    s = s.trim();
                    int width = fm.stringWidth(s);
                    g2.drawString(s, x - width / 2 , startY);
                    startY += lineHeight;
                }
            }

            if (myLayoutType == sandmark.util.graph.graphview.GraphLayout.SPRING_EMBEDDER_LAYOUT &&
                label != null && label.size() > 1){
                int x = node.getX();
                int y = node.getY();
                if(((String)label.get(1)).lastIndexOf("at LV") != -1) {
                    String localVar = (String)label.get(1);
                    localVar = "LV:" + localVar.substring(localVar.length() - 1, localVar.length());
                    int width = fm.stringWidth(localVar);
                    g2.drawString(localVar, x - width/2, y);
                }
            }
        }
    }

    private void drawBendEdge(java.awt.Graphics2D g2,
                          sandmark.util.graph.graphview.NodeDisplayInfo start,
                          sandmark.util.graph.graphview.NodeDisplayInfo end) {
        int startX = start.getX();
        int startY = start.getY() + start.getHeight() / 2;
        int endX = end.getX() - end.getWidth() / 2;
        int endY = end.getY();
        g2.draw(new java.awt.geom.Line2D.Double(startX, startY, startX, endY));
        g2.draw(new java.awt.geom.Line2D.Double(startX, endY, endX, endY));
        drawArrow(g2, startX, endY, endX, endY);
    }

    // draw an edge
    private void drawEdge(java.awt.Graphics2D g2,
                          sandmark.util.graph.graphview.EdgeDisplayInfo edge,
                          sandmark.util.graph.graphview.NodeDisplayInfo start,
                          sandmark.util.graph.graphview.NodeDisplayInfo end) {
        java.awt.Point edgeStart = null;
        java.awt.Point edgeEnd = null;

        // draw bends
        java.util.Iterator bends = edge.getEdgeBends().iterator();
        while (bends.hasNext()) {
            java.awt.Point bend = (java.awt.Point)bends.next();
            if (edgeStart == null)
                edgeStart = computeEdgeStart(start.getX(), start.getY(), bend.x, bend.y,
                                             start.getWidth(), start.getHeight(), start.getShapeType());
            g2.draw(new java.awt.geom.Line2D.Double(edgeStart.x, edgeStart.y,
                                                    bend.x, bend.y));
            edgeStart = bend;
        }
        if (edgeStart == null)
            edgeStart = computeEdgeStart(start.getX(), start.getY(), end.getX(), end.getY(),
                                         start.getWidth(), start.getHeight(), start.getShapeType());
        edgeEnd = computeEdgeStart(end.getX(), end.getY(), edgeStart.x, edgeStart.y,
                                   end.getWidth(), end.getHeight(), end.getShapeType());

        g2.draw(new java.awt.geom.Line2D.Double(edgeStart.x, edgeStart.y,
                                                edgeEnd.x, edgeEnd.y));
        drawArrow(g2, edgeStart.x, edgeStart.y, edgeEnd.x, edgeEnd.y);
    }

    // draw an arrow at the end of an edge
    private void drawArrow(java.awt.Graphics2D g2, int x1, int y1, int x2, int y2) {
        double deltaY = y2 - y1;
        double deltaX = x2 - x1;

        double t = 0;
        if (deltaX == 0)
            deltaX = 0.0001;
        t = deltaY / deltaX;
        double angle = Math.atan(t);
        double angle1 = angle + Math.PI / 10;
        double angle2 = angle - Math.PI / 10;

        double dx = Math.cos(angle1) * 8 * myZoomFactor;
        double dy = Math.sin(angle1) * 8 * myZoomFactor;

        double dx2 = Math.cos(angle2) * 8 * myZoomFactor;
        double dy2 = Math.sin(angle2) * 8 * myZoomFactor;

        double side1X = x2;
        double side2X = x2;
        double side1Y = y2;
        double side2Y = y2;

        if (deltaX >= 0) {
            side1X -= dx;
            side2X -= dx2;
            side1Y -= dy;
            side2Y -= dy2;
        } else {
            side1X += dx;
            side2X += dx2;
            side1Y += dy;
            side2Y += dy2;
        }


        //g2.draw(new java.awt.geom.Line2D.Double(x2, y2, side1X, side1Y));
        //g2.draw(new java.awt.geom.Line2D.Double(x2, y2, side2X, side2Y));
        //g2.draw(new java.awt.geom.Line2D.Double(side1X, side1Y, side2X, side2Y);
        java.awt.Polygon arrow = new java.awt.Polygon(new int[] {(int)x2, (int)side1X, (int)side2X},
                                     new int[] {(int)y2, (int)side1Y, (int)side2Y}, 3);
        g2.draw(arrow);
        g2.fill(arrow);
    }

    // compute the start of the edge between two nodes
    private java.awt.Point computeEdgeStart(int startX, int startY, int endX, int endY,
                                            int width, int height, int shapeType) {
        if (shapeType == NodeDisplayInfo.CIRCLE) {
            int dX = endX - startX;
            int dY = endY - startY;

            double slope = dY / (double)dX;
            double radius = width / 2.0;
            double deltaX = Math.sqrt((radius * radius) / (slope * slope + 1));
            double deltaY = Math.sqrt((radius * radius) - (deltaX * deltaX));

            int sX, sY = 0;
            if (dX > 0)
                sX = (int)(startX + deltaX);
            else
                sX = (int)(startX - deltaX);
            if (dY > 0)
                sY = (int)(startY + deltaY);
            else
                sY = (int)(startY - deltaY);
            return new java.awt.Point(sX, sY);
        } else if (shapeType == NodeDisplayInfo.RECTANGLE) {
            int x = startX;
            int y = startY;
            if (startY < endY) {
                y = startY + height / 2;
            } else {
                y = startY - height / 2;
            }
            return new java.awt.Point(x, y);
        }
        return null;
    }

    private java.awt.Color getStyleColor(int color) {
        switch (color) {
        case sandmark.util.newgraph.GraphStyle.BLACK:
            return java.awt.Color.BLACK;

        default:
            return java.awt.Color.BLACK;
        }
    }

    public void resizeGraph(double factor) {
        factor = java.lang.Math.max(factor, 0.001);
        double zoomFactor = factor / myZoomFactor;
        myZoomFactor = factor;
        java.awt.Rectangle r = getVisibleRect();
        int centerX = (int)((r.x + r.width / 2) * zoomFactor);
        int centerY = (int)((r.y + r.height / 2) * zoomFactor);

        myLayout.resizeGraph(myZoomFactor);
        setPreferredSize(new java.awt.Dimension(myLayout.getMaximumX(),
                                                myLayout.getMaximumY()));
        int x = r.width / 2 - centerX;
        int y = r.height / 2 - centerY;
        setLocation(java.lang.Math.min(x, 0), java.lang.Math.min(y, 0));
        revalidate();
        repaint();
    }

    protected void setGraph(sandmark.util.newgraph.Graph g){
        setLayout(myLayoutType, g, myGraphStyle);
        revalidate();
        repaint();
    }

    private sandmark.util.newgraph.Graph myGraph;
    private sandmark.util.newgraph.Style myGraphStyle;
    private sandmark.util.graph.graphview.GraphLayout myLayout;
    private int myLayoutType;
    private javax.swing.JTextArea myNodeInfoArea;
    private java.util.ArrayList myDrawnEdges;
    private double myZoomFactor;

    // variables used for dragging a node with a mouse
    private sandmark.util.graph.graphview.NodeDisplayInfo myDragNode;
    private int myDX;
    private int myDY;
}
