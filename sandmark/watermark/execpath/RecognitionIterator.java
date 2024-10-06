package sandmark.watermark.execpath;

public class RecognitionIterator implements java.util.Iterator {
    java.io.RandomAccessFile f;
    TraceNode nextNode;
    public RecognitionIterator(java.io.File file) throws java.io.IOException {
	System.out.println("recognizing from " + file);
	f = new java.io.RandomAccessFile(file,"r");
	getNext();
    }
    public boolean hasNext() { return nextNode != null; }
    public Object next() { 
	TraceNode t = nextNode;
	getNext();
	return t;
    }
    public void remove() { throw new UnsupportedOperationException(); }
    private void getNext() { 
	TraceNode prev = nextNode;
	nextNode = null;
	while(nextNode == null) {
	    String line = null;
	    try {
		line = f.readLine();
		if(line == null)
		    return;
	    } catch(java.io.IOException e) {
		return;
	    }

	    TraceNode t = new TraceNode(line,null);
	    if(t.getNodeType() == TraceNode.TYPE_IF ||
	       t.getNodeType() == TraceNode.TYPE_SWITCH || 
	       (prev != null && (prev.getNodeType() == TraceNode.TYPE_IF ||
				 prev.getNodeType() == TraceNode.TYPE_SWITCH)))
		nextNode = t;
	}
    }				  
}
