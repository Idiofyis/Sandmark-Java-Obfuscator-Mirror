package sandmark.watermark.execpath;

/** This class modifies an Application to produce an execution trace
 *  of itself at runtime. To do this, we add a new class to the application
 *  that has a single static Vector in it, and at the beginning of every basic block 
 *  throughout the rest of the application, code is inserted to add a string into 
 *  this static vector that contains information about the current state of the program.
 *  This is the first phase of the execution path watermarker.
 */
public class Tracer{
   private static final java.util.Hashtable typehash = new java.util.Hashtable(11);
   private static final int UNPRINTABLE   = 0;
   private static final int BASIC         = 1;
   private static final int OBJECT        = 2;

   static{
      typehash.put(org.apache.bcel.generic.Type.BOOLEAN, "java.lang.Boolean");
      typehash.put(org.apache.bcel.generic.Type.CHAR, "java.lang.Character");
      typehash.put(org.apache.bcel.generic.Type.BYTE, "java.lang.Byte");
      typehash.put(org.apache.bcel.generic.Type.DOUBLE, "java.lang.Double");
      typehash.put(org.apache.bcel.verifier.statics.DOUBLE_Upper.theInstance(), "java.lang.Double");
      typehash.put(org.apache.bcel.generic.Type.FLOAT, "java.lang.Float");
      typehash.put(org.apache.bcel.generic.Type.INT, "java.lang.Integer");
      typehash.put(org.apache.bcel.generic.Type.LONG, "java.lang.Long");
      typehash.put(org.apache.bcel.verifier.statics.LONG_Upper.theInstance(), "java.lang.Long");
      typehash.put(org.apache.bcel.generic.Type.SHORT, "java.lang.Short");
      
   }

   private sandmark.program.Application application;
   private String listHolderClassName;
   // the name of the class holding the static vector

   private sandmark.program.Class listHolderClass;
   // the Class object for the class holding the static vector

   private sandmark.program.Field listfield;
   // the Field object for the static vector itself


