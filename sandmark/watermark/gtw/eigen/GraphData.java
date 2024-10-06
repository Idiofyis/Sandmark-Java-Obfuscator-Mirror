import java.util.*;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;

public class GraphData {
    private Graph g;
    private DoubleMatrix1D e;
    private double a, b, sum;

    private HashSet listeners;

    private class BestFitParams {
	public double a11, a12, a21, a22;
	public DoubleMatrix1D u, v;

	public BestFitParams(int n) {
	    a11 = 12.0 / ((n - 2) * (n - 3) * (n - 4));
	    a12 = a21 = -6.0 / ((n - 3) * (n - 4));
	    a22 = (4 * n - 10.0) / ((n - 3) * (n - 4));

	    double _u[] = new double[n];
	    for (int i = 0; i < n; i++)
		_u[i] = i;
	    u = new DenseDoubleMatrix1D(_u);

	    double _v[] = new double[n];
	    for (int i = 0; i < n; i++)
		_v[i] = 1;
	    v = new DenseDoubleMatrix1D(_v);
	}
    }

    private static final HashMap params = new HashMap();

    public void setGraph(Graph g) {
	DoubleMatrix2D l = new DenseDoubleMatrix2D(g.numVertices(),
						   g.numVertices());
	for (int i = 0; i < g.numVertices(); i++)
	    for (int j = 0; j < g.numVertices(); j++)
		if (i == j && g.degree(i) > 0)
		    l.set(i, j, 1);
		else if (g.containsEdge(i, j))
		    l.set(i, j, -1 / Math.sqrt(g.degree(i)*g.degree(j)));
		else
		    l.set(i, j, 0);
	EigenvalueDecomposition e = new EigenvalueDecomposition(l);
	DoubleMatrix1D lambda = e.getRealEigenvalues();

	BestFitParams p = null;
	int n = lambda.size();
	Integer _n = new Integer(n);
	synchronized (params) {
	    if (params.containsKey(_n)) {
		p = (BestFitParams)params.get(_n);
	    }
	    else {
		p = new BestFitParams(n);
		params.put(_n, p);
	    }
	}

	double dot1 = lambda.zDotProduct(p.u, 1, n-3);
	double dot2 = lambda.zDotProduct(p.v, 1, n-3);
	
	synchronized (this) {
	    this.g = g;
	    this.e = lambda;
	    a = dot1 * p.a11 + dot2 * p.a12;
	    b = dot1 * p.a21 + dot2 * p.a22;
	    sum = lambda.get(n-2) - (a*(n-2)+b);
	    synchronized (listeners) {
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
		    GraphListener gl = (GraphListener)i.next();
		    gl.graphChanged(g, lambda, a, b, sum);
		}
	    }
	}
    }

    public GraphData(Graph g, GraphListener gl) {
	listeners = new HashSet();
	if (gl != null) {
	    listeners.add(gl);
	}
	setGraph(g);
    }

    public void addListener(GraphListener gl) {
	if (gl != null) {
	    synchronized (listeners) {
		listeners.add(gl);
	    }
	}
    }

    public Graph getGraph() {
	synchronized (this) {
	    return g;
	}
    }

    public EigenData getEigenData() {
	synchronized (this) {
	    return new EigenData(e, a, b, sum);
	}
    }
}

