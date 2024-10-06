package sandmark.watermark.ct.trace;

public class Tracer extends sandmark.util.exec.Overseer {

   /**
    *  The sandmark.watermark.ct.trace.Tracer class contains methods for
    *  running a program and detecting annotation points.
    *
    *  @param p  global property list
    *
    *  <P> Reads properties:
    *  @param Class Path              path to search for class-files
    *  @param Main Class              main class, if not set in Jar file
    *  @param Arguments       program arguments, if any
    *  @param DWM_CT_AnnotatorClass         where Annotator.java lives,
    *                                if not sandmark.watermark.ct.trace.Annotator
    *  @param DWM_MaxTracePoints         max number of annotation points we
    *                                will accept
    * <P>
    */

   private static String annotatorClass = "";
   private java.util.LinkedList list = null;

   class Breakpoint extends sandmark.util.exec.Breakpoint{
      public Breakpoint (String className,
                         String methodName) {
         super(className,methodName);
      }

      public void Action(sandmark.util.exec.MethodCallData data) {
         String value = getMarkValue();
         sandmark.util.StackFrame caller = data.getCallersCaller();
         sandmark.util.ByteCodeLocation location = new sandmark.util.ByteCodeLocation(
                                                                                      caller.getLocation().getMethod(),
                                                                                      caller.getLocation().getLineNumber(),
                                                                                      caller.getLocation().getCodeIndex() - sandmark.watermark.ct.trace.Preprocessor.ADDEDCODESIZE);
         sandmark.util.StackFrame[] stack = data.getCallStack();
         sandmark.util.StackFrame[] stack1 =
            sandmark.util.exec.MethodCallData.deleteIncompleteStackFrames(stack);
         for(int i=0; i<stack1.length; i++) {
            int offset = sandmark.watermark.ct.trace.Preprocessor.ADDEDCODESIZE;
            sandmark.util.ByteCodeLocation origLoc = stack1[i].getLocation();
            sandmark.util.ByteCodeLocation newLoc = 
               new sandmark.util.ByteCodeLocation
               (origLoc.getMethod(),origLoc.getLineNumber(),origLoc.getCodeIndex() - offset);
            stack1[i] = new sandmark.util.StackFrame(newLoc,stack1[i].getThreadID(),stack1[i].getFrameID());
         }
         sandmark.watermark.ct.trace.TracePoint tracePoint =
            new sandmark.watermark.ct.trace.TracePoint(value, location, stack1);
         synchronized (list) {
            list.add(tracePoint);
            list.notifyAll();
         }
      }
   }

   public Tracer(String cmdLine[],
                 sandmark.util.ConfigProperties props) {
      super(cmdLine);
      annotatorClass = props.getProperty("DWM_CT_AnnotatorClass","sandmark.watermark.ct.trace.Annotator");
      registerBreakpoint(new Breakpoint(annotatorClass, "MARK"));
      list = new java.util.LinkedList();
   }

   public java.util.List getTracePoints() {
      return list;
   }


   //----------------------------------------------------------

   protected String getMarkValue () {
      java.util.List classes = vm.classesByName(annotatorClass);
      java.util.Iterator iter = classes.iterator();
      String value = "";
      if (iter.hasNext()) {
         com.sun.jdi.ClassType DWM_CT_AnnotatorClass = (com.sun.jdi.ClassType)iter.next();
         com.sun.jdi.Field VALUE = DWM_CT_AnnotatorClass.fieldByName("VALUE");
         com.sun.jdi.Value markValue = DWM_CT_AnnotatorClass.getValue(VALUE);
         com.sun.jdi.StringReference markString = (com.sun.jdi.StringReference) markValue;
         value = markString.value();
      } else {
         System.exit(1);
      }
      return value;
   }

   //----------------------------------------------------------
   /*
    * Stop tracing the program, by user request.
    */
   public void STOP() {
      synchronized (list) {
         super.STOP();
         list.notifyAll();
      }
   }

} // class Tracer

