package sandmark.util.javagen;

public class LocalRef
   extends sandmark.util.javagen.Expression {
   String name;

   public  LocalRef(String name, String type) {
       this.name = name;
       this.type = type;
   }

   public String toString(String indent)  {
      return name;
   }

   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg,
      org.apache.bcel.generic.MethodGen mg) {
      int localIndex = findLocal(name,mg.getLocalVariables());
      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
      il.append(new org.apache.bcel.generic.ALOAD(localIndex));
   }

 public void toCode(
 	      sandmark.program.Class cg,
          sandmark.program.Method mg) {
         int localIndex = findLocal(name,mg.getLocalVariables());
         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();
         il.append(new org.apache.bcel.generic.ALOAD(localIndex));
   }

   public static int findLocal(
       String name,
       org.apache.bcel.generic.LocalVariableGen[] locals) {
      for(int i=0; i<locals.length; i++)
         if (locals[i].getName().equals(name))
   	       return locals[i].getIndex();
         return -1;
   }

}



