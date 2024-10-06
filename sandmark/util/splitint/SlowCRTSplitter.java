package sandmark.util.splitint;

public class SlowCRTSplitter extends CRTSplitter {
   public SlowCRTSplitter(int bits, int minModuli, int maxparts,
			  javax.crypto.SecretKey w) {
      super(bits, minModuli, maxparts, w);
   }

   public SlowCRTSplitter(int bits, int maxparts, javax.crypto.SecretKey w) {
      super(bits, maxparts, w);
   }

   protected java.util.Iterator filter(Congruence cs[]) {
      boolean exclude[] = new boolean[cs.length];
      java.util.HashMap counts = new java.util.HashMap();
      for (int i = 0; i < numModuli(); i++) {
	 counts.clear();

	 int numcongs = 0;
	 for (int j = 0; j < cs.length; j++)
	    if (!exclude[j]) {
	       Congruence c = cs[j];
	       if (c.prime1 == i || c.prime2 == i) {
		  numcongs++;
		  long res = c.residue % modulo(i);
		  Long r = new Long(res);
		  Integer oldcount = (Integer)counts.get(r);
		  if (oldcount == null)
		     counts.put(r, new Integer(1));
		  else
		     counts.put(r, new Integer(oldcount.intValue() + 1));
	       }
	    }

	 long maxres = -1;
	 int maxcount = 0;
	 int secondcount = 0;
	 java.util.Iterator ri = counts.keySet().iterator();
	 while (ri.hasNext()) {
	    Long tmp = (Long)ri.next();
	    long res = tmp.longValue();
	    int count = ((Integer)counts.get(tmp)).intValue();
	    if (count > maxcount) {
	       maxres = res;
	       secondcount = maxcount;
	       maxcount = count;
	    }
	 }

// 	 System.out.println("maxcount = " + maxcount);
// 	 System.out.println("secondcount = " + secondcount);
	 if (maxcount > 2 * secondcount) {
	    for (int j = 0; j < cs.length; j++)
	       if (!exclude[j]) {
		  Congruence c = cs[j];
		  if (c.prime1 == i || c.prime2 == i) {
		     long res = c.residue % modulo(i);
		     if (res != maxres)
			exclude[j] = true;
		  }
	       }
	 }
      }

      sandmark.util.newgraph.Graph g = 
	 sandmark.util.newgraph.Graphs.createGraph(null, null);
      for (int i = 0; i < cs.length; i++)
	 if (!exclude[i])
	    g = g.addNode(cs[i]);

      sandmark.util.newgraph.Graph h = g;
      for (int i = 0; i < cs.length - 1; i++)
	 for (int j = i+1; j < cs.length; j++) {
	    if (!exclude[i] && !exclude[j]) {
	       Congruence c1 = cs[i];
	       Congruence c2 = cs[j];
	       if (c1.prime1 != c2.prime1 
		   && c1.prime1 != c2.prime2
		   && c1.prime2 != c2.prime1 
		   && c1.prime2 != c2.prime2) {
		  
	       }
	       else if (c1.prime1 == c2.prime1 && c1.prime2 == c2.prime2) {
		  if (c1.residue != c2.residue)
		     g = g.addEdge(c1, c2).addEdge(c2, c1);
		  else
		     // shouldn't happen...  ??
		     h = h.addEdge(c1, c2).addEdge(c2, c1);
	       }
	       else {
		  int prime = -1;
		  if (c1.prime1 == c2.prime1 || c1.prime1 == c2.prime2)
		     prime = c1.prime1;
		  else
		     prime = c1.prime2;
		  long rr1 = c1.residue % modulo(prime);
		  long rr2 = c2.residue % modulo(prime);
		  if (rr1 != rr2)
		     g = g.addEdge(c1, c2).addEdge(c2, c1);
		  else
		     h = h.addEdge(c1, c2).addEdge(c2, c1);
	       }
	    }
	 }

      java.util.Set u = new java.util.HashSet();
      while (g.edgeCount() > 0) {
	 java.util.Iterator nodes = h.nodes();
	 java.lang.Object maxNode = null;
	 int max = -1;
	 java.lang.Object n = nextNode(nodes, u);
	 while (n != null) {
	    int d = h.outDegree(n);
	    if (d > max) {
	       max = d;
	       maxNode = n;
	    }
	    n = nextNode(nodes, u);
	 }
	 nodes = g.succs(maxNode);
	 while (nodes.hasNext()) {
	    java.lang.Object m = nodes.next();
	    g = g.removeNode(m);
	    h = h.removeNode(m);
	 }
	 u.add(maxNode);
      }

      return g.nodes();
   }

   private static java.lang.Object nextNode(java.util.Iterator i,
					    java.util.Set u) {
      while (i.hasNext()) {
	 java.lang.Object n = i.next();
	 if (!u.contains(n))
	    return n;
      }
      return null;
   }

   public static void main(String [] argv) throws Throwable {
      javax.crypto.KeyGenerator kg = 
	 javax.crypto.KeyGenerator.getInstance(getAlgorithm());
      javax.crypto.SecretKey w = kg.generateKey();
      CRTSplitter s = new SlowCRTSplitter(128, 20, w);
      for (long n = 0; n < 1000000; n++) {
	 java.math.BigInteger a[] = 
	    s.split(java.math.BigInteger.valueOf(n));
// 	 for (int i = 0; i < a.length; i++)
// 	    System.out.println(a[i]);
// 	 System.out.println("------------------------------------------");
// 	 System.out.println(s.combine(a));
	 java.math.BigInteger v = s.combine(a);
	 if (v.equals(java.math.BigInteger.valueOf(n)))
	    System.out.println(n);
	 else
	    throw new RuntimeException("n = " + n + ", v = " + v);
      }
   }
}
