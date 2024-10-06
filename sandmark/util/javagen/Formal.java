package sandmark.util.javagen;

public class Formal extends sandmark.util.javagen.Java {
   String name;
   String type;
   public Formal (String name, String type){
      this.name = name;
      this.type = type;
   }

   public String toString(String indent)  {
      String P = type.toString()  +  " "  +  name.toString();
      return P;
   }

   public void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg) {
       org.apache.bcel.generic.Type[] types = mg.getArgumentTypes();
       org.apache.bcel.generic.Type[] types1 = new org.apache.bcel.generic.Type[types.length+1];

       String[] names = mg.getArgumentNames();
       String[] names1 = new String[names.length+1];

       for(int i=0; i<names.length; i++) {
	   types1[i] = types[i];
	   names1[i] = names[i];
       }

       org.apache.bcel.generic.Type Type =
           sandmark.util.javagen.Java.typeToByteCode(type);

       types1[names.length] = Type;
       names1[names.length] = name;

       mg.setArgumentTypes(types1);
       mg.setArgumentNames(names1);

       org.apache.bcel.generic.LocalVariableGen lg =
	   mg.addLocalVariable(name, Type, null, null);

       //  mg.setMaxLocals(mg.getMaxLocals()+1); UNTESTED
   }

   public void toCode(
          sandmark.program.Class cl,
          sandmark.program.Method mg) {
          org.apache.bcel.generic.Type[] types = mg.getArgumentTypes();
          org.apache.bcel.generic.Type[] types1 = new org.apache.bcel.generic.Type[types.length+1];

          String[] names = mg.getArgumentNames();
          String[] names1 = new String[names.length+1];

          //System.out.println("Formal.toCode:mg=" + mg.getName());
          //System.out.println("Formal.toCode:0:" + mg.getMaxLocals());
          for(int i=0; i<names.length; i++) {
             //System.out.println("Formal.toCode:1:" + names[i]);
   		   types1[i] = types[i];
   		   names1[i] = names[i];
          }

          org.apache.bcel.generic.Type Type =
              sandmark.util.javagen.Java.typeToByteCode(type);

          types1[names.length] = Type;
          names1[names.length] = name;

          mg.setArgumentTypes(types1);
          mg.setArgumentNames(names1);

          org.apache.bcel.generic.LocalVariableGen lg =
   	   mg.addLocalVariable(name, Type, null, null);

          String[] namesX = mg.getArgumentNames();

          //for(int i=0; i<namesX.length; i++) {
          //         System.out.println("Formal.toCode:2:" + namesX[i]);
          //}
          //System.out.println("Formal.toCode:3:" + mg.getMaxLocals());

          //  mg.setMaxLocals(mg.getMaxLocals()+1); UNTESTED
   }
}



