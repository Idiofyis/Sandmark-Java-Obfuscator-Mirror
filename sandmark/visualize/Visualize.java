package sandmark.visualize;

/**
 *  Visualize.java -- 
 *  @author Jasvir Nagra <jas@cs.auckland.ac.nz>
 *  Created On      : Sat May 10 14:26:36 2003
 *  Last Modified   : <03/05/10 17:32:46 jas>
 *  Description     : Visualize things
 *  Keywords        : visualization agave sandmark
 *  PURPOSE
 *  	| Sandmark project |
 */

public class Visualize {

    /**
     *  This method is executed in response to the user
     *  selecting the visualize tab.
        @param  f
     */
    public static void visualizePaneSelected(sandmark.gui.SandMarkFrame f) {}

    /**
     *  This method is executed in response to the user
     *  deselecting the visualize tab.
        @param  f
     */
    public static void visualizePaneDeselected(sandmark.gui.SandMarkFrame f) {}

    /**
     *  This method is executed in response to the user
     *  clicking on the visualize button in the visualize tab.
     *  We check that the right arguments have been filled
     *  in. If so, we run the actual visualizer.
        @param  f
     */
    public static void visualizeButtonPressed(sandmark.gui.SandMarkFrame f) {
        String[] data={}; // = f.getVisualizeData();

        if (!(new java.io.File(data[0])).exists())
            {
                sandmark.util.Log.message(0, "File not found: " + data[0]);
                return;
            }

        sandmark.util.Log.message(0,"Visualizing jar-file...");
    }


    /**
     *  Get the GENERAL properties of Visualize
     */
    private static sandmark.util.ConfigProperties sConfigProps;
    public static sandmark.util.ConfigProperties getProperties(){
        if(sConfigProps == null) {
            String[][] props = {
                {"VISUALIZE_JarInput",
                 "",
                 "The jar-file which we want to visualize.",
                 null,"F",
                },
            };
            sConfigProps = new sandmark.util.ConfigProperties(props,sandmark.Console.getConfigProperties());
        }
        return sConfigProps;
    }

    /**
     *  Get the HTML codes of the About page for Visualize
        @return html code for the about page
     */
    public static java.lang.String getAboutHTML(){
        return "<HTML><BODY>Visualize various thingies</BODY></HTML>";
    }

    /**
     *  Get the URL of the Help page for Visualize
        @return URL for the help page
     */
    public static java.lang.String getHelpURL(){
        return "sandmark/visualize/doc/help.html";
    }

    /*
     *  Describe what visualizing is.
     */
    public static java.lang.String getOverview(){
	return "This pane allows you to visualize the internals and execution " +
	    "of a jar file. ";
    }
}

//  Emacs bunkum
//  Local Variables:
//  mode: jde
//  time-stamp-start: "Last Modified[ \t]*:[ 	]+\\\\?[\"<]+"
//  time-stamp-end:   "\\\\?[\">]"
//  End:

