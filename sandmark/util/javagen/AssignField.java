package sandmark.util.javagen;

public class AssignField
   extends sandmark.util.javagen.Statement {

   sandmark.util.javagen.Expression left;
   sandmark.util.javagen.Expression right;
   String Class;
   String field;
   String type;

   public AssignField (
      sandmark.util.javagen.Expression left,
      String Class,
      String field,
      String type,
      sandmark.util.javagen.Expression right){
      this.Class = Class;
      this.field = field;
      this.type = type;
      this.left = left;
      this.right = right;
   }

   public String toString(String indent)  {
    String P = indent  +
               left.toString()  +  "." + field +
               " = "  +
               right.toString();
      return P;
   }


   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
      left.toByteCode(cg,mg);
      right.toByteCode(cg,mg);

      String S = org.apache.bcel.classfile.Utility.getSignature(type);

      org.apache.bcel.generic.ConstantPoolGen cp =
         cg.getConstantPool();
      int index = cp.addFieldref(Class, field, S);

      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
      il.append(new org.apache.bcel.generic.PUTFIELD(index));
   }

   public void toCode(
           sandmark.program.Class cg,
          sandmark.program.Method mg) {
         left.toCode(cg,mg);
         right.toCode(cg,mg);

         String S = org.apache.bcel.classfile.Utility.getSignature(type);

         org.apache.bcel.generic.ConstantPoolGen cp =
            cg.getConstantPool();
         int index = cp.addFieldref(Class, field, S);

         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();
         il.append(new org.apache.bcel.generic.PUTFIELD(index));
      }


}



