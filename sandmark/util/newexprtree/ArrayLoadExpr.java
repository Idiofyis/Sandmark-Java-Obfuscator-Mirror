package sandmark.util.newexprtree;

/** Represents loading from an array.
 *  ArrayLoadExpr.getType() returns the array element type.
 *  Emits: AALOAD, BALOAD, CALOAD, DALOAD, FALOAD, IALOAD, LALOAD, SALOAD.
 */
public class ArrayLoadExpr extends ValueExpr{
   private ValueExpr array;
   private ValueExpr index;
   private org.apache.bcel.generic.Type elementType;

   /** Constructs an ArrayLoadExpr.
    *  @param element the element type of this array.
    *  @param _array the array reference.
    *  @param _index the index to load from.
    */
   public ArrayLoadExpr(org.apache.bcel.generic.Type element, 
                         ValueExpr _array, ValueExpr _index){
      super(element);
      elementType = element;
      array = _array;
      index = _index;
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

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){

      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(array.emitBytecode(factory));
      result.addAll(index.emitBytecode(factory));

      if (elementType.equals(org.apache.bcel.generic.Type.BOOLEAN) ||
          elementType.equals(org.apache.bcel.generic.Type.BYTE)){
         result.add(org.apache.bcel.generic.InstructionConstants.BALOAD);
      }else if (elementType.equals(org.apache.bcel.generic.Type.CHAR)){
         result.add(org.apache.bcel.generic.InstructionConstants.CALOAD);
      }else if (elementType.equals(org.apache.bcel.generic.Type.DOUBLE)){
         result.add(org.apache.bcel.generic.InstructionConstants.DALOAD);
      }else if (elementType.equals(org.apache.bcel.generic.Type.FLOAT)){
         result.add(org.apache.bcel.generic.InstructionConstants.FALOAD);
      }else if (elementType.equals(org.apache.bcel.generic.Type.INT)){
         result.add(org.apache.bcel.generic.InstructionConstants.IALOAD);
      }else if (elementType.equals(org.apache.bcel.generic.Type.LONG)){
         result.add(org.apache.bcel.generic.InstructionConstants.LALOAD);
      }else if (elementType.equals(org.apache.bcel.generic.Type.SHORT)){
         result.add(org.apache.bcel.generic.InstructionConstants.SALOAD);
      }else{
         result.add(org.apache.bcel.generic.InstructionConstants.AALOAD);
      }
      return result;
   }

   public String toString(){
      return "ArrayLoadExpr["+array+","+index+","+elementType+"]";
   }
}

