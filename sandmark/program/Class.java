package sandmark.program;



/**
 * Represents a single class or interface in a Java program.
 * A <CODE>Class</CODE> object embeds a BCEL <CODE>JavaClass</CODE>
 * object in a Sandmark program object.
 * Most methods just call the corresponding BCEL method.
 * Modification methods automatically call the
 * {@link sandmark.program.Object#mark() mark} method
 * to register their changes.
 */

public abstract class Class extends JarElement {



   // INVARIANT: We always have a JavaClass or ClassGen, but never both.
   // Check to see which; the other one is null.

   private org.apache.bcel.classfile.JavaClass jclass;
   private org.apache.bcel.generic.ClassGen cgen;

   // Maintain the same ConstantPoolGen for the lifetime of the class object

   private sandmark.program.ConstantPoolGen cpg;

   // Flags when initialization is complete.

   private boolean initialized;



   // Subclasses provide these to call constructors of the appropriate classes.

   abstract sandmark.program.ConstantPoolGen makeCPG(
      sandmark.program.Class c, org.apache.bcel.classfile.ConstantPool g);
   abstract sandmark.program.Field makeField(
      sandmark.program.Class c, org.apache.bcel.classfile.Field f);
   abstract sandmark.program.Method makeMethod(
      sandmark.program.Class c, org.apache.bcel.classfile.Method m);



   /**
    * Constructs a SandMark Class from a BCEL JavaClass
    * and adds it to an application.
    */
   /*package*/ Class(sandmark.program.Application parent,
         org.apache.bcel.classfile.JavaClass c,sandmark.program.Object orig) {

      jclass = c;
      setName(jclass.getClassName());
      if (! (this instanceof sandmark.program.LibraryClass)) {
         parent.add(this,orig);
      }

      org.apache.bcel.classfile.Field[] jfields = jclass.getFields();
      org.apache.bcel.classfile.Method[] jmeths = jclass.getMethods();

      // add sandmark subclass of ConstantPoolGen to catch changes
      cpg = makeCPG(this, jclass.getConstantPool());

      // create SandMark objects for fields
      for (int i = 0; i < jfields.length; i++) {
         makeField(this, jfields[i]);
      }

      // create SandMark objects for methods
      for (int i = 0; i < jmeths.length; i++) {
         makeMethod(this, jmeths[i]);
      }
      initialized = true;
   }



   /**
    * Converts BCEL data, if necessary, to JavaClass form,
    * and returns the JavaClass.
    */
   private org.apache.bcel.classfile.JavaClass needClass() {
       if (jclass == null) {
         jclass = cgen.getJavaClass();
         jclass.setConstantPool(cpg.getConstantPool());
         cgen = null;
      }
      return jclass;
   }



   /**
    * Converts BCEL data, if necessary, to ClassGen form,
    * and returns the ClassGen.
    */
   private org.apache.bcel.generic.ClassGen needGen() {
      if (cgen == null) {
         cgen = new org.apache.bcel.generic.ClassGen(jclass);
         cgen.setConstantPool(cpg);
         jclass = null;
      }
      return cgen;
   }

   /**
    * Gets the filename used for saving in a jar file.
    */
   public String getJarName() {
      return getName().replace('.', '/') + ".class";
   }

   public String getCanonicalName(){
       return getName();
   }

   /**
    * Makes the jclass member consistent with changes to
    * methods, fields, CPG, etc.
    */
   private void updateJavaClass() {

      // register the latest versions of all underlying bcel.Method objects
      sandmark.program.Method[] m = getMethods();
      org.apache.bcel.classfile.Method[] methods =
         new org.apache.bcel.classfile.Method[m.length];
      for (int i = 0; i < m.length; i++) {
          try { methods[i] = m[i].getMethod(); }
          catch(Exception e) {
              m[i].removeLineNumbers();
              m[i].removeLocalVariables();
              methods[i] = m[i].getMethod();
          }
      }
      jclass.setMethods(methods);

      // register the latest versions of all underlying bcel.Field objects
      sandmark.program.Field[] f = getFields();
      org.apache.bcel.classfile.Field[] fields =
         new org.apache.bcel.classfile.Field[f.length];
      for (int i = 0; i < f.length; i++) {
         fields[i] = f[i].getField();
      }
      jclass.setFields(fields);

      jclass.setConstantPool(cpg.getFinalConstantPool());
   }



