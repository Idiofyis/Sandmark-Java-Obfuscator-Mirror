package sandmark.birthmark.wpp;

public class Tracer extends sandmark.util.exec.Overseer {

   private static String annotatorClass = "";
   private java.util.LinkedList list = null;

   class Breakpoint extends sandmark.util.exec.Breakpoint{
      public Breakpoint(String className, String methodName){
         super(className, methodName);
      }

      public void Action(sandmark.util.exec.MethodCallData data){
         sandmark.util.StackFrame caller = data.getCallersCaller();
         sandmark.util.ByteCodeLocation location = 
            new sandmark.util.ByteCodeLocation(caller.getLocation().getMethod(),
            caller.getLocation().getLineNumber(),
            caller.getLocation().getCodeIndex());
            //new sandmark.util.ByteCodeLocation(caller.location.method,
            //caller.location.lineNumber, caller.location.codeIndex);
         //System.out.println("location: " + location);
         //sandmark.util.StackFrame[] stack = data.getCallStack();
         //sandmark.util.StackFrame[] stack1 = 
            //sandmark.util.exec.MethodCallData.deleteIncompleteStackFrames(stack);
         //for(int i=0; i < stack.length; i++){
            //System.out.println("in loop");
            sandmark.birthmark.wpp.TracePoint tracePoint =
               new sandmark.birthmark.wpp.TracePoint(location);
            synchronized(list){
               //if(!list.contains(tracePoint)){
                  list.add(tracePoint);
                  list.notifyAll();
               //}
            }
         //}
      }
   }//end inner class Breakpoint

   public static String[] constructArgv(String annoFileName,String extraCP,String mainClass,String args) {
      int argn = 3;
      java.util.StringTokenizer S = new java.util.StringTokenizer(args," ");
      int C = S.countTokens();
      argn += C;
      String[] argv = new String[argn];
    
      java.io.File appFile = null; 
      try{ 
        appFile = java.io.File.createTempFile("smk", ".jar");
      }catch(java.io.IOException e){
         e.printStackTrace();
      }
      appFile.deleteOnExit();
      sandmark.program.Application app = null;
      try{
         app = new sandmark.program.Application(annoFileName);
      }catch(Exception e){
         System.out.println("couldn't create annotated app");
      }
      try{
         app.save(new java.io.FileOutputStream(appFile));
      }catch(java.io.FileNotFoundException e){
         System.out.println("file not found");
      }catch(java.io.IOException ex){
         ex.printStackTrace();
      }

      String classPath = "" + appFile + java.io.File.pathSeparatorChar +
            extraCP + java.io.File.pathSeparatorChar;

      argn = 0;
      if (classPath != null) {
         argv[argn++] = "-classpath";
         argv[argn++] = classPath;
      }
    
      argv[argn++] = mainClass;
    
      for(int i=0; i<C; i++)
         argv[argn++] = S.nextToken();
    
          //  System.out.println("constructArgv:");
          //for(int i=0; i<argv.length; i++)
          //    System.out.println(argv[i]);
    
         return argv;
   }

   public Tracer(String argv[]) {
      
      super(argv);
      annotatorClass = //props.getProperty("DWM_AA_AnnotatorClass",
         "sandmark.birthmark.wpp.Annotator";//);
      registerBreakpoint(new Breakpoint(annotatorClass, "MARK"));
      list = new java.util.LinkedList();
   }

   public java.util.List getTracePoints(){
      return list;
   }

   public void STOP(){
      synchronized(list){
         super.STOP();
         list.notifyAll();
      }
   }

   private boolean mExited = false;
   protected synchronized void onProgramExit(com.sun.jdi.VirtualMachine vm) {
      mExited = true;
      notifyAll();
   }

   public synchronized void waitForExit() {
      while(!mExited) try { wait() ; } catch(Exception e) {}
   }

   public synchronized boolean exited() {
      return mExited;
   }

}//end Tracer
