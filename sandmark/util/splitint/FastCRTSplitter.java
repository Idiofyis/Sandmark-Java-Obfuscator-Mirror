package sandmark.util.splitint;

public class FastCRTSplitter extends CRTSplitter {
   public FastCRTSplitter(int bits, int minModuli, int maxparts,
			  javax.crypto.SecretKey w) {
      super(bits, minModuli, maxparts, w);
   }

   public FastCRTSplitter(int bits, int maxparts, javax.crypto.SecretKey w) {
      super(bits, maxparts, w);
   }

   protected java.util.Iterator filter(Congruence cs[]) {
      boolean exclude[] = new boolean[cs.length];
      java.util.HashMap counts = new java.util.HashMap();
      for (int i = 0; i < numModuli(); i++) {
	 counts.clear();

	 for (int j = 0; j < cs.length; j++)
	    if (!exclude[j]) {
	       Congruence c = cs[j];
	       if (c.prime1 == i || c.prime2 == i) {
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
	 java.util.Iterator ri = counts.keySet().iterator();
	 while (ri.hasNext()) {
	    Long tmp = (Long)ri.next();
	    long res = tmp.longValue();
	    int count = ((Integer)counts.get(tmp)).intValue();
	    if (count > maxcount) {
	       maxres = res;
	       maxcount = count;
	    }
	 }

	 if (maxres > 0)
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

      java.util.Vector rval = new java.util.Vector();
      for (int j = 0; j < cs.length; j++)
	 if (!exclude[j])
	    rval.add(cs[j]);
      return rval.iterator();
   }

   public static void main(String [] argv) throws Throwable {
      javax.crypto.KeyGenerator kg = 
	 javax.crypto.KeyGenerator.getInstance(getAlgorithm());
      javax.crypto.SecretKey w = kg.generateKey();
      CRTSplitter s = new FastCRTSplitter(128, 10, w);
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
