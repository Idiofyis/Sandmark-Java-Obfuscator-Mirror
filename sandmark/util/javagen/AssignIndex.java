package sandmark.util.javagen;

public class AssignIndex
   extends sandmark.util.javagen.Statement {

   sandmark.util.javagen.Expression array;
   sandmark.util.javagen.Expression idx;
   sandmark.util.javagen.Expression right;

   public AssignIndex (
      sandmark.util.javagen.Expression array,
      sandmark.util.javagen.Expression idx,
      sandmark.util.javagen.Expression right){
      this.array = array;
      this.idx = idx;
      this.right = right;
   }

   public String toString(String indent)  {
    String P = indent  +
               array.toString()  +
               "[" + idx.toString() + "]" +
               " = "  +
               right.toString();
      return P;
   }


   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {

       array.toByteCode(cg,mg);
       idx.toByteCode(cg,mg);
       right.toByteCode(cg,mg);

       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
       il.append(new org.apache.bcel.generic.AASTORE());
   }

	 public void toCode(
	      sandmark.program.Class cg,
          sandmark.program.Method mg) {
	   array.toCode(cg,mg);
       idx.toCode(cg,mg);
       right.toCode(cg,mg);

       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
       il.append(new org.apache.bcel.generic.AASTORE());
   }
}



