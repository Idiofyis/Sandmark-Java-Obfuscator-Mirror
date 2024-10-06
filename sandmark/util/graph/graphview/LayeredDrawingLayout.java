// LayeredDrawingLayout.java

package sandmark.util.graph.graphview;

/**
 * Graph Layout algorithm that places the nodes in layers.
 *
 * @author Andrzej
 */
public class LayeredDrawingLayout extends GraphLayout {
   private static final boolean USE_SLOW_MINIMIZE_CROSSINGS_ALGORITHM = false;
    // class representing crossings information for each layer
    private class LayerInfo {
	public LayerInfo(int layerNum, int layerPos) {
	    layer = layerNum;
	    pos = layerPos;
	    crossings = new java.util.HashMap();
	}

	public int layer;
	public int pos;
	public int x;
	public java.util.HashMap crossings;
    }

    // Class that represents an edge that spans over more than one layer
    // with dummy nodes
    private class EdgeInfo {
	public EdgeInfo(sandmark.util.newgraph.Edge e,
			java.util.ArrayList dummys) {
	    edge = e;
	    dummyNodes = dummys;
	}

	public sandmark.util.newgraph.Edge edge;
	public java.util.ArrayList dummyNodes;
    }

    /**
     * Constructs a new layout.
     *
     * @param g graph to be laid out
     * @param style style of the graph
     */
    public LayeredDrawingLayout(sandmark.util.newgraph.Graph g,
		      sandmark.util.newgraph.Style style) {
	super(g, style);
    }

    /**
     * Assigns positions to the nodes, so they could be drawn on the screen.
     */
    public void layout() {
	int type = sandmark.util.graph.graphview.NodeDisplayInfo.RECTANGLE;
	myNodes = new java.util.HashMap();
	myLayers = new sandmark.util.SparseVector();
	myRemovedEdges = new java.util.ArrayList();
	myReversedEdges = new java.util.HashMap();

	// remove self edges
	removeSelfEdges();

	// remove cycles
	removeCycles();

	// assign layers
	myNumLayers = assignLayers();

	// insert dummy nodes
	insertDummyNodes();

	layoutNodesNewest(type);

	// minimize crossings
	minimizeBarycenter(1);
	minimizeBarycenter(0);
	if(USE_SLOW_MINIMIZE_CROSSINGS_ALGORITHM)
	   minimizeCrossings();

	minimizeBarycenter(1);
	minimizeBarycenter(0);
	if(USE_SLOW_MINIMIZE_CROSSINGS_ALGORITHM)
	   minimizeCrossings();

	//minimizeBarycenter(1);
	//minimizeBarycenter(0);
	//minimizeCrossings();
	elimIntersections();

	//reverseEdgesBack();
	assignBendsInEdges();
    }

    private void elimIntersections() {
	java.util.Iterator layers = myLayers.iterator();
	while (layers.hasNext()) {
	    sandmark.util.SparseVector layer = (sandmark.util.SparseVector)layers.next();
	    if (layer.size() > 1) {
		for (int i = 2; i <= layer.size(); i++) {
		    //java.lang.Object node1 = layer.get(i - 1);
		    //java.lang.Object node2 = layer.get(i);
		    NodeDisplayInfo node1 = getNodeMap(layer.get(i - 1));
		    NodeDisplayInfo node2 = getNodeMap(layer.get(i));
		    if (node1.intersects(node2)) {
			int dx = node1.getWidth() / 2 + node2.getWidth() / 2 + 20 - (node2.getX() - node1.getX());
			for (int j = i; j <= layer.size(); j++) {
			    NodeDisplayInfo node = getNodeMap(layer.get(j));
			    node.setX(node.getX() + dx);
			}
		    }
		}
	    }
	}
    }

    // removes self edges
    private void removeSelfEdges() {
	java.util.Iterator edges = graph.edges();
	while (edges.hasNext()) {
	    sandmark.util.newgraph.Edge edge =
		(sandmark.util.newgraph.Edge)edges.next();
	    if (edge.sourceNode().equals(edge.sinkNode()))
		graph = graph.removeEdge(edge);
	}
    }

