package sandmark.util.newexprtree;

/** Represents a return-from-subroutine operation.
 *  Emits: RET
 */
public class RetExpr extends Expr{
   private int index;

   /** Constructs a RetExpr with the given local variable index.
    *  @param _index the local variable index of the ReturnAddress value.
    */
   public RetExpr(int _index){
      index = _index;
   }

   /** Returns the local variable index for this RetExpr.
    */
   public int getIndex(){
      return index;
   }

   public String toString(){
      return "RetExpr["+index+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.add(new org.apache.bcel.generic.RET(index));
      return result;
   }
}
