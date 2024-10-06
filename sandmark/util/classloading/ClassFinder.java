package sandmark.util.classloading;

/**
 * Provides a unified interface for finding classes that extend certain interfaces.
 * The class types that can be requested through the methods of this class are
 * defined in sandmark.util.classloading.IClassFinder
 * @see sandmark.util.classloading.IClassFinder
 * @author Andrew Huntwork
 * @version 1.0
 */

public class ClassFinder {
    public static boolean debug = false;
    private static sandmark.util.classloading.IClassFinder sListGen;
    private static java.util.Hashtable sClassShortNames;
    static {
	try {
	    sListGen = new DirClassFinder();
	    if(debug)
		System.out.println("Using Dir");
	} catch(Throwable t) {
	    if(debug)
		System.out.println("Dir list failed");
	}
	if(sListGen == null) {
	    try {
		sListGen = new JarClassFinder();
		if(debug)
		    System.out.println("Using Jar");
	    } catch(Throwable e) {
		if(debug)
		    System.out.println("Jar list failed");
	    }
	}
	if(sListGen == null) {
	    try {
		sListGen = new FileClassFinder();
		if(debug)
		    System.out.println("Using File");
	    } catch(Throwable e) {
		e.printStackTrace();
		throw new Error();
	    }
	}

	sClassShortNames = new java.util.Hashtable();
	for(int i = 0 ; i < IClassFinder.CLASS_COUNT ; i++)
	    for(java.util.Iterator classes = 
		    getClassesWithAncestor(i).iterator() ;
		classes.hasNext() ; ) {
		String className = (String)classes.next();
		String shortName = getClassShortname(className);
		sClassShortNames.put(shortName,className);
	    }
    }
    /**
     * Get all the classes that sListGen knows about it that derive from
     * the class indicated by 'ancestor'
     * @param ancestor one of the constants in IClassFinder
     * @return Collection of String's containing the names of classes derived from class specified by ancestor
     */
    public static java.util.Collection getClassesWithAncestor(int ancestor) {
	return sListGen.getClassesWithAncestor(ancestor);
    }

    /**
     * Get a string suitable for display to the user that describes className
     * @param className a String returned as a member of a Collection by getClassesWithAncestor
     * @return A short String description of className
     */
    public static String getClassShortname(String className) {
	return sListGen.getClassShortname(className);
    }

    public static String getClassByShortname(String shortName) {
	return (String)sClassShortNames.get(shortName);
    }
}

