package sandmark.util.javagen;

public class Field extends sandmark.util.javagen.Java {
   String name;
   String type;
   String[] attributes;
   sandmark.util.javagen.Expression init;

   public Field (
      String name,
      String type,
      String[] attributes){
      this.name = name;
      this.type = type;
      this.attributes = attributes;
      this.init = null;
   }

   public Field (
      String name,
      String type,
      String[] attributes,
      sandmark.util.javagen.Expression init){
      this.name = name;
      this.type = type;
      this.attributes = attributes;
      this.init = init;
   }

   public String toString(String indent)  {
       String P = indent  +
              renderListTerminate(attributes, " ", "")  +
              type.toString()  +  " "  +
              name.toString();
       if (init != null)
          P += " = "  + init.toString();
      return P;
   }

   public void toByteCode(
      org.apache.bcel.generic.ClassGen cg)  {
      int access_flags =
         sandmark.util.javagen.Java.accessFlagsToByteCode(attributes);
      org.apache.bcel.generic.Type field_type =
         sandmark.util.javagen.Java.typeToByteCode(type);
      //      System.out.println("Field.type=" + field_type.toString());
      org.apache.bcel.generic.ConstantPoolGen cp = cg.getConstantPool();

      org.apache.bcel.generic.FieldGen fg =
         new org.apache.bcel.generic.FieldGen(
            access_flags, field_type, name, cp);
      // System.out.println("Field.sig=" + fg.getSignature());
      // System.out.println("Field.decl=" + fg.toString());

      cg.addField(fg.getField());
   }

	public void toCode(
	      sandmark.program.Class cl)  {
	      int access_flags =
	         sandmark.util.javagen.Java.accessFlagsToByteCode(attributes);
	      org.apache.bcel.generic.Type field_type =
	         sandmark.util.javagen.Java.typeToByteCode(type);
	      //      System.out.println("Field.type=" + field_type.toString());
	     // org.apache.bcel.generic.ConstantPoolGen cp = cg.getConstantPool();

	      sandmark.program.LocalField fg =
	         new sandmark.program.LocalField(cl,
	            access_flags, field_type, name);
	      // System.out.println("Field.sig=" + fg.getSignature());
	      // System.out.println("Field.decl=" + fg.toString());

	      //cg.addField(fg.getField());
	   }

}



