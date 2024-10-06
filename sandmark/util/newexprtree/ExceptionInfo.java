package sandmark.util.newexprtree;

/** This class takes the place of CodeExceptionGen 
 *  temporarily while the method is in expression tree form.
 *  It is necessary because CodeExceptionGens keep track of
 *  InstructionHandles, which are ignored by expression trees.
 */
public class ExceptionInfo{
   private org.apache.bcel.generic.ObjectType catchType;
   private Expr startExpr, endExpr, handlerExpr;

   /** The range of the 'try' is from the first instruction
    *  of the 'start' Expr to the last instruction of the 'end'
    *  Expr, inclusive. The handler begins at the first instruction
    *  of the 'handler' Expr.
    */
   public ExceptionInfo(org.apache.bcel.generic.ObjectType _catchType,
                        Expr start, Expr end, Expr handler){
      catchType = _catchType;
      startExpr = start;
      endExpr = end;
      handlerExpr = handler;
   }

   public org.apache.bcel.generic.ObjectType getCatchType(){
      return catchType;
   }
   public void setCatchType(org.apache.bcel.generic.ObjectType type){
      catchType = type;
   }

   public Expr getStartPC(){
      return startExpr;
   }
   public void setStartPC(Expr expr){
      startExpr = expr;
   }

   public Expr getEndPC(){
      return endExpr;
   }
   public void setEndPC(Expr expr){
      endExpr = expr;
   }

   public Expr getHandlerPC(){
      return handlerExpr;
   }
   public void setHandlerPC(Expr expr){
      handlerExpr = expr;
   }
}
