package sandmark.util.splitint;

public interface IntSplitter {
   public abstract java.math.BigInteger[] split(java.math.BigInteger value);
   public abstract java.math.BigInteger combine(java.math.BigInteger parts[]);
   public abstract boolean orderMatters();
}
