package sandmark.util;

public class Inliner {
    private static final boolean DEBUG = false;
    private sandmark.program.Method mCallingMethod;
    public Inliner(sandmark.program.Method caller) {
	mCallingMethod = caller;
    }
    public void inline(sandmark.program.Method callee,
		       org.apache.bcel.generic.InstructionHandle callSite) {
// 	System.out.println("inlining " + 
// 			   sandmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName(callee) +
// 			   " into " + 
// 			   sandmark.analysis.controlflowgraph.ProgramCFG.fieldOrMethodName(mCallingMethod));

	/* We want to leave the method to be inlined unchanged and 
	   insert a munged copy of it into its caller, so make a copy 
	*/
        callee.setMaxStack();
	sandmark.program.Method fixedCallee = callee.copy();
	if(DEBUG) {
	    sandmark.util.newgraph.Graphs.dotInFile(mCallingMethod.getCFG(),
						    "cfg.orig.dot");
	    sandmark.util.graph.graphview.GraphList.instance().add
		(mCallingMethod.getCFG(), "cfg.orig");
	}
	sandmark.analysis.stacksimulator.StackSimulator callerSS =
	    new sandmark.analysis.stacksimulator.StackSimulator(mCallingMethod);
	sandmark.analysis.stacksimulator.StackSimulator calleeSS =
	    new sandmark.analysis.stacksimulator.StackSimulator(fixedCallee);
	sandmark.analysis.stacksimulator.Context callSiteCx =
	    callerSS.getInstructionContext(callSite);
	InlinedStackFrame isf = new InlinedStackFrame
	    (callSiteCx,callee,mCallingMethod.calcMaxLocals());

	java.util.Hashtable ihToCx = new java.util.Hashtable();
	for(org.apache.bcel.generic.InstructionHandle ih = 
		fixedCallee.getInstructionList().getStart() ; ih != null ; 
	    ih = ih.getNext())
	    if(ih.getInstruction() instanceof 
	       org.apache.bcel.generic.ReturnInstruction)
		ihToCx.put(ih,calleeSS.getInstructionContext(ih));

        org.apache.bcel.generic.InstructionHandle topNopIH =
            mCallingMethod.getInstructionList().insert
            (callSite,new org.apache.bcel.generic.NOP());
        updateTargeters(callSite,topNopIH);

	/* These methods could be in different classes, so 
	   the constant pool values referenced by the callee
	   may not exist in the caller, and may have different
	   indices.  So add all constants referenced by callee
	   to constant pool of caller, and update indices 
	*/
        rewriteCPInstrs(fixedCallee,mCallingMethod);

	/* An exception could be thrown and caught inside the callee.
	   This would destroy the stack, so we better have a way to
	   restore the stack at the exit of the callee.  Also, the 
	   callee expects to get its arguments in locals.  So save
	   the entire stack into locals
	*/
	insertStackSaveInstrs
	    (mCallingMethod.getInstructionList(),callSite,callSiteCx,isf,callee, mCallingMethod);

	addExceptionHandlersToCaller
	    (fixedCallee.getExceptionHandlers(),mCallingMethod);

	/* Local variables have to be renumbered to fit into the
	   caller, and we have to restore the stack at every exit
	   of the callee 
	*/
	for(org.apache.bcel.generic.InstructionHandle ih =
		fixedCallee.getInstructionList().getStart() ; ih != null ; 
	    ih = ih.getNext()) {
	    if(ih.getInstruction() instanceof
	       org.apache.bcel.generic.LocalVariableInstruction ||
	       ih.getInstruction() instanceof
	       org.apache.bcel.generic.RET) {
		fixupSlotAccessInstruction(ih,isf);
	    }
	    if(ih.getInstruction() instanceof
	       org.apache.bcel.generic.ReturnInstruction) {
		ih = fixupReturnInstruction
		    (ih,callSite,fixedCallee,
		     (sandmark.analysis.stacksimulator.Context)ihToCx.get(ih),
		     callSiteCx,isf);
	    }
	}
	mCallingMethod.getInstructionList().insert
	    (callSite,fixedCallee.getInstructionList());

	/* Before we can remove the call site, we have to move
	   all the targeters of the call to a different target
	*/
	org.apache.bcel.generic.InstructionHandle nopIH =
	    mCallingMethod.getInstructionList().insert
	    (callSite,new org.apache.bcel.generic.NOP());
	updateTargeters(callSite,nopIH);
	try {
	    mCallingMethod.getInstructionList().delete(callSite);
	} catch(org.apache.bcel.generic.TargetLostException e) {
	    throw new RuntimeException(e.toString());
	}

        mCallingMethod.getInstructionList().setPositions(true);

	mCallingMethod.setMaxStack();

        mCallingMethod.mark();

	fixedCallee.delete();

	if(DEBUG) {
	    try {
		mCallingMethod.getStack();
	    } catch(RuntimeException e) {
		sandmark.util.newgraph.Graphs.dotInFile(mCallingMethod.getCFG(),
							"cfg.dot");
		throw e;
	    }
	}
    }
    private int typeArraySize(org.apache.bcel.generic.Type[] typeArray) {
	int size = 0;
	for(int i = 0 ; i < typeArray.length ; 
	    size += typeArray[i].getSize(), i++) 
	    ;
	return size;
    }
    private static void updateTargeters
	(org.apache.bcel.generic.InstructionHandle curIH,
	 org.apache.bcel.generic.InstructionHandle newIH) {
	org.apache.bcel.generic.InstructionTargeter targeters[];
	int i;
	//System.out.println("updating " + curIH);
	for(targeters = curIH.getTargeters(),i = 0 ; 
	    targeters != null && i < targeters.length ; i++) {
	    //System.out.println("targeter: " + targeters[i]);
	    targeters[i].updateTarget(curIH,newIH);
	}	
    }
    private static void rewriteCPInstrs(sandmark.program.Method oldMeth,
					sandmark.program.Method newMeth) {
	org.apache.bcel.generic.InstructionHandle ihs[];
	int i;
	for(ihs = oldMeth.getInstructionList().getInstructionHandles(),
		i = 0 ; i < ihs.length ; i++) {
	    //System.out.println(ihs[i]);
	    org.apache.bcel.generic.Instruction instr = ihs[i].getInstruction();
	    if(instr instanceof org.apache.bcel.generic.CPInstruction) {
		org.apache.bcel.generic.CPInstruction cpi =
		    (org.apache.bcel.generic.CPInstruction)instr;
		org.apache.bcel.classfile.Constant constant =
		    oldMeth.getConstantPool().getConstant(cpi.getIndex());
		cpi.setIndex
		    (newMeth.getConstantPool().addConstant
		     (constant,oldMeth.getConstantPool()));
	    }
	}
    }
    private static void addExceptionHandlersToCaller
        (org.apache.bcel.generic.CodeExceptionGen exceptions[],
         sandmark.program.Method caller) {
	for( int i = 0 ; i < exceptions.length ; i++)
	    caller.addExceptionHandler(exceptions[i].getStartPC(),exceptions[i].getEndPC(),
                                       exceptions[i].getHandlerPC(),
                                       exceptions[i].getCatchType());
    }
    private static void insertStackSaveInstrs
	(org.apache.bcel.generic.InstructionList il,
	 org.apache.bcel.generic.InstructionHandle site,
	 sandmark.analysis.stacksimulator.Context siteCx,
	 InlinedStackFrame isf,
         sandmark.program.Method callee,
         sandmark.program.Method caller) {
	int afterItemSpace = isf.mLocalsFirstSlot;
	for(int i = 0 ; i < siteCx.getStackSize() ; i++) {
	    sandmark.analysis.stacksimulator.StackData sd[] = 
		siteCx.getStackAt(i);
	    afterItemSpace -= sd[0].getSize();
	    org.apache.bcel.generic.Type type = sd[0].getType();
	    if(type instanceof org.apache.bcel.verifier.structurals.UninitializedObjectType)
		type = ((org.apache.bcel.verifier.structurals.UninitializedObjectType)type).getInitialized();
	    org.apache.bcel.generic.InstructionHandle storeIH = 
                il.insert(site, org.apache.bcel.generic.InstructionFactory.createStore
                          (type,afterItemSpace));
            //Adding this check cast for dynamic inlining --zach
            org.apache.bcel.generic.InstructionFactory factory = 
                new org.apache.bcel.generic.InstructionFactory(caller.getConstantPool());
            if(!callee.isStatic() && i == callee.getArgumentTypes().length-1)
                il.append(storeIH, factory.createCheckCast
                          (new org.apache.bcel.generic.ObjectType
                           (callee.getEnclosingClass().getName())));
	}
        //System.out.println(il);
        //try{System.in.read();}catch(Exception e){ e.printStackTrace();}
    }
    private static void fixupSlotAccessInstruction
	(org.apache.bcel.generic.InstructionHandle ih,
	 InlinedStackFrame isf) {
	org.apache.bcel.generic.IndexedInstruction lvi =
	    (org.apache.bcel.generic.IndexedInstruction)
	    ih.getInstruction();
	lvi.setIndex(lvi.getIndex() + isf.mArgsFirstSlot);
    }
    private static org.apache.bcel.generic.InstructionHandle fixupReturnInstruction
	(org.apache.bcel.generic.InstructionHandle returnIH,
	 org.apache.bcel.generic.InstructionHandle callSiteIH,
	 sandmark.program.Method method,
	 sandmark.analysis.stacksimulator.Context returnCx,
	 sandmark.analysis.stacksimulator.Context callSiteCx,
	 InlinedStackFrame isf) {

	/* Update all targeters of this instruction to target
	   the first instruction inserted by this method 
	*/
	returnIH.setInstruction(new org.apache.bcel.generic.NOP());

	/* Save the return value to a local */
	if(!method.getReturnType().equals
	   (org.apache.bcel.generic.Type.VOID)) {
	    returnIH = method.getInstructionList().append
		(returnIH,
		 org.apache.bcel.generic.InstructionFactory.createStore
		 (method.getReturnType(),isf.mRvFirstSlot));
	}

	/* Pop any remaining garbage from the stack */
	{
	    int trashIndex = method.getReturnType().equals
		(org.apache.bcel.generic.Type.VOID) ? 0 : 1;
	    for(int i = trashIndex ; i < returnCx.getStackSize() ; i++)
		returnIH = method.getInstructionList().append
		    (returnIH,
		     org.apache.bcel.generic.InstructionFactory.createPop
		     (returnCx.getStackAt(i)[0].getSize()));
	}

	/* Reload the stack that was saved at the top of the 
	   inlined method */
	sandmark.analysis.stacksimulator.StackData sd[] = null;
	for(int j = callSiteCx.getStackSize() - 1,
		slotNum = isf.mSavedStackFirstSlot ; 
	    slotNum < isf.mArgsFirstSlot ; 
	    j--,slotNum += sd[0].getSize()) { 
	    sd = callSiteCx.getStackAt(j);
	    org.apache.bcel.generic.Type type = sd[0].getType();
	    if(type instanceof 
	       org.apache.bcel.verifier.structurals.UninitializedObjectType)
		type = 
		    ((org.apache.bcel.verifier.structurals.UninitializedObjectType)
		     type).getInitialized();
	    returnIH = method.getInstructionList().append
		(returnIH,
		 org.apache.bcel.generic.InstructionFactory.createLoad
		 (type,slotNum));
	}

	/* Reload the return value that just got saved */
	if(!method.getReturnType().equals
	   (org.apache.bcel.generic.Type.VOID)) {
	    returnIH = method.getInstructionList().append
		(returnIH,
		 org.apache.bcel.generic.InstructionFactory.createLoad
		 (method.getReturnType(),isf.mRvFirstSlot));
	}

	/* This instruction could be in the middle of the 
	   method to be inlined.  Jump to the instruction
	   after the inlined method, the call site (which
	   will subsequently be removed, with all targeters
	   retargetted)
	*/
	return method.getInstructionList().append
	    (returnIH,
	     (org.apache.bcel.generic.BranchInstruction)
	     (new org.apache.bcel.generic.GOTO(callSiteIH)));
    }
}

