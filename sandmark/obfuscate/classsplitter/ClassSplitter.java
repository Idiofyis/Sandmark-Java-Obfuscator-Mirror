/***********************************************************
 *  ClassSplitting Algorithm  :                             *
 ************************************************************
 *  Idea Suggested by :                                     *
 *  ==================                                      *
 *  Dr.Christian Collberg                                   *
 *  collberg@cs.arizona.edu                                 *
 *                                                          *
 *  Date : 4 May 2002                                       *
 *  -----------------                                       *
 *  Ashok Purushotham       &&      RathnaPrabhu            *
 *  ashok@cs.arizona.edu            prabhu@cs.arizona.edu   *
 ************************************************************/

package sandmark.obfuscate.classsplitter;

/********************************************************************
 *   To  be brief ,the splitting technique splits at the class level.*
 * A class C is broken into classes C 1 , C 2 ...C n -1 and C ,such  *
 * that C 2 inherits from C 1 ... and C inherits from C n-1. C 1 has *
 * fields and methods that only refer to themselves, whereas C 2 has *
 * fields and methods that can refer to themselves as well as fields *
 * and methods in C1 .                                               *
 *********************************************************************/

public class ClassSplitter extends sandmark.obfuscate.ClassObfuscator {
   private static boolean DEBUG = false;

   public void apply(sandmark.program.Class cls) throws Exception {
      // do not apply to classes that implement any interfaces

      if (cls.isInterface())
         return;
      if(cls.getInterfaces().length > 0)
         return;
      
      new sandmark.util.Publicizer().apply(cls.getApplication());
      // make all methods, fields, and classes public (and nonfinal)

      sandmark.analysis.dependencygraph.DependencyGraph depGraph = 
         new sandmark.analysis.dependencygraph.DependencyGraph
         (java.util.Arrays.asList(cls.getFields()),java.util.Arrays.asList(cls.getMethods()));

      java.util.ArrayList topoLevels = topoLevelSort(depGraph,cls);
      // the number of new classes we split cls into will be equal to topoLevels.size()

      if(topoLevels.size() > 1) {
         String superClass = cls.getSuperclassName();
         boolean makeabstract = cls.getSuperClass().isAbstract() || cls.isAbstract();
         for(int i = 0 ; i < topoLevels.size() ; i++) {
            sandmark.program.Class trimableClass = cls;
            if(i != topoLevels.size()-1) {
               trimableClass = trimableClass.copy();
               trimableClass.setAbstract(makeabstract);
               trimableClass.setFinal(false);
            }
            trimableClass.setSuperclassName(superClass);
            int superindex = trimableClass.getConstantPool().addClass(superClass);
            trimableClass.setSuperclassNameIndex(superindex);
            superClass = trimableClass.getName();

            trimClass(trimableClass,(java.util.Hashtable)topoLevels.get(i));
            
            if(i != topoLevels.size()-1) {
               fixupMethods(trimableClass,cls);
               addPassthroughConstructors(trimableClass);
            }

            // XXXash:  this is a hack to workaround addEmptyConstructor sometimes
            // calling the wrong parent constructor.  It should be called only on
            // the original class, whose constructor really does need to be changed
            // to call the constructor of its new superclass

            if (i==topoLevels.size()-1)
               fixupConstructors(trimableClass);
         }
      }
   }

   /** Returns a topological sort of the dependency graph, filtering out the 'pegged' 
    *  members. 
    *  The actual innards of the returned list are hashtables which hash
    *  the name/type of the members to the members themselves. The order of hashtables
    *  in the list is such that everything in a given table has all of its DG successors 
    *  in an earlier (or the same) list. Because of the filtering, pegged members and methods pointing to
    *  pegged members will be in the last hashtable. This method will destroy the given DG.
    */
   private java.util.ArrayList topoLevelSort(sandmark.util.newgraph.MutableGraph depGraph,
                                             sandmark.program.Class clazz) {
      java.util.ArrayList topoLevels = new java.util.ArrayList();
      java.util.Hashtable listItems = new java.util.Hashtable();
      java.util.Iterator nodeIt;
      java.util.Hashtable level;
      java.util.Hashtable peggedObjects = findPeggedObjects(clazz);

      for(boolean progress = true ; progress ; ) {
         for(nodeIt = depGraph.nodes(),progress = false,
                level = new java.util.Hashtable();  nodeIt.hasNext(); ) {

            sandmark.program.Object node = (sandmark.program.Object)nodeIt.next();
            boolean pegged = (peggedObjects.get(node) != null);

            if(!pegged && tableContainsItems(listItems,depGraph.succs(node))) {
               // add member if it is not pegged and all of its 
               // successors are already in the table

               depGraph.removeNode(node);
               level.put(fieldOrMethodKey(node),node);
               listItems.put(node,node);
               progress = true;
            }
         }
         if(progress)
            topoLevels.add(level);
      }

      // everything else must be in the last level, by definition (even pegged things???)
      if(depGraph.nodeCount() > 0) {
         java.util.Hashtable lastLevel = new java.util.Hashtable();
         for(nodeIt = depGraph.nodes() ; nodeIt.hasNext() ; ) {
            Object o = nodeIt.next();
            lastLevel.put(fieldOrMethodKey((sandmark.program.Object)o),o);
         }
         topoLevels.add(lastLevel);
      }

      return topoLevels;
   }


