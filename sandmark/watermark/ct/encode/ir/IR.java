
package sandmark.watermark.ct.encode.ir;

/**
   Base-class for intermediate code statements. Each class has a 'toJava'
   method that converts the IR statement to a sandmark.util.javagen.*
   object.
*/

public class IR implements Cloneable {
   public sandmark.util.newgraph.MutableGraph graph;

protected String renderListSeparate (
   sandmark.watermark.ct.encode.ir.List L, String separator, String indent)  {
   String P = "";
   java.util.Iterator iter = L.iterator();
   while (iter.hasNext()) {
      sandmark.watermark.ct.encode.ir.IR s = (sandmark.watermark.ct.encode.ir.IR) iter.next();
      P += s.toString(indent);
      if (iter.hasNext())
	  P += separator;
   };
   return P;
}

static String renderOps(sandmark.watermark.ct.encode.ir.List L, String indent)  {
   String P = "";
   java.util.Iterator iter = L.iterator();
   while (iter.hasNext()) {
       sandmark.watermark.ct.encode.ir.IR r = (sandmark.watermark.ct.encode.ir.IR) iter.next();
       P += r.toString(indent) + "\n";
   }
   return P;
}

   public sandmark.watermark.ct.encode.ir.IR copy() {
      sandmark.watermark.ct.encode.ir.IR New = null;
      try {
         New = (sandmark.watermark.ct.encode.ir.IR) clone();
      } catch (Exception e) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }
      return New;
   }

   public String toString(String indent) {
      return  "";
   }

   public String toString() {
      return  toString("");
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      return null;
   }
}

