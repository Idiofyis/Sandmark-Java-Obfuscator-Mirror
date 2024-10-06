package sandmark.util.javagen;

public class StaticFunCall
   extends sandmark.util.javagen.Expression {
   String Class;
   String name;
   String type;
   sandmark.util.javagen.List args;

   public StaticFunCall (
      String Class,
      String name,
      String type,
      sandmark.util.javagen.List args){
      this.Class = Class;
      this.name = name;
      this.type = type;
      this.args = args;
   }

   public StaticFunCall (
      String Class,
      String name,
      String type,
      sandmark.util.javagen.Java arg){
      this.Class = Class;
      this.name = name;
      this.type = type;
      this.args = new sandmark.util.javagen.List(arg);
   }

   public StaticFunCall (
      String Class,
      String name,
      String type){
      this.Class = Class;
      this.name = name;
      this.type = type;
      this.args = new sandmark.util.javagen.List();
   }

   public StaticFunCall (
      String Class,
      String name,
      String type,
      sandmark.util.javagen.Java arg1,
      sandmark.util.javagen.Java arg2){
      this.Class = Class;
      this.name = name;
      this.type = type;
      this.args = new sandmark.util.javagen.List(arg1,arg2);
   }

   public String toString(String indent)  {
      String P =  Class + "." + name +
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

       java.util.Iterator iter = args.iterator();
       while (iter.hasNext()) {
          sandmark.util.javagen.Expression s = (sandmark.util.javagen.Expression) iter.next();
          s.toByteCode(cg,mg);
       };

       int index = cp.addMethodref(Class,name,type);
       org.apache.bcel.generic.INVOKESTATIC s =
          new org.apache.bcel.generic.INVOKESTATIC(index);
       il.append(s);
   }
public void toCode(
	      sandmark.program.Class cg,
          sandmark.program.Method mg) {

       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
       org.apache.bcel.generic.ConstantPoolGen cp =
          cg.getConstantPool();

       java.util.Iterator iter = args.iterator();
       while (iter.hasNext()) {
          sandmark.util.javagen.Expression s = (sandmark.util.javagen.Expression) iter.next();
          s.toCode(cg,mg);
       };

       int index = cp.addMethodref(Class,name,type);
       org.apache.bcel.generic.INVOKESTATIC s =
          new org.apache.bcel.generic.INVOKESTATIC(index);
       il.append(s);
   }
}