   /** Returns a hashtable full of all the 'pegged' methods and fields in this class.
    *  A pegged member is one of: <br>
    *    a method that returns an instance of its enclosing class,<br>
    *    a method that takes an instance of its enclosing class as a parameter, <br>
    *    a static method, <br>
    *    an abstract method, <br>
    *    a native method, <br>
    *    a constructor, <br>
    *    a static field, <br>
    *    or a field of the same type as its enclosing class.<br>
    *  Pegged members cannot leave the original class. They must not be moved to one
    *  of the new superclasses.
    */
   private java.util.Hashtable findPeggedObjects(sandmark.program.Class clazz) {
      org.apache.bcel.generic.Type classType =  org.apache.bcel.generic.Type.getType
         ("L" + clazz.getName().replace('.','/') + ";");

      java.util.Hashtable peggedObjects = new java.util.Hashtable();

      for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
         sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
         boolean pegged = false;
         if(method.getReturnType().equals(classType))
            pegged = true;
         for(int j = 0 ; j < method.getArgumentTypes().length ; j++)
            if(method.getArgumentType(j).equals(classType))
               pegged = true;
         if(method.isStatic() || method.getName().equals("<init>") || method.isNative() || method.isAbstract())
            pegged = true;
         if(pegged) {
            peggedObjects.put(method,method);
         }
      }
      for(java.util.Iterator fieldIt = clazz.fields() ; fieldIt.hasNext() ; ) {
         sandmark.program.Field field = (sandmark.program.Field)fieldIt.next();
         if(field.isStatic() || field.getConstantValue()!=null || field.getName().equals("this$0")
            || field.getType().equals(classType)) {

            peggedObjects.put(field,field);
         } else {
            field.setFinal(false);
         }
      }

