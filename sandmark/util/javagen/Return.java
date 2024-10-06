package sandmark.util.javagen;

public class Return
   extends sandmark.util.javagen.Statement {
   sandmark.util.javagen.Expression expr;

   public Return (sandmark.util.javagen.Expression expr){
      this.expr = expr;
   }

   public Return (){
      this.expr = null;
   }

   public String toString(String indent)  {
      String P = indent  +  "return "  +  ((expr!=null)?expr.toString():"");
      return P;
   }


   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
               if (expr != null) {
                 expr.toByteCode(cg,mg);
                 il.append(new org.apache.bcel.generic.ARETURN());
               } else
                 il.append(new org.apache.bcel.generic.RETURN());
   }

       public void toCode(
          sandmark.program.Class cg,
          sandmark.program.Method mg) {
          org.apache.bcel.generic.InstructionList il =
             mg.getInstructionList();
               if (expr != null) {
                 expr.toCode(cg,mg);
                 il.append(new org.apache.bcel.generic.ARETURN());
               } else
                 il.append(new org.apache.bcel.generic.RETURN());
   }
}


