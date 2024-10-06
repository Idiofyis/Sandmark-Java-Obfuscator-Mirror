package sandmark.util.newexprtree;

/** Represents loading a field of a class.
 *  GetFieldExpr.getType() returns the field type.
 *  Emits: GETFIELD, GETSTATIC.
 */
public class GetFieldExpr extends ValueExpr{
   private String classname;
   private String fieldname;
   private org.apache.bcel.generic.Type fieldtype;
   private ValueExpr ref;

   /** Constructs a GetFieldExpr for a non-static field.
    *  @param _classname the name of the class that owns this field.
    *  @param _fieldname the name of the field.
    *  @param _fieldtype the type of the field.
    *  @param _ref the instance of the class that owns this field.
    */
   public GetFieldExpr(String _classname, String _fieldname,
                       org.apache.bcel.generic.Type _fieldtype,
                       ValueExpr _ref){
      super(_fieldtype);
      classname = _classname;
      fieldname = _fieldname;
      fieldtype = _fieldtype;
      ref = _ref;
   }
 
   /** Constructs a GetFieldExpr for a static field.
    *  @param _classname the name of the class that owns this field.
    *  @param _fieldname the name of the field.
    *  @param _fieldtype the type of the field.
    */
   public GetFieldExpr(String _classname, String _fieldname,
                       org.apache.bcel.generic.Type _fieldtype){
      this(_classname, _fieldname, _fieldtype, null);
   }

   /** Returns the name of the class that owns this field.
    */
   public String getClassName(){
      return classname;
   }

   /** Returns the name of the field.
    */
   public String getFieldName(){
      return fieldname;
   }
   
   /** Returns the instance of the class that owns this field.
    *  If this is a static field, this method returns null.
    */
   public ValueExpr getOwnerValue(){
      return ref;
   }

   /** Sets the owner reference for this field
    */
   public void setOwnerValue(ValueExpr owner){
      ref = owner;
   }

   public String toString(){
      String result = "GetField["+classname+"."+fieldname+","+fieldtype;
      if (ref!=null)
         result+=","+ref;
      return result+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){

      java.util.ArrayList result = new java.util.ArrayList();
      if (ref!=null)
         result.addAll(ref.emitBytecode(factory));
      if (ref==null)
         result.add(factory.createGetStatic(classname, fieldname, fieldtype));
      else
         result.add(factory.createGetField(classname, fieldname, fieldtype));
      return result;
   }
}
