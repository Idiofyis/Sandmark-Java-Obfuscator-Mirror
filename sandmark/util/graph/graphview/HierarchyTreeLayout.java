// HierarchyTreeLayout.java

package sandmark.util.graph.graphview;

/**
 * Layout algorithm for trees, like class inheritance hierarchy.
 *
 * @author Andrzej
 */
public class HierarchyTreeLayout extends GraphLayout {

    /**
     * Constructs a new layout.
     *
     * @param g graph to be laid out
     * @param style style of the graph
     */
    public HierarchyTreeLayout(sandmark.util.newgraph.Graph g,
			       sandmark.util.newgraph.Style style) {
	super(g, style);
    }

    /**
     * Assigns positions to the nodes, so they could be drawn on the screen.
     */
    public void layout() {
        java.lang.Object root = graph.roots().next();
        int width = 60;
        int height = 30;
        int type = sandmark.util.graph.graphview.NodeDisplayInfo.RECTANGLE;
        layoutTree(root, width, height, width, height, type);
    }
    
    // lays out the tree
    private int layoutTree(java.lang.Object node, int x, int y,
			  int width, int height, int type) {
        nodeMap.put(node, new sandmark.util.graph.graphview.NodeDisplayInfo(node, x, y, width, height, type));
	x += width + 20;
        java.util.Iterator iter = graph.succs(node);
        while (iter.hasNext()) {
            y = layoutTree(iter.next(), x, y + height + 10,
			   width, height, type);
        }
	return y;
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
