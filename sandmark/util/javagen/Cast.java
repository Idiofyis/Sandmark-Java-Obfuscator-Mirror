package sandmark.util.javagen;

public class Cast
   extends sandmark.util.javagen.Expression {
   sandmark.util.javagen.Expression expr;

   public Cast (
      String typeName,
      sandmark.util.javagen.Expression expr){
      this.type = typeName;
      this.expr = expr;
   }

   public String toString(String indent)  {
      return "(" + type + ")" + expr.toString();
   }


   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
      expr.toByteCode(cg,mg);
      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
      org.apache.bcel.generic.ConstantPoolGen cp =
         cg.getConstantPool();
      int index = cp.addClass(type);
      org.apache.bcel.generic.CHECKCAST cast = new org.apache.bcel.generic.CHECKCAST(index);
      il.append(cast);
   }

	public void toCode(
		      sandmark.program.Class cg,
	          sandmark.program.Method mg) {

	      expr.toCode(cg,mg);
	      org.apache.bcel.generic.InstructionList il =
	         mg.getInstructionList();
	      org.apache.bcel.generic.ConstantPoolGen cp =
	         cg.getConstantPool();
	      int index = cp.addClass(type);
	      org.apache.bcel.generic.CHECKCAST cast = new org.apache.bcel.generic.CHECKCAST(index);
	      il.append(cast);
   }
}



