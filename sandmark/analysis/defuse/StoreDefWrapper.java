package sandmark.analysis.defuse;

public class StoreDefWrapper extends InstructionDefWrapper {
    public StoreDefWrapper(org.apache.bcel.generic.InstructionHandle ih,
			   org.apache.bcel.generic.Type type) {
	super(ih,type);
    }
}
