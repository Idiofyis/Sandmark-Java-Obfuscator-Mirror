package sandmark.util.exec;

public abstract class Breakpoint {

   public static final String[] standardExclude = {"java.*","javax.*","sun.*","com.sun.*"};
   public static final String[] noExclude = {};

   public String className = null;
   public String methodName = null;
   public String signature = null;
   public String[] excludeClasses = {};

   /*
    * Set one breakpoint at the beginning of method className.methodName.
    */
   public Breakpoint (String className,
                      String methodName) {
      this.className = className;
      this.methodName = methodName; 
      this.signature = "*"; 
   }

   public Breakpoint (String className,
                      String methodName,
                      String signature) {
      this.className = className;
      this.methodName = methodName; 
      this.signature = signature; 
   }

   /*
    * Set breakpoints at the beginning of all methods methodName,
    * whose type is signature, except those in excludeClasses. 
    */
   public Breakpoint (String methodName,
                      String signature,
                      String[] excludeClasses) {
      this.className = "*";
      this.methodName = methodName; 
      this.signature = signature;
      this.excludeClasses = excludeClasses;
   }

   /*
    * Set breakpoints at the beginning of all methods methodName,
    * except those in excludeClasses. 
    */
   public Breakpoint (String methodName,
                      String[] excludeClasses) {
      this.className = "*";
      this.methodName = methodName; 
      this.signature = "*";
      this.excludeClasses = excludeClasses;
   }

   /*
    * Set breakpoints at the beginning of all methods methodName,
    * except those in the standard set of exclude classes. 
    */
   public Breakpoint (String methodName) {
      this.className = "*";
      this.methodName = methodName; 
      this.signature = "*";
      this.excludeClasses = standardExclude;
   }

   public String toString() {
      String S = "{";
      for(int i=0; i<excludeClasses.length; i++)
         S += excludeClasses[i] + ",";
      S += "}";
      return "Breakpoint(" + 
         className + "," + 
         methodName + "," + 
         signature + "," + 
         S + ")";
   }
 
   public abstract void Action(sandmark.util.exec.MethodCallData data);
}

