/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard;


/*
 * @author ash
 * Provides sandmark algorithms for wizard to use.
 */
public interface AlgorithmProvider extends java.util.Iterator {
   sandmark.Algorithm [] getAlgorithms();
   void reset();
}
