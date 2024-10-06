package sandmark.util.splitint;

public class CombinationSplitter implements IntSplitter {
   private int maxValue;
   private java.math.BigInteger bigMaxValue;

   public CombinationSplitter(int maxValue) {
      this.maxValue = maxValue;
      bigMaxValue = java.math.BigInteger.valueOf(maxValue);
   }

   public java.math.BigInteger[] split(java.math.BigInteger value) {
      int n = 0;
      java.math.BigInteger combCount = java.math.BigInteger.ONE;
      while (value.compareTo(combCount) >= 0) {
	 value = value.subtract(combCount);
	 n++;
	 java.math.BigInteger numer = 
	    java.math.BigInteger.valueOf(n + maxValue);
	 java.math.BigInteger denom = java.math.BigInteger.valueOf(n);
	 combCount = combCount.multiply(numer).divide(denom);
      }
      
      java.math.BigInteger parts[] = new java.math.BigInteger[n];
      if (n == 0)
	 return parts;
      java.util.SortedSet comb = 
	 sandmark.util.Math.getCombination(value, n+maxValue, maxValue);
      java.util.Iterator i = comb.iterator();
      int prev = -1;
      int currValue = 0;
      int pos = 0;
      while (i.hasNext()) {
	 if (currValue > maxValue)
	    throw new java.lang.RuntimeException();
	 int curr = ((java.lang.Integer)i.next()).intValue();
	 java.math.BigInteger currBigValue = 
	    java.math.BigInteger.valueOf(currValue);
	 for (int j = 0; j < curr-prev-1; j++)
	    parts[pos++] = currBigValue;
	 currValue++;
	 prev = curr;
      }
      while (pos < parts.length)
	 parts[pos++] = bigMaxValue;
      return parts;
   }

   public java.math.BigInteger combine(java.math.BigInteger [] bigParts) {
      for (int i = 0; i < bigParts.length; i++)
	 if (bigMaxValue.compareTo(bigParts[i]) < 0
	     || java.math.BigInteger.ZERO.compareTo(bigParts[i]) > 0)
	    throw new java.lang.IllegalArgumentException();

      int parts[] = new int[bigParts.length];
      for (int i = 0; i < parts.length; i++)
	 parts[i] = bigParts[i].intValue();
      java.util.Arrays.sort(parts);
      
      java.util.Set comb = new java.util.HashSet();
      int currPart = 0;
      int elem = 0;
      for (int i = 0; i < parts.length; i++) {
	 while (parts[i] > currPart) {
	    comb.add(new java.lang.Integer(elem++));
	    currPart++;
	 }
	 elem++;
      }
      while (maxValue > currPart) {
	 comb.add(new java.lang.Integer(elem++));
	 currPart++;
      }

      int n = 0;
      java.math.BigInteger combSum = java.math.BigInteger.ZERO;
      java.math.BigInteger combCount = java.math.BigInteger.ONE;
      while (n < parts.length) {
	 combSum = combSum.add(combCount);
	 n++;
	 java.math.BigInteger numer = 
	    java.math.BigInteger.valueOf(n + maxValue);
	 java.math.BigInteger denom = java.math.BigInteger.valueOf(n);
	 combCount = combCount.multiply(numer).divide(denom);
      }

      return combSum.add(sandmark.util.Math.decodeCombination(comb, 
							      parts.length + maxValue));
   }
   
   public boolean orderMatters() {
      return false;
   }   

   public static void main(String [] argv) {
      for (int k = 5; k <= 15; k++) {
	 CombinationSplitter s = new CombinationSplitter(k);
	 for (int n = 0; n < 1024; n++) {
	    java.math.BigInteger value = 
	       java.math.BigInteger.valueOf(n);
	    java.math.BigInteger [] parts = null;
	    parts = s.split(value);
	    java.util.Arrays.sort(parts);
	    System.out.print("n = " + n + ", parts =");
	    for (int i = 0; i < parts.length; i++)
	       System.out.print(" " + parts[i]);
	    System.out.println();
	    if (!value.equals(s.combine(parts))) {
	       System.err.println("uhoh!  n = " + n + ", result = " 
				  + s.combine(parts));
	       System.exit(-1);
	    }
	 }
      }
   }
}
