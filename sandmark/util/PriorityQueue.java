package sandmark.util;

// Map int -> Object
// Assumes unique keys.
public class PriorityQueue implements Cloneable {

    java.util.TreeMap int2object;
    java.util.Hashtable object2int;

    public PriorityQueue () {
       int2object = new java.util.TreeMap();
       object2int = new java.util.Hashtable();
    }

   public java.lang.Object clone() throws CloneNotSupportedException {
      PriorityQueue p =  new PriorityQueue();
      p.int2object = (java.util.TreeMap) int2object.clone();
      p.object2int = (java.util.Hashtable) object2int.clone();
      return p;
   }

   public PriorityQueue copy() {
      PriorityQueue New = null;
      try {
         New = (PriorityQueue) clone();
      } catch (Exception e) {
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }
      return New;
   }


    public void put(int prio, Object obj) {
       int2object.put(new Integer(prio), obj);
       object2int.put(obj, new Integer(prio));
    }

    public int priority (Object obj) {
	return ((Integer)object2int.get(obj)).intValue();
    }

    public Object deleteMin () {
       Integer key = (Integer) int2object.firstKey();
       Object obj = int2object.get(key);
       int2object.remove(key);
       object2int.remove(obj);
       return obj;
    }

    public Object deleteMax () {
       Integer key = (Integer) int2object.lastKey();
       Object obj = int2object.get(key);
       int2object.remove(key);
       object2int.remove(obj);
       return obj;
    }

    public boolean isEmpty () {
       return int2object.isEmpty();
    }

    public boolean containsKey (Object obj) {
       return object2int.containsKey(obj);
    }

class Ascending implements java.util.Iterator {
    java.util.Iterator iter;  
    Object nextElmt;

    public Ascending() {
	nextElmt=null;
    }

    void get() {
       if ((nextElmt == null) & (!isEmpty()))
          nextElmt = deleteMin();
    }

    public boolean hasNext() {
       get();
       return nextElmt != null;
    }

    public Object next() throws java.util.NoSuchElementException {
       get();
       if (nextElmt == null)
           throw new java.util.NoSuchElementException();
       Object n = nextElmt;
       nextElmt = null;
       return n;
    }

    public void remove() {
    }
  }

    public java.util.Iterator ascending() {
        PriorityQueue p = this.copy();
	java.util.Iterator iter = p.new Ascending();
        return iter;
    }

class Descending implements java.util.Iterator {
    java.util.Iterator iter;  
    Object nextElmt;

    public Descending() {
	nextElmt=null;
    }

    void get() {
       if ((nextElmt == null) & (!isEmpty()))
          nextElmt = deleteMax();
    }

    public boolean hasNext() {
       get();
       return nextElmt != null;
    }

    public Object next() throws java.util.NoSuchElementException {
       get();
       if (nextElmt == null)
           throw new java.util.NoSuchElementException();
       Object n = nextElmt;
       nextElmt = null;
       return n;
    }

    public void remove() {
    }
  }

    public java.util.Iterator descending() {
        PriorityQueue p = this.copy();
	java.util.Iterator iter = p.new Descending();
        return iter;
    }

    public static void main (String args[]) {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("++++++++++++++++ Testing util.PriorityQueue +++++++++++++++++");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	PriorityQueue q = new PriorityQueue();
        String one = "one";
        String two = "two";
        String three = "three";
        String four = "four";
        String five = "five";
        q.put(5, five);
        q.put(2, two);
        q.put(1, one);
        q.put(4, four);
        q.put(3, three);

        System.out.println("priority(one)=" + q.priority(one));
        System.out.println("priority(five)=" + q.priority(five));
        System.out.println("deleteMin()=" + q.deleteMin());
        System.out.println("deleteMin()=" + q.deleteMin());

        System.out.println("-----------------------------------");
        java.util.Iterator iter1 = q.ascending();
        while (iter1.hasNext()) {
	    String obj = (String) iter1.next();
            System.out.println("iterateA: " + obj);
        }

        System.out.println("-----------------------------------");
        java.util.Iterator iter2 = q.descending();
        while (iter2.hasNext()) {
	    String obj = (String) iter2.next();
            System.out.println("iterateB: " + obj);
        }
    }
}

