package sandmark.util.newgraph.codec;

public interface GraphCodec {
   abstract public sandmark.util.newgraph.Graph encode(java.math.BigInteger value,
						       sandmark.util.newgraph.NodeFactory factory);
   abstract public java.math.BigInteger decode(sandmark.util.newgraph.Graph graph)
      throws DecodeFailure;

   abstract public sandmark.util.newgraph.MutableGraph encodeMutable(java.math.BigInteger value,
								     sandmark.util.newgraph.NodeFactory f);
   abstract public java.math.BigInteger decode(sandmark.util.newgraph.MutableGraph graph)
      throws DecodeFailure;

   abstract public sandmark.util.newgraph.Graph encode(java.math.BigInteger value);
   abstract public sandmark.util.newgraph.MutableGraph encodeMutable(java.math.BigInteger value);
   
   public abstract int maxOutDegree();
}
