package sandmark.util.newexprtree;

/** Represents storing an element in an array.
 *  Emits: AASTORE, BASTORE, CASTORE, DASTORE, FASTORE, IASTORE, LASTORE, SASTORE.
 */
public class ArrayStoreExpr extends Expr{
   private ValueExpr array;
   private ValueExpr index;
   private ValueExpr value;
   private org.apache.bcel.generic.Type elementType;

   /** Constructs an ArrayStoreExpr.
    *  @param element the element type of this array (and the type of the value to be stored in it).
    *  @param _array the array to store into.
    *  @param _index the index value to store into.
    *  @param _value the value to store.
    */
   public ArrayStoreExpr(org.apache.bcel.generic.Type element, 
                         ValueExpr _array, ValueExpr _index, ValueExpr _value){
      elementType = element;
      array = _array;
      index = _index;
      value = _value;
   }

   public org.apache.bcel.generic.Type getElementType(){
      return elementType;
   }
   
   public ValueExpr getArrayValue(){
      return array;
   }

   public void setArrayValue(ValueExpr _array){
      array = _array;
   }

   public ValueExpr getIndexValue(){
      return index;
   }

   public void setIndexValue(ValueExpr _index){
      index = _index;
   }

   public ValueExpr getStoreValue(){
      return value;
   }

   public void setStoreValue(ValueExpr store){
      value = store;
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){

      java.util.ArrayList result = new java.util.ArrayList();

      result.addAll(array.emitBytecode(factory));
      result.addAll(index.emitBytecode(factory));
      result.addAll(value.emitBytecode(factory));

      if (elementType.equals(org.apache.bcel.generic.Type.BOOLEAN) ||
          elementType.equals(org.apache.bcel.generic.Type.BYTE)){
         result.add(org.apache.bcel.generic.InstructionConstants.BASTORE);
      }else if (elementType.equals(org.apache.bcel.generic.Type.CHAR)){
         result.add(org.apache.bcel.generic.InstructionConstants.CASTORE);
      }else if (elementType.equals(org.apache.bcel.generic.Type.DOUBLE)){
         result.add(org.apache.bcel.generic.InstructionConstants.DASTORE);
      }else if (elementType.equals(org.apache.bcel.generic.Type.FLOAT)){
         result.add(org.apache.bcel.generic.InstructionConstants.FASTORE);
      }else if (elementType.equals(org.apache.bcel.generic.Type.INT)){
         result.add(org.apache.bcel.generic.InstructionConstants.IASTORE);
      }else if (elementType.equals(org.apache.bcel.generic.Type.LONG)){
         result.add(org.apache.bcel.generic.InstructionConstants.LASTORE);
      }else if (elementType.equals(org.apache.bcel.generic.Type.SHORT)){
         result.add(org.apache.bcel.generic.InstructionConstants.SASTORE);
      }else{
         result.add(org.apache.bcel.generic.InstructionConstants.AASTORE);
      }
      return result;
   }

   public String toString(){
      return "ArrayStoreExpr["+array+","+index+","+value+","+elementType+"]";
   }
}
