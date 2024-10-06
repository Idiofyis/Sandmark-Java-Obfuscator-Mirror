package sandmark.util;

abstract public class MultiIter implements java.util.Iterator {
    java.util.Iterator[] enums;
    java.lang.Object result;
    java.lang.Object[] elmts;
    boolean firstTime = true;

   /**
    * Compose several iterators into a new iterator.
    * Extend this class and override <code>start</code>
    * with a method which starts the k:th enumerator
    * and <code>create</code> which creates the 
    * new element to yield out of the elements 
    * yielded by the individual enumerators.
    */
    public MultiIter() {}

    void init() {
       elmts = new java.lang.Object[count()];
       enums = new java.util.Iterator[count()];

       for(int i=0; i<count(); i++) {
	  enums[i] = start(i,elmts);
          if (i < (count()-1)) 
             if (enums[i].hasNext())
                elmts[i] = enums[i].next();
       }

       firstTime = false;
    }

    public void genNext() {
	if (result != null) return;

	if (firstTime) init();

        for(int i=(count()-1); i>=0; i--) {
	    while (enums[i].hasNext()){
                elmts[i] = enums[i].next();
                for(int j=i+1; j<count(); j++) {
	           enums[j] = start(j, elmts);
                   if (enums[j].hasNext())
	               elmts[j] = enums[j].next();
                   else
	               elmts[j] = null;
		}
                try {
                   result = create(elmts);
                   return;
                } catch (Exception e) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }
	    } 
	}
    }

    /*
     * Start enumerator number k. 
     * elmts[0..k-1] hold the current generated values
     * for the first k enumerators.
     */
    public abstract java.util.Iterator start(
       int k, 
       java.lang.Object[] elmts);

    /*
     * Create the object to be returned by the enumerator.
     * Throws an exception if no element could be generated.
     */
    public abstract java.lang.Object create(
        java.lang.Object[] elmts) throws java.lang.Exception;

    /*
     * Return the number of enumerators.
     */
    public abstract int count();

    public boolean hasNext() {
        genNext();
	return (result != null);
    }

    public java.lang.Object next() {
        genNext();
        if (result==null) throw new java.util.NoSuchElementException();
	java.lang.Object tmp = result;
        result = null;
        return tmp;
    }

    public void remove() {
       throw new UnsupportedOperationException
       ("It is impossible to remove from this iterator!");
    }
}

