package sandmark.util.newexprtree;

/** Represents the creation of a new non-array reference type.
 *  NewExpr.getType() returns the type of the new object.
 *  Emits: NEW
 */
public class NewExpr extends ValueExpr{
   /** Constructs a NewExpr for the given ObjectType.
    */
   public NewExpr(org.apache.bcel.generic.ObjectType type){
      super(type);
   }

   public String toString(){
      return "NewExpr["+getType()+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.add(factory.createNew
                 ((org.apache.bcel.generic.ObjectType)getType()));
      return result;
   }
}
