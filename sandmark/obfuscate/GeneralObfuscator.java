package sandmark.obfuscate;

/**
 *  A GeneralObfuscator object encapsulates code for performing
 *  a particular obfuscation.
 *  Obfuscations are grouped into three abstract subclasses:
 *  <UL>
 *	<LI> AppObfuscator
 *	<LI> ClassObfuscator
 *	<LI> MethodObfuscator
 *  </UL>
 *
	@author		Christian Collberg
	@version	1.1 5/20/2002 K.H.
 */

public abstract class GeneralObfuscator extends sandmark.Algorithm {

    public sandmark.util.ConfigProperties getConfigProperties() {
        return null;
    }
}

