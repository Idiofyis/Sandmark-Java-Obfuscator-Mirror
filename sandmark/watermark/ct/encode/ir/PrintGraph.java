package sandmark.watermark.ct.encode.ir;

public class PrintGraph extends IR {
   public PrintGraph (sandmark.util.newgraph.MutableGraph graph){
      this.graph = graph;
   }

   public String toString(String indent) {
       return indent + "PrintGraph()";
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new PrintGraph(graph);
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
//       String S = graph.toString("        ");
      String S = "blah blah blah";
      return new sandmark.util.javagen.Comment(S);
   }
}


