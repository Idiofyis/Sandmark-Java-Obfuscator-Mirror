package sandmark.util.newgraph;

class EdgeWrapper {
   public Edge edge;
   public NodeWrapper from, to;

   public EdgeWrapper(Edge e, NodeWrapper from, NodeWrapper to) {
      edge = e;
      this.from = from;
      this.to = to;

      if (from == null || from.node == null || to == null || to.node == null)
	 throw new java.lang.NullPointerException();
   }
}
