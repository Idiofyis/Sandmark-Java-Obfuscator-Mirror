package sandmark.analysis.defuse;

public class ThisDefWrapper extends DefWrapper {
    public ThisDefWrapper() {
	super(0,org.apache.bcel.generic.Type.OBJECT);
    }
    public String toString() {
	return "this at LV 0";
    }
    public int hashCode() {
	return toString().hashCode();
    }
    public boolean equals(Object o) {
	return o instanceof ThisDefWrapper;
    }
    protected void setIndex(int index) { throw new UnsupportedOperationException(); }
    public boolean generatedByStart() { return true; }
}
