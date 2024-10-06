package sandmark.util.exec;

/**
 * The sandmark.util.exec.MethodCallData class contains information
 * about a method call: which method was called, what it's
 * signature is, who called it, what thread it's running in,
 * etc. Public fields can be accessed directly but should be
 * treated as read-only.
*/

public class MethodCallData {
    public com.sun.jdi.event.LocatableEvent event;
    public com.sun.jdi.Method method;
    public com.sun.jdi.VirtualMachine vm;
    com.sun.jdi.ThreadReference thread;

    public MethodCallData (
       com.sun.jdi.VirtualMachine vm,
       com.sun.jdi.event.LocatableEvent event,
       com.sun.jdi.Method method) {
	this.vm = vm;
	this.event = event;
	this.method = method;
        thread = event.thread();
    }

    public String getName() {
       return method.name();
    }

    public String getTypeName() {
       com.sun.jdi.ReferenceType type = method.declaringType();
       String typeName = type.name();
       return typeName;
    }

    public String getThreadName() {
       String threadName = thread.name();
       return threadName;
    }

    public long getThreadID() {
       long threadID = thread.uniqueID();
       return threadID;
    }

    public com.sun.jdi.ObjectReference getObject() {
       com.sun.jdi.ObjectReference object = null;
       try {
          com.sun.jdi.StackFrame calleeFrame = thread.frame(0);
          object = calleeFrame.thisObject();
       } catch (Exception e) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }
       return object;
    }

    public long getObjectID() {
       long objectID = -1;
       com.sun.jdi.ObjectReference thisObject = getObject();
       if (thisObject != null)
          objectID = thisObject.uniqueID();
       return objectID;
    }

    public sandmark.util.StackFrame getCallersCaller() {
       return getCallData(2);
    }

    public sandmark.util.StackFrame getCaller() {
       return getCallData(1);
    }

    public sandmark.util.StackFrame getCallee() {
       return getCallData(0);
    }

    /*
     * This is the tricky part. We need to get a unique ID for each
     * frame. To do this, we added some code to the beginning of
     * each method, prior to tracing.
     *    void P() {
     *       long sm$stackID = sandmark.watermark.ct.trace.Annotator.stackFrameNumber++;
     *       ...
     *    }
     * Now we look up the current value of sm$stackID in the current stack
     * frame. For this to work the method has to have its LocalVariableTable
     * intact.
     * We will never be able to get a unique frame ID for system methods,
     * but that's OK. We're just interested in building the call graph
     * for methods in the user's program.
     */
    long getFrameID(com.sun.jdi.StackFrame frame) {
       long frameID = -1;
       try {
	   String name = sandmark.watermark.ct.trace.Preprocessor.STACKID;
	   //   System.out.println("getFrameID:1=" + frame.toString());
	   //   java.util.List locals = frame.visibleVariables();
	   //   java.util.Iterator vars = locals.iterator();
	   //  while (vars.hasNext()) {
	   //     com.sun.jdi.LocalVariable L = (com.sun.jdi.LocalVariable)vars.next();
	   //      System.out.println("getFrameID:2:L=" + L.toString());
	   //   }
	   com.sun.jdi.LocalVariable local = frame.visibleVariableByName(name);
           com.sun.jdi.LongValue value =  (com.sun.jdi.LongValue)frame.getValue(local);
           frameID = value.value();
	   //System.out.println("getFrameID:6:frameID=" + frameID);
       } catch (com.sun.jdi.AbsentInformationException e) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }
       return frameID;
    }

    sandmark.util.StackFrame getCallData(int frameNumber) {
       long codeIndex = sandmark.util.ByteCodeLocation.MISSING_long;
       long lineNumber = sandmark.util.ByteCodeLocation.MISSING_long;
       long threadID = sandmark.util.StackFrame.MISSING_long;
       long frameID = sandmark.util.StackFrame.MISSING_long;
       String name = "?";
       String signature = "?";
       String sourceName = "?";
       try {
          com.sun.jdi.StackFrame frame = thread.frame(frameNumber);
          com.sun.jdi.Location location = frame.location();
          //  sourceName = location.sourceName();
          sourceName = location.declaringType().name();
          name = location.method().name();
          signature = location.method().signature();
          codeIndex = location.codeIndex();
          lineNumber = location.lineNumber();
          threadID = getThreadID();
	  frameID = getFrameID(frame);
       } catch (Exception e) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }
       sandmark.util.MethodID method = 
           new sandmark.util.MethodID(name, signature, sourceName);
       sandmark.util.ByteCodeLocation bc = 
           new sandmark.util.ByteCodeLocation(method, lineNumber, codeIndex);
       return new sandmark.util.StackFrame(bc, threadID, frameID);
    }

    public sandmark.util.StackFrame[] getCallStack() {
       try {
          int frameCount = thread.frameCount();
	  sandmark.util.StackFrame[] cd = new sandmark.util.StackFrame[frameCount];
	  for(int i=0; i<frameCount; i++)
	      cd[i] = getCallData(i); 
          return cd;
       } catch (Exception e) {
          return null;
       }
    }

    /*
     * Return a new stack-trace, with all incomplete
     * frames removed. A frame is incomplete if we
     * have no frameID for it. 
     * In effect, we're removing all stack frames that
     * belong to system classes as well as frames from
     * sandmark.watermark.ct.trace.Annotator.MARK().
     */
    public static sandmark.util.StackFrame[] deleteIncompleteStackFrames(
        sandmark.util.StackFrame[] stack) {
       int OKframes=0;
       for(int i=0; i<stack.length; i++)
	   if (stack[i].getFrameID() != sandmark.util.StackFrame.MISSING_long)
	       OKframes++;

       sandmark.util.StackFrame[] stack1 = new sandmark.util.StackFrame[OKframes];
       int k=0;
       for(int i=0; i<stack.length; i++)
	   if (stack[i].getFrameID() != sandmark.util.StackFrame.MISSING_long)
	       stack1[k++] = stack[i];

       return stack1;
    }
}





