package sandmark.util.newexprtree;

/** Represents a local variable integer increment operaion.
 *  Emits: IINC.
 */
public class IncExpr extends Expr{
   private int index;
   private int increment;
   
   /** Constructs an IncExpr with the given local variable 
    *  index and increment value.
    *  @param _index the local variable index of the integer.
    *  @param _increment the value to increment it by (must be at most a 16-bit quantity).
    */
   public IncExpr(int _index, int _increment){
      index = _index;
      increment = _increment;
      if (increment>Short.MAX_VALUE || increment<Short.MIN_VALUE)
         throw new RuntimeException("Increment value too large");
   }

   /** Returns the local variable index to increment.
    */
   public int getIndex(){
      return index;
   }

   /** Returns the value to increment by.
    */
   public int getIncrement(){
      return increment;
   }

   public String toString(){
      return "IncExpr["+index+","+increment+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.add(new org.apache.bcel.generic.IINC(index, increment));
      return result;
   }
}
