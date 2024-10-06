package sandmark.util.newexprtree;

/** Represents a store of a value to a local variable.
 *  Emits ASTORE*, ISTORE*, DSTORE*, FSTORE*, LSTORE*.
 */  
public class StoreExpr extends Expr{
   private org.apache.bcel.generic.Type type;
   private int index;
   private ValueExpr value;

   /** Constructs a StoreExpr with the given register type,
    *  local variable index, and value to store.
    *  @param _type the type of the value being stored.
    *  @param _index the local variable index.
    *  @param _vlue the value to be stored.
    */
   public StoreExpr(org.apache.bcel.generic.Type _type,
                    int _index, ValueExpr _value){
      type = _type;
      index = _index;
      value = _value;
   }

   /** Returns the local variable index to store to.
    */
   public int getIndex(){
      return index;
   }

   /** Returns the type of the value being stored.
    */
   public org.apache.bcel.generic.Type getLocalType(){
      return type;
   }

   /** Returns the ValueExpr being stored.
    */
   public ValueExpr getStoreValue(){
      return value;
   }

   /** Sets the expression to be stored.
    */
   public void setStoreValue(ValueExpr _value){
      value = _value;
   }

   public String toString(){
      return "StoreExpr["+type+","+index+","+value+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(value.emitBytecode(factory));
      result.add(factory.createStore(mapType(type), index));
      return result;
   }   

   /** This is a helper method for use with 
    *  InstructionFactory.createStore(), cuz it's dumb.
    */
   private static org.apache.bcel.generic.Type mapType
      (org.apache.bcel.generic.Type type){
      if (type instanceof org.apache.bcel.generic.BasicType)
         return type;
      return org.apache.bcel.generic.Type.OBJECT;
   }
}