      return peggedObjects;
   }


   /** For every method or field reference in copiedClass where oldClass 
    *  is the enclosing class of that field or method, change it so that 
    *  copiedClass is the new enclosing class.
    */
   private void fixupMethods(sandmark.program.Class copiedClass,
                             sandmark.program.Class oldClass){

      org.apache.bcel.generic.ConstantPoolGen cpg = copiedClass.getConstantPool();
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(cpg);


      for(java.util.Iterator methodIt = copiedClass.methods(); methodIt.hasNext(); ) {
         sandmark.program.Method method =
            (sandmark.program.Method)methodIt.next();
         if(method.getInstructionList() == null)
            continue;

         boolean makecast=false;

         org.apache.bcel.generic.InstructionHandle ihs[] =
            method.getInstructionList().getInstructionHandles();
         for(int i = 0 ; i < ihs.length ; i++) {
            if(ihs[i].getInstruction() instanceof org.apache.bcel.generic.FieldOrMethod) {
               org.apache.bcel.generic.FieldOrMethod fmi =
                  (org.apache.bcel.generic.FieldOrMethod)ihs[i].getInstruction();
               // for each field or method instruction...


               if(!fmi.getClassName(cpg).equals(oldClass.getName()))
                  continue;
               // only apply when the enclosing class is oldClass


               if(fmi instanceof org.apache.bcel.generic.INVOKEINTERFACE ||
                  fmi instanceof org.apache.bcel.generic.INVOKEVIRTUAL || 
                  fmi instanceof org.apache.bcel.generic.INVOKESPECIAL){
                  // reset all method invocations to be from copiedClass

                  org.apache.bcel.generic.InvokeInstruction ii =
                     (org.apache.bcel.generic.InvokeInstruction)fmi;

                  /*
                  if (!makecast){
                     // check to see if we will need to do our checkcast hack
                     org.apache.bcel.generic.Type[] argtypes = ii.getArgumentTypes(cpg);
                     for (int a=0;a<argtypes.length;a++){
                        if (argtypes[a].getSignature().equals("L"+oldClass.getName().replace('.','/')+";")){
                           makecast = true;
                           break;
                        }
                     }
                  }
                  */

                  if (!ii.getMethodName(cpg).equals("<init>")){
                     // only if this is not <init>

                     ihs[i].setInstruction
                        (factory.createInvoke
                         (copiedClass.getName(),
                          ii.getMethodName(cpg),
                          ii.getReturnType(cpg),
                          ii.getArgumentTypes(cpg),
                          ii.getOpcode()));
                  }
               }
            }
         }


         if (!method.getName().equals("<init>")){
            // if there was a method call in this method that took oldClass as a parameter, 
            // and this is not copiedClass's constructor...

            org.apache.bcel.generic.InstructionList ilist = 
               method.getInstructionList();
            ilist.insert(new org.apache.bcel.generic.ASTORE(0));
            ilist.insert(factory.createCheckCast((org.apache.bcel.generic.ReferenceType)
                                                 org.apache.bcel.generic.Type.getType("L"+oldClass.getName().replace('.', '/')+";")));
            ilist.insert(new org.apache.bcel.generic.ALOAD(0));

            // this hack just takes 'this' and casts it to oldClass, 
            // so that it can be safely passed to the method
         }
      }
   }


   /** For every constructor in this class, it changes every call to a superconstructor
    *  to be a call to the immediate superclass's superconstructor.
    */
   private void fixupConstructors(sandmark.program.Class clazz) {
      // There is a slight issue with this method. Consider a class B that extends A,
      // but in B's constructor it also makes another separate instance of A. Then B's <init>
      // method will contain 2 references to A.<init>, but only one should be changed to 
      // reflect the class splitting. We do dataflow analysis using the StackSimulator 
      // to deal with this.

      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory
         (clazz.getConstantPool());

      for(java.util.Iterator methodIt = clazz.methods(); methodIt.hasNext(); ) {
         sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
         if(!method.getName().equals("<init>"))
            continue;
         // for each constructor..

         org.apache.bcel.generic.InstructionHandle ihs[] =
            method.getInstructionList().getInstructionHandles();
         for(int i = 0 ; i < ihs.length ; i++) {
            if(ihs[i].getInstruction() instanceof org.apache.bcel.generic.INVOKESPECIAL) {
               org.apache.bcel.generic.InvokeInstruction ii =
                  (org.apache.bcel.generic.InvokeInstruction)ihs[i].getInstruction();


               if (ii.getMethodName(clazz.getConstantPool()).equals("<init>") && 
                   isSuperClass(ii.getClassName(clazz.getConstantPool()), clazz.getSuperClass())) {
                  // if the owner of this superconstructor is an ancestor of clazz..

                  // do dataflow analysis to see if this is the right init call
                  org.apache.bcel.generic.Type[] argtypes = 
                     ii.getArgumentTypes(clazz.getConstantPool());
                  // stack height of 'this' will be argtypes.length

                  sandmark.analysis.stacksimulator.StackData[] data = 
                     method.getStack().getInstructionContext(ihs[i]).getStackAt(argtypes.length);

                  if (data[0].getInstruction().getInstruction().equals(org.apache.bcel.generic.InstructionConstants.ALOAD_0)){
                     // this stack item came from a parameter (or 'this')
                     
                     // call the immediate superclass's constructor instead
                     ihs[i].setInstruction
                        (factory.createInvoke
                         (clazz.getSuperclassName(),
                          ii.getMethodName(clazz.getConstantPool()),
                          ii.getReturnType(clazz.getConstantPool()),
                          argtypes,
                          ii.getOpcode()));
                  }
               }
            }
         }
      }
   }



   /** Checks to see if superClassName is the name of an ancestor of 
    *  subClass (where a class is defined to be an ancestor of itself).
    */
   private boolean isSuperClass(java.lang.String superClassName,
                                sandmark.program.Class subClass) {
      if (subClass.getName().equals(superClassName))
         return true;

      if (subClass.getName().equals("java.lang.Object"))
         return false;

      return isSuperClass(superClassName, subClass.getSuperClass());
   }



   /** Returns true iff the given hashtable contains all the items in
    *  the given iterator as keys.
    */
   private boolean tableContainsItems(java.util.Hashtable table,
                                      java.util.Iterator itemIt) {
      while (itemIt.hasNext())
         if(!table.containsKey(itemIt.next()))
            return false;
      return true;
   }



   /** Returns a string name for the given sandmark Object. Either
    *  fieldname+fieldsig or methodname+methodsig.
    */
   private String fieldOrMethodKey(sandmark.program.Object obj) {
      if(obj instanceof sandmark.program.Field){
         sandmark.program.Field field = (sandmark.program.Field)obj;
         return (field.getName() + field.getSignature());
      }
      else if(obj instanceof sandmark.program.Method) {
         sandmark.program.Method method = (sandmark.program.Method)obj;
         return (method.getName() + method.getSignature());
      }
      throw new RuntimeException();
   }


   /***********************************************************************
    * trimclass trims the classfile's contents to hold only those fields  *
    * and methods that the topological sort of the DG asks it to have .   *
    ***********************************************************************/
   private void trimClass(sandmark.program.Class clazz,java.util.Hashtable fieldsAndMethods) {
      sandmark.program.Field fields[] = clazz.getFields();
      for(int i = 0 ; i < fields.length ; i++) {
         if(!fieldsAndMethods.containsKey(fieldOrMethodKey(fields[i]))) {
            fields[i].delete();
         }
      }

      sandmark.program.Method methods[] = clazz.getMethods();
      for(int i = 0 ; i < methods.length ; i++) {
         if(!fieldsAndMethods.containsKey(fieldOrMethodKey(methods[i]))) {
            methods[i].delete();
         }
      }
   }


   /** Removes all the constructors of a class and adds in new constructors
    *  that simply wrap the constructors of the immediate superclass.
    */
   public void addPassthroughConstructors(sandmark.program.Class clazz) {
      sandmark.program.Class superClass = clazz.getSuperClass();

      sandmark.program.Method methods[] = clazz.getMethods();
      for(int i = 0 ; i < methods.length ; i++)
         if(methods[i].getName().equals("<init>"))
            methods[i].delete();

      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory
         (clazz.getConstantPool());


      for(java.util.Iterator superMethods = superClass.methods() ;
          superMethods.hasNext() ; ) {
         sandmark.program.Method method =
            (sandmark.program.Method)superMethods.next();
         if(!method.getName().equals("<init>"))
            continue;

         org.apache.bcel.generic.Type argTypes[] = method.getArgumentTypes();
         org.apache.bcel.generic.InstructionList il =
            new org.apache.bcel.generic.InstructionList();
         il.append
            (org.apache.bcel.generic.InstructionFactory.createLoad
             (org.apache.bcel.generic.Type.OBJECT,0));
         for(int slot = 1,i = 0 ; argTypes != null && i < argTypes.length ;
             slot += argTypes[i].getSize(),i++) {
            il.append
               (org.apache.bcel.generic.InstructionFactory.createLoad
                (argTypes[i],slot));
         }
         il.append
            (factory.createInvoke
             (superClass.getName(),"<init>",method.getReturnType(),
              argTypes,org.apache.bcel.Constants.INVOKESPECIAL));
         il.append
            (org.apache.bcel.generic.InstructionFactory.createReturn
             (org.apache.bcel.generic.Type.VOID));
         sandmark.program.Method passthroughConstructor =
            new sandmark.program.LocalMethod
            (clazz,method.getAccessFlags(),method.getReturnType(),
             argTypes.length == 0 ? org.apache.bcel.generic.Type.NO_ARGS : 
	     argTypes,null,"<init>",il);
      }
   }

   /**
      Constructor
   */
   public ClassSplitter() {}

   public String getShortName() {
      return "Class Splitter";
   }

   public String getLongName() {
      return "Split this class into two classes";
   }

   public java.lang.String getAlgHTML() {
      return
         "<HTML><BODY>" +
         "ClassSplitter obfuscator splits at the class level. " +
         "A class C is broken into classes C 1 , C 2 ...C n -1 and C ,such " +
         "that C 2 inherits from C 1 ... and C inherits from C n-1. C 1 has " +
         "fields and methods that only refer to themselves, whereas C 2 has " +
         "fields and methods that can refer to themselves as well as fields " +
         "and methods in C1.\n" +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:ashok@cs.arizona.edu\">Ashok Purushotham</a> and " +
         "<a href = \"mailto:prabhu@cs.arizona.edu\">RathnaPrabhu</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public java.lang.String getAlgURL() {
      return "sandmark/obfuscate/classsplitter/doc/help_classsplitter.html";
   }

   public sandmark.config.ModificationProperty [] getMutations() {
      return null;
   }

   public sandmark.config.RequisiteProperty[] getPostprohibited(){
      return new sandmark.config.RequisiteProperty[]{
         new sandmark.config.AlgorithmProperty(this)
      };
   }

   public String getAuthor() {
      return "Ashok P. Ramasamy Venkatraj & Rathnaprabhu Rajendran";
   }

   public String getAuthorEmail() {
      return "ashok@cs.arizona.edu prabhu@cs.arizona.edu";
   }

   public String getDescription() {
      return "ClassSplitter splits a class in half by moving some " +
         "methods and fields to a superclass.";
   }



   public static void main(String args[]) throws Exception{
      if (args.length<1)
         return;
      
      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);

      ClassSplitter cs = new ClassSplitter();

      for (java.util.Iterator citer=app.classes(); citer.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
         
         cs.apply(clazz);
      }
      app.save(args[0]+".out");
   }

}
