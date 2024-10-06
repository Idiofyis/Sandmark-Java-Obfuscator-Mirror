package sandmark.program;



/**
 * Represents a single method within a class or interface.
 * A <CODE>Method</CODE> object embeds a BCEL <CODE>MethodGen</CODE>
 * object in a Sandmark program object.
 * Most methods just call the corresponding BCEL method.
 * Modification methods automatically call the
 * {@link sandmark.program.Object#mark() mark} method
 * to register their changes.
 *
 * <P>Here is a simple example of how to use the BCEL interfaces.
 * This code adds a NOP instruction at the beginning of each method
 * in a supplied class.
 * <PRE>
 *    static void addNOPs(sandmark.program.Class c) {
 *       sandmark.program.Method[] mlist = c.getMethods();
 *       java.util.Iterator it = c.methods();
 *       while (it.hasNext()) {
 *          sandmark.program.Method m = (sandmark.program.Method) it.next();
 *          System.out.println(
 *             "    method " + m.getName() + " " + m.getSignature());
 *          org.apache.bcel.generic.InstructionList ilist = m.getInstructionList();
 *          ilist.insert(org.apache.bcel.generic.InstructionConstants.NOP);
 *          m.mark();
 *       }
 *    }
 * </PRE>
 */

public abstract class Method extends sandmark.program.Object {
   private static final String CFG_KEY = "sandmark.program.Method-CFG";
   private static final String IFG_KEY = "sandmark.program.Method-IFG";
   private static final String SS_KEY = "sandmark.program.Method-SS";

   /*package*/ org.apache.bcel.generic.MethodGen methodGen;


   /**
    * Constructs a SandMark Method from a BCEL Method and adds it to a class.
    */
   /*package*/ Method(sandmark.program.Class parent,
                      org.apache.bcel.classfile.Method method) {
      this(parent, new org.apache.bcel.generic.MethodGen
           (method, parent.getName(), parent.getConstantPool()),null);
   }

   Method(sandmark.program.Class parent, 
          org.apache.bcel.generic.MethodGen gen,
          sandmark.program.Object orig){
      super(orig);
      methodGen = gen;
      
      super.setName(constructName());
      parent.add(this,orig);

      if (gen.getConstantPool()!=parent.getConstantPool())
         throw new Error("unequal constant pools");
   }



   /** This method addresses a bug in BCEL that causes LDC_W instructions 
       to have bad internal state combinations. This method need only be called on
       BCEL-parsed methods.
   */
   /*package*/ void fixLDC_WBug(){
      org.apache.bcel.generic.InstructionList ilist = getInstructionList();
      if (ilist==null)
         return;

      for (java.util.Iterator hiter=ilist.iterator();hiter.hasNext();){
         org.apache.bcel.generic.InstructionHandle handle = 
            (org.apache.bcel.generic.InstructionHandle)hiter.next();
         
         if (handle.getInstruction() instanceof org.apache.bcel.generic.LDC){
            org.apache.bcel.generic.LDC ldc = 
               (org.apache.bcel.generic.LDC)handle.getInstruction();
            ldc.setIndex(ldc.getIndex());
         }
      }
   }


   /**
    * Returns the unique name under which this Method is registered
    * in the parent Class object.
    */
   /*package*/ String constructName() {
      return methodGen.getName() + methodGen.getSignature();
   }

   public String getCanonicalName(){
      return getParent().getCanonicalName() + "." +  constructName();
   }

   /**
    * Returns a BCEL Method corresponding to this SandMark Method.
    */
   /*package*/ org.apache.bcel.classfile.Method getMethod() {
      methodGen.setMaxLocals();
      methodGen.setMaxStack();
      return methodGen.getMethod();
   }



   /**
    * Returns a copy of this method.
    * The new method has a random name and is a member of the
    * same class as this method.
    * The new method is always an instance of LocalMethod,
    * and mutable, even if copied from a LibraryMethod instance.
    */
   public sandmark.program.LocalMethod copy() {
      org.apache.bcel.generic.MethodGen mg =
         methodGen.copy(getClassName(), getConstantPool());
      String name;
      do {
         name = "M" + (int)(1e9 * sandmark.util.Random.getRandom().nextDouble());
      } while(getEnclosingClass().getMethod(name,getSignature()) != null);
      mg.setName(name);
      mg.setMaxLocals();
      mg.setMaxStack();
      return new sandmark.program.LocalMethod(
                                              getEnclosingClass(), mg, this);
   }