   /**
    * Saves this object as a classfile on an output stream.
    */
   /*package*/ void save(java.io.OutputStream ostream)
         throws java.io.IOException {

      needClass();                      // work with JavaClass representation
      updateJavaClass();
      jclass.dump(ostream);
   }

   private sandmark.program.Class find(String classname) {
      return getApplication() == null ? LibraryClass.find(classname) :
	 getApplication().findClass(classname);
   }



   /**
    * Returns a copy of this class.
    * The new class has a random name and is a member of the
    * same application as this class.
    * The new class is always an instance of LocalClass,
    * and mutable, even if copied from a LibraryClass instance.
    */
   public sandmark.program.LocalClass copy() {
       needClass();
      updateJavaClass();
      org.apache.bcel.generic.ClassGen cg =
         new org.apache.bcel.generic.ClassGen(jclass);
      String name;
      do {
          name = "C" + (int)(1e9 * sandmark.util.Random.getRandom().nextDouble());
      } while(find(name) != null);
      cg.setClassName(name);
      return new sandmark.program.LocalClass(
         this.getApplication(), cg.getJavaClass(), this);
   }



   /**
    * Adds a method or field to this class.
    */
   /*package*/ void add(sandmark.program.Object o) {
      mark();
      needGen();
      if (o instanceof sandmark.program.Field) {
         addField((sandmark.program.Field)o);
      } else { // must be Method
         addMethod((sandmark.program.Method)o);
      }
   }



   /**
    * Removes a method or field from this class.
    */
   /*package*/ void delete(sandmark.program.Object o) {
      mark();
      if (o instanceof sandmark.program.Field) {
         removeField((sandmark.program.Field) o);
      } else { // must be Method
         removeMethod((sandmark.program.Method) o);
      }
   }



   /**
    * Renames this class.
    * This is a local change and does not alter any references.
    * To make a global change, see
    * {@link sandmark.program.util.Renamer sandmark.program.util.Renamer}.
    *
    * @param newName the new class name
    */
   public void setName(String newName) {
      mark();
      super.setName(newName);
      if (jclass != null) {
         jclass.setClassName(newName);
      } else {
         cgen.setClassName(newName);
      }

   }



   /**
    * Get the byte array representation of this class
    * in a form suitable for writing to a .class file
    */
   public byte[] getBytes() {
      needClass();
      updateJavaClass();
      return jclass.getBytes();
   }



   /**
    * Gets a method by name and signature.
    *
    * @return the method in this class with the given name and signature
    */
   public sandmark.program.Method getMethod(String name, String sig) {
      return (sandmark.program.Method) getMember(name + sig);
   }



   /**
    * Returns an array of all methods within this class.
    *
    * @return a list of Method objects representing the methods
    * of this class
    */
   public sandmark.program.Method[] getMethods() {
      return (sandmark.program.Method[])
         sandmark.util.Misc.buildArray(
            methods(), new sandmark.program.Method[0]);
   }



   /**
    * Returns an iterator over all methods within this class.
    *
    * @return an iterator of <code>sandmark.program.Method</code> objects
    */
   public java.util.Iterator methods() {
      return sandmark.util.Misc.instanceFilter(
         members(), sandmark.program.Method.class);
   }



   /**
    * Gets a field by name.
    *
    * @return the field in this class with the given name
    */
   public sandmark.program.Field getField(String name,String signature) {
      // this will find only fields,
      // because method names include a signature
      return (sandmark.program.Field) getMember(name + "(" + signature + ")");
   }



   /**
    * Returns a list of the fields in this class.
    *
    * @return an array of Field objects
    */
   public sandmark.program.Field[] getFields() {
      return (sandmark.program.Field[])
         sandmark.util.Misc.buildArray(
            fields(), new sandmark.program.Field[0]);
   }



   /**
    * Returns an iterator over all fields within this class.
    *
    * @return an iterator of <code>sandmark.program.Field</code>
    * objects
    */
   public java.util.Iterator fields() {
      return sandmark.util.Misc.instanceFilter(
         members(), sandmark.program.Field.class);
   }



// BCEL wrapper functions for JavaClass methods
// (much of the documentation also comes from there)

