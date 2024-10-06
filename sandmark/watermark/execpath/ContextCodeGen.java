package sandmark.watermark.execpath;

public class ContextCodeGen extends WMCodeGen {
    protected ConditionGenerator mCondGen;
    protected java.util.Iterator mGens[];
    private boolean isLengthOne;
    public ContextCodeGen(sandmark.program.Application app,java.util.Iterator nodes) 
	throws CodeGenException {
	super(app,nodes);

	mNodes.next(); 
	isLengthOne = mNodes.hasNext() ; 
	mNodes.pushBack();

	try {
	    mCondGen = new ConditionGenerator(mNodes,app);
	    mGens = new java.util.Iterator[] {
		mCondGen.getConditions(1,true),
		mCondGen.getConditions(0,true),
	    };
	    if(!mGens[0].hasNext() || !mGens[1].hasNext())
		throw new CodeGenException("not enough tests");
	} catch(IllegalArgumentException e) {
	    throw new CodeGenException(e.toString());
	}
    }
    public void insert(String bits) {
	int length = bits.length();
	int liveInt = findLiveInt();
	int loc = mMethod.calcMaxLocals();
	org.apache.bcel.generic.InstructionHandle nop = 
	    mMethod.getInstructionList().append
	    (new org.apache.bcel.generic.NOP());
	updateTargeters(mIH,nop);
	org.apache.bcel.generic.InstructionList predicates[] =
	    new org.apache.bcel.generic.InstructionList[length + 2];
	int count[] = new int[2];
	for(int i = 0 ; i < length ; i++)
	    count[bits.charAt(i) == '0' ? 0 : 1]++;
	if(isLengthOne && bits.charAt(length - 1) == '0')
	    count[1]++;
	java.util.List tests[] = new java.util.List[] {
	    new java.util.ArrayList(count[0]),
	    new java.util.ArrayList(count[1]),
	};
	for(int i = 0 ; i < count.length ; i++)
	    for(int j = 0 ; j < count[i] && mGens[i].hasNext(); j++)
		tests[i].add(mGens[i].next());
	for(int i = 0 ; i < count.length ; i++)
	    for(java.util.Iterator it = tests[i].iterator() ; it.hasNext() ; ) {
		org.apache.bcel.generic.InstructionList il = 
		    (org.apache.bcel.generic.InstructionList)it.next();
		if(il.size() > 5 && tests[i].size() != 1)
		    it.remove();
	    }
	for(int i = 0 ; i < length ; i++) {
	    int bit = bits.charAt(i) == '0' ? 0 : 1;
	    predicates[i] = 
		((org.apache.bcel.generic.InstructionList)
		 tests[bit].get(i % tests[bit].size())).copy();
	}
	if(isLengthOne && bits.charAt(length - 1) == '0')
	    predicates[length] = 
		((org.apache.bcel.generic.InstructionList)
		 tests[1].get(length % tests[1].size())).copy();
	for(int i = 0 ; i < length + 2 && predicates[i] != null ; i++) {
	    if(predicates[i + 1] == null)
		((org.apache.bcel.generic.BranchHandle)
		 predicates[i].getEnd()).setTarget
		    (isLengthOne ? predicates[0].getStart() : mIH);
	    else
		((org.apache.bcel.generic.BranchHandle)
		 predicates[i].getEnd()).setTarget(predicates[i + 1].getStart());
	}
	org.apache.bcel.generic.InstructionList il = 
	    new org.apache.bcel.generic.InstructionList();
	il.append(new org.apache.bcel.generic.ICONST(0));
	il.append(new org.apache.bcel.generic.ISTORE(loc));
	for(int i = 0 ; i < predicates.length ; i++)
	    for(int j = 0 ; j < predicates.length ; j++)
		if(i != j && predicates[i] == predicates[j] && predicates [i] != null)
		    throw new RuntimeException();
	for(int i = 0 ; i < length + 2 && predicates[i] != null ; i++) {
	    il.append(predicates[i]);
	    il.append(new org.apache.bcel.generic.IINC(loc,1));
	}
	il.setPositions();
	{
	    org.apache.bcel.generic.InstructionTargeter targeters[] =
		nop.getTargeters();
	    if(targeters != null)
		for(int i = 0 ; i < targeters.length ; i++)
		    targeters[i].updateTarget(nop,il.getStart());
	    try { mMethod.getInstructionList().delete(nop); }
	    catch(org.apache.bcel.generic.TargetLostException e) { 
		throw new RuntimeException();
	    }
	}
	mMethod.getInstructionList().insert(mIH,il);
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
		(mIH,new org.apache.bcel.generic.ILOAD(loc));
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
