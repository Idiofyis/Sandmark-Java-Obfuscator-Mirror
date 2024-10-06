package sandmark.util;

/**
 * Implements a circular queue of K elements. 'add(O)'
 * adds a new element at the current position in
 * the queue, possibly overwriting an earlier element
 * (if more then K elements have been added).
 */

class Node {
    java.lang.Object data;
    sandmark.util.Node prev;
    sandmark.util.Node next;
}

public class CircularBuffer {

    sandmark.util.Node buffer;

/**
 * Create a new Buffer.
 * @param size The number of elements in the queue.
 */
    public CircularBuffer (
       int size) {
       buffer = new sandmark.util.Node();
       sandmark.util.Node last = buffer;
       for(int i=0; i<(size-1); i++) {
          sandmark.util.Node n = new sandmark.util.Node();
          last.next = n;
          n.prev = last;
          last = last.next;
       }
       last.next = buffer;
       buffer.prev = last;
    }

/**
 * Add a new element at the current position of the queue.
 * @param obj The new element.
 */
    public Object add (
       java.lang.Object obj) {
       Object oldData = buffer.data;
       buffer.data = obj;
       buffer = buffer.next;
       return oldData;
    }

//------------------------------------------------------------

   class Iterator implements java.util.Iterator {
      sandmark.util.Node current;
      boolean firstTime = true;
      public Iterator () {
         current = buffer.prev;
      }

    public boolean hasNext() {
       return firstTime || (current != buffer.prev);
    }

    public java.lang.Object next() throws java.util.NoSuchElementException {
       java.lang.Object n = current.data;
       current = current.prev;
       firstTime = false;
       return n;
    }

    public void remove() {
    }
   }

/**
 * Generate the elements of the queue, in reverse order
 * by time. I.e. the last added element is produced first.
 */
   public java.util.Iterator iterator() {
     return new Iterator();
   }

/**
 * Generate a printable version of the queue.
 */
   public String toString() {
      String S = "[";
      java.util.Iterator iter = iterator();
      while (iter.hasNext()) {
          java.lang.Object o = iter.next();
          if (o == null)
	      S += "null ";
          else
              S += o.toString() + " ";
      }
      S += "]";
      return S;
   }

//------------------------------------------------------------
    public static void main (String args[]) {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("+++++++++++++++ Testing util.CircularBuffer +++++++++++++++++");
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

	sandmark.util.CircularBuffer buf1 = new sandmark.util.CircularBuffer(3);

        buf1.add(new java.lang.Integer(1));
        buf1.add(new java.lang.Integer(2));
        buf1.add(new java.lang.Integer(3));
        System.out.println(buf1.toString());
        System.out.println("----------------------");

        buf1.add(new java.lang.Integer(1));
        buf1.add(new java.lang.Integer(2));
        buf1.add(new java.lang.Integer(3));
        System.out.println(buf1.toString());
        System.out.println("----------------------");

        buf1.add(new java.lang.Integer(5));
        buf1.add(new java.lang.Integer(5));
        System.out.println(buf1.toString());
        System.out.println("----------------------");

        buf1.add(new java.lang.Integer(7));
        buf1.add(new java.lang.Integer(8));
        buf1.add(new java.lang.Integer(9));
        System.out.println(buf1.toString());
        System.out.println("----------------------");

    }
}