    public org.apache.bcel.generic.ObjectType getType() {
        return (org.apache.bcel.generic.ObjectType)
            org.apache.bcel.generic.Type.getType
            ("L" + getName().replace('.','/') + ";");
    }



   /**
    * Gets the access flags for this object.  See
    * <a href="http://jakarta.apache.org/bcel/apidocs/org/apache/bcel/classfile/AccessFlags.html">
    * the BCEL API documentation</a> for more info.
    */
   public int getAccessFlags() {
      if (jclass != null) {
         return jclass.getAccessFlags();
      } else {
         return cgen.getAccessFlags();
      }
   }



   /**
    * Returns a list of the attributes for this class.  For more information
    * on Attribute objects see the
    * <a href="http://jakarta.apache.org/bcel/apidocs/index.html">
    * BCEL documentation.</a>
    *
    * @return an array of attribute objects
    */
   public org.apache.bcel.classfile.Attribute[] getAttributes() {
      if (jclass != null) {
         return jclass.getAttributes();
      } else {
         return cgen.getAttributes();
      }
   }



   /**
    * Returns the index of the class name in this class's constant pool.
    *
    * @return the index of this class' name
    */
   public int getClassNameIndex() {
      if (jclass != null) {
         return jclass.getClassNameIndex();
      } else {
         return cgen.getClassNameIndex();
      }
   }



   /**
    * Returns the constant pool for this class in an editable form.
    *
    * @return a
    * <a href="http://jakarta.apache.org/bcel/apidocs/org/apache/bcel/generic/ConstantPoolGen.html">
    * ConstantPoolGen</a> object
    */
   public org.apache.bcel.generic.ConstantPoolGen getConstantPool() {
      return cpg;
   }



   /**
    * Returns the name of the source file that this class file was created
    * from.
    *
    * @return the name of the source file for this class, <code>null</code>
    * if the information is not present
    */
   public String getFileName() {
      if (jclass != null) {
         return jclass.getFileName();
      } else {
         return cgen.getFileName();
      }
   }



   /**
    * Returns a list of names of the interfaces that this class implements.
    *
    * @return a list of the implemented interfaces (as Strings)
    */
   public String[] getInterfaceNames() {
      if (jclass != null) {
         return jclass.getInterfaceNames();
      } else {
         return cgen.getInterfaceNames();
      }
   }



   /**
    * Gets the interfaces that this class implements.
    * A null entry in the results indicates an interface that could
    * not be found.
    */
   public sandmark.program.Class[] getInterfaces() {
      String[] inames = getInterfaceNames();
      sandmark.program.Class[] results =
         new sandmark.program.Class[inames.length];
      for (int i = 0; i < inames.length; i++) {
         results[i] = find(inames[i]);
      }
      return results;
   }



   /**
    * Returns the major version number of the class file.
    */
   public int getMajor() {
      if (jclass != null) {
         return jclass.getMajor();
      } else {
         return cgen.getMajor();
      }
   }



   /**
    * Returns the minor version number of the class file.
    */
   public int getMinor() {
      if (jclass != null) {
         return jclass.getMinor();
      } else {
         return cgen.getMinor();
      }
   }



   /**
    * Returns the name of the package that contains this class.
    */
   public String getPackageName() {
      return needClass().getPackageName();
   }



   /**
    * Returns the absolute path of the file from which
    * this class was read.
    */
   public String getSourceFileName() {
      return needClass().getSourceFileName();
   }



   /**
    * Returns the superclass of this class.
    */
   public sandmark.program.Class getSuperClass() {
      return find(needGen().getSuperclassName());
   }



   /**
    * Returns the list of superclasses of this class.
    * The immediate superclass is first, and the list
    * always ends with java.lang.Object.
    */
   public sandmark.program.Class[] getSuperClasses() {
      java.util.List list = new java.util.ArrayList();
      sandmark.program.Class curr = this;
      for (;;) {
         sandmark.program.Class supr = curr.getSuperClass();
         if (supr == null || supr == curr) {
            break;
         }
         list.add(supr);
         curr = supr;
      }
      return (sandmark.program.Class[])
         list.toArray(new sandmark.program.Class[0]);
   }