   /** Modifies the given application to contain trace code.
    */
   public Tracer(sandmark.program.Application app, boolean traceVars) throws Exception{
      application = app;
      sandmark.program.Class[] classes = application.getClasses();

      setupListHolderClass();
                                          
      // for each class
      for (int c=0;c<classes.length;c++){
         sandmark.program.Class clazz = classes[c];
         if (clazz.isInterface())
            continue;
         org.apache.bcel.generic.ConstantPoolGen cpg = clazz.getConstantPool();
         org.apache.bcel.generic.InstructionFactory factory = new org.apache.bcel.generic.InstructionFactory(cpg);


         // for each method
         for (java.util.Iterator methodIter=clazz.methods();methodIter.hasNext(); ){
            sandmark.program.Method method                        = (sandmark.program.Method)methodIter.next();
            org.apache.bcel.generic.InstructionList ilist         = method.getInstructionList();
            sandmark.analysis.initialized.Initialized inittest    = null;
            sandmark.analysis.stacksimulator.StackSimulator stack = null;
            sandmark.analysis.controlflowgraph.MethodCFG cfg      = null;
            sandmark.analysis.defuse.ReachingDefs localdefs       = null;

            if (method.getName().equals("<init>"))
               continue;
            // REMOVE THIS LATER!!!!!

            sandmark.program.Method oldmethod = method.copy();

            int firstUninitializedLocal = 0;
            if (traceVars){
               if(!method.isStatic())
                  firstUninitializedLocal++;
               org.apache.bcel.generic.Type argTypes[] =
                  method.getArgumentTypes();
               for(int i = 0 ; i < argTypes.length ; i++)
                  firstUninitializedLocal += argTypes[i].getSize();
            }
            method.setMaxStack(5+method.getMaxStack());
            method.removeLocalVariables();
            method.removeLineNumbers();


            try{
                cfg = method.getCFG();
                if (traceVars){
                   stack = method.getStack();
                   localdefs = new sandmark.analysis.defuse.ReachingDefs(method);
                   inittest = new sandmark.analysis.initialized.Initialized(method);
                }
            }catch(sandmark.analysis.controlflowgraph.EmptyMethodException eme){
               continue;
            }


            java.util.Hashtable stringToSubroutine = new java.util.Hashtable();
            // hashes strings to basic blocks
            java.util.Hashtable bbToLastAndSubroutine = new java.util.Hashtable();
            // hashes original BBs to pairs of BB[2]{last, subroutine}, where subroutine may be null


            
            // for each basic block
            for (java.util.Iterator bbiter=cfg.basicBlockIterator();bbiter.hasNext(); ){
               sandmark.analysis.controlflowgraph.BasicBlock bb = 
                  (sandmark.analysis.controlflowgraph.BasicBlock)bbiter.next();
               
               if (bb.getInstList().size()==0)
                  continue;

               org.apache.bcel.generic.InstructionHandle bbfirst = 
                  (org.apache.bcel.generic.InstructionHandle)bb.getInstList().get(0);

               
               if (traceVars){
                  // determine how to print each local var
                  // and which subroutine to jump to 
                  String localstring = "";
                  int[] localtypes = new int[method.getMaxLocals()];
                  for (int i=0;i<localtypes.length;i++){
                     java.util.Set defs = localdefs.defs(i,bbfirst);
                     if ((!defs.isEmpty()) && (!hasMultipleTypes(defs)) && inittest.initializedAt(i,bbfirst)){
                        // hacky test to see if I'm preceeded by the first half
                        // of a long or double
                        if (i>0){
                           java.util.Set prevdefs = localdefs.defs(i-1, bbfirst);
                           boolean prevIsLong = false;
                           for (java.util.Iterator defiter = prevdefs.iterator(); defiter.hasNext(); ){
                              sandmark.analysis.defuse.DefWrapper prev = 
                                 (sandmark.analysis.defuse.DefWrapper)defiter.next();
                              if (prev.getType().equals(org.apache.bcel.generic.Type.LONG) || 
                                  prev.getType().equals(org.apache.bcel.generic.Type.DOUBLE) || 
                                  prev.getType() instanceof org.apache.bcel.verifier.statics.LONG_Upper || 
                                  prev.getType() instanceof org.apache.bcel.verifier.statics.DOUBLE_Upper){
                                 prevIsLong = true;
                                 break;
                              }
                           }
                           if (prevIsLong){
                              localtypes[i] = UNPRINTABLE;
                              continue;
                           }
                        }
                        
                        sandmark.analysis.defuse.DefWrapper wrapper = 
                           (sandmark.analysis.defuse.DefWrapper)defs.iterator().next();
                        localtypes[i] = isPrintable(wrapper.getType());
                        localstring += "L["+i+"]="+wrapper.getType().getSignature() + ":";
                     }else{
                        localtypes[i] = UNPRINTABLE;
                     }
                  }
                  // get stack info
                  String stackstring="|";
                  sandmark.analysis.stacksimulator.Context context = 
                     stack.getInstructionContext(bbfirst);
                  for (int i=0;i<context.getStackSize();i++)
                     stackstring += context.getStackAt(i)[0].getType().getSignature() + ":";
                  localstring += stackstring;
                  
                  
                  // this BB will have either a JSR or an inlined subroutine.
                  // it goes right before the current BB
                  org.apache.bcel.generic.InstructionList lastlist = 
                     new org.apache.bcel.generic.InstructionList();
                  org.apache.bcel.generic.InstructionList subroutinelist = 
                     (org.apache.bcel.generic.InstructionList)stringToSubroutine.get(localstring);
                  
                  if (subroutinelist==null){
                     // no subroutine, make it
                     org.apache.bcel.generic.InstructionList jsrlist = 
                        getSubroutine(localtypes, localdefs, cpg, cfg, 
                                      factory, clazz.getName(), method, bb);
                     
                     boolean inline = localstring.indexOf("<UNINITIALIZED OBJECT OF TYPE")!=-1 ||
                        localstring.indexOf(";")!=-1;
                     
                     if (inline){
                        // inline the subroutine
                        jsrlist.delete(jsrlist.getEnd());
                        jsrlist.delete(jsrlist.getStart());
                        lastlist = jsrlist;
                     }else{
                        subroutinelist = jsrlist;
                        stringToSubroutine.put(localstring, subroutinelist);
                        lastlist.append(new org.apache.bcel.generic.JSR(subroutinelist.getStart()));
                     }
                  }else{
                     // subroutine already exists, put in a jump to it
                     lastlist.append(new org.apache.bcel.generic.JSR(subroutinelist.getStart()));
                  }
                  
                  bbToLastAndSubroutine.put(bb, new org.apache.bcel.generic.InstructionList[]{lastlist, subroutinelist});

               }else{
                  // not tracing variables
                  org.apache.bcel.generic.InstructionList traceList = getTraceCode(bb, method, clazz.getName(), cpg, cfg, factory);
                  bbToLastAndSubroutine.put(bb, new org.apache.bcel.generic.InstructionList[]{traceList, null});
               }
            }


            // now that I've made all the subroutines and last blocks, add them to the cfg with edges
            for (java.util.Iterator bbiter = cfg.basicBlockIterator(); bbiter.hasNext(); ){
               sandmark.analysis.controlflowgraph.BasicBlock bb = 
                  (sandmark.analysis.controlflowgraph.BasicBlock)bbiter.next();
               if (bb.getInstList().size()==0)
                  continue;

               org.apache.bcel.generic.InstructionHandle bbfirst = 
                  (org.apache.bcel.generic.InstructionHandle)bb.getInstList().get(0);

               org.apache.bcel.generic.InstructionList[] lastSubroutine = 
                  (org.apache.bcel.generic.InstructionList[])bbToLastAndSubroutine.get(bb);
               
               org.apache.bcel.generic.InstructionList lastlist = lastSubroutine[0];
               org.apache.bcel.generic.InstructionList subroutinelist = lastSubroutine[1];

               sandmark.analysis.controlflowgraph.BasicBlock last = 
                  new sandmark.analysis.controlflowgraph.BasicBlock(cfg);
               sandmark.analysis.controlflowgraph.BasicBlock subroutine = null;

               org.apache.bcel.generic.InstructionHandle instr0 = lastlist.getStart();

               // add last
               cfg.addBlock(last);
               org.apache.bcel.generic.InstructionHandle[] lasthandles = 
                  lastlist.getInstructionHandles();
               for (int i=0;i<lasthandles.length;i++)
                  last.addInst(lasthandles[i]);
               ilist.append(lastlist);

               // add subroutine
               if (subroutinelist!=null){
                  subroutine = new sandmark.analysis.controlflowgraph.BasicBlock(cfg);
                  cfg.addBlock(subroutine);
                  org.apache.bcel.generic.InstructionHandle[] subroutinehandles = 
                     subroutinelist.getInstructionHandles();
                  for (int i=0;i<subroutinehandles.length;i++)
                     subroutine.addInst(subroutinehandles[i]);
                  ilist.append(subroutinelist);
               }

               // reset all branches that target the beginning of bb to the beginning of last
               ilist.redirectBranches(bbfirst, instr0);

               // DO NOT REPLACE THIS WITH ilist.redirectExceptionHandlers!!!!
               org.apache.bcel.generic.CodeExceptionGen[] exceptions = method.getExceptionHandlers();
               for (int i=0;i<exceptions.length;i++){
                  if (exceptions[i].getStartPC()==bbfirst)
                     exceptions[i].setStartPC(instr0);
                  if (exceptions[i].getHandlerPC()==bbfirst)
                     exceptions[i].setHandlerPC(instr0);
               }

               // fix CFG edges
               for (java.util.Iterator edgeIter = cfg.inEdges(bb);edgeIter.hasNext(); ){
                  sandmark.util.newgraph.EdgeImpl edge = 
                     (sandmark.util.newgraph.EdgeImpl)edgeIter.next();
                  Object source = edge.sourceNode();
                  
                  if (edge instanceof sandmark.analysis.controlflowgraph.FallthroughEdge){
                     cfg.addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge(source, last));
                  }else if (edge instanceof sandmark.analysis.controlflowgraph.ExceptionEdge){
                     sandmark.analysis.controlflowgraph.ExceptionEdge exedge = 
                        (sandmark.analysis.controlflowgraph.ExceptionEdge)edge;
                     cfg.addEdge(new sandmark.analysis.controlflowgraph.ExceptionEdge(source, last, exedge.exception()));
                  }else{
                     cfg.addEdge(source, last);
                  }
                  cfg.removeEdge(edge);
               }

               if (bb.fallthroughFrom()!=null)
                  bb.fallthroughFrom().setFallthrough(last);

               last.setFallthrough(bb);
               if (subroutine!=null){
                  cfg.addEdge(last, subroutine);
                  cfg.addEdge(subroutine, bb);
               }else{
                  cfg.addEdge(last, bb);
               }
            }


            try{
               // getByteCode() might throw an exception if the method gets
               // too big, or if the branches get spaced too far away
               // so that the offsets are >32k

               cfg.rewriteInstructionList();
               method.getInstructionList().getByteCode();
               clazz.removeMethod(oldmethod);
               method.mark();
            }catch(Exception ex){
               System.out.println("method too big");
               String name = method.getName();
               method.delete();
               oldmethod.setName(name);
               oldmethod.mark();
            }
         }
         
         makePrinter(clazz, traceVars, factory);
         clazz.mark();
      }
   }

   private org.apache.bcel.generic.InstructionList getTraceCode(sandmark.analysis.controlflowgraph.BasicBlock bb,
                                                                sandmark.program.Method method, String classname,
                                                                org.apache.bcel.generic.ConstantPoolGen cpg,
                                                                sandmark.analysis.controlflowgraph.MethodCFG cfg,
                                                                org.apache.bcel.generic.InstructionFactory factory){

      org.apache.bcel.generic.InstructionList ilist = 
         new org.apache.bcel.generic.InstructionList();

      org.apache.bcel.generic.InstructionHandle bbfirst = 
         (org.apache.bcel.generic.InstructionHandle)bb.getInstList().get(0);

      String typestring = "{}:";
      if (bb.getLastInstruction().getInstruction() instanceof org.apache.bcel.generic.IfInstruction)
         typestring = "{if}:";
      else if (bb.getLastInstruction().getInstruction() instanceof org.apache.bcel.generic.Select)
         typestring = "{switch}:";
      
      ilist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(bbfirst.getPosition())));
      ilist.append(new org.apache.bcel.generic.ACONST_NULL());
      ilist.append(new org.apache.bcel.generic.LDC(cpg.addString(method.getName()+method.getSignature())));
      ilist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(cfg.numSuccs(bb))));
      ilist.append(new org.apache.bcel.generic.LDC(cpg.addString(typestring)));
      ilist.append(factory.createInvoke(classname, "__sandmarkprinter", 
                                        org.apache.bcel.generic.Type.VOID,
                                        new org.apache.bcel.generic.Type[]{
                                           org.apache.bcel.generic.Type.INT,
                                           org.apache.bcel.generic.Type.getType("L"+classname.replace('.', '/')+";"),
                                           org.apache.bcel.generic.Type.STRING,
                                           org.apache.bcel.generic.Type.INT,
                                           org.apache.bcel.generic.Type.STRING
                                        },
                                        org.apache.bcel.Constants.INVOKESTATIC));
      ilist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "finish",
                                        org.apache.bcel.generic.Type.VOID, 
                                        org.apache.bcel.generic.Type.NO_ARGS,
                                        org.apache.bcel.Constants.INVOKESTATIC));
      
      return ilist;
   }

   private org.apache.bcel.generic.InstructionList getSubroutine(int[] localtypes, sandmark.analysis.defuse.ReachingDefs localdefs,
                                                                 org.apache.bcel.generic.ConstantPoolGen cpg,
                                                                 sandmark.analysis.controlflowgraph.MethodCFG cfg,
                                                                 org.apache.bcel.generic.InstructionFactory factory, String classname,
                                                                 sandmark.program.Method method, sandmark.analysis.controlflowgraph.BasicBlock bb){

      org.apache.bcel.generic.InstructionList jsrlist = 
         new org.apache.bcel.generic.InstructionList();

      org.apache.bcel.generic.InstructionHandle bbfirst = 
         (org.apache.bcel.generic.InstructionHandle)bb.getInstList().get(0);



      String typestring = "{}:";
      if (bb.getLastInstruction().getInstruction() instanceof org.apache.bcel.generic.IfInstruction)
         typestring = "{if}:";
      else if (bb.getLastInstruction().getInstruction() instanceof org.apache.bcel.generic.Select)
         typestring = "{switch}:";
      
      
      jsrlist.append(new org.apache.bcel.generic.ASTORE(method.getMaxLocals()));
      jsrlist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(bbfirst.getPosition())));
      if (method.isStatic() || method.getName().equals("<init>"))
         jsrlist.append(new org.apache.bcel.generic.ACONST_NULL());
      else
         jsrlist.append(new org.apache.bcel.generic.ALOAD(0));
      jsrlist.append(new org.apache.bcel.generic.LDC(cpg.addString(method.getName()+method.getSignature())));
      jsrlist.append(new org.apache.bcel.generic.LDC(cpg.addInteger(cfg.numSuccs(bb))));
      jsrlist.append(new org.apache.bcel.generic.LDC(cpg.addString(typestring)));
      jsrlist.append(factory.createInvoke(classname, "__sandmarkprinter", 
                                          org.apache.bcel.generic.Type.VOID,
                                          new org.apache.bcel.generic.Type[]{
                                             org.apache.bcel.generic.Type.INT,
                                             org.apache.bcel.generic.Type.getType("L"+classname.replace('.', '/')+";"),
                                             org.apache.bcel.generic.Type.STRING,
                                             org.apache.bcel.generic.Type.INT,
                                             org.apache.bcel.generic.Type.STRING
                                          },
                                          org.apache.bcel.Constants.INVOKESTATIC));

      for (int i=0;i<localtypes.length;i++){
         if (localtypes[i]==UNPRINTABLE)
            continue;
         

         sandmark.analysis.defuse.DefWrapper wrapper = 
            (sandmark.analysis.defuse.DefWrapper)localdefs.defs(i, bbfirst).iterator().next();
         
         String sigstring = wrapper.getType().getSignature();
         if (wrapper.getType() instanceof org.apache.bcel.verifier.statics.DOUBLE_Upper)
            sigstring = "D";
         else if (wrapper.getType() instanceof org.apache.bcel.verifier.statics.LONG_Upper)                 
            sigstring = "L";

                  
         switch(localtypes[i]){
         case BASIC:
            org.apache.bcel.generic.Type argtype = wrapper.getType();
            if (argtype instanceof org.apache.bcel.verifier.statics.DOUBLE_Upper)
               argtype = org.apache.bcel.generic.Type.DOUBLE;
            else if (argtype instanceof org.apache.bcel.verifier.statics.LONG_Upper)
               argtype = org.apache.bcel.generic.Type.LONG;
            
            if (!(argtype.equals(org.apache.bcel.generic.Type.LONG) ||
                  argtype.equals(org.apache.bcel.generic.Type.DOUBLE) || 
                  argtype.equals(org.apache.bcel.generic.Type.FLOAT)))
               argtype = org.apache.bcel.generic.Type.INT;
            
            jsrlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":" + sigstring + " L["+i+"]=")));
            jsrlist.append(org.apache.bcel.generic.InstructionFactory.createLoad(wrapper.getType(), i));
            jsrlist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "concat",
                                                org.apache.bcel.generic.Type.VOID,
                                                new org.apache.bcel.generic.Type[]{
                                                   org.apache.bcel.generic.Type.STRING,
                                                   argtype
                                                },
                                                org.apache.bcel.Constants.INVOKESTATIC));
            break;
            
            
         case OBJECT:
            jsrlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":" + sigstring + " L["+i+"]=")));
            jsrlist.append(new org.apache.bcel.generic.ALOAD(i));
            jsrlist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "concat",
                                                org.apache.bcel.generic.Type.VOID,
                                                new org.apache.bcel.generic.Type[]{
                                                   org.apache.bcel.generic.Type.STRING,
                                                   org.apache.bcel.generic.Type.OBJECT
                                                },
                                                org.apache.bcel.Constants.INVOKESTATIC));
            break;
         }
      }
      
      jsrlist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "finish",
                                          org.apache.bcel.generic.Type.VOID, 
                                          org.apache.bcel.generic.Type.NO_ARGS,
                                          org.apache.bcel.Constants.INVOKESTATIC));
      jsrlist.append(new org.apache.bcel.generic.RET(method.getMaxLocals()));

      return jsrlist;
   }



   private void setupListHolderClass(){
      listHolderClassName = "sandmark.watermark.execpath.SandmarkListHolder";
      if(application.getClass(listHolderClassName) != null)
         throw new Error("Fatal Error: Application to be traced already contains trace class");
      
      
      java.io.InputStream listHolderClassStream = 
         getClass().getResourceAsStream
      ("/sandmark/watermark/execpath/SandmarkListHolder.class");
      
      try {
         listHolderClass = new sandmark.program.LocalClass
            (application,new org.apache.bcel.classfile.ClassParser
             (listHolderClassStream,"/sandmark/watermark/execpath/SandmarkListHolder.class").parse());
      } catch(java.io.IOException e) {
         throw new Error("Fatal Error: Trace class could not be read from sandmark.jar");
      }
      
      listfield = listHolderClass.getField("head","Lsandmark/watermark/execpath/SMLinkedList;");
      if(listfield == null)
         throw new Error("trace class lacks trace field");
      
      java.io.InputStream listClassStream = 
         getClass().getResourceAsStream
         ("/sandmark/watermark/execpath/SMLinkedList.class");
      
      try {
         new sandmark.program.LocalClass
            (application,new org.apache.bcel.classfile.ClassParser
             (listClassStream,"/sandmark/watermark/execpath/SMLinkedList.class").parse());
      } catch(java.io.IOException e) {
         throw new Error("Fatal Error: Trace class could not be read from sandmark.jar");
      }
   }

   private boolean hasMultipleTypes(java.util.Set defs){
      org.apache.bcel.generic.Type type=null;
      for (java.util.Iterator iter=defs.iterator();iter.hasNext(); ){
         Object obj = iter.next();
         org.apache.bcel.generic.Type mytype = ((sandmark.analysis.defuse.DefWrapper)obj).getType();
         if (type==null)
            type = mytype;
         else if (!type.equals(mytype))
            return true;
      }
      return false;
   }


   private int isPrintable(org.apache.bcel.generic.Type vartype){
      if (vartype==null)
         return UNPRINTABLE;
      if ((vartype instanceof org.apache.bcel.generic.BasicType) || 
          (vartype instanceof org.apache.bcel.verifier.statics.LONG_Upper) ||
          (vartype instanceof org.apache.bcel.verifier.statics.DOUBLE_Upper))
         return BASIC;

      return OBJECT;
   }


   private sandmark.program.Method makePrinter(sandmark.program.Class clazz, boolean traceVars,
                                               org.apache.bcel.generic.InstructionFactory factory){

      org.apache.bcel.generic.ConstantPoolGen cpg = clazz.getConstantPool();
      
      org.apache.bcel.generic.InstructionList printerlist = new org.apache.bcel.generic.InstructionList();

      printerlist.append(new org.apache.bcel.generic.ALOAD(4));
      // "{if}:"

      printerlist.append(factory.createInvoke("java.lang.Thread", "currentThread",
                                              org.apache.bcel.generic.Type.getType("Ljava/lang/Thread;"),
                                              org.apache.bcel.generic.Type.NO_ARGS,
                                              org.apache.bcel.Constants.INVOKESTATIC));
      printerlist.append(factory.createInvoke("java.lang.System", "identityHashCode",
                                              org.apache.bcel.generic.Type.INT,
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.OBJECT
                                              },
                                              org.apache.bcel.Constants.INVOKESTATIC));
      printerlist.append(factory.createInvoke("java.lang.Integer", "toString",
                                              org.apache.bcel.generic.Type.STRING,
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.INT
                                              },
                                              org.apache.bcel.Constants.INVOKESTATIC));
      printerlist.append(factory.createInvoke("java.lang.String", "concat",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKEVIRTUAL));
      // "{if}:thread"

      printerlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":"+clazz.getName()+":")));
      printerlist.append(factory.createInvoke("java.lang.String", "concat",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKEVIRTUAL));
      // "{if}:thread:class:"
      printerlist.append(new org.apache.bcel.generic.ALOAD(2));
      printerlist.append(factory.createInvoke("java.lang.String", "concat",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKEVIRTUAL));
      // "{if}:thread:class:method"
      printerlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":")));
      printerlist.append(factory.createInvoke("java.lang.String", "concat",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKEVIRTUAL));
      // "{if}:thread:class:method:"
      printerlist.append(new org.apache.bcel.generic.ILOAD(0));
      printerlist.append(factory.createInvoke("java.lang.Integer", "toString",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.INT
                                              },
                                              org.apache.bcel.Constants.INVOKESTATIC));
      printerlist.append(factory.createInvoke("java.lang.String", "concat",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKEVIRTUAL));
      // "{if}:thread:class:method:offset"
      printerlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":")));
      printerlist.append(factory.createInvoke("java.lang.String", "concat",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKEVIRTUAL));
      // "{if}:thread:class:method:offset:"
      printerlist.append(new org.apache.bcel.generic.ILOAD(3));
      printerlist.append(factory.createInvoke("java.lang.Integer", "toString",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.INT
                                              },
                                              org.apache.bcel.Constants.INVOKESTATIC));
      printerlist.append(factory.createInvoke("java.lang.String", "concat",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKEVIRTUAL));
      // on stack: "{if}:thread:method:offset:numsuccs"
      printerlist.append(new org.apache.bcel.generic.LDC(cpg.addString("::")));
      printerlist.append(factory.createInvoke("java.lang.String", "concat",
                                              org.apache.bcel.generic.Type.STRING, 
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKEVIRTUAL));
      // on stack: "{if}:thread:method:offset:numsuccs::"
      printerlist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "start",
                                              org.apache.bcel.generic.Type.VOID,
                                              new org.apache.bcel.generic.Type[]{
                                                 org.apache.bcel.generic.Type.STRING
                                              },
                                              org.apache.bcel.Constants.INVOKESTATIC));
      

      if  (traceVars){
         // now do the fields
         sandmark.program.Field[] staticFields = new sandmark.program.Field[clazz.getFields().length];
         sandmark.program.Field[] instanceFields = new sandmark.program.Field[staticFields.length];
         int[] staticTypes = new int[staticFields.length];
         int[] instanceTypes = new int[instanceFields.length];
         int staticIndex=0, instanceIndex=0;
         
         // must separate out the static fields from the instance fields
         for (java.util.Iterator fielditer = clazz.fields(); fielditer.hasNext(); ){
            sandmark.program.Field field = (sandmark.program.Field)fielditer.next();
            if (field.isStatic()){
               staticFields[staticIndex] = field;
               staticTypes[staticIndex] = isPrintable(field.getType());
               staticIndex++;
            }else{
               instanceFields[instanceIndex] = field;
               instanceTypes[instanceIndex] = isPrintable(field.getType());
               instanceIndex++;
            }
         }
         
         // print out static fields
         for (int i=0;i<staticIndex;i++){
            if (staticTypes[i]==UNPRINTABLE)
               continue;
            
            switch(staticTypes[i]){
            case BASIC:
               org.apache.bcel.generic.Type argtype = staticFields[i].getType();
               
               if (!(argtype.equals(org.apache.bcel.generic.Type.LONG) ||
                     argtype.equals(org.apache.bcel.generic.Type.DOUBLE) || 
                     argtype.equals(org.apache.bcel.generic.Type.FLOAT)))
                  argtype = org.apache.bcel.generic.Type.INT;
               
               printerlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":static "+ staticFields[i].getType().getSignature() + " " +staticFields[i].getName()+"=")));
               printerlist.append(factory.createGetStatic(clazz.getName(), staticFields[i].getName(), staticFields[i].getType()));
               printerlist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "concat",
                                                       org.apache.bcel.generic.Type.VOID,
                                                       new org.apache.bcel.generic.Type[]{
                                                          org.apache.bcel.generic.Type.STRING,
                                                          argtype
                                                       },
                                                       org.apache.bcel.Constants.INVOKESTATIC));
               break;
               
               
            case OBJECT:
               printerlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":static "+ staticFields[i].getType().getSignature() + " " +staticFields[i].getName()+"=")));
               printerlist.append(factory.createGetStatic(clazz.getName(), staticFields[i].getName(), staticFields[i].getType()));
               printerlist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "concat",
                                                       org.apache.bcel.generic.Type.VOID,
                                                       new org.apache.bcel.generic.Type[]{
                                                          org.apache.bcel.generic.Type.STRING,
                                                          org.apache.bcel.generic.Type.OBJECT,
                                                       },
                                                       org.apache.bcel.Constants.INVOKESTATIC));
               break;
            }
         } // end for each static field
         
         // stack: out, "thread:method:numsuccs:num:S var1=___:S var2=___"
         
         
         // set up a branch to skip the instance fields, if no instance is supplied
         printerlist.append(new org.apache.bcel.generic.ALOAD(1));
         org.apache.bcel.generic.IFNULL ifnull = new org.apache.bcel.generic.IFNULL(null);
         // this target must be changed later!!
         printerlist.append(ifnull);
         
         
         // print out instance fields
         for (int i=0;i<instanceIndex;i++){
            if (instanceTypes[i]==UNPRINTABLE)
               continue;
            
            switch(instanceTypes[i]){
            case BASIC:
               org.apache.bcel.generic.Type argtype = instanceFields[i].getType();
               if (!(argtype.equals(org.apache.bcel.generic.Type.LONG) ||
                     argtype.equals(org.apache.bcel.generic.Type.DOUBLE) || 
                     argtype.equals(org.apache.bcel.generic.Type.FLOAT)))
                  argtype = org.apache.bcel.generic.Type.INT;
               
               printerlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":this "+instanceFields[i].getType().getSignature() + " "+  instanceFields[i].getName()+"=")));
               printerlist.append(new org.apache.bcel.generic.ALOAD(1));
               printerlist.append(factory.createGetField(clazz.getName(), instanceFields[i].getName(), instanceFields[i].getType()));
               printerlist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "concat",
                                                       org.apache.bcel.generic.Type.VOID,
                                                       new org.apache.bcel.generic.Type[]{
                                                          org.apache.bcel.generic.Type.STRING,
                                                          argtype
                                                       },
                                                       org.apache.bcel.Constants.INVOKESTATIC));
               break;
               
            case OBJECT:
               printerlist.append(new org.apache.bcel.generic.LDC(cpg.addString(":this "+instanceFields[i].getType().getSignature() + " "+  instanceFields[i].getName()+"=")));
               printerlist.append(new org.apache.bcel.generic.ALOAD(1));
               printerlist.append(factory.createGetField(clazz.getName(), instanceFields[i].getName(), instanceFields[i].getType()));
               printerlist.append(factory.createInvoke("sandmark.watermark.execpath.SandmarkListHolder", "concat",
                                                       org.apache.bcel.generic.Type.VOID,
                                                       new org.apache.bcel.generic.Type[]{
                                                          org.apache.bcel.generic.Type.STRING,
                                                          org.apache.bcel.generic.Type.OBJECT,
                                                       },
                                                       org.apache.bcel.Constants.INVOKESTATIC));
               break;
            }
         } // end for each instance field
         
         org.apache.bcel.generic.InstructionHandle endhandle = 
            printerlist.append(new org.apache.bcel.generic.RETURN());
         
         ifnull.setTarget(endhandle);
      }else{
         // not tracing vars
         printerlist.append(new org.apache.bcel.generic.RETURN());
      }

      sandmark.program.LocalMethod printer = 
         new sandmark.program.LocalMethod(clazz, 
                                          org.apache.bcel.Constants.ACC_PRIVATE | org.apache.bcel.Constants.ACC_STATIC,
                                          org.apache.bcel.generic.Type.VOID,
                                          new org.apache.bcel.generic.Type[]{
                                             org.apache.bcel.generic.Type.INT,
                                             org.apache.bcel.generic.Type.getType("L"+clazz.getName().replace('.', '/')+";"),
                                             org.apache.bcel.generic.Type.STRING,
                                             org.apache.bcel.generic.Type.INT,
                                             org.apache.bcel.generic.Type.STRING
                                          },
                                          null,
                                          "__sandmarkprinter",
                                          printerlist);

      printer.mark();

      return printer;
   }


   public static void main(String args[]) throws Exception{
      if (args.length<1) return;
      sandmark.program.Application app = new sandmark.program.Application(args[0]);
      new Tracer(app, true);
      app.save(args[0]+".out");
   }
}
