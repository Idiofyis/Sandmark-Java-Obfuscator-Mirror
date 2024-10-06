package sandmark.util.newexprtree;

/** Represents storing a value into a field of a class.
 *  PutField.getType() returns the type of this field.
 *  Emits PUTFIELD, PUTSTATIC.
 */
public class PutFieldExpr extends Expr{
   private String classname;
   private String fieldname;
   private org.apache.bcel.generic.Type fieldtype;
   private ValueExpr ref, value;

   /** Constructs a PutFieldExpr for a non-static field.
    *  @param _classname the name of the class that owns this field.
    *  @param _fieldname the name of the field.
    *  @param _fieldtype the type of the field.
    *  @param _value the value to store into this field.
    *  @param _ref the class instance whose field gets changed.
    */
   public PutFieldExpr(String _classname, String _fieldname, 
                       org.apache.bcel.generic.Type _fieldtype,
                       ValueExpr _value, ValueExpr _ref){
      classname = _classname;
      fieldname = _fieldname;
      fieldtype = _fieldtype;
      value = _value;
      ref = _ref;
   }

   /** Constructs a PutFieldExpr for static fields.
    *  @param _classname the name of the class that owns this field.
    *  @param _fieldname the name of the field.
    *  @param _fieldtype the type of the field.
    *  @param _value the value to store into this field.
    */
   public PutFieldExpr(String _classname, String _fieldname,
                       org.apache.bcel.generic.Type _fieldtype,
                       ValueExpr _value){
      this(_classname, _fieldname, _fieldtype, _value, null);
   }

   /** Returns the name of the class owning this field.
    */
   public String getClassName(){
      return classname;
   }
   
   /** Returns the name of the field.
    */
   public String getFieldName(){
      return fieldname;
   }

   /** Returns the type of the field (equal to getType()).
    */
   public org.apache.bcel.generic.Type getFieldType(){
      return fieldtype;
   }

   /** Returns the class instance that owns the field.
    *  If it is a static field, this returns null.
    */
   public ValueExpr getOwnerValue(){
      return ref;
   }

   /** Sets the reference on which this putfield works.
    *  Null implies static field.
    */
   public void setOwnerValue(ValueExpr owner){
      ref = owner;
   }

   /** Returns the value to be assigned to the field.
    */
   public ValueExpr getFieldValue(){
      return value;
   }

   /** Sets the value to be assigned to the field.
    */
   public void setFieldValue(ValueExpr _value){
      if (_value==null)
         throw new IllegalArgumentException("Value must be nonnull");
      value = _value;
   }

   public String toString(){
      String result = "PutField["+classname+"."+fieldname+","+fieldtype;
      if (ref!=null)
         result+=","+ref;
      return result+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      if (ref!=null)
         result.addAll(ref.emitBytecode(factory));
      result.addAll(value.emitBytecode(factory));
      if (ref==null)
         result.add(factory.createPutStatic(classname, fieldname, fieldtype));
      else
         result.add(factory.createPutField(classname, fieldname, fieldtype));
      return result;
   }
}