   /**
    * Returns the name of the superclass of this class.
    */
   public String getSuperclassName() {
      if (jclass != null) {
         return jclass.getSuperclassName();
      } else {
         return cgen.getSuperclassName();
      }
   }



   /**
    * Returns the index in the constant pool of the name
    * of the superclass of this class.
    */
   public int getSuperclassNameIndex() {
      if (jclass != null) {
         return jclass.getSuperclassNameIndex();
      } else {
         return cgen.getSuperclassNameIndex();
      }
   }



   /**
    * Returns true if this class is derived from the given class.
    * Equivalent to the "instanceof" operator.
    */
   public boolean instanceOf(sandmark.program.Class super_class) {
      this.needClass();
      super_class.needClass();
      return this.jclass.instanceOf(super_class.jclass);
   }



   /**
    * Returns true if this is a class, not an interface.
    */
   public boolean isClass() {
      // implemented locally to avoid heavyweight conversion to JavaClass
      return (getAccessFlags() & org.apache.bcel.Constants.ACC_INTERFACE) == 0;
   }



   /**
    * Replaces the access flags associated with this class.
    */
   public void setAccessFlags(int flags) {
      mark();
      if (jclass != null) {
         jclass.setAccessFlags(flags);
      } else {
         cgen.setAccessFlags(flags);
      }
   }



   /**
    * Replaces the array of attributes associated with this class.
    */
   public void setAttributes(org.apache.bcel.classfile.Attribute[] attributes) {
      mark();
      needClass().setAttributes(attributes);
   }



   /**
    * Sets the name of this class.
    */
   public void setClassName(String class_name) {
       getParent().mark();
       mark();
      setName(class_name);
   }



   /**
    * Sets the class name index, and consequently the class name.
    * The argument value must reference a valid entry in the
    * constant pool.
    */
   public void setClassNameIndex(int class_name_index) {
       getParent().mark();
      mark();                   // verify mutability

      // call setName to get everything updated properly
      org.apache.bcel.classfile.ConstantClass c =
         (org.apache.bcel.classfile.ConstantClass)
            cpg.getConstant(class_name_index);
      setName(c.getBytes(cpg.getConstantPool()));

      // but also call setClassNameIndex to be sure of setting the right index
      // in case there are multiple entries for the string in the constant pool
      if (jclass != null) {
         jclass.setClassNameIndex(class_name_index);
      } else {
         cgen.setClassNameIndex(class_name_index);
      }
   }



   /**
    * Sets the file name of the class, also known as the SourceFile attribute.
    */
   public void setFileName(String file_name) {
      mark();
      needClass().setFileName(file_name);
   }



   /**
    * Sets the list of interfaces implemented by this class.
    */
   public void setInterfaceNames(String[] interface_names) {
       getParent().mark();
       mark();
      needClass().setInterfaceNames(interface_names);
   }



   /**
    * Sets the major version number of the class file.
    * The usual argument is org.apache.bcel.Constants.MAJOR.
    */
   public void setMajor(int major) {
      mark();
      if (jclass != null) {
         jclass.setMajor(major);
      } else {
         cgen.setMajor(major);
      }
   }



   /**
    * Sets the minor version number of the class file.
    * The usual argument is org.apache.bcel.Constants.MINOR.
    */
   public void setMinor(int minor) {
      mark();
      if (jclass != null) {
         jclass.setMinor(minor);
      } else {
         cgen.setMinor(minor);
      }
   }



   /**
    * Sets the value that represents the
    * absolute path of the file from which this class was read.
    */
   public void setSourceFileName(String source_file_name) {
      mark();
      needClass().setSourceFileName(source_file_name);
   }



   /**
    * Sets the name of the superclass from which this class is derived.
    */
   public void setSuperclassName(String superclass_name) {
       getParent().mark();
       mark();
      if (jclass != null) {
         jclass.setSuperclassName(superclass_name);
      } else {
         cgen.setSuperclassName(superclass_name);
      }
   }



   /**
    * Sets the index of the superclass name in the constant pool.
    */
   public void setSuperclassNameIndex(int superclass_name_index) {
       getParent().mark();
       mark();
      if (jclass != null) {
         jclass.setSuperclassNameIndex(superclass_name_index);
      } else {
         cgen.setSuperclassNameIndex(superclass_name_index);
      }
   }



// BCEL wrapper functions for ClassGen methods, excluding duplicates of above



