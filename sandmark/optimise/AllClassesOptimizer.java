package sandmark.optimise;


/**
 *  An AllClassesOptimizer applies a ClassOptimizer
 *  to all the classes in an application.
	@author		Christian Collberg
	@version 	1.0
 */

public abstract class AllClassesOptimizer extends sandmark.optimise.AppOptimizer {

    sandmark.optimise.ClassOptimizer co;

/**
 *  Constructs an AllClassesOptimizer using a
 *  ClassOptimizer and its label.
	@param	co
 */
public AllClassesOptimizer(sandmark.optimise.ClassOptimizer co) {
    this.co = co;
}

/**
 *  Applies the class optimization to all classes contained in an application.
 */
public void apply(sandmark.program.Application app) throws Exception {
    java.util.Iterator itr = app.classes();
    while(itr.hasNext()) {
	co.apply((sandmark.program.Class) itr.next());
    }
}



} // class AllClassesOptimizer

