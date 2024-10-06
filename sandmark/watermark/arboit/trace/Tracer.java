package sandmark.watermark.arboit.trace;

public class Tracer extends sandmark.util.exec.Overseer {

   private static String annotatorClass = "";
   private java.util.LinkedList list = null;
   private sandmark.watermark.arboit.trace.TracePoint nextObjext;
   private boolean done = false;
   private java.util.LinkedList callStackList = null;

   class Breakpoint extends sandmark.util.exec.Breakpoint{
      public Breakpoint(String className, String methodName){
         super(className, methodName);
      }

      public void Action(sandmark.util.exec.MethodCallData data){
         sandmark.util.StackFrame caller = data.getCallersCaller();
         sandmark.util.ByteCodeLocation location = 
            new sandmark.util.ByteCodeLocation(caller.getLocation().getMethod(),
            caller.getLocation().getLineNumber(), caller.getLocation().getCodeIndex());
         //System.out.println("location: " + location);
         //sandmark.util.StackFrame[] stack = data.getCallStack();
         //sandmark.util.StackFrame[] stack1 = 
            //sandmark.util.exec.MethodCallData.deleteIncompleteStackFrames(stack);
         //for(int i=0; i < stack.length; i++){
            //System.out.println("in loop");
            sandmark.watermark.arboit.trace.TracePoint tracePoint =
               new sandmark.watermark.arboit.trace.TracePoint(location);
            synchronized(list){
               if(!list.contains(tracePoint)){
                  list.add(tracePoint);
                  list.notifyAll();
               }
            }
         //}
      }
   }//end inner class Breakpoint

   public Tracer(String cmdLine[],
                 sandmark.util.ConfigProperties props) {
      super(cmdLine);
      annotatorClass = props.getProperty("DWM_AA_AnnotatorClass",
         "sandmark.watermark.arboit.trace.Annotator");
      registerBreakpoint(new Breakpoint(annotatorClass, "MARK"));
      list = new java.util.LinkedList();
      callStackList = new java.util.LinkedList();
   }

   public java.util.List getTracePoints(){
      return list;
   }

   public void STOP(){
      synchronized(list){
         done = true;
         super.STOP();
         list.notifyAll();
      }
   }

   private boolean mExited = false;
   protected synchronized void onProgramExit(com.sun.jdi.VirtualMachine vm) {
      mExited = true;
      notifyAll();
   }

   public synchronized boolean exited() {
      return mExited;
   }

}//end Tracer
