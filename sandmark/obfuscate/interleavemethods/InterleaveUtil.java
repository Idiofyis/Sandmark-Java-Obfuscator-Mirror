package sandmark.obfuscate.interleavemethods;

public class InterleaveUtil {
    private final static boolean DEBUG = false;
    public static int getSlot(sandmark.program.Method m){       
        m.setMaxLocals();
        return m.calcMaxLocals();
    }
    /**Return an LV index 'i' available to both input methods s.t. every 
       slot > i is unused in both methods. 
    */
    public static int getSlot(sandmark.program.Method A,
                              sandmark.program.Method B){        
        return Math.max(getSlot(A), getSlot(B));
    } 

    public static StackOp getStack(org.apache.bcel.generic.Instruction inst,
                                   org.apache.bcel.generic.ConstantPoolGen cpg){
        if(inst instanceof org.apache.bcel.generic.DUP)
            return new StackOp(1,2);
        else if(inst instanceof org.apache.bcel.generic.ATHROW)
            return new StackOp(1,1);
        else if(inst instanceof org.apache.bcel.generic.DUP_X1)
            return new StackOp(2,3);       
        
        int consumes = 0, produces = 0;
        if(inst instanceof org.apache.bcel.generic.StackConsumer)
            consumes = ((org.apache.bcel.generic.StackConsumer)inst).consumeStack(cpg);
        if(inst instanceof org.apache.bcel.generic.StackProducer)
            produces = ((org.apache.bcel.generic.StackProducer)inst).produceStack(cpg);
        return new StackOp(consumes, produces);
    }   

    /** A and B have same sig, so arguments in locals are of same type.
        This method re-numbers the locals s.t. types match across methods. In addition,
        it allows each LV index to store only one type.
    */
    public static void syncLocalVars(sandmark.program.Method A,
                                     sandmark.program.Method B){      
        makeLVsDistinct(A);
        makeLVsDistinct(B);
        incrementLVs(B, A.calcMaxLocals());
        A.mark();
        B.mark();
        if(DEBUG){
            System.out.println("B after LV sync:\n" + B.getInstructionList());
            System.out.println("A after LV sync:\n" + A.getInstructionList());
        }
    }

    //Don't let any index have more than one type assigned to it
    private static void makeLVsDistinct(sandmark.program.Method M) {
	sandmark.analysis.defuse.ReachingDefs rd = 
	    new sandmark.analysis.defuse.ReachingDefs(M);
	sandmark.analysis.defuse.DUWeb webs[] = rd.defUseWebs();
	int firstNonParam = getCount(M) + 1; //M doesn't yet have the byte argument that we will end up adding
	int nextAvailLocal = M.calcMaxLocals();
	if(nextAvailLocal <= firstNonParam)
	    nextAvailLocal = firstNonParam + 1;
	java.util.HashSet localsSeen = new java.util.HashSet();
	for(int i = 0 ; i < webs.length ; i++) {
	    boolean containsPeggedDef = false;
	    for(java.util.Iterator defs = webs[i].defs().iterator() ;
		!containsPeggedDef && defs.hasNext() ; )
		if(!(defs.next() instanceof 
		     sandmark.analysis.defuse.InstructionDefWrapper))
		    containsPeggedDef = true;
	    if(localsSeen.contains(new Integer(webs[i].getIndex())) ||
	       (webs[i].getType().getSize() == 2 && 
		localsSeen.contains(new Integer(webs[i].getIndex() + 1))) ||
	       (!containsPeggedDef && webs[i].getIndex() < firstNonParam)) {
		webs[i].setIndex(nextAvailLocal);
		nextAvailLocal += webs[i].getType().getSize();
	    }
	    localsSeen.add(new Integer(webs[i].getIndex()));
	    if(webs[i].getType().getSize() == 2)
		localsSeen.add(new Integer(webs[i].getIndex() + 1));
	}
    }
    //increment all non-arg lv's by incVal
    private static void incrementLVs(sandmark.program.Method M, int incVal){
        org.apache.bcel.generic.InstructionHandle ih =
            M.getInstructionList().getStart();
        int count = getCount(M);      
        while(ih != null){
            if(sandmark.analysis.defuse.ReachingDefs.isDef(ih) ||
	       sandmark.analysis.defuse.ReachingDefs.isUse(ih)) {
                org.apache.bcel.generic.IndexedInstruction lvi =
                    (org.apache.bcel.generic.IndexedInstruction)
		    ih.getInstruction();
                if(lvi.getIndex() >= count)
                    lvi.setIndex(incVal + lvi.getIndex());
            }
            ih = ih.getNext();
        }
        M.setMaxLocals();
    }

    private static int getLVindex
        (sandmark.program.Method M, int argIndex){
        int retVal = 0, ctr = 0;
        org.apache.bcel.generic.Type[] types = M.getArgumentTypes();       
        while(ctr != argIndex)
            retVal += types[ctr++].getSize();        
        if(!M.isStatic())
            retVal++;      
        return retVal;
    }
  
   /*The count variable for m is defined as "a measure of the 
    number of argument values, where an argument value of type 
    long or type double contributes two units to the count 
    value and an argument of any other type contributes one unit."*/    
    public static int getCount(sandmark.program.Method m){             
        int index = 0;
	org.apache.bcel.generic.Type args[] = m.getArgumentTypes();
        for(int i = 0; i < args.length; i++)
	    index += args[i].getSize();
        if(!m.isStatic())
            index++;
        return index;
    }
}


