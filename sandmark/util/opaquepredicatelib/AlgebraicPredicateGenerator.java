package sandmark.util.opaquepredicatelib;

public class AlgebraicPredicateGenerator extends OpaquePredicateGenerator {
    private static String trueExprs[] = {
	"(x*(x-1))%2 == 0",
    };
    private static String falseExprs[] = {
	"(x*(x-1))%2 != 0",
    };
    private static String unknownExprs[] = {
	"(x+y)%3 == 0",
    };
    private static final int MAX_VARS = 2; //keep in sync with above exprs
    private static PredicateInfo sInfo;
    public static PredicateInfo getInfo() {
	if(sInfo == null)
	    sInfo = new PredicateInfo
		(OpaqueManager.PT_ALGEBRAIC,
		 OpaqueManager.PV_TRUE | OpaqueManager.PV_FALSE |
		 OpaqueManager.PV_UNKNOWN);
	return sInfo;
    }

    public void insertPredicate
	(sandmark.program.Method method,
	 org.apache.bcel.generic.InstructionHandle ih,int valueType) {
	int liveInts[] = findLiveInts(method,ih);
	if(liveInts == null || liveInts.length == 0)
	    throw new Error("canInsertPredicate should have returned false");
	if((valueType & (valueType - 1)) != 0)
	   throw new Error("ambiguous insertion value");
	
	Object liveObjects[] = 
	    new Object[liveInts.length < MAX_VARS ? 
	               MAX_VARS : liveInts.length];
	for(int i = 0 ; i < liveInts.length ; i++)
	    liveObjects[i] = new Integer(liveInts[i]);
	for(int i = liveInts.length ; i < liveObjects.length ; i++)
	    liveObjects[i] = liveObjects[0];
	String valueExprs[];
	switch(valueType) {
	case OpaqueManager.PV_TRUE:
	   valueExprs = trueExprs;
	break;
	case OpaqueManager.PV_FALSE:
	   valueExprs = falseExprs;
	break;
	case OpaqueManager.PV_UNKNOWN:
	   valueExprs = unknownExprs;
	break;
	default:
	   throw new Error("Unknown value type");
	}
	int ndx = sandmark.util.Random.getRandom().nextInt() % 
	    valueExprs.length;
	if(ndx < 0)
	    ndx += valueExprs.length;
	ExprTree tree = ExprTree.parse(valueExprs[ndx]);
	java.util.List instrs[] = tree.getInstructionLists(method,liveObjects);
	org.apache.bcel.generic.InstructionList il =
	    new org.apache.bcel.generic.InstructionList();
	for(int i = 0 ; i < instrs.length ; i++) {
	    for(java.util.Iterator instrIt = instrs[i].iterator() ; 
		instrIt.hasNext() ; ) {
		org.apache.bcel.generic.Instruction instr = 
		    (org.apache.bcel.generic.Instruction)instrIt.next();
		il.append(instr);
	    }
	}
	
	ThreadPredicateGenerator.updateTargeters(ih,il.getStart());
	method.getInstructionList().insert(ih,il);
	method.setMaxLocals();
	method.setMaxStack();
	method.mark();	
    }
    public boolean canInsertPredicate
	(sandmark.program.Method method,
	 org.apache.bcel.generic.InstructionHandle ih,int valueType) {
	int liveInts[] = findLiveInts(method,ih);
	return liveInts.length != 0;
    }
    private int [] findLiveInts(sandmark.program.Method method,
				org.apache.bcel.generic.InstructionHandle ih) {
	sandmark.analysis.defuse.ReachingDefs rd = 
	    new sandmark.analysis.defuse.ReachingDefs(method);
	sandmark.analysis.liveness.Liveness lv =
	    new sandmark.analysis.liveness.Liveness(method);
	sandmark.analysis.defuse.DUWeb webs[] = rd.defUseWebs();
	java.util.Set liveInts = new java.util.HashSet();
	for(int i = 0 ; i < webs.length ; i++) {
	    if(!lv.liveAt(webs[i],ih))
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
		liveInts.add(new Integer(webs[i].getIndex()));
	}
	int li[] = new int[liveInts.size()];
	int i = 0;
	for(java.util.Iterator it = liveInts.iterator() ; it.hasNext() ; i++) {
	    Integer inte = (Integer)it.next();
	    li[i] = inte.intValue();
	}
	return li;
    }
}
