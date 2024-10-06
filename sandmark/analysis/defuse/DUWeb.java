package sandmark.analysis.defuse;

public class DUWeb extends sandmark.util.newgraph.MutableGraph
    implements Comparable,sandmark.util.newgraph.LabeledNode {
    private java.util.Set mDefs;
    private java.util.Set mUses;
    public DUWeb() {
	mDefs = new java.util.HashSet();
	mUses = new java.util.HashSet();
    }
    public void addUse(org.apache.bcel.generic.InstructionHandle use) {
	mUses.add(use);
	if(!hasNode(use))
	    super.addNode(use);
    }
    public void addDef(DefWrapper def) {
	mDefs.add(def);
	if(!hasNode(def))
	    super.addNode(def);
    }
    public void addNode(Object o) {
	throw new UnsupportedOperationException();
    }
    public void removeNode(Object o) {
	throw new UnsupportedOperationException();
    }
    public java.util.Set defs() {
	return mDefs;
    }
    public java.util.Set uses() {
	return mUses;
    }
    public int compareTo(Object o) {
	if(!(o instanceof DUWeb))
	    throw new RuntimeException("comparing DUWeb to " + 
				       o.getClass().getName());

	DUWeb other = (DUWeb)o;

        java.util.ArrayList myUses = new java.util.ArrayList();
        java.util.ArrayList otherUses = new java.util.ArrayList();
        for(java.util.Iterator uses = uses().iterator() ; uses.hasNext() ; )
           myUses.add(uses.next());
        for(java.util.Iterator uses = other.uses().iterator() ; uses.hasNext() ; )
           otherUses.add(uses.next());

        IHComparator ihc = new IHComparator();
        java.util.Collections.sort(myUses,ihc);
        java.util.Collections.sort(otherUses,ihc);

        for(int i = 0,bound = myUses.size() > otherUses.size() ? 
               otherUses.size() : myUses.size(),rv ; i < bound ; i++)
           if((rv = ihc.compare(myUses.get(i),otherUses.get(i))) != 0)
              return rv;

	if(uses().size() > other.uses().size())
	    return 1;
	else if(other.uses().size() > uses().size())
	    return -1;

        java.util.ArrayList myDefs = new java.util.ArrayList();
        java.util.ArrayList otherDefs = new java.util.ArrayList();
        for(java.util.Iterator defs = defs().iterator() ; defs.hasNext() ; )
           myDefs.add(defs.next());
        for(java.util.Iterator defs = other.defs().iterator() ; defs.hasNext() ; )
           otherDefs.add(defs.next());

        DefComparator dc = new DefComparator();
        java.util.Collections.sort(myDefs,dc);
        java.util.Collections.sort(otherDefs,dc);

        for(int i = 0,bound = myDefs.size() > otherDefs.size() ? 
               otherDefs.size() : myDefs.size(),rv ; i < bound ; i++)
           if((rv = dc.compare(myDefs.get(i),otherDefs.get(i))) != 0)
              return rv;

	if(defs().size() > other.defs().size())
	    return 1;
	else if(other.defs().size() > defs().size())
	    return -1;
	
	return 0;
    }
    public void setIndex(int index) {
	for(java.util.Iterator it = defs().iterator() ; it.hasNext() ; ) {
	    sandmark.analysis.defuse.DefWrapper def =
		(sandmark.analysis.defuse.DefWrapper)it.next();
	    def.setIndex(index);
	}
        for(java.util.Iterator it = uses().iterator() ; it.hasNext() ; ) {
            org.apache.bcel.generic.InstructionHandle ih =
                (org.apache.bcel.generic.InstructionHandle)it.next();
            org.apache.bcel.generic.IndexedInstruction lvi =
                (org.apache.bcel.generic.IndexedInstruction)
                ih.getInstruction();
            lvi.setIndex(index);
        }
    }
    public int getIndex() {
	return defs().size() > 0 ? 
	    ((DefWrapper)defs().iterator().next()).getIndex() :
	    ((org.apache.bcel.generic.IndexedInstruction)
	     ((org.apache.bcel.generic.InstructionHandle)
	      uses().iterator().next()).getInstruction()).getIndex();
    }

    public org.apache.bcel.generic.Type getType() {
	if(mDefs.size() != 0)
	    return ((DefWrapper)mDefs.iterator().next()).getType();
	if(mUses.size() == 0)
	    return null;
	org.apache.bcel.generic.Instruction instr = 
	    ((org.apache.bcel.generic.InstructionHandle)
	     mUses.iterator().next()).getInstruction();
	if(instr instanceof org.apache.bcel.generic.ALOAD)
	    return org.apache.bcel.generic.Type.OBJECT;
	if(instr instanceof org.apache.bcel.generic.ILOAD)
	    return org.apache.bcel.generic.Type.INT;
	if(instr instanceof org.apache.bcel.generic.LLOAD)
	    return org.apache.bcel.generic.Type.LONG;
	if(instr instanceof org.apache.bcel.generic.DLOAD)
	    return org.apache.bcel.generic.Type.DOUBLE;
	if(instr instanceof org.apache.bcel.generic.FLOAD)
	    return org.apache.bcel.generic.Type.FLOAT;
	if(instr instanceof org.apache.bcel.generic.IINC)
	    return org.apache.bcel.generic.Type.INT;
	if(instr instanceof org.apache.bcel.generic.RET)
	    return org.apache.bcel.generic.Type.OBJECT;
	throw new RuntimeException("Shouldn't be here");
    }

    public String toString() {
        String s = "defs:\n";
        for(java.util.Iterator it = defs().iterator() ; it.hasNext() ; )
            s += "   " + it.next() + "\n";
        s += "uses:\n";
        for(java.util.Iterator it = uses().iterator() ; it.hasNext() ; )
            s += "   " + it.next() + "\n";
        return s;
    }
    public String getLongLabel() { return toString(); }
    public String getShortLabel() { return "Register " + getIndex(); }
}

