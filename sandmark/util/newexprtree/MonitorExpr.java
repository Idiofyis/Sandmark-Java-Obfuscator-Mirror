package sandmark.util.newexprtree;

/** This class represents an operation involving monitors.
 *  Emits: MONITORENTER, MONITOREXIT.
 */
public class MonitorExpr extends Expr{
   private boolean entering;
   private ValueExpr ref;

   /** Constructs a MonitorExpr with the given reference to lock/unlock
    *  and a flag value specifying whether to lock or unlock that reference.
    *  @param _ref the reference to lock/unlock.
    *  @param _enter true iff the reference should be locked, false iff it should be unlocked.
    */
   public MonitorExpr(ValueExpr _ref, boolean _enter){
      ref = _ref;
      entering = _enter;
   }

   /** Returns the reference to be locked/unlocked.
    */
   public ValueExpr getValue(){
      return ref;
   }

   /** Sets the reference to be locked/unlocked.
    */
   public void setValue(ValueExpr lock){
      ref = lock;
   }

   /** Returns true iff this Expr is going to lock the reference.
    */
   public boolean isEntering(){
      return entering;
   }

   public String toString(){
      return "MonitorExpr["+entering+","+ref+"]";
   }
   
   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(ref.emitBytecode(factory));
      result.add(entering ? 
                 org.apache.bcel.generic.InstructionConstants.MONITORENTER : 
                 org.apache.bcel.generic.InstructionConstants.MONITOREXIT);
      return result;
   }
}
