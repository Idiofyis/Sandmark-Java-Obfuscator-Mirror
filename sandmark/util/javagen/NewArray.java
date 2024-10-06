package sandmark.util.javagen;

public class NewArray
   extends sandmark.util.javagen.Expression {
   int count;

   public NewArray (String type, int count){
      this.type = type;
      this.count = count;
   }

   public String toString(String indent)  {
      String P = "new "  +
                 type.toString()  +
                 "[" + count + "]";
      return P;
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {

      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();
      org.apache.bcel.generic.ConstantPoolGen cp =
         cg.getConstantPool();

      org.apache.bcel.generic.PUSH push = new org.apache.bcel.generic.PUSH(cp,count);
      il.append(push);

      int index = cp.addClass(type);
      org.apache.bcel.generic.ANEWARRAY anew = new org.apache.bcel.generic.ANEWARRAY(index);
      il.append(anew);
   }
   public void toCode(
   	      sandmark.program.Class cg,
          sandmark.program.Method mg) {

         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();
         org.apache.bcel.generic.ConstantPoolGen cp =
            cg.getConstantPool();

         org.apache.bcel.generic.PUSH push = new org.apache.bcel.generic.PUSH(cp,count);
         il.append(push);

         int index = cp.addClass(type);
         org.apache.bcel.generic.ANEWARRAY anew = new org.apache.bcel.generic.ANEWARRAY(index);
         il.append(anew);
   }

}



