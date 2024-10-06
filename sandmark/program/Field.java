package sandmark.program;

/**
 * Represents one field in a Java class or interface.
 * A <CODE>Field</CODE> object embeds a BCEL <CODE>FieldGen</CODE>
 * object in a Sandmark program object.
 * Most methods just call the corresponding BCEL method.
 * Modification methods automatically call the
 * {@link sandmark.program.Object#mark() mark} method
 * to register their changes.
 */

public abstract class Field extends sandmark.program.Object {



   /*package*/ org.apache.bcel.generic.FieldGen fieldGen;



   /**
    * Constructs a SandMark Field from a BCEL Field and adds it to a class.
    */
   /*package*/ Field(sandmark.program.Class parent,
                     org.apache.bcel.classfile.Field f) {

      org.apache.bcel.generic.ConstantPoolGen cpg =
         ((sandmark.program.Class)parent).getConstantPool();

      fieldGen =
         new org.apache.bcel.generic.FieldGen(f, cpg);
      setAttributes(f.getAttributes());

      super.setName(constructName());

      parent.add(this);

   }

   String constructName() {
        String name = fieldGen.getName() + "(" + fieldGen.getSignature() + ")";
        return name;
    }

   public String getCanonicalName(){
       return getParent().getCanonicalName() + "." + constructName();
   }

   /**
    * Returns the BCEL Field corresponding to this SandMark Field.
    */
   /*package*/ org.apache.bcel.classfile.Field getField() {

      //NOTE: This is a hack!  FieldGen and Field seem to share underlying
      //attribute data.  When a FieldGen is converted to a Field, this data
      //is duplicated.  In the event that this bug is ever fixed in BCEL,
      //this code can be removed.

      org.apache.bcel.classfile.Attribute[] atts =  fieldGen.getAttributes();
      org.apache.bcel.classfile.Field f = fieldGen.getField();
      f.setAttributes(atts);
      fieldGen.removeAttributes();
      for (int i = 0; i < atts.length; i++) {
         fieldGen.addAttribute(atts[i]);
      }
      return f;
   }



   /**
    * Returns a copy of this field..
    * The new field has a random name and is a member of the
    * same class as this field.
    * The new method is always an instance of LocalField,
    * and mutable, even if copied from a LibraryField instance.
    */
   public sandmark.program.LocalField copy() {
      org.apache.bcel.generic.FieldGen fg = fieldGen.copy(getConstantPool());
      fg.setName("F" + (int)(1e9 * sandmark.util.Random.getRandom().nextDouble()));
      return new sandmark.program.LocalField(
         (sandmark.program.Class) getParent(), fg.getField());
   }

    public sandmark.program.Class getEnclosingClass() {
        return (sandmark.program.Class)getParent();
    }



   /**
    * Returns the ConstantPoolGen associated with this field and its class.
    */
   public org.apache.bcel.generic.ConstantPoolGen getConstantPool() {
      return ((sandmark.program.Class)getParent()).getConstantPool();
   }

    public String getName() {
        return fieldGen.getName();
    }



   /**
    * Sets the ConstantPoolGen associated with this field.
    * It is only safe to do this for a whole class at a time,
    * so this method is restricted although there is a similar
    * public method at the class level.
    */
   /*package*/ void setCPG(sandmark.program.ConstantPoolGen cpg) {
      fieldGen.setConstantPool(cpg);
   }



   public org.apache.bcel.classfile.Attribute[] getAttributes() {
      return fieldGen.getAttributes();
   }

   public org.apache.bcel.classfile.ConstantValue getConstantValue() {
      return getField().getConstantValue();
   }

   public int getAccessFlags() {
      return fieldGen.getAccessFlags();
   }

   public String getInitValue() {
      return fieldGen.getInitValue();
   }

   public int getNameIndex() {
      return getField().getNameIndex();
   }

   public String getSignature() {
      return fieldGen.getSignature();
   }

   public int getSignatureIndex() {
      return getField().getSignatureIndex();
   }

   public org.apache.bcel.generic.Type getType() {
      return fieldGen.getType();
   }



   // BCEL wrapper methods
   //
   // These wrapper methods all begin by calling mark() to verify
   // that this object is mutable.

   public void addAttribute(org.apache.bcel.classfile.Attribute a) {
      mark();
      fieldGen.addAttribute(a);
   }

   public void cancelInitValue() {
      mark();
      fieldGen.cancelInitValue();
   }

   public void removeAttribute(org.apache.bcel.classfile.Attribute a) {
      mark();
      fieldGen.removeAttribute(a);
   }

   public void removeAttributes() {
      mark();
      fieldGen.removeAttributes();
   }

   public void setAccessFlags(int flags) {
      mark();
      fieldGen.setAccessFlags(flags);
   }

   public void setAttributes(org.apache.bcel.classfile.Attribute[] a) {
      mark();
      fieldGen.removeAttributes();
      for (int i = 0; i < a.length; i++) {
         fieldGen.addAttribute(a[i]);
      }
   }

   public void setInitValue(boolean b) {
      mark();
      fieldGen.setInitValue(b);
   }

   public void setInitValue(byte b) {
      mark();
      fieldGen.setInitValue(b);
   }