    // removes cycles in the graph
    private void removeCycles() {
	java.util.ArrayList list = greedyCycleRemoval();
	java.util.Iterator edges = graph.edges();
	while (edges.hasNext()) {
	    sandmark.util.newgraph.Edge edge =
		(sandmark.util.newgraph.Edge)edges.next();
	    java.lang.Object source = edge.sourceNode();
	    java.lang.Object sink = edge.sinkNode();
	    if (list.indexOf(source) > list.indexOf(sink)) {
		sandmark.util.newgraph.Edge reversed =
		    new sandmark.util.newgraph.EdgeImpl(sink, source);
		//myReversedEdges.add(new ReversedEdge(edge, reversed));
		myReversedEdges.put(reversed, edge);
		graph = graph.removeEdge(edge);
		graph = graph.addEdge(reversed);
	    }
	}
    }

    // removes cycles
    private java.util.ArrayList greedyCycleRemoval() {
	sandmark.util.newgraph.Graph g = graph;
	java.util.ArrayList sl = new java.util.ArrayList();
	java.util.ArrayList sr = new java.util.ArrayList();

	while (g.nodeCount() > 0) {
	    for (;;) {
		java.util.Iterator iter = g.reverseRoots();
		if (iter.hasNext()) {
		    java.lang.Object node = iter.next();
		    g = g.removeNode(node);
		    sr.add(0, node);
		} else
		    break;
	    }
	    for (;;) {
		java.util.Iterator iter = g.roots();
		if (iter.hasNext()) {
		    java.lang.Object node = iter.next();
		    g = g.removeNode(node);
		    sl.add(node);
		} else
		    break;
	    }
	    if (g.nodeCount() > 0) {
		java.lang.Object node = g.nodes().next();
		int diff = g.outDegree(node) - g.inDegree(node);
		java.util.Iterator iter = g.nodes();
		while (iter.hasNext()) {
		    java.lang.Object n = iter.next();
		    int x = g.outDegree(n) - g.inDegree(n);
		    if (x > diff) {
			node = n;
			diff = x;
		    }
		}
		g = g.removeNode(node);
		sl.add(node);
	    }
	}
	sl.addAll(sr);
	return sl;
    }

    // reverses edges back that have been reversed to remove cycles
    /*    private void reverseEdgesBack() {
	java.util.Iterator iter = myReversedEdges.iterator();
	while (iter.hasNext()) {
	    ReversedEdge re = (ReversedEdge)iter.next();
	    graph = graph.removeEdge(re.reversed);
	    graph = graph.addEdge(re.edge);
	}
	}*/

    // assign bends in the graph in place of the dummy nodes
    private void assignBendsInEdges() {
	java.util.Iterator iter = myRemovedEdges.iterator();
	while (iter.hasNext()) {
	    EdgeInfo edgeInfo = (EdgeInfo)iter.next();
	    java.lang.Object edge = myReversedEdges.get(edgeInfo.edge);
	    if (edge == null)
		edge = edgeInfo.edge;
	    else
		java.util.Collections.reverse(edgeInfo.dummyNodes);
	    java.util.Iterator dummyNodes = edgeInfo.dummyNodes.iterator();
	    while (dummyNodes.hasNext()) {
		NodeDisplayInfo dummyInfo = getNodeMap(dummyNodes.next());
		java.awt.Point bendPoint = new java.awt.Point(dummyInfo.getX(), dummyInfo.getY());
		((EdgeDisplayInfo)edgeMap.get(edge)).addBend(bendPoint);
	    }
	}
    }

    // assign nodes to different layers
    private int assignLayers() {
	int maxLayer = 0;
	java.util.Iterator sinks = graph.reverseRoots();
	while (sinks.hasNext()) {
	    addToLayer(sinks.next(), 1);
	    maxLayer = 1;
	}
	java.util.Iterator nodes = graph.nodes();
	while (nodes.hasNext()) {
	    java.lang.Object node = nodes.next();
	    if (myNodes.get(node) == null) {
		int layer = findLongestPath(node) + 1;
		if (layer > maxLayer)
		    maxLayer = layer;
		addToLayer(node, layer);
	    }
	}
	return maxLayer;
    }

    // add a node to a layer
    private void addToLayer(java.lang.Object node, int layerNum) {
	sandmark.util.SparseVector layer =
	    (sandmark.util.SparseVector)myLayers.get(layerNum);
	if (layer == null) {
	    layer = new sandmark.util.SparseVector();
	    myLayers.set(layerNum, layer);
	}
	int pos = layer.size() + 1;
	layer.set(pos, node);
	myNodes.put(node, new LayerInfo(layerNum, pos));
    }

