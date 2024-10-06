package sandmark.analysis.defuse;

public class UninitializedDefWrapper extends DefWrapper {
    public UninitializedDefWrapper(int index) {
	super(index,org.apache.bcel.generic.Type.VOID);
    }
    public boolean generatedByStart() { return true; }
}