class IHComparator implements java.util.Comparator {
   public int compare(Object o1,Object o2) {
      if(!(o1 instanceof org.apache.bcel.generic.InstructionHandle &&
           o2 instanceof org.apache.bcel.generic.InstructionHandle))
         throw new RuntimeException("can't compare non IH's " + 
                                    o1.getClass().getName() + " " + 
                                    o2.getClass().getName());

      org.apache.bcel.generic.InstructionHandle ih1 =
         (org.apache.bcel.generic.InstructionHandle)o1;
      org.apache.bcel.generic.InstructionHandle ih2 =
         (org.apache.bcel.generic.InstructionHandle)o2;

      return ih1.getPosition() - ih2.getPosition();
   }
   public boolean equals(Object o) { return o.getClass() == getClass(); }
}

class DefComparator implements java.util.Comparator {
   public int compare(Object o1,Object o2) {
      if(!(o1 instanceof DefWrapper && o2 instanceof DefWrapper))
         throw new RuntimeException("can't compare non-DefWrapper's");

      sandmark.analysis.defuse.DefWrapper dw1 =
         (sandmark.analysis.defuse.DefWrapper)o1;
      sandmark.analysis.defuse.DefWrapper dw2 =
         (sandmark.analysis.defuse.DefWrapper)o2;

      if(dw1 instanceof ThisDefWrapper && !(dw2 instanceof ThisDefWrapper))
         return 1;
      if(dw2 instanceof ThisDefWrapper && !(dw1 instanceof ThisDefWrapper))
         return -1;
      if(dw1 instanceof ThisDefWrapper)
         return 0;

      if(dw1 instanceof ParamDefWrapper && !(dw2 instanceof ParamDefWrapper))
         return 1;
      if(dw2 instanceof ParamDefWrapper && !(dw1 instanceof ParamDefWrapper))
         return -1;
      if(dw1 instanceof ParamDefWrapper)
         return dw1.getIndex() - dw2.getIndex();

      return ((InstructionDefWrapper)dw1).getIH().getPosition() - 
         ((InstructionDefWrapper)dw2).getIH().getPosition();
   }
   public boolean equals(Object o) {
      return o.getClass() == getClass();
   }
}
