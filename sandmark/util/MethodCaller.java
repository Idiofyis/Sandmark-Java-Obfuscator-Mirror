package sandmark.util;



/**
 * Given a method mg that is defined/declared by one or more classes (all part
 * of the same inheritance tree), this class can be used to find all the classes
 * in the application that can invoke mg, based on its access settings.
 *
 * @author Srinivas Visvanathan
 *
 */
public class MethodCaller
{
    
    /**
     * mg is some method that is implemented by one or more classes (all part of
     * the same inheritance tree). This method returns the list of classes
     * (sandmark.program.Class) that can invoke mg based on mg's access
     * settings. If mg is:
     *
     * <ul>
     *	<li> public: the list will contain all the classes in the application
     *	<li> private: the list will only containing the class defining mg
     *	<li> protected: the list will contain all the classes in cg's hierarchy
     *	(cg is the class owning mg), that can receive the message mg
     *	<li> has no access modifiers: the list will contain all the classes in
     *	the same package as cg (class owning mg).
     * </ul>
     */
    public static java.util.List findMethodCallers(sandmark.program.Method mg)
    {
	return findMethodCallers(mg,null);
    }


    /** 
     * Basically does the same thing as the other version of findMethodCallers.
     * Takes an extra argument "receivers" which is the set of classes that can
     * receive message mg as computed by findMethodReceivers (see package 
     * {@link sandmark.util.MethodReceiver}). If you just used
     * findMethodReceivers to compute the set of classes that can receive mg as a
     * message, then you can pass this set to findMethodCallers so that it doesn't
     * have to recompute it itself (as is done in the other version of this
     * method)
     */
    public static java.util.List findMethodCallers(sandmark.program.Method mg,
			java.util.Set receivers)
    {
	java.util.HashSet rcvrs = (java.util.HashSet)receivers;
	java.util.List result = new java.util.LinkedList();
	sandmark.program.Class cg = mg.getEnclosingClass();
	sandmark.program.Application app = cg.getApplication();

	//if mg is private, only cg's members can invoke mg
	if (mg.isPrivate()) {
	    result.add(cg);
	    return result;
	}

	//if public, all classes in the app can invoke mg
	if (mg.isPublic()) {
	    java.util.Iterator it = app.classes();
	    while (it.hasNext())
		result.add((sandmark.program.Class)it.next());
	    return result;
	}

	//if protected, all subclasses and superclasses that can receive the
	//method call can also call this method. All classes in this package can
	//also call the method
	if (mg.isProtected())
	{
	    if (rcvrs == null)
		rcvrs = (java.util.HashSet)
		    sandmark.util.MethodReceiver.findMethodReceivers(mg);
	    else
		//make a shallow copy of the set, so that we don't modify the
		//set passed as input to this method. Elements are not cloned 
		//and so will be referenced by both sets. Further additions 
		//to rcvrs will add to the clone set
		rcvrs = (java.util.HashSet)rcvrs.clone();

	    String pName = cg.getPackageName();
	    java.util.Iterator it = app.classes();
	    while (it.hasNext()) {
		sandmark.program.Class cls = (sandmark.program.Class)it.next();
		if (cls.getPackageName().equals(pName))
		    rcvrs.add(cls);
	    }

	    result.addAll(rcvrs);
	    return result;
	}

	//if no modifier has been given, then all classes in the same package
	//can invoke this method
	String pName = cg.getPackageName();
	java.util.Iterator it = app.classes();
	while (it.hasNext()) {
	    sandmark.program.Class cls = (sandmark.program.Class)it.next();
	    if (cls.getPackageName().equals(pName))
		result.add(cls);
	}

	return result;
    }
}
