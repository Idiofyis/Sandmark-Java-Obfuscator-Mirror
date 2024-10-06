package sandmark.obfuscate.nodesplitter;

public class NodeSplitter extends sandmark.obfuscate.AppObfuscator {
   private static final boolean DEBUG = false;
   
   // create a new name for the pointer field (will be "next"+someInt)
   private static String getPointerFieldName
      (sandmark.program.Class origClass,sandmark.program.Class splitClass) {
      String name;
      for(int i = 0 ; origClass.containsField
             (name = "next" + i,splitClass.getType().getSignature()) != null; i++){}
        
      return name;
   }

   // create a new name for the split class (will be oldName+someInt)
   private static String getSplitClassName(sandmark.program.Class cls) {
      String clsName = cls.getName();
      sandmark.program.Application app = cls.getApplication();
      String name;
      for(int suffix = 0 ; app.getClass((name = clsName + suffix)) != null; suffix++){}
      return name;
   }

   private static sandmark.program.Class createSplitClass
      (sandmark.program.Class cls,java.util.Hashtable movedFields) {

      String interfaces[] = cls.getInterfaceNames();
      boolean serializable = false;
      for (int i = 0; !serializable && i < interfaces.length; i++)
         if ("java.io.Serializable".equals(interfaces[i]))
            serializable = true;

      String newInterfaces[] = (serializable) ? new String[] {"java.io.Serializable"} : new String[0];

      String splitClassName = getSplitClassName(cls);
      sandmark.program.Class splitClass = 
         new sandmark.program.LocalClass(cls.getApplication(),
                                         splitClassName,
                                         "java.lang.Object",
                                         cls.getFileName(),
                                         cls.getAccessFlags() & 
                                         (~org.apache.bcel.Constants.ACC_ABSTRACT),
                                         newInterfaces);
      
      if(DEBUG)
         System.out.println("split class is " + splitClass.getName());

      sandmark.program.Field fields[] = cls.getFields();

      java.util.Random r = sandmark.util.Random.getRandom();
      boolean needStaticField = false;
      for (int i = 0 ; i < fields.length ; i++) {
         if (true) {//r.nextDouble() > 0.5) {
            if (fields[i].isStatic())
               needStaticField = true;
            sandmark.program.Field copy = new sandmark.program.LocalField
               (splitClass,fields[i].getAccessFlags(),fields[i].getType(),
                fields[i].getName());
            movedFields.put(new sandmark.util.FieldID(fields[i]),
                            new sandmark.util.FieldID(copy));
            if(copy.isFinal()) {
               org.apache.bcel.classfile.Attribute attrs[] =
                  fields[i].getAttributes();
               for(int j = 0 ; j < attrs.length ; i++)
                  if(attrs[i] instanceof 
                     org.apache.bcel.classfile.ConstantValue)
                     copy.addAttribute(attrs[i]);
            }
            if(DEBUG)
               System.out.println("added field " + copy.getName() + 
                                  " to " + splitClass.getName());
         }
      }

      splitClass.addEmptyConstructor(org.apache.bcel.Constants.ACC_PUBLIC);

      // If we're moving a static field, we need to make sure the 
      // original class's <clinit> is called before any field in the
      // new class is accessed.  So we add a static field to the original
      // class and set it from the new class's <clinit>, thus invoking
      // the original class's <clinit>.
      if (needStaticField) {
         String name = "dummyfield";
         org.apache.bcel.generic.Type type =
            org.apache.bcel.generic.Type.BOOLEAN;
         int suffix = 0;
         while (cls.containsField(name + suffix,type.getSignature()) != null)
            suffix++;
         name = name + suffix;
         int access_flags = org.apache.bcel.Constants.ACC_STATIC
            | org.apache.bcel.Constants.ACC_PUBLIC;
         new sandmark.program.LocalField(cls, access_flags, type, name);
         type = org.apache.bcel.generic.Type.VOID;
         org.apache.bcel.generic.InstructionList il =
            new org.apache.bcel.generic.InstructionList();
         int index = splitClass.getConstantPool().addFieldref(cls.getName(), name, "Z");
         il.append(new org.apache.bcel.generic.ICONST(1));
         il.append(new org.apache.bcel.generic.PUTSTATIC(index));
         il.append(new org.apache.bcel.generic.RETURN());
         new sandmark.program.LocalMethod(splitClass, access_flags, type,
                                          null, null, "<clinit>", il);
      }

      return splitClass;
   }

   
   // adds the pointer field to the original class, and updates the
   // constructors to set it
   private static sandmark.program.Field addPointerField
      (sandmark.program.Class origClass,
       sandmark.program.Class splitClass) {

      String fieldName = getPointerFieldName(origClass,splitClass);
      org.apache.bcel.generic.ObjectType type = splitClass.getType();
      sandmark.program.Field pointerField = new sandmark.program.LocalField
         (origClass,org.apache.bcel.Constants.ACC_PUBLIC,type,fieldName);
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory
         (origClass.getConstantPool());

      
      // create the new instructions to go in each <init>
      org.apache.bcel.generic.InstructionList ilnew = 
         new org.apache.bcel.generic.InstructionList();
      ilnew.append(new org.apache.bcel.generic.ALOAD(0));
      ilnew.append
         (factory.createGetField
          (origClass.getName(),pointerField.getName(),
           pointerField.getType()));
      org.apache.bcel.generic.BranchHandle branch =
         ilnew.append(new org.apache.bcel.generic.IFNONNULL(null));
      ilnew.append(new org.apache.bcel.generic.ALOAD(0));
      ilnew.append(factory.createNew(splitClass.getType()));
      ilnew.append(new org.apache.bcel.generic.DUP());
      ilnew.append
         (factory.createInvoke
          (splitClass.getName(),"<init>",org.apache.bcel.generic.Type.VOID,
           org.apache.bcel.generic.Type.NO_ARGS,
           org.apache.bcel.Constants.INVOKESPECIAL));
      ilnew.append
         (factory.createPutField
          (origClass.getName(),pointerField.getName(),
           pointerField.getType()));
      branch.setTarget(ilnew.append(new org.apache.bcel.generic.NOP()));


      // for each <init> method...
      for(java.util.Iterator methods = origClass.methods(); methods.hasNext(); ) {
         sandmark.program.Method method = 
            (sandmark.program.Method)methods.next();
         if(!method.getName().equals("<init>"))
            continue;
         sandmark.analysis.stacksimulator.StackSimulator ss =
            method.getStack();
         org.apache.bcel.generic.InstructionList il = 
            method.getInstructionList();


         // for each <init> method, go through the instructions and find the 
         // super-constructor calls, and copy them into a list
         java.util.ArrayList constructors = new java.util.ArrayList();
         for(org.apache.bcel.generic.InstructionHandle ih = il.getStart() ; 
             ih != null ; ih = ih.getNext()) {
            if(!(ih.getInstruction() instanceof 
                 org.apache.bcel.generic.INVOKESPECIAL))
               continue;
            org.apache.bcel.generic.INVOKESPECIAL inv =
               (org.apache.bcel.generic.INVOKESPECIAL)ih.getInstruction();
            if(!inv.getName(method.getConstantPool()).equals("<init>"))
               continue;
            org.apache.bcel.generic.Type argTypes[] = 
               inv.getArgumentTypes(method.getConstantPool());
            sandmark.analysis.stacksimulator.Context cx =
               ss.getInstructionContext(ih);
            sandmark.analysis.stacksimulator.StackData sd[] =
               cx.getStackAt(argTypes.length);
            if(sd[0].getInstruction().getInstruction().equals
               (org.apache.bcel.generic.InstructionConstants.ALOAD_0))
               constructors.add(ih);
         }

         if(constructors.size() == 0)
            throw new Error("didn't find super-constructor invoke.  " +
                            "resulting app WILL crash");
	    
         // for each super.<init> call, add the new instructions after it
         for(java.util.Iterator cons = constructors.iterator() ; 
             cons.hasNext() ; ) {
            org.apache.bcel.generic.InstructionHandle ih = 
               (org.apache.bcel.generic.InstructionHandle)cons.next();
            il.append(ih,ilnew.copy());
         }

         method.setMaxStack();

      }
      return pointerField;
   }


