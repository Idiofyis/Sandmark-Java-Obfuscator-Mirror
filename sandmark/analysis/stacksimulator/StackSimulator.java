package sandmark.analysis.stacksimulator;

/**
 * StackSimulator is a utility that performs a conservative evaluation
 * of a method, and enumerates all possible contexts that an instruction
 * could be executed in.  The simulator tracks the local variable table
 * and execution stack for a method, but does not attempt to track anything
 * else. 
 * Unreachable instructions will NOT be simulated, and the context for such
 * instructions will be empty (i.e. an EmptyContext)
 *
 * @author Kelly Heffner (kheffner@cs.arizona.edu)
 */
public class StackSimulator
   implements org.apache.bcel.Constants
{
   private java.util.HashMap ih2c;
   // hashes the leader (handle) of each block to its incoming Context

   private java.util.HashMap bb2edges;
   // hashes each block to its list (ArrayList) of incoming edges

   private java.util.HashMap bb2incoming;
   // hashes each block to its list (ArrayList) of incoming blocks

   private sandmark.analysis.controlflowgraph.MethodCFG methodCFG;
   private int mMaxLocals;

   private org.apache.bcel.generic.ConstantPoolGen myCpg;

   private java.util.HashSet seenjsrs;
   // set of JSRs (handles) that have been simulated
   // (unreachable JSRs will NOT be in this set)

   public static final boolean DEBUG = false;

   /**
    * Constructs a simulator for a method represented by some
    * method control flow graph.
    * @param methodCFG the cfg for the method
    */
   public StackSimulator(sandmark.analysis.controlflowgraph.MethodCFG methodCFG)
   {
      seenjsrs=new java.util.HashSet();
      ih2c = new java.util.HashMap();
      bb2edges = new java.util.HashMap();
      bb2incoming = new java.util.HashMap();

      this.methodCFG = methodCFG;
      this.methodCFG.removeUnreachable();
      myCpg = methodCFG.method().getConstantPool();

      this.methodCFG.method().getInstructionList().setPositions();

      org.apache.bcel.generic.CodeExceptionGen [] handlers =
         methodCFG.method().getExceptionHandlers();

      java.util.Iterator blocks = methodCFG.nodes();
      while(blocks.hasNext()){
         Object block = blocks.next();
         bb2incoming.put(block, new java.util.ArrayList());
         bb2edges.put(block, new java.util.ArrayList());
      }

      mMaxLocals = methodCFG.method().calcMaxLocals();
      Context initialCX = getInitialContext();
      simulate(methodCFG.source(), initialCX);
   }
   
   public StackSimulator(sandmark.program.Method method)
   {
      this(method.getCFG());
   }

   /** This method sets the initial context on entry to the method
    *  (i.e. incoming from the source). This method should only be called once ever.
    */
   private Context getInitialContext() {
      sandmark.program.Method method = methodCFG.method();
      Context cx = new EmptyContext(mMaxLocals);

      if(!method.isStatic()) {
         if(method.getName().equals("<init>")){
            cx = cx.replaceVariable
               (new UninitializedReferenceData
                (method.getEnclosingClass().getType(),null),0);
         }else{
            cx = cx.replaceVariable
               (new ReferenceData
                (method.getEnclosingClass().getType(),null),0);
         }
      }

      // now set args
      org.apache.bcel.generic.Type args[] = 
         method.getArgumentTypes();

      for(int i = method.isStatic() ? 0 : 1,j = 0 ; j < args.length ; 
          i += args[j].getSize(),j++)

         if(args[j].equals(org.apache.bcel.generic.Type.BOOLEAN))
            cx = cx.replaceVariable(new BooleanData(null),i);
         else if(args[j].equals(org.apache.bcel.generic.Type.BYTE) ||
                 args[j].equals(org.apache.bcel.generic.Type.SHORT) ||
                 args[j].equals(org.apache.bcel.generic.Type.CHAR) ||
                 args[j].equals(org.apache.bcel.generic.Type.INT))
            cx = cx.replaceVariable(new IntData(null),i);
         else if(args[j].equals(org.apache.bcel.generic.Type.DOUBLE))
            cx = cx.replaceVariable(new DoubleData(null),i);
         else if(args[j].equals(org.apache.bcel.generic.Type.FLOAT))
            cx = cx.replaceVariable(new FloatData(null),i);
         else if(args[j].equals(org.apache.bcel.generic.Type.LONG))
            cx = cx.replaceVariable(new LongData(null),i);
         else if(args[j] instanceof org.apache.bcel.generic.ArrayType){
            cx = cx.replaceVariable
               (new ArrayReferenceData
                ((org.apache.bcel.generic.ReferenceType)args[j],null,
                 new IntData(null)),i);
         }
         else if(args[j] instanceof org.apache.bcel.generic.ReferenceType)
            cx = cx.replaceVariable
               (new ReferenceData
                ((org.apache.bcel.generic.ReferenceType)args[j],
                 null),i);
         else
            throw new RuntimeException("unknown type " + args[j]);

      return cx;
   }

   /**
      Returns the context information for a given instruction.  This
      is the context that this instruction would be executed in (ie
      the <i>incoming</i> context.
      @param ih the instruction handle query
      @return the context that this instruction would execute in
   */
   public sandmark.analysis.stacksimulator.Context getInstructionContext
      (org.apache.bcel.generic.InstructionHandle ih) {
      return getInstructionContext(ih,true);
   }

   /** Starts with the incoming BB context and executes instructions
    *  up to the given instruction. It will execute ih iff before==false.
    */
   public sandmark.analysis.stacksimulator.Context getInstructionContext
      (org.apache.bcel.generic.InstructionHandle ih, boolean before)
   {
      sandmark.analysis.stacksimulator.Context c =
         (sandmark.analysis.stacksimulator.Context)
         ih2c.get(methodCFG.getBlock(ih));

      org.apache.bcel.generic.InstructionHandle leader =
         methodCFG.getBlock(ih).getIH();

      if(c == null)
         return new EmptyContext(mMaxLocals);

      while(true){
         if (before && leader==ih)
            break;
         c = execute(leader,c);
         if(!before && leader==ih)
            break;
         leader = leader.getNext();
      }

      return c;
   }


   /**
      Collects contextual information for one basic block of the control
      flow graph. 
      @param block a basic block
      @param incomingContext the context for the first instruction in this block
   */
   private void simulate(sandmark.analysis.controlflowgraph.BasicBlock initialBlock,
                         sandmark.analysis.stacksimulator.Context initialIncomingContext) {
      // programmer's note: this method has been converted from a recursive algorithm
      //                    to a stack-based algorithm cuz the call stack got too large.

      java.util.LinkedList blockList = new java.util.LinkedList();
      java.util.LinkedList contextList = new java.util.LinkedList();

      blockList.add(initialBlock);
      contextList.add(initialIncomingContext);

      while(!blockList.isEmpty()){
         java.util.LinkedList tempBlockList = new java.util.LinkedList();
         java.util.LinkedList tempContextList = new java.util.LinkedList();

         sandmark.analysis.controlflowgraph.BasicBlock block =
            (sandmark.analysis.controlflowgraph.BasicBlock)blockList.removeFirst();
         Context incomingContext = (Context)contextList.removeFirst();


         java.util.ArrayList instList = block.getInstList();
         sandmark.analysis.stacksimulator.Context context = incomingContext;

         sandmark.analysis.stacksimulator.Context startC =
            (sandmark.analysis.stacksimulator.Context)ih2c.get(block);

         if(incomingContext != null && startC != null &&
            incomingContext.getStackSize() != startC.getStackSize()) {
            throw new RuntimeException("Bad stack size");
         }

         // if we've seen this context before, skip it
         if(incomingContext.isSubcontextOf(startC)){
            continue;
         }

         //otherwise, merge them
         context = incomingContext.merge(startC);

         java.util.ArrayList edges = (java.util.ArrayList)bb2edges.get(block);
         if(edges == null) {
            edges = new java.util.ArrayList();
            bb2edges.put(block, edges);
         }

         ih2c.put(block,context);

         /* while we simulate this block, we must save every context
            that has changes to the local variables. if there are any exception handlers
            around this block, we must simulate these contexts in them.
         */
         org.apache.bcel.generic.CodeExceptionGen[] handlers = 
            methodCFG.method().getExceptionHandlers();
         java.util.Hashtable context2handlerset = new java.util.Hashtable();
         boolean laststore=false;
         java.util.BitSet handlerbits = new java.util.BitSet();
         // bitvector for which handlers this block is inside

         java.util.Iterator instItr = instList.iterator();
         while(instItr.hasNext()) {
            org.apache.bcel.generic.InstructionHandle ih =
               (org.apache.bcel.generic.InstructionHandle)instItr.next();

            handlerbits = activeHandlers(handlers, ih);

            // if this instruction changes a local variable, simulate it in any handlers
            if (ih.getInstruction() instanceof org.apache.bcel.generic.StoreInstruction ||
                ih.getInstruction() instanceof org.apache.bcel.generic.IINC){
               for (int i=0;i<handlers.length;i++){
                  if (handlerbits.get(i)){
                     java.util.HashSet set = (java.util.HashSet)context2handlerset.get(context);
                     if (set==null){
                        set = new java.util.HashSet();
                        context2handlerset.put(context, set);
                     }
                     set.add(handlers[i]);
                  }
               }
               laststore=true;
            }else{
               laststore=false;
            }

            // do the actual simulation of the instruction
            context = execute(ih, context);
         }

         // if the last instruction was a store, then I don't need to simulate
         // the context afterward in any handlers
         if (!laststore){
            for (int i=0;i<handlers.length;i++){
               if (handlerbits.get(i)){
                  java.util.HashSet set = (java.util.HashSet)context2handlerset.get(context);
                  if (set==null){
                     set = new java.util.HashSet();
                     context2handlerset.put(context, set);
                  }
                  set.add(handlers[i]);
               }
            }
         }
         // now i have done the effect of this block on my context

         org.apache.bcel.generic.Instruction last = null;
         if (instList.size()!=0){
            last = ((org.apache.bcel.generic.InstructionHandle)
                    instList.get(instList.size()-1)).getInstruction();
         }


         // must treat RETs specially... cannot follow branch edges out of them,
         // because they will be wrong. successors of RETs are those instructions
         // after JSRs in 'seenjsrs' that have the right target.

         if (last instanceof org.apache.bcel.generic.RET){
            int index = ((org.apache.bcel.generic.RET)last).getIndex();
            StackData[] var = context.getLocalVariableAt(index);
            ReturnaddressData rad = (ReturnaddressData)var[0];

            // recursively process all known successors
            for (java.util.Iterator jiter=seenjsrs.iterator();jiter.hasNext();){
               org.apache.bcel.generic.InstructionHandle jsrhandle = 
                  (org.apache.bcel.generic.InstructionHandle)jiter.next();
               org.apache.bcel.generic.JsrInstruction jsr = 
                  (org.apache.bcel.generic.JsrInstruction)jsrhandle.getInstruction();

               if (jsr.getTarget()==rad.getTarget()){
                  sandmark.analysis.controlflowgraph.BasicBlock succ = 
                     methodCFG.getBlock(jsrhandle.getNext());

                  java.util.ArrayList incoming =
                     (java.util.ArrayList)bb2incoming.get(succ);
                  if (incoming == null) {
                     incoming = new java.util.ArrayList();
                     bb2incoming.put(succ, incoming);
                  }
               
                  Context cx = context.undefinedVersion();  // FIX!!!
                  if (!incoming.contains(block)){
                     incoming.add(block);
                  }else{
                     cx = cx.undefinedVersion();
                  }
                  tempBlockList.add(succ);
                  tempContextList.add(cx);
               }
            }
         }else{
            // now recursively process all successor blocks
            java.util.Iterator edgeIter = block.graph().outEdges(block);
            while (edgeIter.hasNext()) {
               sandmark.util.newgraph.Edge e =
                  (sandmark.util.newgraph.Edge)edgeIter.next();

               if (e instanceof sandmark.analysis.controlflowgraph.ExceptionEdge){
                  continue;
               }

               sandmark.analysis.controlflowgraph.BasicBlock succ =
                  (sandmark.analysis.controlflowgraph.BasicBlock)e.sinkNode();
               if (succ==methodCFG.sink())
                  continue;

               java.util.ArrayList incoming =
                  (java.util.ArrayList)bb2incoming.get(succ);
               if (incoming == null) {
                  incoming = new java.util.ArrayList();
                  bb2incoming.put(succ, incoming);
               }

               // stop infinite recursion for loops
               if (edges.contains(e)) {
                  if (!incoming.contains(block))
                     incoming.add(block);
                  Context cx = context.undefinedVersion();
                  tempBlockList.add(succ);
                  tempContextList.add(cx);
               }
               else {
                  if (!incoming.contains(block))
                     incoming.add(block);
                  edges.add(e);
                  tempBlockList.add(succ);
                  tempContextList.add(context.undefinedVersion());  // FIX!!
               }
            }
         }

         // for each different local variable context inside a try-catch, simulate
         // it on the handler block
         for (java.util.Enumeration e=context2handlerset.keys();e.hasMoreElements();){
            Context key = (Context)e.nextElement();
            java.util.Set handlerset = (java.util.Set)context2handlerset.get(key);
            for (java.util.Iterator hiter=handlerset.iterator();hiter.hasNext();){
               org.apache.bcel.generic.CodeExceptionGen gen = 
                  (org.apache.bcel.generic.CodeExceptionGen)hiter.next();
               sandmark.analysis.controlflowgraph.BasicBlock succ = 
                  methodCFG.getBlock(gen.getHandlerPC());

               key = key.clearStack();
               org.apache.bcel.generic.ReferenceType extype = 
                  gen.getCatchType();
               if (extype==null){
                  extype = (org.apache.bcel.generic.ReferenceType)
                     org.apache.bcel.generic.Type.getType("Ljava/lang/Throwable;");
               }
               key = key.push(new ReferenceData(extype, null));

               // prevent infinite recursion
               java.util.ArrayList incoming =
                  (java.util.ArrayList)bb2incoming.get(succ);
               if (incoming == null) {
                  incoming = new java.util.ArrayList();
                  bb2incoming.put(succ, incoming);
               }

               if (incoming.contains(block)){
                  key = key.undefinedVersion();
               }else{
                  incoming.add(block);
               }

               //               System.out.println("Adding exception handler");

               tempBlockList.add(succ);
               tempContextList.add(key.undefinedVersion()); // FIX!!!
            }
         }

         // this is done to preserve the same order that 
         // the old recursive method would have done
         blockList.addAll(0, tempBlockList);
         contextList.addAll(0, tempContextList);
      }// end of while stack nonempty
   }


   /** Returns the set of exception handlers that are active at the
    *  given instruction.
    *  @return a BitSet of active handlers (parallel to 'handlers' array)
    */
   private java.util.BitSet activeHandlers
      (org.apache.bcel.generic.CodeExceptionGen[] handlers,
       org.apache.bcel.generic.InstructionHandle ih){
      
      java.util.BitSet result = new java.util.BitSet(handlers.length);
      
      for (int i=0;i<handlers.length;i++){
         if (handlers[i].getStartPC().getPosition()<=ih.getPosition() &&
             handlers[i].getEndPC().getPosition()>=ih.getPosition()){
            result.set(i);
         }
      }
      return result;
   }


   /**
      Simulates the execution of an instruction in the current
      context.  Does not modify the incoming context,
      returns the outgoing context
      @param instH the instruction handle for the instruction to simulate
      @param context the incoming context for this instruction
   */
   private Context execute(org.apache.bcel.generic.InstructionHandle instH,
                           sandmark.analysis.stacksimulator.Context context)
   {
      org.apache.bcel.generic.Instruction inst = instH.getInstruction();
      int opcode = inst.getOpcode();


      if(inst instanceof org.apache.bcel.generic.ACONST_NULL){
         context = context.push(new sandmark.analysis.stacksimulator.ReferenceData
                                (org.apache.bcel.generic.Type.NULL, instH));
      }
      else if(inst instanceof org.apache.bcel.generic.ArithmeticInstruction){
         context = doArithmetic(instH, context);
      }
      else if(inst instanceof org.apache.bcel.generic.ArrayInstruction){
         context = doArray(instH, context);
      }
      else if(inst instanceof org.apache.bcel.generic.ARRAYLENGTH){
         sandmark.analysis.stacksimulator.StackData[] arrays =
            context.getStackAt(0);
         context = context.pop();
         java.util.ArrayList toPush = new java.util.ArrayList();
         boolean det = true;
         for(int i = 0; i < arrays.length; i++){
            if(arrays[i] instanceof
               sandmark.analysis.stacksimulator.ArrayReferenceData){
               sandmark.analysis.stacksimulator.ArrayReferenceData arr =
                  (sandmark.analysis.stacksimulator.ArrayReferenceData)arrays[i];
               toPush.add(new sandmark.analysis.stacksimulator.IntData
                          (arr.getLength(), instH));
            }
            else det = false;
         }
         if(!det)
            toPush.add(new sandmark.analysis.stacksimulator.IntData(instH));
         context = context.push((sandmark.analysis.stacksimulator.StackData[])
                                toPush.toArray(new sandmark.analysis.stacksimulator.StackData[]{}));
      }
      else if(inst instanceof org.apache.bcel.generic.ATHROW){
         context = context.pop();
      }
      else if(inst instanceof org.apache.bcel.generic.BIPUSH){
         org.apache.bcel.generic.BIPUSH bipush =
            (org.apache.bcel.generic.BIPUSH) inst;

         context = context.push(new sandmark.analysis.stacksimulator.IntData
                                (bipush.getValue().intValue(), instH));
      }
      else if(inst instanceof org.apache.bcel.generic.BranchInstruction){
         switch(opcode){
         case TABLESWITCH:
         case LOOKUPSWITCH:
            context = context.pop();
            break;
         case GOTO:
         case GOTO_W:
            //do nothing
            break;
         case IF_ACMPEQ:
         case IF_ACMPNE:
         case IF_ICMPEQ:
         case IF_ICMPGE:
         case IF_ICMPGT:
         case IF_ICMPLE:
         case IF_ICMPLT:
         case IF_ICMPNE:
            context = context.pop();
            context = context.pop();
            break;
         case IFEQ:
         case IFGE:
         case IFGT:
         case IFLE:
         case IFLT:
         case IFNE:
         case IFNONNULL:
         case IFNULL:
            context = context.pop();
            break;
         case JSR:
         case JSR_W:{
            org.apache.bcel.generic.InstructionHandle jsrtarget = 
               ((org.apache.bcel.generic.JsrInstruction)inst).getTarget();
            seenjsrs.add(instH);
            //push label
            context = context.push
               (new sandmark.analysis.stacksimulator.ReturnaddressData(instH, jsrtarget));
            break;
         }
         }
      }
      else if(inst instanceof org.apache.bcel.generic.ConversionInstruction){
         context = doConversion(instH, context);
      }
      else if(inst instanceof org.apache.bcel.generic.CPInstruction){
         switch(opcode){
         case INSTANCEOF:
            context = context.pop(); //remove object ref
            context = context.push(new sandmark.analysis.stacksimulator.BooleanData(instH));
            break;
         case CHECKCAST:{
            org.apache.bcel.generic.CHECKCAST cast = 
               (org.apache.bcel.generic.CHECKCAST)inst;
            org.apache.bcel.generic.Type casttype = 
               cast.getType(myCpg);
            context = context.replaceStack
               (new StackData[]
                  {new ReferenceData((org.apache.bcel.generic.ReferenceType)casttype, instH)}, 0);
            break;
         }
         case ANEWARRAY:
         case MULTIANEWARRAY:
            // ANEWARRAY will report getType as the element type, 
            // MULTIANEWARRAY will give the full array type

            int dimension = opcode==ANEWARRAY?1:
               ((org.apache.bcel.generic.MULTIANEWARRAY)inst).getDimensions();
            sandmark.analysis.stacksimulator.StackData[] lengthSet =
               context.getStackAt(0);
            for(int dimCounter = 0 ; dimCounter < dimension ; dimCounter++)
               context = context.pop();

            sandmark.analysis.stacksimulator.StackData[] arrays =
               new sandmark.analysis.stacksimulator.StackData[lengthSet.length];

            org.apache.bcel.generic.LoadClass arrayInst =
               (org.apache.bcel.generic.LoadClass)inst;

            org.apache.bcel.generic.Type arraytype = 
               arrayInst.getType(myCpg);
            if (opcode==ANEWARRAY)
               arraytype = new org.apache.bcel.generic.ArrayType(arraytype, 1);

            for(int l = 0; l < arrays.length; l++){
               arrays[l] = new sandmark.analysis.stacksimulator.ArrayReferenceData
                  ((org.apache.bcel.generic.ReferenceType)arraytype,
                   instH, (sandmark.analysis.stacksimulator.PrimitiveData)lengthSet[l]);
            }
            context = context.push(arrays);
            break;
         case NEW:
            org.apache.bcel.generic.NEW mew = (org.apache.bcel.generic.NEW)inst;
            // >^..^< -mew!
            context = context.push(new UninitializedReferenceData
                                   (mew.getLoadClassType(myCpg), instH));
            break;
         case LDC:
         case LDC2_W:
         case LDC_W:
            org.apache.bcel.generic.CPInstruction ldc =
               (org.apache.bcel.generic.CPInstruction)inst;
            org.apache.bcel.generic.Type loadType = ldc.getType(myCpg);
            if(loadType.equals(org.apache.bcel.generic.Type.INT))
               context = context.push(new sandmark.analysis.stacksimulator.IntData(instH));
            else if(loadType.equals(org.apache.bcel.generic.Type.FLOAT))
               context = context.push(new sandmark.analysis.stacksimulator.FloatData(instH));
            else if(loadType.equals(org.apache.bcel.generic.Type.DOUBLE))
               context = context.push(new sandmark.analysis.stacksimulator.DoubleData(instH));
            else if(loadType.equals(org.apache.bcel.generic.Type.LONG))
               context = context.push(new sandmark.analysis.stacksimulator.LongData(instH));
            else
               context = context.push(new sandmark.analysis.stacksimulator.ReferenceData
                                      ((org.apache.bcel.generic.ReferenceType)loadType, instH));
            break;
         case PUTFIELD:
            context = context.pop(); //object ref
            //no break here on purpose!
         case PUTSTATIC:
            context = context.pop(); //value for put
            break;
         case GETFIELD:
            context = context.pop(); //object ref
            //no break here on purpose!
         case GETSTATIC:
            org.apache.bcel.generic.FieldInstruction fi =
               (org.apache.bcel.generic.FieldInstruction)inst;
            org.apache.bcel.generic.Type fieldType = fi.getFieldType(myCpg);

            sandmark.analysis.stacksimulator.StackData toPush =
               getDataForType(fieldType, instH);
            context = context.push(toPush);
            break;
         case INVOKEINTERFACE:
         case INVOKESPECIAL:
         case INVOKEVIRTUAL:
         case INVOKESTATIC:
            org.apache.bcel.generic.InvokeInstruction invoke =
               (org.apache.bcel.generic.InvokeInstruction)inst;
            int args = invoke.getArgumentTypes(myCpg).length;
            //pop the arguments
            for(int i = 0; i < args; i++)
               context = context.pop();

            if (opcode == INVOKEVIRTUAL
                || opcode == INVOKEINTERFACE
                || opcode == INVOKESPECIAL) {
               if (opcode == INVOKESPECIAL &&
                   invoke.getMethodName(myCpg).equals("<init>")) {
                  context = context.initializeTop(instH);
               }

               context = context.pop();
            }

            //then push on the result
            org.apache.bcel.generic.Type retType = invoke.getReturnType(myCpg);

            if(!retType.equals(org.apache.bcel.generic.Type.VOID))
               context = context.push(getDataForType(retType, instH));
            break;
         default:
            throw new IllegalArgumentException
               ("Did not implement instruction code: " + opcode);
         }

      }
      else if(inst instanceof org.apache.bcel.generic.DCMPG){
         context = doComparison(context, new Double(Double.NaN), true, instH);
      }
      else if(inst instanceof org.apache.bcel.generic.DCMPL){
         context = doComparison(context, new Double(Double.NaN), false, instH);
      }
      else if(inst instanceof org.apache.bcel.generic.DCONST){
         org.apache.bcel.generic.DCONST dconst =
            (org.apache.bcel.generic.DCONST)inst;
         context = context.push(new sandmark.analysis.stacksimulator.DoubleData
                                (dconst.getValue().doubleValue(), instH));
      }
      else if(inst instanceof org.apache.bcel.generic.FCMPG){
         context = doComparison(context, new Float(Float.NaN), true, instH);
      }
      else if(inst instanceof org.apache.bcel.generic.FCMPL){
         context = doComparison(context, new Float(Float.NaN), false, instH);
      }
      else if(inst instanceof org.apache.bcel.generic.FCONST){
         org.apache.bcel.generic.FCONST fconst =
            (org.apache.bcel.generic.FCONST)inst;
         context = context.push(new sandmark.analysis.stacksimulator.FloatData
                                (fconst.getValue().floatValue(), instH));
      }
      else if(inst instanceof org.apache.bcel.generic.ICONST){
         org.apache.bcel.generic.ICONST iconst =
            (org.apache.bcel.generic.ICONST)inst;
         context = context.push(new sandmark.analysis.stacksimulator.IntData
                                (iconst.getValue().intValue(), instH));
      }
      else if(inst instanceof org.apache.bcel.generic.LCMP){
         sandmark.analysis.stacksimulator.StackData [] values2 =
            context.getStackAt(0);
         context = context.pop();
         sandmark.analysis.stacksimulator.StackData [] values1 =
            context.getStackAt(0);
         context = context.pop();
         sandmark.analysis.stacksimulator.StackData [] toPush =
            new sandmark.analysis.stacksimulator.StackData[values2.length * values1.length];
         for(int i1 = 0; i1 < values1.length; i1++){
            sandmark.analysis.stacksimulator.PrimitiveData v1 =
               (sandmark.analysis.stacksimulator.PrimitiveData)values1[i1];
            for(int i2 = 0; i2 < values2.length; i2++){
               int index = i1*values2.length + i2;
               sandmark.analysis.stacksimulator.PrimitiveData v2 =
                  (sandmark.analysis.stacksimulator.PrimitiveData)values2[i2];
               if(v1.hasDefinedValue() && v2.hasDefinedValue())
                  toPush[index] = (new sandmark.analysis.stacksimulator.CompositeIntData
                                   (v2, v1,
                                    ((Comparable)v2.getValue()).compareTo(v1.getValue()), instH));
               else
                  toPush[index] = (new sandmark.analysis.stacksimulator.CompositeIntData
                                   (v2, v1, instH));
            }
         }
         context = context.push(toPush);
      }
      else if(inst instanceof org.apache.bcel.generic.LCONST){
         org.apache.bcel.generic.LCONST lconst =
            (org.apache.bcel.generic.LCONST)inst;
         context = context.push(new sandmark.analysis.stacksimulator.LongData
                                (lconst.getValue().longValue(), instH));
      }
      else if(inst instanceof org.apache.bcel.generic.LocalVariableInstruction){
         org.apache.bcel.generic.LocalVariableInstruction lvInst =
            (org.apache.bcel.generic.LocalVariableInstruction)inst;
         int varIndex = lvInst.getIndex();

         if(inst instanceof org.apache.bcel.generic.LoadInstruction){
            try{
               sandmark.analysis.stacksimulator.StackData [] toPush;
               Class theClass = null;
               switch(opcode){
               case ILOAD:
               case ILOAD_0:
               case ILOAD_1:
               case ILOAD_2:
               case ILOAD_3:
                  theClass = Class.forName("sandmark.analysis.stacksimulator.IntData");
                  break;
               case DLOAD:
               case DLOAD_0:
               case DLOAD_1:
               case DLOAD_2:
               case DLOAD_3:
                  theClass = Class.forName("sandmark.analysis.stacksimulator.DoubleData");
                  break;
               case FLOAD:
               case FLOAD_0:
               case FLOAD_1:
               case FLOAD_2:
               case FLOAD_3:
                  theClass = Class.forName("sandmark.analysis.stacksimulator.FloatData");
                  break;
               case LLOAD:
               case LLOAD_0:
               case LLOAD_1:
               case LLOAD_2:
               case LLOAD_3:
                  theClass = Class.forName("sandmark.analysis.stacksimulator.LongData");
                  break;
               case ALOAD:
               case ALOAD_0:
               case ALOAD_1:
               case ALOAD_2:
               case ALOAD_3:
                  sandmark.analysis.stacksimulator.StackData [] temp =
                     context.getLocalVariableAt(varIndex);
                  if(temp == null){
                     
                     //                     System.out.println("!!!!!!! Cannot determine local variable type!!!!!!!!!");
                     
                     toPush = new sandmark.analysis.stacksimulator.StackData[]{
                        new sandmark.analysis.stacksimulator.ReferenceData
                        (org.apache.bcel.generic.Type.OBJECT, instH)
                     };
                  }
                  else{
                     java.util.ArrayList data = new java.util.ArrayList();
                     for(int i = 0; i < temp.length; i++){
                        if(temp[i] instanceof
                           sandmark.analysis.stacksimulator.ArrayReferenceData){
                           sandmark.analysis.stacksimulator.ArrayReferenceData ard =
                              (sandmark.analysis.stacksimulator.ArrayReferenceData)temp[i];
                           data.add(new sandmark.analysis.stacksimulator.ArrayReferenceData
                                    ((org.apache.bcel.generic.ReferenceType)
                                     ard.getType(), ard.getLength(), instH));
                        } else if(temp[i] instanceof
                                  sandmark.analysis.stacksimulator.ReferenceData) {
                           data.add(new sandmark.analysis.stacksimulator.ReferenceData
                                    ((org.apache.bcel.generic.ReferenceType)
                                     temp[i].getType(), instH));
                        }
                     }
                     toPush = (StackData [])data.toArray(new StackData[0]);
                  }
                  context = context.push(toPush);
                  return context;
               default:
                  throw new Error("Unknown instruction " + instH);
               }
               toPush = doLoad(context.getLocalVariableAt(varIndex),
                               theClass.getConstructor(new Class[]{
                                  Class.forName("java.lang.Number"),
                                  Class.forName("org.apache.bcel.generic.InstructionHandle")}),
                               theClass.getConstructor(new Class[]{
                                  Class.forName("org.apache.bcel.generic.InstructionHandle")}),
                               instH);

               context = context.push(toPush);
            }catch(ClassNotFoundException cnfe){
               throw new RuntimeException(cnfe.toString());
            }catch(NoSuchMethodException nsme){
               throw new RuntimeException(nsme.toString());
            }
         }
         else if(inst instanceof org.apache.bcel.generic.StoreInstruction){
            sandmark.analysis.stacksimulator.StackData [] top =
               context.getStackAt(0);
            int i = 0;
            try{
               context = context.pop();
               Class theClass = null;
               sandmark.analysis.stacksimulator.StackData [] toStore =
                  new sandmark.analysis.stacksimulator.StackData [top.length];
               switch(opcode){
               case ISTORE:
               case ISTORE_0:
               case ISTORE_1:
               case ISTORE_2:
               case ISTORE_3:
                  theClass = Class.forName("sandmark.analysis.stacksimulator.IntData");
                  break;
               case DSTORE:
               case DSTORE_0:
               case DSTORE_1:
               case DSTORE_2:
               case DSTORE_3:
                  theClass = Class.forName("sandmark.analysis.stacksimulator.DoubleData");
                  break;
               case FSTORE:
               case FSTORE_0:
               case FSTORE_1:
               case FSTORE_2:
               case FSTORE_3:
                  theClass = Class.forName("sandmark.analysis.stacksimulator.FloatData");
                  break;
               case LSTORE:
               case LSTORE_0:
               case LSTORE_1:
               case LSTORE_2:
               case LSTORE_3:
                  theClass = Class.forName("sandmark.analysis.stacksimulator.LongData");
                  break;
               case ASTORE:
               case ASTORE_0:
               case ASTORE_1:
               case ASTORE_2:
               case ASTORE_3:
               default:
                  for(i = 0; i < toStore.length; i++){
                     if(top[i] instanceof sandmark.analysis.stacksimulator.ArrayReferenceData){
                        toStore[i] = new sandmark.analysis.stacksimulator.ArrayReferenceData
                           ((org.apache.bcel.generic.ReferenceType)top[i].getType(), instH,
                            ((sandmark.analysis.stacksimulator.ArrayReferenceData)top[i]).getLengthData());
                     }
                     else if (top[i] instanceof sandmark.analysis.stacksimulator.ReferenceData) {
                        toStore[i] = new sandmark.analysis.stacksimulator.ReferenceData
                           ((org.apache.bcel.generic.ReferenceType)top[i].getType(), instH);
                     }
                     else /* ReturnaddressData */ {
                        ReturnaddressData rad = (ReturnaddressData)top[i];
                        toStore[i] = new sandmark.analysis.stacksimulator.ReturnaddressData(instH, rad.getTarget());
                     }
                  }
                  context = context.replaceVariable(toStore, varIndex);
                  return context;
               }

               for(i = 0; i < toStore.length; i++){
                  sandmark.analysis.stacksimulator.PrimitiveData num =
                     (sandmark.analysis.stacksimulator.PrimitiveData)top[i];
                  java.lang.reflect.Constructor fact;
                  Object [] args;
                  if(num.hasDefinedValue()){
                     fact = theClass.getConstructor(new Class[]{
                        Class.forName("java.lang.Number"),
                        Class.forName("org.apache.bcel.generic.InstructionHandle")});
                     args = new Object[]{num.getValue(), instH};

                  }
                  else{
                     fact = theClass.getConstructor(new Class[]{
                        Class.forName("org.apache.bcel.generic.InstructionHandle")});
                     args = new Object[]{instH};
                  }

                  toStore[i] = (sandmark.analysis.stacksimulator.StackData)fact.newInstance(args);
               }
               context = context.replaceVariable(toStore, varIndex);
            }
            catch(Exception e){
               e.printStackTrace();
               throw new RuntimeException(e);
            }
         }
         else if(inst instanceof org.apache.bcel.generic.IINC){
            sandmark.analysis.stacksimulator.StackData [] toInc =
               context.getLocalVariableAt(varIndex);
            sandmark.analysis.stacksimulator.StackData [] toPush = null;
            if(toInc == null)
               context = context.replaceVariable(new sandmark.analysis.stacksimulator.IntData
                                                 (instH), varIndex);
            else{
               toPush = new sandmark.analysis.stacksimulator.StackData[toInc.length];
               for(int i = 0; i < toInc.length; i++){
                  sandmark.analysis.stacksimulator.PrimitiveData num =
                     (sandmark.analysis.stacksimulator.PrimitiveData)toInc[i];
                  if(num.hasDefinedValue()){
                     toPush[i] = new sandmark.analysis.stacksimulator.CompositeIntData
                        (num, null, num.getValue().intValue() + 1, instH);
                  }
                  else
                     toPush[i] = new sandmark.analysis.stacksimulator.CompositeIntData
                        (num, null, instH);
               }

               context = context.replaceVariable(toPush, varIndex);
            }

         }
         else throw new IllegalArgumentException("Invalid local variable " +
                                                 "instruction: " + inst);
      }
      else if(inst instanceof org.apache.bcel.generic.MONITORENTER){
         context = context.pop();
      }
      else if(inst instanceof org.apache.bcel.generic.MONITOREXIT){
         context = context.pop();
      }
      else if(inst instanceof org.apache.bcel.generic.NEWARRAY){
         sandmark.analysis.stacksimulator.StackData[] lengthSet =
            context.getStackAt(0);
         context = context.pop();
         sandmark.analysis.stacksimulator.StackData[] arrays =
            new sandmark.analysis.stacksimulator.StackData[lengthSet.length];

         org.apache.bcel.generic.NEWARRAY arrayInst =
            (org.apache.bcel.generic.NEWARRAY)inst;

         for(int l = 0; l < arrays.length; l++)
            arrays[l] = new sandmark.analysis.stacksimulator.ArrayReferenceData
               ((org.apache.bcel.generic.ReferenceType)arrayInst.getType(),
                instH, (sandmark.analysis.stacksimulator.PrimitiveData)lengthSet[l]);
         context = context.push(arrays);
      }
      else if(inst instanceof org.apache.bcel.generic.NOP){
         /*DO NOTHING*/
      }
      else if(inst instanceof org.apache.bcel.generic.RET){
         /*DO NOTHING*/
      }
      else if(inst instanceof org.apache.bcel.generic.ReturnInstruction){
         if(!(inst instanceof org.apache.bcel.generic.RETURN))
            context = context.pop();
      }
      else if(inst instanceof org.apache.bcel.generic.SIPUSH){
         org.apache.bcel.generic.SIPUSH sipush =
            (org.apache.bcel.generic.SIPUSH)inst;
         context = context.push(new sandmark.analysis.stacksimulator.IntData
                                (sipush.getValue().intValue(), instH));
      }
      else if(inst instanceof org.apache.bcel.generic.StackInstruction){
         context = doStack(instH, context);
      }
      else{
         throw new IllegalArgumentException(inst.getClass().getName() +
                                            " is not a valid instruction.");
      }

      return context;
   }

   // helper method to simulate StackInstructions
   private Context doStack(org.apache.bcel.generic.InstructionHandle instH,
                           sandmark.analysis.stacksimulator.Context context)
   {
      org.apache.bcel.generic.Instruction inst = instH.getInstruction();
      int opcode = inst.getOpcode();

      sandmark.analysis.stacksimulator.StackData[] data;
      sandmark.analysis.stacksimulator.StackData[] data2 = null;

      switch(opcode){
      case DUP:
         data = context.getStackAt(0);
         if(data[0].getSize() > 1)
            throw new IllegalArgumentException("Cannot DUP a 2 byte" +
                                               " data member.");
         context = context.push(data);
         break;
      case DUP_X1:
         data = context.getStackAt(0);
         if(data[0].getSize() > 1)
            throw new IllegalArgumentException("Cannot DUP_X1 a 2 byte" +
                                               " data member.");
         context = context.pushAt(2, data);
         break;
      case DUP_X2:
         data = context.getStackAt(0);
         if(data[0].getSize() > 1)
            throw new IllegalArgumentException("Cannot DUP_X2 a 2 byte" +
                                               " data member.");
         //Adding stuff here to make it work ...zach
         StackData[] tmp_data = context.getStackAt(1);
         if(tmp_data[0].getSize() > 1) //form2
            context = context.pushAt(2, data);
         else //form 1
            context = context.pushAt(3, data);
         //done fixing it..i hope
         break;
      case DUP2:
         data = context.getStackAt(0);
         if(data[0].getSize() > 1){
            context = context.push(data);
         }
         else{
            context = context.push(context.getStackAt(1));
            context = context.push(data);
         }
         break;
      case DUP2_X1:
         boolean singles = false;
         data = context.getStackAt(0);
         if(data[0].getSize() == 1){
            data2 = context.getStackAt(1);
            if(data2[0].getSize() > 1)
               throw new IllegalArgumentException("Cannot DUP2_X1 with 1 " +
                                                  "and 2 byte data members.");
            //otherwise, push both 1 byters
            singles = true;
         }
         sandmark.analysis.stacksimulator.StackData[] skip =
            context.getStackAt(singles?2:1);
         if(skip[0].getSize() > 1)
            throw new IllegalArgumentException("Cannot DUP2_X1 over 2 byte" +
                                               " data member.");

         if(singles){
            context = context.pushAt(3, data);
            context = context.pushAt(4, data2);
         }
         else
            context = context.pushAt(2, data);

         break;
      case DUP2_X2:
         data = context.getStackAt(0);
         if(data[0].getSize() > 1) {
            data2 = context.getStackAt(1);
            if(data2[0].getSize() > 1)
               context = context.pushAt(2, data);
            else{
               if(context.getStackAt(2)[0].getSize() > 1)
                  throw new IllegalArgumentException("Cannot DUP2_X2 one 2 byte," +
                                                     " one one byte, and another 2 byte data members.");
               context = context.pushAt(3, data);
            }
         }
         else{ //two one byte members
            data2 = context.getStackAt(1);
            if(data2[0].getSize() > 1)
               throw new IllegalArgumentException("Cannot DUP2_X2 one 1 byte," +
                                                  " and one 2 byte data member.");

            sandmark.analysis.stacksimulator.StackData[] filler1 =
               context.getStackAt(2);
            if(filler1[0].getSize() > 1){
               context = context.pushAt(3, data);
               context = context.pushAt(4, data2);
            }else{
               if(context.getStackAt(3)[0].getSize() > 1)
                  throw new IllegalArgumentException("Cannot DUP2_X2 three 1 byte," +
                                                     " and one 2 byte data member.");
               context = context.pushAt(4, data);
               context = context.pushAt(5, data2);
            }
         }
         break;
      case POP:
         if(context.getStackSize() == 0) {
            throw new IllegalArgumentException
               ("cannot POP from empty stack at " + instH);
         }
         data = context.getStackAt(0);
         if(data[0].getSize() > 1)
            throw new IllegalArgumentException("Cannot POP a two byte" +
                                               "data member");
         context = context.pop();
         break;
      case POP2:
         data = context.getStackAt(0);
         context = context.pop();
         if(data[0].getSize() == 1){
            data = context.getStackAt(0);
            if(data[0].getSize() != 1)
               throw new IllegalArgumentException("Cannot POP2 one 1 byte" +
                                                  " data member, and one 2 byte data member.");
            context = context.pop();
         }
         break;
      case SWAP:
         sandmark.analysis.stacksimulator.StackData[] data1 =
            context.getStackAt(0);
         data2 = context.getStackAt(1);
         if(data1[0].getSize() != 1 || data2[0].getSize() != 1)
            throw new IllegalArgumentException("The SWAP operation can" +
                                               " only be performed on two 1 byte data members.");
         context = context.pop();
         context = context.pushAt(1, data1);
         break;
      default:
         throw new IllegalArgumentException(inst.getClass().getName() +
                                            " is not a valid instruction.");
      }

      return context;
   }

   // helper method to simulate ArithmeticInstructions
   private Context doArithmetic(org.apache.bcel.generic.InstructionHandle instH,
                                sandmark.analysis.stacksimulator.Context context)
   {
      org.apache.bcel.generic.Instruction inst = instH.getInstruction();
      int opcode = inst.getOpcode();

      //change this if statement, its ugly.
      if(opcode == DNEG || opcode == FNEG || opcode == INEG || opcode == LNEG){
         sandmark.analysis.stacksimulator.StackData [] op1 = context.getStackAt(0);
         sandmark.analysis.stacksimulator.StackData [] result =
            new sandmark.analysis.stacksimulator.StackData[op1.length];
         context = context.pop();
         for(int o1 = 0; o1 < op1.length; o1++){
            sandmark.analysis.stacksimulator.PrimitiveData arg1 =
               (sandmark.analysis.stacksimulator.PrimitiveData)op1[o1];

            switch(opcode){
            case DNEG: //double negation
               if(arg1.hasDefinedValue())
                  result[o1] = new sandmark.analysis.stacksimulator.CompositeDoubleData
                     (arg1, null, arg1.getValue().doubleValue() * -1, instH);
               else
                  result[o1] = new sandmark.analysis.stacksimulator.CompositeDoubleData
                     (arg1, null, instH);
               break;
            case FNEG: //float negation
               if(arg1.hasDefinedValue())
                  result[o1] = new sandmark.analysis.stacksimulator.CompositeFloatData
                     (arg1, null, arg1.getValue().floatValue() * -1, instH);
               else
                  result[o1] = new sandmark.analysis.stacksimulator.CompositeFloatData
                     (arg1, null, instH);
               break;
            case INEG: //int negation
               if(arg1.hasDefinedValue())
                  result[o1] = new sandmark.analysis.stacksimulator.CompositeIntData
                     (arg1, null, arg1.getValue().intValue() * -1, instH);
               else
                  result[o1] = new sandmark.analysis.stacksimulator.CompositeIntData
                     (arg1, null, instH);
               break;
            case LNEG: //long negation
               if(arg1.hasDefinedValue())
                  result[o1] = new sandmark.analysis.stacksimulator.CompositeLongData
                     (arg1, null, arg1.getValue().longValue() * -1, instH);
               else
                  result[o1] = new sandmark.analysis.stacksimulator.CompositeLongData
                     (arg1, null, instH);
               break;
            default:
               throw new RuntimeException("Kelly forgot to simulate opcode: " + opcode);
            }
         }
         context = context.push(result);

      }
      else{
         sandmark.analysis.stacksimulator.StackData [] op2 = context.getStackAt(0);
         context = context.pop();
         sandmark.analysis.stacksimulator.StackData [] op1 = context.getStackAt(0);
         context = context.pop();

         sandmark.analysis.stacksimulator.StackData [] result =
            new sandmark.analysis.stacksimulator.StackData[op1.length * op2.length];

         for(int o1 = 0; o1 < op1.length; o1++){
            sandmark.analysis.stacksimulator.PrimitiveData arg1 =
               (sandmark.analysis.stacksimulator.PrimitiveData)op1[o1];
            for(int o2 = 0; o2 < op2.length; o2++){
               sandmark.analysis.stacksimulator.PrimitiveData arg2 =
                  (sandmark.analysis.stacksimulator.PrimitiveData)op2[o2];
               int resIndex = o1*op2.length + o2;
               switch(opcode){

               case DADD: //add doubles
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     double value=0;
                     try{
                        value = arg1.getValue().doubleValue() +
                           arg2.getValue().doubleValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeDoubleData
                        (arg1, arg2, instH);
                  break;

               case DDIV: //divide doubles
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     double value=0;
                     try{
                        value = arg1.getValue().doubleValue() /
                           arg2.getValue().doubleValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeDoubleData
                        (arg1, arg2, instH);
                  break;

               case DMUL: //multiply doubles
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     double value=0;
                     try{
                        value = arg1.getValue().doubleValue() *
                           arg2.getValue().doubleValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeDoubleData
                        (arg1, arg2, instH);
                  break;

               case DREM: //mod doubles
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     double value=0;
                     try{
                        value = arg1.getValue().doubleValue() %
                           arg2.getValue().doubleValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeDoubleData
                        (arg1, arg2, instH);
                  break;

               case DSUB: //subtract doubles
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     double value=0;
                     try{
                        value = arg1.getValue().doubleValue() -
                           arg2.getValue().doubleValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeDoubleData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeDoubleData
                        (arg1, arg2, instH);
                  break;

               case FADD: //add floats
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     float value=0;
                     try{
                        value = arg1.getValue().floatValue() +
                           arg2.getValue().floatValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeFloatData
                        (arg1, arg2, instH);
                  break;

               case FDIV: //divide floats
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     float value=0;
                     try{
                        value = arg1.getValue().floatValue() /
                           arg2.getValue().floatValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeFloatData
                        (arg1, arg2, instH);
                  break;

               case FMUL: //multiply floats
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     float value=0;
                     try{
                        value = arg1.getValue().floatValue() *
                           arg2.getValue().floatValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeFloatData
                        (arg1, arg2, instH);
                  break;

               case FREM: //mod floats
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     float value=0;
                     try{
                        value = arg1.getValue().floatValue() %
                           arg2.getValue().floatValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeFloatData
                        (arg1, arg2, instH);
                  break;

               case FSUB: //subtract floats
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue()){
                     boolean got=false;
                     float value=0;
                     try{
                        value = arg1.getValue().floatValue() -
                           arg2.getValue().floatValue();
                        got=true;
                     }catch(java.lang.ArithmeticException t){}
                     
                     if (got){
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, value, instH);
                     }else{
                        result[resIndex] =
                           new sandmark.analysis.stacksimulator.CompositeFloatData
                           (arg1, arg2, instH);
                     }
                  }else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeFloatData
                        (arg1, arg2, instH);
                  break;

               case IADD: //integer add
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, arg1.getValue().intValue() +
                         arg2.getValue().intValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;

               case IAND: //integer bitwise and
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, arg1.getValue().intValue() &
                         arg2.getValue().intValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;

               case IDIV: //integer div
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue() &&
                     arg2.getValue().intValue() != 0){ //look out for div by 0
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, arg1.getValue().intValue() /
                         arg2.getValue().intValue(), instH);
                  }
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;
               case IMUL: //integer multiply
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, arg1.getValue().intValue() *
                         arg2.getValue().intValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;
               case IOR: //integer bitwise or
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, arg1.getValue().intValue() |
                         arg2.getValue().intValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;
               case IREM: //integer mod
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue() && arg2.getValue().intValue()!=0){
                     int value=0;
                     if (arg2.getValue().intValue()!=0)
                        value = arg1.getValue().intValue() % 
                           arg2.getValue().intValue();

                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, value, instH);
                  }
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;

               case ISHL: //int shift left logical - shifts value 2 by value 1
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2,
                         arg2.getValue().intValue() <<
                         arg1.getValue().intValue(),
                         instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;

               case ISHR: //int shift right
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2,
                         arg2.getValue().intValue() >>
                         arg1.getValue().intValue(),
                         instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;

               case ISUB: //int subtraction (phew)
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, arg1.getValue().intValue() -
                         arg2.getValue().intValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;
               case IUSHR: //int unsigned shift right
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2,
                         arg2.getValue().intValue() >>>
                         arg1.getValue().intValue(),
                         instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;
               case IXOR: //int exclusive bitwise or
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, arg2.getValue().intValue() ^
                         arg1.getValue().intValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeIntData
                        (arg1, arg2, instH);
                  break;
               case LADD: //long add
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, arg1.getValue().longValue() +
                         arg2.getValue().longValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LAND: //long bitwise and
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, arg1.getValue().longValue() &
                         arg2.getValue().longValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LDIV: //long div
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue() && arg2.getValue().longValue()!=0L)
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, arg1.getValue().longValue() /
                         arg2.getValue().longValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LMUL: //long multiply
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, arg1.getValue().longValue() *
                         arg2.getValue().longValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LOR: //long bitwise or
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, arg1.getValue().longValue() |
                         arg2.getValue().longValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LREM: //long mod
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue() && arg2.getValue().longValue()!=0L)
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, arg1.getValue().longValue() %
                         arg2.getValue().longValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LSHL: //long shift left logical - shifts value 2 by value 1
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2,
                         arg1.getValue().longValue() <<
                         arg2.getValue().longValue(),
                         instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LSHR: //long shift right
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2,
                         arg1.getValue().longValue() >>
                         arg2.getValue().longValue(),
                         instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LSUB: //long subtraction (phew)
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, arg1.getValue().longValue() -
                         arg2.getValue().longValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LUSHR: //long unsigned shift right
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2,
                         arg1.getValue().longValue() >>>
                         arg2.getValue().longValue(),
                         instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               case LXOR: //long exclusive bitwise or
                  if(arg1.hasDefinedValue() && arg2.hasDefinedValue())
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, arg1.getValue().longValue() ^
                         arg2.getValue().longValue(), instH);
                  else
                     result[resIndex] =
                        new sandmark.analysis.stacksimulator.CompositeLongData
                        (arg1, arg2, instH);
                  break;
               default:
                  throw new RuntimeException("Kelly forgot to simulate opcode: " + opcode);
               }
            }
         }
         context = context.push(result);
      }

      return context;
   }

   // helper method to simulate ConversionInstructions
   private Context doConversion(org.apache.bcel.generic.InstructionHandle ih,
                                sandmark.analysis.stacksimulator.Context context)
   {
      int opcode = ih.getInstruction().getOpcode();
      sandmark.analysis.stacksimulator.StackData [] values = context.getStackAt(0);
      context = context.pop();
      sandmark.analysis.stacksimulator.StackData [] toPush =
         new sandmark.analysis.stacksimulator.StackData [values.length];

      for(int v = 0; v < values.length; v++){
         sandmark.analysis.stacksimulator.PrimitiveData arg =
            (sandmark.analysis.stacksimulator.PrimitiveData)values[v];
         switch(opcode){
         case D2F:
         case L2F:
         case I2F:
            if(arg.hasDefinedValue())
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeFloatData
                  (arg, null, arg.getValue().floatValue(), ih);
            else
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeFloatData
                  (arg, null, ih);
            break;
         case D2I:
         case F2I:
         case L2I:
            if(arg.hasDefinedValue())
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeIntData
                  (arg, null, arg.getValue().intValue(), ih);
            else
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeIntData
                  (arg, null, ih);
            break;
         case D2L:
         case F2L:
         case I2L:
            if(arg.hasDefinedValue())
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeLongData
                  (arg, null, arg.getValue().longValue(), ih);
            else
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeLongData
                  (arg, null, ih);
            break;
         case F2D:
         case I2D:
         case L2D:
            if(arg.hasDefinedValue())
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeDoubleData
                  (arg, null, arg.getValue().doubleValue(), ih);
            else
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeDoubleData
                  (arg, null, ih);
            break;
         case I2B:
            if(arg.hasDefinedValue())
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeIntData
                  (arg, null, arg.getValue().byteValue(), ih);
            else
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeIntData
                  (arg, null, ih);
            break;
         case I2C:
            if(arg.hasDefinedValue())
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeIntData
                  (arg, null, (char)arg.getValue().intValue(), ih);
            else
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeIntData
                  (arg, null, ih);
            break;
         case I2S:
            if(arg.hasDefinedValue())
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeIntData
                  (arg, null, arg.getValue().shortValue(), ih);
            else
               toPush[v] = new sandmark.analysis.stacksimulator.CompositeFloatData
                  (arg, null, ih);
            break;
         }
      }
      return context.push(toPush);
   }

   // helper method to simulate ArrayInstructions
   private Context doArray(org.apache.bcel.generic.InstructionHandle ih,
                           sandmark.analysis.stacksimulator.Context context)
   {
      int opcode = ih.getInstruction().getOpcode();
      switch(opcode){
      case AALOAD:{
         StackData[] data = context.getStackAt(1);
         org.apache.bcel.generic.Type type =          
            data[0].getType();
         boolean isnull=(type.equals(org.apache.bcel.generic.Type.NULL));
         for (int i=1;i<data.length && !(type instanceof org.apache.bcel.generic.ArrayType);i++){
            if (!type.equals(org.apache.bcel.generic.Type.NULL))
               isnull=false;
            type = data[i].getType();
         }

         org.apache.bcel.generic.ReferenceType elementType = null;

         if (type instanceof org.apache.bcel.generic.ArrayType){
            elementType = (org.apache.bcel.generic.ReferenceType)
               ((org.apache.bcel.generic.ArrayType)type).getElementType();
         }
         else if (isnull){
            // this will only happen if there were no array types and all
            // the types were Type.NULL
            elementType = org.apache.bcel.generic.Type.NULL;
         }
         else{
            //            System.out.println("@@@@@@@@@@@ Cannot determine array type:"+type+" @@@@@@@@@@@@@@@@@@@");

            elementType = org.apache.bcel.generic.Type.OBJECT;
         }

         context = context.pop();
         context = context.pop();

         context = context.push(new sandmark.analysis.stacksimulator.ReferenceData
                                (elementType, ih));
         break;
      }
      case BALOAD:
      case CALOAD:
      case IALOAD:
      case SALOAD:
         context = context.pop();
         context = context.pop();
         context = context.push(new sandmark.analysis.stacksimulator.IntData(ih));
         break;
      case DALOAD:
         context = context.pop();
         context = context.pop();
         context = context.push(new sandmark.analysis.stacksimulator.DoubleData(ih));
         break;
      case FALOAD:
         context = context.pop();
         context = context.pop();
         context = context.push(new sandmark.analysis.stacksimulator.FloatData(ih));
         break;
      case LALOAD:
         context = context.pop();
         context = context.pop();
         context = context.push(new sandmark.analysis.stacksimulator.LongData(ih));
         break;
      case AASTORE:
      case BASTORE:
      case CASTORE:
      case DASTORE:
      case FASTORE:
      case IASTORE:
      case LASTORE:
      case SASTORE:
         context = context.pop();
         context = context.pop();
         context = context.pop();
         break;
      default:
         throw new RuntimeException("Kelly forgot to simulate opcode: " + opcode);
      }

      return context;
   }

   // helper method to simulate ComparisonInstructions
   private Context doComparison(sandmark.analysis.stacksimulator.Context context,
                                java.lang.Number NaNValue, boolean NaN,
                                org.apache.bcel.generic.InstructionHandle instH)
   {
      /*
        If value2 is greater than value1, the integer 1 is pushed onto
        the stack. If value1 is greater than value2, the integer -1
        is pushed onto the stack.
        (G)If either number is NaN, the integer 1 is pushed onto the stack.
        (L) If either number is NaN, the integer -1 is pushed onto the stack.
        +0.0 and -0.0 are treated as equal.
      */

      /*I know its weird, but according to the Jasmin instruction spec,
        http://mrl.nyu.edu/~meyer/jvmref/, cmpl is the same as cmpg
      */

      sandmark.analysis.stacksimulator.StackData [] value1 =
         context.getStackAt(0);
      context = context.pop();
      sandmark.analysis.stacksimulator.StackData [] value2 =
         context.getStackAt(0);
      context = context.pop();
      sandmark.analysis.stacksimulator.StackData [] toPush =
         new sandmark.analysis.stacksimulator.StackData
         [value1.length * value2.length];

      for(int i1 = 0; i1 < value1.length; i1++){
         sandmark.analysis.stacksimulator.PrimitiveData v1 =
            (sandmark.analysis.stacksimulator.PrimitiveData)value1[i1];
         for(int i2 = 0; i2 < value2.length; i2++){
            sandmark.analysis.stacksimulator.PrimitiveData v2 =
               (sandmark.analysis.stacksimulator.PrimitiveData)value2[i2];

            int index = i1*value2.length + i2;

            if(v1.hasDefinedValue() && v2.hasDefinedValue()){
               if(v1.getValue().equals(NaNValue) ||
                  v2.getValue().equals(NaNValue))
                  toPush[index] = new sandmark.analysis.stacksimulator.CompositeIntData
                     (v1, v2, NaN?1:-1, instH);
               else{
                  int compare = ((Comparable)v2.getValue()).compareTo(v1.getValue());
                  toPush[index] = new
                     sandmark.analysis.stacksimulator.CompositeIntData
                     (v1, v2, compare>0?1:compare<0?-1:0, instH);
               }

            }
            else
               toPush[index] = new sandmark.analysis.stacksimulator.CompositeIntData
                  (v1, v2, instH);
         }
      }
      return context.push(toPush);
   }

   private sandmark.analysis.stacksimulator.StackData getDataForType
      (org.apache.bcel.generic.Type dataType,
       org.apache.bcel.generic.InstructionHandle ih)
   {
      if (dataType instanceof org.apache.bcel.generic.ArrayType){
         return new ArrayReferenceData
            ((org.apache.bcel.generic.ReferenceType)dataType, ih, new IntData(null));
      }
      else if(dataType instanceof org.apache.bcel.generic.ReferenceType)
         return new sandmark.analysis.stacksimulator.ReferenceData
            ((org.apache.bcel.generic.ReferenceType)dataType, ih);
      else if(dataType.equals(org.apache.bcel.generic.Type.INT) ||
              dataType.equals(org.apache.bcel.generic.Type.CHAR) ||
              dataType.equals(org.apache.bcel.generic.Type.SHORT) ||
              dataType.equals(org.apache.bcel.generic.Type.BOOLEAN) ||
              dataType.equals(org.apache.bcel.generic.Type.BYTE))
         return new sandmark.analysis.stacksimulator.IntData(ih);
      else if(dataType.equals(org.apache.bcel.generic.Type.LONG))
         return new sandmark.analysis.stacksimulator.LongData(ih);
      else if(dataType.equals(org.apache.bcel.generic.Type.FLOAT))
         return new sandmark.analysis.stacksimulator.FloatData(ih);
      else if(dataType.equals(org.apache.bcel.generic.Type.DOUBLE))
         return new sandmark.analysis.stacksimulator.DoubleData(ih);
      else
         throw new IllegalArgumentException(dataType + "is not a valid type.");
   }

   private static void apply(sandmark.program.Application app) throws Exception
   {
      java.util.Iterator classItr = app.classes();
      while(classItr.hasNext()){
         sandmark.program.Class cls = (sandmark.program.Class)classItr.next();
         org.apache.bcel.generic.ConstantPoolGen cpg =
            cls.getConstantPool();
         sandmark.program.Method[] methods =
            cls.getMethods();
         String className = cls.getName();

         for(int i = 0; i < methods.length; i++){
            if(methods[i].isAbstract() || methods[i].isInterface() || methods[i].isNative() ||
               methods[i].getInstructionList()==null)
               continue;

            //            System.out.println("Doing method "+className+"."+methods[i].getName()+methods[i].getSignature());

            StackSimulator simul = new StackSimulator(methods[i]);
            org.apache.bcel.generic.InstructionHandle [] handles =
               methods[i].getInstructionList().getInstructionHandles();
            for(int h = 0; h < handles.length; h++){
               Context cx = simul.getInstructionContext(handles[h]);
               if(cx == null)
                  throw new RuntimeException(handles[h] + " has null context");
            }
         }
      }
   }

   public static void apply(sandmark.program.Method method, int index) {
      StackSimulator simul = new StackSimulator(method);
      org.apache.bcel.generic.InstructionHandle [] handles =
         method.getInstructionList().getInstructionHandles();

      for (int h=0;h<handles.length;h++){
         if (handles[h].getPosition()!=index)
            continue;
         System.out.println("Context for "+handles[h]);
         Context context = simul.getInstructionContext(handles[h]);
         for (int i=0;i<context.getStackSize();i++){
            System.out.println("Stack Index "+i);
            StackData[] data = context.getStackAt(i);
            for (int j=0;j<data.length;j++){
               System.out.println("   "+data[j]);
            }
         }

         for (int i=0;i<context.getLocalVariableCount();i++){
            StackData[] data = context.getLocalVariableAt(i);
            if (data==null)
               continue;
            System.out.println("Local Variable "+i);
            
            for (int j=0;j<data.length;j++){
               System.out.println("   "+data[j]);
            }
         }
      }
      method.mark();
   }

   /** For testing purposes only */
   public static void main(String[] args) throws Exception
   {
      if (args.length < 1){
         System.out.println("Usage: StackSimulator JAR_FILE");
         System.exit(1);
      }

      try{
         sandmark.program.Application app =
            new sandmark.program.Application(args[0]);
         if(args.length >= 4){
            int index=-1;
            if (args.length>4)
               index = Integer.parseInt(args[4]);
            apply(app.getClass(args[1]).getMethod(args[2],args[3]), index);
         }
         else
            apply(app);
      }
      catch (java.io.IOException e){
         System.err.println("I/O Error: " + e.getMessage());
      }
   }

   private sandmark.analysis.stacksimulator.StackData [] doLoad
      (sandmark.analysis.stacksimulator.StackData[] temp,
       java.lang.reflect.Constructor withValue,
       java.lang.reflect.Constructor withOutValue,
       org.apache.bcel.generic.InstructionHandle instH)
   {
      sandmark.analysis.stacksimulator.StackData [] toPush;
      try{
         if(temp == null){
            toPush = new sandmark.analysis.stacksimulator.StackData []{
               (sandmark.analysis.stacksimulator.StackData)
               withOutValue.newInstance(new Object[]{instH})
            };
         }else{
            toPush = new sandmark.analysis.stacksimulator.StackData[temp.length];
            for(int i = 0; i < toPush.length; i++){
               PrimitiveData num = null;

               if (temp[i] instanceof PrimitiveData)
                  num = (PrimitiveData)temp[i];

               if (num != null && num.hasDefinedValue())
                  toPush[i] = (StackData)
                     withValue.newInstance
                     (new Object[]{num.getValue(), instH});
               else
                  toPush[i] = (StackData)
                     withOutValue.newInstance
                     (new Object[]{instH});
            }
         }
         return toPush;
      }
      catch (RuntimeException re) {
         throw re;
      }
      catch (Exception e) { //InstanciationException, IllegalAccessException,
         //ClassNotFoundException, NoSuchMethodExeption...
         throw new RuntimeException(e.toString());
      }
   }

}

