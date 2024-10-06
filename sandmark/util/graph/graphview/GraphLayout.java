// GraphLayout.java

package sandmark.util.graph.graphview;

/**
 * Abstract class representing a general layout for a graph.
 *
 * @author Andrzej
 */
public abstract class GraphLayout {

    protected sandmark.util.newgraph.Graph graph;
    protected sandmark.util.newgraph.Style graphStyle;
    protected java.util.HashMap nodeMap;
    protected java.util.HashMap edgeMap;
    
    // types of layout
    /** Layout for tree graphs. */
    public static final int TREE_LAYOUT = 0;
    /** Similar to tree layout, but for class inheritance hierarchy. */
    public static final int HIERARCHY_TREE_LAYOUT = 1;
    /** Random layout. */
    public static final int SIMPLE_GRAPH_LAYOUT = 2;
    /** Layout that uses a force directed algorithm. */
    public static final int SPRING_EMBEDDER_LAYOUT = 3;
    /** Layout that draws the graph in layers. */
    public static final int LAYERED_DRAWING_LAYOUT = 4;

    /**
     * Creates a new GraphLayout. Lays out the graph if it can be laid out
     * using this algorithm.
     *
     * @param g graph to layout
     * @param style style of the graph
     */
    public GraphLayout(sandmark.util.newgraph.Graph g,
		       sandmark.util.newgraph.Style style) {
        graph = g;
	graphStyle = style;
	nodeMap = new java.util.HashMap();
	edgeMap = new java.util.HashMap();
	assignEdgeInformation();
	layout();
	placeGraphAt(30, 30);
	//resizeGraph(0.5);
    }

    /**
     * Assigns positions to the nodes, so they could be drawn on the screen.
     */
    public abstract void layout();


    /**
     * Returns whether this graph can be laid out using this algorithm.
     *
     * @return true if this algorithm can lay out the graph, false otherwise
     */
    public abstract boolean canLayout();

    /**
     * Returns the display information for the particular node.
     *
     * @param node node of the graph
     * @return information how to display the node
     */
    public sandmark.util.graph.graphview.NodeDisplayInfo getNodeMap(java.lang.Object node) {
	return (sandmark.util.graph.graphview.NodeDisplayInfo)nodeMap.get(node);
    }

    /**
     * Returns the display information for the particular edge.
     *
     * @param edge edge of the graph
     * @return information how to display the edge
     */
    public sandmark.util.graph.graphview.EdgeDisplayInfo getEdgeMap(sandmark.util.newgraph.Edge edge) {
	return (sandmark.util.graph.graphview.EdgeDisplayInfo)edgeMap.get(edge);
    }

    /**
     * Creates display information for each edge.
     */
    public void assignEdgeInformation() {
	java.util.Iterator iter = graph.edges();
	while (iter.hasNext()) {
	    sandmark.util.newgraph.Edge e =
		(sandmark.util.newgraph.Edge)iter.next();
	    edgeMap.put(e, new EdgeDisplayInfo(e));
	}
    }

    /**
     * Returns the maximum x coordinate of a node in the graph.
     *
     * @return max x coordinate of a node in the graph
     */
    public int getMaximumX() {
	java.util.Iterator iter = nodeIterator();
	int max = 0;
	while (iter.hasNext()) {
	    sandmark.util.graph.graphview.NodeDisplayInfo n =
		(sandmark.util.graph.graphview.NodeDisplayInfo)iter.next();
	    int x = n.getX() + n.getWidth() / 2;
	    if (x > max)
		max = x;
	}
	return max;
    }

    /**
     * Returns the maximum y coordinate of a node in the graph.
     *
     * @return max y coordinate of a node in the graph
     */
    public int getMaximumY() {
	java.util.Iterator iter = nodeIterator();
	int max = 0;
	while (iter.hasNext()) {
	    sandmark.util.graph.graphview.NodeDisplayInfo n =
		(sandmark.util.graph.graphview.NodeDisplayInfo)iter.next();
	    int y = n.getY() + n.getHeight() / 2;
	    if (y > max)
		max = y;
	}
	return max;
    }

