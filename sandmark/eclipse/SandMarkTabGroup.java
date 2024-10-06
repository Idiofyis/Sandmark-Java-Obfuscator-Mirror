/*
 * Created on Mar 25, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.eclipse;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsMainTab;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsBuilderTab;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SandMarkTabGroup extends AbstractLaunchConfigurationTabGroup {
   
   public void createTabs(ILaunchConfigurationDialog arg0, String arg1) {
      ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
         //new SandMarkMainTab(),
         //new RefreshTab(),
         new SandMarkConfigTab(),
		 //new CommonTab(),
		 new ExternalToolsBuilderTab(),
      };
      setTabs(tabs);
   }
}
