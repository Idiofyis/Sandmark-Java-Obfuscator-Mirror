package sandmark.watermark.execpath;

public class StreamPump extends Thread {
    public java.io.InputStream mStream;
    public StreamPump(java.io.InputStream is) {
	mStream = is;
    }
    public void run() {
	byte buf[] = new byte[1024];
	int rv;
	try {
	    while((rv = mStream.read(buf)) != -1)
		System.out.write(buf,0,rv);
	} catch(java.io.IOException e) {
	    e.printStackTrace();
	}
    }
}
