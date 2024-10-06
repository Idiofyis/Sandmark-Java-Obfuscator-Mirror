package sandmark.program;



/**
 * Wraps the ConstantPoolGen class in order to intercept
 * method calls and throw exceptions on modification attempts.
 */

class LibraryCPG extends ConstantPoolGen {



   /**
    * Constructs a Sandmark LibraryCPG  from a BCEL ConstantPool.
    */
   LibraryCPG(sandmark.program.Class c,
         org.apache.bcel.classfile.ConstantPool cp) {

      super(c, cp);
   }



   // These stubs just throw exceptions, because a LibraryCPG is immutable.

   public int addString(String str) {
      return unsupp();
   }

   public int addClass(String str) {
      return unsupp();
   }

   public int addClass(org.apache.bcel.generic.ObjectType type) {
      return unsupp();
   }

   public int addArrayClass(org.apache.bcel.generic.ArrayType type) {
      return unsupp();
   }

   public int addInteger(int n) {
      return unsupp();
   }

   public int addFloat(float n) {
      return unsupp();
   }

   public int addUtf8(String n) {
      return unsupp();
   }

   public int addLong(long n) {
      return unsupp();
   }

   public int addDouble(double n) {
      return unsupp();
   }

   public int addNameAndType(String name, String signature) {
      return unsupp();
   }

   public int addMethodref(
         String class_name, String method_name, String signature) {
      return unsupp();
   }

   public int addMethodref(org.apache.bcel.generic.MethodGen method) {
      return unsupp();
   }

   public int addMethodref(sandmark.program.Method method) {
      return unsupp();
   }

   public int addInterfaceMethodref(
         String class_name, String method_name, String signature) {
      return unsupp();
   }

   public int addInterfaceMethodref(org.apache.bcel.generic.MethodGen method) {
      return unsupp();
   }

   public int addInterfaceMethodref(sandmark.program.Method method) {
      return unsupp();
   }

   public int addFieldref(String class_name,String field_name,String signature){
      return unsupp();
   }

   public void setConstant(int i, org.apache.bcel.classfile.Constant c) {
      unsupp();
   }

   public int addConstant(
         org.apache.bcel.classfile.Constant c, ConstantPoolGen cp) {
      return unsupp();
   }

   private int unsupp() {
      throw new java.lang.UnsupportedOperationException(
         "immutable: " + this.toString());
   }



}

