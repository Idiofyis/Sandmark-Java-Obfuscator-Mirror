package sandmark.watermark.ct.encode.ir;

public class Fixup extends Method {
   public sandmark.util.newgraph.MutableGraph subGraph1;
   public sandmark.util.newgraph.MutableGraph subGraph2;

   public Fixup (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.util.newgraph.MutableGraph subGraph1, 
      sandmark.util.newgraph.MutableGraph subGraph2, 
      sandmark.watermark.ct.encode.ir.List ops){
      this.graph = graph;
      this.subGraph1 = subGraph1;
      this.subGraph2 = subGraph2;
      this.ops = ops;
      this.formals = new sandmark.watermark.ct.encode.ir.List();
   }

   public Fixup (
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.util.newgraph.MutableGraph subGraph1, 
      sandmark.util.newgraph.MutableGraph subGraph2, 
      sandmark.watermark.ct.encode.ir.List ops, 
      sandmark.watermark.ct.encode.ir.List formals){
      this.graph = graph;
      this.subGraph1 = subGraph1;
      this.subGraph2 = subGraph2;
      this.ops = ops;
      this.formals = formals;
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new Fixup(graph, subGraph1, subGraph2, 
                       (sandmark.watermark.ct.encode.ir.List) ops.copy(), 
                       (sandmark.watermark.ct.encode.ir.List) formals.copy());
   }

   public String name() {
      return "Fixup_" + subGraph1 + "_" + subGraph1;
   }

   public String toString(String indent) {
      return indent + 
             "Fixup("  +
             subGraph1  +  ", "  +
             subGraph2 +  "):\n" + 
             renderOps(formals, indent) +
             renderOps(ops, indent);

   }
}