   /**
    * Returns the class that contains this method.
    * @return the class object for the enclosing class
    */
   public sandmark.program.Class getEnclosingClass() {
      return (sandmark.program.Class)getParent();
   }



   /**
    * Returns the method in the superclass that this method overrides.
    * @return the superclass method that is overridden or <code>null</code>
    * if no method exists
    */
   public sandmark.program.Method getSuperMethod() {
      //traverse up the parent classes
      sandmark.program.Class currClass =
         ((sandmark.program.Class)this.getParent()).getSuperClass();

      String methodKey = constructName();

      while (currClass != null) {
         sandmark.program.Method superMethod =
            (sandmark.program.Method) currClass.retrieve(methodKey);

         if (superMethod != null) {
            return superMethod;
         }

         currClass = currClass.getSuperClass();
      }

      return null;
   }



   /**
    * Returns the control flow graph for this method.
    * Paths due to exceptions are included.
    * @return a control flow graph
    */
   public sandmark.analysis.controlflowgraph.MethodCFG getCFG() {
      return getCFG(true);
   }



   /**
    * Returns the control flow graph for this method.
    * The parameter specifies whether paths due to exceptions are included.
    * @return a control flow graph
    */
   public sandmark.analysis.controlflowgraph.MethodCFG getCFG(
                                                              boolean withExceptions) {
      String key = CFG_KEY + withExceptions;
      sandmark.analysis.controlflowgraph.MethodCFG cfg =
         (sandmark.analysis.controlflowgraph.MethodCFG)retrieve(key);

      if (cfg == null) {
         cfg = new sandmark.analysis.controlflowgraph.MethodCFG(
                                                                this, withExceptions);
         cache(key, cfg);
      }

      return cfg;
   }



   /**
    * Returns the interference graph for this method's local variables.
    * @return an interference graph
    */
   public sandmark.analysis.interference.InterferenceGraph getIFG() {
      sandmark.analysis.interference.InterferenceGraph ifg =
         (sandmark.analysis.interference.InterferenceGraph)retrieve(IFG_KEY);

      if (ifg == null) {
         ifg = new sandmark.analysis.interference.InterferenceGraph(this);
         cache(IFG_KEY, ifg);
      }

      return ifg;
   }



   /**
    * Returns information about the state of the data stack
    * at each point in the method.
    * @return a simulation of the data stack throughout the method
    */
   public sandmark.analysis.stacksimulator.StackSimulator getStack() {
      sandmark.analysis.stacksimulator.StackSimulator ss =
         (sandmark.analysis.stacksimulator.StackSimulator)retrieve(SS_KEY);

      if (ss == null) {
         ss = new sandmark.analysis.stacksimulator.StackSimulator(this);
         cache(SS_KEY, ss);
      }

      return ss;
   }



   /**
    * Returns true if this method has the right name (<CODE>main</CODE>)
    * and signature (<CODE>public static void (String[])</CODE>)
    * to be the initial entry point of a Java program.
    */
   public boolean isMain() {
      int f = org.apache.bcel.Constants.ACC_PUBLIC
         | org.apache.bcel.Constants.ACC_STATIC;
      return (getAccessFlags() & f) == f
         && getName().equals("main")
         && getSignature().equals("([Ljava/lang/String;)V");
   }



   /**
    * Returns the ConstantPoolGen associated with this method and its class.
    */
   public org.apache.bcel.generic.ConstantPoolGen getConstantPool() {
      return getCPG();
   }



   /**
    * Returns the ConstantPoolGen associated with this method and its class.
    */
   public org.apache.bcel.generic.ConstantPoolGen getCPG() {
      return ((sandmark.program.Class)getParent()).getConstantPool();
   }



   /**
    * Sets the ConstantPoolGen associated with this method.
    * It is only safe to do this for a whole class at a time,
    * so this method is restricted although there is a similar
    * public method at the class level.
    */
   /*package*/ void setCPG(sandmark.program.ConstantPoolGen cpg) {
      methodGen.setConstantPool(cpg);
   }

   // BCEL wrapper functions from class Method