    // returns the length of the longest path from the given node
    private int findLongestPath(java.lang.Object node) {
	java.util.Iterator succs = graph.succs(node);
	int max = 0;
	while (succs.hasNext()) {
	    int length = findLongestPath(succs.next()) + 1;
	    if (length > max)
		max = length;
	}
	return max;
    }

    // inserts dummy nodes on the edges spanning over more than one layer
    private void insertDummyNodes() {
	java.util.Iterator edges = graph.edges();
	while (edges.hasNext()) {
	    sandmark.util.newgraph.Edge e =
		(sandmark.util.newgraph.Edge)edges.next();
	    java.lang.Object source = e.sourceNode();
	    java.lang.Object sink = e.sinkNode();
	    int sourceLayer = ((LayerInfo)myNodes.get(source)).layer;
	    int sinkLayer = ((LayerInfo)myNodes.get(sink)).layer;
	    int span = sourceLayer - sinkLayer;
	    if (span > 1) {
		graph = graph.removeEdge(e);
		java.util.ArrayList dummyNodes = new java.util.ArrayList();
		java.lang.Object prev = source;
		for (int i = sourceLayer - 1; i > sinkLayer; i--) {
		    DummyNode dummy = new DummyNode();
		    dummyNodes.add(dummy);
		    graph = graph.addEdge(prev, dummy);
		    prev = dummy;
		    addToLayer(dummy, i);
		}
		graph = graph.addEdge(prev, sink);
		myRemovedEdges.add(new EdgeInfo(e, dummyNodes));
	    }
	}
    }

    /*    private void assignInitialXPos() {
	java.util.Iterator nodes = graph.nodes();
	while (nodes.hasNext()) {
	    java.lang.Object node = nodes.next();
	    LayerInfo nodeInfo = (LayerInfo)myNodes.get(node);
	    
	}
	}*/

    // Minimizes crossings in between layers
    //TODO:  figure out the complexity of this algorithm and then
    //find a better algorithm.  This takes many seconds to run for
    //TTT
    private void minimizeCrossings() {
	//System.out.println("total crossings before = " + calculateTotalCrossings());
	int minCross = calculateTotalCrossings();
	int numCross = minCross;
	int counter = 0;
	for (;;) {
	    counter++;
	    for (int i = myNumLayers; i > 1; i--) {
		calculateCrossings(i);
		sandmark.util.SparseVector layer
		    = (sandmark.util.SparseVector)myLayers.get(i);
		int oldNumCrossings = 0;
		int numCrossings = computeNumCross(layer);
		while (numCrossings > oldNumCrossings) {
		    for (int j = 1; j < layer.size(); j++) {
			java.lang.Object u = layer.get(j);
			java.lang.Object v = layer.get(j + 1);
			Integer uvCross =
			    (Integer)((LayerInfo)myNodes.get(u)).crossings.get(v);
			Integer vuCross =
			    (Integer)((LayerInfo)myNodes.get(v)).crossings.get(u);
			if (uvCross.compareTo(vuCross) > 0) {
			    switchLayerPositions(layer, j, u, v);
			}
		    }
		    oldNumCrossings = numCrossings;
		    numCrossings = computeNumCross(layer);
		}
	    }
	    //System.out.println(counter + ": total crossings after top to bottom = " + calculateTotalCrossings());
	    numCross = calculateTotalCrossings();
	    if (counter < 20) {
		if (numCross < minCross)
		    minCross = numCross;
	    }
	    else {
		if (numCross == minCross || counter > 100)
		break;
	    }
	    for (int i = 2; i <= myNumLayers; i++) {
		calculateCrossings(i);
		sandmark.util.SparseVector layer
		    = (sandmark.util.SparseVector)myLayers.get(i);
		int oldNumCrossings = 0;
		int numCrossings = computeNumCross(layer);
		while (numCrossings > oldNumCrossings) {
		    for (int j = 1; j < layer.size(); j++) {
			java.lang.Object u = layer.get(j);
			java.lang.Object v = layer.get(j + 1);
			Integer uvCross =
			    (Integer)((LayerInfo)myNodes.get(u)).crossings.get(v);
			Integer vuCross =
			    (Integer)((LayerInfo)myNodes.get(v)).crossings.get(u);
			if (uvCross.compareTo(vuCross) >= 0) {
			    switchLayerPositions(layer, j, u, v);
			}
		    }
		    oldNumCrossings = numCrossings;
		    numCrossings = computeNumCross(layer);
		}
	    }
	    //System.out.println(counter + ": total crossings after bottom to top = " + calculateTotalCrossings());
	    numCross = calculateTotalCrossings();
	    if (counter < 20) {
		if (numCross < minCross)
		    minCross = numCross;
	    }
	    else {
		if (numCross == minCross || counter > 100)
		    break;
	    }
	}
    }

