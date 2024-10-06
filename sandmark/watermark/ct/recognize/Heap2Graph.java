package sandmark.watermark.ct.recognize;

/**
 * Walks the object graph starting at a particular root node
 * and builds the corresponding sandmark.util.newgraph.Graph.
 */

public class Heap2Graph {
   
   /**
    * Walks the object graph starting at the root node.
    * Construct and return the corresponding sandmark.util.newgraph.Graph.
    */
   public static sandmark.util.newgraph.Graph unpack
      (com.sun.jdi.VirtualMachine vm) {
      return unpack(new sandmark.util.exec.Heap(vm));
   }
   public static sandmark.util.newgraph.Graph unpack
      (com.sun.jdi.VirtualMachine vm,
       com.sun.jdi.ObjectReference root) {
      return unpack(new sandmark.util.exec.Heap(vm, root));
   }

   public static sandmark.util.newgraph.Graph unpack
      (com.sun.jdi.VirtualMachine vm,java.util.List roots) {
      return unpack(new sandmark.util.exec.Heap(vm,roots));
   }
   private static sandmark.util.newgraph.Graph unpack
      (sandmark.util.exec.Heap heap) {
      
      sandmark.util.newgraph.Graph graph = 
         sandmark.util.newgraph.Graphs.createGraph(null,null);
      while (heap.hasNext()) {
         sandmark.util.exec.HeapData obj = (sandmark.util.exec.HeapData) heap.next();
         Long thisNode = new Long(obj.uniqueID);
         graph = graph.addNode(thisNode);
         for(int i=0; i<obj.refs.length; i++)
            if (obj.refs[i] != sandmark.util.exec.HeapData.NULL) {
               Long succNode = new Long(obj.refs[i]);
               graph = graph.addNode(succNode).addEdge
                  (new sandmark.util.newgraph.TypedEdge(thisNode,succNode,i));
            }
      }
      return graph;
   }
}
