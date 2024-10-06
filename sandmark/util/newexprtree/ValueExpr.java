package sandmark.util.newexprtree;

/** ValueExpr is the abstract parent of all Exprs that can put values onto 
 *  the stack. Only a ValueExpr can be the child of another Expr. Every ValueExpr
 *  can tell you what type of value it will put on the stack.
 */
public abstract class ValueExpr extends Expr{
   private org.apache.bcel.generic.Type type;

   public ValueExpr(org.apache.bcel.generic.Type _type){
      type = _type;
   }

   public org.apache.bcel.generic.Type getType(){
      return type;
   }
}
