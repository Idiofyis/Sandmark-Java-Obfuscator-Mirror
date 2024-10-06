package sandmark.util.newexprtree;

/** Represents an arithmetic negation of a number.
 *  Emits INEG, DNEG, FNEG, LNEG.
 */
public class NegateArithmeticExpr extends ArithmeticExpr{
   private ValueExpr value;

   /** Creates a NegateArithmeticExpr for the given type, to be 
    *  applied to the given ValueExpr.
    *  @param type the type of _value (should equal _value.getType()).
    *  @param _value the value to be negated.
    */
   public NegateArithmeticExpr(org.apache.bcel.generic.BasicType type,
                               ValueExpr _value){
      super(type, ArithmeticExpr.NEG);
      value = _value;
   }

   /** Returns the value to be negated.
    */
   public ValueExpr getValue(){
      return value;
   }

   /** Sets the value to be negated.
    */
   public void setValue(ValueExpr _value){
      value = _value;
   }

   public String toString(){
      return "NegateArithmeticExpr["+value+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(value.emitBytecode(factory));
      switch(getType().getType()){
      case T_BOOLEAN:
      case T_CHAR:
      case T_SHORT:
      case T_BYTE:
      case T_INT:
         result.add(org.apache.bcel.generic.InstructionConstants.INEG);
         break;

      case T_FLOAT:
         result.add(org.apache.bcel.generic.InstructionConstants.FNEG);
         break;

      case T_DOUBLE:
         result.add(org.apache.bcel.generic.InstructionConstants.DNEG);
         break;

      case T_LONG:
         result.add(org.apache.bcel.generic.InstructionConstants.LNEG);
         break;
      }
      return result;
   }
}
