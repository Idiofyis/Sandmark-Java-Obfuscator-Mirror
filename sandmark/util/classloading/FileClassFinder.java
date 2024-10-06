package sandmark.util.classloading;

/*
  Algorithms.txt format:
  full.class.name:Short Name (no colons or commas allowed):APP_OBFUSCATOR,...,STAT_WATERMARKER
  The last field is a list of the algorithms that this class implements.
*/

/**
 * Provides a list of classes, short descriptions of those classes, and 
 * which of the classes specified in sandmark.util.classloading.IClassFinder the
 * classes derive from, based on the contents of Algorithms.txt, a text file
 * found by a call to ClassLoader.getSystemClassLoader.getResource("Algorithms.txt")
 * @see sandmark.util.classloading.IClassFinder
 * @author Andrew Huntwork
 * @version 1.0
 */

class FileClassFinder
    implements sandmark.util.classloading.IClassFinder {
    private static boolean initSucceeded = false;
    private static java.util.Hashtable sAlgNameToNdx = 
	new java.util.Hashtable();
    private static java.util.List[] sClassesByAncestor;
    private static java.util.Hashtable sClassShortNames =
	new java.util.Hashtable();
    static {
	sClassesByAncestor = new java.util.List[CLASS_COUNT];
	for(int i = 0 ; i < CLASS_COUNT ; i++)
	    sClassesByAncestor[i] = new java.util.ArrayList();
	for(int i = 0 ; i < CLASS_COUNT ; i++)
	    sAlgNameToNdx.put(CLASS_IDS[i],new java.lang.Integer(i));
    };
    public java.util.Collection getClassesWithAncestor(int algoType) {
	if(ClassFinder.debug) {
	    System.out.println("classes with ancestor " + algoType
			       + ":");
	    java.util.Iterator it = 
		sClassesByAncestor[algoType].iterator();
	    while(it.hasNext()) {
		String s = (String)it.next();
		System.out.println("\t" + s);
	    }
	}
	return sClassesByAncestor[algoType];
    }
    public String getClassShortname(String className) {
	return (String)sClassShortNames.get(className);
    }
    private void loadClassList() {
	java.io.InputStream algoFileStream = 
	    getClass().getClassLoader().getResourceAsStream("Algorithms.txt");
	if(algoFileStream == null) {
	    return;
	}
	java.io.BufferedReader aFSBufferedReader = 
	    new java.io.BufferedReader(new java.io.InputStreamReader(algoFileStream));
	try {
	    String line;
	    while((line = aFSBufferedReader.readLine()) != null) {
		String[] fields = line.split("\\:|\\,");
		sClassShortNames.put(fields[0],fields[1]);
		for(int i = 2 ; i < fields.length ; i++) {
		    if(sAlgNameToNdx.get(fields[i]) != null)
			sClassesByAncestor[((java.lang.Integer)sAlgNameToNdx.get(fields[i])).intValue()].add(fields[0]);
		}
	    }
	    initSucceeded = true;
	} catch(java.io.IOException e) {
	    if(ClassFinder.debug) {
		System.out.println(e);
		e.printStackTrace();
	    }
	}
    }
    public FileClassFinder() throws Exception {
        loadClassList();
	if(!initSucceeded)
	    throw new Exception();
    }
}

