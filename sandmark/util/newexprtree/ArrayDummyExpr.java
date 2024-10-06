package sandmark.util.newexprtree;

/** Represents a dummy expression for an array reference.
 *  This is implicitly a single-dimensional array, but can be
 *  made multi-dimensional by making the element type be an arraytype
 *  as well.
 */
public class ArrayDummyExpr extends ObjectDummyExpr{
   private org.apache.bcel.generic.Type elementType;
   public ArrayDummyExpr(org.apache.bcel.generic.Type element){
      super(new org.apache.bcel.generic.ArrayType(element, 1));
      elementType = element;
   }

   public ArrayDummyExpr(){
      super(org.apache.bcel.generic.Type.OBJECT);
      elementType = null;
   }

   /** Returns the element type of this array. If this
    *  is meant to represent a multi-dimensional array,
    *  then this will be an ArrayType.
    */
   public org.apache.bcel.generic.Type getElementType(){
      return elementType;
   }

   public String toString(){
      return "ArrayDummyExpr["+elementType+"]";
   }
}
