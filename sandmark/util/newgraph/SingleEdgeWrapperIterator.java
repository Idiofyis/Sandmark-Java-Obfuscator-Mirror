package sandmark.util.newgraph;

class SingleEdgeWrapperIterator extends EdgeWrapperIterator {
   private EdgeWrapper ew;

   public SingleEdgeWrapperIterator(EdgeWrapper w) {
      ew = w;
   }

   public EdgeWrapper getNext() {
      EdgeWrapper rval = ew;
      ew = null;
      return rval;
   }

   public int numEdges() {
      return 1;
   }
}
