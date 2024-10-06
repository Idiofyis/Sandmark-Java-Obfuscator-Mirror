package sandmark.util.newexprtree;

/** Represents pushing a String literal constant onto the stack.
 *  StringConstantExpr.getType() return Type.STRING.
 *  Emits LDC.
 */
public class StringConstantExpr extends ConstantExpr{
   private String value;

   public StringConstantExpr(String _value){
      super(org.apache.bcel.generic.Type.STRING);
      value = _value;
   }

   public Object getValue(){
      return value;
   }

   public String toString(){
      return "StringConstantExpr[\""+value+"\"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.add(factory.createConstant(value));
      return result;
   }
}
