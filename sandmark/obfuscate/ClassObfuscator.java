package sandmark.obfuscate;

/**
 *  A ClassObfuscator object encapsulates code for performing
 *  an obfuscation that operates on a whole class.
	@author		Christian Collberg
	@version	1.0
 */

public abstract class ClassObfuscator extends GeneralObfuscator 
    implements sandmark.ClassAlgorithm {
    
    /**
     *  Applies this obfuscation to a single class.
     */
    abstract public void apply(sandmark.program.Class cls) throws Exception;
}

