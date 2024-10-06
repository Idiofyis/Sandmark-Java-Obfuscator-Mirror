package sandmark.watermark.ct.encode.ir;

abstract public class Method extends IR {
   public sandmark.watermark.ct.encode.ir.List ops;
   public sandmark.watermark.ct.encode.ir.List formals;
   public String returnType = "void";

   public Method (){}

   public abstract String name();

   public void setFormals(sandmark.watermark.ct.encode.ir.List formals) {
      this.formals = formals;
   }

   public sandmark.watermark.ct.encode.ir.List getFormals() {
      return formals;
   }
    /*
   public String signature() {
      String[] argv = new String[formals.size()];
      int i = 0;
      java.util.Iterator fiter = formals.iterator();
      while (fiter.hasNext()) {
         sandmark.watermark.ct.encode.ir.Formal f = (sandmark.watermark.ct.encode.ir.Formal) fiter.next();
         argv[i++] = org.apache.bcel.classfile.Utility.getSignature(f.type);
      }
      String sig = "";
      try {
        sig = org.apache.bcel.classfile.Utility.methodTypeToSignature(returnType, argv);
      } catch (Exception e) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }
      return sig;
   }
    */

    public String signature() {
      String sig = "(";
      java.util.Iterator fiter = formals.iterator();
      while (fiter.hasNext()) {
         sandmark.watermark.ct.encode.ir.Formal f = (sandmark.watermark.ct.encode.ir.Formal) fiter.next();
         sig += org.apache.bcel.classfile.Utility.getSignature(f.type);
      }
      sig += ")" + org.apache.bcel.classfile.Utility.getSignature(returnType);
      return sig;
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
     String[] attributes = {"public","static"};

      sandmark.util.javagen.List args = new sandmark.util.javagen.List();
      java.util.Iterator fiter = formals.iterator();
      while (fiter.hasNext()) {
         sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) fiter.next();
         sandmark.util.javagen.Java F = f.toJava(props);
         args.cons(F);
      }

      sandmark.util.javagen.List body = new sandmark.util.javagen.List();
      java.util.Iterator oiter = ops.iterator();
      while (oiter.hasNext()) {
         sandmark.watermark.ct.encode.ir.IR o = (sandmark.watermark.ct.encode.ir.IR) oiter.next();
         sandmark.util.javagen.Java O = o.toJava(props);
         body.cons(O);
      }

      sandmark.util.javagen.Return ret = new sandmark.util.javagen.Return();
      body.cons(ret);

      sandmark.util.javagen.Method method =
        new sandmark.util.javagen.Method (name(), "void", attributes, args, body);

       return method;
   }
}


