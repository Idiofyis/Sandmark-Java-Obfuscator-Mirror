package sandmark.util.splitint;

public abstract class CRTSplitter extends ResidueSplitter {
   public boolean orderMatters() {
      return false;
   }

   private long p[];
   private javax.crypto.Cipher c;
   private javax.crypto.SecretKey w;
   private sandmark.util.newgraph.Graph splittingGraph;

   private static java.math.BigInteger MAX_LONG =
      java.math.BigInteger.valueOf(Long.MAX_VALUE);

   public CRTSplitter(int bits, int minModuli, int maxparts,
		      javax.crypto.SecretKey w) {
      java.math.BigInteger bigmod =
	 java.math.BigInteger.ONE.shiftLeft(bits);
      
      int pbits = 31;
      java.math.BigInteger pmod;
      do {
	 p = findmods(pbits--);
	 pmod = java.math.BigInteger.ONE;
	 for (int i = 0; i < p.length; i++)
	    pmod = pmod.multiply(java.math.BigInteger.valueOf(p[i]));
      } while (p.length < minModuli || bigmod.compareTo(pmod) > 0);

      try {
	 c = 
	    javax.crypto.Cipher.getInstance(getAlgorithm() + "/ECB/NoPadding");
      }
      catch (java.security.NoSuchAlgorithmException e) {
	 throw new RuntimeException("algorithm " + getAlgorithm() 
				    + " not found");
      }
      catch (javax.crypto.NoSuchPaddingException e) {
	 throw new RuntimeException("padding NoPadding not found");
      }
      this.w = w;

      if (maxparts <= 0)
	  splittingGraph = null;
      else {
	  if (maxparts < p.length - 1)
	      throw new RuntimeException("maxparts must be at least " +
					 (p.length-1));
	  Integer ia[] = new Integer[p.length];
	  for (int i = 0; i < p.length; i++)
	      ia[i] = new Integer(i);
	  java.util.Iterator ii = java.util.Arrays.asList(ia).iterator();
	  sandmark.util.newgraph.Graph g = 
	      sandmark.util.newgraph.Graphs.createGraph(ii, null);
	  int diff = 1;
	  int maxedges = 2*maxparts;
	  while (g.edgeCount() < maxedges && diff <= p.length/2) {
	      for (int i = 0; g.edgeCount() < maxedges && i < p.length; i++) {
		  Integer other = ia[(i+diff) % p.length];
		  g = g.addEdge(ia[i], other).addEdge(other, ia[i]);
	      }
	      diff++;
	  }
	  splittingGraph = g;
      }
   }

   public CRTSplitter(int bits, int maxparts, javax.crypto.SecretKey w) {
      this(bits, 0, maxparts, w);
   }

   public static String getAlgorithm() {
      return "Blowfish";
   }

   private static byte[] convert(long l) {
      byte b[] = new byte[8];
      for (int j = 0; j < 8; j++)
	 b[j] = (byte)(l >> (j*8));
      return b;
   }

   private static long convert(byte b[]) {
      long l = 0;
      for (int j = 0; j < 8; j++)
	 l |= (255 & (long)b[j]) << (j*8);
      return l;
   }

   public java.math.BigInteger[] split(java.math.BigInteger value) {
       int parts = p.length*(p.length-1)/2;
       if (splittingGraph != null)
	   parts = splittingGraph.edgeCount() / 2;

      long rval[] = new long[parts];

      int index = 0;
      long base = 0;
      for (int i = 0; i < p.length - 1; i++)
	 for (int j = i + 1; j < p.length; j++) {
	     long modulo = p[i]*p[j];
	     java.math.BigInteger residue = 
		 value.mod(java.math.BigInteger.valueOf(modulo));
	     java.math.BigInteger part = 
		 residue.add(java.math.BigInteger.valueOf(base));
	     if (part.compareTo(MAX_LONG) > 0
		 || part.compareTo(java.math.BigInteger.ZERO) < 0)
		 throw new RuntimeException("unexpected value");
	     if (splittingGraph == null 
		 || splittingGraph.hasEdge(new Integer(i), new Integer(j)))
		 rval[index++] = part.longValue();
	     base += modulo;
	 }

      synchronized(c) {
	 try {
	    c.init(javax.crypto.Cipher.ENCRYPT_MODE, w);
	 }
	 catch (java.security.InvalidKeyException e) {
	    throw new RuntimeException("bad key");
	 }
	 java.math.BigInteger r[] = new java.math.BigInteger[rval.length];
	 java.util.Vector indices = new java.util.Vector();
	 java.util.Random rand = sandmark.util.Random.getRandom();
	 for (int i = 0; i < r.length; i++)
	    indices.add(new Integer(i));
	 for (int i = 0; i < r.length; i++) {
	    java.io.ByteArrayOutputStream bos = 
	       new java.io.ByteArrayOutputStream(8);
	    byte b[] = convert(rval[i]);
	    javax.crypto.CipherOutputStream cos = 
	       new javax.crypto.CipherOutputStream(bos, c);
	    try {
	       cos.write(b);
	    }
	    catch (java.io.IOException e) {
	       throw new RuntimeException("shouldn't happen");
	    }
	    Integer rindex = 
	       ((Integer)indices.remove(rand.nextInt(indices.size())));
	    r[rindex.intValue()] = 
	       java.math.BigInteger.valueOf(convert(bos.toByteArray()));
	 }
	 return r;
      }
   }

   protected class Congruence {
      public long residue;
      public int prime1, prime2;

      public int hashCode() {
	 int p1 = Math.min(prime1, prime2);
	 int p2 = Math.max(prime1, prime2);
	 return p1 ^ (p2 << 16) ^ ((int)residue);
      }

