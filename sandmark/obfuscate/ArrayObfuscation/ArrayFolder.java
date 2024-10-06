package sandmark.obfuscate.ArrayObfuscation;

/** This obfuscation looks for fields that are single-dimensional
 *  arrays of primitive types, and folds them. That is, it will make them into 
 *  multi-dimensional arrays with a mapping between linear indexes and multi-dimensional 
 *  indexes. We randomly fold the array either once or twice. The new dimensions will have length
 *  2, so that each fold is a fold 'in half' so to speak. We only use primitive types because 
 *  strange problems can arise when folding arrays of references. 
 *  We must place severe restrictions on the arrays which we can fold, because folded arrays
 *  are a fundamentally different type fom their unfolded selves. The canFold method
 *  acts as a filter to remove all the fields that cannot be folded. Specifically, each such 
 *  field must pass the following tests:
 *
 *  1. First, we find the set of operands that might be 'put' into the field f. 
 *     Call it PUT(f). Also, we find the set of all 'gets' of the field. Call it GET(f).
 *     It is convenient to label (PUT(f) union GET(f)) as ALL(f).
 *
 *  2. For each field f, PUT(f) must only contain ACONST_NULL or NEWARRAY instructions. This is to
 *     ensure that we can trace the origins of the field value, and avoid alias problems
 *     involving reference semantics.
 *
 *  3. For each i in ALL(f), make sure i is not:
 *     a. passed as a method parameter
 *     b. used in a CHECKCAST
 *     c. used in an INSTANCEOF
 *     d. stored in a different field
 *     e. stored into a different array
 *     f. returned from a method
 *
 *  4. For each i in ALL(f), find all instructions that may use i as an operand.
 *     For each of these instructions, look at the set of values each operand may take on.
 *     If one of these sets intersects ALL(f) but is not a subset of ALL(f),
 *     then f cannot be folded. (in other words, you can't have a folded array and a non-folded
 *     array be used in the same place, because there would be a type conflict).
 *
 *  Once all the foldable fields have been identified, we patch the instructions that access them.
 *  For each field f, we actually fold all arrays in PUT(f) (i.e. we replace all the NEWARRAY instructions
 *  with calls to SandmarkFolder.newarray, which will return a folded version of the array). We also 
 *  replace all array instructions with static method calls to do the actual work. We only replace the
 *  instructions that we know act only upon the folded arrays. The other array instructions remain the same.
 */
