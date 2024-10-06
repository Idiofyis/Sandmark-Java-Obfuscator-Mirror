package sandmark.util.exec;

public class EventHandler {

   public String[] excludeClasses = {};
   public String[] includeClasses = {};

   public EventHandler(String[] includeClasses,
                       String[] excludeClasses) {
      this.excludeClasses = excludeClasses;
      this.includeClasses = includeClasses;
   }

   public void onMethodEntry(sandmark.util.exec.MethodCallData data) {}

   public void onMethodExit(sandmark.util.exec.MethodCallData data) {}

   public void onProgramExit(com.sun.jdi.VirtualMachine vm) {}
}

