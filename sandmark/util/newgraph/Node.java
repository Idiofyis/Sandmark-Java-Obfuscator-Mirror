package sandmark.util.newgraph;

public class Node implements Cloneable {
   protected int number;
  
   public Node (int number) {
      this.number = number;
   }

   public Node () {
      this.number = -1;
   }

   public java.lang.Object clone() throws CloneNotSupportedException {
      return new Node(number);
   }

   public int nodeNumber() {
      return number;
   }

   public void setNodeNumber(int number) {
      this.number = number;
   }

   public boolean equals (java.lang.Object node) {
      return (node instanceof Node) && this.number == ((Node)node).number;
   }

   public int hashCode () {
      return this.number * this.number;
   }

   public String name() {
      return "n"  +  nodeNumber();
   }

   public String toString() {
      return "" + number;
   }
}

