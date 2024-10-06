package sandmark.util.newgraph;

public class StatusEdgeSet extends EdgeSet {
   private Status s;

   public StatusEdgeSet(Status s) {
      this.s = s;
   }

   public EdgeWrapperIterator iterator() {
      synchronized(s) {
	 s.setAccessed();
	 return super.iterator();
      }
   }

   public NodeWrapperIterator sourceIterator() {
      synchronized(s) {
	 s.setAccessed();
	 return super.sourceIterator();
      }
   }

   public NodeWrapperIterator sinkIterator() {
      synchronized(s) {
	 s.setAccessed();
	 return super.sinkIterator();
      }
   }
}
