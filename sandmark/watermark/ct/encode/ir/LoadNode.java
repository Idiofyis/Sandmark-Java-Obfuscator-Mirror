package sandmark.watermark.ct.encode.ir;

public class LoadNode extends NodeStorage {

   public LoadNode (sandmark.util.newgraph.MutableGraph graph, 
                    sandmark.util.newgraph.MutableGraph subGraph, 
                    sandmark.util.newgraph.Node node, 
                    sandmark.watermark.ct.encode.storage.NodeStorage location){
       super(graph, subGraph, node, location);
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new LoadNode(graph, subGraph, node, location);
   }

   public String toString(String indent) {
      return indent +  
             node.name()  +  
             " = LoadNode("  + 
             graph  +  
             ", '"  +  location  +  "')";
   }

   public sandmark.util.javagen.Java toJava(sandmark.util.ConfigProperties props) {
      sandmark.util.javagen.Java stat = location.toJavaLoad(props);
      stat.setComment(toString());
      return stat;
   }
}


