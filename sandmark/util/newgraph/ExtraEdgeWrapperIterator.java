package sandmark.util.newgraph;

class ExtraEdgeWrapperIterator extends EdgeWrapperIterator {
   private EdgeWrapperIterator i;
   private EdgeWrapper n;
   private int num;

   public ExtraEdgeWrapperIterator(EdgeWrapperIterator i, EdgeWrapper n) {
      this.i = i;
      this.n = n;
      num = i.numEdges() + 1;
   }

   public EdgeWrapper getNext() {
      if (n == null)
	 return i.getNext();
      else {
	 EdgeWrapper rval = n;
	 n = null;
	 return rval;
      }
   }

   public int numEdges() {
      return num;
   }
}
