// TreeLayout.java

package sandmark.util.graph.graphview;

/**
 * Layout algorithm for trees.
 *
 * @author Andrzej
 */
public class TreeLayout extends GraphLayout {

    /**
     * Constructs a new layout.
     *
     * @param g graph to be laid out
     * @param style style of the graph
     */
    public TreeLayout(sandmark.util.newgraph.Graph g,
		      sandmark.util.newgraph.Style style) {
	super(g, style);
    }

    /**
     * Assigns positions to the nodes, so they could be drawn on the screen.
     */
    public void layout() {
	java.lang.Object root = graph.roots().next();
	int width = 30;
	int height = 30;
	int type = sandmark.util.graph.graphview.NodeDisplayInfo.CIRCLE;
	layoutTree(root, width, height, width, height, type);
    }

    // assigns positions to the nodes in the tree
    private int layoutTree(java.lang.Object n, int min, int y,
			   int width, int height, int type) {
        int minimum = min;
        java.util.Iterator iter = graph.succs(n);
        while (iter.hasNext()) {
            minimum = layoutTree(iter.next(), minimum, y + (height * 3),
				 width, height, type);
        }
        int leftmost = minimum;
        int rightmost = minimum;
        iter = graph.succs(n);
        while (iter.hasNext()) {
            sandmark.util.graph.graphview.NodeDisplayInfo info =
		getNodeMap(iter.next());
            int x = info.getX();
            if (leftmost == minimum || x < leftmost)
                leftmost = x;
            if (rightmost == minimum || x > rightmost)
                rightmost = x;
        }
        int xPos = (leftmost + rightmost) / 2;
        nodeMap.put(n, new sandmark.util.graph.graphview.NodeDisplayInfo(n, xPos, y, width, height, type));
        int nextX = xPos + (width * 2);
        if (minimum > nextX)
            return minimum;
        else
            return nextX;
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
