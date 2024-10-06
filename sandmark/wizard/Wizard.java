package sandmark.wizard;

/**
 *  A Wizard object encapsulates code for performing
 *  multiple obfuscations and/or watermarking algorithms.

 @author Kelly Heffner
 @version 1.0 11/17/2003
 */

public abstract class Wizard {

    /**
       Performs another iteration of the algorithm, returning
       false if the algorithm is done.  Ideally this allows for
       the user of the wizard to setup a system with
       <code>while(wizard.step())</code>.
    */
    public boolean step(){
        return false;
    }

    public abstract sandmark.wizard.modeling.Choice getLastChoice();

    public void runChoice(sandmark.wizard.modeling.Choice c) throws Exception{
        sandmark.Algorithm alg = c.getAlg();
        sandmark.program.Object obj = c.getTarget();

        if(alg instanceof sandmark.watermark.StaticWatermarker)
            ((sandmark.watermark.StaticWatermarker)alg).embed
                (sandmark.watermark.StaticWatermarker.getEmbedParams
                 ((sandmark.program.Application)obj));
        else if(alg instanceof sandmark.watermark.DynamicWatermarker)
            ((sandmark.watermark.DynamicWatermarker)alg).embed
                (sandmark.watermark.DynamicWatermarker.getEmbedParams
                 ((sandmark.program.Application)obj));
        else if(alg instanceof sandmark.obfuscate.ClassObfuscator)
            ((sandmark.obfuscate.ClassObfuscator)alg).apply
                ((sandmark.program.Class)obj);
        else if(alg instanceof sandmark.obfuscate.MethodObfuscator)
            ((sandmark.obfuscate.MethodObfuscator)alg).apply
                ((sandmark.program.Method)obj);
        else if(alg instanceof sandmark.obfuscate.AppObfuscator)
            ((sandmark.obfuscate.AppObfuscator)alg).apply
                ((sandmark.program.Application)obj);

    }

    public sandmark.util.ConfigProperties getConfigProperties(){
        return new sandmark.util.ConfigProperties(new String[0][0], null);
    }
}
