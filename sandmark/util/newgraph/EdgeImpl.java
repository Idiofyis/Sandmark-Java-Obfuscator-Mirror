package sandmark.util.newgraph;

public class EdgeImpl implements Edge {
   private java.lang.Object from, to;
   private int num;

   private static int count = 0;

   public EdgeImpl(java.lang.Object _from, java.lang.Object _to) {
      from = _from;
      to = _to;
      num = getNum();
   }

   private static synchronized int getNum() {
      return count++;
   }

   public java.lang.Object sourceNode() {
      return from;
   }

   public java.lang.Object sinkNode() {
      return to;
   }

   public int hashCode() {
      return sourceNode().hashCode() + sinkNode().hashCode() + num;
   }

   public Edge clone(Object source,Object sink) 
      throws CloneNotSupportedException {
      return new EdgeImpl(source,sink);
   }
}
