public class RandomGraphData extends GraphData implements Runnable {
    private int n;
    private double p;

    public RandomGraphData(int _n, double _p, GraphListener l) {
	super(generateGraph(_n, _p), l);
	n = _n;
	p = _p;
	new Thread(this).start();
    }

    private static Graph generateGraph(int n, double p) {
	Graph g = new Graph(n);
	for (int i = 0; i < n - 1; i++)
	    for (int j = i + 1; j < n; j++)
		if (GraphEigenvalues.r.nextDouble() < p) {
		    g.addEdge(i, j);
		}
	return g;
    }

    public void run() {
	while (true) {
	    setGraph(generateGraph(n, p));
	}
    }
}

