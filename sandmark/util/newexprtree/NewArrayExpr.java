package sandmark.util.newexprtree;

/** Represents the creation of a new array type.
 *  This can be an array of basic types, an array of 
 *  reference types, or a multidimensional array of any type.
 *  NewArrayExpr.getType() returns the full type of the created array.
 *  Emits NEWARRAY, ANEWARRAY, MULTIANEWARRAY.
 */
public class NewArrayExpr extends ValueExpr{
   private ValueExpr[] counts;
   
   /** Constructs a NewArrayExpr with the given type and list of 
    *  dimension values. 
    *  @param _type the total array type of the new array to be made (NOT THE ELEMENT TYPE!)
    *  @param _counts an array of ValueExprs that represent the size of each dimension. 
    *        The number of dimensions of this array will be taken to be _counts.length.
    */
   public NewArrayExpr(org.apache.bcel.generic.ArrayType _type,
                       ValueExpr[] _counts){
      super(_type);
      counts = _counts;
   }

   /** Returns the array of dimension sizes.
    */
   public ValueExpr[] getCounts(){
      return counts;
   }

   /** Sets the list of sizes for this array instruction.
    */
   public void setCounts(ValueExpr[] _counts){
      if (_counts==null || _counts.length==0)
         throw new IllegalArgumentException("Size list must be non-empty");
      counts=_counts;
   }

   public String toString(){
      String result = "NewArrayString["+getType();
      for (int i=0;i<counts.length;i++)
         result += ","+counts[i];
      return result+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      for (int i=0;i<counts.length;i++)
         result.addAll(counts[i].emitBytecode(factory));

      org.apache.bcel.generic.ArrayType atype = 
         (org.apache.bcel.generic.ArrayType)getType();
      for (int i=0;i<counts.length-1;i++){
         atype = (org.apache.bcel.generic.ArrayType)atype.getElementType();
      }
      result.add(factory.createNewArray(atype.getElementType(), (short)counts.length));
      return result;
   }
}

