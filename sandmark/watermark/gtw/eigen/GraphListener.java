import cern.colt.matrix.DoubleMatrix1D;

public interface GraphListener {
    public abstract void graphChanged(Graph g, DoubleMatrix1D e, 
				      double a, double b, double sum);
}

