package sandmark.util.newgraph;

class NodeAttributes {
   public EdgeSet inEdges, outEdges;

   public NodeAttributes() {
      
   }

   private EdgeSet addEdge(EdgeWrapper e, EdgeSet s) {
      if (s == null)
	 s = createEdgeSet();
      
      s.addEdge(e);
      return s;
   }

   protected EdgeSet createEdgeSet() {
      return new EdgeSet();
   }

   public void addOutEdge(EdgeWrapper e) {
      outEdges = addEdge(e, outEdges);
   }
   
   public void addInEdge(EdgeWrapper e) {
      inEdges = addEdge(e, inEdges);
   }
}
