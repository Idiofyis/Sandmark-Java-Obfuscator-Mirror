package sandmark.watermark.ct.encode.ir;

public class Construct extends Method {
   public Construct (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.watermark.ct.encode.ir.List ops){
      this.graph = graph;
      this.ops = ops;
      this.formals = new sandmark.watermark.ct.encode.ir.List();
   }

   public Construct (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.watermark.ct.encode.ir.List ops,
      sandmark.watermark.ct.encode.ir.List formals){
      this.graph = graph;
      this.ops = ops;
      this.formals = formals;
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new Construct(graph, 
             (sandmark.watermark.ct.encode.ir.List) ops.copy(), 
             (sandmark.watermark.ct.encode.ir.List) formals.copy());
   }

   public String toString(String indent) {
      return indent + 
             "Construct():\n" + 
             renderOps(formals, indent) +
             renderOps(ops, indent);
   }

   public String name() {
      return "build";
   }
}


