package sandmark.watermark.execpath;

public class TraceReader implements java.util.Iterator {
    String nextLine;
    java.io.BufferedReader reader;
    public TraceReader(java.io.File tf) throws java.io.IOException {
	java.io.FileInputStream fis = new java.io.FileInputStream(tf);
	reader = new java.io.BufferedReader
	    (new java.io.InputStreamReader(fis));
	nextLine = reader.readLine();
    }
    public boolean hasNext() { return nextLine != null; }
    public Object next() { 
	String curLine = nextLine;
	try { nextLine = reader.readLine(); }
	catch(java.io.IOException e) { nextLine = null; }
	return curLine;
    }
    public void remove() { throw new UnsupportedOperationException(); }
}
