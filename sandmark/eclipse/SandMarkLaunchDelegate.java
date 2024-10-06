/*
 * Created on Mar 25, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.eclipse;

import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.core.runtime.IPath;

import sandmark.obfuscate.Obfuscator;
import sandmark.util.classloading.ClassFinder;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SandMarkLaunchDelegate implements ILaunchConfigurationDelegate {
   public void launch(ILaunchConfiguration configuration, String mode, 
                      ILaunch launch, IProgressMonitor monitor) 
      throws CoreException {
    monitor.beginTask("running Sandmark",1);
    
   	String algShortName = 
   		configuration.getAttribute(SandMarkConfigTab.ALG_NAME_KEY,"Class Encrypter");
   	String algClassName = ClassFinder.getClassByShortname(algShortName);
   	sandmark.Algorithm alg;
   	try {
   		alg = (sandmark.Algorithm)Class.forName(algClassName).newInstance();
   	} catch(Exception e) { throw new CoreException(null); }
   	
   	String jarPath = 
   		configuration.getAttribute(SandMarkConfigTab.ALG_INPUT_FILE,(String)null);
   	java.io.File jarFile = new java.io.File(jarPath);
   	sandmark.program.Application app;
	try {
		app = new sandmark.program.Application(jarFile);
	} catch(Exception e) {
		throw new CoreException(null);
	}
   	try {
   		Obfuscator.runObfuscation(app,alg);
   		app.save(jarFile);
   	} catch(Exception e) { throw new CoreException(null); }
   	
      monitor.worked(1);
      monitor.done();
   }
}