    // switch positions of two nodes in the given layer
    private void switchLayerPositions(sandmark.util.SparseVector layer, int j, java.lang.Object u, java.lang.Object v) {
	int uPos = ((LayerInfo)myNodes.get(u)).pos;
	int vPos = ((LayerInfo)myNodes.get(v)).pos;
	((LayerInfo)myNodes.get(u)).pos = vPos;
	((LayerInfo)myNodes.get(v)).pos = uPos;
	layer.set(j, v);
	layer.set(j + 1, u);
	int uX = ((NodeDisplayInfo)nodeMap.get(u)).getX();
	int vX = ((NodeDisplayInfo)nodeMap.get(v)).getX();
	((NodeDisplayInfo)nodeMap.get(u)).setX(vX);
	((NodeDisplayInfo)nodeMap.get(v)).setX(uX);
    }

    // check if node n intersects with other nodes in the layer
    private NodeDisplayInfo checkIntersectNodes(NodeDisplayInfo n, int layer,
						java.util.ArrayList nodesVisited) {
	java.util.Iterator nodes =
	    ((sandmark.util.SparseVector)myLayers.get(layer)).iterator();
	while (nodes.hasNext()) {
	    NodeDisplayInfo nodeInfo = (NodeDisplayInfo)nodeMap.get(nodes.next());
	    if (!n.equals(nodeInfo) && nodesVisited.contains(nodeInfo) && n.intersects(nodeInfo)) {
		return nodeInfo;
	    }
	}
	return null;
    }

    // eliminates intersection between two nodes
    private void eliminateIntersection(NodeDisplayInfo move, NodeDisplayInfo n) {
	//System.out.println(move + " " + n);
	int moveX = move.getX();
	int nX = n.getX();
	if (moveX < nX) {
	    int dif = nX - moveX;
	    //System.out.println("moveX " + moveX + " dif " + dif + " m width " + move.getWidth() + " n width " + n.getWidth());
	    //System.out.println("moving " + move + " left by " + (moveX - dif - move.getWidth() / 2 - n.getWidth() / 2 - 10));
	    move.setX(moveX + dif - move.getWidth() / 2 - n.getWidth() / 2 - 20);
	} else {
	    int dif = moveX - nX;
	    //System.out.println("moveX " + moveX + " nX " + nX + " dif " + dif + " m width " + move.getWidth() + " n width " + n.getWidth());
	    //System.out.println("moving " + move + " right by " + (moveX + dif + move.getWidth() / 2 + n.getWidth() / 2 + 10));
	    move.setX(moveX - dif + move.getWidth() / 2 + n.getWidth() / 2 + 20);
	}
    }