class InlinedStackFrame {
    int mRvFirstSlot;
    int mSavedStackFirstSlot;
    int mArgsFirstSlot;
    int mLocalsFirstSlot;
    int mUnusedFirstSlot;
    InlinedStackFrame(sandmark.analysis.stacksimulator.Context cx,
			    sandmark.program.Method callee,
			    int firstUnusedSlot) {
	mRvFirstSlot = -1;
	if(!callee.getReturnType().equals
	   (org.apache.bcel.generic.Type.VOID)) {
	    mRvFirstSlot = firstUnusedSlot;
	    firstUnusedSlot += callee.getReturnType().getSize();
	}
	mSavedStackFirstSlot = firstUnusedSlot;
	mArgsFirstSlot = mSavedStackFirstSlot + 
	    cxSize(cx,argCount(callee));
	firstUnusedSlot += cxSize(cx,0);
	mLocalsFirstSlot = firstUnusedSlot;
	firstUnusedSlot += callee.getMaxLocals();
	mUnusedFirstSlot = firstUnusedSlot;
    }
    static int cxSize(sandmark.analysis.stacksimulator.Context cx,
		       int startStackItem) {
	int size = 0;
	for(int i = startStackItem ; i < cx.getStackSize() ; i++) {
	    size += (cx.getStackAt(i))[0].getSize();
	}
	//System.out.println("cxSize: " + size);
	return size;
    }
    static int argCount(sandmark.program.Method mg) {
	int count = mg.getArgumentTypes().length;
	if((mg.getAccessFlags() & org.apache.bcel.Constants.ACC_STATIC) == 0)
	    count++;
	//System.out.println("argc: " + count);
	return count;
    }
}

