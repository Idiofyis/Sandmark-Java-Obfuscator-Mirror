package sandmark.watermark.ct.encode.ir;

public class CreateNode extends IR {
   public sandmark.util.newgraph.MutableGraph subGraph; 
   public sandmark.util.newgraph.Node node;

   public CreateNode(
      sandmark.util.newgraph.MutableGraph graph, 
      sandmark.util.newgraph.MutableGraph subGraph, 
      sandmark.util.newgraph.Node node){
      this.graph = graph;
      this.subGraph = subGraph;
      this.node = node;
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new CreateNode(graph, subGraph, node);
   }

   public String toString(String indent) {
      return indent + 
             node.name() + 
             " = CreateNode("  +  
             subGraph  +  ")";
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      String nodeName = node.name();
      String nodeType = props.getProperty("Node Class");

      sandmark.util.javagen.Expression New = 
         new sandmark.util.javagen.New(nodeType);
      sandmark.util.javagen.Java local =
        new sandmark.util.javagen.Local(nodeName, nodeType, New);
      local.setComment(toString());
      return local;
   }
}


