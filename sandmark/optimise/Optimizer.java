package sandmark.optimise;


/**
 *  The sandmark.optimise.Optimizer class supervises optimization
 *  as parameterized by the global Properties and possibly also by
 *  a Scoreboard and other things yet to be determined.
        @author         Christian Collberg
        @version        1.0
 */

public class Optimizer {
    
    public static void runOptimization
        (sandmark.program.Application app,sandmark.Algorithm alg) 
        throws Exception {
        try{
            if (alg instanceof sandmark.optimise.AppOptimizer) {
                ((sandmark.optimise.AppOptimizer)(alg)).apply(app);
            }
            else if (alg instanceof sandmark.optimise.ClassOptimizer) {
                java.util.Iterator classes = app.classes();
                while(classes.hasNext())
                    ((sandmark.optimise.ClassOptimizer)(alg)).apply
                        ((sandmark.program.Class)classes.next());
                
            }
            else { // (alg instanceof sandmark.optimise.MethodOptimizer) {
                java.util.Iterator classes = app.classes();
                while(classes.hasNext()) {
                    java.util.Iterator methods = 
                        ((sandmark.program.Class) classes.next()).methods();
                    while(methods.hasNext())
                        ((sandmark.optimise.MethodOptimizer)(alg)).apply
                            ((sandmark.program.Method) methods.next());
                }
            }
            sandmark.util.Log.message(0,"Done optimizing!");
        } catch (OptimizationException e) {
            e.printStackTrace();
            sandmark.util.Log.message(0, "Optimization failed", e);
        }
    }
}




