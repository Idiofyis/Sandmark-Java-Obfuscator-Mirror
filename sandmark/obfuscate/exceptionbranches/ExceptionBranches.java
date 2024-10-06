package sandmark.obfuscate.exceptionbranches;

/** This class changes all branches (except JSRs) into 'throw' instructions.
 *  For an 'if' statement there are 2 possible targets -- the fallthrough and the branch.
 *  This can be replicated by throwing one of 2 different exception types, placing handlers
 *  around that 'throw' which will redirect control flow to the appropriate place.
 *  But of course, knowing which exception to throw would be directly related to the
 *  runtime values of the 'if' operands. So we make a static helper method that will
 *  take these 2 values and return a subclass of Throwable, which will be the appropriate 
 *  exception to cause the hander to go to the right place. Similar schemes work for
 *  goto instrutions and switch statements. 
 *  The helper class we use is DispatcherException (formerly 'Dispatcher', but it became
 *  convenient to make it an exception type itself). The dispatcher has many static helper methods
 *  that are designed to emulate the behaviors of various branch instructions, but return 
 *  Throwable instances. In order to know which exceptions it should return when, each
 *  obfuscated class has had its <clinit> augmented to make calls to DispatcherException.register
 *  or Dispatcher.registerSwitch. These methods assign a globally unique ID number to each branch point,
 *  and these ID numbers are also hashkeys that map to a list of exceptions. When you register an
 *  'if' statement in the dispatcher, you give it 2 exception class types (order is important) and
 *  the first one will be used if the comparison succeeds, the second if it fails. Instances of these
 *  exception classes are created at runtime via Class.forName(exceptionName).newInstance().
 *
 *  To simplifyy this entire scheme, we only replace branches at which there is an empty stack
 *  (except for the operands needed by the branch instruction). That way, when the 'throw' instruction
 *  destroys the stack, nothing is lost.
 */
