package sandmark.watermark.ct.encode.ir;

public class SaveNode extends NodeStorage {

   public SaveNode (sandmark.util.newgraph.MutableGraph graph, 
                    sandmark.util.newgraph.MutableGraph subGraph, 
                    sandmark.util.newgraph.Node node, 
                    sandmark.watermark.ct.encode.storage.NodeStorage location){
        super(graph, subGraph, node, location);
  }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new SaveNode(graph, subGraph, node, location);
   }

   public String toString(String indent) {
      return indent + 
             "SaveNode("  +  
             node.name() +  
             ", "  +  
             subGraph  +  
             ", '"  + location + "')";
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      sandmark.util.javagen.Java stat = location.toJavaStore(props);
      stat.setComment(toString());
      return stat;
   }
}

