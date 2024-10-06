// EdgeDisplayInfo

package sandmark.util.graph.graphview;

/**
 * Information about displaying an edge.
 *
 * @author Andrzej
 */
public class EdgeDisplayInfo {
    /**
     * Constructs a new info about an edge.
     *
     * @param e edge of the graph
     */
    public EdgeDisplayInfo(sandmark.util.newgraph.Edge e) {
	myEdge = e;
	myBends = new java.util.ArrayList();
    }

    /**
     * Returns an ArrayList of bends in the edge.
     *
     * @return a list of bends in the edge
     */
    public java.util.ArrayList getEdgeBends() {
	return myBends;
    }

    public java.util.ArrayList getOriginalEdgeBends() {
	if (myOriginalBends == null) {
	    myOriginalBends = new java.util.ArrayList();
	    for (int i = 0; i < myBends.size(); i++) {
		java.awt.Point bend = (java.awt.Point) myBends.get(i);
		myOriginalBends.add(new java.awt.Point(bend.x, bend.y));
	    }
	}
	return myOriginalBends;
    }

    /**
     * Adds a point representing a bend in the visual representation of
     * the edge.
     *
     * @param p point that represents a bend in the edge
     */
    public void addBend(java.awt.Point p) {
	myBends.add(p);
    }

    private sandmark.util.newgraph.Edge myEdge;
    private java.util.ArrayList myBends;
    private java.util.ArrayList myOriginalBends;
}
