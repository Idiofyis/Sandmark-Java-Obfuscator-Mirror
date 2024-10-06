package sandmark.obfuscate.ArrayObfuscation;

/** Caveat: 
 *    None of the split arrays can be passed to any
 *    java library method that attempts to use any array-specific instructions
 *    on them (because I can't patch them).
 */
public class ArraySplitter extends sandmark.obfuscate.AppObfuscator
   implements org.apache.bcel.Constants{

   private static final String SANDMARK_HASHHOLDER = "HashHolder";

   public String getShortName(){
      return "Array Splitter";
   }

   public String getLongName(){
      return "Array Splitter";
   }

   public String getAlgHTML(){
      return null;
   }

   public String getAlgURL(){
      return "sandmark/obfuscate/ArrayObfuscation/doc/help_splitter.html";
   }
   
   public String getAuthor(){
      return "Mike Stepp";
   }
   
   public String getAuthorEmail(){
      return "steppm@cs.arizona.edu";
   }

   public String getDescription(){
      return "Splits an array into 2 arrays, while preserving program semantics.";
   }
   
   public sandmark.config.ModificationProperty[] getMutations(){
      return null;
   }


   /*
     fix:
     putfield, putstatic, arraylength, aaload
     baload, caload, saload, daload, faload
     iaload, laload, aastore, bastore, castore
     sastore, dastore, fastore, iastore, lastore
   */
   private static void patchInstruction(org.apache.bcel.generic.InstructionHandle handle,
                                        sandmark.program.Method method,
                                        java.util.ArrayList arrayfields,
                                        java.util.ArrayList newfields,
                                        sandmark.analysis.classhierarchy.ClassHierarchy ch){

      sandmark.program.Class clazz = method.getEnclosingClass();
      sandmark.program.Application app = clazz.getApplication();

      org.apache.bcel.generic.ConstantPoolGen cpg = 
         clazz.getConstantPool();
      org.apache.bcel.generic.InstructionFactory factory = 
         new org.apache.bcel.generic.InstructionFactory(cpg);

      org.apache.bcel.generic.InstructionList ilist = 
         method.getInstructionList();


      switch(handle.getInstruction().getOpcode()){
      case PUTFIELD:{
         /*
           LDC <fieldname>
           INVOKESTATIC SandmarkHashHolder.putvar(Object, String)Object
           CHECKCAST <type>[]
           SWAP
           DUP_X1
           LDC <fieldname>
           INVOKESTATIC SandmarkHashHolder.getEnd(String)Object
           CHECKCAST <type>[]
           PUTFIELD <end>
           PUTFIELD <front>
         */
         org.apache.bcel.generic.PUTFIELD putfield = 
            (org.apache.bcel.generic.PUTFIELD)handle.getInstruction();

         sandmark.program.Field field = null;
         try{
            field = ch.resolveFieldReference(new sandmark.util.FieldID(putfield.getFieldName(cpg),
                                                                       putfield.getSignature(cpg),
                                                                       putfield.getClassName(cpg)),
                                             clazz);
         }catch(Throwable t){
            return;
         }
         if (field==null)
            return;

         int index=-1;
         if ((index=arrayfields.indexOf(field))!=-1){
            org.apache.bcel.generic.InstructionHandle ih = handle;
                     
            handle.setInstruction(factory.createConstant(clazz.getName()+"."+field.getName()+field.getSignature()));
            ih = ilist.append(ih, factory.createInvoke(SANDMARK_HASHHOLDER, "putvar",
                                                       org.apache.bcel.generic.Type.OBJECT,
                                                       new org.apache.bcel.generic.Type[]{
                                                          org.apache.bcel.generic.Type.OBJECT,
                                                          org.apache.bcel.generic.Type.STRING
                                                       },
                                                       org.apache.bcel.Constants.INVOKESTATIC));
            ih = ilist.append(ih, factory.createCheckCast((org.apache.bcel.generic.ReferenceType)field.getType()));
            ih = ilist.append(ih, org.apache.bcel.generic.InstructionConstants.SWAP);
            ih = ilist.append(ih, org.apache.bcel.generic.InstructionConstants.DUP_X1);
            ih = ilist.append(ih, factory.createConstant(clazz.getName()+"."+field.getName()+field.getSignature()));
            ih = ilist.append(ih, factory.createInvoke(SANDMARK_HASHHOLDER, "getEnd",
                                                       org.apache.bcel.generic.Type.OBJECT,
                                                       new org.apache.bcel.generic.Type[]{
                                                          org.apache.bcel.generic.Type.STRING
                                                       },
                                                       org.apache.bcel.Constants.INVOKESTATIC));
            ih = ilist.append(ih, factory.createCheckCast((org.apache.bcel.generic.ReferenceType)field.getType()));
            ih = ilist.append(ih, factory.createPutField(putfield.getClassName(cpg),
                                                         ((sandmark.program.Field)newfields.get(index)).getName(),
                                                         field.getType()));
            ih = ilist.append(ih, putfield);
         }
         break;
      }
                  
      case PUTSTATIC:{
         /*
           LDC <fieldname>
           INVOKESTATIC SandmarkHashHolder.putvar(Object, String)Object
           CHECKCAST <type>[]
           PUTSTATIC <front>
           LDC <fieldname>
           INVOKESTATIC SandmarkHashHolder.getEnd(String)Object
           CHECKCAST <type>[]
           PUTSTATIC <end>
         */
         org.apache.bcel.generic.PUTSTATIC putstatic = 
            (org.apache.bcel.generic.PUTSTATIC)handle.getInstruction();

         sandmark.program.Field field = null;
         try{
            field = ch.resolveFieldReference(new sandmark.util.FieldID(putstatic.getFieldName(cpg),
                                                                       putstatic.getSignature(cpg),
                                                                       putstatic.getClassName(cpg)),
                                             clazz);
         }catch(Throwable t){
            return;
         }
         if (field==null)
            return;

         int index=-1;
         if ((index=arrayfields.indexOf(field))!=-1){
            org.apache.bcel.generic.InstructionHandle ih = handle;
                     
            handle.setInstruction(factory.createConstant(clazz.getName()+"."+field.getName()+field.getSignature()));
            ih = ilist.append(ih, factory.createInvoke(SANDMARK_HASHHOLDER, "putvar",
                                                       org.apache.bcel.generic.Type.OBJECT,
                                                       new org.apache.bcel.generic.Type[]{
                                                          org.apache.bcel.generic.Type.OBJECT,
                                                          org.apache.bcel.generic.Type.STRING
                                                       },
                                                       org.apache.bcel.Constants.INVOKESTATIC));
            ih = ilist.append(ih, factory.createCheckCast((org.apache.bcel.generic.ReferenceType)field.getType()));
            ih = ilist.append(ih, putstatic);
            ih = ilist.append(ih, factory.createConstant(clazz.getName()+"."+field.getName()+field.getSignature()));
            ih = ilist.append(ih, factory.createInvoke(SANDMARK_HASHHOLDER, "getEnd",
                                                       org.apache.bcel.generic.Type.OBJECT,
                                                       new org.apache.bcel.generic.Type[]{
                                                          org.apache.bcel.generic.Type.STRING
                                                       },
                                                       org.apache.bcel.Constants.INVOKESTATIC));
            ih = ilist.append(ih, factory.createCheckCast((org.apache.bcel.generic.ReferenceType)field.getType()));
            ih = ilist.append(ih, factory.createPutStatic(putstatic.getClassName(cpg),
                                                          ((sandmark.program.Field)newfields.get(index)).getName(),
                                                          field.getType()));
         }
         break;
      }

                  
      case ARRAYLENGTH:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "arraylength",
                                                    org.apache.bcel.generic.Type.INT,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.OBJECT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;
                  
      case AALOAD:{
         sandmark.analysis.stacksimulator.Context context = 
            method.getStack().getInstructionContext(handle);

         if (context.getStackSize()<2)
            return;

         sandmark.analysis.stacksimulator.StackData[] data = 
            context.getStackAt(1);
         if (data==null)
            return;
         
         org.apache.bcel.generic.Type type = null;
         for (int i=0;type==null && i<data.length;i++){
            if (data[i].getType() instanceof org.apache.bcel.generic.ArrayType)
               type=data[i].getType();
         }
         if (!(type instanceof org.apache.bcel.generic.ArrayType))
            return;

         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "aaload",
                                                    org.apache.bcel.generic.Type.OBJECT,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.OBJECT,
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         ilist.append(handle, factory.createCheckCast((org.apache.bcel.generic.ReferenceType)
                                                      ((org.apache.bcel.generic.ArrayType)type).getElementType()));
         break;
      }
                  
      case BALOAD:{
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "baload",
                                                    org.apache.bcel.generic.Type.INT,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("Ljava/lang/Object;"),
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;
      }
                  
      case CALOAD:{
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "caload",
                                                    org.apache.bcel.generic.Type.CHAR,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[C"),
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;
      }
                  

      case DALOAD:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "daload",
                                                    org.apache.bcel.generic.Type.DOUBLE,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[D"),
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case FALOAD:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "faload",
                                                    org.apache.bcel.generic.Type.FLOAT,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[F"),
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case IALOAD:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "iaload",
                                                    org.apache.bcel.generic.Type.INT,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[I"),
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case LALOAD:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "laload",
                                                    org.apache.bcel.generic.Type.LONG,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[J"),
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case SALOAD:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "saload",
                                                    org.apache.bcel.generic.Type.SHORT,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[S"),
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

         ////////////////////////////////////////////////////////////////////////////////////

      case AASTORE:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "aastore",
                                                    org.apache.bcel.generic.Type.VOID,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.OBJECT,
                                                       org.apache.bcel.generic.Type.INT,
                                                       org.apache.bcel.generic.Type.OBJECT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case BASTORE:{
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "bastore",
                                                    org.apache.bcel.generic.Type.VOID,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("Ljava/lang/Object;"),
                                                       org.apache.bcel.generic.Type.INT,
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;
      }
                  
      case CASTORE:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "castore",
                                                    org.apache.bcel.generic.Type.VOID,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[C"),
                                                       org.apache.bcel.generic.Type.INT,
                                                       org.apache.bcel.generic.Type.CHAR
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case DASTORE:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "dastore",
                                                    org.apache.bcel.generic.Type.VOID,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[D"),
                                                       org.apache.bcel.generic.Type.INT,
                                                       org.apache.bcel.generic.Type.DOUBLE
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case FASTORE:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "fastore",
                                                    org.apache.bcel.generic.Type.VOID,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[F"),
                                                       org.apache.bcel.generic.Type.INT,
                                                       org.apache.bcel.generic.Type.FLOAT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case IASTORE:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "iastore",
                                                    org.apache.bcel.generic.Type.VOID,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[I"),
                                                       org.apache.bcel.generic.Type.INT,
                                                       org.apache.bcel.generic.Type.INT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case LASTORE:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "lastore",
                                                    org.apache.bcel.generic.Type.VOID,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[J"),
                                                       org.apache.bcel.generic.Type.INT,
                                                       org.apache.bcel.generic.Type.LONG
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;

      case SASTORE:
         handle.setInstruction(factory.createInvoke(SANDMARK_HASHHOLDER, "sastore",
                                                    org.apache.bcel.generic.Type.VOID,
                                                    new org.apache.bcel.generic.Type[]{
                                                       org.apache.bcel.generic.Type.getType("[S"),
                                                       org.apache.bcel.generic.Type.INT,
                                                       org.apache.bcel.generic.Type.SHORT
                                                    },
                                                    org.apache.bcel.Constants.INVOKESTATIC));
         break;
      }
   }



   public void apply(sandmark.program.Application app) throws Exception{
      sandmark.analysis.classhierarchy.ClassHierarchy ch = 
         app.getHierarchy();

      java.util.ArrayList arrayfields = new java.util.ArrayList();
      java.util.ArrayList newfields = new java.util.ArrayList();

      // gather all fields with single-dimension array type
      for (java.util.Iterator citer=app.classes();citer.hasNext();){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
         
         for (java.util.Iterator fiter=clazz.fields();fiter.hasNext();){
            sandmark.program.Field field = (sandmark.program.Field)fiter.next();
            if (field.getType() instanceof org.apache.bcel.generic.ArrayType){
               if (((org.apache.bcel.generic.ArrayType)field.getType()).getDimensions()==1){
                  arrayfields.add(field);
               }
            }
         }
      }

      sandmark.program.Field[] fieldarray = 
         (sandmark.program.Field[])arrayfields.toArray(new sandmark.program.Field[0]);

      canSplit(fieldarray, app, ch);
      arrayfields.clear();

      for (int i=0;i<fieldarray.length;i++){
         if (fieldarray[i]!=null)
            arrayfields.add(fieldarray[i]);
      }

      // if nothing is going to be split, exit
      if (arrayfields.size()==0)
         return;


      // make the end arrays for each split array
      for (int i=0;i<arrayfields.size();i++){
         sandmark.program.Field field = 
            (sandmark.program.Field)arrayfields.get(i);
         sandmark.program.Class clazz = field.getEnclosingClass();
         String endname = field.getName()+"_end";
         while(clazz.getField(endname, field.getSignature())!=null)
            endname += '0';
         newfields.add(new sandmark.program.LocalField(clazz, field.getAccessFlags(),
                                                       field.getType(), endname));
         clazz.mark();
      }

      // patch every method in the application
      for (java.util.Iterator citer=app.classes();citer.hasNext();){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
         
         for (java.util.Iterator miter = clazz.methods();miter.hasNext();){
            sandmark.program.Method method = (sandmark.program.Method)miter.next();
            
            org.apache.bcel.generic.InstructionList ilist = 
               method.getInstructionList();
            if (ilist==null)
               continue;
            
            org.apache.bcel.generic.InstructionHandle[] handles = 
               ilist.getInstructionHandles();
            for (int i=0;i<handles.length;i++){
               patchInstruction(handles[i], method, arrayfields, newfields, ch);
            }
            method.mark();
         }
      }
      
      addHelperClasses(app);
   }


   /* Determines if the given fields are safe to split with this algorithm.
    * Ensures that:
    * 1. Any PUTFIELD/PUTSTATIC to the given field will only
    *    put an array that was allocated in the same method as the PUT.
    * 2. Any GET from the field, or copy of the value from a PUT to that field
    *    will not be used inappropriately, as follows:
    *    a. passed as a method parameter
    *    b. stored inside another array
    *    c. returned from a method
    *    d. put into a different field
    *
    * Unsafe fields will be nulled out in the 'fields' array.
    */
   protected static void canSplit(sandmark.program.Field[] fields,
                                  sandmark.program.Application app,
                                  sandmark.analysis.classhierarchy.ClassHierarchy ch){
      
      java.util.HashSet[] newarrays = new java.util.HashSet[fields.length];
      // for each field, the set of values that might be PUT into that field (handles)
      java.util.HashSet[] mygets = new java.util.HashSet[fields.length];
      // for each field, the set of GETs of that field (handles)
      java.util.HashSet[] otherputvalues = new java.util.HashSet[fields.length];
      // for each field, the set of values stored by PUTs into OTHER fields

      for (int i=0;i<fields.length;i++){
         newarrays[i]=new java.util.HashSet();
         mygets[i] = new java.util.HashSet();
         otherputvalues[i] = new java.util.HashSet();
      }

      // this list will contain all the values that are abused (handles)
      java.util.HashSet allbadvalues = new java.util.HashSet();

      for (java.util.Iterator citer=app.classes();citer.hasNext();){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();

         for (java.util.Iterator miter=clazz.methods();miter.hasNext();){
            sandmark.program.Method method = (sandmark.program.Method)miter.next();
            org.apache.bcel.generic.InstructionList ilist = 
               method.getInstructionList();

            if (ilist==null)
               continue;

            sandmark.analysis.stacksimulator.StackSimulator stack = 
               method.getStack();
            
            org.apache.bcel.generic.InstructionHandle[] handles = 
               ilist.getInstructionHandles();
            
            for (int i=0;i<handles.length;i++){
               if (handles[i].getInstruction().getOpcode()==PUTFIELD ||
                   handles[i].getInstruction().getOpcode()==PUTSTATIC){
                  
                  org.apache.bcel.generic.FieldInstruction put = 
                     (org.apache.bcel.generic.FieldInstruction)handles[i].getInstruction();
                  sandmark.program.Field resolvedField=null;
                  try{
                     resolvedField = ch.resolveFieldReference
                        (new sandmark.util.FieldID(put.getFieldName(clazz.getConstantPool()),
                                                   put.getSignature(clazz.getConstantPool()),
                                                   put.getClassName(clazz.getConstantPool())),
                         clazz);
                  }catch(Throwable t){
                     throw new RuntimeException("Cannot resolve field reference");
                  }
                  if (resolvedField==null){
                     throw new RuntimeException("Cannot resolve field reference");
                  }

                  sandmark.analysis.stacksimulator.Context context = 
                     stack.getInstructionContext(handles[i]);
                  if (context.getStackSize()==0)
                     continue;

                  for (int f=0;f<fields.length;f++){
                     if (fields[f]==null)
                        continue;
                     if (fields[f].equals(resolvedField)){
                        // goes into newarrays[f]
                        newarrays[f].addAll(getSources(method, stack, context.getStackAt(0)));
                     }else{
                        // goes into otherputvalues[f]
                        otherputvalues[f].addAll(getSources(method, stack, context.getStackAt(0)));
                     }
                  }
               }

               else if (handles[i].getInstruction().getOpcode()==GETFIELD ||
                        handles[i].getInstruction().getOpcode()==GETSTATIC){
                  
                  org.apache.bcel.generic.FieldInstruction get = 
                     (org.apache.bcel.generic.FieldInstruction)handles[i].getInstruction();
                  sandmark.program.Field resolvedField=null;
                  try{
                     resolvedField = ch.resolveFieldReference
                        (new sandmark.util.FieldID(get.getFieldName(clazz.getConstantPool()),
                                                   get.getSignature(clazz.getConstantPool()),
                                                   get.getClassName(clazz.getConstantPool())),
                         clazz);
                  }catch(Throwable t){
                     throw new RuntimeException("Cannot resolve field reference");
                  }
                  if (resolvedField==null){
                     throw new RuntimeException("Cannot resolve field reference");
                  }

                  for (int f=0;f<fields.length;f++){
                     if (fields[f]==null)
                        continue;
                     if (fields[f].equals(resolvedField)){
                        mygets[f].add(handles[i]);
                        break;
                        // no more than one should match
                     }
                  }
               }

               // start looking for baddies

               else if (handles[i].getInstruction() instanceof org.apache.bcel.generic.InvokeInstruction){
                  org.apache.bcel.generic.Type[] argtypes = 
                     ((org.apache.bcel.generic.InvokeInstruction)handles[i].getInstruction()).getArgumentTypes(clazz.getConstantPool());
                  sandmark.analysis.stacksimulator.Context context = 
                     stack.getInstructionContext(handles[i]);

                  if (context.getStackSize()<argtypes.length){
                     // this must be a dead instruction, skip
                     continue;
                  }

                  for (int j=0;j<argtypes.length;j++){
                     allbadvalues.addAll(getSources(method, stack, context.getStackAt(j)));
                  }
               }else if (handles[i].getInstruction() instanceof org.apache.bcel.generic.AASTORE){
                  sandmark.analysis.stacksimulator.Context context = 
                     stack.getInstructionContext(handles[i]);
                  if (context.getStackSize()<1){
                     // this must be a dead instruction, skip
                     continue;
                  }
                  allbadvalues.addAll(getSources(method, stack, context.getStackAt(0)));
               }else if (handles[i].getInstruction() instanceof org.apache.bcel.generic.ARETURN){
                  sandmark.analysis.stacksimulator.Context context = 
                     stack.getInstructionContext(handles[i]);
                  if (context.getStackSize()<1){
                     // this must be a dead instruction, skip
                     continue;
                  }
                  allbadvalues.addAll(getSources(method, stack, context.getStackAt(0)));
               }
            }

         }// end foreach method
      }// end foreach class


      // check to see that each field is ok
      for (int f=0;f<fields.length;f++){
         if (fields[f]==null)
            continue;
         
         for (java.util.Iterator iiter=newarrays[f].iterator();fields[f]!=null && iiter.hasNext();){
            org.apache.bcel.generic.InstructionHandle newarray = 
               (org.apache.bcel.generic.InstructionHandle)iiter.next();
            if (newarray==null){
               fields[f]=null;
               break;
            }
            // null --> method parameter (or caught exception or returnaddress?), not safe!

            switch(newarray.getInstruction().getOpcode()){
            case ACONST_NULL:
            case NEWARRAY:
            case ANEWARRAY:
            case MULTIANEWARRAY:
               // these 3 are ok, the rest are not
               break;
            default:
               fields[f]=null;
               continue;
            }

            if (allbadvalues.contains(newarray) || 
                otherputvalues[f].contains(newarray)){
               fields[f]=null;
               break;
            }
         }

         if (fields[f]==null)
            continue;
         
         for (java.util.Iterator getiter=mygets[f].iterator();getiter.hasNext();){
            Object next = getiter.next();
            if (allbadvalues.contains(next) ||
                otherputvalues[f].contains(next)){
               fields[f]=null;
               break;
            }
         }
      }
   }


   /** Finds the instructions that made the values for the given list of stackdata.
    *  If any of the stackdata entries are ALOAD, then we trace through the local var to 
    *  the ASTORE that put it there, and take those instructions.
    *  @return a HashSet full of handles (and possibly null)
    */
   protected static java.util.HashSet getSources
      (sandmark.program.Method method,
       sandmark.analysis.stacksimulator.StackSimulator stack,
       sandmark.analysis.stacksimulator.StackData[] argdata){
      
      if (argdata==null)
         return new java.util.HashSet();

      java.util.HashSet result = new java.util.HashSet();
      java.util.LinkedList seenloads = new java.util.LinkedList();
      java.util.LinkedList queue = new java.util.LinkedList();
      
      for (int i=0;i<argdata.length;i++)
         queue.add(argdata[i]);

      while(!queue.isEmpty()){
         sandmark.analysis.stacksimulator.StackData data = 
            (sandmark.analysis.stacksimulator.StackData)queue.removeFirst();

         org.apache.bcel.generic.InstructionHandle handle = 
            data.getInstruction();

         if (handle==null){
            result.add(null);
            continue;
         }

         if (handle.getInstruction() instanceof org.apache.bcel.generic.ALOAD){
            if (seenloads.contains(handle))
               continue;
            seenloads.add(handle);
            int index = ((org.apache.bcel.generic.ALOAD)handle.getInstruction()).getIndex();
            sandmark.analysis.stacksimulator.StackData[] local = 
               stack.getInstructionContext(handle).getLocalVariableAt(index);

            if (local==null)
               continue;
            
            // local should be full of ASTOREs, but might have some other things
            for (int i=0;i<local.length;i++){
               if (local[i].getInstruction()==null){
                  // method parameter
                  result.add(null);
               }else if (local[i].getInstruction().getInstruction() instanceof org.apache.bcel.generic.ASTORE){
                  sandmark.analysis.stacksimulator.StackData[] storedata = 
                     stack.getInstructionContext(local[i].getInstruction()).getStackAt(0);
                  if (storedata==null)
                     continue;
                  for (int j=0;j<storedata.length;j++)
                     queue.add(storedata[j]);
               }// it might actually be another kind of STORE, because of strange things about JSRs
            }
         }
         else if (handle.getInstruction() instanceof org.apache.bcel.generic.CHECKCAST){
            if (seenloads.contains(handle))
               continue;
            seenloads.add(handle);
            sandmark.analysis.stacksimulator.StackData[] top = 
               stack.getInstructionContext(handle).getStackAt(0);
            for (int i=0;i<top.length;i++){
               result.add(top[i].getInstruction());
            }
         }else{
            result.add(handle);
         }
      }

      return result;
   }

   private void addHelperClasses(sandmark.program.Application app){
      try{
         java.io.InputStream smNodeStream =
            getClass().getResourceAsStream
            (SANDMARK_HASHHOLDER+".class");
         org.apache.bcel.classfile.JavaClass jc = 
            new org.apache.bcel.classfile.ClassParser
            (smNodeStream,SANDMARK_HASHHOLDER).parse();
         new sandmark.program.LocalClass(app,jc);
      }catch(Exception e) {
         throw new Error("Couldn't add HashHolder class");
      }
   }

   public static void main(String args[]) throws Throwable{
      if (args.length<1)
         return;

      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);
      
      ArraySplitter split = new ArraySplitter();
      split.apply(app);
      app.save(args[0]+".out");
   }
}

