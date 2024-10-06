package sandmark.analysis.defuse;

public abstract class DefWrapper {
    private int lvIndex;
    org.apache.bcel.generic.Type type;
    public DefWrapper(int lvIndex,org.apache.bcel.generic.Type type) {
	this.lvIndex = lvIndex;
	this.type = type;
    }
    public int getIndex() {
	return lvIndex;
    }
    protected void setIndex(int index) {
	lvIndex = index;
    }
    public int getWidth() { return type.getSize(); }
    public org.apache.bcel.generic.Type getType() { return type; }
    public abstract boolean generatedByStart();
}
