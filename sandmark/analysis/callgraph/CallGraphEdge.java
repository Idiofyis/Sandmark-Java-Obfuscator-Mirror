package sandmark.analysis.callgraph;

/** This is an edge class used by CallGraph.
 *  In addition to storing a source and sink node, 
 *  it stores a set of instruction handles. 
 *  See the description of CallGraph for details.
 */
public class CallGraphEdge implements sandmark.util.newgraph.Edge{
   private Object sourceNode, sinkNode;
   private java.util.HashSet handles;

   public CallGraphEdge(CallGraphEdge edge){
      sourceNode = edge.sourceNode;
      sinkNode = edge.sinkNode;
      handles = new java.util.HashSet(edge.handles);
   }

   public CallGraphEdge(Object source, Object sink){
      sourceNode = source;
      sinkNode = sink;
      handles = new java.util.HashSet();
   }

   public Object sourceNode(){
      return sourceNode;
   }

   public Object sinkNode(){
      return sinkNode;
   }

   public void addHandle(org.apache.bcel.generic.InstructionHandle ih){
      handles.add(ih);
   }

   public java.util.Set getHandles(){
      return handles;
   }

   public sandmark.util.newgraph.Edge clone(Object source,Object sink) 
      throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }
}
