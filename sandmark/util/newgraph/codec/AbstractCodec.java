package sandmark.util.newgraph.codec;

class DefaultNodeFactory implements sandmark.util.newgraph.NodeFactory {
   private int num = 0;
   
   public synchronized java.lang.Object createNode() {
      return new java.lang.Integer(num++);
   }
}

abstract class AbstractCodec implements GraphCodec {
   public sandmark.util.newgraph.MutableGraph encodeMutable(java.math.BigInteger value,
							    sandmark.util.newgraph.NodeFactory factory) {
      return new sandmark.util.newgraph.MutableGraph(encode(value, factory));
   }

   public java.math.BigInteger decode(sandmark.util.newgraph.MutableGraph g) 
      throws DecodeFailure {
      return decode(g.graph());
   }

   public sandmark.util.newgraph.Graph encode(java.math.BigInteger value) {
      return encode(value, new DefaultNodeFactory());
   }

   public sandmark.util.newgraph.MutableGraph encodeMutable(java.math.BigInteger value) {
      return encodeMutable(value, new DefaultNodeFactory());
   }

   private void testOne(java.math.BigInteger num,boolean dumpDot) 
      throws DecodeFailure {
      sandmark.util.newgraph.Graph g = encode(num);
      if(dumpDot)
	 sandmark.util.newgraph.Graphs.dotInFile(g,num + ".dot");
      if(!num.equals(decode(g)))
	 throw new Error(num + " failed!");
   }

   public final void test(String[] args) throws Exception {
      if(args.length != 0) {
	 boolean dumpDot = 
	    args.length > 1 && args[1].equals("1") ? true : false;
	 testOne(new java.math.BigInteger(args[0]),dumpDot);
	 return;
      }
      
      for(int i = 0 ; i < 1000 ; i++)
	 testOne(java.math.BigInteger.valueOf(i),false);

      for(int i = 0 ; i < 1000 ; i++) {
	 int test = sandmark.util.Random.getRandom().nextInt();
	 if(test < 0)
	    continue;
	 testOne(java.math.BigInteger.valueOf(test),false);
      }
   }
}
