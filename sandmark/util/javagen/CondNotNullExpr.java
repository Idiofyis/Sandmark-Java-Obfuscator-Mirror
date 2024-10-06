package sandmark.util.javagen;

public class CondNotNullExpr
   extends sandmark.util.javagen.Expression {
   sandmark.util.javagen.Expression cond;
   sandmark.util.javagen.Expression expr1;
   sandmark.util.javagen.Expression expr2;

   public CondNotNullExpr (
      sandmark.util.javagen.Expression cond,
      sandmark.util.javagen.Expression expr1,
      sandmark.util.javagen.Expression expr2,
      String type){
      this.cond = cond;
      this.expr1 = expr1;
      this.expr2 = expr2;
      this.type = type;
   }

   public String toString(String indent)  {
      String P = indent  +
                 "("  +  cond.toString()  +  " != null)?"  +
                 expr1.toString()  +
                 ":"  +
                 expr2.toString();
      return P;
   }


   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg,
      org.apache.bcel.generic.MethodGen mg) {
      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();

      cond.toByteCode(cg,mg);
      org.apache.bcel.generic.IFNULL test = new org.apache.bcel.generic.IFNULL(null);
      il.append(test);

      expr1.toByteCode(cg,mg);
      org.apache.bcel.generic.GOTO branch = new org.apache.bcel.generic.GOTO(null);
      il.append(branch);
      org.apache.bcel.generic.InstructionHandle h1 =
          il.append(new org.apache.bcel.generic.NOP());
      test.setTarget(h1);
      expr2.toByteCode(cg,mg);
      org.apache.bcel.generic.InstructionHandle h2 =
          il.append(new org.apache.bcel.generic.NOP());
      branch.setTarget(h2);
   }

   public void toCode(
   	      sandmark.program.Class cg,
          sandmark.program.Method mg) {

         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();

         cond.toCode(cg,mg);
         org.apache.bcel.generic.IFNULL test = new org.apache.bcel.generic.IFNULL(null);
         il.append(test);

         expr1.toCode(cg,mg);
         org.apache.bcel.generic.GOTO branch = new org.apache.bcel.generic.GOTO(null);
         il.append(branch);
         org.apache.bcel.generic.InstructionHandle h1 =
             il.append(new org.apache.bcel.generic.NOP());
         test.setTarget(h1);
         expr2.toCode(cg,mg);
         org.apache.bcel.generic.InstructionHandle h2 =
             il.append(new org.apache.bcel.generic.NOP());
         branch.setTarget(h2);
      }


}



