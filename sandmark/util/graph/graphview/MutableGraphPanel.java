package sandmark.util.graph.graphview;

public class MutableGraphPanel
    extends sandmark.util.graph.graphview.GraphPanel
    implements sandmark.util.newgraph.GraphListener{

    private sandmark.util.newgraph.MutableGraph myGraph;

    /**
     * Constructs a new Panel for drawing the graph.
     *
     * @param g graph to be drawn
     * @param style style containing information about the nodes
     * @param type type of the layout, see {@link GraphLayout} for different
     *             types
     * @param nodeInfo text area where node information can be displayed
     */
    public MutableGraphPanel(sandmark.util.newgraph.MutableGraph g,
                             sandmark.util.newgraph.Style style,
                             int type, javax.swing.JTextArea nodeInfo) {
        super(g.graph(), style, type, nodeInfo);
        myGraph = g;
        myGraph.addGraphListener(this);
    }

    public void graphChanged(sandmark.util.newgraph.MutableGraph g){
        super.setGraph(g.graph());
    }

}
