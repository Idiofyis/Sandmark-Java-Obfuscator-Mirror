package sandmark.util.javagen;

public class Method extends sandmark.util.javagen.Java {
   String name;
   String type;
   String[] attributes;
   sandmark.util.javagen.List formals;
   sandmark.util.javagen.List stats;

   public Method (String name,
                  String type,
                  String[] attributes,
                  sandmark.util.javagen.List formals,
                  sandmark.util.javagen.List stats){
      this.name = name;
      this.type = type;
      this.attributes = attributes;
      this.formals = formals;
      this.stats = stats;
   }

   public String toString(String indent)  {
      String P = outlineComment();
      P += indent  +
           renderListTerminate(attributes, " ", "")  +
           type  +  " "  +  name  +   " (" ;
      P += renderListSeparate(formals, ", ", "");
      P += ") {\n";
      P += renderStats(stats, indent);
      P += indent  +  "}\n";
      return P;
   }

   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg)  {
      int access_flags =
         sandmark.util.javagen.Java.accessFlagsToByteCode(attributes);
      org.apache.bcel.generic.Type return_type =
         sandmark.util.javagen.Java.typeToByteCode(type);
      org.apache.bcel.generic.Type[] arg_types = org.apache.bcel.generic.Type.NO_ARGS;
      String[] arg_names = {};
      String method_name = name;
      String class_name = cg.getClassName();
      org.apache.bcel.generic.InstructionList il =
         new org.apache.bcel.generic.InstructionList();
      org.apache.bcel.generic.ConstantPoolGen cp = cg.getConstantPool();

      System.out.println("Method.toByteCode: " + method_name);
      org.apache.bcel.generic.MethodGen mg =
         new org.apache.bcel.generic.MethodGen(
            access_flags, return_type,  arg_types,
	    arg_names, method_name, class_name, il, cp);

      java.util.Iterator fiter = formals.iterator();
      while (fiter.hasNext()) {
          sandmark.util.javagen.Formal f = (sandmark.util.javagen.Formal) fiter.next();
          f.toByteCode(cg,mg);
      };

      java.util.Iterator siter = stats.iterator();
      while (siter.hasNext()) {
          sandmark.util.javagen.Statement s = (sandmark.util.javagen.Statement) siter.next();
          s.toByteCode(cg,mg);
      };

      // il.append(org.apache.bcel.generic.InstructionConstants.RETURN);

      mg.setMaxStack();
      cg.addMethod(mg.getMethod());
   }



   public void toCode(
      sandmark.program.Class cl)  {
      int access_flags =
         sandmark.util.javagen.Java.accessFlagsToByteCode(attributes);
      org.apache.bcel.generic.Type return_type =
         sandmark.util.javagen.Java.typeToByteCode(type);
      org.apache.bcel.generic.Type[] arg_types = org.apache.bcel.generic.Type.NO_ARGS;
      String[] arg_names = {};
      String method_name = name;
      org.apache.bcel.generic.InstructionList il =
          new org.apache.bcel.generic.InstructionList();
      org.apache.bcel.generic.ConstantPoolGen cp = cl.getConstantPool();

      sandmark.program.LocalMethod lm= new sandmark.program.LocalMethod(cl,
         access_flags,return_type,arg_types,arg_names,method_name,il);

      java.util.Iterator fiter = formals.iterator();
      while (fiter.hasNext()) {
          sandmark.util.javagen.Formal f = (sandmark.util.javagen.Formal) fiter.next();
          f.toCode(cl,lm);
      };

      java.util.Iterator siter = stats.iterator();
      while (siter.hasNext()) {
          sandmark.util.javagen.Statement s = (sandmark.util.javagen.Statement) siter.next();
          s.toCode(cl,lm);
      };

      //il.append(org.apache.bcel.generic.InstructionConstants.RETURN);
      lm.setMaxStack();
      lm.mark();
   }
}




