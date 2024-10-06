package sandmark.util.newgraph;

abstract class EdgeWrapperIterator {
   abstract public EdgeWrapper getNext();
   abstract public int numEdges();

   public final java.util.Iterator iterator() {
      return new java.util.Iterator() {
	    private EdgeWrapper nextWrapper = getNext();

	    public boolean hasNext() {
	       return nextWrapper != null;
	    }

	    public java.lang.Object next() {
	       if (nextWrapper == null)
		  throw new java.util.NoSuchElementException();

	       java.lang.Object rval = nextWrapper.edge;
	       nextWrapper = getNext();
	       return rval;
	    }

	    public void remove() {
	       throw new UnsupportedOperationException();
	    }
	 };
   }
}
