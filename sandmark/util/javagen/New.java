package sandmark.util.javagen;

public class New
   extends sandmark.util.javagen.Expression {
   sandmark.util.javagen.List args;

   public New (String type,
               sandmark.util.javagen.List args){
      this.type = type;
      this.args = args;
   }

   public New (String type,
               sandmark.util.javagen.Java arg){
      this.type = type;
      this.args = new sandmark.util.javagen.List(arg);
   }

   public New (String type){
      this.type = type;
      this.args = new sandmark.util.javagen.List();
   }

   public String toString(String indent)  {
      String P = "new "  +
                 type + "(" +
                 renderListSeparate(args, ", ", indent)  +
                 ")" ;
      return P;
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {

       org.apache.bcel.generic.InstructionList il =
          mg.getInstructionList();
       org.apache.bcel.generic.ConstantPoolGen cp =
          cg.getConstantPool();

       int index = cp.addClass(type);
       org.apache.bcel.generic.NEW n =
          new org.apache.bcel.generic.NEW(index);
       il.append(n);
       il.append(org.apache.bcel.generic.InstructionConstants.DUP);

       String sig = "(";
       java.util.Iterator iter = args.iterator();
       while (iter.hasNext()) {
          sandmark.util.javagen.Expression s = (sandmark.util.javagen.Expression) iter.next();
          s.toByteCode(cg,mg);
          sig += s.getSig();
       };
       sig += ")V";

       int constructorRef = cp.addMethodref(type, "<init>", sig);
       org.apache.bcel.generic.INVOKESPECIAL s =
          new org.apache.bcel.generic.INVOKESPECIAL(constructorRef);
       il.append(s);
   }

  public void toCode(
  	      sandmark.program.Class cg,
          sandmark.program.Method mg) {

          org.apache.bcel.generic.InstructionList il =
             mg.getInstructionList();
          org.apache.bcel.generic.ConstantPoolGen cp =
             cg.getConstantPool();

          int index = cp.addClass(type);
          org.apache.bcel.generic.NEW n =
             new org.apache.bcel.generic.NEW(index);
          il.append(n);
          il.append(org.apache.bcel.generic.InstructionConstants.DUP);

          String sig = "(";
          java.util.Iterator iter = args.iterator();
          while (iter.hasNext()) {
             sandmark.util.javagen.Expression s = (sandmark.util.javagen.Expression) iter.next();
             s.toCode(cg,mg);
             sig += s.getSig();
          };
          sig += ")V";

          int constructorRef = cp.addMethodref(type, "<init>", sig);
          org.apache.bcel.generic.INVOKESPECIAL s =
             new org.apache.bcel.generic.INVOKESPECIAL(constructorRef);
          il.append(s);
      }

}



