package sandmark.util.opaquepredicatelib;

/**
 * ThreadLib creates run time deterministic/nondeterministic predicates
 * at the requested byte code position of a node within the
 * CFG of a method.
 * @author  {tapas@cs.arizona.edu}
 *
 */

public class ThreadPredicateGenerator extends OpaquePredicateGenerator {
   private static final boolean DEBUG = false;
   private static final int NUMTHREADS = 4;

   /* ASH 2004/01/07:  I'm pretty sure this is a non-deterministic predicate.
      Each thread does this:  static field f = f * 5; sleep randomly; f -= 1;
      There's no synchronization.  The actual predicate tests whether f % 2 == 0.
      Clearly, this changes from time to time.
   */
   /**
    *  This method creates a 'threadClassName' class extending from the 'Thread' class;
    *  The run() method implements the actual operations ie. manipulating the 
    *  static localvar value; We can create 2 instances of this 'threadClassName' class and 
    *  'run' them in parallel. The resultant localvar value will be non-deterministic.
    */
   protected sandmark.program.Class createThreadClass
      (sandmark.program.Application app) {
      String threadClassName = null;
      {
         int i = 0;
         for( ; app.getClass("C" + i) != null; i++){}
         threadClassName = "C" + i;
      }

      sandmark.program.Class threadClass =
         new sandmark.program.LocalClass
         (app,threadClassName,"java/lang/Thread", "",
          org.apache.bcel.Constants.ACC_PUBLIC|
          org.apache.bcel.Constants.ACC_STATIC|
          org.apache.bcel.Constants.ACC_SUPER,
          null);

      sandmark.program.Field predicateField = 
         new sandmark.program.LocalField
         (threadClass,
          org.apache.bcel.Constants.ACC_PUBLIC |
          org.apache.bcel.Constants.ACC_STATIC,
          org.apache.bcel.generic.Type.INT,
          "threadVar");


      {/* Make the <init> method */
         org.apache.bcel.generic.InstructionList ilist =
            new org.apache.bcel.generic.InstructionList();
	    
         ilist.append(org.apache.bcel.generic.InstructionConstants.ALOAD_0);
         org.apache.bcel.generic.PUSH pushinstr =
            new org.apache.bcel.generic.PUSH
            (threadClass.getConstantPool(), "Demo thread");
         ilist.append(pushinstr);
	    
         org.apache.bcel.generic.InstructionFactory f =
            new org.apache.bcel.generic.InstructionFactory
            (threadClass.getConstantPool());
	    
         org.apache.bcel.generic.Instruction invokespecialInstr =
            f.createInvoke("java/lang/Thread", "<init>",
                           org.apache.bcel.generic.Type.VOID, 
                           new org.apache.bcel.generic.Type[]{
                              org.apache.bcel.generic.Type.STRING
                           },
                           org.apache.bcel.Constants.INVOKESPECIAL);
         ilist.append(invokespecialInstr);
         ilist.append(org.apache.bcel.generic.InstructionConstants.ALOAD_0);
         org.apache.bcel.generic.Instruction invokevirtualInstr = 
            f.createInvoke("java/lang/Thread", "start",
                           org.apache.bcel.generic.Type.VOID, 
                           org.apache.bcel.generic.Type.NO_ARGS,
                           org.apache.bcel.Constants.INVOKEVIRTUAL);
         ilist.append(invokevirtualInstr);
         ilist.append(org.apache.bcel.generic.InstructionConstants.RETURN);
	    
         sandmark.program.Method initMethod = 
            new sandmark.program.LocalMethod(threadClass,org.apache.bcel.Constants.ACC_PUBLIC,
                                             org.apache.bcel.generic.Type.VOID, 
                                             org.apache.bcel.generic.Type.NO_ARGS,
                                             null, "<init>", ilist);
      }


      {/* create the 'run' method to implement the thread execution */
         org.apache.bcel.generic.InstructionFactory factory =
            new org.apache.bcel.generic.InstructionFactory(threadClass.getConstantPool());

         org.apache.bcel.generic.InstructionList irunlist = 
            new org.apache.bcel.generic.InstructionList();

         irunlist.append(org.apache.bcel.generic.InstructionConstants.NOP);
         irunlist.append(org.apache.bcel.generic.InstructionConstants.ICONST_5);
         irunlist.append(org.apache.bcel.generic.InstructionConstants.ISTORE_1);
         org.apache.bcel.generic.BranchHandle topBranch = 
            irunlist.append(new org.apache.bcel.generic.GOTO(null));
         org.apache.bcel.generic.Instruction getStatic = 
            factory.createGetStatic
            (threadClass.getName(),
             predicateField.getName(),
             predicateField.getType());
         org.apache.bcel.generic.InstructionHandle loopTop =
            irunlist.append(getStatic);
         irunlist.append(org.apache.bcel.generic.InstructionConstants.ICONST_5);
         irunlist.append(org.apache.bcel.generic.InstructionConstants.IMUL);
         org.apache.bcel.generic.Instruction putStatic =
            factory.createPutStatic
            (threadClass.getName(),
             predicateField.getName(),
             predicateField.getType());
         irunlist.append(putStatic);
         irunlist.append(factory.createInvoke
                         ("java/lang/Math", "random", 
                          org.apache.bcel.generic.Type.DOUBLE,
                          org.apache.bcel.generic.Type.NO_ARGS,
                          org.apache.bcel.Constants.INVOKESTATIC));
         irunlist.append(org.apache.bcel.generic.InstructionConstants.D2I);
         irunlist.append(new org.apache.bcel.generic.SIPUSH((short)1000));
         irunlist.append(org.apache.bcel.generic.InstructionConstants.IMUL);
         irunlist.append(org.apache.bcel.generic.InstructionConstants.I2L); 
         org.apache.bcel.generic.InstructionHandle start_pc = 
            irunlist.append
            (factory.createInvoke("java/lang/Thread", "sleep",
                                  org.apache.bcel.generic.Type.VOID, 
                                  new org.apache.bcel.generic.Type[] {
                                     org.apache.bcel.generic.Type.LONG,
                                  }, 
                                  org.apache.bcel.Constants.INVOKESTATIC));
         org.apache.bcel.generic.InstructionHandle end_pc = 
            irunlist.append(org.apache.bcel.generic.InstructionConstants.NOP);
         irunlist.append(getStatic);
         irunlist.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
         irunlist.append(org.apache.bcel.generic.InstructionConstants.ISUB);
         irunlist.append(putStatic);
         irunlist.append(new org.apache.bcel.generic.IINC(1, -1));
         topBranch.setTarget
            (irunlist.append(org.apache.bcel.generic.InstructionConstants.ILOAD_1));
         irunlist.append(new org.apache.bcel.generic.IFGT(loopTop));
         org.apache.bcel.generic.InstructionHandle returnIH =
            irunlist.append(org.apache.bcel.generic.InstructionConstants.RETURN);
         org.apache.bcel.generic.InstructionHandle handler = 
            irunlist.append(org.apache.bcel.generic.InstructionConstants.POP);
         irunlist.append(new org.apache.bcel.generic.IINC(1, 1));
         irunlist.append(new org.apache.bcel.generic.GOTO(returnIH));

         sandmark.program.LocalMethod runMethod =
            new sandmark.program.LocalMethod
            (threadClass, org.apache.bcel.Constants.ACC_PUBLIC, 
             org.apache.bcel.generic.Type.VOID,
             org.apache.bcel.generic.Type.NO_ARGS,
             null,"run",irunlist);

         org.apache.bcel.generic.ObjectType classtype = 
            (org.apache.bcel.generic.ObjectType)
            org.apache.bcel.generic.Type.getType
            ("Ljava/lang/InterruptedException;");

         runMethod.addExceptionHandler
            (start_pc, end_pc, handler, classtype);

         runMethod.mark();

         
         if (!irunlist.contains(handler))
            System.out.println("poop");


      }
      return threadClass;
   }