   public int getAccessFlags() {
      return methodGen.getAccessFlags();
   }

   public org.apache.bcel.classfile.Attribute[] getAttributes() {
      return methodGen.getAttributes();
   }

   public org.apache.bcel.classfile.Code getCode() {
      return methodGen.getMethod().getCode();
   }

   public org.apache.bcel.classfile.ExceptionTable getExceptionTable() {
      return methodGen.getMethod().getExceptionTable();
   }

   public String getName() {
      return methodGen.getName();
   }

   public org.apache.bcel.classfile.LocalVariableTable getLocalVariableTable() {
      return methodGen.getLocalVariableTable(getCPG());
   }

   public String getSignature() {
      return methodGen.getSignature();
   }

   public org.apache.bcel.generic.Type getType() {
      return methodGen.getType();
   }

   public String getArgumentName(int i) {
      return methodGen.getArgumentName(i);
   }

   public String[] getArgumentNames() {
      return methodGen.getArgumentNames();
   }

   public org.apache.bcel.generic.Type getArgumentType(int i) {
      return methodGen.getArgumentType(i);
   }

   public org.apache.bcel.generic.Type[] getArgumentTypes() {
      return methodGen.getArgumentTypes();
   }

   public String getClassName() {
      return methodGen.getClassName();
   }

   public org.apache.bcel.classfile.Attribute[] getCodeAttributes() {
      return methodGen.getCodeAttributes();
   }

   public org.apache.bcel.generic.CodeExceptionGen[] getExceptionHandlers() {
      return methodGen.getExceptionHandlers();
   }

   public String[] getExceptions() {
      return methodGen.getExceptions();
   }

   public org.apache.bcel.generic.InstructionList getInstructionList() {
      return methodGen.getInstructionList();
   }

   public org.apache.bcel.generic.LineNumberGen[] getLineNumbers() {
      return methodGen.getLineNumbers();
   }

   public org.apache.bcel.classfile.LineNumberTable getLineNumberTable() {
      return methodGen.getLineNumberTable(getCPG());
   }

   public org.apache.bcel.generic.LocalVariableGen[] getLocalVariables() {
      return methodGen.getLocalVariables();
   }

   public int getMaxLocals() {
      return methodGen.getMaxLocals();
   }

   /* Lifted directly from BCEL's setMaxLocals() in MethodGen.java ver 1.7 */
   public int calcMaxLocals() {
      org.apache.bcel.generic.InstructionList il =
         getInstructionList();
      org.apache.bcel.generic.Type arg_types[] =
         getArgumentTypes();
      org.apache.bcel.generic.ConstantPoolGen cp =
         getConstantPool();

      if(il == null)
         return 0;

      int max = isStatic()? 0 : 1;

      if(arg_types != null)
         for(int i=0; i < arg_types.length; i++)
            max += arg_types[i].getSize();

      for(org.apache.bcel.generic.InstructionHandle ih = il.getStart();
          ih != null; ih = ih.getNext()) {
         org.apache.bcel.generic.Instruction ins = ih.getInstruction();

         if((ins instanceof
             org.apache.bcel.generic.LocalVariableInstruction) ||
            (ins instanceof org.apache.bcel.generic.RET) ||
            (ins instanceof org.apache.bcel.generic.IINC)) {
            int index =
               ((org.apache.bcel.generic.IndexedInstruction)
                ins).getIndex() +
               ((org.apache.bcel.generic.TypedInstruction)
                ins).getType(cp).getSize();

            if(index > max)
               max = index;
         }
      }

      return max;
   }

   public int getMaxStack() {
      return methodGen.getMaxStack();
   }

   public org.apache.bcel.generic.Type getReturnType() {
      return methodGen.getReturnType();
   }



   public void addAttribute(org.apache.bcel.classfile.Attribute a) {
      mark();
      methodGen.addAttribute(a);
   }



   public void removeAttribute(org.apache.bcel.classfile.Attribute a) {
      mark();
      methodGen.removeAttribute(a);
   }



   public void removeAttributes() {
      mark();
      methodGen.removeAttributes();
   }



   public void setAccessFlags(int access_flags) {
      mark();
      methodGen.setAccessFlags(access_flags);
   }



