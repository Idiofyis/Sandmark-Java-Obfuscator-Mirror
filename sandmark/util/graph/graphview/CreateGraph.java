package sandmark.util.graph.graphview;

public class CreateGraph {
    public static void main(String[] args) throws Exception {
	sandmark.util.newgraph.GraphStyle style =
	    new sandmark.util.newgraph.EditableGraphStyle();
	//GraphList list = new GraphList();
	GraphList.instance().add(createTree(), style, "ATree");
	GraphList.instance().add(createSelfMultiGraph(), style, "SelfMultiGraph");
	GraphList.instance().add(createCycleGraph(), style, "CycleGraph");
	GraphList.instance().add(createMultiGraph(), style, "MultiGraph");
	GraphList.instance().add(createCrossGraph(), style, "CrossGraph");
	GraphList.instance().add(createTestGraph(), style, "TestGraph");
	GraphList.instance().add(createGraph(), style, "Graph");
	GraphList.instance().add(createSimpleGraph(), style, "SimpleGraph");
	//sandmark.util.newgraph.Graph g = createTree();
	//sandmark.util.newgraph.Graph g = createSelfMultiGraph();
	new GraphViewFrame(GraphList.instance()).show();
	//sandmark.util.newgraph.Graphs.dotInFile(g, gstyle, "testgraph.dot");
    }

