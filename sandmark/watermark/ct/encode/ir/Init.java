package sandmark.watermark.ct.encode.ir;

public class Init extends Method {

   public Init (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.watermark.ct.encode.ir.List ops){
      this.graph = graph;
      this.ops = ops;
      this.formals = new sandmark.watermark.ct.encode.ir.List();
   }

   public Init (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.watermark.ct.encode.ir.List ops, 
      sandmark.watermark.ct.encode.ir.List formals){
      this.graph = graph;
      this.ops = ops;
      this.formals = formals;
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new Init(graph, 
                      (sandmark.watermark.ct.encode.ir.List) ops.copy(), 
                      (sandmark.watermark.ct.encode.ir.List) formals.copy());
   }

   public String name() {
      return "init";
   }

   public String toString(String indent) {
      return indent + 
             "Init("  +  
             graph + "):\n" +
             renderOps(formals, indent) +
             renderOps(ops, indent);
   }
}


