package sandmark.util.newexprtree;

/** Represents a dummy value of an exception on the stack.
 *  Among other things, this type of dummy expression is assumed
 *  to be on the stack at the start of an exception handler block.
 */
public class ExceptionDummyExpr extends ObjectDummyExpr{
   public ExceptionDummyExpr(){
      super((org.apache.bcel.generic.ReferenceType)
            org.apache.bcel.generic.Type.getType("Ljava/lang/Throwable;"));
   }

   public ExceptionDummyExpr(org.apache.bcel.generic.ObjectType type){
      super(type);
   }

   public String toString(){
      return "ExceptionDummyExpr["+getType()+"]";
   }
}
