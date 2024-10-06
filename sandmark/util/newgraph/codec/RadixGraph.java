package sandmark.util.newgraph.codec;

public class RadixGraph extends CycleAndDigitsCodec 
    implements TypedEdgeCodec {   
   protected java.math.BigInteger decode(int digits[],int cycleLength) {
      java.math.BigInteger r = java.math.BigInteger.valueOf(cycleLength);
      java.math.BigInteger v = java.math.BigInteger.ZERO;
      for(int i = 0 ; i < cycleLength ; i++) {
	 v = v.multiply(r);
	 v = v.add(java.math.BigInteger.valueOf(digits[i]));
      }
      return v;
   }

   protected int [] digits(java.math.BigInteger value,int radix)  {
      int digits[] = new int[radix];
      java.math.BigInteger R = java.math.BigInteger.valueOf(radix);
      int slot;
      for(slot = digits.length - 1 ; 
	  value.compareTo(java.math.BigInteger.ZERO) > 0 ; slot--) {
	 java.math.BigInteger[] DR = value.divideAndRemainder(R);
	 java.math.BigInteger D = DR[0];
	 java.math.BigInteger M = DR[1];
	 digits[slot] = M.intValue();
	 value = D;
      };
      for(int i = 0 ; i <= slot ; i++)
	 digits[i] = 0;
      return digits;
   }

   protected int cycleLength(java.math.BigInteger value)  {
      int k;
      for(k = 2 ; ; k++) {
	 java.math.BigInteger bi = java.math.BigInteger.valueOf(k).pow(k);
	 if(bi.compareTo(value) > 0)
	    break;
      } 
      return k;
   }
   
   public static void main(String argv[]) throws Exception {
      new RadixGraph().test(argv);
   }
}

