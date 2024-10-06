package sandmark.util.newgraph;

public class TypedEdge extends EdgeImpl {
   private int mType;
   public TypedEdge(Object from,Object to,int type) {
      super(from,to);
      mType = type;
   }
   public int getType() { return mType; }
   public Edge clone(Object source,Object sink) {
      return new TypedEdge(source,sink,mType);
   }
}
