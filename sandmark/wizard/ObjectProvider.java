/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard;


/**
 * @author ash
 *
 * Provides sandmark Objects for wizard
 */
public interface ObjectProvider extends java.util.Iterator {
   void addObject(sandmark.program.Object o);
   sandmark.program.Object [] getObjects();
   void reset();
   void removeObject(sandmark.program.Object o);
}
