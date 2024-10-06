package sandmark.util.newgraph;

class DoubleNodeWrapperIterator extends NodeWrapperIterator {
   DoubleNodeWrapperIterator(NodeWrapperIterator a, java.util.Iterator b) {
      inner = a;
      extra = b;
   }

   private NodeWrapperIterator inner;
   private java.util.Iterator extra;

   public NodeWrapper getNext() {
      if (inner != null) {
	 NodeWrapper rval = inner.getNext();
	 if (rval == null)
	    inner = null;
	 else
	    return rval;
      }
      
      if (extra != null) {
	 if (extra.hasNext())
	    return (NodeWrapper)extra.next();
	 else
	    extra = null;
      }

      return null;
   }
}
