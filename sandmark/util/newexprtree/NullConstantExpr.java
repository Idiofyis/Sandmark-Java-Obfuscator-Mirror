package sandmark.util.newexprtree;

/** An Expr that will put the null reference on the stack.
 *  NullConstantExpr.getType() gives Type.NULL.
 *  Emits ACONST_NULL.
 */
public class NullConstantExpr extends ConstantExpr{
   public NullConstantExpr(){
      super(org.apache.bcel.generic.Type.NULL);
   }

   public Object getValue(){
      return null;
   }

   public String toString(){
      return "NullConstantExpr[null]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.add(org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
      return result;
   }
}
