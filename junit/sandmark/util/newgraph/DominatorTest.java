package sandmark.util.newgraph;

public class DominatorTest extends junit.framework.TestCase {
   private void assertDom(Graph g, boolean correct, java.lang.Object n[],
			  int dominated, int dominator) {
      boolean actual = g.reachable(n[dominator], n[dominated]);
      if (correct)
	 assertTrue("" + n[dominator] + " should dominate " + n[dominated],
		    actual);
      else
	 assertFalse("" + n[dominator] + " shouldn't dominate " + n[dominated],
		     actual);
   }

   private Graph g;
   private java.lang.Object n[];

   private Graph h;
   private java.lang.Object s[];

   private Graph j;
   private java.lang.Object jn[];

   protected void setUp() {
      g = Graphs.createGraph(null, null);
      n = new java.lang.Object[10];
      for (int i = 0; i < n.length; i++)
	 g = g.addNode(n[i] = new java.lang.Integer(i));

      g = g.addEdge(n[0], n[1]);
      g = g.addEdge(n[0], n[2]);
      g = g.addEdge(n[1], n[2]);
      g = g.addEdge(n[2], n[3]);
      g = g.addEdge(n[3], n[4]);
      g = g.addEdge(n[3], n[5]);
      g = g.addEdge(n[4], n[6]);
      g = g.addEdge(n[5], n[6]);
      g = g.addEdge(n[6], n[7]);
      g = g.addEdge(n[6], n[3]);
      g = g.addEdge(n[7], n[8]);
      g = g.addEdge(n[7], n[9]);
      g = g.addEdge(n[7], n[2]);
      g = g.addEdge(n[8], n[0]);
      g = g.addEdge(n[9], n[6]);

      h = Graphs.createGraph(null, null);
      s = new java.lang.Object[8];
      s[0] = "entry";
      for (int i = 1; i <= 6; i++)
	 s[i] = "B" + i;
      s[7] = "exit";
      h = h.addEdge(s[0], s[1]);
      h = h.addEdge(s[1], s[2]);
      h = h.addEdge(s[1], s[3]);
      h = h.addEdge(s[3], s[4]);
      h = h.addEdge(s[4], s[5]);
      h = h.addEdge(s[4], s[6]);
      h = h.addEdge(s[6], s[4]);
      h = h.addEdge(s[2], s[7]);
      h = h.addEdge(s[5], s[7]);

      j = Graphs.createGraph(null, null);
      jn = new java.lang.Object[10];
      for (int i = 0; i < jn.length; i++)
	 j = j.addNode(jn[i] = new java.lang.Integer(i));

      j = j.addEdge(jn[0], jn[1]);
      j = j.addEdge(jn[1], jn[2]);
      j = j.addEdge(jn[1], jn[3]);
      j = j.addEdge(jn[2], jn[3]);
      j = j.addEdge(jn[2], jn[7]);
      j = j.addEdge(jn[3], jn[4]);
      j = j.addEdge(jn[4], jn[5]);
      j = j.addEdge(jn[4], jn[6]);
      j = j.addEdge(jn[5], jn[6]);
      j = j.addEdge(jn[5], jn[8]);
      j = j.addEdge(jn[6], jn[1]);
      j = j.addEdge(jn[6], jn[7]);
      j = j.addEdge(jn[7], jn[8]);
      j = j.addEdge(jn[8], jn[9]);
   }

