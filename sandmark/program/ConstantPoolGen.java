package sandmark.program;

/**
 * Wraps the ConstantPoolGen class in order to intercept BCEL method calls.
 * Each method that modifies the constant pool calls the <CODE>mark</CODE>
 * method of the associated class in addition to performing its usual function.
 */

abstract class ConstantPoolGen extends org.apache.bcel.generic.ConstantPoolGen {



   /**
    * The enclosing Class object.
    */
   private sandmark.program.Class smclass;



   /**
    * Constructs a Sandmark ConstantPoolGen from a BCEL ConstantPool.
    */
   ConstantPoolGen(sandmark.program.Class c,
         org.apache.bcel.classfile.ConstantPool cp) {

      super(cp);
      smclass = c;
   }



   /**
    * Gets the class associated with this constant pool.
    */
   sandmark.program.Class getSMClass() {
      return smclass;
   }



   // most headers below were extracted unedited from BCEL ConstantPoolGen


   /***
    * Add a new String constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param str String to add
    * @return index of entry
    */
   public int addString(String str) {
      smclass.mark();
      return super.addString(str);
   }


   /***
    * Add a new Class reference to the ConstantPool,
    * if it is not already in there.
    *
    * @param str Class to add
    * @return index of entry
    */
   public int addClass(String str) {
      smclass.mark();
      return super.addClass(str);
   }


   /***
    * Add a new Class reference to the ConstantPool for a given type.
    *
    * @param str Class to add
    * @return index of entry
    */
   public int addClass(org.apache.bcel.generic.ObjectType type) {
      smclass.mark();
      return super.addClass(type);
   }


   /***
    * Add a reference to an array class (e.g. String[][]) as needed by
    * MULTIANEWARRAY instruction, e.g. to the ConstantPool.
    *
    * @param type type of array class
    * @return index of entry
    */
   public int addArrayClass(org.apache.bcel.generic.ArrayType type) {
      smclass.mark();
      return super.addArrayClass(type);
   }


   /***
    * Add a new Integer constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param n integer number to add
    * @return index of entry
    */
   public int addInteger(int n) {
      smclass.mark();
      return super.addInteger(n);
   }


   /***
    * Add a new Float constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param n Float number to add
    * @return index of entry
    */
   public int addFloat(float n) {
      smclass.mark();
      return super.addFloat(n);
   }


   /***
    * Add a new Utf8 constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param n Utf8 string to add
    * @return index of entry
    */
   public int addUtf8(String n) {
      smclass.mark();
      return super.addUtf8(n);
   }


   /***
    * Add a new long constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param n Long number to add
    * @return index of entry
    */
   public int addLong(long n) {
      smclass.mark();
      return super.addLong(n);
   }


   /***
    * Add a new double constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param n Double number to add
    * @return index of entry
    */
   public int addDouble(double n) {
      smclass.mark();
      return super.addDouble(n);
   }


   /***
    * Add a new NameAndType constant to the ConstantPool
    * if it is not already in there.
    *
    * @param n NameAndType string to add
    * @return index of entry
    */
   public int addNameAndType(String name, String signature) {
      smclass.mark();
      return super.addNameAndType(name, signature);
   }


   /***
    * Add a new Methodref constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param n Methodref string to add
    * @return index of entry
    */
   public int addMethodref(
         String class_name, String method_name, String signature) {
      smclass.mark();
      return super.addMethodref(class_name, method_name, signature);
   }


   /***
    * Add a new Methodref constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param method method to add
    * @return index of entry
    */
   public int addMethodref(org.apache.bcel.generic.MethodGen method) {
      smclass.mark();
      return super.addMethodref(method);
   }


   /***
    * Add a new Methodref constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param method method to add
    * @return index of entry
    */
   public int addMethodref(sandmark.program.Method method) {
      return addMethodref(
         method.getClass().getName(), method.getName(), method.getSignature());
   }


   /***
    * Add a new InterfaceMethodref constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param n InterfaceMethodref string to add
    * @return index of entry
    */
   public int addInterfaceMethodref(
         String class_name, String method_name, String signature) {
      smclass.mark();
      return super.addInterfaceMethodref(class_name, method_name, signature);
   }


   /***
    * Add a new InterfaceMethodref constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param method method to add
    * @return index of entry
    */
   public int addInterfaceMethodref(org.apache.bcel.generic.MethodGen method) {
      smclass.mark();
      return super.addInterfaceMethodref(method);
   }


   /***
    * Add a new InterfaceMethodref constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param method method to add
    * @return index of entry
    */
   public int addInterfaceMethodref(sandmark.program.Method method) {
      return addInterfaceMethodref(
         method.getClass().getName(), method.getName(), method.getSignature());
   }


   /***
    * Add a new Fieldref constant to the ConstantPool,
    * if it is not already in there.
    *
    * @param n Fieldref string to add
    * @return index of entry
    */
   public int addFieldref(String class_name,String field_name,String signature){
      smclass.mark();
      return super.addFieldref(class_name, field_name, signature);
   }


   /***
    * "Use with care!" is the entire BCEL description.
    * (It should be something like "Do not use, it's broken.")
    *
    * @param i index in constant pool
    * @param c new constant pool entry at index i
    */
   public void setConstant(int i, org.apache.bcel.classfile.Constant c) {
      smclass.mark();
      super.setConstant(i, c);
   }


   /*** Import constant from another ConstantPool and return new index.
    */
   public int addConstant(
         org.apache.bcel.classfile.Constant c, ConstantPoolGen cp) {
      smclass.mark();
      return super.addConstant(c, cp);
   }


}

