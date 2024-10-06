package sandmark.newstatistics;

/**

        @author         Christian Collberg
        @version        1.0
*/
public class Statistics  {


    /**
     *  Get the GENERAL properties of Statistics
     */
    private static sandmark.util.ConfigProperties sConfigProps;
    public static sandmark.util.ConfigProperties getProperties(){
        return sandmark.Console.getConfigProperties();
    }

    /**
     *  Get the HTML codes of the About page for Statistics
        @return html code for the about page
     */
    public static java.lang.String getAboutHTML(){
        return "<HTML><BODY>This is Statistics.java</BODY></HTML>";
    }

    /**
     *  Get the URL of the Help page for Statistics
        @return URL for the help page
     */
    public static java.lang.String getHelpURL(){
        return "sandmark/newstatistics/doc/help.html";
    }

    /*
     *  Describe what statistics is.
     */
    public static java.lang.String getOverview(){
	return "This pane allows you to compute Software Complexity " +
               "Metrics for a jar file. These are used by SandMark's " +
               "obfuscation loop to choose which obfuscation algorithm " +
               "to apply to which part of the program.";
    }
}

