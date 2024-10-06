package sandmark.util.newexprtree;

/** Represents the NO-OP instruction.
 *  Emits NOP.
 */
public class NopExpr extends Expr{
   public NopExpr(){}

   public String toString(){
      return "NopExpr[]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.add(org.apache.bcel.generic.InstructionConstants.NOP);
      return result;
   }   
}
