package sandmark.metric;

/** This class contains the utility modules for Metric classes. It consists of
 *  various APIs that evaluate diffent code statistics of the target application.
 *  @author     Tapas R. Sahoo
 */
public class StatsUtil
{
   public static int getNumClasses(sandmark.program.Application app)
   {
      return app.getClasses().length;
   }

   public static String getClassNameAt(sandmark.program.Application app, int index)
   {
      sandmark.program.Class[] classes = app.getClasses();
      return classes[index].getName();
   }

   /** returns packageNames in the format A/B/C.. stored in vector
    *  (see 'parsePackage' method for implementation detail
    */
   public static java.util.Vector getPackageNames(sandmark.program.Application app)
   {
      java.util.HashSet set = new java.util.HashSet();

      for (java.util.Iterator classiter = app.classes(); classiter.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)classiter.next();
         set.add(clazz.getPackageName());
      }
      return new java.util.Vector(set);
   }


   public static java.util.Vector getListOfClassesByPackageName(sandmark.program.Application app, String packageName)
   {
      java.util.Vector myClasses = new java.util.Vector();

      for (java.util.Iterator classiter = app.classes();classiter.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)classiter.next();
         if (clazz.getPackageName().equals(packageName))
            myClasses.add(clazz);
      }
      return myClasses;
   }


   public static int getNumberOfConditionalStats(sandmark.program.Method method)
   {
      int count=0;

      if(method.getInstructionList() == null)
          return 0;

      //org.apache.bcel.generic.Instruction[] instrs =
      //method.getInstructionList().getInstructions();
      java.util.Iterator instrs = method.getInstructionList().iterator();

      //for (int i=0;i<instrs.length;i++){
      while(instrs.hasNext()){
          if(((org.apache.bcel.generic.InstructionHandle)instrs.next()).getInstruction()
             instanceof org.apache.bcel.generic.IfInstruction)
              count++;
      }
      return count;
   }


   public static String[] getMethodNames(sandmark.program.Class clazz)
   {
      sandmark.program.Method methods[] = clazz.getMethods();
      String[] names = new String[methods.length];

      for (int i=0;i<methods.length;i++)
         names[i] = methods[i].getName();
      return names;
   }



   public static int getApplicationCallCount(sandmark.program.Method method)
   {
      return getNamesOfMethodsInvoked(method).length;
   }


   public static String[] getNamesOfMethodsInvoked(sandmark.program.Method method)
   {
      java.util.ArrayList names = new java.util.ArrayList();

      if(method.getInstructionList() == null)
          return new String[]{};

      java.util.Iterator instrs =
          method.getInstructionList().iterator();

      org.apache.bcel.generic.ConstantPoolGen cpg = method.getEnclosingClass().getConstantPool();
      sandmark.program.Application app = method.getEnclosingClass().getApplication();

      //for (int i=0;i<instrs.length;i++){
      while(instrs.hasNext()){
          org.apache.bcel.generic.Instruction inst =
              ((org.apache.bcel.generic.InstructionHandle)
               instrs.next()).getInstruction();
         if (inst instanceof org.apache.bcel.generic.InvokeInstruction){
            org.apache.bcel.generic.InvokeInstruction invoke =
               (org.apache.bcel.generic.InvokeInstruction)inst;


            String methodName = invoke.getMethodName(cpg);
            sandmark.program.Class parent = app.getClass(invoke.getClassName(cpg));
            // if the method is in a non-application class, then parent will be null (right?)
            // kheffner - no it will be an instance of LibraryClass

            if (!(methodName.equals("<init>") || parent instanceof sandmark.program.LibraryClass))
               names.add(methodName);
         }
      }

      return (String[])names.toArray(new String[0]);
   }


   public static int getNumberOfScalarLocals(sandmark.program.Method method)
   {
      int count=0;
      if(method.isInterface() || method.isAbstract() || method.isNative())
          return 0;
      sandmark.analysis.interference.InterferenceGraph ifg = method.getIFG();
      for (java.util.Iterator nodes=ifg.nodes(); nodes.hasNext(); ){
         sandmark.analysis.defuse.DUWeb web =
            (sandmark.analysis.defuse.DUWeb)nodes.next();
         if (web.getType() instanceof org.apache.bcel.generic.BasicType){
            count++;
         }
      }
      return count;
   }


   public static int getNumberOfVectorLocals(sandmark.program.Method method)
   {
      int count=0;
      if(method.isInterface() || method.isAbstract() || method.isNative())
          return 0;
      sandmark.analysis.interference.InterferenceGraph ifg = method.getIFG();
      for (java.util.Iterator nodes=ifg.nodes(); nodes.hasNext(); ){
         sandmark.analysis.defuse.DUWeb web =
            (sandmark.analysis.defuse.DUWeb)nodes.next();
         if (web.getType() instanceof org.apache.bcel.generic.ArrayType){
            count++;
         }
      }
      return count;
   }


   public static int[] getMethodVectorDimensions(sandmark.program.Method method)
   {
      java.util.Vector dims = new java.util.Vector(10);

      sandmark.analysis.interference.InterferenceGraph ifg = method.getIFG();
      for (java.util.Iterator nodes=ifg.nodes(); nodes.hasNext(); ){
         sandmark.analysis.defuse.DUWeb web =
            (sandmark.analysis.defuse.DUWeb)nodes.next();
         org.apache.bcel.generic.Type mytype = web.getType();
         if (mytype instanceof org.apache.bcel.generic.ArrayType){
            dims.add(new Integer(((org.apache.bcel.generic.ArrayType)mytype).getDimensions()));
         }
      }
      int[] dimensions = new int[dims.size()];
      for (int i=0;i<dims.size();i++)
         dimensions[i] = ((Integer)dims.get(i)).intValue();
      return dimensions;
   }


   // we define "hierarchy level" to be the length of the path from java.lang.Object to me
   public static int getClassHierarchyLevel(sandmark.program.Class clazz)
   {
      return clazz.getSuperClasses().length;
   }


   public static int getNumberOfSubClasses(sandmark.program.Class clazz)
   {
      sandmark.analysis.classhierarchy.ClassHierarchy ch = clazz.getApplication().getHierarchy();
      return ch.subClasses(clazz).length-1;
   }

   public static int getNumberOfSuperClasses(sandmark.program.Class clazz)
   {
      return clazz.getSuperClasses().length;
   }


   public static java.util.Vector getAllClassNames(sandmark.program.Application app)
   {
      sandmark.program.Class classes[] = app.getClasses();

      java.util.Vector classNames = new java.util.Vector(classes.length);
      for(int i=0;i<classes.length;i++)
         classNames.add(classes[i].getName());
      return classNames;
   }


   // returns the names of all the methods this class owns that were added by a superclass in the same application.
   // i.e. java methods are not counted and overridden methods are not counted
   public static java.util.Set getApplicationMethodsInherited(sandmark.program.Class clazz)
   {
      sandmark.program.Class superclass = clazz.getSuperClass();

      sandmark.program.Application app = clazz.getApplication();

      if (superclass == null || app.getClass(superclass.getName())==null)
         return new java.util.HashSet();

      java.util.Set methods = getApplicationMethodsInherited(superclass);

      sandmark.program.Method[] supermethods = superclass.getMethods();
      for (int i=0;i<supermethods.length;i++){
         if (!supermethods[i].isPrivate())
            methods.add(supermethods[i].getName()+supermethods[i].getSignature());
      }

      return methods;
   }


   public static int getNumberOfTotalPublicMethods(sandmark.program.Application app)
   {
      int publicCount=0;
      for (java.util.Iterator classiter = app.classes(); classiter.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)classiter.next();
         sandmark.program.Method[] methods = clazz.getMethods();
         for(int m=0; m<methods.length; m++)
            if(methods[m].isPublic())
               publicCount++;
      }
      return publicCount;
   }



   // hashes Instruction.getName() Strings to an Integer count of that kind of instruction within this method
   public static java.util.Hashtable getByteCodeUsage(sandmark.program.Method method)
   {
      java.util.Hashtable usage = new java.util.Hashtable();

      if(method.getInstructionList() == null)
          return usage;

      //org.apache.bcel.generic.Instruction[] instrs =
      //method.getInstructionList().getInstructions();
      java.util.Iterator instrs = method.getInstructionList().iterator();

      //for (int i=0;i<instrs.length;i++){
      while(instrs.hasNext()){
          org.apache.bcel.generic.Instruction instruction =
              ((org.apache.bcel.generic.InstructionHandle)instrs.next()).getInstruction();
          Integer count = (Integer)usage.get(instruction.getName());
          if (count==null){
              usage.put(instruction.getName(), new Integer(1));
          }else{
              usage.put(instruction.getName(), new Integer(count.intValue()+1));
          }
      }
      return usage;
   }


   // opcode is the result of Instruction.getName()
   public static int getNumberOfOpcodesInMethod(sandmark.program.Method method, String opcode)
   {
      java.util.Hashtable hash = getByteCodeUsage(method);
      Integer count = (Integer)hash.get(opcode);
      if (count==null) return 0;
      else return count.intValue();
   }


   // opcode is the result of Instruction.getName()
   public static int getNumberOfOpcodesInClass(sandmark.program.Class clazz, String opcode)
   {
      sandmark.program.Method[] methods = clazz.getMethods();
      int number = 0;
      for (int i=0;i<methods.length;i++){
         number += getNumberOfOpcodesInMethod(methods[i], opcode);
      }
      return number;
   }


   // opcode is the result of Instruction.getName()
   public static int getNumberOfOpcodesInPackage(sandmark.program.Application app, String packageName, String opcode)
   {
      java.util.Vector list = getListOfClassesByPackageName(app, packageName);
      int number = 0;
      for (java.util.Iterator classiter = list.iterator(); classiter.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)classiter.next();
         number += getNumberOfOpcodesInClass(clazz, opcode);
      }
      return number;
   }

   public static int getMethodSizeInBytes(sandmark.program.Method method)
   {
       if(method.getInstructionList() == null)
           return 0;

       //org.apache.bcel.generic.Instruction instrs[] = method.getInstructionList().getInstructions();
       java.util.Iterator instrs = method.getInstructionList().iterator();

       int iSize=0;
       //for(int k=0;k<instrs.length;k++)
       while(instrs.hasNext()){
           org.apache.bcel.generic.Instruction instruction =
               ((org.apache.bcel.generic.InstructionHandle)instrs.next()).getInstruction();
           iSize += instruction.getLength();
       }
       return iSize;
   }

   // this method calculates the number of public instance methods in the class
   public static int getNumberOfPublicMethods(sandmark.program.Class clazz)
   {
      int numPublic = 0;
      sandmark.program.Method[] methods = clazz.getMethods();
      for (int i=0;i<methods.length;i++)
         if (methods[i].isPublic())
            numPublic++;
      return numPublic;
   }


   // this method calculates the number of instance methods in the class
   public static int getNumberOfInstanceMethods(sandmark.program.Class clazz)
   {
      int numInstance=0;
      sandmark.program.Method[] methods = clazz.getMethods();
      for (int i=0;i<methods.length;i++){
         if (!methods[i].isStatic())
            numInstance++;
      }
      return numInstance;
   }


   // this method calculates the number of instance variables in a class
   public static int getNumberOfInstanceVariables(sandmark.program.Class clazz)
   {
      sandmark.program.Field[] fields = clazz.getFields();
      int numFields=0;
      for (int i=0;i<fields.length;i++)
         if (!fields[i].isStatic())
            numFields++;
      return numFields;
   }


   public static int getNumberOfAbstractClassesInPackage(sandmark.program.Application app, String packageName)
   {
      java.util.Vector list = getListOfClassesByPackageName(app, packageName);
      int numAbstract=0;
      for (java.util.Iterator classiter = list.iterator();classiter.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)classiter.next();
         if (clazz.isAbstract())
            numAbstract++;
      }
      return numAbstract;
   }


   public static int getNumberOfMethodsOverridden(sandmark.program.Class clazz)
   {
      sandmark.program.Class[] superclasses = clazz.getSuperClasses();
      java.util.HashSet hash = new java.util.HashSet();
      for (int i=0;i<superclasses.length;i++){
         sandmark.program.Method[] supermethods = superclasses[i].getMethods();
         for (int j=0;j<supermethods.length;j++){
            hash.add(supermethods[j].getName()+supermethods[j].getSignature());
         }
      }
      int overrides=0;
      sandmark.program.Method[] methods = clazz.getMethods();

      for (int i=0;i<methods.length;i++){
         if (hash.contains(methods[i].getName()+methods[i].getSignature()))
            overrides++;
      }
      return overrides;
   }
}
