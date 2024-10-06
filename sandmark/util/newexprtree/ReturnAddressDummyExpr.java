package sandmark.util.newexprtree;

/** Represents a ReturnAddress value on the stack, as from a JSR
 *  instruction. 
 *  ReturnAddressDummyExpr.getType() return ReturnaddressType.NO_TARGET.
 */
public class ReturnAddressDummyExpr extends DummyExpr{
   public ReturnAddressDummyExpr(){
      super(org.apache.bcel.generic.ReturnaddressType.NO_TARGET);
   }

   public String toString(){
      return "ReturnAddressDummyExpr[]";
   }
}