   private static void adjustReferences
      (sandmark.program.Application app,
       sandmark.analysis.classhierarchy.ClassHierarchy ch,
       java.util.Hashtable movedFields,java.util.Hashtable pointerFields) {

      for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();

         for(java.util.Iterator methods = cls.methods(); methods.hasNext(); ) {
            sandmark.program.Method method = 
               (sandmark.program.Method)methods.next();
            if(method.getInstructionList() == null)
               continue;
	    
            org.apache.bcel.generic.InstructionFactory factory = 
               new org.apache.bcel.generic.InstructionFactory
               (method.getConstantPool());
            if(DEBUG)
               System.out.println("adjusting " + cls.getName() + "." + 
                                  method.getName());

            boolean changed = false;
            for(org.apache.bcel.generic.InstructionHandle ih = 
                   method.getInstructionList().getStart(); ih != null; ih = ih.getNext()) {

               org.apache.bcel.generic.Instruction curr = ih.getInstruction();
               if(!(curr instanceof org.apache.bcel.generic.FieldInstruction))
                  continue;
               // only updating field instructions

               org.apache.bcel.generic.FieldInstruction insn =
                  (org.apache.bcel.generic.FieldInstruction)curr;
               sandmark.util.FieldID fid = null,newFID = null;
               try {
                  fid = new sandmark.util.FieldID
                     (ch.resolveFieldReference
                      (new sandmark.util.FieldID
                       (insn.getName(method.getConstantPool()),
                        insn.getSignature(method.getConstantPool()),
                        insn.getClassName(method.getConstantPool())),
                       cls));
                  newFID = 
                     (sandmark.util.FieldID)movedFields.get(fid);
               } catch(sandmark.analysis.classhierarchy.ClassHierarchyException e) {
               }
               if(newFID == null)
                  continue;

               // if the field mentioned has been moved to the split class...

               changed = true;
               sandmark.program.Field pointerField = 
                  (sandmark.program.Field)pointerFields.get(fid.getClassName());
               if(pointerField == null)
                  throw new Error("no pointer field for modified class " + 
                                  fid.getClassName());
               org.apache.bcel.generic.Instruction loadPointer =
                  factory.createGetField
                  (pointerField.getEnclosingClass().getName(),
                   pointerField.getName(),pointerField.getType());
               org.apache.bcel.generic.Instruction getMovedField =
                  factory.createGetField
                  (newFID.getClassName(),newFID.getName(),
                   org.apache.bcel.generic.Type.getType
                   (newFID.getSignature()));
               org.apache.bcel.generic.Instruction putMovedField =
                  factory.createPutField
                  (newFID.getClassName(),newFID.getName(),
                   org.apache.bcel.generic.Type.getType
                   (newFID.getSignature()));
               org.apache.bcel.generic.Instruction getStaticMovedField =
                  factory.createGetStatic
                  (newFID.getClassName(),newFID.getName(),
                   org.apache.bcel.generic.Type.getType
                   (newFID.getSignature()));
               org.apache.bcel.generic.Instruction putStaticMovedField =
                  factory.createPutStatic
                  (newFID.getClassName(),newFID.getName(),
                   org.apache.bcel.generic.Type.getType
                   (newFID.getSignature()));
	       
               if(DEBUG)
                  System.out.println("screwing around with " + 
                                     ih.getPosition() + ": " +
                                     insn.toString(cls.getConstantPool().getConstantPool()));
               org.apache.bcel.generic.InstructionList il =
                  method.getInstructionList();

               if (insn instanceof org.apache.bcel.generic.GETSTATIC) {
                  ih.setInstruction(getStaticMovedField);
               } else if (insn instanceof org.apache.bcel.generic.GETFIELD) {
                  org.apache.bcel.generic.InstructionHandle newIH =
                     il.insert(insn, loadPointer);
                  ih.setInstruction(getMovedField);
                  il.redirectBranches(ih, newIH);
                  redirectHandlers(method, ih, newIH, ih);
               } else if (insn instanceof org.apache.bcel.generic.PUTSTATIC) {
                  ih.setInstruction(putStaticMovedField);
               } else if (insn instanceof org.apache.bcel.generic.PUTFIELD) {
                  org.apache.bcel.generic.InstructionHandle newIH = null;
                  org.apache.bcel.generic.Type fieldType = 
                     org.apache.bcel.generic.Type.getType(fid.getSignature());
                  if (fieldType.getSize() == 2) {
                     // oldref, value1, value2
                     newIH =
                        il.insert(insn, new org.apache.bcel.generic.DUP2_X1());
                     // value1, value2, oldref, value1, value2
                     il.insert(insn, new org.apache.bcel.generic.POP2());
                     // value1, value2, oldref
                     il.insert(insn, loadPointer);
                     // value1, value2, newref
                     il.insert(insn, new org.apache.bcel.generic.DUP_X2());
                     // newref, value1, value3, newref
                     il.insert(insn, new org.apache.bcel.generic.POP());
                     // newref, value1, value2
                  } else {
                     // oldref, value
                     newIH =
                        il.insert(insn, new org.apache.bcel.generic.SWAP());
                     // value, oldref
                     il.insert(insn, loadPointer);
                     // value, newref
                     il.insert(insn, new org.apache.bcel.generic.SWAP());
                     // newref, value
                  }
                  ih.setInstruction(putMovedField);
                  il.redirectBranches(ih, newIH);
                  redirectHandlers(method, ih, newIH, ih);
               } else
                  throw new java.lang.RuntimeException("don't know what to do");

            }

            if (changed) {
               method.removeLineNumbers();
               method.removeLocalVariables();
               method.setMaxStack();
            }
         }
      }
   }

   private static void redirectHandlers(sandmark.program.Method method, 
                                        org.apache.bcel.generic.InstructionHandle old,
                                        org.apache.bcel.generic.InstructionHandle top,
                                        org.apache.bcel.generic.InstructionHandle bottom){

      org.apache.bcel.generic.CodeExceptionGen[] handlers = 
         method.getExceptionHandlers();

      for (int i=0;i<handlers.length;i++){
         if (handlers[i].getStartPC()==old)
            handlers[i].setStartPC(top);
         if (handlers[i].getEndPC()==old)
            handlers[i].setEndPC(bottom);
         if (handlers[i].getHandlerPC()==old)
            handlers[i].setHandlerPC(top);
      }
   }

   public String getShortName()
   {
      return "Split Classes";
   }

   public String getLongName() {
      return "Split this class into two classes";
   }

   public java.lang.String getAlgHTML()
   {
      return
         "<HTML><BODY>" +
         "NodeSplitter is a class obfuscator." +
         " The algorithm splits a class into two classes." +
         " This is an effective attack against the CT watermarking algorithm." +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:prabhu@cs.arizona.edu\">Rathna Prabhu</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/nodesplitter/doc/help.html";
   }

   public java.lang.String getAuthor(){
      return "Rathna Prabhu";
   }

   public java.lang.String getAuthorEmail(){
      return "prabhu@cs.arizona.edu";
   }

   public java.lang.String getDescription(){
      return
         "Split a class such that every object becomes two " +
         "objects which are linked together on a bogus field. " +
         "This is an effective attack on the CT watermarking " +
         "algorithm";
   }

   public sandmark.config.ModificationProperty[] getMutations()
   {
      return new sandmark.config.ModificationProperty[]{};
   }

   public void apply(sandmark.program.Application app) throws Exception {
      new sandmark.util.Publicizer().apply(app);

      java.util.Hashtable movedFields = new java.util.Hashtable();
      java.util.Hashtable pointerFields = new java.util.Hashtable();
      for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
         sandmark.program.Class cls = (sandmark.program.Class)classes.next();
         if (cls.isInterface())
            continue;

         if(DEBUG)
            System.out.println("Splitting " + cls.getName());

         sandmark.program.Class splitClass = createSplitClass(cls,movedFields);
         sandmark.program.Field pointerField = addPointerField(cls,splitClass);
         pointerFields.put(cls.getName(),pointerField);
      }

      sandmark.analysis.classhierarchy.ClassHierarchy ch = 
         new sandmark.analysis.classhierarchy.ClassHierarchy(app);
      adjustReferences(app,ch,movedFields,pointerFields);

      for(java.util.Iterator fields = movedFields.keySet().iterator() ; 
          fields.hasNext() ; ) {
         sandmark.util.FieldID fid = (sandmark.util.FieldID)fields.next();
         sandmark.program.Field field = 
            app.getClass(fid.getClassName()).getField
            (fid.getName(),fid.getSignature());
         if(DEBUG)
            System.out.println("removing field " + field.getName() + 
                               " from " + field.getEnclosingClass().getName());
         field.getEnclosingClass().removeField(field);
      }
   }


   public static void main(String args[]) throws Throwable{
      if (args.length<1) return;

      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);

      new NodeSplitter().apply(app);

      app.save(args[0]+".out");
   }
}

