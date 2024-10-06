package sandmark.watermark.execpath;

public class TraceIndexer{
   private java.io.RandomAccessFile file;
   private java.util.Hashtable bb2OffsetList;
    private java.util.Hashtable threadToLength;
    private long traceLength;

    static class TracePoint {
	String threadname;
	String classname;
	String methodname;
	int offset;
	public TracePoint(String thread,String clazz,String method,int off) {
	    threadname = thread;
	    classname = clazz;
	    methodname = method;
	    offset = off;
	}
	public TracePoint(TraceNode node) {
	    this(node.getThreadName(),node.getClassName(),node.getMethodName(),
		 node.getOffset());
	}
	public int hashCode() {
	    return threadname.hashCode() + classname.hashCode() +
		methodname.hashCode() + offset;
	}
	public boolean equals(Object o) {
	    if(!(o instanceof TracePoint))
		return false;

	    TracePoint other = (TracePoint)o;
	    return threadname.equals(other.threadname) &&
		classname.equals(other.classname) && 
		methodname.equals(other.methodname) && offset == other.offset;
	}
	public String toString() {
	    return threadname + " " + classname + " " + methodname + " " + offset;
	}
    }
   public TraceIndexer(java.io.File traceFile) throws java.io.IOException{
      file = new java.io.RandomAccessFile(traceFile, "r");
      bb2OffsetList = new java.util.Hashtable(100);
      threadToLength = new java.util.Hashtable();
      
      String line = null;
      long fp=0;
      TraceNode node = null;
      while((line=file.readLine())!=null){
	  traceLength++;
	  if((traceLength % 1000) == 0)
	      System.out.println("read " + traceLength + " lines");
         node = new TraceNode(line, "");
         TracePoint identity = new TracePoint
	     (node.getThreadName(),node.getClassName(),
	      node.getMethodName() + node.getMethodSignature(),
	      node.getOffset());

	 Integer threadLength = (Integer)threadToLength.get
	     (node.getThreadName());
	 threadToLength.put
	     (node.getThreadName(),
	      (threadLength = new Integer
	       (threadLength == null ? 0 : (threadLength.intValue() + 1))));

         java.util.List list = (java.util.List)bb2OffsetList.get(identity);
         if (list==null){
            list = new java.util.Vector();
            bb2OffsetList.put(identity, list);
         }
         list.add(new Long(fp));

         fp = file.getFilePointer();
      }
      file.close();
   }
   
    public java.util.Hashtable getThreadLengths() {
	return threadToLength;
    }

    public long getTraceLength() { return traceLength; }
   
   public java.util.List getTracePoints(String threadName){
      java.util.Vector v = new java.util.Vector();
      for(java.util.Iterator it = bb2OffsetList.keySet().iterator() ; 
	  it.hasNext() ; ) {
	  TracePoint t = (TracePoint)it.next();
	  if(t.threadname.equals(threadName))
	      v.add(t);
      }
      return v;
   }

   public java.util.List getOffsetList(TracePoint t){
      java.util.List list = (java.util.List)bb2OffsetList.get(t);
      return list;
   }

   public static void main(String args[]) throws java.io.IOException{
      TraceIndexer index = new TraceIndexer(new java.io.File(args[0]));
      System.out.println
	  (index.getOffsetList
	   (new TracePoint("5813833", "CaffeineMarkEmbeddedBenchmark", "addTest(I)V", 89)));
   }
}