public class ArrayFolder extends sandmark.obfuscate.AppObfuscator
   implements org.apache.bcel.Constants{

   private static final boolean DEBUG = false;

   private String SANDMARK_FOLDER = "Folder";

   public ArrayFolder(){}

   public String getShortName(){
      return "Array Folder";
   }

   public String getLongName(){
      return "Array Folder";
   }

   public String getAlgHTML(){
      return null;
   }

   public String getAlgURL(){
      return "sandmark/obfuscate/ArrayObfuscation/doc/help_folder.html";
   }
   
   public String getAuthor(){
      return "Mike Stepp";
   }
   
   public String getAuthorEmail(){
      return "steppm@cs.arizona.edu";
   }

   public String getDescription(){
      return "Turns a single-dimensional array into a multi-dimensional array.";
   }
   
   public sandmark.config.ModificationProperty[] getMutations(){
      return null;
   }


   /** After finding all the foldable fields, this method will actually
    *  change all the instructions that manipulate them.
    *  @param method the method whose instructions will be patched.
    *  @param adddimensions an array, parallel to allfields, that holds the number of 
    *         dimensions that were added to each array field.
    *  @param allfields the array of all the fields (unfoldable fields will be nulled out)
    *  @param newarrays the set of array-creating instructions that need to be patched.
    *  @param uses the instructions that use items in newarrays that need to be patched.
    */
   private void patchInstructions(sandmark.program.Method method,
                                  int[] adddimensions,
                                  sandmark.program.Field[] allfields,
                                  java.util.HashSet[] newarrays,
                                  java.util.HashSet[] uses){

      org.apache.bcel.generic.InstructionList ilist = 
         method.getInstructionList();

      org.apache.bcel.generic.InstructionFactory factory = 
         new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());
      org.apache.bcel.generic.InstructionHandle[] handles = 
         ilist.getInstructionHandles();
      
      for (int i=0;i<handles.length;i++){
         for (int f=0;f<allfields.length;f++){
            if (allfields[f]==null)
               continue;
            
            // newsig will look like "[,[B" for a byte array with one extra
            // dimension added, or "[[,[B" for a byte array with 2 extra dimensions, etc.
            String newsig = ","+allfields[f].getType().getSignature();
            for (int n=0;n<adddimensions[f];n++)
               newsig = '['+newsig;
            
            if (newarrays[f].contains(handles[i])){
               org.apache.bcel.generic.Instruction inst = 
                  handles[i].getInstruction();
               
               // don't need to patch ACONST_NULL, just NEWARRAY
               if (inst.getOpcode()==NEWARRAY){
                  org.apache.bcel.generic.InstructionHandle ih = handles[i];
                  org.apache.bcel.generic.ArrayType newtype = 
                     new org.apache.bcel.generic.ArrayType
                     (((org.apache.bcel.generic.NEWARRAY)inst).getType(), adddimensions[f]);
                  
                  // replace with a call to SandmarkFolder.newarray(int,String)Object
                  // and then a cast to the right array type
                  handles[i].setInstruction(factory.createConstant(newsig));
                  ih = ilist.append(ih, factory.createInvoke(SANDMARK_FOLDER, 
                                                             "newarray", org.apache.bcel.generic.Type.OBJECT,
                                                             new org.apache.bcel.generic.Type[]{
                                                                org.apache.bcel.generic.Type.INT,
                                                                org.apache.bcel.generic.Type.STRING
                                                             },
                                                             INVOKESTATIC));
                  ih = ilist.append(ih, factory.createCheckCast(newtype));
                  
               }

            }else if (uses[f].contains(handles[i])){
               org.apache.bcel.generic.Instruction inst = 
                  handles[i].getInstruction();

               if (inst instanceof org.apache.bcel.generic.ArrayInstruction){
                  org.apache.bcel.generic.InstructionHandle ih = handles[i];

                  switch(inst.getOpcode()){
                  case BALOAD:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "baload", org.apache.bcel.generic.Type.INT,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT
                                                                    },
                                                                    INVOKESTATIC));
                     break;
                           
                  case CALOAD:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "caload", org.apache.bcel.generic.Type.CHAR,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT
                                                                    },
                                                                    INVOKESTATIC));
                     break;

                  case DALOAD:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "daload", org.apache.bcel.generic.Type.DOUBLE,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT
                                                                    },
                                                                    INVOKESTATIC));
                     break;

                  case FALOAD:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "faload", org.apache.bcel.generic.Type.FLOAT,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT
                                                                    },
                                                                    INVOKESTATIC));
                     break;

                  case IALOAD:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "iaload", org.apache.bcel.generic.Type.INT,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT
                                                                    },
                                                                    INVOKESTATIC));
                     break;
                           
                  case LALOAD:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "laload", org.apache.bcel.generic.Type.LONG,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT
                                                                    },
                                                                    INVOKESTATIC));
                     break;

                  case SALOAD:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER, 
                                                                    "saload", org.apache.bcel.generic.Type.SHORT,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT
                                                                    },
                                                                    INVOKESTATIC));
                     break;
                           
                     ////////////////////////////

                  case FASTORE:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "fastore", org.apache.bcel.generic.Type.VOID,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT,
                                                                       org.apache.bcel.generic.Type.FLOAT
                                                                    },
                                                                    INVOKESTATIC));
                     break;

                  case DASTORE:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "dastore", org.apache.bcel.generic.Type.VOID,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT,
                                                                       org.apache.bcel.generic.Type.DOUBLE
                                                                    },
                                                                    INVOKESTATIC));
                     break;

                  case LASTORE:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "lastore", org.apache.bcel.generic.Type.VOID,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT,
                                                                       org.apache.bcel.generic.Type.LONG
                                                                    },
                                                                    INVOKESTATIC));
                     break;
                           
                  case BASTORE:
                  case CASTORE:
                  case IASTORE:
                  case SASTORE:
                     handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                    "iastore", org.apache.bcel.generic.Type.VOID,
                                                                    new org.apache.bcel.generic.Type[]{
                                                                       org.apache.bcel.generic.Type.OBJECT,
                                                                       org.apache.bcel.generic.Type.INT,
                                                                       org.apache.bcel.generic.Type.INT
                                                                    },
                                                                    INVOKESTATIC));
                     break;

                  case AASTORE:
                  case AALOAD:
                     break;
                  }

               }else if (inst.getOpcode()==ARRAYLENGTH){
                  handles[i].setInstruction(factory.createInvoke(SANDMARK_FOLDER,
                                                                 "arraylength", org.apache.bcel.generic.Type.INT,
                                                                 new org.apache.bcel.generic.Type[]{
                                                                    org.apache.bcel.generic.Type.OBJECT
                                                                 },
                                                                 INVOKESTATIC));
               }else{
                  throw new RuntimeException("This shouldn't happen!!! "+inst);
               }
            }
         }
      }
   }

   // for debugging only
   private static String getLongFieldName(sandmark.program.Field field){
      return field.getEnclosingClass().getName()+"."+field.getName()+field.getSignature();
   }


   
   public void apply(sandmark.program.Application app) throws Exception{
      sandmark.analysis.classhierarchy.ClassHierarchy ch = 
         app.getHierarchy();

      java.util.ArrayList arrayfields = new java.util.ArrayList();

      // locate all single-dimension array fields with primite element type
      for (java.util.Iterator citer=app.classes();citer.hasNext();){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
         for (java.util.Iterator fiter=clazz.fields();fiter.hasNext();){
            sandmark.program.Field field = (sandmark.program.Field)fiter.next();
            if (field.getType() instanceof org.apache.bcel.generic.ArrayType){
               org.apache.bcel.generic.ArrayType mytype = 
                  (org.apache.bcel.generic.ArrayType)field.getType();

               if (mytype.getDimensions()==1 && 
                   mytype.getElementType() instanceof org.apache.bcel.generic.BasicType){

                  arrayfields.add(field);
               }
            }
         }
      }
      // found all array fields, whittle them down

      if (arrayfields.size()==0){
         if (DEBUG)
            System.out.println("No single-dim array fields at all!");
         return;
      }
      
      sandmark.program.Field[] allfields = 
         (sandmark.program.Field[])arrayfields.toArray(new sandmark.program.Field[0]);

      if (DEBUG){
         for (int f=0;f<allfields.length;f++){
            System.out.println("Potential fold "+getLongFieldName(allfields[f]));
         }
      }

      arrayfields=null;
      java.util.HashSet[] newarrays = new java.util.HashSet[allfields.length];
      java.util.HashSet[] myreferences = new java.util.HashSet[allfields.length];
      java.util.HashSet[] uses = canFold(allfields, newarrays, app, ch, myreferences);

      // canFold will null out elements of allfields and fill 
      // in elements of newarrays and myreferences.
      // the non-null elements of allfields should be safe to fold.

      // early termination check...
      boolean any=false;
      for (int f=0;f<allfields.length;f++){
         if (allfields[f]!=null){
            any=true;
            break;
         }
      }
      if (!any){
         if (DEBUG)
            System.out.println("No foldable fields!");
         return;
      }
      // keep going!!


      if (DEBUG){
         for (int i=0;i<allfields.length;i++){
            if (allfields[i]!=null){
               System.out.println("Folding "+
                                  allfields[i].getEnclosingClass().getName()+"."+
                                  allfields[i].getName()+allfields[i].getSignature());
            }
         }
      }


      // randomly decide how much to fold each array (once or twice)
      int[] adddimensions = new int[allfields.length];
      java.util.Random random = sandmark.util.Random.getRandom();
      for (int f=0;f<allfields.length;f++){
         if (allfields[f]==null)
            continue;
         adddimensions[f] = 1+random.nextInt(2);  // random{1,2}
      }

      addHelperClasses(app);

      // patch all the uses and the newarrays
      for (java.util.Iterator citer=app.classes();citer.hasNext();){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
         
         for (java.util.Iterator miter=clazz.methods();miter.hasNext();){
            sandmark.program.Method method = (sandmark.program.Method)miter.next();
            org.apache.bcel.generic.InstructionList ilist = 
               method.getInstructionList();

            if (ilist==null)
               continue;

            patchInstructions(method, adddimensions, allfields, newarrays, uses);

            // fix all the constant pool indexes for the field references
            for (int f=0;f<allfields.length;f++){
               if (allfields[f]==null)
                  continue;

               for (java.util.Iterator refiter=myreferences[f].iterator();refiter.hasNext();){
                  org.apache.bcel.generic.InstructionHandle ref = 
                     (org.apache.bcel.generic.InstructionHandle)refiter.next();
                  if (!ilist.contains(ref))
                     continue;
                  
                  org.apache.bcel.generic.FieldInstruction fi = 
                     (org.apache.bcel.generic.FieldInstruction)ref.getInstruction();
                  String classname = fi.getClassName(method.getConstantPool());
                  String fieldname = fi.getFieldName(method.getConstantPool());
                  String fieldsig = fi.getSignature(method.getConstantPool());
                  org.apache.bcel.generic.InstructionFactory factory = 
                     new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());
                  
                  ref.setInstruction(factory.createFieldAccess(classname, fieldname, 
                                                            new org.apache.bcel.generic.ArrayType
                                                            (org.apache.bcel.generic.Type.getType(fieldsig), adddimensions[f]),
                                                               ref.getInstruction().getOpcode()));
               }
            }
         }// end foreach method
      }// end foreach class


      // now give the fields their new types
      for (int f=0;f<allfields.length;f++){
         if (allfields[f]==null)
            continue;
         
         allfields[f].setType(new org.apache.bcel.generic.ArrayType
                              (allfields[f].getType(), adddimensions[f]));
      }
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
    * @return the uses of each field
    */
   protected static java.util.HashSet[] canFold(sandmark.program.Field[] fields,
                                                java.util.HashSet[] newarrays,
                                                sandmark.program.Application app,
                                                sandmark.analysis.classhierarchy.ClassHierarchy ch,
                                                java.util.HashSet[] myreferences){
      
      java.util.HashSet[] mygets = new java.util.HashSet[fields.length];
      // for each field, the set of GETs of that field (handles)
      java.util.HashSet[] otherputvalues = new java.util.HashSet[fields.length];
      // for each field, the set of values stored by PUTs into OTHER fields

      for (int i=0;i<fields.length;i++){
         newarrays[i] = new java.util.HashSet();
         myreferences[i] = new java.util.HashSet();
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
                        newarrays[f].addAll(ArraySplitter.getSources(method, stack, context.getStackAt(0)));
                        myreferences[f].add(handles[i]);
                     }else{
                        // goes into otherputvalues[f]
                        otherputvalues[f].addAll(ArraySplitter.getSources(method, stack, context.getStackAt(0)));
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
                        myreferences[f].add(handles[i]);
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
                     allbadvalues.addAll(ArraySplitter.getSources(method, stack, context.getStackAt(j)));
                  }
               }else if (handles[i].getInstruction() instanceof org.apache.bcel.generic.AASTORE ||
                         handles[i].getInstruction() instanceof org.apache.bcel.generic.ARETURN ||
                         handles[i].getInstruction() instanceof org.apache.bcel.generic.INSTANCEOF ||
                         handles[i].getInstruction() instanceof org.apache.bcel.generic.CHECKCAST){
                  sandmark.analysis.stacksimulator.Context context = 
                     stack.getInstructionContext(handles[i]);
                  if (context.getStackSize()<1){
                     // this must be a dead instruction, skip
                     continue;
                  }
                  allbadvalues.addAll(ArraySplitter.getSources(method, stack, context.getStackAt(0)));
               }
            }

         }// end foreach method
      }// end foreach class


      // check to see that each field is ok
      for (int f=0;f<fields.length;f++){
         if (fields[f]==null)
            continue;

         org.apache.bcel.generic.Type mytype=null;
         
         for (java.util.Iterator iiter=newarrays[f].iterator();fields[f]!=null && iiter.hasNext();){
            org.apache.bcel.generic.InstructionHandle newarray = 
               (org.apache.bcel.generic.InstructionHandle)iiter.next();
            if (newarray==null){
               if (DEBUG)
                  System.out.println(getLongFieldName(fields[f])+" can be assigned from NULL handle!");

               fields[f]=null;
               break;
            }
            // null --> method parameter (or caught exception or returnaddress?), not safe!

            switch(newarray.getInstruction().getOpcode()){
            case ACONST_NULL:
               break;
            case NEWARRAY:{
               org.apache.bcel.generic.Type arraytype = 
                  ((org.apache.bcel.generic.NEWARRAY)newarray.getInstruction()).getType();
               if (mytype==null){
                  mytype=arraytype;
               }else if (!mytype.equals(arraytype)){
                  if (DEBUG)
                     System.out.println(getLongFieldName(fields[f])+" has inconsistent types!");

                  fields[f]=null;
                  continue;
               }
               break;
            }

            default:
               if (DEBUG)
                  System.out.println(getLongFieldName(fields[f]) + " is not ACONST_NULL or NEWARRAY!");


               fields[f]=null;
               continue;
            }

            if (allbadvalues.contains(newarray) || 
                otherputvalues[f].contains(newarray)){
               if (DEBUG)
                  System.out.println(getLongFieldName(fields[f])+" has values that get abused!");

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
               if (DEBUG)
                  System.out.println(getLongFieldName(fields[f])+" has GETs that are abused!");

               fields[f]=null;
               break;
            }
         }
      }

      
      // find all uses of these fields and check that they can't use anything else
      java.util.HashSet[] uses = new java.util.HashSet[fields.length];
      for (int f=0;f<fields.length;f++){
         uses[f] = new java.util.HashSet();
      }

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
               sandmark.analysis.stacksimulator.Context context = 
                  stack.getInstructionContext(handles[i]);
               org.apache.bcel.generic.Instruction inst = handles[i].getInstruction();

               if (inst instanceof org.apache.bcel.generic.ArrayInstruction){
                  // check stack
                  switch(inst.getOpcode()){
                  case BALOAD:
                  case CALOAD:
                  case DALOAD:
                  case FALOAD:
                  case IALOAD:
                  case LALOAD:
                  case SALOAD:
                     if (context.getStackSize()<2)
                        continue;
                     filterNormals(fields, newarrays, mygets, uses, context.getStackAt(1), handles[i]);
                     break;
                     
                  case BASTORE:
                  case CASTORE:
                  case DASTORE:
                  case FASTORE:
                  case IASTORE:
                  case LASTORE:
                  case SASTORE:
                     // stores
                     if (context.getStackSize()<3)
                        continue;
                     filterNormals(fields, newarrays, mygets, uses, context.getStackAt(2), handles[i]);
                     break;
                  }

               }else if (inst instanceof org.apache.bcel.generic.ALOAD){
                  // check local
                  int index = ((org.apache.bcel.generic.ALOAD)inst).getIndex();
                  if (index>=context.getLocalVariableCount())
                     continue;
                  filterNormals(fields, newarrays, mygets, null, context.getLocalVariableAt(index), handles[i]);

               }else if (inst.getOpcode()==ARRAYLENGTH){
                  // check stack
                  if (context.getStackSize()==0)
                     continue;
                  filterNormals(fields, newarrays, mygets, uses, context.getStackAt(0), handles[i]);

               }else if (inst instanceof org.apache.bcel.generic.ASTORE){
                  // check stack
                  if (context.getStackSize()==0)
                     continue;
                  filterNormals(fields, newarrays, mygets, null, context.getStackAt(0), handles[i]);

               }else if (inst instanceof org.apache.bcel.generic.InvokeInstruction){
                  // check reference on which this is being invoked (not for static)
                  org.apache.bcel.generic.InvokeInstruction invoke = 
                     (org.apache.bcel.generic.InvokeInstruction)inst;
                  if (invoke.getOpcode()==INVOKESTATIC)
                     continue;
                  int numargs = invoke.getArgumentTypes(clazz.getConstantPool()).length+1;
                  if (context.getStackSize()<numargs)
                     continue;
                  filterNormals(fields, newarrays, mygets, null, context.getStackAt(numargs-1), handles[i]);
               }
            }
         }// end foreach method
      }// end foreach class
      
      return uses;
   }


   // method to test for intersection of operands with ALL(f) 
   // (see comments at beginning of class)
   private static void filterNormals(sandmark.program.Field[] fields, 
                                     java.util.HashSet[] newarrays,
                                     java.util.HashSet[] mygets,
                                     java.util.HashSet[] uses,
                                     sandmark.analysis.stacksimulator.StackData[] data,
                                     org.apache.bcel.generic.InstructionHandle handle){

      if (data==null)
         return;

      for (int f=0;f<fields.length;f++){
         if (fields[f]==null)
            continue;

         boolean any=false;
         boolean all=true;
         
         for (int s=0;s<data.length;s++){
            if (newarrays[f].contains(data[s].getInstruction()) ||
                mygets[f].contains(data[s].getInstruction())){
               any=true;
            }else{
               all=false;
            }
         }
         
         if (any && !all){
            if (DEBUG)
               System.out.println(getLongFieldName(fields[f])+" is used along with others!");

            fields[f]=null;
         }else if (any && uses!=null){
            uses[f].add(handle);
         }
      }
   }

   // Adds the SandmarkFolder class to the application
   private void addHelperClasses(sandmark.program.Application app){
      try{
         java.io.InputStream smNodeStream =
            getClass().getResourceAsStream
            (SANDMARK_FOLDER+".class");
         org.apache.bcel.classfile.JavaClass jc = 
            new org.apache.bcel.classfile.ClassParser
            (smNodeStream,SANDMARK_FOLDER).parse();
         new sandmark.program.LocalClass(app,jc);
      }catch(Exception e) {
         throw new Error("Couldn't add Folder class");
      }
   }


   public static void main(String args[]) throws Throwable{
      if (args.length<1) 
         return;
      
      sandmark.program.Application app =
         new sandmark.program.Application(args[0]);

      new ArrayFolder().apply(app);
      app.save(args[0]+".out");
   }
}