   public void testNewDominator() {
      Graph d = h.dominatorTree(s[0]);

      for (int i = 0; i < s.length - 1; i++)
	 for (int j = i + 1; j < s.length; j++)
	    assertDom(d, false, s, i, j);

      assertDom(d, true, s, 0, 0);
      for (int i = 1; i < s.length; i++) {
	 assertDom(d, true, s, i, 0);
	 assertDom(d, true, s, i, 1);
	 assertDom(d, true, s, i, i);
      }

      assertDom(d, false, s, 3, 2);

      assertDom(d, false, s, 4, 2);
      assertDom(d, true, s, 4, 3);

      assertDom(d, false, s, 5, 2);
      assertDom(d, true, s, 5, 3);
      assertDom(d, true, s, 5, 4);

      assertDom(d, false, s, 6, 2);
      assertDom(d, true, s, 6, 3);
      assertDom(d, true, s, 6, 4);
      assertDom(d, false, s, 6, 5);

      for (int i = 2; i < 7; i++)
	 assertDom(d, false, s, 7, i);

      d = g.dominatorTree(n[0]);

      for (int i = 0; i < n.length - 1; i++)
	 for (int j = i + 1; j < n.length; j++)
	    assertDom(d, false, n, i, j);
      
      for (int i = 0; i < n.length; i++) {
	 assertDom(d, true, n, i, 0);
	 assertDom(d, true, n, i, i);
      }

      assertDom(d, false, n, 2, 1);

      assertDom(d, false, n, 3, 1);
      assertDom(d, true, n, 3, 2);
      
      assertDom(d, false, n, 4, 1);
      assertDom(d, true, n, 4, 2);
      assertDom(d, true, n, 4, 3);

      assertDom(d, false, n, 5, 1);
      assertDom(d, true, n, 5, 2);
      assertDom(d, true, n, 5, 3);
      assertDom(d, false, n, 5, 4);
      
      assertDom(d, false, n, 6, 1);
      assertDom(d, true, n, 6, 2);
      assertDom(d, true, n, 6, 3);
      assertDom(d, false, n, 6, 4);
      assertDom(d, false, n, 6, 5);
      
      assertDom(d, false, n, 7, 1);
      assertDom(d, true, n, 7, 2);
      assertDom(d, true, n, 7, 3);
      assertDom(d, false, n, 7, 4);
      assertDom(d, false, n, 7, 5);
      assertDom(d, true, n, 7, 6);

      assertDom(d, false, n, 8, 1);
      assertDom(d, true, n, 8, 2);
      assertDom(d, true, n, 8, 3);
      assertDom(d, false, n, 8, 4);
      assertDom(d, false, n, 8, 5);
      assertDom(d, true, n, 8, 6);
      assertDom(d, true, n, 8, 7);

      assertDom(d, false, n, 9, 1);
      assertDom(d, true, n, 9, 2);
      assertDom(d, true, n, 9, 3);
      assertDom(d, false, n, 9, 4);
      assertDom(d, false, n, 9, 5);
      assertDom(d, true, n, 9, 6);
      assertDom(d, true, n, 9, 7);

      d = j.dominatorTree(jn[0]);

      for (int i = 0; i < jn.length - 1; i++)
	 for (int j = i + 1; j < jn.length; j++)
	    assertDom(d, false, jn, i, j);

      assertDom(d, true, jn, 0, 0);
      for (int i = 1; i < jn.length; i++) {
	 assertDom(d, true, jn, i, 0);
	 assertDom(d, true, jn, i, 1);
	 assertDom(d, true, jn, i, i);
      }

      assertDom(d, false, jn, 3, 2);

      assertDom(d, false, jn, 4, 2);
      assertDom(d, true, jn, 4, 3);
      
      assertDom(d, false, jn, 5, 2);
      assertDom(d, true, jn, 5, 3);
      assertDom(d, true, jn, 5, 4);

      assertDom(d, false, jn, 6, 2);
      assertDom(d, true, jn, 6, 3);
      assertDom(d, true, jn, 6, 4);
      assertDom(d, false, jn, 6, 5);

      for (int i = 2; i < 7; i++)
	 assertDom(d, false, jn, 7, i);

      for (int i = 2; i < 8; i++)
	 assertDom(d, false, jn, 8, i);
   }

   public static void main(String [] argv) {
      DominatorTest t = new DominatorTest();
      t.setUp();
      t.testNewDominator();
   }
}
