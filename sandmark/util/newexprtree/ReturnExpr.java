package sandmark.util.newexprtree;

/** Represents a return-from-method instruction, possibly with a return value.
 *  Emits RETURN, ARETURN, IRETURN, DRETURN, FRETURN, LRETURN.
 */
public class ReturnExpr extends Expr{
   private ValueExpr value;
   private org.apache.bcel.generic.Type type;

   /** Represents a return operation with a return value (non-void).
    *  @param _value the return value.
    *  @param _type the return type (should equal _value.getType()).
    */
   public ReturnExpr(ValueExpr _value,
                     org.apache.bcel.generic.Type _type){
      value = _value;
      type = _type;
   }

   /** Represents a void return.
    */
   public ReturnExpr(){
      type = org.apache.bcel.generic.Type.VOID;
   }

   /** Returns the return value of this operation.
    *  If this is a void return, it will be null.
    */
   public ValueExpr getReturnValue(){
      return value;
   }
   
   /** Sets the value being returned by this return statement.
    */
   public void setReturnValue(ValueExpr _returnValue){
      if (isVoid())
         throw new IllegalArgumentException("Void return cannot have a return value");
      else if (_returnValue==null)
         throw new IllegalArgumentException("Nonvoid return must have non-null value");
      value = _returnValue;
   }

   /** Returns true iff this is a void return.
    */
   public boolean isVoid(){
      return (type.equals(org.apache.bcel.generic.Type.VOID));
   }

   public String toString(){
      return "ReturnExpr["+(value==null ? "]" : value+"]");
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      if (value!=null)
         result.addAll(value.emitBytecode(factory));
      if (type!=null)
         result.add(factory.createReturn(type));
      else
         result.add(org.apache.bcel.generic.InstructionConstants.RETURN);
      return result;
   }
}
