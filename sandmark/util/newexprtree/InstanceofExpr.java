package sandmark.util.newexprtree;

/** Performs the instanceof test on a reference.
 *  InstanceofExpr.getType() return Type.INT.
 *  Emits: INSTANCEOF.
 */
public class InstanceofExpr extends ValueExpr{
   private ValueExpr ref;
   private org.apache.bcel.generic.ReferenceType totype;

   /** Constructs an InstanceofExpr with the given value to test and type.
    *  @param _ref the reference value to test.
    *  @param type the target type.
    */
   public InstanceofExpr(ValueExpr _ref, 
                         org.apache.bcel.generic.ReferenceType type){
      super(org.apache.bcel.generic.Type.INT);
      ref = _ref;
      totype = type;
   }

   /** Returns the reference to be tested.
    */
   public ValueExpr getTestValue(){
      return ref;
   }

   /** Sets the value to test.
    */
   public void setTestValue(ValueExpr test){
      ref = test;
   }

   /** Returns the target type of the INSTANCEOF.
    */
   public org.apache.bcel.generic.ReferenceType getTestType(){
      return totype;
   }

   public String toString(){
      return "InstanceofExpr["+totype+","+ref+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(ref.emitBytecode(factory));
      result.add(factory.createInstanceOf(totype));
      return result;
   }
}