    /**
     * Returns the minimum x coordinate of a node in the graph.
     *
     * @return min x coordinate of a node in the graph
     */
    public int getMinimumX() {
	java.util.Iterator iter = nodeIterator();
	int min = 0;
	while (iter.hasNext()) {
	    sandmark.util.graph.graphview.NodeDisplayInfo n =
		(sandmark.util.graph.graphview.NodeDisplayInfo)iter.next();
	    int x = n.getX() - n.getWidth() / 2;
	    if (x < min || min == 0)
		min = x;
	}
	return min;
    }

    /**
     * Returns the minimum y coordinate of a node in the graph.
     *
     * @return min y coordinate of a node in the graph
     */
    public int getMinimumY() {
	java.util.Iterator iter = nodeIterator();
	int min = 0;
	while (iter.hasNext()) {
	    sandmark.util.graph.graphview.NodeDisplayInfo n =
		(sandmark.util.graph.graphview.NodeDisplayInfo)iter.next();
	    int y = n.getY() - n.getHeight() / 2;
	    if (y < min || min == 0)
		min = y;
	}
	return min;
    }

    /**
     * Moves the whole graph to the given minimum x and y coordinates.
     *
     * @param x minimum x coordinate
     * @param y minimum y coordinate
     */
    public void placeGraphAt(int x, int y) {
	int minX = getMinimumX();
	int minY = getMinimumY();
	int dX = minX - x;
	int dY = minY - y;
	java.util.Iterator iter = nodeIterator();
	while (iter.hasNext()) {
	    sandmark.util.graph.graphview.NodeDisplayInfo n =
		(sandmark.util.graph.graphview.NodeDisplayInfo)iter.next();
	    n.setX(n.getX() - dX);
	    n.setY(n.getY() - dY);
	}
	java.util.Iterator edges = edgeIterator();
	while (edges.hasNext()) {
	    sandmark.util.graph.graphview.EdgeDisplayInfo edge =
		(sandmark.util.graph.graphview.EdgeDisplayInfo)edges.next();
	    java.util.Iterator bends = edge.getEdgeBends().iterator();
	    while (bends.hasNext()) {
		java.awt.Point bend = (java.awt.Point)bends.next();
		bend.x = bend.x - dX;
		bend.y = bend.y - dY;
	    }
	}
    }

    public void resizeGraph(double factor) {
	java.util.Iterator nodes = nodeIterator();
	while (nodes.hasNext()) {
	    sandmark.util.graph.graphview.NodeDisplayInfo n =
		(sandmark.util.graph.graphview.NodeDisplayInfo)nodes.next();
	    n.updatePositionAndSize((int)(n.getOriginalX() * factor),
				    (int)(n.getOriginalY() * factor),
				    (int)(n.getOriginalWidth() * factor),
				    (int)(n.getOriginalHeight() * factor));
	}
	java.util.Iterator edges = edgeIterator();
	while (edges.hasNext()) {
	    sandmark.util.graph.graphview.EdgeDisplayInfo edge =
		(sandmark.util.graph.graphview.EdgeDisplayInfo)edges.next();
	    java.util.Iterator bends = edge.getEdgeBends().iterator();
	    java.util.Iterator originalBends = edge.getOriginalEdgeBends().iterator();
	    while (originalBends.hasNext()) {
		java.awt.Point bend = (java.awt.Point)bends.next();
		java.awt.Point originalBend = (java.awt.Point)originalBends.next();
		bend.x = (int)(originalBend.x * factor);
		bend.y = (int)(originalBend.y * factor);
	    }
	}	
    }

    /**
     * Returns an iterator over the display information of each node in the
     * graph.
     *
     * @return iterator over the display info of each node in the graph
     */
    public java.util.Iterator nodeIterator() {
	return nodeMap.values().iterator();
    }

    /**
     * Returns an iterator over the display information of each edge in the
     * graph.
     *
     * @return iterator over the display info of each edge in the graph
     */
    public java.util.Iterator edgeIterator() {
	return edgeMap.values().iterator();
    }
}