   public void setInitValue(char c) {
      mark();
      fieldGen.setInitValue(c);
   }

   public void setInitValue(double d) {
      mark();
      fieldGen.setInitValue(d);
   }

   public void setInitValue(float f) {
      mark();
      fieldGen.setInitValue(f);
   }

   public void setInitValue(int i) {
      mark();
      fieldGen.setInitValue(i);
   }

   public void setInitValue(long l) {
      mark();
      fieldGen.setInitValue(l);
   }

   public void setInitValue(short s) {
      mark();
      fieldGen.setInitValue(s);
   }

   public void setInitValue(String str) {
      mark();
      fieldGen.setInitValue(str);
   }

   public void setName(String name) {
      mark();
      fieldGen.setName(name);
      super.setName(constructName());
   }

   public void setNameIndex(int name_index) {
      mark();
      org.apache.bcel.classfile.Field tempField = getField();
      tempField.setNameIndex(name_index);
      fieldGen = new org.apache.bcel.generic.FieldGen
         (tempField, ((sandmark.program.Class)getParent()).getConstantPool());

      super.setName(constructName());
   }

   public void setSignatureIndex(int signature_index) {
      mark();
      org.apache.bcel.classfile.Field tempField = getField();
      tempField.setSignatureIndex(signature_index);
      fieldGen = new org.apache.bcel.generic.FieldGen
         (tempField, ((sandmark.program.Class)getParent()).getConstantPool());

   }

   public void setType(org.apache.bcel.generic.Type type) {
      mark();
      fieldGen.setType(type);
   }



   /** Returns true if the ACC_PUBLIC access flag is set. */
   public boolean isPublic()            { return fieldGen.isPublic(); }

   /** Returns true if the ACC_PRIVATE access flag is set. */
   public boolean isPrivate()           { return fieldGen.isPrivate(); }

   /** Returns true if the ACC_PROTECTED access flag is set. */
   public boolean isProtected()         { return fieldGen.isProtected(); }

   /** Returns true if the ACC_STATIC access flag is set. */
   public boolean isStatic()            { return fieldGen.isStatic(); }

   /** Returns true if the ACC_FINAL access flag is set. */
   public boolean isFinal()             { return fieldGen.isFinal(); }

   /** Returns true if the ACC_SYNCHRONIZED access flag is set. */
   public boolean isSynchronized()      { return fieldGen.isSynchronized(); }

   /** Returns true if the ACC_VOLATILE access flag is set. */
   public boolean isVolatile()          { return fieldGen.isVolatile(); }

   /** Returns true if the ACC_TRANSIENT access flag is set. */
   public boolean isTransient()         { return fieldGen.isTransient(); }

   /** Returns true if the ACC_NATIVE access flag is set. */
   public boolean isNative()            { return fieldGen.isNative(); }

   /** Returns true if the ACC_INTERFACE access flag is set. */
   public boolean isInterface()         { return fieldGen.isInterface(); }

   /** Returns true if the ACC_ABSTRACT access flag is set. */
   public boolean isAbstract()          { return fieldGen.isAbstract(); }

   /** Returns true if the ACC_STRICTFP access flag is set. */
   public boolean isStrictfp()          { return fieldGen.isStrictfp(); }



   /** Sets or clears the ACC_PUBLIC access flag. */
   public void setPublic(boolean flag)
      { mark(); fieldGen.isPublic(flag); }

   /** Sets or clears the ACC_PRIVATE access flag. */
   public void setPrivate(boolean flag)
      { mark(); fieldGen.isPrivate(flag); }

   /** Sets or clears the ACC_PROTECTED access flag. */
   public void setProtected(boolean flag)
      { mark(); fieldGen.isProtected(flag); }

   /** Sets or clears the ACC_STATIC access flag. */
   public void setStatic(boolean flag)
      { mark(); fieldGen.isStatic(flag); }

   /** Sets or clears the ACC_FINAL access flag. */
   public void setFinal(boolean flag)
      { mark(); fieldGen.isFinal(flag); }

   /** Sets or clears the ACC_SYNCHRONIZED access flag. */
   public void setSynchronized(boolean flag)
      { mark(); fieldGen.isSynchronized(flag); }

   /** Sets or clears the ACC_VOLATILE access flag. */
   public void setVolatile(boolean flag)
      { mark(); fieldGen.isVolatile(flag); }

   /** Sets or clears the ACC_TRANSIENT access flag. */
   public void setTransient(boolean flag)
      { mark(); fieldGen.isTransient(flag); }

   /** Sets or clears the ACC_NATIVE access flag. */
   public void setNative(boolean flag)
      { mark(); fieldGen.isNative(flag); }

   /** Sets or clears the ACC_INTERFACE access flag. */
   public void setInterface(boolean flag)
      { mark(); fieldGen.isInterface(flag); }

   /** Sets or clears the ACC_ABSTRACT access flag. */
   public void setAbstract(boolean flag)
      { mark(); fieldGen.isAbstract(flag); }

   /** Sets or clears the ACC_STRICTFP access flag. */
   public void setStrictfp(boolean flag)
      { mark(); fieldGen.isStrictfp(flag); }

}