   /**
    * Adds an attribute to this class.
    */
   public void addAttribute(org.apache.bcel.classfile.Attribute a) {
      mark();
      needGen().addAttribute(a);
   }



   /**
    * Adds an empty constructor to this class.
    */
   public void addEmptyConstructor(int access_flags) {

      mark();
      needGen();
      cgen.addEmptyConstructor(access_flags);

      // now we must find the thing to make a sandmark Method object
      org.apache.bcel.classfile.Method mlist[] = cgen.getMethods();
      for (int i = 0; i < mlist.length; i++) {
         org.apache.bcel.classfile.Method m = mlist[i];
         if (m.getName().equals("<init>") && m.getSignature().equals("()V")) {
            new sandmark.program.LocalMethod(this, m);
            return;
         }
      }

      throw new RuntimeException("internal error: lost the constructor");
   }



   /**
    * Adds a field to this class.
    */
   public void addField(sandmark.program.Field f) {
      mark();
      if (initialized) {                // if not already there
         needGen().addField(f.getField());
      }
      super.add(f);
   }



   /**
    * Adds a new interface implemented by this class.
    */
   public void addInterface(String name) {
       getParent().mark();
       mark();
      needGen().addInterface(name);
   }



   /**
    * Adds a method to this class.
    */
   public void addMethod(sandmark.program.Method m) {
      mark();
      if (initialized) {                // if not already there
         needGen().addMethod(m.getMethod());
      }
      super.add(m);
   }



   /**
    * Returns true if the specified field is registered as part of this class.
    */
   public boolean containsField(sandmark.program.Field f) {
      return f.getParent() == this;
   }



   /**
    * Returns the specified field, if part of this class;
    * otherwise returns null.
    */
   public sandmark.program.Field containsField(String name,String signature) {
      return getField(name,signature);
   }



   /**
    * Returns the specified method, if part of this class;
    * otherwise returns null.
    */
   public sandmark.program.Method containsMethod(String name,String signature) {
      return getMethod(name, signature);
   }



   /**
    * Removes an attribute from this class.
    */
   public void removeAttribute(org.apache.bcel.classfile.Attribute a) {
      mark();
      needGen().removeAttribute(a);
   }



   /**
    * Removes a field from this class.
    */
   public void removeField(sandmark.program.Field f) {
      mark();
      needGen().removeField(f.getField());
      super.delete(f);
   }



   /**
    * Removes a method from this class.
    */
   public void removeMethod(sandmark.program.Method m) {
      mark();
      super.delete(m);
   }



   /**
    * Removes an interface from the list of those implemented by this class.
    */
   public void removeInterface(String name) {
       getParent().mark();
       mark();
      needGen().removeInterface(name);
   }



   /**
    * Replaces a field in this class.
    */
   public void replaceField(
         sandmark.program.Field oldField, sandmark.program.Field newField) {
      mark();
      delete(oldField);
      add(newField);
   }



   /**
    * Replaces a method in this class.
    */
   public void replaceMethod(
         sandmark.program.Method oldMethod, sandmark.program.Method newMethod) {
      mark();
      delete(oldMethod);
      add(newMethod);
   }



   /**
    * Replaces the constant pool used by this class.
    * A new SandMark ConstantPoolGen is created and installed
    * in this class and all constituent methods and fields.
    * References in those components are <STRONG> not </STRONG>
    * updated.  The new ConstantPoolGen is returned.
    */
   public org.apache.bcel.generic.ConstantPoolGen
         setConstantPool(org.apache.bcel.classfile.ConstantPool g) {

      cpg = makeCPG(this, g);
      sandmark.program.Method[] mlist = getMethods();
      for (int i = 0; i < mlist.length; i++) {
         mlist[i].setCPG(cpg);
      }
      sandmark.program.Field[] flist = getFields();
      for (int i = 0; i < flist.length; i++) {
         flist[i].setCPG(cpg);
      }
      if (jclass != null) {
         jclass.setConstantPool(cpg.getConstantPool());
      } else {
         cgen.setConstantPool(cpg);
      }
      return cpg;
   }



