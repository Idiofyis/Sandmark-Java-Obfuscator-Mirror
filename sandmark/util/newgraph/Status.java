package sandmark.util.newgraph;

class Status {
   private boolean accessed;

   public Status() {
      accessed = false;
   }

   public final void checkNotAccessed() {
      if (accessed)
	 throw new java.lang.RuntimeException("graph must be immutable");
   }
   
   public final void setAccessed() {
      if (!accessed) {
	 synchronized(this) {
	    accessed = true;
	 }
      }
   }
}
