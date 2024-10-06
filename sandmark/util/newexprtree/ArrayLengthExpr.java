package sandmark.util.newexprtree;

/** Represents an operation to get the length of an array.
 *  ArraylengthExpr.getType() gives Type.INT.
 *  Emits: ARRAYLENGTH.
 */
public class ArrayLengthExpr extends ValueExpr{
   private ValueExpr array;

   /** Constructs an ArrayLengthExpr for the given array.
    *  @param _array the array whose length you want to find.
    */
   public ArrayLengthExpr(ValueExpr _array){
      super(org.apache.bcel.generic.Type.INT);
      array = _array;
   }

   /** Returns the array to measure.
    */
   public ValueExpr getArrayValue(){
      return array;
   }

   public void setArrayValue(ValueExpr _array){
      array = _array;
   }

   public String toString(){
      return "ArrayLengthExpr["+array+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){

      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(array.emitBytecode(factory));
      result.add(org.apache.bcel.generic.InstructionConstants.ARRAYLENGTH);
      return result;
   }
}
