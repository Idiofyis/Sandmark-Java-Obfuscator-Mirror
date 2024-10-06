package sandmark.watermark.execpath;

public class WMCodeGen {

    public static class CodeGenException extends Exception {
	CodeGenException(String msg) { super(msg); }
    }

    public static class PushBackIterator implements java.util.Iterator {
	Object lastItem;
	boolean pushedBack = false;
	java.util.Iterator it;
	public PushBackIterator(java.util.Iterator it) {
	    this.it = it;
	}
	public Object next() {
	    if(pushedBack) {
		pushedBack = false;
		return lastItem;
	    }
	    return lastItem = it.next();
	}
	public boolean hasNext() {
	    return pushedBack || it.hasNext();
	}
	public void remove() {
	    throw new UnsupportedOperationException();
	}
	public void pushBack() { pushedBack = true; }
    }

    protected sandmark.program.Method mMethod;
    protected org.apache.bcel.generic.InstructionHandle mIH;
    protected PushBackIterator mNodes;
    public WMCodeGen(sandmark.program.Application app,java.util.Iterator nodes) 
	throws CodeGenException {
	mNodes = new PushBackIterator(nodes);
	TraceNode node = (TraceNode)mNodes.next();
	mNodes.pushBack();
	sandmark.program.Class clazz = 
	    app.getClass(node.getClassName());
	mMethod = 
	    clazz.getMethod(node.getMethodName(),
			    node.getMethodSignature());
	mIH = mMethod.getInstructionList().findHandle
	    (node.getOffset());
	if(mIH == null) {
	    System.out.println(mMethod.getEnclosingClass().getName() + " " +
			       mMethod.getName());
	    System.out.println(mMethod.getInstructionList());
	    throw new Error("bad offset " + node.getOffset());
	}
    }
    public void insert(String bits) {
	org.apache.bcel.generic.InstructionList il = 
	    new org.apache.bcel.generic.InstructionList();
	org.apache.bcel.generic.BranchHandle ifs[] =
	    new org.apache.bcel.generic.BranchHandle[bits.length() + 1];
	il.append(new org.apache.bcel.generic.ICONST(0));
	il.append(new org.apache.bcel.generic.ISTORE(0));
	il.append(new org.apache.bcel.generic.ICONST(0));
	il.append(new org.apache.bcel.generic.ISTORE(1));
	org.apache.bcel.generic.InstructionHandle nopIH = 
	    il.append(new org.apache.bcel.generic.NOP());
	for(int i = 0 ; i < bits.length() ; i++) {
	    il.append(new org.apache.bcel.generic.ILOAD
		      (bits.charAt(i) == '0' ? 0 : 1));
	    ifs[i] = il.append(new org.apache.bcel.generic.IFEQ(nopIH));
	    il.append(new org.apache.bcel.generic.ICONST(0));
	    il.append(new org.apache.bcel.generic.POP());
	}
	il.append(new org.apache.bcel.generic.ILOAD(1));
	ifs[ifs.length - 1] = 
	    il.append(new org.apache.bcel.generic.IFNE(nopIH));
	il.append(new org.apache.bcel.generic.ICONST(1));
	il.append(new org.apache.bcel.generic.ISTORE(1));
	il.append(new org.apache.bcel.generic.GOTO(nopIH));
	for(int i = 0 ; i < ifs.length - 1 ; i++)
	    ifs[i].setTarget(ifs[i + 1].getPrev());
	ifs[ifs.length - 1].setTarget
	    (il.append(new org.apache.bcel.generic.NOP()));
	il.setPositions(true);
	incrementLocals(il,mMethod.getMaxLocals());
	updateTargeters(mIH,il.getStart());
	mMethod.getInstructionList().insert(mIH,il);
	mMethod.setMaxLocals();
	mMethod.mark();
	mMethod.removeLineNumbers();
	mMethod.removeLocalVariables();
	mMethod.getInstructionList().setPositions(true);
    }
    protected static void updateTargeters(org.apache.bcel.generic.InstructionHandle orig,
					  org.apache.bcel.generic.InstructionHandle newH) {
	org.apache.bcel.generic.InstructionTargeter targeters[] =
	    orig.getTargeters();
	if(targeters == null)
	    return;

	for(int i = 0 ; i < targeters.length ; i++)
	    if(targeters[i] instanceof org.apache.bcel.generic.CodeExceptionGen) {
		org.apache.bcel.generic.CodeExceptionGen ceg =
		    (org.apache.bcel.generic.CodeExceptionGen)targeters[i];
		if(ceg.getHandlerPC() == orig)
		    ceg.setHandlerPC(newH);
	    } else
		targeters[i].updateTarget(orig,newH);
    }
    private static void incrementLocals(org.apache.bcel.generic.InstructionList list,int inc) {
	if(list.getStart() == null)
	    throw new RuntimeException();
	for(org.apache.bcel.generic.InstructionHandle ih = list.getStart() ; 
	    ih != null ; ih = ih.getNext()) {
	    org.apache.bcel.generic.Instruction instr = ih.getInstruction();
	    if(!(instr instanceof org.apache.bcel.generic.LocalVariableInstruction) &&
	       !(instr instanceof org.apache.bcel.generic.RET))
		continue;
	    org.apache.bcel.generic.IndexedInstruction ii = 
		(org.apache.bcel.generic.IndexedInstruction)instr;
	    ii.setIndex(ii.getIndex() + inc);
	}
    }
    public static void main(String argv[]) throws Exception {
	sandmark.program.Application app =
	    new sandmark.program.Application();
	sandmark.program.Class clazz = 
	    new sandmark.program.LocalClass
	    (app,"foo","java.lang.Object",
	     "foo.java",org.apache.bcel.Constants.ACC_PUBLIC |
	     org.apache.bcel.Constants.ACC_SUPER,null);
	org.apache.bcel.generic.InstructionList list =
	    new org.apache.bcel.generic.InstructionList();
	list.append(new org.apache.bcel.generic.RETURN());
	sandmark.program.Method method =
	    new sandmark.program.LocalMethod
	    (clazz,org.apache.bcel.Constants.ACC_PUBLIC |
	     org.apache.bcel.Constants.ACC_STATIC |
	     org.apache.bcel.Constants.ACC_SUPER,
	     org.apache.bcel.generic.Type.VOID,
	     new org.apache.bcel.generic.Type[] { 
		 org.apache.bcel.generic.Type.getType
		 ("[Ljava/lang/String;")},null,"main",list);
	TraceNode node = new TraceNode(null,null); //XXXash need a valid line
	//new WMCodeGen(app,new TraceNode[] {node}).insert("011001");
	System.out.println(method.getInstructionList());
    }
    protected int findLiveInt() {
	sandmark.analysis.defuse.ReachingDefs rd = 
	    new sandmark.analysis.defuse.ReachingDefs(mMethod);
	sandmark.analysis.liveness.Liveness lv =
	    new sandmark.analysis.liveness.Liveness(mMethod);
	sandmark.analysis.defuse.DUWeb webs[] = rd.defUseWebs();
	for(int i = 0 ; i < webs.length ; i++) {
	    if(!lv.liveAt(webs[i],mIH))
		continue;
	    boolean allIntTypes = true;
	    for(java.util.Iterator defs = webs[i].defs().iterator() ; 
		allIntTypes && defs.hasNext() ;) {
		sandmark.analysis.defuse.DefWrapper dw =
		    (sandmark.analysis.defuse.DefWrapper)defs.next();
		if(!dw.getType().equals(org.apache.bcel.generic.Type.INT))
		    allIntTypes = false;
	    }
	    if(allIntTypes)
		return webs[i].getIndex();
	}
	return -1;
    }
}
