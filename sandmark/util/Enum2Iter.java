package sandmark.util;

public class Enum2Iter implements java.util.Iterator {

/**
 * Convert a java.util.Enumeration to a java.util.Iterator.
 */

java.util.Enumeration enum;
public Enum2Iter (
   java.util.Enumeration enum) {
   this.enum = enum;
}

public boolean hasNext()  {
   return enum.hasMoreElements();
}
    
    
public Object next() {
   return enum.nextElement();
}
    

public void remove() {
   throw new UnsupportedOperationException
   ("It is impossible to remove from this iterator!");
}

}

