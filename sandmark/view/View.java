package sandmark.view;

/**

        @author         Christian Collberg
        @version        1.0
*/
public class View  {


    /**
     *  Get the GENERAL properties of View
     */
    private static sandmark.util.ConfigProperties sConfigProps;
    public static sandmark.util.ConfigProperties getProperties(){
        if(sConfigProps == null) {
            String[][] props = {
                {"VIEW_JarInput",
                 "",
                 "The jar-file which we want to view.",
                 null,"F",
                },
            };
            sConfigProps = new sandmark.util.ConfigProperties(props,sandmark.Console.getConfigProperties());
        }
        return sConfigProps;
    }

    /**
     *  Get the HTML codes of the About page for View
        @return html code for the about page
     */
    public static java.lang.String getAboutHTML(){
        return "<HTML><BODY>This is View.java</BODY></HTML>";
    }

    /**
     *  Get the URL of the Help page for View
        @return URL for the help page
     */
    public static java.lang.String getHelpURL(){
        return "sandmark/view/doc/help.html";
    }

    /*
     *  Describe what viewing is.
     */
    public static java.lang.String getOverview(){
	return "This pane allows you to view the bytecode methods " +
               "of a jar file. The methods can be sorted by software " +
               "complexity metrics. This allows you to launch manual " +
               "attacks against a software watermark.";
    }
}

