package sandmark.util.javagen;

public class IfNotNull
   extends sandmark.util.javagen.Statement {

   sandmark.util.javagen.Expression expr;
   sandmark.util.javagen.List stats;

   public IfNotNull (
       sandmark.util.javagen.Expression expr,
       sandmark.util.javagen.List stats){
      this.expr = expr;
      this.stats = stats;
   }

   public IfNotNull (
       sandmark.util.javagen.Expression expr,
       sandmark.util.javagen.Statement stat){
      this.expr = expr;
      this.stats = new sandmark.util.javagen.List(stat);
   }

   public String toString(String indent)  {
       //  System.out.println(expr);
       //   System.out.println(stats);
      String P =   indent  +
                   "if ("  +
                   expr.toString()  +
                   " != null) " +
                   renderBlock(stats, indent);
      return P;
   }

   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg,
      org.apache.bcel.generic.MethodGen mg) {
      expr.toByteCode(cg,mg);
      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
      org.apache.bcel.generic.IFNULL branch = new org.apache.bcel.generic.IFNULL(null);
      il.append(branch);

      java.util.Iterator siter = stats.iterator();
      while (siter.hasNext()) {
          sandmark.util.javagen.Statement s = (sandmark.util.javagen.Statement) siter.next();
          s.toByteCode(cg,mg);
      }
      org.apache.bcel.generic.InstructionHandle h1 = il.append(new org.apache.bcel.generic.NOP());
      branch.setTarget(h1);
   }


		 public void toCode(
		           sandmark.program.Class cg,
          sandmark.program.Method mg) {
	      expr.toCode(cg,mg);
	      org.apache.bcel.generic.InstructionList il =
	         mg.getInstructionList();
	      org.apache.bcel.generic.IFNULL branch = new org.apache.bcel.generic.IFNULL(null);
	      il.append(branch);

	      java.util.Iterator siter = stats.iterator();
	      while (siter.hasNext()) {
	          sandmark.util.javagen.Statement s = (sandmark.util.javagen.Statement) siter.next();
	          s.toCode(cg,mg);
	      }
	      org.apache.bcel.generic.InstructionHandle h1 = il.append(new org.apache.bcel.generic.NOP());
	      branch.setTarget(h1);
	   }

}



