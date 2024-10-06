package sandmark.watermark.ct.encode.ir;

/* 
 * Superclass for intermediate code instructions that load and
 * store nodes from global storage.
 */
public class NodeStorage extends IR {
   public sandmark.util.newgraph.MutableGraph subGraph;
   public sandmark.util.newgraph.Node node;
   public sandmark.watermark.ct.encode.storage.NodeStorage location;

   public NodeStorage (sandmark.util.newgraph.MutableGraph graph, 
                    sandmark.util.newgraph.MutableGraph subGraph, 
                    sandmark.util.newgraph.Node node, 
                    sandmark.watermark.ct.encode.storage.NodeStorage location){
      this.graph = graph;
      this.subGraph = subGraph;
      this.node = node;
      this.location = location;
   }
}

