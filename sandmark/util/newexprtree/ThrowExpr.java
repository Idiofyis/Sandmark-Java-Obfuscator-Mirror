package sandmark.util.newexprtree;

/** Represents throwing an exception.
 *  Emits: ATHROW.
 */
public class ThrowExpr extends Expr{
   private ValueExpr exception;

   /** Constructs a ThrowExpr for the given exception.
    *  @param _exception the exception to throw.
    */
   public ThrowExpr(ValueExpr _exception){
      exception = _exception;
   }

   /** Returns the exception to be thrown.
    */
   public ValueExpr getExceptionValue(){
      return exception;
   }

   public void setExceptionValue(ValueExpr _ex){
      exception = _ex;
   }

   public String toSting(){
      return "ThrowExpr["+exception+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){

      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(exception.emitBytecode(factory));
      result.add(org.apache.bcel.generic.InstructionConstants.ATHROW);
      return result;
   }
}
