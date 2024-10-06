package sandmark.util.graph.graphview;

/**
 * This class represents the node in the display of the graph.
 *
 * @author Andrzej
 */
public class NodeDisplayInfo implements java.lang.Comparable {
    /**
     * Constructs display information about a particular node.
     *
     * @param node node of the graph
     * @param x x coordinate of the node
     * @param y y coordinate of the node
     * @param width width of the node
     * @param height height of the node
     * @param type shape of the node
     */
    public NodeDisplayInfo(java.lang.Object node, int x, int y,
			   int width, int height, int type) {
	myNode = node;
	myX = x;
	myY = y;
	myShapeType = type;
	createShape(x, y, width, height, type);
    }

    /**
     * Constructs display information about a particular node.
     *
     * @param node node of the graph
     * @param x x coordinate of the node
     * @param y y coordinate of the node
     * @param diameter diameter of the node
     * @param type shape of the node
     */
    public NodeDisplayInfo(java.lang.Object node, int x, int y,
			   int diameter, int type) {
	this(node, x, y, diameter, diameter, type);
    }

    /**
     * Compares the x coordinates of this node and another node.
     *
     * @param o node to be compared
     * @return the value 0 if the coordinates are equal, less than 0 if the
     *         coordinate of this node is less than other, and greater than 0
     *         if the coordinate of this node is greater than other
     */
    public int compareTo(java.lang.Object o) {
	return new Integer(myX).compareTo(new Integer(((NodeDisplayInfo)o).myX));
    }

    /**
     * Returns the x coordinate of this node.
     *
     * @return x coordinate of this node
     */
    public int getX() {
	return myX;
    }

    /**
     * Returns the y coordinate of this node.
     *
     * @return y coordinate of this node
     */
    public int getY() {
	return myY;
    }

    /**
     * Returns the x coordinate of this node.
     *
     * @return x coordinate of this node
     */

    public int getOriginalX() {
	if (myOriginalX == -1)
	    myOriginalX = myX;
	return myOriginalX;
    }

    /**
     * Returns the y coordinate of this node.
     *
     * @return y coordinate of this node
     */
    public int getOriginalY() {
	if (myOriginalY == -1)
	    myOriginalY = myY;
	return myOriginalY;
    }

    /**
     * Sets the x coordinate of this node.
     *
     * @param x new x coordinate of the node
     */
    public void setX(int x) {
	myX = x;
	myShape.setFrame(myX - myWidth / 2,
			 myY - myHeight / 2,
			 myWidth, myHeight);
    }

    /**
     * Sets the y coordinate of this node.
     *
     * @param y new y coordinate of the node
     */
    public void setY(int y) {
	myY = y;
	myShape.setFrame(myX - myWidth / 2,
			 myY - myHeight / 2,
			 myWidth, myHeight);
    }

    /**
     * Returns the width of the node.
     *
     * @return width of the node
     */
    public int getWidth() {
	return myWidth;
    }

    /**
     * Returns the height of the node.
     *
     * @return height of the node
     */
    public int getHeight() {
	return myHeight;
    }

    /**
     * Returns the width of the node.
     *
     * @return width of the node
     */
    public int getOriginalWidth() {
	if (myOriginalWidth == -1)
	    myOriginalWidth = myWidth;
	return myOriginalWidth;
    }

    /**
     * Returns the height of the node.
     *
     * @return height of the node
     */
    public int getOriginalHeight() {
	if (myOriginalHeight == -1)
	    myOriginalHeight = myHeight;
	return myOriginalHeight;
    }

    /**
     * Updates the position of this node.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void updatePosition(int x, int y) {
	myX = x;
	myY = y;
	myShape.setFrame(x - myWidth / 2,
			 y - myHeight / 2,
			 myWidth, myHeight);
    }

    public void updatePositionAndSize(int x, int y, int width, int height) {
	myWidth = width;
	myHeight = height;
	updatePosition(x, y);
    }

    public void updateOriginalPositions(double factor) {
	myOriginalX = (int)(myX / factor);
	myOriginalY = (int)(myY / factor);
    }

    // creates shape representing this node
    private void createShape(int x, int y, int w, int h, int type) {
	myWidth = w;
	myHeight = h;
	if (type == CIRCLE)
	    myShape = new java.awt.geom.Ellipse2D.Double(x - myWidth / 2,
							 y - myHeight / 2,
							 myWidth, myHeight);
	else if (type == RECTANGLE)
	    myShape = new java.awt.geom.Rectangle2D.Double(x - myWidth / 2,
							   y - myHeight / 2,
							   myWidth, myHeight);
    }

    /**
     * Returns the shape of this node.
     *
     * @return shape of this node
     */
    public java.awt.geom.RectangularShape getShape() {
	return myShape;
    }

    /**
     * Returns the type of the shape of this node.
     *
     * @return type of the shape of this node
     */
    public int getShapeType() {
	return myShapeType;
    }

    /**
     * Returns true if this node contains the point [x, y]
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @return true if the node contains point [x, y], false otherwise
     */
    public boolean contains(int x, int y) {
	return myShape.contains(x, y);
    }

    /**
     * Returns true if this node contains the point p
     *
     * @param p point to check if it is inside the node
     * @return true if the node contains point p, false otherwise
     */
    public boolean contains(java.awt.Point p) {
	return myShape.contains(p);
    }

    /**
     * Returns true if this node intersects node n
     *
     * @param n node to check for intersections with
     * @return true if this node intersects node n, false otherwise
     */
    public boolean intersects(NodeDisplayInfo n) {
	return myShape.intersects(n.getShape().getFrame());
    }

    /**
     * Returns the node object associated with this display information.
     *
     * @return node of the graph
     */
    public java.lang.Object getNode() {
	return myNode;
    }

    /** Round shape of the node. */
    public static final int CIRCLE = 0;
    /** Rectangular shape of the node. */
    public static final int RECTANGLE = 1;

    private java.lang.Object myNode;
    private java.awt.geom.RectangularShape myShape;
    private int myShapeType;
    private int myX;
    private int myY;
    private int myWidth;
    private int myHeight;
    private int myOriginalX = -1;
    private int myOriginalY = -1;
    private int myOriginalWidth = -1;
    private int myOriginalHeight = -1;
}
