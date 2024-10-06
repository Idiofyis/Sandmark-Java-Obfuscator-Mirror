package sandmark.util.graph.graphview;

/**
 * This class represents the list of graphs to be drawn.  The user creates
 * the list of graphs:
 * <pre>
     GraphList list = new GraphList();
 * </pre>
 * and then can add graphs along with style and the name of the graph
 * to the list.
 * <pre>
     list.add(graph, style, name);
 * </pre>
 * 
 * @author Andrzej
 */
public class GraphList extends java.util.Observable {
    // private class that stores the graph, style and name of the graph
    private class GraphInfo {
	public GraphInfo(sandmark.util.newgraph.Graph g,
			 sandmark.util.newgraph.Style s,
			 java.lang.String n) {
	    graph = g;
	    style = s;
	    name = n;
	}
	
	public sandmark.util.newgraph.Graph graph;
	public sandmark.util.newgraph.Style style;
	public java.lang.String name;
    }
    
    // Constructs an empty list of graphs.
    private GraphList() {
	myGraphList = new java.util.ArrayList();
    }

    // the unique instance of this class
    private static GraphList gl_instance = null;

    /**
     * Returns the unique instance of this class.
     * @return the unique instance of this class
     */
    public static GraphList instance() {
	if (gl_instance == null)
	    gl_instance = new GraphList();
	return gl_instance;
    }

    /**
     * Deletes all graphs from the list.
     */
    public void clear() {
	myGraphList.clear();
	setChanged();
	notifyObservers("clear");
    }

    /**
     * Adds a graph to the list of graphs.
     *
     * @param graph graph to be added to the list
     * @param style style of the graph
     * @param name name of the graph
     */
    public void add(sandmark.util.newgraph.Graph graph,
		    sandmark.util.newgraph.GraphStyle style,
		    java.lang.String name) {
	add(graph, style.localize(graph), name);
    }

    /**
     * Adds a mutable graph to the list of graphs.
     *
     * @param graph graph to be added to the list
     * @param style style of the graph
     * @param name name of the graph
     */
    public void add(sandmark.util.newgraph.MutableGraph graph,
		    sandmark.util.newgraph.GraphStyle style,
		    java.lang.String name) {
	add(graph.graph(), style.localize(graph), name);
    }

    /**
     * Adds a graph with a default style to the list of graphs.
     *
     * @param graph graph to be added to the list
     * @param name name of the graph
     */
    public void add(sandmark.util.newgraph.Graph graph,
		    java.lang.String name) {
	sandmark.util.newgraph.GraphStyle style =
	    new sandmark.util.newgraph.EditableGraphStyle();
	add(graph, style.localize(graph), name);
    }

    /**
     * Adds a mutable graph with a default style to the list of graphs.
     *
     * @param graph graph to be added to the list
     * @param name name of the graph
     */
    public void add(sandmark.util.newgraph.MutableGraph graph,
		    java.lang.String name) {
	sandmark.util.newgraph.GraphStyle style =
	    new sandmark.util.newgraph.EditableGraphStyle();
	add(graph.graph(), style.localize(graph), name);
    }

    // adds a graph to the list of graphs
    private void add(sandmark.util.newgraph.Graph g,
		    sandmark.util.newgraph.Style s, java.lang.String name) {
	boolean found = false;
	for (int i = 0; i < myGraphList.size(); i++) {
	    GraphInfo gi = (GraphInfo)myGraphList.get(i);
	    if (gi.name.equals(name)) {
		gi.graph = g;
		gi.style = s;
		found = true;
	    }
	}
	if (!found) {
	    myGraphList.add(new GraphInfo(g, s, name));
	}
	setChanged();
	notifyObservers("add");
    }

    /**
     * Returns the graph associated with the index.
     *
     * @param index index of the graph in the list
     */
    public sandmark.util.newgraph.Graph getGraph(int index) {
	return ((GraphInfo)myGraphList.get(index)).graph;
    }

    /**
     * Returns the style of the graph associated with the index.
     *
     * @param index index of the graph in the list
     */
    public sandmark.util.newgraph.Style getStyle(int index) {
	return ((GraphInfo)myGraphList.get(index)).style;
    }

    /**
     * Returns an array of the graph names.
     */
    public java.lang.String[] getGraphNames() {
	java.lang.String[] result = new java.lang.String[myGraphList.size()];
	for (int i = 0; i < myGraphList.size(); i++)
	    result[i] = ((GraphInfo)myGraphList.get(i)).name;
	return result;
    }

    private java.util.ArrayList myGraphList;
}
