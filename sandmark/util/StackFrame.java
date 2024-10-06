package sandmark.util;

public class StackFrame implements java.io.Serializable {
   public static final long MISSING_long = -1;

   private sandmark.util.ByteCodeLocation location = null;
   private long threadID = MISSING_long;
   private long frameID = MISSING_long;

    public StackFrame (
       sandmark.util.ByteCodeLocation location,
       long threadID) {
	this.location = location;
	this.threadID = threadID;
    }

    public StackFrame (
       sandmark.util.ByteCodeLocation location,
       long threadID,
       long frameID) {
	this.location = location;
	this.threadID = threadID;
	this.frameID = frameID;
    }
    
    public sandmark.util.ByteCodeLocation getLocation() { return location; }
    public long getFrameID() { return frameID; }
    public long getThreadID() { return threadID; }

    public String toString() {
       return "FRAME[" + location.toString() + ", THRD=" + threadID + ", ID=" + frameID + "]";
    }
    
    public boolean equals(java.lang.Object o) {
	StackFrame c = (StackFrame) o;
        return (c.location.equals(location) &&
               (c.threadID==threadID) &&
               (c.frameID==frameID));
    }

    public int hashCode() {
	return (int)(threadID + frameID) + 
               location.hashCode();
    }
}

