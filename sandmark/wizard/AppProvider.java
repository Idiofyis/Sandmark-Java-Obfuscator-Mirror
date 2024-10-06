/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard;

/*
 * @author ash
 */
 
/**
 * DefaultObjectProvider that provides sandmark Applications.
 */ 
public class AppProvider extends DefaultObjectProvider {
   public void addObject(sandmark.program.Object o) {
      if(o instanceof sandmark.program.Application && 
         !mObjects.contains(o))
         mObjects.add(o);
   }
}
