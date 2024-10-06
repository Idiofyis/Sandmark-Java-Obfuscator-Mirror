package sandmark.obfuscate;


/**
 *  The sandmark.obfuscate.Obfuscator class supervises obfuscation
 *  as parameterized by the global Properties and possibly also by
 *  a Scoreboard and other things yet to be determined.
        @author         Christian Collberg
        @version        1.0
 */

public class Obfuscator {
    
    public static void runObfuscation
        (sandmark.program.Application app,sandmark.Algorithm alg) 
        throws Exception {
        try{
            if (alg instanceof sandmark.obfuscate.AppObfuscator) {
                ((sandmark.obfuscate.AppObfuscator)(alg)).apply(app);
            }
            else if (alg instanceof sandmark.obfuscate.ClassObfuscator) {
                java.util.Iterator classes = app.classes();
                while(classes.hasNext())
                    ((sandmark.obfuscate.ClassObfuscator)(alg)).apply
                        ((sandmark.program.Class)classes.next());
                
            }
            else { // (alg instanceof sandmark.obfuscate.MethodObfuscator) {
                java.util.Iterator classes = app.classes();
                while(classes.hasNext()) {
                    java.util.Iterator methods = 
                        ((sandmark.program.Class) classes.next()).methods();
                    while(methods.hasNext())
                        ((sandmark.obfuscate.MethodObfuscator)(alg)).apply
                            ((sandmark.program.Method) methods.next());
                }
            }
            sandmark.util.Log.message(0,"Done obfuscating!");
        } catch (ObfuscationException e) {
            e.printStackTrace();
            sandmark.util.Log.message(0, "Obfuscation failed", e);
        }
    }
}




