package sandmark.watermark.ct.trace.callforest;

public class Edge extends sandmark.util.newgraph.LabeledEdge {

   public static final int MISSING_weight  = -1;
   private int weight = MISSING_weight;

   public Edge (
      sandmark.watermark.ct.trace.callforest.Node from, 
      sandmark.watermark.ct.trace.callforest.Node to, 
      String label,
      int weight) {
      super(from, to, label);
      this.weight = weight;
    }

/**
 * Format the data in an easy to parse form.
 */  
   public String toString() {
      sandmark.util.newgraph.Node source =
	 (sandmark.util.newgraph.Node)sourceNode();
      sandmark.util.newgraph.Node sink =
	 (sandmark.util.newgraph.Node)sinkNode();
      return "edge(" + source.nodeNumber() + "," + 
	 sink.nodeNumber() + "," + 
// 	 theEdgeNumber + "," +
	 weight + ")";
   }

   /**
    * Return the weight of this edge. 
    */
   protected int getWeight() {
      return weight;
   }

   /**
    * Set the weight of this edge. 
    */
   protected void setWeight(int weight) {
      this.weight = weight;
   }

}

