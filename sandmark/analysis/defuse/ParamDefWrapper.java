package sandmark.analysis.defuse;

public class ParamDefWrapper extends DefWrapper {
    private int paramListIndex;
    public ParamDefWrapper(int paramListIndex,int lvIndex,
			   org.apache.bcel.generic.Type type) {
	super(lvIndex,type);
	this.paramListIndex = paramListIndex;
    }
    public int getParamListIndex() {
	return paramListIndex;
    }
    public String toString() {
	return "param " + getParamListIndex() + " at LV " + getIndex();
    }
    public int hashCode() {
	return getParamListIndex() + getIndex() << 1;
    }
    public boolean equals(Object o) {
	if(!(o instanceof ParamDefWrapper))
	    return false;
	ParamDefWrapper other = (ParamDefWrapper)o;
	return other.getParamListIndex() == getParamListIndex() &&
	    other.getIndex() == getIndex();
    }
    protected void setIndex(int index) { throw new UnsupportedOperationException(); }
    public boolean generatedByStart() { return true; } 
}
