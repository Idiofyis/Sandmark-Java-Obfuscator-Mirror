package sandmark.wizard.modeling.wmdag;

/**
   A WMDAGNode represents a node in a directed acyclic graph which models the
   dependency relationships between program transformations and how they affect
   the order in which the transformations can be applied.
   @author Ginger Myles
*/

public class WMDAGNode extends sandmark.util.newgraph.Node {

   private sandmark.Algorithm myAlg;
   private int myLevel;
   private java.util.ArrayList currentPostprohibits;

   /**
      Constructs a WM DAG Node with a given label, level, and set of post
      prohibitions.
   */
   public WMDAGNode(sandmark.Algorithm alg, int level, 
                    java.util.ArrayList postProhibits){
      myAlg = alg;
      myLevel = level;
      currentPostprohibits = postProhibits;
   }

   public sandmark.Algorithm getAlg(){
      return myAlg;
   }

   public int getLevel(){
      return myLevel;
   }

   public java.util.ArrayList getPostprohibits(){
      return currentPostprohibits;
   }

   public boolean equals(java.lang.Object o){
      WMDAGNode node = (WMDAGNode)o;
      return myAlg == node.myAlg && myLevel == node.myLevel &&
         currentPostprohibits.equals(node.currentPostprohibits);
   }
   
   public int hashCode() {
      return (myAlg == null ? 0 : myAlg.hashCode()) + 
         myLevel + currentPostprohibits.hashCode(); 
   }
}
