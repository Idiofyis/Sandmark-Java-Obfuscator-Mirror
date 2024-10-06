package sandmark.util;

/**
 *  WeakBuffer.java -- 
 *  @author Jasvir Nagra <jas@cs.auckland.ac.nz>
 *  Created On      : Fri May  2 11:27:31 2003
 *  Last Modified   : <03/05/09 11:46:32 jas>
 *  Description     : Implements a buffer with weak objects that will be gc'ed if not in ordinary use
 *  Keywords        : gc weak buffer
 *  PURPOSE
 *  	| Sandmark project |
 */

public class WeakBuffer {
    private java.util.WeakHashMap buffer;

    public WeakBuffer () {
	buffer = new java.util.WeakHashMap();
    }

    public void add ( java.lang.Object obj ) {
	buffer.put ( obj, null );
    }

    public java.util.Iterator iterator() {
	return buffer.keySet().iterator();
    }
    
    public String toString() {
	return buffer.keySet().toString();
   }

    public int size() {
	return buffer.size();
    }

    public static void main ( String args[] ) {
	WeakBuffer hash = new WeakBuffer();
	hash.add ( "hello" );
	hash.add ( "world" );
	hash.add ( new Integer ( 1 ) );
	hash.add ( new Object () );
	System.out.println( hash );
	java.util.Iterator i = hash.iterator();
	while ( i.hasNext() ) {
	    System.out.println( i.next() );
	}
    }

}


//  Emacs bunkum
//  Local Variables:
//  mode: jde
//  time-stamp-start: "Last Modified[ \t]*:[ 	]+\\\\?[\"<]+"
//  time-stamp-end:   "\\\\?[\">]"
//  End:

