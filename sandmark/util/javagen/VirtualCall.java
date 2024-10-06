package sandmark.util.javagen;

public class VirtualCall
   extends sandmark.util.javagen.Statement {
   String Class;
   String name;
   String type;
   sandmark.util.javagen.Expression obj;
   sandmark.util.javagen.List args;

   public VirtualCall (
      sandmark.util.javagen.Expression obj,
      String Class,
      String name,
      String type,
      sandmark.util.javagen.List args){
      this.obj = obj;
      this.Class = Class;
      this.name = name;
      this.type = type;
      this.args = args;
   }

   public VirtualCall (
      sandmark.util.javagen.Expression obj,
      String Class,
      String name,
      String type,
      sandmark.util.javagen.Java arg){
      this.obj = obj;
      this.Class = Class;
      this.name = name;
      this.type = type;
      this.args = new sandmark.util.javagen.List(arg);
   }

   public VirtualCall (
      sandmark.util.javagen.Expression obj,
      String Class,
      String name,
      String type){
      this.obj = obj;
      this.name = name;
      this.type = type;
      this.args = new sandmark.util.javagen.List();
   }

   public VirtualCall (
      sandmark.util.javagen.Expression obj,
      String Class,
      String name,
      String type,
      sandmark.util.javagen.Java arg1,
      sandmark.util.javagen.Java arg2){
      this.obj = obj;
      this.name = name;
      this.Class = Class;
      this.type = type;
      this.args = new sandmark.util.javagen.List(arg1,arg2);
   }

   public String toString(String indent)  {
      String P = indent +
                 obj.toString() + "." + name +
                "(" + renderListSeparate(args, ", ", indent) + ")" ;
      return P;
   }


   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {

       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
       org.apache.bcel.generic.ConstantPoolGen cp =
          cg.getConstantPool();

       obj.toByteCode(cg,mg);

       java.util.Iterator iter = args.iterator();
       while (iter.hasNext()) {
          sandmark.util.javagen.Expression s = (sandmark.util.javagen.Expression) iter.next();
          s.toByteCode(cg,mg);
       };

       int index = cp.addMethodref(Class,name,type);
       org.apache.bcel.generic.INVOKEVIRTUAL s =
          new org.apache.bcel.generic.INVOKEVIRTUAL(index);
       il.append(s);
   }
	public void toCode(
	           sandmark.program.Class cg,
          sandmark.program.Method mg) {

          org.apache.bcel.generic.InstructionList il =
             mg.getInstructionList();
          org.apache.bcel.generic.ConstantPoolGen cp =
             cg.getConstantPool();

          obj.toCode(cg,mg);

          java.util.Iterator iter = args.iterator();
          while (iter.hasNext()) {
             sandmark.util.javagen.Expression s = (sandmark.util.javagen.Expression) iter.next();
             s.toCode(cg,mg);
          };

          int index = cp.addMethodref(Class,name,type);
          org.apache.bcel.generic.INVOKEVIRTUAL s =
             new org.apache.bcel.generic.INVOKEVIRTUAL(index);
          il.append(s);
      }


}