   public void setAttributes(org.apache.bcel.classfile.Attribute[] attributes) {
      //methodGen.setAttributes(attributes);
      //I find it very strange that methodGen has removeAttributes and
      //addAttribute, but no setAttributes

      //I am doing this the "less efficient" way to avoid the conversion
      //to Method and back to MethodGen
      mark();
      methodGen.removeAttributes();
      for (int i = 0; i < attributes.length; i++) {
         methodGen.addAttribute(attributes[i]);
      }
   }



   public void setName(String newName) {
      //need to rehash the member in its parent
      mark();
      methodGen.setName(newName);
      super.setName(constructName());
   }



   public void setType(org.apache.bcel.generic.Type type) {
      mark();
      methodGen.setType(type);
      super.setName(constructName());
   }



   public void removeLineNumber(org.apache.bcel.generic.LineNumberGen l) {
      mark();
      methodGen.removeLineNumber(l);
   }

   public void removeLineNumbers() {
      mark();
      methodGen.removeLineNumbers();
   }

   public void removeLocalVariable(org.apache.bcel.generic.LocalVariableGen l) {
      mark();
      methodGen.removeLocalVariable(l);
   }

   public void removeLocalVariables() {
      mark();
      methodGen.removeLocalVariables();
   }

   public void removeNOPs() {
      mark();
      methodGen.removeNOPs();
   }

   public void addCodeAttribute(org.apache.bcel.classfile.Attribute a) {
      mark();
      methodGen.addCodeAttribute(a);
   }

   public void addException(String class_name) {
      mark();
      methodGen.addException(class_name);
   }

   public org.apache.bcel.generic.CodeExceptionGen addExceptionHandler
      (org.apache.bcel.generic.InstructionHandle start_pc,
       org.apache.bcel.generic.InstructionHandle end_pc,
       org.apache.bcel.generic.InstructionHandle handler_pc,
       org.apache.bcel.generic.ObjectType catch_type) {

      mark();
      return methodGen.addExceptionHandler
         (start_pc, end_pc, handler_pc, catch_type);
   }

   public org.apache.bcel.generic.LineNumberGen addLineNumber
      (org.apache.bcel.generic.InstructionHandle ih,
       int src_line) {

      mark();
      return methodGen.addLineNumber(ih, src_line);
   }

   public org.apache.bcel.generic.LocalVariableGen addLocalVariable
      (String name,
       org.apache.bcel.generic.Type type,
       org.apache.bcel.generic.InstructionHandle start,
       org.apache.bcel.generic.InstructionHandle end) {

      mark();
      return methodGen.addLocalVariable(name, type, start, end);
   }

   public org.apache.bcel.generic.LocalVariableGen addLocalVariable
      (String name, org.apache.bcel.generic.Type type,
       int slot, org.apache.bcel.generic.InstructionHandle start,
       org.apache.bcel.generic.InstructionHandle end) {

      mark();
      return methodGen.addLocalVariable(name, type, slot, start, end);
   }


   public void removeCodeAttribute(org.apache.bcel.classfile.Attribute a) {
      mark();
      methodGen.removeCodeAttribute(a);
   }

   public void removeCodeAttributes() {
      mark();
      methodGen.removeCodeAttributes();
   }

   public void removeException(String c) {
      mark();
      methodGen.removeException(c);
   }

   public void removeExceptionHandler(
                                      org.apache.bcel.generic.CodeExceptionGen c) {
      mark();
      methodGen.removeExceptionHandler(c);
   }

   public void removeExceptionHandlers() {
      mark();
      methodGen.removeExceptionHandlers();
   }

   public void removeExceptions() {
      mark();
      methodGen.removeExceptions();
   }

   ///////////////////

   public void setArgumentName(int i, String name) {
      mark();
      methodGen.setArgumentName(i, name);
   }

   public void setArgumentNames(String[] arg_names) {
      mark();
      methodGen.setArgumentNames(arg_names);
   }

   public void setArgumentType(int i, org.apache.bcel.generic.Type type) {
      mark();
      methodGen.setArgumentType(i, type);
      super.setName(constructName());
   }

   public void setArgumentTypes(org.apache.bcel.generic.Type[] arg_types) {
      mark();
      methodGen.setArgumentTypes(arg_types);
      super.setName(constructName());
   }


