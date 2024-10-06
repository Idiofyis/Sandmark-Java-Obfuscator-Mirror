package sandmark.util.newgraph;

class DoubleEdgeWrapperIterator extends EdgeWrapperIterator {
   DoubleEdgeWrapperIterator(EdgeWrapperIterator a, EdgeWrapperIterator b) {
      this.a = a;
      this.b = b;
      num = a.numEdges() + b.numEdges();
   }

   private EdgeWrapperIterator a, b;
   private int num;

   public EdgeWrapper getNext() {
      if (a != null) {
	 EdgeWrapper rval = a.getNext();
	 if (rval == null)
	    a = null;
	 else
	    return rval;
      }

      if (b != null) {
	 EdgeWrapper rval = b.getNext();
	 if (rval == null)
	    b = null;
	 return rval;
      }

      return null;
   }

   public int numEdges() {
      return num;
   }
}
