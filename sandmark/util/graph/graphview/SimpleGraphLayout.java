// SimpleGraphLayout.java

package sandmark.util.graph.graphview;

/**
 * Layout that places nodes in random positions.
 *
 * @author Andrzej
 */
public class SimpleGraphLayout extends GraphLayout {
    
    /**
     * Constructs a new layout.
     *
     * @param g graph to be laid out
     * @param style style of the graph
     */
    public SimpleGraphLayout(sandmark.util.newgraph.Graph g,
			     sandmark.util.newgraph.Style style) {
	super(g, style);
    }

    /**
     * Assigns positions to the nodes, so they could be drawn on the screen.
     */
    public void layout() {
        int x = 100;
        int y = 50;
        boolean b = false;
        java.util.Iterator nodes = graph.nodes();
        while (nodes.hasNext()) {
	    java.lang.Object node = nodes.next();
            nodeMap.put(node, new NodeDisplayInfo(node, (int)(Math.random() * 400), (int)(Math.random() * 400), 30, 30, NodeDisplayInfo.CIRCLE));
        }
	/*
	while (nodes.hasNext()) {
	    java.lang.Object node = nodes.next();
            nodeMap.put(node, new sandmark.util.graph.graphview.NodeDisplayInfo(node, x, y, 30, 30, sandmark.util.graph.graphview.NodeDisplayInfo.CIRCLE));
            b = !b;
            if (b)
                x = 500;
            else {
                x = 100;
                y += 100;
            }
        }
	*/
    }

    /**
     * Returns whether this graph can be laid out using this algorithm.
     *
     * @return true if this algorithm can lay out the graph, false otherwise
     */
    public boolean canLayout() {
	return true;
    }
}
