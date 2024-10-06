package sandmark.obfuscate;


/**
 *  An AllClassesObfuscator applies a ClassObfuscator
 *  to all the classes in an application.
	@author		Christian Collberg
	@version 	1.0
 */

public abstract class AllClassesObfuscator extends sandmark.obfuscate.AppObfuscator {

    sandmark.obfuscate.ClassObfuscator co;

/**
 *  Constructs an AllClassesObfuscator using a
 *  ClassObfuscator and its label.
	@param	co
 */
public AllClassesObfuscator(sandmark.obfuscate.ClassObfuscator co) {
    this.co = co;
}

/**
 *  Applies the class obfuscation to all classes contained in an application.
 */
public void apply(sandmark.program.Application app) throws Exception {
    java.util.Iterator itr = app.classes();
    while(itr.hasNext()) {
	co.apply((sandmark.program.Class) itr.next());
    }
}



} // class AllClassesObfuscator

