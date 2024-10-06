package sandmark.util.javagen;

public class Local
   extends sandmark.util.javagen.Statement {
   String name;
   String type;
   sandmark.util.javagen.Expression init;

   public Local (
      String name,
      String type,
      sandmark.util.javagen.Expression init){
      this.name = name;
      this.type = type;
      this.init = init;
   }

   public String toString(String indent)  {
      String P = indent  +
                 type.toString()  +  " "  +
                 name.toString()  +  " = "  +
                 init.toString() ;
      return P;
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
       org.apache.bcel.generic.ConstantPoolGen cp =
          cg.getConstantPool();

       org.apache.bcel.generic.Type T =
           sandmark.util.javagen.Java.typeToByteCode(type);

       init.toByteCode(cg,mg);

       org.apache.bcel.generic.LocalVariableGen lg =
	   mg.addLocalVariable(name, T, null, null);
       int index = lg.getIndex();
       lg.setStart(il.append(new org.apache.bcel.generic.ASTORE(index)));

       // mg.setMaxLocals(mg.getMaxLocals()+1); UNTESTED
   }


	 public void toCode(
	           sandmark.program.Class cg,
          sandmark.program.Method mg) {
          org.apache.bcel.generic.InstructionList il =
             mg.getInstructionList();
          org.apache.bcel.generic.ConstantPoolGen cp =
             cg.getConstantPool();

          org.apache.bcel.generic.Type T =
              sandmark.util.javagen.Java.typeToByteCode(type);

          init.toCode(cg,mg);

          org.apache.bcel.generic.LocalVariableGen lg =
   	   	  mg.addLocalVariable(name, T, null, null);
          int index = lg.getIndex();
          lg.setStart(il.append(new org.apache.bcel.generic.ASTORE(index)));

          // mg.setMaxLocals(mg.getMaxLocals()+1); UNTESTED
   }
}