    // minimize Barycenter is a method for reducing crossings between the
    // layers
    private void minimizeBarycenter(int dir) {
	// downward sweep
	if (dir == 0) {
	    for (int i = myNumLayers - 1; i >= 1; i--) {
		java.util.Iterator nodes =
		    ((sandmark.util.SparseVector)myLayers.get(i)).iterator();
		java.util.ArrayList nodesVisited = new java.util.ArrayList();
		while (nodes.hasNext()) {
		    java.lang.Object node = nodes.next();
		    NodeDisplayInfo nodeInfo = (NodeDisplayInfo)nodeMap.get(node);
		    nodesVisited.add(nodeInfo);
		    int degree = 0;
		    int sum = 0;
		    java.util.Iterator preds = graph.preds(node);
		    		    
		    while (preds.hasNext()) {
			java.lang.Object pred = preds.next();
			int predX = ((NodeDisplayInfo)nodeMap.get(pred)).getX();
			sum += predX;
			degree++;
		    }

		    if (degree != 0) {
			int avg = sum / degree;
			((NodeDisplayInfo)nodeMap.get(node)).setX(avg);
		    }
		    for (;;) {
			NodeDisplayInfo intersection = checkIntersectNodes(nodeInfo, i, nodesVisited);
			if (intersection != null) {
			    eliminateIntersection(nodeInfo, intersection);
			    NodeDisplayInfo lastIntersection = nodeInfo;
			    for (;;) {
				intersection = checkIntersectNodes(lastIntersection, i, nodesVisited);
				if (intersection == null)
				    break;
				else {
				    eliminateIntersection(intersection, lastIntersection);
				    lastIntersection = intersection;
				}
			    }
			} else
			    break;
		    }
		    //checkIntersection(nodeInfo, i, nodesVisited);
		}
	    }
	} else {
	    // upward sweep
	    for (int i = 2; i <= myNumLayers; i++) {
		java.util.Iterator nodes =
		    ((sandmark.util.SparseVector)myLayers.get(i)).iterator();
		java.util.ArrayList nodesVisited = new java.util.ArrayList();
		while (nodes.hasNext()) {
		    java.lang.Object node = nodes.next();
		    NodeDisplayInfo nodeInfo = (NodeDisplayInfo)nodeMap.get(node);
		    nodesVisited.add(nodeInfo);
		    int degree = 0;
		    int sum = 0;
		    java.util.Iterator succs = graph.succs(node);
		    while (succs.hasNext()) {
			java.lang.Object succ = succs.next();
			int succX = ((NodeDisplayInfo)nodeMap.get(succ)).getX();
			sum += succX;
			degree++;
		    }
		    if (degree != 0) {
			int avg = sum / degree;
			((NodeDisplayInfo)nodeMap.get(node)).setX(avg);
		    }
		    for (;;) {
			NodeDisplayInfo intersection = checkIntersectNodes(nodeInfo, i, nodesVisited);
			if (intersection != null) {
			    eliminateIntersection(nodeInfo, intersection);
			    NodeDisplayInfo lastIntersection = nodeInfo;
			    for (;;) {
				intersection = checkIntersectNodes(lastIntersection, i, nodesVisited);
				if (intersection == null)
				    break;
				else {
				    eliminateIntersection(intersection, lastIntersection);
				    lastIntersection = intersection;
				}
			    }
			} else {
			    break;
			}
		    }
		    //checkIntersection(nodeInfo, i, nodesVisited);
		}
	    }
	}
	// fix layer positions
	assignLayerPositions();
    }

    // sorts the nodes in layers based on their x coordinates
    private void assignLayerPositions() {
	java.util.Iterator iter = myLayers.iterator();
	int counter = 0;
	while (iter.hasNext()) {
	    counter++;
	    sandmark.util.SparseVector layer = (sandmark.util.SparseVector)iter.next();
	    java.util.ArrayList nodeInfoLayer = new java.util.ArrayList();
	    java.util.Iterator nodes = layer.iterator();
	    while (nodes.hasNext())
		nodeInfoLayer.add(nodeMap.get(nodes.next()));

	    java.util.Collections.sort(nodeInfoLayer);
	    for (int i = 0; i < nodeInfoLayer.size(); i++) {
		java.lang.Object node = ((NodeDisplayInfo)nodeInfoLayer.get(i)).getNode();
		layer.set(i + 1, node);
		//System.out.println("layer " + counter + " pos " + i + " node " + node);
		((LayerInfo)myNodes.get(node)).pos = i + 1;
	    }
	}
    }

    // calculates crossings in each layer
    private int calculateTotalCrossings() {
	int result = 0;
	for (int i = 2; i <= myNumLayers; i++) {
	    calculateCrossings(i);
	    result +=
		computeNumCross((sandmark.util.SparseVector)myLayers.get(i));
	    //System.out.println("layer " + i + " : " + computeNumCross((sandmark.util.SparseVector)myLayers.get(i)));
	}
	return result;
    }

    // computes number of crossings in the given layer
    private int computeNumCross(sandmark.util.SparseVector layer) {
	int numCrossings = 0;
	for (int i = 1; i < layer.size(); i++) {
	    java.lang.Object u = layer.get(i);
	    for (int j = i + 1; j <= layer.size(); j++) {
		java.lang.Object v = layer.get(j);
		int uvCross =
		    ((Integer)((LayerInfo)myNodes.get(u)).crossings.get(v)).intValue();
		//System.out.println(u + " " + v + " : " + uvCross + " layer size: " + layer.size());
		numCrossings += uvCross;
	    }
	}
	return numCrossings;
    }

