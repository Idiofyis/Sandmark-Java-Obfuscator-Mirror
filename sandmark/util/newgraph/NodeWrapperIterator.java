package sandmark.util.newgraph;

abstract class NodeWrapperIterator {
   abstract public NodeWrapper getNext();

   public final java.util.Iterator iterator() {
      return new java.util.Iterator() {
	    private NodeWrapper nextWrapper = getNext();

	    public boolean hasNext() {
	       return nextWrapper != null;
	    }

	    public java.lang.Object next() {
	       if (nextWrapper == null)
		  throw new java.util.NoSuchElementException();

	       java.lang.Object rval = nextWrapper.node;
	       nextWrapper = getNext();
	       return rval;
	    }

	    public void remove() {
	       throw new UnsupportedOperationException();
	    }
	 };
   }
}
