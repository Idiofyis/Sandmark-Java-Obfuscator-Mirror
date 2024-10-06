package sandmark.util.newexprtree;

/** Represents a goto operation.
 *  Emits: GOTO_W.
 */
public class GotoExpr extends BranchExpr{
   /*package*/ GotoExpr(org.apache.bcel.generic.InstructionHandle target){
      super(target);
   }

   public GotoExpr(Expr target){
      super(target);
   }

   public String toString(){
      return "GotoExpr[]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){

      java.util.ArrayList result = new java.util.ArrayList();
      result.add(new org.apache.bcel.generic.GOTO_W(null));
      return result;
   }
}