   /** Returns true if the ACC_PUBLIC access flag is set. */
   public boolean isPublic()            { return needGen().isPublic(); }

   /** Returns true if the ACC_PRIVATE access flag is set. */
   public boolean isPrivate()           { return needGen().isPrivate(); }

   /** Returns true if the ACC_PROTECTED access flag is set. */
   public boolean isProtected()         { return needGen().isProtected(); }

   /** Returns true if the ACC_STATIC access flag is set. */
   public boolean isStatic()            { return needGen().isStatic(); }

   /** Returns true if the ACC_FINAL access flag is set. */
   public boolean isFinal()             { return needGen().isFinal(); }

   /** Returns true if the ACC_SYNCHRONIZED access flag is set. */
   public boolean isSynchronized()      { return needGen().isSynchronized(); }

   /** Returns true if the ACC_VOLATILE access flag is set. */
   public boolean isVolatile()          { return needGen().isVolatile(); }

   /** Returns true if the ACC_TRANSIENT access flag is set. */
   public boolean isTransient()         { return needGen().isTransient(); }

   /** Returns true if the ACC_NATIVE access flag is set. */
   public boolean isNative()            { return needGen().isNative(); }

   /** Returns true if the ACC_INTERFACE access flag is set. */
   public boolean isInterface()         { return needGen().isInterface(); }

   /** Returns true if the ACC_ABSTRACT access flag is set. */
   public boolean isAbstract()          { return needGen().isAbstract(); }

   /** Returns true if the ACC_STRICTFP access flag is set. */
   public boolean isStrictfp()          { return needGen().isStrictfp(); }

   /** Returns true if the ACC_SUPER access flag is set.  */
   public boolean isSuper() {
      // implemented locally to avoid heavyweight conversion to JavaClass
      return (getAccessFlags() & org.apache.bcel.Constants.ACC_SUPER) != 0;
   }



   /** Sets or clears the ACC_PUBLIC access flag. */
   public void setPublic(boolean flag)
      { mark(); needGen().isPublic(flag); }

   /** Sets or clears the ACC_PRIVATE access flag. */
   public void setPrivate(boolean flag)
      { mark(); needGen().isPrivate(flag); }

   /** Sets or clears the ACC_PROTECTED access flag. */
   public void setProtected(boolean flag)
      { mark(); needGen().isProtected(flag); }

   /** Sets or clears the ACC_STATIC access flag. */
   public void setStatic(boolean flag)
      { mark(); needGen().isStatic(flag); }

   /** Sets or clears the ACC_FINAL access flag. */
   public void setFinal(boolean flag)
      { mark(); needGen().isFinal(flag); }

   /** Sets or clears the ACC_SYNCHRONIZED access flag. */
   public void setSynchronized(boolean flag)
      { mark(); needGen().isSynchronized(flag); }

   /** Sets or clears the ACC_VOLATILE access flag. */
   public void setVolatile(boolean flag)
      { mark(); needGen().isVolatile(flag); }

   /** Sets or clears the ACC_TRANSIENT access flag. */
   public void setTransient(boolean flag)
      { mark(); needGen().isTransient(flag); }

   /** Sets or clears the ACC_NATIVE access flag. */
   public void setNative(boolean flag)
      { mark(); needGen().isNative(flag); }

   /** Sets or clears the ACC_INTERFACE access flag. */
   public void setInterface(boolean flag)
      { mark(); needGen().isInterface(flag); }

   /** Sets or clears the ACC_ABSTRACT access flag. */
   public void setAbstract(boolean flag)
      { mark(); needGen().isAbstract(flag); }

   /** Sets or clears the ACC_STRICTFP access flag. */
   public void setStrictfp(boolean flag)
      { mark(); needGen().isStrictfp(flag); }

   /** Sets or clears the ACC_SUPER access flag. */
    public void setSuper(boolean flag){
        mark();
        if(flag)
            needGen().setAccessFlags(getAccessFlags() |
                                     org.apache.bcel.Constants.ACC_SUPER);
        else
            needGen().setAccessFlags(getAccessFlags() &
                                     ~org.apache.bcel.Constants.ACC_SUPER);
    }
}

