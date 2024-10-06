package sandmark.util.newexprtree;

/** DummyExpr is the abstract parent of all dummy expressions.
 *  Dummy expressions correspond to no bytecode instructions. 
 *  They are used as placeholders for stack operands that were
 *  not produced in the same basic block in which they were consumed.
 *  The emitBytecode method of any DummyExpr will give an empty ArrayList.
 */
public abstract class DummyExpr extends ValueExpr{
   public DummyExpr(org.apache.bcel.generic.Type type){
      super(type);
   }

   public final java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      return new java.util.ArrayList();
   }
}
