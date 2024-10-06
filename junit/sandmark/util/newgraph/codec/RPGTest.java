package sandmark.util.newgraph.codec;

public class RPGTest extends junit.framework.TestCase {
   public void testEncodeDecode() {
      GraphCodec rpg = new ReduciblePermutationGraph();
      for (int n = 0; n < 20000; n++) {
	 java.math.BigInteger value = java.math.BigInteger.valueOf(n);
	 sandmark.util.newgraph.Graph g = rpg.encode(value);
	 try {
	    java.math.BigInteger newValue = rpg.decode(g);
	    assertEquals(value, newValue);
	 }
	 catch (DecodeFailure df) {
	    df.printStackTrace();
	    fail("n = " + n);
	 }
      }
   }

   public static void main(String [] argv) {
      RPGTest t = new RPGTest();
      t.testEncodeDecode();
   }
}
