package sandmark.util.javagen;

public abstract class Statement extends sandmark.util.javagen.Java {
   public abstract void toByteCode(
       org.apache.bcel.generic.ClassGen cg,
       org.apache.bcel.generic.MethodGen mg);
   public abstract void toCode(
        sandmark.program.Class cl,
          sandmark.program.Method mg);
}

