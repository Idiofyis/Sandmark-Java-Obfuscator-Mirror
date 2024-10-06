package sandmark.analysis.controlflowgraph;

public class TopoMaintainingCallGenerator implements CallGenerator {
    public static boolean DEBUG = false;
    private MethodCFG mSrcCFG;
    private sandmark.program.Method mDestMG;
    private sandmark.program.Method mSrcMG;
    private org.apache.bcel.generic.Type[] mDestArgTypes;
    private org.apache.bcel.generic.Type mDestReturnType;
    private sandmark.analysis.stacksimulator.Context mSrcEdgeCx;
    private int mSrcCurrentMaxLocals;
    private Edge mSrcEdge;
    private org.apache.bcel.generic.InstructionFactory mFactory;
    public void addPhantomCall(MethodCFG srcMethod,Edge srcEdge,
                               MethodCFG destMethod) {
	mSrcCFG = srcMethod;
	mSrcEdge = srcEdge;
	mDestMG = destMethod.method();
	mSrcMG = srcMethod.method();

	mDestArgTypes = mDestMG.getArgumentTypes();
	mDestReturnType = mDestMG.getReturnType();

	mSrcMG.getInstructionList().setPositions(true);
	mSrcCurrentMaxLocals = mSrcMG.calcMaxLocals();

	if(mSrcMG.getExceptions() != null && mSrcMG.getExceptions().length != 0)
	    throw new RuntimeException("Can't maintain topology for methods " +
				       "that throw checked exception");

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

	java.util.ArrayList newInstrs = new java.util.ArrayList();
	generateCall(newInstrs);
	generateRvPop(newInstrs);
	generateRestore(newInstrs);
	mSrcMG.getInstructionList().setPositions(true);
	insertCode(newInstrs);

        mSrcMG.setMaxLocals();
	mSrcMG.getInstructionList().setPositions(true);

        if(DEBUG) {
            System.out.println("simulating stack");
		new sandmark.analysis.stacksimulator.StackSimulator(mSrcCFG);
            System.out.println("done simulating stack");
        }
    }
    private void generateCall(java.util.ArrayList instrs) {
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
	short invokeType = mDestMG.isStatic() ? 
	    org.apache.bcel.Constants.INVOKESTATIC : 
	    org.apache.bcel.Constants.INVOKEVIRTUAL;	
	org.apache.bcel.generic.InvokeInstruction inv =
	    mFactory.createInvoke
	    (mDestMG.getClassName(),mDestMG.getName(),
	     mDestMG.getReturnType(),mDestMG.getArgumentTypes(),
	     invokeType);
	org.apache.bcel.generic.InstructionHandle callIH =
	    mSrcMG.getInstructionList().append(inv);
	instrs.add(callIH);
    }
    private void generateRvPop(java.util.ArrayList instrs) {
	org.apache.bcel.generic.InstructionHandle ih;
	if(!mDestReturnType.equals(org.apache.bcel.generic.Type.VOID)) {
	    org.apache.bcel.generic.Instruction pop =
		org.apache.bcel.generic.InstructionFactory.createPop(mDestReturnType.getSize());
	    ih = mSrcMG.getInstructionList().append(pop);
	} else {
	    ih = mSrcMG.getInstructionList().append
		(new org.apache.bcel.generic.NOP());
	}
	instrs.add(ih);
    }
    private void generateRestore(java.util.ArrayList instrs) {
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
    }
    private void insertCode(java.util.ArrayList instrs) {
	org.apache.bcel.generic.InstructionHandle origLeader =
	    ((BasicBlock)mSrcEdge.dest()).getIH();
	org.apache.bcel.generic.InstructionHandle newLeader =
	    (org.apache.bcel.generic.InstructionHandle)instrs.get(0);

	mSrcMG.getInstructionList().redirectExceptionHandlers
	    (mSrcMG.getExceptionHandlers(),origLeader,newLeader);
	mSrcMG.getInstructionList().redirectBranches(origLeader,newLeader);

	instrs.addAll(((BasicBlock)mSrcEdge.dest()).getInstList());
	((BasicBlock)mSrcEdge.dest()).getInstList().clear();
	((BasicBlock)mSrcEdge.dest()).getInstList().addAll(instrs);
    }
}

