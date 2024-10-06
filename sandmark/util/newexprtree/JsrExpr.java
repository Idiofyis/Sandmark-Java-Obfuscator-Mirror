package sandmark.util.newexprtree;

/** Represents a jump-to-subroutine operation.
 *  Emits: JSR_W.
 */
public class JsrExpr extends BranchExpr{
   /*package*/ JsrExpr(org.apache.bcel.generic.InstructionHandle target){
      super(target);
   }

   public JsrExpr(Expr target){
      super(target);
   }

   public String toString(){
      return "JsrExpr[]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){

      java.util.ArrayList result = new java.util.ArrayList();
      result.add(new org.apache.bcel.generic.JSR_W(null));
      return result;
   }
}
