package sandmark.watermark.execpath;

public class LoopCodeGen extends WMCodeGen {
    public LoopCodeGen(sandmark.program.Application app,java.util.Iterator nodes) 
	throws CodeGenException {
	super(app,nodes);
    }
    public void insert(String bits) {
	org.apache.bcel.generic.InstructionList list =
	    new org.apache.bcel.generic.InstructionList();
	org.apache.bcel.generic.InstructionFactory factory =
	    new org.apache.bcel.generic.InstructionFactory
	    (mMethod.getConstantPool());
	int liveInt = findLiveInt();
	int freeLoc = mMethod.calcMaxLocals();
	list.append(new org.apache.bcel.generic.ICONST(0));
	list.append(new org.apache.bcel.generic.ISTORE(freeLoc));
	for(int i = 0 ; i < bits.length() ; ) {
	    long theBits = 0;
	    int curBits = 63;
	    if(i + curBits > bits.length())
		curBits = bits.length() - i;

	    if(i + curBits != bits.length())
		for( ; curBits > 0 && bits.charAt(i + curBits - 1) != '1' ; 
		     curBits--)
		    ;

	    if(curBits <= 0)
		throw new RuntimeException(bits + " " + i);

	    for(int j = 0 ; j < curBits ; j++)
		theBits |= ((bits.charAt(i + j) == '1' ? 1L : 0L) << (j + 1));

	    i += curBits;

	    if(i != bits.length()) {
		theBits &= ~(1L << curBits);
		curBits--;
	    }

	    list.append(factory.createConstant(new Long(theBits)));
	    list.append(factory.createConstant(new Integer(curBits + 1)));
	    org.apache.bcel.generic.BranchHandle loopHead = 
		list.append(new org.apache.bcel.generic.GOTO(null));
	    org.apache.bcel.generic.InstructionHandle loopTarget =
		list.append(new org.apache.bcel.generic.ICONST(1));
	    list.append(new org.apache.bcel.generic.ISUB());
	    list.append(new org.apache.bcel.generic.DUP_X2());
	    list.append(new org.apache.bcel.generic.POP());
	    list.append(new org.apache.bcel.generic.DUP2());
	    list.append(new org.apache.bcel.generic.L2I());
	    list.append(new org.apache.bcel.generic.ICONST(1));
	    list.append(new org.apache.bcel.generic.IAND());
	    org.apache.bcel.generic.BranchHandle bitBranch =
		list.append(new org.apache.bcel.generic.IFEQ(null));
	    list.append(new org.apache.bcel.generic.IINC(freeLoc,1));
	    bitBranch.setTarget
		(list.append(new org.apache.bcel.generic.ICONST(1)));
	    list.append(new org.apache.bcel.generic.LUSHR());
	    list.append(new org.apache.bcel.generic.DUP2_X1());
	    list.append(new org.apache.bcel.generic.POP2());
	    loopHead.setTarget
		(list.append(new org.apache.bcel.generic.DUP()));
	    list.append(new org.apache.bcel.generic.IFNE(loopTarget));
	    list.append(new org.apache.bcel.generic.POP());
	    list.append(new org.apache.bcel.generic.POP2());
	}
	updateTargeters(mIH,list.getStart());
	mMethod.getInstructionList().insert(mIH,list);
	    
	if(liveInt != -1) {
	    sandmark.util.opaquepredicatelib.PredicateFactory preds[] =
		sandmark.util.opaquepredicatelib.OpaqueManager.getPredicatesByValue
		(sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE);
	    sandmark.util.opaquepredicatelib.OpaquePredicateGenerator pred =
		preds[0].createInstance();
	    pred.insertPredicate
		(mMethod,mIH,
		 sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE);
	    org.apache.bcel.generic.BranchHandle branch = 
		mMethod.getInstructionList().insert
		(mIH,new org.apache.bcel.generic.IFNE(null));
	    mMethod.getInstructionList().insert
		(mIH,new org.apache.bcel.generic.ILOAD(liveInt));
	    mMethod.getInstructionList().insert
		(mIH,new org.apache.bcel.generic.ILOAD(freeLoc));
	    mMethod.getInstructionList().insert
		(mIH,new org.apache.bcel.generic.IADD());
	    mMethod.getInstructionList().insert
		(mIH,new org.apache.bcel.generic.ISTORE(liveInt));
	    branch.setTarget
		(mMethod.getInstructionList().insert
		 (mIH,new org.apache.bcel.generic.NOP()));
	}

	mMethod.setMaxLocals();
	mMethod.mark();
	mMethod.removeLineNumbers();
	mMethod.removeLocalVariables();
	mMethod.getInstructionList().setPositions(true);
    }
}
