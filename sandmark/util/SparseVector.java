package sandmark.util;

/**
 * This is similar to java.util.Vector.
 * However, 
 * <OL>
 * <LI> we count from 1;
 * <LI> we ignore missing elements. So, get(i) will
 *      return null rather than throw an exception if element
 *      i does not exist;
 * <LI> the vector grows automatically. So, whenever we try to
 *      set(i,obj) the vector is grown to accomodate object
 *      number i;
 * <LI> When iterating over a vector we ignore (i.e. don't return)
 *      null elements. 
 * </OL>
 */

public class SparseVector implements Cloneable {
  java.util.Vector elements;
  
  public SparseVector () {
     elements = new java.util.Vector();
  }

  public SparseVector (int initialCapacity) {
     elements = new java.util.Vector(initialCapacity);
  }

   public java.lang.Object clone() throws CloneNotSupportedException {
      SparseVector v = new SparseVector();
      v.elements = (java.util.Vector) elements.clone();
      return v;
   }

   public SparseVector copy() {
      SparseVector New = null;
      try {
         New = (SparseVector) clone();
      } catch (Exception e) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }
      return New;
   }

 public Object get(int index) {
     index--;
     try {
        return elements.get(index);
     } catch (Exception e) {
        return null;
     }
  }

  public Object set(int index, Object elmt) {
    index--;
    elements.ensureCapacity(index+1);
    if (elements.size() < (index+1))
       elements.setSize(index+1);
     try {
        return elements.set(index,elmt);
     } catch (Exception e) {
       return null;
     }
  }

public int size()  {
    return elements.size();
}

class Iterator implements java.util.Iterator {
    java.util.Enumeration enum;  
    Object nextElmt=null;

    public Iterator() {
       enum = elements.elements();
    }

    void get () {
      while ((nextElmt == null) && enum.hasMoreElements())
         nextElmt = enum.nextElement();
    }

    public boolean hasNext() {
       get();
       return nextElmt != null;
    }

    public Object next() throws java.util.NoSuchElementException {
       get();
       if (nextElmt == null)
	   throw new java.util.NoSuchElementException();
       Object e = nextElmt;
       nextElmt = null;
       return e;
    }

    public void remove() {
    }
}

public java.util.Iterator iterator()  {
    return new Iterator();
}

}

