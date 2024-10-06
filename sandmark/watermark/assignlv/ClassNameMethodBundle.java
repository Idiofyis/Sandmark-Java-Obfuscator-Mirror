package sandmark.watermark.assignlv;

public class ClassNameMethodBundle {

   private String name = "";
   private sandmark.program.Method method = null;
   private int methodArraySlot = -1;

   public ClassNameMethodBundle(String name, sandmark.program.Method method,
      int i){
      this.name = name;
      this.method = method;
      methodArraySlot = i;
   }

   public sandmark.program.Method getMethod(){
      return method;
   }

   public String getClassName(){
      return name;
   }

   public int getSlot(){
      return methodArraySlot;
   }

   public String toString(){
      String s =
      "Slot: " + this.methodArraySlot + " contains class " + this.name + " and method " + this.method.getName();
      return s;
   }
}

