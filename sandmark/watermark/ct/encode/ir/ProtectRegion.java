package sandmark.watermark.ct.encode.ir;

public class ProtectRegion extends sandmark.watermark.ct.encode.ir.IR {
   public sandmark.watermark.ct.encode.ir.List ops;

   public ProtectRegion(sandmark.watermark.ct.encode.ir.List ops){
      this.graph = null;
      this.ops = ops;
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new ProtectRegion((sandmark.watermark.ct.encode.ir.List)ops.copy());
   }

   public String toString(String indent) {
       return indent + "protect(\n" + renderOps(ops, indent + "   ") + indent + ")";
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      sandmark.util.javagen.List body = new sandmark.util.javagen.List();
      java.util.Iterator iter = ops.iterator();
      while (iter.hasNext()) {
         sandmark.watermark.ct.encode.ir.IR f = (sandmark.watermark.ct.encode.ir.IR) iter.next();
         sandmark.util.javagen.Java F = f.toJava(props);
         body.cons(F);
      }
      sandmark.util.javagen.List Catch = new sandmark.util.javagen.List();
      sandmark.util.javagen.Try stat = 
	  new sandmark.util.javagen.Try(body, "java.lang.Exception", Catch);
      // stat.setComment(toString());
      return stat;
   }
}


