package sandmark.util.javagen;

public class Discard
   extends sandmark.util.javagen.Statement {
    sandmark.util.javagen.Expression expr;

   public Discard (
      sandmark.util.javagen.Expression expr){
      this.expr = expr;
   }

   public String toString(String indent)  {
      return expr.toString();
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();

      expr.toByteCode(cg,mg);

      il.append(new org.apache.bcel.generic.POP());
   }
   public void toCode(
   	           sandmark.program.Class cg,
          sandmark.program.Method mg) {
         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();

         expr.toCode(cg,mg);

         il.append(new org.apache.bcel.generic.POP());
   }
}



