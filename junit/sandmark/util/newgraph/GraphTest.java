package sandmark.util.newgraph;

public class GraphTest extends junit.framework.TestCase {
   private Graph rootGraph, rootGraph2;
   private java.lang.Integer rootNode, isolatedNode;

   protected void setUp() {
      Graph g = Graphs.createGraph(null, null);
      java.lang.Integer n1 = new java.lang.Integer(1),
	 n2 = new java.lang.Integer(2),
	 n3 = new java.lang.Integer(3),
	 n4 = new java.lang.Integer(4),
	 n5 = new java.lang.Integer(5);
      g = g.addNode(n1).addNode(n2).addNode(n3).addNode(n4).addNode(n5);
      g = g.addEdge(n1, n2).addEdge(n1, n3).addEdge(n1, n5);
      g = g.addEdge(n4, n4);
      g = g.addEdge(n2, n5).addEdge(n5, n3);

      rootGraph = g;
      rootNode = n1;
      isolatedNode = n4;

      rootGraph2 = g.addEdge(n5, n2);
   }

   public void testAddRemoveNode() {
      int values[] = {10, 50, 100, 500};
      for (int j = 0; j < values.length; j++) {
	 Graph g = Graphs.createGraph(null, null);
	 for (int i = 0; i < values[j]; i++) {
	    g = g.addNode(new java.lang.Integer(i));
	    assertEquals("j = " + j, i + 1, g.nodeCount());
	 }
	 for (int i = values[j]-1; i >= 0; i--) {
	    assertTrue(g.hasNode(new java.lang.Integer(i)));
	    g = g.removeNode(new java.lang.Integer(i));
	    java.util.Iterator it = g.nodes();
	    while (it.hasNext()) {
	       Object o = it.next();
	       assertNotNull(o);
	    }
	    for (int k = i; k < values[j]; k++)
	       assertFalse("k = " + k, g.hasNode(new java.lang.Integer(k)));
	    for (int k = 0; k < i; k++)
	       assertTrue("i = " + i + ", j = " + j + ", k = " + k, 
			  g.hasNode(new java.lang.Integer(k)));
	    assertEquals("j = " + j, i, g.nodeCount());
	 }
      }
   }

   public void testNodeIterator() {
      Graph g = Graphs.createGraph(null, null);
      for (int i = 0; i < 1000; i++)
	 g = g.addNode(new java.lang.Integer(i));
      java.util.Iterator i = g.nodes();
      java.util.Set s = new java.util.HashSet();
      while (i.hasNext())
	 s.add(i.next());
      assertEquals(1000, s.size());
   }

   public void testConsolidate() {
      Graph g = Graphs.createGraph(null, null);
      for (int i = 0; i < 1000; i++)
	 g = g.addNode(new java.lang.Integer(i));
      assertEquals(g.nodeCount(), g.consolidate().nodeCount());
   }

   public void testAddRemoveEdge() {
      Graph g = Graphs.createGraph(null, null);
      java.lang.Integer nodes[] = new java.lang.Integer[50];
      for (int i = 0; i < nodes.length; i++)
	 g = g.addNode(nodes[i] = new java.lang.Integer(i));
      assertEquals(nodes.length, g.nodeCount());

      int count = 0;
      for (int i = 0; i < nodes.length; i++)
	 for (int j = 0; j < nodes.length; j++) {
	    g = g.addEdge(nodes[i], nodes[j]);
	    assertTrue(g.hasEdge(nodes[i], nodes[j]));
	    assertEquals(++count, g.edgeCount());
	    java.util.Set s = new java.util.HashSet();
	    java.util.Iterator it = g.edges();
	    while (it.hasNext())
	       s.add(it.next());
	    assertEquals(count, s.size());
	    assertEquals(nodes.length, g.nodeCount());
	 }

      for (int i = 0; i < nodes.length; i++)
	 for (int j = 0; j < nodes.length; j++)
	    assertTrue(g.hasEdge(nodes[i], nodes[j]));

      Graph old = g;
      int oldCount = count;

      for (int i = 0; i < nodes.length; i++)
	 for (int j = 0; j < nodes.length; j++) {
	    assertTrue(g.hasEdge(nodes[i], nodes[j]));
	    g = g.removeEdge(nodes[i], nodes[j]);
	    assertEquals(--count, g.edgeCount());
	    assertFalse("i = " + i + ", j = " + j,
			g.hasEdge(nodes[i], nodes[j]));
	    java.util.Set s = new java.util.HashSet();
	    java.util.Iterator it = g.edges();
	    while (it.hasNext())
	       s.add(it.next());
	    assertEquals(count, s.size());
	    assertEquals(nodes.length, g.nodeCount());
	 }

      java.util.Iterator edges = old.edges();
      while (edges.hasNext()) {
	 old = old.removeEdge((Edge)edges.next());
	 assertEquals(--oldCount, old.edgeCount());
      }
   }

