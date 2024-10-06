package sandmark.util.newgraph.codec;

public class PermutationGraph extends CycleAndDigitsCodec 
   implements TypedEdgeCodec {

   protected int cycleLength(java.math.BigInteger value) {
      java.math.BigInteger nfact = java.math.BigInteger.ONE;
      int n = 1;
      do {
	 n++;
	 java.math.BigInteger N = java.math.BigInteger.valueOf(n);
	 nfact = nfact.multiply(N);
      } while (nfact.compareTo(value)<=0);
      return n;
   }

   // encode perIndex into the corresponding permutation
   // PRE2: 0 <= perIndex < factorial(perLength)
   // POST1: perIndex = 0
   protected int [] digits(java.math.BigInteger value,int cycleLength) {
      int digits[] = new int[cycleLength];
      for(int i = 0 ; i < cycleLength ; i++) 
	 digits[i] = i;

      for(int r = 2 ; r <= cycleLength ; r++) {
	 java.math.BigInteger R = java.math.BigInteger.valueOf(r);
	 java.math.BigInteger[] DR = value.divideAndRemainder(R);
	 java.math.BigInteger D = DR[0];
	 java.math.BigInteger S = DR[1];
	 int s = S.intValue();
	 value = D;           // we're reducing perIndex to 0
	 swap(digits,r - 1,s);
      }

      if(value.compareTo(java.math.BigInteger.ZERO) != 0)
	 throw new Error("Postcondition 1 of index2perm() is violated! " +
			 "perIndex=" + value);

      return digits;
   }

   protected java.math.BigInteger decode(int[] digits,int cycleLength) {
      java.math.BigInteger perIndex=java.math.BigInteger.valueOf(0);
      int f = 0;
      for(int r = cycleLength ; r >= 2 ; r--) {
	 for(int s = 0 ; s < r ; s++) {
	    if(digits[s] == r - 1) {
	       f = s;
	       break;
	    }
	 }
	 int t = digits[r - 1];
	 digits[r - 1] = digits[f];
	 digits[f] = t;
	 java.math.BigInteger F = java.math.BigInteger.valueOf(f);
	 java.math.BigInteger R = java.math.BigInteger.valueOf(r);
	 perIndex = F.add(R.multiply(perIndex));
      }
      return perIndex;
   }

   private static void swap(int perm[],int i,int j) {
      int tmp = perm[i];
      perm[i] = perm[j];
      perm[j] = tmp;
   }

   public static void main(String argv[]) throws Exception {
      new PermutationGraph().test(argv);
   }
}




