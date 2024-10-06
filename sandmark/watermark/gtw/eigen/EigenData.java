import cern.colt.matrix.DoubleMatrix1D;

public class EigenData {
    public DoubleMatrix1D e;
    public double a, b, sum;

    public EigenData(DoubleMatrix1D _e, double _a, double _b, double _sum) {
	e = _e;
	a = _a;
	b = _b;
	sum = _sum;
    }
}