      public String toString() {
	 long modulo = p[prime1] * p[prime2];
	 return "" + residue + " mod " + modulo;
      }

      public boolean equals(Object o) {
	 if (!(o instanceof Congruence))
	    return false;

	 Congruence c = (Congruence)o;
	 if (residue != c.residue)
	    return false;

	 if (prime1 == c.prime1 && prime2 == c.prime2)
	    return true;
	 else if (prime1 == c.prime2 && prime2 == c.prime1)
	    return true;
	 else
	    return false;
      }
   }

   protected int numModuli() {
      return p.length;
   }

   protected long modulo(int i) {
      return p[i];
   }

   abstract protected java.util.Iterator filter(Congruence cs[]);

   public java.math.BigInteger[] combineRes(java.math.BigInteger parts[]) {
      java.util.HashSet seen = new java.util.HashSet();

      synchronized(c) {
	 try {
	    c.init(javax.crypto.Cipher.DECRYPT_MODE, w);
	 }
	 catch (java.security.InvalidKeyException e) {
	    throw new RuntimeException("bad key");
	 }
	 for (int k = 0; k < parts.length; k++) {
	    java.io.ByteArrayOutputStream bos =
	       new java.io.ByteArrayOutputStream(8);
	    javax.crypto.CipherOutputStream cos =
	       new javax.crypto.CipherOutputStream(bos, c);
	    try {
	       cos.write(convert(parts[k].longValue()));
	    }
	    catch (java.io.IOException e) {
	       throw new RuntimeException("shouldn't happen");
	    }

	    long piece = convert(bos.toByteArray());

	    if (piece >= 0) {
	       Congruence c = new Congruence();
	       boolean done = false;
	       for (int i = 0; !done && i < p.length - 1; i++)
		  for (int j = i + 1; !done && j < p.length; j++) {
		     long modulo = p[i]*p[j];
		     if (piece >= modulo)
			piece -= modulo;
		     else {
			done = true;
			c.prime1 = i;
			c.prime2 = j;
			c.residue = piece;
			seen.add(c);
		     }
		  }
	    }
	 }
      }

      Congruence ca[] = new Congruence[seen.size()];
      java.util.Iterator j = seen.iterator();
      for ( int k = 0; j.hasNext(); k++)
	 ca[k] = (Congruence)j.next();
      java.util.Iterator i = filter(ca);

      java.math.BigInteger residue = java.math.BigInteger.ZERO;
      java.math.BigInteger modulo = java.math.BigInteger.ONE;
      while (i.hasNext()) {
	 Congruence c = (Congruence)i.next();
	 java.math.BigInteger m = 
	    java.math.BigInteger.valueOf(p[c.prime1])
	      .multiply(java.math.BigInteger.valueOf(p[c.prime2]));
	 java.math.BigInteger tmp[] = chinese(residue, modulo, 
					      java.math.BigInteger.valueOf(c.residue), m);
	 residue = tmp[0];
	 modulo = tmp[1];
      }

      java.math.BigInteger rval[] = new java.math.BigInteger[2];
      rval[0] = residue;
      rval[1] = modulo;
      return rval;
   }

   private static java.math.BigInteger[] chinese(java.math.BigInteger a,
						 java.math.BigInteger m,
						 java.math.BigInteger b,
						 java.math.BigInteger n) {
      java.math.BigInteger g[] = euclid(m, n);
      java.math.BigInteger diff = a.subtract(b);
      java.math.BigInteger d[] = diff.divideAndRemainder(g[2]);
      if (!d[1].equals(java.math.BigInteger.ZERO))
	 return null;
      java.math.BigInteger rval[] = new java.math.BigInteger[2];
      rval[1] = m.multiply(n).divide(g[2]);
      rval[0] = n.multiply(g[1]).multiply(d[0]).add(b).mod(rval[1]);
      return rval;
   }

   private static java.math.BigInteger[] euclid(java.math.BigInteger m,
						java.math.BigInteger n) {
      java.math.BigInteger rval[] = new java.math.BigInteger[3];
      if (m.equals(java.math.BigInteger.ZERO)) {
	 rval[0] = java.math.BigInteger.ZERO;
	 rval[1] = java.math.BigInteger.ONE;
	 rval[2] = n;
      }
      else {
	 java.math.BigInteger d[] = n.divideAndRemainder(m);
	 java.math.BigInteger r[] = euclid(d[1], m);
	 rval[0] = r[1].subtract(r[0].multiply(d[0]));
	 rval[1] = r[0];
	 rval[2] = r[2];
      }
      return rval;
   }

   private static long gcd(long a, long b) {
      if (a == 0)
	 return b;
      else
	 return gcd(b % a, a);
   }

   private static long[] findmods(int bits) {
      long tmp[] = new long[2];
      long modulo = ((long)1) << bits;

      tmp[0] = modulo;

      long sum = 0;
      int index = 1;

      while (sum >= 0) {
	 modulo--;
	 boolean good = true;
	 for (int i = 0; i < index && good; i++)
	    if (gcd(modulo, tmp[i]) > 1)
	       good = false;
	 if (good) {
	    for (int i = 0; i < index && sum >= 0; i++)
	       sum += modulo * tmp[i];
	    if (index >= tmp.length) {
	       long tmp2[] = new long[tmp.length * 2];
	       System.arraycopy(tmp, 0, tmp2, 0, index);
	       tmp = tmp2;
	    }
	    tmp[index++] = modulo;
	 }
      }

      long rval[] = new long[index-1];
      System.arraycopy(tmp, 0, rval, 0, index-1);
      return rval;
   }
}

