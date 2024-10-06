package sandmark.util.newexprtree;

/** Represents a method call operaton. 
 *  InvokeExpr.getType() returns the return type of this method.
 *  NOTE: technically, void methods should not be ValueExprs since they
 *     do not put values onto the stack. this means that you could potentially
 *     have a void method be the operand of some other Expr. don't do that.
 *  Emits: INVOKESPECIAL, INVOKEINTERFACE, INVOKESTATIC, INVOKEVIRTUAL.
 */
public class InvokeExpr extends ValueExpr{
   private String classname, methodname, methodsig;
   private ValueExpr[] args;
   private ValueExpr ref;
   private short code;

   /** Constructs an InvokeExpr with the given classname, methodname, method signature,
    *  argument values, target reference, and invoke type. This constructor is not for use
    *  with static method calls.
    *  @param _classname the name of the class owning the method to call.
    *  @param _methodname the name of the method to call.
    *  @param _methodsig the method signature.
    *  @param _args the argument values to pass.
    *  @param _ref the reference to call the method on.
    *  @param _code one of Constants.INVOKESPECIAL, Constants.INVOKEINTERFACE, Constants.INVOKEVIRTUAL.
    */
   public InvokeExpr(String _classname, String _methodname,
                     String _methodsig, ValueExpr[] _args,
                     ValueExpr _ref, short _code){
      super(org.apache.bcel.generic.Type.getReturnType(_methodsig));
      classname = _classname;
      methodname = _methodname;
      methodsig = _methodsig;
      args = _args;
      ref = _ref;
      code = _code;

      if (!(code==INVOKEVIRTUAL || code==INVOKESTATIC ||
            code==INVOKESPECIAL || code==INVOKEINTERFACE))
         throw new RuntimeException("Bad code value: "+code);
   }

   /** Constructs an InvokeExpr that represents a static method call.
    *  @param _classname the name of the class owning the method to call.
    *  @param _methodname the name of the method to call.
    *  @param _methodsig the method signature.
    *  @param _args the argument values to pass.
    */
   public InvokeExpr(String _classname, String _methodname,
                     String _methodsig, ValueExpr[] _args){
      this(_classname, _methodname, _methodsig, 
           _args, null, INVOKESTATIC);
   }

   /** Returns the name of the class that owns the method to call.
    */
   public String getClassName(){
      return classname;
   }

   /** Returns the name of the method to call.
    */
   public String getMethodName(){
      return methodname;
   }

   /** Returns the reference on which this method is called.
    *  If this is a static method, returns null.
    */
   public ValueExpr getReferenceValue(){
      return ref;
   }

   /** Sets the reference to be invoked on.
    */
   public void setReferenceValue(ValueExpr _ref){
      ref = _ref;
   }

   /** Returns the signature of the method to call.
    */
   public String getSignature(){
      return methodsig;
   }

   /** Returns the argument values to pass tothe method call.
    */
   public ValueExpr[] getArgumentValues(){
      return args;
   }

   public void setArgumentValues(ValueExpr[] _args){
      args = _args;
   }

   /** Returns the invoke type (INVOKESTATIC, INVOKESPECIAL, etc).
    */
   public short getCode(){
      return code;
   }

   public String toString(){
      String result = "InvokeExpr["+code+","+classname+"."+methodname+methodsig;
      if (ref!=null)
         result += ","+ref;
      for (int i=0;i<args.length;i++)
         result += ","+args[i];
      return result+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      if (ref!=null)
         result.addAll(ref.emitBytecode(factory));
      for (int i=0;i<args.length;i++)
         result.addAll(args[i].emitBytecode(factory));
      result.add(factory.createInvoke(classname, methodname, getType(), 
                                      org.apache.bcel.generic.Type.getArgumentTypes(methodsig), code));
      return result;
   }
}