   public void testOld() {
      Graph g = Graphs.createGraph(null, null);
      java.lang.Integer n1, n2, n3, n4;
      g = g.addNode(n1 = new java.lang.Integer(1));
      g = g.addNode(n2 = new java.lang.Integer(2));
      g = g.addNode(n3 = new java.lang.Integer(3));
      g = g.addNode(n4 = new java.lang.Integer(4));
      g = g.addEdge(n1, n2);
      g = g.addEdge(n2, n3);
      g = g.addEdge(n2, n4);
      g = g.addEdge(n3, n1);
      g = g.addEdge(n1, n1);
      g = g.addEdge(n4, n1);
      assertEquals(4, g.nodeCount());
      assertEquals(6, g.edgeCount());
      
      java.util.Iterator edges = g.inEdges(n1);
      int count = 0;
      while (edges.hasNext()) {
	 Edge e = (Edge)edges.next();
	 count++;
	 int source = ((java.lang.Integer)e.sourceNode()).intValue();
	 assertTrue(source == 1 || source == 3 || source == 4);
      }
      assertEquals(3, count);

      edges = g.outEdges(n1);
      count = 0;
      while (edges.hasNext()) {
	 Edge e = (Edge)edges.next();
	 count++;
	 int sink = ((java.lang.Integer)e.sinkNode()).intValue();
	 assertTrue(sink == 1 || sink == 2);
      }
      assertEquals(2, count);

      Graph h = g.removeNode(n1);
      assertFalse(h.hasNode(n1));
      assertEquals(3, h.nodeCount());
      assertEquals(2, h.edgeCount());
      assertEquals(4, g.nodeCount());
      assertEquals(6, g.edgeCount());
   }

   public void testRoots() {
      Graph g = rootGraph;

      java.util.Iterator i = g.roots();
      int count = 0;
      while (i.hasNext()) {
	 int value = ((java.lang.Integer)i.next()).intValue();
	 count++;
	 assertEquals(1, value);
      }
      assertEquals(1, count);

      i = g.reverseRoots();
      count = 0;
      while (i.hasNext()) {
	 int value = ((java.lang.Integer)i.next()).intValue();
	 count++;
	 assertEquals(3, value);
      }
      assertEquals(1, count);
   }

   public void testUnreachable() {
      Graph g = rootGraph.removeUnreachable(rootNode);
      assertEquals(rootGraph.nodeCount() - 1, g.nodeCount());
      assertEquals(rootGraph.edgeCount() - 1, g.edgeCount());

      g = rootGraph.removeUnreachable(isolatedNode);
      assertEquals(1, g.nodeCount());
      assertEquals(1, g.edgeCount());
   }

   public void testDepthFirst() {
      java.util.Iterator i = rootGraph2.depthFirst(rootNode);
      java.util.Set s = new java.util.HashSet();
      int count = 0;
      while (i.hasNext()) {
	 s.add(i.next());
	 count++;
	 assertTrue(count < rootGraph2.nodeCount());
      }
      assertEquals(rootGraph2.nodeCount() - 1, s.size());
   }

   public void testBreadthFirst() {
      java.util.Iterator i = rootGraph2.breadthFirst(rootNode);
      java.util.Set s = new java.util.HashSet();
      int count = 0;
      while (i.hasNext()) {
	 s.add(i.next());
	 count++;
	 assertTrue(count < rootGraph2.nodeCount());
      }
      assertEquals(rootGraph2.nodeCount() - 1, s.size());
   }
}
