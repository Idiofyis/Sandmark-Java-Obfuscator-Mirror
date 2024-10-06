package sandmark.util.classloading;

/**
 * Provides a list of classes, short descriptions of those classes, and 
 * which of the classes specified in sandmark.util.classloading.IClassFinder the
 * classes derive from, based on the contents of the directory specified
 * in the system property "SMARK_ROOT"
 * @see sandmark.util.classloading.IClassFinder
 * @author Andrew Huntwork
 * @version 1.0
 */

class DirClassFinder 
implements sandmark.util.classloading.IClassFinder {
    private static Class[] sAncestorsByNumber;
    private static java.lang.Throwable sT;
    private static boolean didLoad = false;
    private static java.util.Set[] sClassesByAncestor;
    public java.util.Collection getClassesWithAncestor(int ancestor) {
        return sClassesByAncestor[ancestor];
    }
    public String getClassShortname(String className) {
        Class c;
        Object o;
        try {
            c = Class.forName(className);
            o = c.newInstance();
        } catch(Throwable e) {
            return "";
        }
        if(o instanceof sandmark.Algorithm) {
            sandmark.Algorithm algo = 
                (sandmark.Algorithm)o;
            return algo.getShortName();
        }
        return className;
    }
    public DirClassFinder() throws java.lang.Throwable {
        loadClasses();
        if(sT != null)
            throw sT;
    }
    private void loadClasses() {
        if(didLoad)
            return;
        didLoad = true;
        
        ClassLoader loader = getClass().getClassLoader();
        
        sClassesByAncestor =
            new java.util.Set[CLASS_COUNT];
        for(int i = 0 ; i < CLASS_COUNT ; i++)
            sClassesByAncestor[i] = new java.util.HashSet();
        try {
            sAncestorsByNumber = new Class[CLASS_COUNT];
            for(int i = 0 ; i < CLASS_COUNT ; i++)
                sAncestorsByNumber[i] = loader.loadClass(CLASS_NAMES[i]);
            java.io.File dir = new java.io.File
                (System.getProperty("SMARK_ROOT") + "/sandmark/");
            findFiles(dir,sAncestorsByNumber,sClassesByAncestor,loader);
        } catch(Throwable t) {
            if(ClassFinder.debug)
                t.printStackTrace();
            sT = t;
        }
    }
    private static void findFiles(java.io.File dir,Class supers[],
                                  java.util.Set subs[],ClassLoader loader) {
        if(ClassFinder.debug)
            System.out.println("dir: " + dir);
        java.io.File[] classFiles = dir.listFiles(new java.io.FilenameFilter() {
            public boolean accept(java.io.File _dir, String name) {
                return name.endsWith(".class");
            }
        });
        
        for (int i = 0; i < classFiles.length; i++) {
            if(ClassFinder.debug)
                System.out.println("classes: " + classFiles[i]);
            String className = null;
            try {
                className = classFiles[i].toString();
                int ind= className.lastIndexOf("sandmark");
                className = className.substring(ind,className.length());
                className = className.substring(0,className.length() - 6);
                className = className.replace(java.io.File.separatorChar,'.');
                
                Class clazz = loader.loadClass(className);
                
                for(int j = 0 ; j < supers.length ; j++) {
                    if(supers[j].isAssignableFrom(clazz) &&
                       clazz.newInstance() != null)
                        subs[j].add(clazz.getName());
                }
            } catch(Throwable t) {
                if(ClassFinder.debug) {
                    t.printStackTrace(); 
                    System.out.println("can't load " + className + ": " + t);
                }
            }
        }
        
        java.io.File[] subdirs = dir.listFiles(new java.io.FileFilter() {
            public boolean accept(java.io.File file) {
                return file.isDirectory();
            }
        });       
        
        for (int i = 0; i < subdirs.length; i++) {
            if(ClassFinder.debug)
                System.out.println("dirs: " + subdirs[i]);
            findFiles(subdirs[i],supers,subs,loader);
        }
    }
}

