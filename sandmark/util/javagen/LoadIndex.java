package sandmark.util.javagen;

public class LoadIndex
   extends sandmark.util.javagen.Expression {

   sandmark.util.javagen.Expression array;
   sandmark.util.javagen.Expression idx;

   public LoadIndex (
      sandmark.util.javagen.Expression array,
      sandmark.util.javagen.Expression idx,
      String type){
      this.array = array;
      this.idx = idx;
      this.type = type;
   }

   public String toString(String indent)  {
      String P = array.toString()  +
                 "[" + idx.toString() + "]";
      return P;
   }


   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {

       array.toByteCode(cg,mg);
       idx.toByteCode(cg,mg);

       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
       il.append(new org.apache.bcel.generic.AALOAD());
   }
public void toCode(
	      sandmark.program.Class cg,
          sandmark.program.Method mg) {

       array.toCode(cg,mg);
       idx.toCode(cg,mg);

       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
       il.append(new org.apache.bcel.generic.AALOAD());
   }
}



