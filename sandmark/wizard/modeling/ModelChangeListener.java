/*
 * Created on Apr 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.modeling;


/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ModelChangeListener {
   void modelChanged();
   void choicesAdded(Choice [] choices);
   void choicesRemoved(Choice [] choices);
}
