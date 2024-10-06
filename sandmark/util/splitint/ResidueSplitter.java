package sandmark.util.splitint;

public abstract class ResidueSplitter implements IntSplitter {
   public abstract java.math.BigInteger[] 
      combineRes(java.math.BigInteger parts[]);

   public java.math.BigInteger combine(java.math.BigInteger parts[]) {
      return combineRes(parts)[0];
   }
}