    // calculate crossings between each node in the given layer
    private void calculateCrossings(int l) {
	sandmark.util.SparseVector layer =
	    (sandmark.util.SparseVector)myLayers.get(l);
	java.util.Iterator iter = layer.iterator();
	while (iter.hasNext()) {
	    java.lang.Object u = iter.next();
	    java.util.Iterator iter1 = layer.iterator();
	    LayerInfo uInfo = (LayerInfo)myNodes.get(u);
	    while (iter1.hasNext()) {
		java.lang.Object v = iter1.next();
		int numCross = calculateCrossings(u, v);
		uInfo.crossings.put(v, new java.lang.Integer(numCross));
	    }
	}
    }

    // calculate number of crossings between u and v nodes
    private int calculateCrossings(java.lang.Object u, java.lang.Object v) {
	int numCrossings = 0;
	java.util.Iterator uOutEdges = graph.outEdges(u);
	while (uOutEdges.hasNext()) {
	    sandmark.util.newgraph.Edge uEdge =
		(sandmark.util.newgraph.Edge) uOutEdges.next();
	    java.lang.Object w = uEdge.sinkNode();
	    java.util.Iterator vOutEdges = graph.outEdges(v);
	    while(vOutEdges.hasNext()) {
		sandmark.util.newgraph.Edge vEdge =
		    (sandmark.util.newgraph.Edge) vOutEdges.next();
		java.lang.Object z = vEdge.sinkNode();
		if (((LayerInfo)myNodes.get(z)).pos 
		        < ((LayerInfo)myNodes.get(w)).pos)
		    numCrossings++;
	    }
	}
	return numCrossings;
    }

    private void layoutNodesNewest(int type) {
	int y = 0;
	for (int layer = myLayers.size(); layer >= 1; layer--) {
	    y += 50;
	    sandmark.util.SparseVector layerVector =
		(sandmark.util.SparseVector)myLayers.get(layer);
	    java.util.Iterator nodes = layerVector.iterator();
	    int x = 0;
	    int maxHeight = NODE_DEFAULT_HEIGHT;
	    while (nodes.hasNext()) {
		java.lang.Object node = nodes.next();
		java.awt.Dimension dimension = calculateNodeDimension(node);
		if (dimension.height > maxHeight)
		    maxHeight = dimension.height;
		x += dimension.width / 2 + 50;
		nodeMap.put(node, new NodeDisplayInfo(node, x, y, dimension.width,
						      dimension.height, type));
		x += dimension.width / 2;
	    }
	    y += maxHeight / 2;
	    nodes = layerVector.iterator();
	    while (nodes.hasNext()) {
		java.lang.Object node = nodes.next();
		NodeDisplayInfo nodeInfo = (NodeDisplayInfo)nodeMap.get(node);
		nodeInfo.setY(y);
	    }
	    y += maxHeight / 2;
	}
    }

    private java.awt.Dimension calculateNodeDimension(java.lang.Object node) {
	int width = NODE_DEFAULT_WIDTH;
	int height = NODE_DEFAULT_HEIGHT;
	if (!(node instanceof DummyNode) && graphStyle.isNodeLabeled(node)) {
	    java.lang.String label = graphStyle.getNodeLabel(node);
	    int index = 0;
	    int maxLine = 0;
	    int numLines = 0;
	    for (;;) {
		numLines++;
		int newLine = label.indexOf("\n", index);
		if (newLine == -1) {
		    int lineLength = label.length() - index;
		    if (lineLength > maxLine)
			maxLine = lineLength;
		    break;
		}
		int lineLength = newLine - index;
		if (lineLength > maxLine)
		    maxLine = lineLength;
		index = newLine + 1;
	    }
	    int fontSize = graphStyle.getNodeFontsize(node);
	    int maxLineWidth = maxLine * fontSize * 6 / 10 + 15;
	    if (maxLineWidth > width)
		width = maxLineWidth;
	    int nodeHeight = numLines * (fontSize * 13 / 10) + 15;
	    if (nodeHeight > height)
		height = nodeHeight;
	}
	return new java.awt.Dimension(width, height);
    }

    /**
     * Returns whether this graph can be laid out using this algorithm.
     *
     * @return true if this algorithm can lay out the graph, false otherwise
     */
    public boolean canLayout() {
	return true;
    }

    private java.util.HashMap myNodes;
    private sandmark.util.SparseVector myLayers;
    private int myNumLayers;
    private java.util.ArrayList myRemovedEdges;
    private java.util.HashMap myReversedEdges;
    private static final int NODE_DEFAULT_WIDTH = 50;
    private static final int NODE_DEFAULT_HEIGHT = 30;
}
