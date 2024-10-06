package sandmark.util.javagen;

public class StaticRef
   extends sandmark.util.javagen.Expression {
   String Class;
   String field;

   public  StaticRef(String Class, String field, String type) {
       this.Class = Class;
       this.field = field;
       this.type = type;
   }

   public String toString(String indent)  {
      return Class + "." + field;
   }

   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg,
      org.apache.bcel.generic.MethodGen mg) {

      org.apache.bcel.generic.ConstantPoolGen cp =
         cg.getConstantPool();
      int index = cp.addFieldref(Class, field,
                      org.apache.bcel.classfile.Utility.getSignature(type));

      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
      il.append(new org.apache.bcel.generic.GETSTATIC(index));
   }
   public void toCode(
   	      sandmark.program.Class cg,
          sandmark.program.Method mg) {
         org.apache.bcel.generic.ConstantPoolGen cp =
            cg.getConstantPool();
         int index = cp.addFieldref(Class, field,
                         org.apache.bcel.classfile.Utility.getSignature(type));

         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();
         il.append(new org.apache.bcel.generic.GETSTATIC(index));
   }
}



