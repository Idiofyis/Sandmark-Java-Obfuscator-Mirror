/**
 * The sandmark.util.exec.DumpAll is essentially a testing class
 * for sandmark.util.exec.Overseer. It traces every method call
 * in a program, and at the end, dumps every reachable
 * object on the heap. Call it like this:
 * <P>
 * <PRE>
 *  java -classpath jpda.jar DumpAll '-classpath .' HelloWorld
 * </PRE>
 *
*/

package sandmark.util.exec;

class DumpAll extends sandmark.util.exec.Overseer{
   long allocCount = 0;
   java.io.PrintWriter writer;

   public DumpAll(
       String[] includeClasses,
       String[] excludeClasses,
       java.io.PrintWriter writer,
       String[] argv) {
       super(includeClasses,excludeClasses, argv);
       this.writer = writer;
   }

   public void onMethodEntry (
      sandmark.util.exec.MethodCallData data) {
      methodEvent("ENTER: ", data);
      if (data.method.isConstructor() || data.method.isStaticInitializer()) {
          writer.println("ALLOC " + allocCount + " : " + data.getObjectID());
          allocCount++;
      }
   }

   public void onMethodExit (
      sandmark.util.exec.MethodCallData data) {
      methodEvent("EXIT: ", data);
   }

   public void onProgramExit (
      com.sun.jdi.VirtualMachine vm) {
      sandmark.util.exec.Heap heap = new sandmark.util.exec.Heap(vm);
      while (heap.hasNext()) {
	 sandmark.util.exec.HeapData obj = (sandmark.util.exec.HeapData) heap.next();
         writer.print(obj.uniqueID + " " + obj.name + ":" + obj.type + " ");
         for(int i=0; i<obj.refs.length; i++)
	     writer.print(obj.refs[i] + " ");
         writer.println();
      }
   }

   void methodEvent(
      String what,
      sandmark.util.exec.MethodCallData data) {
      sandmark.util.StackFrame caller = data.getCaller();
      String callerD = "caller=(" + 
                           caller.getLocation().getMethod().getName() + "," +
      	                   caller.getLocation().getCodeIndex() + "," +
                           caller.getLocation().getLineNumber() +
                       ")";
      sandmark.util.StackFrame callee = data.getCallee();
      String calleeD = "callee=(" + 
                           callee.getLocation().getMethod().getName() + "," +
      	                   callee.getLocation().getCodeIndex() + "," +
                           callee.getLocation().getLineNumber() +
                       ")";
       String threadD = "thread=(" + data.getThreadName() + "," + data.getThreadID() + ")";
       String who = data.getObjectID() + "." + data.getTypeName() + "." + data.getName();
       String D = what + who + " " + callerD + " " + calleeD + " " + data.getThreadID();
       writer.println(D);
    }

    public static void printLegend(java.io.PrintWriter writer) {
       writer.print("LEGEND: ");
       writer.print("'OP: object.class.method ");
       writer.print("caller=(method,source,bytecodeIdx,sourceLine) ");
       writer.print("callee=(method,source,bytecodeIdx,sourceLine) ");
       writer.println("thread=(name,ID)'");
    }

    public static void main(String argv[])
        throws sandmark.util.exec.TracingException {
       String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*"};
       String[] includes = {};

       java.io.PrintWriter writer = new java.io.PrintWriter(System.out);
       printLegend(writer);
       sandmark.util.exec.DumpAll dumper = 
          new sandmark.util.exec.DumpAll(includes, excludes, writer, argv);
       dumper.run();
       dumper.waitToComplete();
       writer.close();
   }
}