   /** entry API for this predicate lib; 
    *  returns 1 on success, 0 on failure;
    *  The idea is to create a static field in the current class.
    *  Then we create a Thread class and two instances of threads that access this 
    *  static field and alter its value dynamically and return its value normalized 
    *  to 0 or 1.
    *  -- opaquely non-deterministic predicate
    */

   public void insertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {

      sandmark.program.Class threadClass = 
         createThreadClass(method.getApplication());
      sandmark.program.Field predicateField = threadClass.getFields()[0];

      org.apache.bcel.generic.InstructionFactory f =
         new org.apache.bcel.generic.InstructionFactory
         (method.getConstantPool());
	
      org.apache.bcel.generic.NEW newInstr = f.createNew(threadClass.getType());
      org.apache.bcel.generic.Instruction initInstr =
         f.createInvoke(threadClass.getName(), "<init>",
                        org.apache.bcel.generic.Type.VOID, 
                        org.apache.bcel.generic.Type.NO_ARGS,
                        org.apache.bcel.Constants.INVOKESPECIAL);

      org.apache.bcel.generic.InstructionHandle start =
         method.getInstructionList().getStart();
      for(int k=0;k<NUMTHREADS;k++) {
         method.getInstructionList().insert(start, newInstr);
         method.getInstructionList().insert(start, initInstr);
      }

      org.apache.bcel.generic.FieldInstruction getStatic =
         f.createGetStatic(threadClass.getName(), 
                           predicateField.getName(),
                           predicateField.getType());

      org.apache.bcel.generic.InstructionList predInstrList =
         new org.apache.bcel.generic.InstructionList();

      predInstrList.append(getStatic);
      predInstrList.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
      predInstrList.append(org.apache.bcel.generic.InstructionConstants.IADD);
      predInstrList.append(org.apache.bcel.generic.InstructionConstants.ICONST_2);
      predInstrList.append(org.apache.bcel.generic.InstructionConstants.IREM);

      updateTargeters(ih,predInstrList.getStart());
      method.getInstructionList().insert(ih,predInstrList);
      method.mark();
   }

