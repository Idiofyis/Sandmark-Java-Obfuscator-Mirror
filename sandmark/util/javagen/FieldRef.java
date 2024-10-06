package sandmark.util.javagen;

public class FieldRef
   extends sandmark.util.javagen.Expression {
   sandmark.util.javagen.Expression left;
   String field;
   String Class;

    public  FieldRef(
       sandmark.util.javagen.Expression left,
       String Class,
       String field,
       String type) {
       this.left = left;
       this.Class = Class;
       this.field = field;
       this.type = type;
   }

   public String toString(String indent)  {
      return left + "." + field;
   }

   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg,
      org.apache.bcel.generic.MethodGen mg) {

      left.toByteCode(cg,mg);

      org.apache.bcel.generic.ConstantPoolGen cp =
         cg.getConstantPool();
      int index = cp.addFieldref(Class, field,
                      org.apache.bcel.classfile.Utility.getSignature(type));

      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
      il.append(new org.apache.bcel.generic.GETFIELD(index));
   }
   public void toCode(
   	      sandmark.program.Class cg,
             sandmark.program.Method mg) {

         left.toCode(cg,mg);

         org.apache.bcel.generic.ConstantPoolGen cp =
            cg.getConstantPool();
         int index = cp.addFieldref(Class, field,
                         org.apache.bcel.classfile.Utility.getSignature(type));

         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();
         il.append(new org.apache.bcel.generic.GETFIELD(index));
   }
}



