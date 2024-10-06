package sandmark.util.javagen;

public class EmptyStatement
   extends sandmark.util.javagen.Statement {

   public EmptyStatement (){
   }

   public String toString(String indent)  {
      return indent+";";
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
      org.apache.bcel.generic.InstructionList il =
         mg.getInstructionList();

      il.append(new org.apache.bcel.generic.NOP());
   }
   public void toCode(
   	      sandmark.program.Class cg,
          sandmark.program.Method mg) {
         org.apache.bcel.generic.InstructionList il =
            mg.getInstructionList();

         il.append(new org.apache.bcel.generic.NOP());
   }
}



