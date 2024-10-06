package sandmark.util.newgraph.codec;

/**  Utility class for generating Catalan numbers.
 *   The Catalan number C_n can be defined as the
 *   number of totally balanced binary sequences of
 *   length 2n.
 *   @author Zach Heidepriem
 *   @see sandmark.util.newgraph.codec.TotallyBalancedBinarySequence
 *   @see sandmark.util.newgraph.codec.PlantedPlaneCubicTree
*/   

public class CatalanNumbers {   
    
    /**@return the minimum n s.t. C_n <= bigInt
     */
    public static int findMinimumNumber(java.math.BigInteger bigInt){
        int ctr = 1;
        bigInt = bigInt.add(java.math.BigInteger.ONE);
        while(getCatalanNumber(ctr++).compareTo(bigInt) < 0);
        return ctr-1;
    }    

    /**@return C_n = (1 / (n+1)) * (2n \choose n)
       @throws IllegalArgumentException if n < 1
     */
    public static java.math.BigInteger getCatalanNumber(long n){
        if(n < 1)
            throw new IllegalArgumentException("Catalan numbers are defined for n > 0");

        java.math.BigInteger b = 
	    sandmark.util.Math.combinations((int)(2*n), (int)n).divide
	    (java.math.BigInteger.valueOf(n + 1));
	return b;
    }   

    /**For testing
     */
    public static void main(String[] args){         
        for(long i = 1; i <= 15; i++)
            System.out.println(i + ":" + findMinimumNumber(java.math.BigInteger.valueOf(i)));
        /*java.math.BigInteger maxVal =  new java.math.BigInteger
            ("9999999999999999999999999999999999");
        for(java.math.BigInteger i = java.math.BigInteger.ONE; 
            i.compareTo(maxVal) < 0;
            i = i.multiply(new java.math.BigInteger("2")))
            System.out.println(i + ":" + findMinimumNumber(i));
        */
    }
}
