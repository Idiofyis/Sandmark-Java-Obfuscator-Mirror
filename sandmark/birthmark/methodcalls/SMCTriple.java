package sandmark.birthmark.methodcalls;

public class SMCTriple implements java.util.Comparator{

   private String methodName;
   private String className;
   private String signature;

   public SMCTriple(String methodName, String className, String signature){
      this.methodName = methodName;
      this.className = className;
      this.signature = signature;
   }

   public String getMethodName(){
      return methodName;
   }

   public String getClassName(){
      return className;
   }

   public String getSignature(){
      return signature;
   }

   public boolean equals(Object o){
      SMCTriple t = (SMCTriple)o;
      if(this.getMethodName().equals(t.getMethodName()) &&
         this.getClassName().equals(t.getClassName()) &&
         this.getSignature().equals(t.getSignature()))
         return true;
      else
         return false;
   }


   public int compare(Object o1, Object o2){
      SMCTriple t1 = (SMCTriple)o1;
      SMCTriple t2 = (SMCTriple)o2;
      return t1.getSignature().compareTo(t2.getSignature());
   }


   public String toString(){
      return "(" + className + ", " + methodName + ", " + signature + ")";
   }
}
