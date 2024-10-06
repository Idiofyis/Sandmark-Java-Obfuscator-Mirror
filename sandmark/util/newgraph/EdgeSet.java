package sandmark.util.newgraph;

class EdgeSet {
   private EdgeWrapper edgeArray[];
   private int curr;
   private static final int START_SIZE = 4;
   
   public EdgeSet() {
      edgeArray = new EdgeWrapper[START_SIZE];
      curr = 0;
   }
   
   public void addEdge(EdgeWrapper e) {
      if (curr == edgeArray.length) {
	 EdgeWrapper tmp[] = new EdgeWrapper[2*edgeArray.length];
	 System.arraycopy(edgeArray, 0, tmp, 0, curr);
	 tmp[curr++] = e;
	 edgeArray = tmp;
      }
      else {
	 edgeArray[curr++] = e;
      }
   }
   
   public int size() {
      return curr;
   }
   
   public EdgeWrapperIterator iterator() {
      return new EdgeWrapperIterator() {
	    private int i = 0;
	    
	    public EdgeWrapper getNext() {
	       if (i >= edgeArray.length)
		  return null;
	       else
		  return edgeArray[i++];
	    }

	    public int numEdges() {
	       return size();
	    }
	 };
   }

   private NodeWrapper sourceList[];
   private NodeWrapper sinkList[];

   private class WrapperListIterator extends NodeWrapperIterator {
      private NodeWrapper list[];
      private int curr;
      
      public WrapperListIterator(NodeWrapper list[]) {
	 this.list = list;
	 curr = 0;
      }
      
      public NodeWrapper getNext() {
	 if (curr < 0)
	    return null;
	 else if (curr >= list.length) {
	    curr = -1;
	    return null;
	 }
	 else {
	    NodeWrapper rval = list[curr++];
	    if (rval == null)
	       curr = -1;
	    return rval;
	 }
      }
   }

   public NodeWrapperIterator sourceIterator() {
      if (sourceList == null) {
	 sourceList = new NodeWrapper[curr];
	 int sourceCurr = 0;
	 for (int i = 0; i < curr; i++) {
	    EdgeWrapper ew = edgeArray[i];
	    if (ew.from.mySlot == 0) {
	       ew.from.mySlot = 1;
	       sourceList[sourceCurr++] = ew.from;
	    }
	 }
	 for (int i = 0; i < sourceCurr; i++)
	    sourceList[i].mySlot = 0;
      }
      return new WrapperListIterator(sourceList);
   }

   public NodeWrapperIterator sinkIterator() {
      if (sinkList == null) {
	 sinkList = new NodeWrapper[curr];
	 int sinkCurr = 0;
	 for (int i = 0; i < curr; i++) {
	    EdgeWrapper ew = edgeArray[i];
	    if (ew.to.mySlot == 0) {
	       ew.to.mySlot = 1;
	       sinkList[sinkCurr++] = ew.to;
	    }
	 }
	 for (int i = 0; i < sinkCurr; i++)
	    sinkList[i].mySlot = 0;
      }
      return new WrapperListIterator(sinkList);
   }
}   
