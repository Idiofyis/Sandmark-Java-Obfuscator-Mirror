package sandmark.obfuscate;
/**
 *  An AppObfuscator object encapsulates code for performing an
 *  obfuscation that encompasses the entire application program.
	@author		Christian Collberg
	@version	1.0
 */

public abstract class AppObfuscator extends GeneralObfuscator 
    implements sandmark.AppAlgorithm {

    /**
     *  Applies this obfuscation to an application.
     */
    abstract public void apply(sandmark.program.Application app)
	throws Exception;
}

