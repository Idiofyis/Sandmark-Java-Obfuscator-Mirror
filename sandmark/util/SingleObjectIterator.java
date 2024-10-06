/*
 * Created on Apr 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.util;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SingleObjectIterator implements java.util.Iterator {
   private Object o;
   private boolean didReturn = false;
   public SingleObjectIterator(Object u) { o = u; }
   public boolean hasNext() { return !didReturn; }
   public Object next() { didReturn = true; return o; }
   public void remove() { throw new UnsupportedOperationException(); }

}