   public void insertInterproceduralPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih, int valueType) {


      sandmark.program.Class threadClass = 
         createThreadClass(method.getApplication());
      sandmark.program.Field predicateField = threadClass.getFields()[0];

      sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
         findInterproceduralDominators(method, ih, NUMTHREADS);

      if (blocks==null || blocks.length==0){
         insertPredicate(method, ih, valueType);
         return;
      }

      java.util.Random random = sandmark.util.Random.getRandom();
      for(int k=0;k<NUMTHREADS;k++) {
         org.apache.bcel.generic.InstructionHandle insertpoint = 
            (org.apache.bcel.generic.InstructionHandle)
            blocks[k].getInstList().get(random.nextInt(blocks[k].getInstList().size()));

         org.apache.bcel.generic.InstructionFactory f =
            new org.apache.bcel.generic.InstructionFactory
            (blocks[k].graph().method().getConstantPool());
         
         org.apache.bcel.generic.Instruction initInstr =
            f.createInvoke(threadClass.getName(), "<init>",
                           org.apache.bcel.generic.Type.VOID, 
                           org.apache.bcel.generic.Type.NO_ARGS,
                           org.apache.bcel.Constants.INVOKESPECIAL);

         blocks[k].graph().method().getInstructionList().insert(insertpoint, 
                                                                f.createNew(threadClass.getType()));
         blocks[k].graph().method().getInstructionList().insert(insertpoint, initInstr);
         blocks[k].graph().method().mark();
      }

      org.apache.bcel.generic.InstructionFactory f =
         new org.apache.bcel.generic.InstructionFactory
         (method.getConstantPool());

      org.apache.bcel.generic.InstructionList predInstrList =
         new org.apache.bcel.generic.InstructionList();
      predInstrList.append(f.createGetStatic(threadClass.getName(), 
                                             predicateField.getName(),
                                             predicateField.getType()));
      predInstrList.append(org.apache.bcel.generic.InstructionConstants.ICONST_1);
      predInstrList.append(org.apache.bcel.generic.InstructionConstants.IADD);
      predInstrList.append(org.apache.bcel.generic.InstructionConstants.ICONST_2);
      predInstrList.append(org.apache.bcel.generic.InstructionConstants.IREM);

      updateTargeters(ih, predInstrList.getStart());
      method.getInstructionList().insert(ih, predInstrList);
      method.mark();
   }



   public boolean canInsertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,int valueType) {
      return true;
   }

   public static void updateTargeters
      (org.apache.bcel.generic.InstructionHandle oldIH,
       org.apache.bcel.generic.InstructionHandle newIH) {

      org.apache.bcel.generic.InstructionTargeter targeters[] =
         oldIH.getTargeters();
      if(targeters == null || targeters.length == 0)
         return;
      for(int i = 0 ; i < targeters.length ; i++) {
         if(targeters[i] instanceof 
            org.apache.bcel.generic.CodeExceptionGen) {
            org.apache.bcel.generic.CodeExceptionGen ceg =
               (org.apache.bcel.generic.CodeExceptionGen)targeters[i];
            if(ceg.getStartPC() == oldIH)
               ceg.setStartPC(newIH);
            if(ceg.getHandlerPC() == oldIH)
               ceg.setHandlerPC(newIH);
         } else
            targeters[i].updateTarget(oldIH,newIH);
      }
   }

   private static PredicateInfo sInfo;
   public static PredicateInfo getInfo() {
      if(sInfo == null)
         sInfo = new PredicateInfo(OpaqueManager.PT_THREAD,
                                   OpaqueManager.PV_UNKNOWN);
      return sInfo;
   }

   public static void main(String args[]) throws Exception{
      if (args.length<1)
         return;

      sandmark.program.Application app =
         new sandmark.program.Application(args[0]);

      sandmark.program.Class clazz = app.getClass("test4");
      sandmark.program.Method method = clazz.getMethod("d", "()V");
      org.apache.bcel.generic.InstructionList ilist = 
         method.getInstructionList();
      org.apache.bcel.generic.InstructionHandle insertpoint = 
         ilist.getInstructionHandles()[4];
      org.apache.bcel.generic.InstructionFactory factory =
         new org.apache.bcel.generic.InstructionFactory(clazz.getConstantPool());


      org.apache.bcel.generic.BranchInstruction ifinstr=null, gotoinstr=null;
      org.apache.bcel.generic.InstructionHandle first=null, target1=null, target2=null;

      first=ilist.insert(insertpoint, factory.createGetStatic("java.lang.System", "out", 
                                                              org.apache.bcel.generic.Type.getType("Ljava/io/PrintStream;")));
      ilist.insert(insertpoint, org.apache.bcel.generic.InstructionConstants.SWAP);
      ilist.insert(insertpoint, ifinstr=new org.apache.bcel.generic.IFEQ(null));
      ilist.insert(insertpoint, new org.apache.bcel.generic.LDC(clazz.getConstantPool().addString("nonzero")));
      ilist.insert(insertpoint, gotoinstr=new org.apache.bcel.generic.GOTO(null));
      target1=ilist.insert(insertpoint, new org.apache.bcel.generic.LDC(clazz.getConstantPool().addString("zero")));
      target2=ilist.insert(insertpoint, factory.createInvoke("java.io.PrintStream", "println", 
                                                             org.apache.bcel.generic.Type.VOID,
                                                             new org.apache.bcel.generic.Type[]{
                                                                org.apache.bcel.generic.Type.STRING
                                                             },
                                                             org.apache.bcel.Constants.INVOKEVIRTUAL));
      ifinstr.setTarget(target1);
      gotoinstr.setTarget(target2);
      
      
      ThreadPredicateGenerator pred = new ThreadPredicateGenerator();
      pred.insertInterproceduralPredicate(method, first, 0);

      app.save(args[0]+".out");
   }
}

