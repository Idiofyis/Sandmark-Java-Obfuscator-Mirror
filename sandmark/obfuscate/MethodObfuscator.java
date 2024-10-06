package sandmark.obfuscate;
/**
 *  A MethodObfuscator object encapsulates code for performing
 *  an obfuscation that operates on a single method.
        @author         Christian Collberg
        @version        1.0
 */

public abstract class MethodObfuscator extends GeneralObfuscator 
    implements sandmark.MethodAlgorithm {

    /**
     *  Applies this obfuscation to a single method.
     */
    abstract public void apply(sandmark.program.Method meth) throws Exception;
}

