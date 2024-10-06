/*
 * Created on Apr 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.program;


/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ObjectMemberChangeListener {
   void addedObject(sandmark.program.Object parent,
                    sandmark.program.Object added);
   void deletingObject(sandmark.program.Object parent,
                       sandmark.program.Object deleted);
   void copiedObject(sandmark.program.Object parent,
                     sandmark.program.Object orig,
                     sandmark.program.Object copy);
}
