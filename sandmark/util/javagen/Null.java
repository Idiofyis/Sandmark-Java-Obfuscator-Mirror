package sandmark.util.javagen;

public class Null
   extends sandmark.util.javagen.Expression {

   public Null() {
       this.type = "void";
   }

   public String toString(String indent)  {
      this.type = "void";
      return "null";
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
       org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
       il.append(new org.apache.bcel.generic.ACONST_NULL());
   }
  public void toCode(
  	      sandmark.program.Class cg,
          sandmark.program.Method mg) {
          org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();
          il.append(new org.apache.bcel.generic.ACONST_NULL());
   }
}



