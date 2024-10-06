package sandmark.util.newgraph;

class EdgeIteratorWrapper extends EdgeWrapperIterator {
   private java.util.Iterator i;
   private int num;

   public EdgeIteratorWrapper(java.util.Iterator i, int num) {
      this.i = i;
      this.num = num;
   }

   public EdgeWrapper getNext() {
      if (i == null)
	 return null;
      else {
	 EdgeWrapper rval = null;
	 if (i.hasNext())
	    rval = (EdgeWrapper)i.next();
	 if (rval == null)
	    i = null;
	 return rval;
      }
   }

   public int numEdges() {
      return num;
   }
}
