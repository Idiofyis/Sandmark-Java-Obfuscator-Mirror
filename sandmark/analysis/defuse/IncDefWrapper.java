package sandmark.analysis.defuse;

public class IncDefWrapper extends InstructionDefWrapper {
    public IncDefWrapper(org.apache.bcel.generic.InstructionHandle ih) {
	super(ih,org.apache.bcel.generic.Type.INT);
    }
    public org.apache.bcel.generic.IINC getIncInstruction() {
	return (org.apache.bcel.generic.IINC)getIH().getInstruction();
    }
}
