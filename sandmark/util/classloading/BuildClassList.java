package sandmark.util.classloading;

public class BuildClassList {
    public static void main(String argv[]) throws Exception {
	java.util.Hashtable ht =
	    new java.util.Hashtable();
	for(int i = 0 ; i < IClassFinder.CLASS_IDS.length ; i++) {
	    java.util.Iterator it = 
		ClassFinder.getClassesWithAncestor(i).iterator();
	    while(it.hasNext()) {
		String className = (String)it.next();
		String implStr = (String)ht.get(className);
		if(implStr == null)
		    ht.put(className,IClassFinder.CLASS_IDS[i]);
		else
		    ht.put(className,implStr.concat("," + IClassFinder.CLASS_IDS[i]));
	    }
	}
	String outputFileName = argv[0];
	java.io.PrintWriter outputFile = 
	    new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(outputFileName)));
	java.util.Enumeration classNames = ht.keys();
	while(classNames.hasMoreElements()) {
	    String className = (String)classNames.nextElement();
	    String implStr = (String)ht.get(className);
	    outputFile.println(className + ":" + 
			       ClassFinder.getClassShortname(className) +
			       ":" + implStr);
	}
	outputFile.close();
    }
}

