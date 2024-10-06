package sandmark.analysis.liveness;

public class Liveness {
    private static boolean HACK_AROUND_JVM_VERIFICATION_BUG = false;
    private java.util.Hashtable mBBToInfo;
    private java.util.Hashtable mUseToInt;
    private sandmark.analysis.controlflowgraph.MethodCFG mCFG;
    public Liveness(sandmark.program.Method method) {
        java.util.Set uses = findUses(method.getInstructionList());
        mCFG = method.getCFG();
        mCFG.consolidate();
        mUseToInt = numberUses(uses);
        mBBToInfo = initializeBlocks(uses);
        compute();
    }
    public boolean liveAt(sandmark.analysis.defuse.DUWeb web,
                          org.apache.bcel.generic.InstructionHandle ih) {
        sandmark.analysis.controlflowgraph.BasicBlock bb = mCFG.getBlock(ih);
        java.util.Set usesByIndex[] = collectUsesByIndex(mUseToInt.keySet());
        BBInfo partialInfo = calcPartialBlockInfo(bb,ih,usesByIndex);
        java.util.BitSet liveAtIH =
            new java.util.BitSet(mUseToInt.keySet().size());
        for(java.util.Iterator it = mCFG.succs(bb) ; it.hasNext() ; )
            liveAtIH.or(((BBInfo)mBBToInfo.get(it.next())).out);
        if(!HACK_AROUND_JVM_VERIFICATION_BUG)
            liveAtIH.andNot(partialInfo.kill);
        liveAtIH.or(partialInfo.gen);
        java.util.BitSet webUseMask =
            new java.util.BitSet(mUseToInt.keySet().size());
        for(java.util.Iterator it = web.uses().iterator() ; it.hasNext() ; )
            webUseMask.set(((Integer)mUseToInt.get(it.next())).intValue());
        liveAtIH.and(webUseMask);
        return liveAtIH.cardinality() != 0;
    }
    private java.util.Set findUses(org.apache.bcel.generic.InstructionList il) {
        java.util.HashSet uses = new java.util.HashSet();

        org.apache.bcel.generic.InstructionHandle ihs[] =
            il.getInstructionHandles();
        for(int i = 0 ; i < ihs.length ; i++)
            if(ihs[i].getInstruction() instanceof
               org.apache.bcel.generic.LoadInstruction ||
               ihs[i].getInstruction() instanceof
               org.apache.bcel.generic.IINC ||
               ihs[i].getInstruction() instanceof
               org.apache.bcel.generic.RET)
                uses.add(ihs[i]);

        return uses;
    }
    private void compute() {
        for(boolean progress = true ; progress ; ) {
            progress = false;
            for(java.util.Iterator it = mCFG.nodes() ; it.hasNext() ; ) {
                Object node = it.next();
                BBInfo info = (BBInfo)mBBToInfo.get(node);
                java.util.BitSet bs =
                    new java.util.BitSet(mBBToInfo.keySet().size());
                for(java.util.Iterator succs = mCFG.succs(node) ;
                    succs.hasNext() ; )
                    bs.or(((BBInfo)mBBToInfo.get(succs.next())).out);
                if(!HACK_AROUND_JVM_VERIFICATION_BUG)
                    bs.andNot(info.kill);
                bs.or(info.gen);
                if(!bs.equals(info.out)) {
                    progress = true;
                    info.out = bs;
                }
            }
        }
    }
    private BBInfo calcPartialBlockInfo
        (sandmark.analysis.controlflowgraph.BasicBlock bb,
         org.apache.bcel.generic.InstructionHandle lastIH,
         java.util.Set usesByIndex[]) {
        int usecount = mUseToInt.keySet().size();
        java.util.BitSet gen = new java.util.BitSet(usecount);
        java.util.BitSet kill = new java.util.BitSet(usecount);
        java.util.ArrayList al = bb.getInstList();
        for(int i = al.size() - 1 ; i >= 0 ; i--) {
            org.apache.bcel.generic.InstructionHandle ih =
                (org.apache.bcel.generic.InstructionHandle)al.get(i);
            if(sandmark.analysis.defuse.ReachingDefs.isDef(ih)) {
                int lvindex =
                    ((org.apache.bcel.generic.IndexedInstruction)
                     ih.getInstruction()).getIndex();
                if(usesByIndex.length > lvindex &&
                   usesByIndex[lvindex] != null)
                    if(!HACK_AROUND_JVM_VERIFICATION_BUG)
                        for(java.util.Iterator useIt = usesByIndex[lvindex].iterator() ;
                            useIt.hasNext() ; ) {
                            int ndx =
                                ((Integer)mUseToInt.get(useIt.next())).intValue();
                            gen.clear(ndx);
                            kill.set(ndx);
                        }
            }
            if(sandmark.analysis.defuse.ReachingDefs.isUse(ih)) {
                int lvindex =
                    ((org.apache.bcel.generic.IndexedInstruction)
                     ih.getInstruction()).getIndex();
                Integer ndx = (Integer)mUseToInt.get(ih);
                if(ndx != null) {
                    gen.set(ndx.intValue());
                    kill.clear(ndx.intValue());
                }
            }
            if(ih == lastIH)
                break;
        }
        return new BBInfo(gen,kill);
    }
    private java.util.Hashtable numberUses(java.util.Set uses) {
        java.util.Hashtable useToInt = new java.util.Hashtable();
        int i = 0;
        for(java.util.Iterator it = uses.iterator() ; it.hasNext() ; )
            useToInt.put(it.next(),new Integer(i++));
        return useToInt;
    }
    private java.util.Hashtable initializeBlocks(java.util.Set uses) {
        java.util.Hashtable bbToInfo = new java.util.Hashtable();
        java.util.Set usesByIndex[] = collectUsesByIndex(uses);
        int usecount = mUseToInt.keySet().size();
        for(java.util.Iterator it = mCFG.nodes() ; it.hasNext() ; ) {
            sandmark.analysis.controlflowgraph.BasicBlock bb =
                (sandmark.analysis.controlflowgraph.BasicBlock)it.next();
            BBInfo info = calcPartialBlockInfo(bb,bb.getIH(),usesByIndex);
            bbToInfo.put(bb,info);
        }
        return bbToInfo;
    }
    private java.util.Set [] collectUsesByIndex(java.util.Set uses) {
        java.util.Vector vec = new java.util.Vector();
        for(java.util.Iterator it = uses.iterator() ; it.hasNext() ; ) {
            org.apache.bcel.generic.InstructionHandle ih =
                (org.apache.bcel.generic.InstructionHandle)it.next();
            org.apache.bcel.generic.IndexedInstruction li =
                (org.apache.bcel.generic.IndexedInstruction)ih.getInstruction();
            while(vec.size() <= li.getIndex())
                vec.add(new java.util.HashSet());
            java.util.Set set = (java.util.Set)vec.get(li.getIndex());
            set.add(ih);
        }
        return (java.util.Set [])vec.toArray(new java.util.Set[0]);
    }
    public static void main(String argv[]) throws Exception {
        sandmark.program.Application app =
            new sandmark.program.Application(argv[0]);
        for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
            sandmark.program.Class clazz =
                (sandmark.program.Class)classes.next();
            for(java.util.Iterator methods = clazz.methods() ;
                methods.hasNext() ; ) {
                sandmark.program.Method method =
                    (sandmark.program.Method)methods.next();
                if(method.getInstructionList() == null)
                    continue;
                sandmark.analysis.defuse.ReachingDefs rd =
                    new sandmark.analysis.defuse.ReachingDefs(method);
                Liveness l = new Liveness(method);
                for(java.util.Iterator defs = rd.defs().iterator() ;
                    defs.hasNext() ; ) {
                    sandmark.analysis.defuse.DefWrapper def =
                        (sandmark.analysis.defuse.DefWrapper)defs.next();
                    if(!(def instanceof sandmark.analysis.defuse.InstructionDefWrapper))
                        continue;
                    org.apache.bcel.generic.InstructionHandle ih =
                        ((sandmark.analysis.defuse.InstructionDefWrapper)def).getIH();
                    l.liveAt(rd.defUseWebs()[0],ih);
                }
            }
        }
    }
}

class BBInfo {
    java.util.BitSet out;
    java.util.BitSet gen;
    java.util.BitSet kill;
    BBInfo(java.util.BitSet gen,java.util.BitSet kill) {
        this.gen = gen;
        this.kill = kill;
        this.out = (java.util.BitSet)gen.clone();
    }
}
