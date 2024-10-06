package sandmark.util.newgraph;

class ExtraNodeWrapperIterator extends NodeWrapperIterator {
   private NodeWrapperIterator i;
   private NodeWrapper n;

   public ExtraNodeWrapperIterator(NodeWrapperIterator i, NodeWrapper n) {
      this.i = i;
      this.n = n;
   }

   public NodeWrapper getNext() {
      if (n == null)
	 return i.getNext();
      else {
	 NodeWrapper rval = n;
	 n = null;
	 return rval;
      }
   }
}
