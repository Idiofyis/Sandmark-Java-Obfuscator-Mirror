package sandmark.birthmark.wpp;

/**
   
   @author Ginger Myles
*/

public class DAGNode extends sandmark.util.newgraph.Node
                     implements java.util.Comparator {

   private String myLabel = "";

   public DAGNode(){}
   /**
      Constructs a DAG Node with a given label.
   */
   public DAGNode(String label){

      myLabel = label;
   }

   public String getLabel(){
      return myLabel;
   }

   public boolean equals(java.lang.Object o){
      DAGNode node = (DAGNode)o;
      if(this.getLabel().equals(node.getLabel()))
         return true;
      else
         return false;
   }

   public int compare(java.lang.Object o1, java.lang.Object o2){
      DAGNode n1 = (DAGNode)o1;
      DAGNode n2 = (DAGNode)o2;
      return n1.getLabel().compareTo(n2.getLabel());
   }

   public String toString(){
      return this.getLabel();
   }

}
