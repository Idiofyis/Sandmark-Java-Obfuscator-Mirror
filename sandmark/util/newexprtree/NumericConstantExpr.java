package sandmark.util.newexprtree;

/** Represents pushing a numeric constant onto the stack.
 *  One of {int,float,double,long}.
 *  NumericConstantExpr.getType() returns the type of the constant.
 *  Emits BIPUSH, SIPUSH, ICONST*, FCONST*, DCONST*, LCONST*, LDC*, LDC2_W
 */
public class NumericConstantExpr extends ConstantExpr{
   private Number value;

   public NumericConstantExpr(org.apache.bcel.generic.BasicType _type,
                              Number _value){
      super(_type);
      value = _value;
   }

   public Object getValue(){
      return value;
   }

   public Number getNumericValue(){
      return value;
   }

   public String toString(){
      return "NumericConstantExpr["+getType()+","+value+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.add(factory.createConstant(value));
      return result;
   }
}
