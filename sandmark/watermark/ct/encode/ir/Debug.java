package sandmark.watermark.ct.encode.ir;

public class Debug extends IR {
   String msg;

   public Debug (sandmark.util.newgraph.MutableGraph graph, String msg){
      this.graph = graph;
      this.msg = msg;
   }

   public String toString(String indent) {
       return indent + "Debug(\"" + msg + "\")";
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new Debug(graph, msg);
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      return new sandmark.util.javagen.VirtualCall(
             new sandmark.util.javagen.StaticRef("java.lang.System", "out", "java.io.PrintStream"),
             "java.io.PrintStream", 
             "println", 
             "(Ljava/lang/String;)V", 
              new sandmark.util.javagen.LiteralString(msg)
      );
   }
}