public class ExceptionBranches extends sandmark.obfuscate.MethodObfuscator
   implements org.apache.bcel.Constants{

   private static int POSITION_ITERATOR=0;
   // global counter for position values

   // the application in question
   private sandmark.program.Application app;
   // true iff a call to addDispatcherClass has been made
   private boolean addedDispatcher;
   // the global list of pre-made exception classes (sandmark.program.Class)
   private java.util.ArrayList exceptionList;


   /** Creates an ExceptionBranches instance for the given application.
    *  Only methods in this application can be obfuscated (it checks!).
    */
   public ExceptionBranches(sandmark.program.Application _app){
      init(_app);
   }

   public ExceptionBranches(){
      // this must be here!!
   }

   private void init(sandmark.program.Application _app){
      if (app == _app)
         return;
      app = _app;
      exceptionList = new java.util.ArrayList();
      addedDispatcher=false;
   }

   public String getShortName() { return "Exception Branches"; }

   public String getLongName() {
      return "Replace branches with thrown exceptions.";
   }

   public java.lang.String getAlgHTML() {
      return "<HTML><BODY>" +
         "Exception Branches replaces all branches that have an empty stack " +
         "(except JSRs) with the throwing of exceptions. Handlers are cleverly "+
         "placed around these throws so that the destinations will be the same as the "+
         "original branches."+
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:steppm@cs.arizona.edu\">Mike Stepp</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }
    
   public java.lang.String getAlgURL() {
      return "sandmark/obfuscate/exceptionbranches/doc/help.html";
   }

   public java.lang.String getAuthor() { return "Mike Stepp"; }

   public java.lang.String getAuthorEmail() { return "steppm@cs.arizona.edu"; }

   public java.lang.String getDescription() {
      return "Exception Branches replaces all branches that have an empty stack " +
         "(except JSRs) with the throwing of exceptions. Handlers are cleverly "+
         "placed around these throws so that the destinations will be the same as the "+
         "original branches.";
   }

   public sandmark.config.ModificationProperty[] getMutations() {
      return null;
   }

   
   /** Apply the obfuscation to the given method. If the method has a null
    *  InstructionList (i.e. native or abstract/interface), nothing happens.
    */
   public void apply(sandmark.program.Method method) throws Exception{
      init(method.getEnclosingClass().getApplication());

      if (method.getInstructionList()==null)
         return;

      org.apache.bcel.generic.InstructionList ilist = 
         method.getInstructionList();
      org.apache.bcel.generic.InstructionFactory factory = 
         new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());
      org.apache.bcel.generic.InstructionHandle[] handles = 
         ilist.getInstructionHandles();

      java.util.ArrayList branches = new java.util.ArrayList();

      // copy out the exception handlers 
      // (so i can add new exception handlers to the front of the list later)
      java.util.ArrayList handlers = new java.util.ArrayList();
      org.apache.bcel.generic.CodeExceptionGen[] gens = 
         method.getExceptionHandlers();
      for (int i=0;i<gens.length;i++)
         handlers.add(gens[i]);

      sandmark.analysis.stacksimulator.StackSimulator stack = 
         method.getStack();

      // I will need to generate this many exception classes
      int neededexceptions=0;

      // find all the branches with empty stacks
      for (int i=0;i<handles.length;i++){
         if (handles[i].getInstruction() instanceof org.apache.bcel.generic.IfInstruction){
            short code = handles[i].getInstruction().getOpcode();
            int numargs=2;
            switch(code){
            case IFNONNULL: case IFNULL:
            case IFEQ: case IFGE: case IFGT:
            case IFLE: case IFLT: case IFNE:
               numargs=1;
               break;
            }

            if (stack.getInstructionContext(handles[i]).getStackSize()!=numargs)
               continue;

            branches.add(handles[i]);
            neededexceptions+=2;

         }else if (handles[i].getInstruction() instanceof org.apache.bcel.generic.GotoInstruction){
            if (stack.getInstructionContext(handles[i]).getStackSize()==0){
               branches.add(handles[i]);
               neededexceptions++;
            }
         }else if (handles[i].getInstruction() instanceof org.apache.bcel.generic.Select){
            if (stack.getInstructionContext(handles[i]).getStackSize()==1){
               org.apache.bcel.generic.Select select = 
                  (org.apache.bcel.generic.Select)handles[i].getInstruction();
               org.apache.bcel.generic.InstructionHandle[] cases = 
                  select.getTargets();

               branches.add(handles[i]);
               neededexceptions+=select.getMatchs().length+1;
            }
         }
      }

      // if i didn't find any good branches, quit
      if (branches.size()==0)
         return;
      
      // create the exception classes
      sandmark.program.Class[] exclasses = 
         makeExceptionClasses(method, neededexceptions);

      java.util.ArrayList newhandlers = new java.util.ArrayList();
      // the new exception handlers I will make

      // maps Instructions to Integers
      java.util.Hashtable branch2position = new java.util.Hashtable();
      // maps Instructions to String[]s
      java.util.Hashtable branch2exceptions = new java.util.Hashtable();
      // maps Select Instructions to int[]s
      java.util.Hashtable switch2matches = new java.util.Hashtable();

      // add the global handlers for each exception
      int exindex=0;
      for (int i=0;i<branches.size();i++){
         org.apache.bcel.generic.InstructionHandle branch = 
            (org.apache.bcel.generic.InstructionHandle)branches.get(i);
         
         if (branch.getInstruction() instanceof org.apache.bcel.generic.IfInstruction){
            org.apache.bcel.generic.IfInstruction inst = 
               (org.apache.bcel.generic.IfInstruction)branch.getInstruction();

            short code = inst.getOpcode();
            int numargs=2;
            org.apache.bcel.generic.Type[] args =
               new org.apache.bcel.generic.Type[]{
                  org.apache.bcel.generic.Type.INT,
                  org.apache.bcel.generic.Type.INT,
                  org.apache.bcel.generic.Type.INT
               };
            
            switch(code){
            case IF_ACMPEQ: case IF_ACMPNE:
               args =
                  new org.apache.bcel.generic.Type[]{
                     org.apache.bcel.generic.Type.OBJECT,
                     org.apache.bcel.generic.Type.OBJECT,
                     org.apache.bcel.generic.Type.INT
                  };
               break;

            case IFNONNULL: case IFNULL:
               numargs=1;
               args =
                  new org.apache.bcel.generic.Type[]{
                     org.apache.bcel.generic.Type.OBJECT,
                     org.apache.bcel.generic.Type.INT
                  };
               break;

            case IFEQ: case IFGE: case IFGT:
            case IFLE: case IFLT: case IFNE:
               numargs=1;
               args =
                  new org.apache.bcel.generic.Type[]{
                     org.apache.bcel.generic.Type.INT,
                     org.apache.bcel.generic.Type.INT
                  };
               break;
            }
            
            // if instructions will call DispatcherException.{ifeq, ifge, if_icmpeq, ...}
            // the name of the static method will equal the instruction name.

            org.apache.bcel.generic.InstructionHandle top, bottom;
            top = ilist.insert(branch, factory.createConstant(new Integer(POSITION_ITERATOR)));
            ilist.insert(branch, factory.createInvoke("sandmark.obfuscate.exceptionbranches.DispatcherException", 
                                                      branch.getInstruction().getName(),
                                                      org.apache.bcel.generic.Type.getType("Ljava/lang/Throwable;"),
                                                      args,
                                                      INVOKESTATIC));
            bottom = ilist.insert(branch, org.apache.bcel.generic.InstructionConstants.ATHROW);
            
            // we use '=' - delimited strings for simplicity of registering them later (see fixClinit)
            String exes = exclasses[exindex].getName()+"="+ 
               exclasses[exindex+1].getName();
            
            // need 2 handlers since there are two possible targets
            org.apache.bcel.generic.CodeExceptionGen handler1 = 
               new org.apache.bcel.generic.CodeExceptionGen
               (bottom, bottom, inst.getTarget(), exclasses[exindex++].getType());
            
            org.apache.bcel.generic.CodeExceptionGen handler2 = 
               new org.apache.bcel.generic.CodeExceptionGen
               (bottom, bottom, branch.getNext(), exclasses[exindex++].getType());

            newhandlers.add(handler1);
            newhandlers.add(handler2);

            branch2position.put(inst, new Integer(POSITION_ITERATOR));
            branch2exceptions.put(inst, exes);
            POSITION_ITERATOR++;

            updateTargeters(branch, top, bottom);
            
            try{
               inst.setTarget(null);
               ilist.delete(branch);
            }catch(Throwable t){
               System.err.println("This shouldn't happen!!!");
            }
            
         }else if (branch.getInstruction() instanceof org.apache.bcel.generic.GotoInstruction){
            org.apache.bcel.generic.GotoInstruction inst = 
               (org.apache.bcel.generic.GotoInstruction)branch.getInstruction();

            // goto instructions are replaced with DispatcherException.dogoto,
            // since I can't make a method called 'goto'.
            org.apache.bcel.generic.InstructionHandle top, bottom;
            top = ilist.insert(branch, factory.createConstant(new Integer(POSITION_ITERATOR)));
            ilist.insert(branch, factory.createInvoke("sandmark.obfuscate.exceptionbranches.DispatcherException", "dogoto",
                                                      org.apache.bcel.generic.Type.getType("Ljava/lang/Throwable;"),
                                                      new org.apache.bcel.generic.Type[]{
                                                         org.apache.bcel.generic.Type.INT
                                                      },
                                                      INVOKESTATIC));
            bottom = ilist.insert(branch, org.apache.bcel.generic.InstructionConstants.ATHROW);
            
            String exes = exclasses[exindex].getName();

            // only 1 possible target
            org.apache.bcel.generic.CodeExceptionGen handler = 
               new org.apache.bcel.generic.CodeExceptionGen
               (bottom, bottom, inst.getTarget(), exclasses[exindex++].getType());

            newhandlers.add(handler);

            branch2position.put(inst, new Integer(POSITION_ITERATOR));
            branch2exceptions.put(inst, exes);

            POSITION_ITERATOR++;

            updateTargeters(branch, top, bottom);
            
            try{
               inst.setTarget(null);
               ilist.delete(branch);
            }catch(Throwable t){
               System.err.println("This shouldn't happen!!!");
            }

         }else if (branch.getInstruction() instanceof org.apache.bcel.generic.Select){
            org.apache.bcel.generic.Select select = 
               (org.apache.bcel.generic.Select)branch.getInstruction();

            // LOOKUPSWITCH and TABLESWITCH instructions both act the same in theory...
            // they just have different ways of organizing their match-target pairs.
            // they both get replaced with DispatcherException.doswitch

            org.apache.bcel.generic.InstructionHandle top, bottom;
            top = ilist.insert(branch, factory.createConstant(new Integer(POSITION_ITERATOR)));
            ilist.insert(branch, factory.createInvoke("sandmark.obfuscate.exceptionbranches.DispatcherException", "doswitch",
                                                      org.apache.bcel.generic.Type.getType("Ljava/lang/Throwable;"),
                                                      new org.apache.bcel.generic.Type[]{
                                                         org.apache.bcel.generic.Type.INT,
                                                         org.apache.bcel.generic.Type.INT
                                                      },
                                                      INVOKESTATIC));
            bottom = ilist.insert(branch, org.apache.bcel.generic.InstructionConstants.ATHROW);

            int[] matches = select.getMatchs();
            org.apache.bcel.generic.InstructionHandle[] cases = 
               select.getTargets();
            org.apache.bcel.generic.InstructionHandle defaultcase = 
               select.getTarget();

            String matchstr="";
            String exes = exclasses[exindex].getName()+"=";
            for (int j=0;j<cases.length;j++){
               exes += exclasses[exindex+j+1].getName()+"=";
               matchstr+=matches[j]+"=";
            }
            
            branch2exceptions.put(select, exes);
            branch2position.put(select, new Integer(POSITION_ITERATOR));
            switch2matches.put(select, matchstr);

            // there's matches.length+1 handlers, since the matches don't include the 'default' case.
            org.apache.bcel.generic.CodeExceptionGen[] switchhandlers = 
               new org.apache.bcel.generic.CodeExceptionGen[cases.length+1];
            
            switchhandlers[0] = new org.apache.bcel.generic.CodeExceptionGen
               (bottom, bottom, defaultcase, exclasses[exindex++].getType());
            newhandlers.add(switchhandlers[0]);
            
            for (int j=0;j<cases.length;j++){
               switchhandlers[j+1] = new org.apache.bcel.generic.CodeExceptionGen
                  (bottom, bottom, cases[j], exclasses[exindex++].getType());
               newhandlers.add(switchhandlers[j+1]);
            }
            
            POSITION_ITERATOR++;
            
            updateTargeters(branch, top, bottom);
            
            try{
               select.setTarget(null);
               ilist.delete(branch);
            }catch(Throwable t){
               System.err.println("This shouldn't happen!!!");
            }
         }
      }
      
      // now add ACONST_NULL and POP everywhere, so that fallthroughs still work
      // i.e:  ACONST_NULL
      //       POP
      //       <old branch target>
      //       ....
      // 
      //  (then we redirect all branches from old target to the POP)
      
      java.util.HashSet donetargets = new java.util.HashSet();
      for (int i=0;i<newhandlers.size();i++){
         org.apache.bcel.generic.CodeExceptionGen gen = 
            (org.apache.bcel.generic.CodeExceptionGen)newhandlers.get(i);

         org.apache.bcel.generic.InstructionHandle ih, target = 
            gen.getHandlerPC();
         if (donetargets.contains(target))
            continue;
         
         ilist.insert(target, org.apache.bcel.generic.InstructionConstants.ACONST_NULL);
         ih=ilist.insert(target, org.apache.bcel.generic.InstructionConstants.POP);
         
         updateTargeters(target, ih, ih);
         donetargets.add(target);
         donetargets.add(ih);
      }

      // update clinit to make calls to DispatcherException.register 
      // and DispatcherException.registerSwitch
      fixClinit(method, branch2position, 
                branch2exceptions, switch2matches);

      // clear then out of the Method since we kept backups
      method.removeExceptionHandlers();

      // add in the new handlers, then null out their targets
      for (int i=0;i<newhandlers.size();i++){
         org.apache.bcel.generic.CodeExceptionGen gen = 
            (org.apache.bcel.generic.CodeExceptionGen)newhandlers.get(i);
         method.addExceptionHandler(gen.getStartPC(), gen.getEndPC(),
                                    gen.getHandlerPC(), gen.getCatchType());
         gen.setStartPC(null);
         gen.setEndPC(null);
         gen.setHandlerPC(null);
      }

      // add in the old handlers, then null out their targets
      for (int i=0;i<handlers.size();i++){
         org.apache.bcel.generic.CodeExceptionGen gen = 
            (org.apache.bcel.generic.CodeExceptionGen)handlers.get(i);
         method.addExceptionHandler(gen.getStartPC(), gen.getEndPC(),
                                    gen.getHandlerPC(), gen.getCatchType());
         gen.setStartPC(null);
         gen.setEndPC(null);
         gen.setHandlerPC(null);
      }

      // add the DispatcherException class to the jar (if not already there)
      addDispatcherClass();

      method.mark();
   }


   /** This method will add DispatcherException to the jar, 
    *  if it hasn't been added already
    */
   private void addDispatcherClass(){
      if (!addedDispatcher){
         try{
            java.io.InputStream stream =
               getClass().getResourceAsStream
               ("/sandmark/obfuscate/exceptionbranches/DispatcherException.class");
            org.apache.bcel.classfile.JavaClass jc = 
               new org.apache.bcel.classfile.ClassParser
               (stream,"sandmark.obfuscate.exceptionbranches.DispatcherException").parse();
            new sandmark.program.LocalClass(app,jc);
            addedDispatcher=true;
         }catch(Exception e) {
            throw new Error("Couldn't add Dispatcher class");
         }
      }
   }


   /** Updates/adds the <clinit> method to the parent class,
    *  to add calls to DispctaherException.register and
    *  DispatcherException.registerSwitch.
    */
   private void fixClinit(sandmark.program.Method method,
                          java.util.Hashtable branch2position,
                          java.util.Hashtable branch2exceptions,
                          java.util.Hashtable switch2matches){


      sandmark.program.Method clinit = method.getEnclosingClass().getMethod("<clinit>", "()V");
      org.apache.bcel.generic.InstructionList clinitlist=null;
      if (clinit==null){
         // if it doesn't exist, make it!
         clinitlist=new org.apache.bcel.generic.InstructionList();
         clinitlist.append(org.apache.bcel.generic.InstructionConstants.RETURN);
         
         clinit = new sandmark.program.LocalMethod
            (method.getEnclosingClass(), 
             org.apache.bcel.Constants.ACC_PUBLIC |
             org.apache.bcel.Constants.ACC_STATIC,
             org.apache.bcel.generic.Type.VOID,
             org.apache.bcel.generic.Type.NO_ARGS,
             null, "<clinit>", clinitlist);
      }else{
         clinitlist = clinit.getInstructionList();
      }
      
      org.apache.bcel.generic.InstructionFactory factory = 
         new org.apache.bcel.generic.InstructionFactory(clinit.getConstantPool());

      // register each branch
      for (java.util.Enumeration keyiter=branch2position.keys();keyiter.hasMoreElements();){
         org.apache.bcel.generic.Instruction branch = 
            (org.apache.bcel.generic.Instruction)keyiter.nextElement();
         Integer position = (Integer)branch2position.get(branch);
         String exceptions = (String)branch2exceptions.get(branch);
         
         if (branch instanceof org.apache.bcel.generic.Select){
            String defaultex = exceptions.substring(0,exceptions.indexOf('='));
            exceptions = exceptions.substring(exceptions.indexOf('=')+1);
            String matches = (String)switch2matches.get(branch);
            
            clinitlist.insert(factory.createInvoke("sandmark.obfuscate.exceptionbranches.DispatcherException", 
                                                   "registerSwitch", 
                                                   org.apache.bcel.generic.Type.VOID,
                                                   new org.apache.bcel.generic.Type[]{
                                                      org.apache.bcel.generic.Type.INT,
                                                      org.apache.bcel.generic.Type.STRING,
                                                      org.apache.bcel.generic.Type.STRING,
                                                      org.apache.bcel.generic.Type.STRING
                                                   },
                                                   INVOKESTATIC));
            clinitlist.insert(factory.createConstant(defaultex));
            clinitlist.insert(factory.createConstant(exceptions));
            clinitlist.insert(factory.createConstant(matches));
            clinitlist.insert(factory.createConstant(position));
         }else{
            clinitlist.insert(factory.createInvoke("sandmark.obfuscate.exceptionbranches.DispatcherException", 
                                                   "register", 
                                                   org.apache.bcel.generic.Type.VOID,
                                                   new org.apache.bcel.generic.Type[]{
                                                      org.apache.bcel.generic.Type.INT,
                                                      org.apache.bcel.generic.Type.STRING
                                                   },
                                                   INVOKESTATIC));
            clinitlist.insert(factory.createConstant(exceptions));
            clinitlist.insert(factory.createConstant(position));
         }
      }
   }

   /** This method does smart targeter updating. 
    *  @param old the old target
    *  @param top the first instruction of the new list replacing 'old'
    *  @param bottom the last instruction of the new list replacing 'old'
    */
   private static void updateTargeters(org.apache.bcel.generic.InstructionHandle old,
                                       org.apache.bcel.generic.InstructionHandle top,
                                       org.apache.bcel.generic.InstructionHandle bottom){

      org.apache.bcel.generic.InstructionTargeter[] targeters = old.getTargeters();

      // if there are no targeters, 'targeters' will be null (bad design!!)
      if (targeters==null)
         return;

      // the only difference is that you probably want any exception handlers ending
      // at 'old' to now end at 'bottom' rather than 'top'.
      for (int i=0;i<targeters.length;i++){
         if (targeters[i] instanceof org.apache.bcel.generic.CodeExceptionGen){
            org.apache.bcel.generic.CodeExceptionGen gen = 
               (org.apache.bcel.generic.CodeExceptionGen)targeters[i];
            if (gen.getStartPC()==old)
               gen.setStartPC(top);
            if (gen.getEndPC()==old)
               gen.setEndPC(bottom);
            if (gen.getHandlerPC()==old)
               gen.setHandlerPC(top);
         }else{
            targeters[i].updateTarget(old, top);
         }
      }
   }

   /** This will make, add, and return the list of exception 
    *  classes needed for a given method. There will be exactly 'howmany' of them.
    *  ("There are howmany?" "Exactly!"). They might not actually be created by this call...
    *  we cache classes so that we can reuse them between methods.
    */
   private sandmark.program.Class[] makeExceptionClasses
      (sandmark.program.Method method, int howmany){

      java.util.ArrayList newclasses = new java.util.ArrayList(howmany);

      // try to take as many as we can from the list of already-made ones
      for (int i=0;howmany>0 && i<exceptionList.size();i++){
         newclasses.add(exceptionList.get(i));
         howmany--;
      }
      
      // if you need more, make 'em
      for (int i=0;i<howmany;i++){
         sandmark.program.LocalClass newclass = 
            new sandmark.program.LocalClass(app, "SandmarkException"+(exceptionList.size()), 
                                            "java.lang.Throwable", null, 
                                            org.apache.bcel.Constants.ACC_PUBLIC,
                                            null);
         newclasses.add(newclass);
         exceptionList.add(newclass);
                                            
         org.apache.bcel.generic.InstructionFactory factory = 
            new org.apache.bcel.generic.InstructionFactory(newclass.getConstantPool());

         org.apache.bcel.generic.InstructionList ilist =
            new org.apache.bcel.generic.InstructionList();
         ilist.append(org.apache.bcel.generic.InstructionConstants.ALOAD_0);
         ilist.append(factory.createInvoke("java.lang.Throwable", "<init>",
                                           org.apache.bcel.generic.Type.VOID,
                                           org.apache.bcel.generic.Type.NO_ARGS,
                                           org.apache.bcel.Constants.INVOKESPECIAL));
         ilist.append(org.apache.bcel.generic.InstructionConstants.RETURN);

         // these classes directly extend Throwable and 
         // have nothing but a no-arg constructor that just calls super().

         new sandmark.program.LocalMethod(newclass, org.apache.bcel.Constants.ACC_PUBLIC,
                                          org.apache.bcel.generic.Type.VOID,
                                          org.apache.bcel.generic.Type.NO_ARGS,
                                          null, "<init>", ilist);
      }

      return (sandmark.program.Class[])newclasses.toArray
         (new sandmark.program.Class[0]);
   }


   public static void main(String args[]) throws Throwable{
      if (args.length<1)
         return;
      
      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);
      ExceptionBranches eb = new ExceptionBranches(app);

      if (args.length>=4){
         sandmark.program.Method method = app.getClass(args[1]).getMethod(args[2], args[3]);
         eb.apply(method);
      }else if (args.length==2){
         sandmark.program.Class clazz = app.getClass(args[1]);
         sandmark.program.Method[] methods = clazz.getMethods();
         for (int i=0;i<methods.length;i++){
            eb.apply(methods[i]);
         }
      }else if (args.length==1){
         sandmark.program.Class[] classes = app.getClasses();
         for (int i=0;i<classes.length;i++){
            sandmark.program.Method[] methods = classes[i].getMethods();
            for (int j=0;j<methods.length;j++){
               eb.apply(methods[j]);
            }
         }
      }

      app.save(args[0]+".out");
   }
}