    public static sandmark.util.newgraph.Graph createSelfMultiGraph() {
	sandmark.util.newgraph.Graph g =
	    sandmark.util.newgraph.Graphs.createGraph(null, null);
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2),
	    n3 = new java.lang.Integer(3);
	g = g.addEdge(n1, n2).addEdge(n2, n3);
	g = g.addEdge(n1, n3).addEdge(n1, n3);
	g = g.addEdge(n1, n3).addEdge(n1, n3);
	g = g.addEdge(n1, n3).addEdge(n1, n3);
	return g;
    }

    public static sandmark.util.newgraph.Graph createCycleGraph() {
	sandmark.util.newgraph.Graph g =
	    sandmark.util.newgraph.Graphs.createGraph(null, null);
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2),
	    n3 = new java.lang.Integer(3),
	    n4 = new java.lang.Integer(4),
	    n5 = new java.lang.Integer(5),
	    n6 = new java.lang.Integer(6),
	    n7 = new java.lang.Integer(7),
	    n8 = new java.lang.Integer(8),
	    n9 = new java.lang.Integer(9);
	g = g.addEdge(n1, n2).addEdge(n2, n3);
	g = g.addEdge(n4, n5).addEdge(n6, n5);
	g = g.addEdge(n8, n7).addEdge(n9, n8);
	g = g.addEdge(n4, n1).addEdge(n7, n4);
	g = g.addEdge(n2, n5).addEdge(n5, n8);
	g = g.addEdge(n3, n6).addEdge(n6, n9);
	return g;
    }

    public static sandmark.util.newgraph.Graph createMultiGraph() {
	sandmark.util.newgraph.Graph g =
	    sandmark.util.newgraph.Graphs.createGraph(null, null);
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2),
	    n3 = new java.lang.Integer(3);
	g = g.addNode(n1).addNode(n2);
	g = g.addEdge(n1, n2).addEdge(n1, n2);
	//g = g.addEdge(n1, n2);//.addEdge(n1, n2);
	//g = g.addEdge(n1, n2).addEdge(n1, n2);
	//g = g.addEdge(n1, n2).addEdge(n1, n2);
	//g = g.addEdge(n1, n2).addEdge(n1, n2);
	//g = g.addEdge(n1, n2).addEdge(n1, n2);
	//g = g.addEdge(n1, n2).addEdge(n1, n2);
	//g = g.addEdge(n1, n2).addEdge(n1, n2);
	//g = g.addEdge(n1, n3);
	return g;
    }

    public static sandmark.util.newgraph.Graph createCrossGraph() {
	sandmark.util.newgraph.Graph g =
	    sandmark.util.newgraph.Graphs.createGraph(null, null);
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2),
	    n3 = new java.lang.Integer(3),
	    n4 = new java.lang.Integer(4),
	    n5 = new java.lang.Integer(5),
	    n6 = new java.lang.Integer(6),
	    n7 = new java.lang.Integer(7),
	    n8 = new java.lang.Integer(8),
	    n9 = new java.lang.Integer(9),
	    n10 = new java.lang.Integer(10),
	    n11 = new java.lang.Integer(11),
	    n12 = new java.lang.Integer(12),
	    n13 = new java.lang.Integer(13),
	    n14 = new java.lang.Integer(14),
	    n15 = new java.lang.Integer(15),
	    n16 = new java.lang.Integer(16),
	    n17 = new java.lang.Integer(17),
	    n18 = new java.lang.Integer(18),
	    n19 = new java.lang.Integer(19),
	    n20 = new java.lang.Integer(20);
	g = g.addEdge(n1, n2).addEdge(n1, n3).addEdge(n1, n4);
	g = g.addEdge(n8, n5).addEdge(n8, n6).addEdge(n8, n7);
	g = g.addEdge(n9, n2).addEdge(n9, n3).addEdge(n9, n4);
	g = g.addEdge(n10, n5).addEdge(n10, n6).addEdge(n10, n7);
	g = g.addEdge(n11, n2).addEdge(n11, n3).addEdge(n11, n4);
	g = g.addEdge(n12, n5).addEdge(n12, n6).addEdge(n12, n7);
	g = g.addEdge(n2, n13).addEdge(n2, n14).addEdge(n2, n15);
	g = g.addEdge(n3, n16).addEdge(n3, n17).addEdge(n3, n18);
	g = g.addEdge(n4, n13).addEdge(n4, n14).addEdge(n4, n15);
	g = g.addEdge(n5, n16).addEdge(n5, n17).addEdge(n5, n18);
	g = g.addEdge(n6, n13).addEdge(n6, n14).addEdge(n6, n15);
	g = g.addEdge(n7, n16).addEdge(n7, n17).addEdge(n7, n18);
	g = g.addEdge(n16, n19).addEdge(n17, n19).addEdge(n18, n19);
	g = g.addEdge(n13, n20).addEdge(n14, n20).addEdge(n15, n20);
	return g;
    }

    public static sandmark.util.newgraph.Graph createTestGraph() {
	sandmark.util.newgraph.Graph g =
	    sandmark.util.newgraph.Graphs.createGraph(null, null);
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2),
	    n3 = new java.lang.Integer(3),
	    n4 = new java.lang.Integer(4),
	    n5 = new java.lang.Integer(5),
	    n6 = new java.lang.Integer(6),
	    n7 = new java.lang.Integer(7),
	    n8 = new java.lang.Integer(8),
	    n9 = new java.lang.Integer(9),
	    n10 = new java.lang.Integer(10),
	    n11 = new java.lang.Integer(11),
	    n12 = new java.lang.Integer(12);
	g = g.addNode(n1).addNode(n2).addNode(n3).addNode(n4).addNode(n5);
	g = g.addNode(n6).addNode(n7).addNode(n8).addNode(n9).addNode(n10);
	g = g.addNode(n11).addNode(n12);
	g = g.addEdge(n12, n1).addEdge(n1, n2).addEdge(n1, n11);
	g = g.addEdge(n2, n3).addEdge(n2, n5);
	g = g.addEdge(n3, n4).addEdge(n3, n7).addEdge(n3, n8);
	g = g.addEdge(n4, n5);
	g = g.addEdge(n5, n6).addEdge(n5, n10);
	g = g.addEdge(n6, n7).addEdge(n6, n8);
	g = g.addEdge(n7, n8).addEdge(n7, n10);
	g = g.addEdge(n8, n9).addEdge(n8, n11);
	g = g.addEdge(n9, n10);
	g = g.addEdge(n10, n11);

	g = g.addEdge(n1, n1);
	g = g.addEdge(n2, n2);
	g = g.addEdge(n3, n3);
	g = g.addEdge(n4, n4);
	g = g.addEdge(n5, n5);
	g = g.addEdge(n6, n6);
	g = g.addEdge(n7, n7);
	g = g.addEdge(n8, n8);
	g = g.addEdge(n9, n9);
	g = g.addEdge(n10, n10);
	g = g.addEdge(n11, n11);
	g = g.addEdge(n12, n12);

	return g;
    }

    public static sandmark.util.newgraph.Graph createTree() {
	sandmark.util.newgraph.Graph g =
	    sandmark.util.newgraph.Graphs.createGraph(null, null);
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2),
	    n3 = new java.lang.Integer(3),
	    n4 = new java.lang.Integer(4),
	    n5 = new java.lang.Integer(5),
	    n6 = new java.lang.Integer(6),
	    n7 = new java.lang.Integer(7),
	    n8 = new java.lang.Integer(8),
	    n9 = new java.lang.Integer(9);
	java.lang.String n10 = "10";
	g = g.addNode(n1).addNode(n2).addNode(n3).addNode(n4).addNode(n5);
	g = g.addNode(n6).addNode(n7).addNode(n8).addNode(n9).addNode(n10);
	g = g.addEdge(n1, n2).addEdge(n1, n3).addEdge(n1, n4);
	g = g.addEdge(n2, n5).addEdge(n2, n6);
	g = g.addEdge(n3, n7).addEdge(n3, n8).addEdge(n3, n9);
	g = g.addEdge(n4, n10);
	return g;
    }

    public static sandmark.util.newgraph.Graph createGraph() {
	sandmark.util.newgraph.Graph g =
	    sandmark.util.newgraph.Graphs.createGraph(null, null);
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2),
	    n3 = new java.lang.Integer(3),
	    n4 = new java.lang.Integer(4),
	    n5 = new java.lang.Integer(5);
	g = g.addNode(n1).addNode(n2).addNode(n3).addNode(n4).addNode(n5);
	g = g.addEdge(n1, n2).addEdge(n1, n3).addEdge(n2, n3);
	g = g.addEdge(n2, n4).addEdge(n3, n5).addEdge(n4, n5);
	return g;
    }

    public static sandmark.util.newgraph.Graph createSimpleGraph() {
	sandmark.util.newgraph.Graph g =
	    sandmark.util.newgraph.Graphs.createGraph(null, null);
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2);
	    //n3 = new java.lang.Integer(3);
	g = g.addEdge(n1, n2).addEdge(n1, n1).addEdge(n2, n2);
	g = g.addEdge(n1, n1).addEdge(n1, n1);
	g = g.addEdge(n2, n2).addEdge(n2, n2);
	g = g.addEdge(n1, n1).addEdge(n1, n1);
	g = g.addEdge(n2, n2).addEdge(n2, n2);
	g = g.addEdge(n1, n1).addEdge(n1, n1);
	g = g.addEdge(n2, n2).addEdge(n2, n2);
	//g = g.addEdge(n1, n1).addEdge(n1, n1);
	//g = g.addEdge(n2, n2).addEdge(n2, n2);
	/*
	java.lang.Integer n1 = new java.lang.Integer(1),
	    n2 = new java.lang.Integer(2),
	    n3 = new java.lang.Integer(3),
	    n4 = new java.lang.Integer(4),
	    n5 = new java.lang.Integer(5),
	    n6 = new java.lang.Integer(6);
	g = g.addEdge(n1, n2).addEdge(n2, n3).addEdge(n3, n1);
	g = g.addEdge(n4, n5).addEdge(n5, n4);//.addEdge(n6, n4);
	    //	g = g.addEdge(n6, n6);
	    */
	return g;
    }
}
