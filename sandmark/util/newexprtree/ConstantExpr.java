package sandmark.util.newexprtree;

/** Represents an instruction that will push a constant onto
 *  the stack (i.e. an int, float, double, long, String, or null).
 *  Constant expressions have no children expressions.
 */
public abstract class ConstantExpr extends ValueExpr{
   public ConstantExpr(org.apache.bcel.generic.Type type){
      super(type);
   }
   public abstract Object getValue();
}
