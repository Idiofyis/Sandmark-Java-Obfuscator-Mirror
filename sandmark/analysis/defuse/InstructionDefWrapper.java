package sandmark.analysis.defuse;

public class InstructionDefWrapper extends DefWrapper {
    private org.apache.bcel.generic.InstructionHandle ih;
    public InstructionDefWrapper(org.apache.bcel.generic.InstructionHandle ih,
				 org.apache.bcel.generic.Type type) {
	super(((org.apache.bcel.generic.IndexedInstruction)
	       ih.getInstruction()).getIndex(),type);
	this.ih = ih;
    }
    public org.apache.bcel.generic.InstructionHandle getIH() {
	return ih;
    }
    public String toString() {
	return getIH() + " at LV " + getIndex();
    }
    public int hashCode() {
	return ih.hashCode();
    }
    public boolean equals(Object o) {
	if(!(o instanceof InstructionDefWrapper))
	    return false;
	InstructionDefWrapper other = (InstructionDefWrapper)o;
	return other.ih.equals(ih) && other.getIndex() == getIndex();
    }
    protected void setIndex(int index) {
	org.apache.bcel.generic.IndexedInstruction instr = 
	    (org.apache.bcel.generic.IndexedInstruction)
	    getIH().getInstruction();
	instr.setIndex(index);
	super.setIndex(index);
    }
    public boolean generatedByStart() { return false; }
}
