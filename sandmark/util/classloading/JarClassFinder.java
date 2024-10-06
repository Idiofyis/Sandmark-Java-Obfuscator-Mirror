package sandmark.util.classloading;

/**
 * Provides a list of classes, short descriptions of those classes, and 
 * which of the classes specified in sandmark.util.classloading.IClassFinder the
 * classes derive from, based on the contents of the jar file specified
 * by the system property "SMARK_PATH"
 * @see sandmark.util.classloading.IClassFinder
 * @author Andrew Huntwork
 * @version 1.0
 */

class JarClassFinder
    implements sandmark.util.classloading.IClassFinder {
    private static java.util.jar.JarFile sJF;
    private static Class[] sAncestorsByNumber;
    private static Throwable sT;
    private static java.util.List[] sClassesByAncestor;
    private static boolean didLoad = false;
    static {
	sClassesByAncestor = 
	    new java.util.List[CLASS_COUNT];
	for(int i = 0 ; i < CLASS_COUNT ; i++)
	    sClassesByAncestor[i] = new java.util.ArrayList();
	try {
	    sJF = new java.util.jar.JarFile(System.getProperty("SMARK_PATH"));
	    sAncestorsByNumber = new Class[CLASS_COUNT];
	    for(int i = 0 ; i < CLASS_COUNT ; i++)
		sAncestorsByNumber[i] = Class.forName(CLASS_NAMES[i]);
	} catch(Throwable t) {
	    if(ClassFinder.debug)
		t.printStackTrace();
	    sT = t;
	}
    }
    public JarClassFinder() throws Throwable {
	loadClassFilesFromJar(); 
	if(sT != null)
	    throw sT;
    }
    private void loadClassFilesFromJar() throws Throwable {
               if(didLoad)
                   return;
               didLoad = true;
               
	java.util.Enumeration files = sJF.entries();
	java.lang.ClassLoader cl = getClass().getClassLoader();
	while(files.hasMoreElements()) {
	    Class c = null;
	    String name = "";
	    try {
		java.util.jar.JarEntry entry = (java.util.jar.JarEntry)files.nextElement();
		name = entry.getName();
		name = name.substring(0,name.length() - 6);
		name = name.replace(java.io.File.separatorChar,'.');
		name = name.substring(0,name.length());
		c = cl.loadClass(name);
		for(int i = 0 ; i < sAncestorsByNumber.length ; i++) {
		    if(sAncestorsByNumber[i].isAssignableFrom(c) &&
		            c.newInstance() != null) {
		        sClassesByAncestor[i].add(c.getName());
		    }
		}
	    } catch(StringIndexOutOfBoundsException e) {
		//the name is too short or something, ignore it.
	    } catch(ClassNotFoundException e) {
		//It's not a class, just some random file.  ignore it
	    } catch(Throwable e) {
		if(ClassFinder.debug) {
		   e.printStackTrace();
		    System.out.println("bad class: " + name + e);
		}
	    }
	    if(c != null) {
	    }
	}
    }
    public java.util.Collection getClassesWithAncestor(int ancestor) {
	if(ClassFinder.debug) {
	    System.out.println("classes with ancestor " + ancestor
			       + ":");
	    java.util.Iterator it = 
		sClassesByAncestor[ancestor].iterator();
	    while(it.hasNext()) {
		String s = (String)it.next();
		System.out.println("\t" + s);
	    }
	}
	return sClassesByAncestor[ancestor];
    }
    public String getClassShortname(String className) {
	Class c;
	Object o;
	try {
	    c = Class.forName(className);
	    o = c.newInstance();
	} catch(Throwable e) {
	    if(ClassFinder.debug) {
		System.out.println("bad class " + className);
		System.out.println(e);
		e.printStackTrace();
	    }
	    return "";
	}
	if(o instanceof sandmark.Algorithm) {
	    sandmark.Algorithm algo = 
		(sandmark.Algorithm)o;
	    if(false)
		System.out.println("returning shortname " +
				   algo.getShortName() + 
				   " for class " + className);
	    return algo.getShortName();
	}
	if(ClassFinder.debug)
	    System.out.println("returning classname as shortname for " +
			       className);
	return className;
    }
}

