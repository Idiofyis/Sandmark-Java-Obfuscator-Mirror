package sandmark.watermark.ct.encode.ir;

public class Create extends Method {
   public sandmark.util.newgraph.MutableGraph subGraph;

   public Create (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.util.newgraph.MutableGraph subGraph, 
      sandmark.watermark.ct.encode.ir.List ops){
      this.graph = graph;
      this.subGraph = subGraph;
      this.ops = ops;
      this.formals = new sandmark.watermark.ct.encode.ir.List();
   }

   public Create (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.util.newgraph.MutableGraph subGraph, 
      sandmark.watermark.ct.encode.ir.List ops,
      sandmark.watermark.ct.encode.ir.List formals){
      this.graph = graph;
      this.subGraph = subGraph;
      this.ops = ops;
      this.formals = formals;
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new Create(graph, subGraph, 
             (sandmark.watermark.ct.encode.ir.List) ops.copy(), 
             (sandmark.watermark.ct.encode.ir.List) formals.copy());
   }

   public String toString(String indent) {
      return indent + 
             "Create(" + subGraph.hashCode() + "):\n" + 
             renderOps(formals, indent) +
             renderOps(ops, indent);
   }

   public String name() {
      return "Create_" + subGraph.hashCode();
   }
}


