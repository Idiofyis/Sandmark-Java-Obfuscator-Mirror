package sandmark.smash;

/**
 *  Smash.java -- 
 *  @author Jasvir Nagra <jas@cs.auckland.ac.nz>
 *  Created On      : Thu Jun  5 15:10:39 2003
 *  Last Modified   : <03/05/22 22:42:10 jas>
 *  Description     : Sandmark (again) shell
 *  Keywords        : sandmark shell
 *  PURPOSE
 *      | Sandmark project |
 */

public class Smash {
    
    public static final boolean DEBUG = false;

    public static void initVariables(koala.dynamicjava.interpreter.TreeInterpreter intr) {
        intr.defineVariable("PS1", "smash$ ");
        intr.defineVariable("PS2", "> ");
        intr.defineVariable("PATH", "");      
        intr.defineVariable("PROMPT_COMMAND", "");
    }

    public static boolean initScript(koala.dynamicjava.interpreter.TreeInterpreter intr) {
	String initscripts[] = new String[] { 
	    "sandmark/smash/init.smash","sandmark/smash/staticwm.smash",
	    "sandmark/smash/dynwm.smash","sandmark/smash/obfuscate.smash",
	};
        try {
	    ClassLoader loader = sandmark.smash.Smash.class.getClassLoader();
	    for(int i = 0 ; i < initscripts.length ; i++) {
		java.io.InputStream script = loader.getResourceAsStream(initscripts[i]);
		if(script == null)
		    throw new java.io.IOException("Couldn't find init script " + initscripts[i]);
		java.io.InputStreamReader reader = new java.io.InputStreamReader(script);
		intr.interpret(reader,initscripts[i]);
	    }
        }catch(java.io.IOException e) {
            System.out.println(e.getMessage());
	    return false;
        }
	return true;
    }

    public static void source(String file, 
		    koala.dynamicjava.interpreter.TreeInterpreter intr) {
        try {
            intr.interpret(file);
        } catch(java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        koala.dynamicjava.interpreter.TreeInterpreter interpreter
            = new koala.dynamicjava.interpreter.TreeInterpreter(
                         new koala.dynamicjava.parser.wrapper.JavaCCParserFactory());
        interpreter.setAccessible(true);
        java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(System.in));

        System.out.println("Initializing variables...");      
        initVariables(interpreter);

        System.out.println("Initializing script...");
        if(!initScript(interpreter)) {
	    System.out.println("initialization failed");
	    System.exit(1);
	}
	System.out.println("Initialization complete...\n");
	System.out.println("Type help(); at smash$ prompt for options\n");

        String list = "";
        String line;

        interpreter.interpret(new java.io.StringReader(
                        ""+interpreter.getVariable("PROMPT_COMMAND")),"STDIN");
        System.out.print("\n" + interpreter.getVariable("PS1"));
        try {
            while((line=reader.readLine())!=null) {       
                try {
                    if(!line.equals("")) {
                        /*String dummystr = "help(\"helloW\");";
                        interpreter.interpret(new java.io.StringReader(dummystr), "STDIN");*/
                        list = list + "\n" + line;
                        interpreter.interpret(new java.io.StringReader(list), "STDIN");
                        list = "";
                        interpreter.interpret(new java.io.StringReader(
                                ""+interpreter.getVariable("PROMPT_COMMAND")),"STDIN");
                        System.out.print("\n"+interpreter.getVariable("PS1"));
                    }
                    else {
                        //list = list + "\n" + line;
                        System.out.print(interpreter.getVariable("PS1"));
                    }
                }catch(koala.dynamicjava.interpreter.InterpreterException e) {
                    System.err.println("Parse error: "+e);
                    interpreter.interpret(new java.io.StringReader(
                                ""+interpreter.getVariable("PROMPT_COMMAND")),"STDIN");
                    System.out.print("\n"+interpreter.getVariable("PS1"));
                    list = "";
                }
            }
        } catch(java.io.IOException e) {
            System.err.println("Interpreter terminated: "+e);
        }
    }

    public static String[] findAlgorithms(int algType) {
        String algClassNames[] =
            (String[])
            sandmark.util.classloading.ClassFinder.getClassesWithAncestor
            (algType).toArray(new String[0]);
        java.util.Arrays.sort(algClassNames,new java.util.Comparator() {
                public int compare(Object o1,Object o2) {
                    return sandmark.util.classloading.ClassFinder.getClassShortname((String)o1).compareTo
                        (sandmark.util.classloading.ClassFinder.getClassShortname((String)o2));
                }
            });
        return algClassNames;
    }

    public static sandmark.Algorithm algo = null;
    public static sandmark.util.ConfigProperties props = null;

    public static String inputJar = null;
    public static String outputJar = null;
    public static String wmKey = null;
    public static String wmVal = null;
    
    public static sandmark.Algorithm getAlgorithmObj(String className)
    {
        System.out.println("looking for " + className);
        int CLASS_COUNT=sandmark.util.classloading.IClassFinder.CLASS_COUNT;
        for(int algtype=0; algtype<CLASS_COUNT; algtype++) {
            String[] algClassNames = findAlgorithms(algtype);
            for(int k=0; k<algClassNames.length; k++) {
                String shortName = 
                    sandmark.util.classloading.ClassFinder.getClassShortname(algClassNames[k]);
                if(DEBUG){
                    System.out.println("shortName - "+shortName);
                    System.out.println("algClassNames[] - "+algClassNames[k]);
                }
                if(shortName.equals(className)) {
                    Class c;
                    Object o;
                    try {
                        c = Class.forName(algClassNames[k]);
                        o = c.newInstance();
                    } catch(Throwable e) {
                        if(DEBUG)System.out.println(" Throwable: "+ e);
                        return null;
                    }
                    if(o instanceof sandmark.Algorithm) {
                        sandmark.Algorithm algObj = (sandmark.Algorithm)o;
                        return algObj;
                    }
                    else {
                        if(DEBUG)System.out.println(" Algorithm name not instance of sandmark.Algorithm...");
                        return null;
                    }
                }
            }
        }
        if(DEBUG)System.out.println(" Algorithm name match not found ...");
        return null;
    }

    public static java.util.Iterator wmRetrieveIter;
    public static boolean traceInProgress = false;

}
