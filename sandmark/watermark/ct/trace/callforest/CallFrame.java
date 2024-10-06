package sandmark.watermark.ct.trace.callforest;

// This class is similar to sandmark.util.StackFrame, 
// except for how frames are compared for equality.
class CallFrame {
   public sandmark.util.MethodID method = null;
   public long threadID;
   public long frameID;

    public CallFrame (
       sandmark.util.MethodID method,
       long threadID,
       long frameID) {
	this.method = method;
	this.threadID = threadID;
	this.frameID = frameID;
    }

    public String toString() {
       return "CallFrame(" + method.toString() + "," + threadID + "," + frameID + ")";
    }

    public boolean equals(java.lang.Object o) {
	CallFrame c = (CallFrame) o;
        return c.method.equals(method) && (c.threadID==threadID) && (c.frameID==frameID);
    }

    public int hashCode() {
	return (int)(threadID + frameID) + method.hashCode();
    }
}


