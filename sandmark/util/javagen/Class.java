package sandmark.util.javagen;

public class Class extends sandmark.util.javagen.Java {
   String parent;
   String name;
   String Package;
   sandmark.util.javagen.List fields;
   sandmark.util.javagen.List methods;
   String[] attributes = {"public","super"};
   static sandmark.util.javagen.List staticStats = new sandmark.util.javagen.List();

   public Class (String parent,
                 String name,
                 sandmark.util.javagen.List fields,
                 sandmark.util.javagen.List methods){
      this.Package = "";
      this.parent = parent;
      this.name = name;
      this.fields = fields;
      this.methods = methods;
   }

   public Class (String parent,
                 String name,
                 String Package,
                 sandmark.util.javagen.List fields,
                 sandmark.util.javagen.List methods){
      this.Package = Package;
      this.parent = parent;
      this.name = name;
      this.fields = fields;
      this.methods = methods;
   }

   public static void addStaticStat(sandmark.util.javagen.Statement stat) {
      staticStats.cons(stat);
   }

   public sandmark.util.javagen.Method createStaticMethod() {
      sandmark.util.javagen.List formals = new sandmark.util.javagen.List();
      String[] attributes = {"static"};
      staticStats.cons(new sandmark.util.javagen.Return());
      sandmark.util.javagen.Method M =
         new sandmark.util.javagen.Method(
	    "<clinit>", "void", attributes, formals, staticStats);
      return M;
   }

   public String toString(String indent)  {
      String P = outlineComment();
      if (!Package.equals(""))
	  P += "package " + Package + ";\n";
      P += "public class " + name + " extends " + parent + " {\n";
      P += renderListTerminate(fields, ";\n", indent + "  ") + "\n";
      P += renderListTerminate(methods, "\n", indent + "  ");
      P += "   static {\n";
      P += renderStats(staticStats, indent + "  ");
      P += "   }\n";
      P += "}\n";
      return P;
  }

  public org.apache.bcel.generic.ClassGen toByteCode() {
      int access_flags = sandmark.util.javagen.Java.accessFlagsToByteCode(attributes);
      String class_name = "";
      if (Package.equals(""))
	  class_name = name;
      else
	  class_name = Package + "." + name;
      String super_class_name = parent;
      String file_name = name + ".java";
      String[] interfaces = {};

      org.apache.bcel.generic.ClassGen cg =
         new org.apache.bcel.generic.ClassGen(
            class_name, super_class_name, file_name,
            access_flags, interfaces);

      java.util.Iterator fiter = fields.iterator();
      while (fiter.hasNext()) {
          sandmark.util.javagen.Field f = (sandmark.util.javagen.Field) fiter.next();
          f.toByteCode(cg);
      };

      java.util.Iterator miter = methods.iterator();
      while (miter.hasNext()) {
          sandmark.util.javagen.Method m = (sandmark.util.javagen.Method) miter.next();
          m.toByteCode(cg);
      };
      createStaticMethod().toByteCode(cg);

      cg.addEmptyConstructor(org.apache.bcel.Constants.ACC_PUBLIC);
      return cg;
   }

 public sandmark.program.Class toCode(sandmark.program.Application app)
 {
	int access_flags = sandmark.util.javagen.Java.accessFlagsToByteCode(attributes);
	String class_name = "";
	if (Package.equals(""))
	class_name = name;
	else
	class_name = Package + "." + name;
	String super_class_name = parent;
	String file_name = name + ".java";
	int[] interfaces = {};
	org.apache.bcel.classfile.Field[] field={};
	org.apache.bcel.classfile.Method[] method={};
	org.apache.bcel.classfile.Attribute[] attribute={};


	int class_index;
	int super_class_index;

	org.apache.bcel.generic.ConstantPoolGen cpg=
				new org.apache.bcel.generic.ConstantPoolGen();
				class_index=cpg.addClass(class_name);
				super_class_index=cpg.addClass(super_class_name);

	/*org.apache.bcel.generic.ClassGen cg =
	         new org.apache.bcel.generic.ClassGen(
	            class_name, super_class_name, file_name,class_name,
	            access_flags, interfaces);
	sandmark.program.LocalClass cl = new sandmark.program.LocalClass(app,cg.getJavaClass());*/

	org.apache.bcel.classfile.JavaClass jc=
	 new org.apache.bcel.classfile.JavaClass(class_index,super_class_index,
	 					file_name,45,3,access_flags, cpg.getFinalConstantPool(),interfaces,field,method,attribute);
	sandmark.program.LocalClass cl = new sandmark.program.LocalClass(app,jc);

	java.util.Iterator fiter = fields.iterator();
	      while (fiter.hasNext()) {
	          sandmark.util.javagen.Field f = (sandmark.util.javagen.Field) fiter.next();
	          f.toCode(cl);
	      };

	      java.util.Iterator miter = methods.iterator();
	      while (miter.hasNext()) {
	          sandmark.util.javagen.Method m = (sandmark.util.javagen.Method) miter.next();
	          m.toCode(cl);
	      };
	      createStaticMethod().toCode(cl);

      cl.addEmptyConstructor(org.apache.bcel.Constants.ACC_PUBLIC);

	return cl;

 }

}

