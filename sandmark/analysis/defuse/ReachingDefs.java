package sandmark.analysis.defuse;

public class ReachingDefs {

    class CachedDef {
	org.apache.bcel.generic.InstructionHandle ih;
	java.util.BitSet defs;
	CachedDef(org.apache.bcel.generic.InstructionHandle i,
		  java.util.BitSet bs) { ih = i ; defs = bs; }
    }

   private boolean mFindUninitializedVars;
    private boolean DEBUG = false;
    private boolean HACK_AROUND_JVM_VERIFICATION_BUG = false;
    private java.util.Hashtable mDefToInt;
   private java.util.Hashtable mBBToInfo;
   private java.util.Set mUses;
   private sandmark.analysis.controlflowgraph.MethodCFG mCFG;
    private CachedDef mCachedDef;

    public ReachingDefs(sandmark.program.Method method) {
	this(method,false);
    }
    public ReachingDefs(sandmark.program.Method method,
			boolean findUninitialized) {
       mFindUninitializedVars = findUninitialized;
	if(DEBUG) {
	    System.out.println("computing for " + method.getName());
	    System.out.println(method.getInstructionList());
	}

	mCFG = method.getCFG();
	mCFG.consolidate();

        mUses = findUses();

        if(method.getInstructionList() == null)
           throw new sandmark.analysis.controlflowgraph.EmptyMethodException();

	mDefToInt = findAndNumberDefs(findUninitialized);
	mBBToInfo = compute();
    }
   public boolean findUninitializedVars() { return mFindUninitializedVars; }
   public static boolean isUse(org.apache.bcel.generic.InstructionHandle ih) {
      org.apache.bcel.generic.Instruction instr = ih.getInstruction();
      return instr instanceof org.apache.bcel.generic.LoadInstruction ||
         instr instanceof org.apache.bcel.generic.IINC ||
         instr instanceof org.apache.bcel.generic.RET;
   }
   public static boolean isDef(org.apache.bcel.generic.InstructionHandle ih) {
      org.apache.bcel.generic.Instruction instr = ih.getInstruction();
      return instr instanceof org.apache.bcel.generic.StoreInstruction ||
         instr instanceof org.apache.bcel.generic.IINC;
   }
    public java.util.Set uses() {
       return new java.util.HashSet(mUses);
    }
    public java.util.Set uses(DefWrapper def) {
        int ndx = ((Integer)mDefToInt.get(def)).intValue();
        int defCount = mDefToInt.keySet().size();
        java.util.HashSet hs = new java.util.HashSet();
        for(java.util.Iterator it = uses().iterator() ; it.hasNext() ; ) {
            org.apache.bcel.generic.InstructionHandle use = 
               (org.apache.bcel.generic.InstructionHandle)it.next();
            sandmark.analysis.controlflowgraph.BasicBlock bb =
               mCFG.getBlock(use);
            BBInfo info = (BBInfo)mBBToInfo.get(bb);
            BBInfo pbi = calcPartialBlockInfo(bb,use,null);
            java.util.BitSet rds = new java.util.BitSet(defCount);
            for(java.util.Iterator preds = mCFG.preds(bb) ; preds.hasNext() ; )
               rds.or(((BBInfo)mBBToInfo.get(preds.next())).out);
            rds.andNot(pbi.kill);
            rds.or(pbi.gen);
            if(rds.get(ndx) && 
               def.getIndex() == 
               ((org.apache.bcel.generic.IndexedInstruction)
                use.getInstruction()).getIndex())
                hs.add(use);
        }
        return hs;
    }
    public java.util.Set defs() {
       return new java.util.HashSet(mDefToInt.keySet());
    }
    public java.util.Set defs(org.apache.bcel.generic.InstructionHandle use) {
       return defs(((org.apache.bcel.generic.IndexedInstruction)
                    use.getInstruction()).getIndex(),use);
    }
    public java.util.Set defs
       (int lvnum,org.apache.bcel.generic.InstructionHandle ih) {
	java.util.BitSet in;

	if(mCachedDef != null && mCachedDef.ih == ih)
	    in = mCachedDef.defs;
	else {
	    in = new java.util.BitSet(mDefToInt.keySet().size());
	    sandmark.analysis.controlflowgraph.BasicBlock bb = mCFG.getBlock(ih);
	    for(java.util.Iterator preds = mCFG.preds(bb) ; preds.hasNext() ; )
		in.or(((BBInfo)mBBToInfo.get(preds.next())).out);
	    BBInfo pbi = calcPartialBlockInfo(bb,ih,null);
	    in.andNot(pbi.kill);
	    in.or(pbi.gen);
	    mCachedDef = new CachedDef(ih,in);
	}

        return bitsToDefs(in,lvnum);
    }
   public java.util.HashSet bitsToDefs(java.util.BitSet bits,int lvnum) {
      java.util.HashSet hs = new java.util.HashSet();
      for(java.util.Iterator it = mDefToInt.keySet().iterator() ; 
          it.hasNext() ; ) {
         DefWrapper def = (DefWrapper)it.next();
         int defNum = ((Integer)mDefToInt.get(def)).intValue();
         if(def.getIndex() == lvnum && bits.get(defNum))
            hs.add(def);
	}
      
      return hs;
   }
    public DUWeb [] defUseWebs() {
	if(DEBUG)
	    System.out.println("calc'ing du webs");

	java.util.ArrayList webs = new java.util.ArrayList();
        java.util.Set defSets[] = collectDefsByLVIndex();
        for(java.util.Iterator blocks = mCFG.nodes() ; blocks.hasNext() ; ) {
           sandmark.analysis.controlflowgraph.BasicBlock bb =
              (sandmark.analysis.controlflowgraph.BasicBlock)blocks.next();
           java.util.BitSet in = new java.util.BitSet(mDefToInt.keySet().size());
           for(java.util.Iterator preds = mCFG.preds(bb) ; preds.hasNext() ; )
              in.or(((BBInfo)mBBToInfo.get(preds.next())).out);
           java.util.Hashtable useInfo = new java.util.Hashtable();
           calcPartialBlockInfo(bb,null,defSets,useInfo);
           for(java.util.Iterator uses = useInfo.keySet().iterator() ; 
               uses.hasNext() ; ) {
              org.apache.bcel.generic.InstructionHandle ih =
                 (org.apache.bcel.generic.InstructionHandle)uses.next();
              UseInfo info = (UseInfo)useInfo.get(ih);
              if(info.blockEffectOnly) {
                 info.gen.or(in);
                 info.gen.andNot(info.kill);
                 info.blockEffectOnly = false;
              }
              java.util.Set defs = bitsToDefs
                 (info.gen,((org.apache.bcel.generic.IndexedInstruction)
                            ih.getInstruction()).getIndex());
              DUWeb web = new DUWeb();
              web.addUse(ih);
              for(java.util.Iterator it = defs.iterator() ; it.hasNext() ; ) {
                 DefWrapper def = (DefWrapper)it.next();
                 web.addDef(def);
                 web.addEdge(def,ih);
              }
              webs.add(web);
           }
        }
	
	for(java.util.Iterator defs = defs().iterator() ; defs.hasNext() ; ) {
	    DefWrapper def = (DefWrapper)defs.next();
	    DUWeb mergedWeb = new DUWeb();
	    for(java.util.Iterator webIt = webs.iterator() ; 
		webIt.hasNext() ; ) {
		DUWeb web = (DUWeb)webIt.next();
		if(web.hasNode(def)) {
		    merge(mergedWeb,web);
		    webIt.remove();
		} else if(def instanceof InstructionDefWrapper &&
			  web.hasNode(((InstructionDefWrapper)def).getIH())) {
		    merge(mergedWeb,web);
		    webIt.remove();
		}
	    }
	    if(!mergedWeb.hasNode(def))
		mergedWeb.addDef(def);
	    webs.add(mergedWeb);
	}

	if(DEBUG)
	    System.out.println("webs: " + webs);
        return (DUWeb [])webs.toArray(new DUWeb[0]);
    }
   private java.util.Set findUses() {
      java.util.Set uses = new java.util.HashSet();
      for(org.apache.bcel.generic.InstructionHandle ih =
             mCFG.method().getInstructionList().getStart() ; ih != null ; 
          ih = ih.getNext())
         if(isUse(ih))
            uses.add(ih);
      return uses;
   }
    private java.util.Hashtable findAndNumberDefs(boolean findUninitialized) {
	java.util.Hashtable defToInt = new java.util.Hashtable();
	int nextInt = 0;
	if(!mCFG.method().isStatic())
	    defToInt.put(new ThisDefWrapper(),
			 new Integer(nextInt++));
	int firstUninitializedLocal = mCFG.method().isStatic() ? 0 : 1;
	for(int i = 0 ; i < mCFG.method().getArgumentTypes().length ; 
	    firstUninitializedLocal += 
		mCFG.method().getArgumentType(i).getSize(),i++)
	    defToInt.put
		(new ParamDefWrapper
		 (i,firstUninitializedLocal,mCFG.method().getArgumentType(i)),
		 new Integer(nextInt++));

	if(findUninitialized)
	    for(int maxLocals = mCFG.method().getMaxLocals() ; 
		firstUninitializedLocal < maxLocals ; firstUninitializedLocal++)
		defToInt.put
		    (new UninitializedDefWrapper(firstUninitializedLocal),
		     new Integer(nextInt++));

        for(org.apache.bcel.generic.InstructionHandle ih = 
               mCFG.method().getInstructionList().getStart() ; ih != null ;
            ih = ih.getNext()) {
	    if(ih.getInstruction() instanceof 
	       org.apache.bcel.generic.StoreInstruction)
		defToInt.put(new StoreDefWrapper
		     (ih,((org.apache.bcel.generic.TypedInstruction)
		       ih.getInstruction()).getType
		      (mCFG.method().getConstantPool())),new Integer(nextInt++));
	    else if(ih.getInstruction() instanceof org.apache.bcel.generic.IINC)
		defToInt.put(new IncDefWrapper(ih),new Integer(nextInt++));
            else if(isDef(ih))
               throw new RuntimeException("unkown def");
	}
	if(DEBUG)
	    System.out.println("defs: " + defToInt);
	return defToInt;
    }
    private java.util.Hashtable compute() {
       java.util.Set defSets[] = collectDefsByLVIndex();

       java.util.Hashtable bbToInfo = initializeBBInfo(mCFG.nodes(),defSets);
       int defCount = mDefToInt.keySet().size();

       java.util.Hashtable blockToPreds = new java.util.Hashtable();
       for(java.util.Iterator nodes = mCFG.nodes() ; nodes.hasNext() ; ) {
	   java.lang.Object node = nodes.next();
	   java.util.HashSet predSet = new java.util.HashSet();
	   for(java.util.Iterator preds = mCFG.preds(node) ; 
	       preds.hasNext() ; )
	       predSet.add(preds.next());
	   blockToPreds.put(node,predSet);
       }

	for(boolean progress = true ; progress ; ) {
	    progress = false;
	    for(java.util.Iterator it = mCFG.nodes() ; it.hasNext() ; ) {
		sandmark.analysis.controlflowgraph.BasicBlock bb =
		    (sandmark.analysis.controlflowgraph.BasicBlock)it.next();
		BBInfo info = (BBInfo)bbToInfo.get(bb);
		java.util.BitSet bs = 
		    new java.util.BitSet(defCount);
		for(java.util.Iterator preds = 
			((java.util.HashSet)blockToPreds.get(bb)).iterator() ; 
		    preds.hasNext() ; )
		    bs.or(((BBInfo)bbToInfo.get(preds.next())).out);
		if(!HACK_AROUND_JVM_VERIFICATION_BUG)
		    bs.andNot(info.kill);
		bs.or(info.gen);
		if(!bs.equals(info.out)) {
		    progress = true;
		    info.out = bs;
		}
	    }
	}

        return bbToInfo;
    }
   private BBInfo calcPartialBlockInfo
      (sandmark.analysis.controlflowgraph.BasicBlock bb,
       org.apache.bcel.generic.InstructionHandle lastIH,
       java.util.Set defSets[]) {
      return calcPartialBlockInfo(bb,lastIH,defSets,null);
   }
   private BBInfo calcPartialBlockInfo
      (sandmark.analysis.controlflowgraph.BasicBlock bb,
       org.apache.bcel.generic.InstructionHandle lastIH,
       java.util.Set defSets[],java.util.Hashtable useInfo) {
      if(defSets == null)
         defSets = collectDefsByLVIndex();

      java.util.BitSet gen = new java.util.BitSet
         (mDefToInt.keySet().size());
      java.util.BitSet kill = new java.util.BitSet
         (mDefToInt.keySet().size());
      if(bb == bb.graph().source()) {
         for(java.util.Iterator it = mDefToInt.keySet().iterator() ;
             it.hasNext() ; ) {
            DefWrapper o = (DefWrapper)it.next();
            if(o.generatedByStart())
               gen.set(((Integer)mDefToInt.get(o)).intValue());
         }
      }

      UseInfo lastUseInfo = null;
      for(java.util.Iterator ihIt = bb.getInstList().iterator() ; 
          ihIt.hasNext() ; ) {
         org.apache.bcel.generic.InstructionHandle ih = 
            (org.apache.bcel.generic.InstructionHandle)ihIt.next();

         if(useInfo != null && isUse(ih)) {
            if(lastUseInfo == null)
               lastUseInfo = new UseInfo((java.util.BitSet)gen.clone(),
                                         (java.util.BitSet)kill.clone());
            useInfo.put(ih,lastUseInfo);
         }

         if(ih == lastIH)
            break;

         if(!isDef(ih))
            continue;

         lastUseInfo = null;

         Integer ndx = (Integer)mDefToInt.get
            (new InstructionDefWrapper
             (ih,((org.apache.bcel.generic.TypedInstruction)
                  ih.getInstruction()).getType(mCFG.method().getConstantPool())));
         int lvindex = 
            ((org.apache.bcel.generic.IndexedInstruction)
             ih.getInstruction()).getIndex();
         //System.out.println("finding defset for " + ih + " with index " + lvindex);
         if(!HACK_AROUND_JVM_VERIFICATION_BUG) {
            for(java.util.Iterator killedIt = defSets[lvindex].iterator() ; 
                killedIt.hasNext() ; ) {
               Object o = killedIt.next();
               kill.set(((Integer)mDefToInt.get(o)).intValue());
               gen.clear(((Integer)mDefToInt.get(o)).intValue());
            }
            kill.clear(ndx.intValue());
         }
         gen.set(ndx.intValue());		
      }

      return new BBInfo(gen,kill);
   }
   private java.util.Hashtable initializeBBInfo
      (java.util.Iterator basicBlocks,java.util.Set defSets[]) {
	java.util.Hashtable bbToInfo = new java.util.Hashtable();

	if(DEBUG)
	    for(int i = 0 ; i < defSets.length ; i++)
		System.out.println(i + " : " + defSets[i]);

	while(basicBlocks.hasNext()) {
	    sandmark.analysis.controlflowgraph.BasicBlock bb = 
		(sandmark.analysis.controlflowgraph.BasicBlock)basicBlocks.next();
	    bbToInfo.put(bb,calcPartialBlockInfo(bb,null,defSets));
	}
	
	return bbToInfo;
    }
    private java.util.Set [] collectDefsByLVIndex() {
	java.util.Vector sets = new java.util.Vector();
	for(java.util.Iterator it = mDefToInt.keySet().iterator() ; it.hasNext() ; ) {
           DefWrapper def = (DefWrapper)it.next();
	    int ndx = def.getIndex();
	    while(sets.size() <= ndx)
		sets.add(new java.util.HashSet());
	    java.util.Set set = 
		(java.util.Set)sets.get(ndx);
	    set.add(def);
	    //System.out.println("adding " + ih + " to " + set + " for lv " + si.getIndex());
	}
	return (java.util.Set [])sets.toArray(new java.util.Set[0]);
    }
    private void merge(DUWeb gr,DUWeb subGr) {
	if(gr == subGr) {
	    if(DEBUG)
		System.out.println("merging with self");
	    return;
	}
	for(java.util.Iterator it = subGr.defs().iterator() ; it.hasNext() ; ) {
	    DefWrapper def = (DefWrapper)it.next();
	    gr.addDef(def);
	}
        for(java.util.Iterator it = subGr.uses().iterator() ; it.hasNext() ; ) {
	    org.apache.bcel.generic.InstructionHandle ih = 
		(org.apache.bcel.generic.InstructionHandle)it.next();
	    gr.addUse(ih);
	}
        for(java.util.Iterator it = subGr.edges() ; it.hasNext() ; ) {
            sandmark.util.newgraph.Edge e = 
                (sandmark.util.newgraph.Edge)it.next();
	    if(!gr.hasEdge(e.sourceNode(),e.sinkNode()))
		gr.addEdge(e.sourceNode(),e.sinkNode());
        }
    }        

