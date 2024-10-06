public class KPartiteGraphData extends GraphData implements Runnable {
    private int parts, n;
    private double p;

    public KPartiteGraphData(int _parts, int _n, double _p, GraphListener l) {
	super(generateGraph(_parts, _n, _p), l);
	parts = _parts;
	n = _n;
	p = _p;
	new Thread(this).start();
    }

    private static Graph generateGraph(int parts, int n, double p) {
	Graph g = new Graph(n);
	for (int k = 0; k < parts; k++) {
	    for (int i = k*n/parts; i < (k+1)*n/parts; i++)
		for (int j = (k+1)*n/parts; j < n; j++)
		    if (GraphEigenvalues.r.nextDouble() < p) {
			g.addEdge(i, j);
		    }
	}
	return g;
    }

    public void run() {
	while (true) {
	    setGraph(generateGraph(parts, n, p));
	}
    }
}

