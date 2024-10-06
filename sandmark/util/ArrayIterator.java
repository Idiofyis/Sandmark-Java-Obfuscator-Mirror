package sandmark.util;

public class ArrayIterator implements java.util.Iterator {
   private java.lang.Object a[];
   private int curr, length;

   public ArrayIterator(java.lang.Object a[], int fromIndex, int toIndex) {
      this.a = a;
      curr = fromIndex;
      length = toIndex;
   }

   public boolean hasNext() {
      return curr < length;
   }

   public java.lang.Object next() {
      if (!hasNext())
	 throw new java.util.NoSuchElementException();

      return a[curr++];
   }

   public void remove() {
      throw new java.lang.UnsupportedOperationException();
   }
}
