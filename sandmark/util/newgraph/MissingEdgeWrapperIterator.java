package sandmark.util.newgraph;

class MissingEdgeWrapperIterator extends EdgeWrapperIterator {
   private EdgeWrapper e;
   private EdgeWrapperIterator i;
   private int num;

   public MissingEdgeWrapperIterator(EdgeWrapperIterator i, EdgeWrapper e) {
      this.i = i;
      this.e = e;
      num = i.numEdges() - 1;
   }

   public EdgeWrapper getNext() {
      if (i == null)
	 return null;
      else {
	 EdgeWrapper rval;
	 do {
	    rval = i.getNext();
	 } while (rval != null && rval == e);
	 if (rval == null)
	    i = null;
	 return rval;
      }
   }

   public int numEdges() {
      return num;
   }
}
