package sandmark.util.newexprtree;

/** Represents a load from a local variable.
 *  LoadExpr.getType() returns the type of the local variable.
 *  Emits: ALOAD*, ILOAD*, DLOAD*, FLOAD*, LLOAD*.
 */
public class LoadExpr extends ValueExpr{
   private int index;

   /** Constructs a LoadExpr with the given type and local variable index.
    *  @param _type the type of the local.
    *  @param _index the local variable index.
    */
   public LoadExpr(org.apache.bcel.generic.Type _type,
                   int _index){
      super(_type);
      index = _index;
   }

   /** Returns the local variable index of this operation.
    */
   public int getIndex(){
      return index;
   }

   public String toString(){
      return "LoadExpr["+getType()+","+index+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.add(factory.createLoad(mapType(getType()), index));
      return result;
   }   

   private static org.apache.bcel.generic.Type mapType
      (org.apache.bcel.generic.Type type){
      if (type instanceof org.apache.bcel.generic.BasicType)
         return type;
      return org.apache.bcel.generic.Type.OBJECT;
   }
}
