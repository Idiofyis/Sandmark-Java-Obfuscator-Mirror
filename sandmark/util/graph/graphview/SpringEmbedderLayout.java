// SpringEmbedderLayout.java

package sandmark.util.graph.graphview;

/**
 * Layout that places nodes based on the force directed algorithm. It uses
 * springs between nodes to determine forces between nodes in the graph.
 *
 * @author Andrzej
 */
public class SpringEmbedderLayout extends GraphLayout {

    /**
     * Constructs a new layout.
     *
     * @param g graph to be laid out
     * @param style style of the graph
     */
    public SpringEmbedderLayout(sandmark.util.newgraph.Graph g,
				sandmark.util.newgraph.Style style) {
	super(g, style);
    }

    // class that stores the x and y coordinate and x and y force for a
    // particular node
    private class NodeInfo {
	public double x, y, xDisp, yDisp;

	public NodeInfo(double _x, double _y) {
	    x = _x;
	    y = _y;
	    xDisp = 0.0;
	    yDisp = 0.0;
	}
    }

    /**
     * Assigns positions to the nodes, so they could be drawn on the screen.
     */
    public void layout() {
	graph = removeAllMultipleEdges(graph);

        int numIterations = 500;

	java.util.HashMap nodes = new java.util.HashMap();
	java.util.Iterator iter = graph.nodes();

	
	while (iter.hasNext()) {
	    nodes.put(iter.next(), new NodeInfo(Math.random() * 500 + 50,
						Math.random() * 400 + 50));
	}

	/*
        double xCoord = 100;
        double yCoord = 100;
        boolean b = false;
        while (iter.hasNext()) {
            nodes.put(iter.next(), new NodeInfo(xCoord, yCoord));
            b = !b;
            if (b)
                xCoord = 110;
            else {
                xCoord = 100;
                yCoord += 10;
            }
        }
	*/

	double area = 500 * 500;
	double k = 100.0;
	//double k = java.lang.Math.sqrt(area / graph.nodeCount());
	double t = 50;

      	for (int i = 0; i < numIterations; i++) {

	    // calculate repulsive forces
	    java.util.Iterator vertices = graph.nodes();
	    while (vertices.hasNext()) {
		java.lang.Object v = vertices.next();
		NodeInfo vInfo = (NodeInfo)nodes.get(v);
		vInfo.xDisp = 0.0;
		vInfo.yDisp = 0.0;

		java.util.Iterator otherVertices = graph.nodes();
                while (otherVertices.hasNext()) {
		    java.lang.Object u = otherVertices.next();
                    if (u != v) {
			NodeInfo uInfo = (NodeInfo)nodes.get(u);
			double dx = vInfo.x - uInfo.x;
			if (dx >= 0.0 && dx < 0.1)
			    dx = 0.1;
			else if (dx < 0.0 && dx > -0.1)
			    dx = -0.1;
			double dy = vInfo.y - uInfo.y;
			if (dy >= 0.0 && dy < 0.1)
			    dy = 0.1;
			else if (dy < 0.0 && dy > -0.1)
			    dy = -0.1;
			double dxAbs = java.lang.Math.abs(dx);
			double dyAbs = java.lang.Math.abs(dy);
			//vInfo.xDisp += (dx / dxAbs) * (k / dxAbs) * (dxAbs / distance(dx, dy));
			//vInfo.yDisp += (dy / dyAbs) * (k / dyAbs) * (dyAbs / distance(dx, dy));
			vInfo.xDisp += (dx / dxAbs) * (k / distance(dx, dy)) * (dxAbs / distance(dx, dy));
			vInfo.yDisp += (dy / dyAbs) * (k / distance(dx, dy)) * (dyAbs / distance(dx, dy));
			//System.out.println("repulsive " + v + "\nxDisp " + vInfo.xDisp + " yDisp " + vInfo.yDisp);
                    }
                }
	    }

	    /*
	    vertices = graph.nodes();
	    while (vertices.hasNext()) {
		NodeInfo info = (NodeInfo)nodes.get(vertices.next());
		//System.out.println("xDisp " + info.xDisp);
		//System.out.println("yDisp " + info.yDisp);
		info.x += info.xDisp;
		info.y += info.yDisp;
		info.xDisp = 0.0;
		info.yDisp = 0.0;
	    }
	    */
	    
	    // calculate attractive forces
	    java.util.Iterator edges = graph.edges();
	    while (edges.hasNext()) {
		sandmark.util.newgraph.Edge e =
		    (sandmark.util.newgraph.Edge) edges.next();
		java.lang.Object v = e.sourceNode();
		java.lang.Object u = e.sinkNode();
		NodeInfo vInfo = (NodeInfo)nodes.get(v);
		NodeInfo uInfo = (NodeInfo)nodes.get(u);
		double dx = vInfo.x - uInfo.x;
		if (dx >= 0.0 && dx < 0.1)
		    dx = 0.1;
		else if (dx < 0.0 && dx > -0.1)
		    dx = -0.1;
		double dy = vInfo.y - uInfo.y;
		if (dy >= 0.0 && dy < 0.1)
		    dy = 0.1;
		else if (dy < 0.0 && dy > -0.1)
		    dy = -0.1;
		double dxAbs = java.lang.Math.abs(dx);
		double dyAbs = java.lang.Math.abs(dy);
		vInfo.xDisp -= (dx / dxAbs) * dxAbs / k;
		vInfo.yDisp -= (dy / dyAbs) * dyAbs / k;
		uInfo.xDisp += (dx / dxAbs) * dxAbs / k;
		uInfo.yDisp += (dy / dyAbs) * dyAbs / k;
		//vInfo.xDisp -= (dx / dxAbs) * (dxAbs / k) * (dxAbs / distance(dx, dy));
		//vInfo.yDisp -= (dy / dyAbs) * (dyAbs / k) * (dyAbs / distance(dx, dy));
		//uInfo.xDisp += (dx / dxAbs) * (dxAbs / k) * (dxAbs / distance(dx, dy));
		//uInfo.yDisp += (dy / dyAbs) * (dyAbs / k) * (dyAbs / distance(dx, dy));
		//System.out.println("repulsive " + v + "\nxDisp " + vInfo.xDisp + " yDisp " + vInfo.yDisp);
		//System.out.println("repulsive " + u + "\nxDisp " + vInfo.xDisp + " yDisp " + vInfo.yDisp);
	    }

	    /*
	    vertices = graph.nodes();
	    while (vertices.hasNext()) {
		NodeInfo info = (NodeInfo)nodes.get(vertices.next());
		//System.out.println("xDisp " + info.xDisp);
		//System.out.println("yDisp " + info.yDisp);
		info.x += info.xDisp;
		info.y += info.yDisp;
	    }
	    */
	    
	    
	    // update positions
	    vertices = graph.nodes();
	    while (vertices.hasNext()) {
		NodeInfo info = (NodeInfo)nodes.get(vertices.next());
		//System.out.println("xDisp " + info.xDisp);
		//System.out.println("yDisp " + info.yDisp);
		
		
		//info.x += info.xDisp / java.lang.Math.abs(info.xDisp) *
		//    java.lang.Math.min(info.xDisp, t);
		//info.y += info.yDisp / java.lang.Math.abs(info.yDisp) *
		//    java.lang.Math.min(info.yDisp, t);
		
		/*
		if (info.xDisp >= 0)
		    info.x += java.lang.Math.min(java.lang.Math.abs(info.xDisp), t);
		else
		    info.x -= java.lang.Math.min(java.lang.Math.abs(info.xDisp), t);
		if (info.yDisp >= 0)
		    info.y += java.lang.Math.min(java.lang.Math.abs(info.yDisp), t);
		else
		    info.y -= java.lang.Math.min(java.lang.Math.abs(info.yDisp), t);
		*/
		info.x += info.xDisp;
		info.y += info.yDisp;

		info.x =
		    java.lang.Math.min(600, java.lang.Math.max(0, info.x));
		info.y =
		    java.lang.Math.min(500, java.lang.Math.max(0, info.y));
		//System.out.println("iter " + i + ": x " + info.x);
		//System.out.println("iter " + i + ": y " + info.y);
	    }
	    
	    //t -= 1;
	    //t = java.lang.Math.max(1, t);
	}

	iter = graph.nodes();
	while (iter.hasNext()) {
	    java.lang.Object node = iter.next();
	    int x = (int)(((NodeInfo)(nodes.get(node))).x);
	    int y = (int)(((NodeInfo)(nodes.get(node))).y);
	    int nodeShape;
	    switch(graphStyle.getNodeShape(node))
		{
		case sandmark.util.newgraph.EditableGraphStyle.BOX: 
		    nodeShape = sandmark.util.graph.graphview.NodeDisplayInfo.RECTANGLE; break;
		case sandmark.util.newgraph.EditableGraphStyle.CIRCLE:
		    nodeShape = sandmark.util.graph.graphview.NodeDisplayInfo.CIRCLE; break;
		default:
		    nodeShape = sandmark.util.graph.graphview.NodeDisplayInfo.CIRCLE; break;
		}
	    nodeMap.put(node, new sandmark.util.graph.graphview.NodeDisplayInfo(node, x, y, 35, 35, nodeShape));
	}
    }

    private double distance(double dx, double dy) {
	return Math.sqrt(dx * dx + dy * dy);
    }

    private sandmark.util.newgraph.Graph removeAllMultipleEdges(sandmark.util.newgraph.Graph g) {
        g = g.removeMultipleEdges();
        java.util.Iterator iter = g.edges();
        while (iter.hasNext()) {
            sandmark.util.newgraph.Edge e =
                (sandmark.util.newgraph.Edge)iter.next();
            if (g.hasEdge(e.sinkNode(), e.sourceNode()))
                g = g.removeEdge(e);
        }
	return g;
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
