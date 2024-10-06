package sandmark.watermark.util;

/**
 * This implementation of MethodMarker takes a
 * BasicBlockMarker and uses it to mark each basic
 * block in a method.  The BasicBlockMarker is also
 * used to recognize embedded values from each basic
 * block in the method.
 */

public class EveryBlockMarker extends MethodMarker {
    private BasicBlockMarker marker;

    /**
     * Constructs a marker that marks basic blocks using
     * the given {@link BasicBlockMarker}.
     *
     * @param _marker used to embed and recognize values in
     * each basic block of a method
     */
    public EveryBlockMarker(BasicBlockMarker _marker) {
	marker = _marker;
    }

    /**
     * Attempts to encode the given value in the given method.  The
     * method is modified in place.  If the number of bits in the given
     * value exceeds that of the value returned by 
     * {@link #getCapacity(sandmark.program.Method)}, 
     * an {@link IllegalArgumentException} 
     * will be thrown.<br><br>
     * Each basic block in the method will be marked with the given
     * value.
     *
     * @param method method to encode data in
     * @param value data to encode
     * @throws java.lang.IllegalArgumentException if value has too many bits
     * @see #getCapacity(sandmark.program.Method)
     */
    public final void embed(sandmark.program.Method method,
			    java.math.BigInteger value) {
	java.util.Iterator i = method.getCFG(false).basicBlockIterator();
	while (i.hasNext()) {
	    sandmark.analysis.controlflowgraph.BasicBlock b = 
		(sandmark.analysis.controlflowgraph.BasicBlock)i.next();
	    marker.embed(b, value);
	}
	method.mark();
    }

    private class CountOrderer implements java.util.Comparator {
	private java.util.HashMap counts;

	public CountOrderer(java.util.HashMap _counts) {
	    counts = _counts;
	}

	private int getCount(Object o) {
	    if (!counts.containsKey(o))
		return 0;
	    else {
		Integer i = (Integer)counts.get(o);
		return i.intValue();
	    }
	}

	public int compare(Object o1, Object o2) {
	    int c1 = getCount(o1), c2 = getCount(o2);
	    if (c1 == c2)
		return 0;
	    else if (c1 > c2)
		return -1;
	    else
		return 1;
	}
    }

    /**
     * Returns an {@link java.util.Iterator} over all 
     * values found to be embedded in the
     * given method.  The returned
     * {@link java.util.Iterator} will iterate over
     * all values found in at least one basic block of the
     * method, sorted in descending order according to
     * how many basic blocks each value was found in.
     *
     * @param method method to search for marks in
     */
    public final java.util.Iterator recognize(sandmark.program.Method method) {
	java.util.HashMap counts = new java.util.HashMap();
	java.util.Iterator i = method.getCFG(false).basicBlockIterator();

	while (i.hasNext()) {
	    sandmark.analysis.controlflowgraph.BasicBlock b = 
		(sandmark.analysis.controlflowgraph.BasicBlock)i.next();
	    java.util.Iterator values = marker.recognize(b);
	    while (values.hasNext()) {
		java.math.BigInteger value = 
		    (java.math.BigInteger)values.next();
		if (counts.containsKey(value)) {
		    Integer count = (Integer)counts.get(value);
		    counts.put(value, 
			       new Integer(count.intValue() + 1));
		}
		else
		    counts.put(value, new Integer(1));
	    }
	}

	java.util.TreeSet sorted = 
	    new java.util.TreeSet(new CountOrderer(counts));
	i = counts.keySet().iterator();
	while (i.hasNext())
	    sorted.add(i.next());

	return sorted.iterator();
    }

    /**
     * Returns the number of bits that can be encoded into the given method.
     * This is calculated to be the smallest capacity among the basic
     * blocks of the method, as returned by the underlying
     * {@link BasicBlockMarker}.
     *
     * @param method method to report the bit capacity of
     * @see #embed(sandmark.program.Method, java.math.BigInteger)
     * @see #embed(sandmark.program.Method, long)
     * @see BasicBlockMarker#getCapacity(sandmark.analysis.controlflowgraph.BasicBlock)
     */
    public final int getCapacity(sandmark.program.Method method) {
	if (marker.capacityIsConstant())
	    return marker.getCapacity(null);
	else {
	    java.util.Iterator i = method.getCFG(false).basicBlockIterator();
	    if (!i.hasNext())
		return 0;
	    else {
		sandmark.analysis.controlflowgraph.BasicBlock b = 
		    (sandmark.analysis.controlflowgraph.BasicBlock)i.next();
		int min = marker.getCapacity(b);
		while (i.hasNext()) {
		    b = (sandmark.analysis.controlflowgraph.BasicBlock)i.next();
		    int capacity = marker.getCapacity(b);
		    if (capacity < min)
			min = capacity;
		}
		return min;
	    }
	}
    }
}

