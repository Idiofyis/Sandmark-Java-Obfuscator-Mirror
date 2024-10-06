package sandmark.birthmark.wpp;

public class DAGEdge extends sandmark.util.newgraph.EdgeImpl
                      implements java.util.Comparator {

   private int edgeCount;

   public DAGEdge(DAGNode source, DAGNode sink){
      super(source, sink);
      edgeCount = 1;
   }

   public int getEdgeCount(){
      return edgeCount;
   }

   public void increaseEdgeCount(){
      edgeCount++;
   }

   public boolean equals(java.lang.Object o){
      DAGEdge e = (DAGEdge)o;
      if(((DAGNode)this.sourceNode()).equals(e.sourceNode()) &&
         ((DAGNode)this.sinkNode()).equals(e.sinkNode()))
         return true;
      else
         return false;
   }

   public int compare(java.lang.Object o1, java.lang.Object o2){
      DAGEdge e1 = (DAGEdge)o1;
      DAGEdge e2 = (DAGEdge)o2;
      if(e1.sourceNode().equals(e2.sourceNode()) &&
         e1.sinkNode().equals(e2.sinkNode()))
         return 0;
      else
         return -1;
   }

   public String toString(){
      return (DAGNode)this.sourceNode() + ", " + (DAGNode)this.sinkNode() + ", "
         + this.getEdgeCount();
   }
}     
