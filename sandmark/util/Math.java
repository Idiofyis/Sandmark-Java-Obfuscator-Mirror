package sandmark.util;

public class Math {   
   /**
    * Returns the value C(n,k).
    *
    * @param n number of elements to choose from
    * @param k number of elements to choose
    * @return number of possible combinations
    */
   public static java.math.BigInteger combinations(int n, int k) {
       return combinations(java.math.BigInteger.valueOf(n),
                           java.math.BigInteger.valueOf(k));
   }

 /**@return n!
    @throws IllegalArgumentException if n < 0
    */
    public static java.math.BigInteger factorial(long n){
	return factorial(java.math.BigInteger.valueOf(n));
    }

    public static java.math.BigInteger factorial(java.math.BigInteger n) {
        if(n.compareTo(java.math.BigInteger.ZERO) < 0) 
            throw new IllegalArgumentException("Factorials are defined for n >= 0");

	java.math.BigInteger rv = java.math.BigInteger.ONE;
	for( ; n.compareTo(java.math.BigInteger.ONE) > 0 ; 
	     n = n.subtract(java.math.BigInteger.ONE))
	    rv = rv.multiply(n);

	return rv;
    }

   /**
    * Returns the value C(n,k).
    *
    * @param n number of elements to choose from
    * @param k number of elements to choose
    * @return number of possible combinations
    */
   public static java.math.BigInteger combinations(java.math.BigInteger n,
                                                   java.math.BigInteger k) {
       java.math.BigInteger zero = java.math.BigInteger.ZERO;
       java.math.BigInteger one = java.math.BigInteger.ONE;

      if (k.compareTo(zero) < 0 || k.compareTo(n) > 0)
          return zero;

      java.math.BigInteger reflect = n.subtract(k);
      if (k.compareTo(reflect) > 0)
	  k = reflect;

      java.math.BigInteger current = one;

      for(java.math.BigInteger i = n;
          i.compareTo(k) > 0;
          i = i.subtract(one)){
	 current = current.multiply(i);
      }
      current = current.divide(factorial(n.subtract(k))); 
      return current;
   }
   
   public static java.util.SortedSet getCombination(java.math.BigInteger val, 
						     int n, int k) {
      if (val.compareTo(java.math.BigInteger.ZERO) < 0 ||
	  val.compareTo(combinations(n, k)) >= 0)
	 return null;
      
      return getCombination(val, n, k, 0);
   }

   private static java.util.SortedSet getCombination(java.math.BigInteger val, 
						     int n, int k, 
						     int smallest) 
   {
      if (val.equals(java.math.BigInteger.ZERO)) {
	 java.util.SortedSet rval = new java.util.TreeSet();
	 for (int i = 0; i < k; i++)
	    rval.add(new Integer(smallest+i));
	 return rval;
      }

      java.math.BigInteger c = combinations(n-1, k-1);
      if (val.compareTo(c) >= 0)
	 return getCombination(val.subtract(c), n-1, k, smallest+1);
      else {
	 java.util.SortedSet s = getCombination(val, n-1, k-1, smallest+1);
	 s.add(new Integer(smallest));
	 return s;
      }
   }

   public static java.math.BigInteger decodeCombination(java.util.Set c,
							int n) {
      java.math.BigInteger sum = java.math.BigInteger.ZERO;

      for (int i = 0, found = 0; found < c.size() && i < n; i++) {
	 if (c.contains(new Integer(i)))
	    found++;
	 else
	    sum = sum.add(combinations(n-i-1, c.size()-found-1));
      }

      return sum;
   }
   
}
