package sandmark.util.javagen;

public class AssignStatic
   extends sandmark.util.javagen.Statement {
   String Class;
   String field;
   String type;
   sandmark.util.javagen.Expression right;

   public  AssignStatic(
      String Class,
      String field,
      String type,
      sandmark.util.javagen.Expression right) {
       this.Class = Class;
       this.field = field;
       this.type = type;
       this.right = right;
   }

   public String toString(String indent)  {
      return indent + Class + "." + field + " = " + right.toString();
   }

   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg,
      org.apache.bcel.generic.MethodGen mg) {

      right.toByteCode(cg,mg);

      String S = org.apache.bcel.classfile.Utility.getSignature(type);

      org.apache.bcel.generic.ConstantPoolGen cp =
         cg.getConstantPool();
      int index = cp.addFieldref(Class, field, S);

      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
      il.append(new org.apache.bcel.generic.PUTSTATIC(index));
   }

   public void toCode(
	           sandmark.program.Class cg,
          sandmark.program.Method mg) {

         right.toCode(cg,mg);

         String S = org.apache.bcel.classfile.Utility.getSignature(type);

         org.apache.bcel.generic.ConstantPoolGen cp =
            cg.getConstantPool();
         int index = cp.addFieldref(Class, field, S);

         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();
         il.append(new org.apache.bcel.generic.PUTSTATIC(index));
   }
}