    public static void main(String argv[]) throws Exception {
        System.err.println(argv[0]);
	sandmark.program.Application app = new sandmark.program.Application(argv[0]);
	for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
	    for(java.util.Iterator methods = 
		    ((sandmark.program.Class)classes.next()).methods() ; 
		methods.hasNext() ; ) {
                sandmark.program.Method method = 
                    (sandmark.program.Method)methods.next();
                if(method.getInstructionList() == null)
                    continue;
		System.out.println("computing for " + 
				   method.getEnclosingClass().getName() + "." +
				   method.getName());
		ReachingDefs rd = 
		    new ReachingDefs(method);
		java.util.HashSet hs = new java.util.HashSet();
		for(java.util.Iterator uses = rd.uses().iterator() ; 
		    uses.hasNext() ; ) {
		    org.apache.bcel.generic.InstructionHandle ih = 
			(org.apache.bcel.generic.InstructionHandle)uses.next();
		    System.out.println(ih + " is defined by:");
		    hs.addAll(rd.defs(ih));
		    for(java.util.Iterator rds = rd.defs(ih).iterator() ; rds.hasNext() ; )
			System.out.println(rds.next());
		}
		if(!method.isStatic()) {
		    System.out.println("uses of this: " + 
				       rd.uses(new ThisDefWrapper()));
		    if(rd.uses(new ThisDefWrapper()).size() == 0 &&
		       rd.uses().size() != 0)
			System.out.println("missing 'this' uses");
		}
		java.util.Set unusedDefs = rd.defs();
		unusedDefs.removeAll(hs);
		if(unusedDefs.size() > 0)
		    System.out.println("unused defs: " + unusedDefs);
		DUWeb webs[] = rd.defUseWebs();
		for(int i = 0 ; i < webs.length ; i++)
		    System.out.println(webs[i]);
		if(webs.length == 0 && rd.uses().size() != 0)
		    throw new RuntimeException();
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

class UseInfo {;
   java.util.BitSet kill;
   java.util.BitSet gen;
   boolean blockEffectOnly = true;
    UseInfo(java.util.BitSet gen,java.util.BitSet kill) {
       this.gen = gen;
       this.kill = kill;
    }
}
