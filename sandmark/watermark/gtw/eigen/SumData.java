import cern.colt.matrix.DoubleMatrix1D;
import java.util.HashSet;
import java.util.Iterator;

public class SumData {
    private class DataSet {
	public int num;
	public double [] data;

	public DataSet(int capacity) {
	    data = new double[capacity];
	    num = 0;
	}

	public void addElement(double d) {
	    synchronized (this) {
		while (num >= data.length)
		    increaseCapacity();
		data[num++] = d;
	    }
	}

	public int numElements() {
	    synchronized (this) {
		return num;
	    }
	}

	public double getElement(int i) {
	    synchronized (this) {
		return data[i];
	    }
	}

	private void increaseCapacity() {
	    double [] newData = new double[data.length*2];
	    System.arraycopy(data, 0, newData, 0, data.length);
	    data = newData;
	}
    }

    private class SumListener implements GraphListener {
	private int dataSet;

	public SumListener(int _dataSet) {
	    dataSet = _dataSet;
	}

	public void graphChanged(Graph g, DoubleMatrix1D e, 
				 double a, double b,
				 double sum) {
	    data[dataSet].addElement(sum);
	    signalChange();
	}
    }

    private DataSet [] data;
    private int numSets;
    private HashSet listeners;

    public SumData() {
	this(3);
    }

    public SumData(int capacity) {
	data = new DataSet[capacity];
	numSets = 0;
	listeners = new HashSet();
    }

    public GraphListener getListener() {
	synchronized (this) {
	    while (numSets >= data.length)
		increaseCapacity();
	    data[numSets] = new DataSet(100);
	    return new SumListener(numSets++);
	}
    }

    public void addListener(BasicListener l) {
	synchronized (listeners) {
	    listeners.add(l);
	}
    }

    private void signalChange() {
	synchronized (listeners) {
	    Iterator i = listeners.iterator();
	    while (i.hasNext()) {
		BasicListener l = (BasicListener)i.next();
		l.somethingChanged();
	    }
	}
    }

    private void increaseCapacity() {
	DataSet [] newData = new DataSet[data.length*2];
	System.arraycopy(data, 0, newData, 0, data.length);
	data = newData;
    }

    public int numSets() {
	synchronized (this) {
	    return numSets;
	}
    }

    public int setSize(int i) {
	synchronized (this) {
	    return data[i].numElements();
	}
    }

    public double setElement(int set, int element) {
	synchronized (this) {
	    return data[set].getElement(element);
	}
    }
}