   public void setInstructionList(org.apache.bcel.generic.InstructionList il) {
      mark();
      methodGen.setInstructionList(il);
   }

   public void setMaxLocals() {
      mark();
      methodGen.setMaxLocals();
   }

   public void setMaxLocals(int m) {
      mark();
      methodGen.setMaxLocals(m);
   }

   public void setMaxStack() {
      mark();
      methodGen.setMaxStack();
   }

   public void setMaxStack(int m) {
      mark();
      methodGen.setMaxStack(m);
   }

   public void setReturnType(org.apache.bcel.generic.Type return_type) {
      mark();
      methodGen.setReturnType(return_type);
      super.setName(constructName());
   }

   public void stripAttributes(boolean flag) {
      mark();
      methodGen.stripAttributes(flag);
   }



   /** Returns true if the ACC_PUBLIC access flag is set. */
   public boolean isPublic()            { return methodGen.isPublic(); }

   /** Returns true if the ACC_PRIVATE access flag is set. */
   public boolean isPrivate()           { return methodGen.isPrivate(); }

   /** Returns true if the ACC_PROTECTED access flag is set. */
   public boolean isProtected()         { return methodGen.isProtected(); }

   /** Returns true if the ACC_STATIC access flag is set. */
   public boolean isStatic()            { return methodGen.isStatic(); }

   /** Returns true if the ACC_FINAL access flag is set. */
   public boolean isFinal()             { return methodGen.isFinal(); }

   /** Returns true if the ACC_SYNCHRONIZED access flag is set. */
   public boolean isSynchronized()      { return methodGen.isSynchronized(); }

   /** Returns true if the ACC_VOLATILE access flag is set. */
   public boolean isVolatile()          { return methodGen.isVolatile(); }

   /** Returns true if the ACC_TRANSIENT access flag is set. */
   public boolean isTransient()         { return methodGen.isTransient(); }

   /** Returns true if the ACC_NATIVE access flag is set. */
   public boolean isNative()            { return methodGen.isNative(); }

   /** Returns true if the ACC_INTERFACE access flag is set. */
   public boolean isInterface()         { return methodGen.isInterface(); }

   /** Returns true if the ACC_ABSTRACT access flag is set. */
   public boolean isAbstract()          { return methodGen.isAbstract(); }

   /** Returns true if the ACC_STRICTFP access flag is set. */
   public boolean isStrictfp()          { return methodGen.isStrictfp(); }



   /** Sets or clears the ACC_PUBLIC access flag. */
   public void setPublic(boolean flag)
   { mark(); methodGen.isPublic(flag); }

   /** Sets or clears the ACC_PRIVATE access flag. */
   public void setPrivate(boolean flag)
   { mark(); methodGen.isPrivate(flag); }

   /** Sets or clears the ACC_PROTECTED access flag. */
   public void setProtected(boolean flag)
   { mark(); methodGen.isProtected(flag); }

   /** Sets or clears the ACC_STATIC access flag. */
   public void setStatic(boolean flag)
   { mark(); methodGen.isStatic(flag); }

   /** Sets or clears the ACC_FINAL access flag. */
   public void setFinal(boolean flag)
   { mark(); methodGen.isFinal(flag); }

   /** Sets or clears the ACC_SYNCHRONIZED access flag. */
   public void setSynchronized(boolean flag)
   { mark(); methodGen.isSynchronized(flag); }

   /** Sets or clears the ACC_VOLATILE access flag. */
   public void setVolatile(boolean flag)
   { mark(); methodGen.isVolatile(flag); }

   /** Sets or clears the ACC_TRANSIENT access flag. */
   public void setTransient(boolean flag)
   { mark(); methodGen.isTransient(flag); }

   /** Sets or clears the ACC_NATIVE access flag. */
   public void setNative(boolean flag)
   { mark(); methodGen.isNative(flag); }

   /** Sets or clears the ACC_INTERFACE access flag. */
   public void setInterface(boolean flag)
   { mark(); methodGen.isInterface(flag); }

   /** Sets or clears the ACC_ABSTRACT access flag. */
   public void setAbstract(boolean flag)
   { mark(); methodGen.isAbstract(flag); }

   /** Sets or clears the ACC_STRICTFP access flag. */
   public void setStrictfp(boolean flag)
   { mark(); methodGen.isStrictfp(flag); }
}
