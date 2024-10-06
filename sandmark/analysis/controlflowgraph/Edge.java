package sandmark.analysis.controlflowgraph;

public class Edge {
    private Object mSrc;
    private Object mDest;
    public Edge(Object src,Object dest) {
	mSrc = src;
	mDest = dest;
    }
    public Object src() {
	return mSrc;
    }
    public Object dest() {
	return mDest;
    }
}

