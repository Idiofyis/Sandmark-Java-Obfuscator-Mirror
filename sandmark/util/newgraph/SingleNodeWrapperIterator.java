package sandmark.util.newgraph;

class SingleNodeWrapperIterator extends NodeWrapperIterator {
   private NodeWrapper nw;

   public SingleNodeWrapperIterator(NodeWrapper nw) {
      this.nw = nw;
   }

   public NodeWrapper getNext() {
      NodeWrapper rval = nw;
      nw = null;
      return rval;
   }
}
