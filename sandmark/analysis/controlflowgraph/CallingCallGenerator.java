package sandmark.analysis.controlflowgraph;

public class CallingCallGenerator implements CallGenerator {
    public static boolean DEBUG = false;
    private MethodCFG mSrcCFG;
    private sandmark.program.Method mDestMG;
    private sandmark.program.Method mSrcMG;
    private org.apache.bcel.generic.Type[] mDestArgTypes;
    private org.apache.bcel.generic.Type mDestReturnType;
    private boolean mSrcEdgeIsFallthrough;
    private sandmark.analysis.stacksimulator.Context mSrcEdgeCx;
    private int mSrcCurrentMaxLocals;
    private Edge mSrcEdge;
    private org.apache.bcel.generic.InstructionFactory mFactory;
    private BasicBlock mCallBlock;
    private BasicBlock mRvPopBlock;
    private BasicBlock mRestoreAndGotoBlock;
    private BasicBlock mExceptionHandlerBlock;
    public void addPhantomCall(MethodCFG srcMethod,Edge srcEdge,
                               MethodCFG destMethod) {
	mSrcCFG = srcMethod;
	mSrcEdge = srcEdge;
	mDestMG = destMethod.method();
	mSrcMG = srcMethod.method();

	mDestArgTypes = mDestMG.getArgumentTypes();
	mDestReturnType = mDestMG.getReturnType();
	mSrcEdgeIsFallthrough = mSrcCFG.edgeIsFallthrough
	    ((BasicBlock)srcEdge.src(),(BasicBlock)srcEdge.dest());
	if(DEBUG)
	    System.out.println("edge " + (mSrcEdgeIsFallthrough ? "is" : "is not") +
			       " a fallthrough edge");

	mSrcMG.getInstructionList().setPositions(true);
	mSrcCurrentMaxLocals = mSrcMG.calcMaxLocals();

	if(DEBUG)
        System.out.println("simulating stack");
	sandmark.analysis.stacksimulator.StackSimulator ss =
	    new sandmark.analysis.stacksimulator.StackSimulator(mSrcCFG);
	if(DEBUG)
        System.out.println("done simulating stack");

	mSrcEdgeCx = ss.getInstructionContext
	    (((BasicBlock)mSrcEdge.dest()).getIH());
	if(mSrcEdgeCx == null) {
	    if(DEBUG)
		System.out.println
		    ("no context for " +
		     ((BasicBlock)mSrcEdge.dest()).getInstList().get(0));
	    return;
	}

	mFactory =
	    new org.apache.bcel.generic.InstructionFactory
	    (mSrcCFG.method().getConstantPool());

	buildCallBlock();
	buildRvPopBlock();
	buildRestoreAndGotoBlock();
	java.util.List newExceptions = buildExceptionHandlerBlock();
	mSrcMG.getInstructionList().setPositions(true);
	insertBlocks(newExceptions);

        mSrcMG.setMaxLocals();
	mSrcMG.getInstructionList().setPositions(true);

        if(DEBUG) {
            System.out.println("simulating stack");
            new sandmark.analysis.stacksimulator.StackSimulator(mSrcCFG);
            System.out.println("done simulating stack");
        }
    }
    private void buildCallBlock() {
	java.util.ArrayList instrs = new java.util.ArrayList();
	int stackSize = mSrcEdgeCx.getStackSize();
	for(int i = 0 ; i < stackSize ; i++) {
	    sandmark.analysis.stacksimulator.StackData stackItems[] =
		mSrcEdgeCx.getStackAt(i);
	    org.apache.bcel.generic.Type stackType =
		stackItems[0].getType();
	    if(stackType instanceof 
	       org.apache.bcel.verifier.structurals.UninitializedObjectType) {
		stackType =
		    ((org.apache.bcel.verifier.structurals.UninitializedObjectType)
		     stackType).getInitialized();
	    }
	    instrs.add(mSrcMG.getInstructionList().append
		       (org.apache.bcel.generic.InstructionFactory.createStore(stackType,
					     mSrcCurrentMaxLocals)));
	    mSrcCurrentMaxLocals += stackItems[0].getSize();
	}
	if(!mDestMG.isStatic()) {
	    org.apache.bcel.generic.Instruction push =
		org.apache.bcel.generic.InstructionFactory.createNull
		(org.apache.bcel.generic.Type.getType
		 ("Ljava/lang/Object;"));
	    org.apache.bcel.generic.InstructionHandle pushIH =
		mSrcMG.getInstructionList().append(push);
	    instrs.add(pushIH);
	}
	for(int i = 0 ; i < mDestArgTypes.length ; i++) {
	    org.apache.bcel.generic.Instruction push =
		org.apache.bcel.generic.InstructionFactory.createNull(mDestArgTypes[i]);
	    org.apache.bcel.generic.InstructionHandle pushIH =
		mSrcMG.getInstructionList().append(push);
	    instrs.add(pushIH);
	}
	short invokeType = org.apache.bcel.Constants.INVOKEVIRTUAL;
	if(mDestMG.isStatic())
	    invokeType = org.apache.bcel.Constants.INVOKESTATIC;
	else if(mDestMG.getName().equals("<init>"))
	    invokeType = org.apache.bcel.Constants.INVOKESPECIAL;
	org.apache.bcel.generic.InvokeInstruction inv =
	    mFactory.createInvoke
	    (mDestMG.getClassName(),mDestMG.getName(),
	     mDestMG.getReturnType(),mDestMG.getArgumentTypes(),
	     invokeType);
	org.apache.bcel.generic.InstructionHandle callIH =
	    mSrcMG.getInstructionList().append(inv);
	instrs.add(callIH);

	/* Put all the instructions into a block */
	mCallBlock = new BasicBlock(mSrcCFG);
	java.util.Iterator ihIt = instrs.iterator();
	while(ihIt.hasNext()) {
	    org.apache.bcel.generic.InstructionHandle ih =
		(org.apache.bcel.generic.InstructionHandle)
		ihIt.next();
	    mCallBlock.addInst(ih);
	}
    }
    private void buildRvPopBlock() {
	org.apache.bcel.generic.InstructionHandle ih;
	if(!mDestReturnType.equals(org.apache.bcel.generic.Type.VOID)) {
	    org.apache.bcel.generic.Instruction pop =
		org.apache.bcel.generic.InstructionFactory.createPop(mDestReturnType.getSize());
	    ih = mSrcMG.getInstructionList().append(pop);
	} else {
	    ih = mSrcMG.getInstructionList().append
		(new org.apache.bcel.generic.NOP());
	}
	mRvPopBlock = new BasicBlock(mSrcCFG);
	mRvPopBlock.addInst(ih);
    }
    private void buildRestoreAndGotoBlock() {
	java.util.ArrayList instrs = new java.util.ArrayList();
	int stackSize = mSrcEdgeCx.getStackSize();
	for(int i = stackSize - 1,
		nextAvailLocal = mSrcCurrentMaxLocals ; i >= 0 ; i--) {
	    sandmark.analysis.stacksimulator.StackData stackItems[] =
		mSrcEdgeCx.getStackAt(i);
	    org.apache.bcel.generic.Type stackType =
		stackItems[0].getType();
	    if(stackType instanceof 
	       org.apache.bcel.verifier.structurals.UninitializedObjectType) {
		stackType =
		    ((org.apache.bcel.verifier.structurals.UninitializedObjectType)
		     stackType).getInitialized();
	    }
	    instrs.add(mSrcMG.getInstructionList().append
		       (org.apache.bcel.generic.InstructionFactory.createLoad(stackType,
					    nextAvailLocal -
					    stackItems[0].getSize())));
	    nextAvailLocal -= stackItems[0].getSize();
	}

	java.util.ArrayList srcInstrs =
	    ((BasicBlock)mSrcEdge.src()).getInstList();
	org.apache.bcel.generic.InstructionHandle srcLastInstrH =
	    (org.apache.bcel.generic.InstructionHandle)srcInstrs.get
	    (srcInstrs.size() - 1);
	java.util.ArrayList destInstrs =
	    ((BasicBlock)mSrcEdge.dest()).getInstList();
	org.apache.bcel.generic.InstructionHandle destFirstInstrH =
	    (org.apache.bcel.generic.InstructionHandle)destInstrs.get(0);
	org.apache.bcel.generic.InstructionTargeter targeter = null;
	if(srcLastInstrH != null &&
	   srcLastInstrH.getInstruction() instanceof
	   org.apache.bcel.generic.InstructionTargeter)
	    targeter = (org.apache.bcel.generic.InstructionTargeter)
		srcLastInstrH.getInstruction();

	/* If the edge that was chosen above was a targeted edge,
	   not a fallthrough edge, change the target to the
	   beginning of tthe call block, and insert a goto to
	   jump the the original target after the call */
	if(targeter != null &&
	   targeter.containsTarget(destFirstInstrH))
	    targeter.updateTarget(destFirstInstrH,
				  mCallBlock.getIH());

	if(!mSrcEdgeIsFallthrough) {
	    org.apache.bcel.generic.BranchInstruction goToInstruction =
		org.apache.bcel.generic.InstructionFactory.createBranchInstruction
		    (org.apache.bcel.Constants.GOTO,
						 destFirstInstrH);
	    org.apache.bcel.generic.BranchHandle goToIH =
		mSrcMG.getInstructionList().append
		(goToInstruction);
	    instrs.add(goToIH);
	}
	if(instrs.size() == 0)
	    instrs.add(mSrcMG.getInstructionList().append
		       (new org.apache.bcel.generic.NOP()));
	mRestoreAndGotoBlock = new BasicBlock(mSrcCFG);
	java.util.Iterator it = instrs.iterator();
	while(it.hasNext())
	    mRestoreAndGotoBlock.addInst
		((org.apache.bcel.generic.InstructionHandle)it.next());
    }
    private java.util.List buildExceptionHandlerBlock() {
	String exceptions[] = mDestMG.getExceptions();
	if(exceptions == null || exceptions.length == 0)
	    return new java.util.LinkedList();

	mExceptionHandlerBlock = new BasicBlock(mSrcCFG);
	java.util.ArrayList callInstrs = mCallBlock.getInstList();
	org.apache.bcel.generic.InstructionHandle callIH =
	    (org.apache.bcel.generic.InstructionHandle)
	    callInstrs.get(callInstrs.size() - 1);
	
	org.apache.bcel.generic.InstructionHandle popIH =
	    mSrcMG.getInstructionList().append
	    (org.apache.bcel.generic.InstructionFactory.createPop(1));
	
	org.apache.bcel.generic.BranchInstruction goToInstruction =
	org.apache.bcel.generic.InstructionFactory.createBranchInstruction
	    (org.apache.bcel.Constants.GOTO,
					     mRestoreAndGotoBlock.getIH());
	org.apache.bcel.generic.BranchHandle goToIH =
	    mSrcMG.getInstructionList().append
	    (goToInstruction);
	mExceptionHandlerBlock.addInst(popIH);
	mExceptionHandlerBlock.addInst(goToIH);
	java.util.ArrayList newExceptions = new java.util.ArrayList();
	for(int i = 0 ; i < exceptions.length ; i++)
	    newExceptions.add(mSrcMG.addExceptionHandler
			      (callIH,callIH,popIH,
			       new org.apache.bcel.generic.ObjectType
			       (exceptions[i])));
	return newExceptions;
    }
    private void insertBlocks(java.util.List newExceptions) {
	mSrcCFG.addNode(mCallBlock);
	mSrcCFG.addNode(mRvPopBlock);
	mSrcCFG.addNode(mRestoreAndGotoBlock);
	if(mExceptionHandlerBlock != null)
	    mSrcCFG.addNode(mExceptionHandlerBlock);
	mSrcCFG.addEdge(new FallthroughEdge(mCallBlock,mRvPopBlock));
	if(mExceptionHandlerBlock != null)
	    mSrcCFG.addEdge
		(new sandmark.analysis.controlflowgraph.ExceptionEdge
		 (mCallBlock,mExceptionHandlerBlock,
		  (org.apache.bcel.generic.CodeExceptionGen [])
		  newExceptions.toArray
		  (new org.apache.bcel.generic.CodeExceptionGen[0])));
	mSrcCFG.addEdge(new FallthroughEdge(mRvPopBlock,mRestoreAndGotoBlock));
	if(mExceptionHandlerBlock != null)
	    mSrcCFG.addEdge(mExceptionHandlerBlock,mRestoreAndGotoBlock);
	if(mSrcEdgeIsFallthrough) {
	    BasicBlock src = (BasicBlock)mSrcEdge.src();
	    BasicBlock dest = (BasicBlock)mSrcEdge.dest();
	    src.setFallthrough(mCallBlock);
	    mSrcCFG.addEdge(new FallthroughEdge(src,mCallBlock));
	    mRestoreAndGotoBlock.setFallthrough(dest);
	    mSrcCFG.addEdge(new FallthroughEdge(mRestoreAndGotoBlock,dest));
	} else {
	    mSrcCFG.addEdge(mSrcEdge.src(),mCallBlock);
	    mSrcCFG.addEdge(mRestoreAndGotoBlock,mSrcEdge.dest());
	}
	mCallBlock.setFallthrough(mRvPopBlock);
	mRvPopBlock.setFallthrough(mRestoreAndGotoBlock);

	mSrcCFG.removeEdge(mSrcEdge.src(),mSrcEdge.dest());

	org.apache.bcel.generic.CodeExceptionGen exceptions[] =
	    mSrcMG.getExceptionHandlers();
	for(int i = 0 ; i < exceptions.length ; i++) {
	    if(exceptions[i].getHandlerPC() ==
		((BasicBlock)mSrcEdge.dest()).getIH()) {
                if(DEBUG)
                    System.out.println("Exception edge");
		exceptions[i].setHandlerPC(mCallBlock.getIH());
		java.util.ArrayList throwers = new java.util.ArrayList();
		java.util.Iterator predIter = 
		   mSrcCFG.preds(mSrcEdge.dest());
		while (predIter.hasNext())
		   throwers.add(predIter.next());
		java.util.Iterator throwerIt = throwers.iterator();
		while(throwerIt.hasNext()) {
		    BasicBlock pred = (BasicBlock)throwerIt.next();
                    if(DEBUG)
                        System.out.println("thrower pred: " + pred);
		    if(pred != null) {
                        if(DEBUG)
                            System.out.println("Moving extra exception edge " +
                                               "from " + pred + " to " +
                                               mSrcEdge.dest() + " to go to " +
                                               mCallBlock);
			mSrcCFG.removeEdge(pred,mSrcEdge.dest());
			mSrcCFG.addEdge(pred,mCallBlock);
		    }
		}
	    }
	}
	mSrcMG.getInstructionList().setPositions(true);
	//mSrcCFG.printCFG();
    }
}

