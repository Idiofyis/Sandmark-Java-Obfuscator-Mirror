package sandmark.util.newexprtree;

/** Represents an operation that compares two values and 
 *  leaves a boolean result on the stack.
 *  ComparisonExpr.getType() returns Type.INT.
 *  Emits: DCMPL, DCMPG, FCMPL, FCMPG, LCMP.
 */
public class ComparisonExpr extends ValueExpr{
   private short code;
   private ValueExpr left, right;

   /** Constructs a ComparisonExpr with the given operands, and the comparison type.
    *  @param _left the 'left' or first operand.
    *  @param _right the 'right' or second operand.
    *  @param _code the type of comparison to do 
    *   (one of Constants.DCMPL, Constants.DCMPG, Constants.FCMPL, Constants.FCMPG, Constants.LCMP).
    */
   public ComparisonExpr(ValueExpr _left, ValueExpr _right, short _code){
      super(org.apache.bcel.generic.Type.INT);
      left = _left;
      right = _right;
      code = _code;
      if (!(code==DCMPL || code==DCMPG ||
            code==FCMPL || code==FCMPG ||
            code==LCMP))
         throw new RuntimeException("Bad code value: "+code);
   }

   /** Returns the 'left' or first operand.
    */
   public ValueExpr getLeftValue(){
      return left;
   }

   /** Sets the left value
    */
   public void setLeftValue(ValueExpr _left){
      left = _left;
   }

   /** Returns the 'right' or second operand.
    */
   public ValueExpr getRightValue(){
      return right;
   }

   /** Sets the right value
    */
   public void setRightValue(ValueExpr _right){
      right = _right;
   }

   /** Returns the comparison type code value.
    */
   public short getCode(){
      return code;
   }

   public String toString(){
      return "ComparisonExpr["+left+","+right+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(left.emitBytecode(factory));
      result.addAll(right.emitBytecode(factory));

      switch(code){
      case DCMPL:
         result.add(org.apache.bcel.generic.InstructionConstants.DCMPL);
         break;
      case DCMPG:
         result.add(org.apache.bcel.generic.InstructionConstants.DCMPG);
         break;
      case FCMPL:
         result.add(org.apache.bcel.generic.InstructionConstants.FCMPL);
         break;
      case FCMPG:
         result.add(org.apache.bcel.generic.InstructionConstants.FCMPG);
         break;
      case LCMP:
         result.add(org.apache.bcel.generic.InstructionConstants.LCMP);
         break;
      }
      return result;
   }
}
