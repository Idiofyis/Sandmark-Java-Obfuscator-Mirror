package sandmark;

/**
*  The sandmark.Console class presents a graphical interface
*  to the SandMark system.
   @author     Christian Collberg
   @version 1.0
*/

public class Console {

    /**

    */
    public final static String versionString = "$Revision: 1.78.2.1 $";

    private static sandmark.util.ConfigProperties sConfigProps;
    public static sandmark.util.ConfigProperties getConfigProperties(){
        if(sConfigProps == null) {
	    String args[][] = {
                {"Input File",
                 "",
                 "The input jar file.",
                 null,"J","A",
                },
	    };
            sConfigProps = new sandmark.util.ConfigProperties(args,null);
	}
        return sConfigProps;
    }

    /**
     *  Get the URL for the Help page for Sandmark
   @return url for help page
     */
    public static java.lang.String getHelpURL(){
   return "sandmark/html/sandmark.html";
    }

    //--------------------------------------------------------------
    //                            Utils
    //--------------------------------------------------------------

    /**
     * Construct a new filename based on the 'input' filename.
     * If the input filename is xxxx.jar, then the output filename
     * becomes xxxx_suffix.jar. If the output filename already
     * exists, no change is made.
     * 
     *    @param output         The output filename.
     *    @param input          The input filename.
     *    @param suffix         The string added to the filename.
     *                          Should not contain the "_".
     */
    public static String constructOutputFileName(String in,String suf)
    {
        return in.endsWith(".jar")
           ? in.substring(0, in.length() - 4) + "_" + suf + ".jar"
           : "";
    }

    /**
     * Experimental method that checks that the basic requirements for running Sandmark
     * have been met
     *
     * I am a little torn about whether to do all the tests and say
     * all the ways in which current installation will fail or report
     * the first failure and quit because its likely that fixing the
     * first fixes them all.
     */
    public static void sanityCheck () {
    	String errors = "";
    	int i = 0;
    	String[] checks = { 
    			"java.util.regex.Pattern", "JDK version 1.4",
    			"com.sun.jdi.Bootstrap", "tools.jar in CLASSPATH", 
    	};
    	
    	try {
    		for ( i=0; i < checks.length; i+=2 ) {
    			Class.forName ( checks[i] );
    		}
    	} catch ( ClassNotFoundException e ) {
    		errors += "SandMark requires " + checks[i + 1] + "\n";
    	}
    	if(!errors.equals(""))
    		throw new UnsupportedOperationException(errors);
    }

} // class Console


