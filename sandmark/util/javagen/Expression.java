package sandmark.util.javagen;

public abstract class Expression extends sandmark.util.javagen.Java {
   String type;

   public String getType() {
       return type;
   }

   public String getSig() {
      return org.apache.bcel.classfile.Utility.getSignature(type);
   }

   public abstract void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg);

   public abstract void toCode(
	      sandmark.program.Class cg,
          sandmark.program.Method mg) ;
}

