package sandmark.util.newexprtree;

/** Represents a dummy value of a primitive type (int, short, double, etc).
 *  PrimitiveDummyExpr.getType() will return the given BasicType.
 */
public class PrimitiveDummyExpr extends DummyExpr{
   public PrimitiveDummyExpr(org.apache.bcel.generic.BasicType _type){
      super(_type);
   }

   public String toString(){
      return "PrimitiveDummyExpr["+getType()+"]";
   }
}
