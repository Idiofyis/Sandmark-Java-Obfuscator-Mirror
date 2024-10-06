package sandmark.watermark;

/**
 *  A GeneralWatermarker object encapsulates code for running
 *  a particular watermark algorithm.
 *  Watermarks are grouped into two abstract subclasses:
 *  <UL>
 *	<LI> StaticWatermarker
 *	<LI> DynamicWatermarker
 *  </UL>
 *
 *  <P> A watermarker has a label that is used to refer to it
 *  in the GUI.
 */

public abstract class GeneralWatermarker extends sandmark.Algorithm 
    implements sandmark.AppAlgorithm {

    /**
     *  Get the GENERAL properties of watermark
     */
    private static sandmark.util.ConfigProperties sConfigProps;
   public static sandmark.util.ConfigProperties getProperties(){
       if(sConfigProps == null) {
           sConfigProps = 
               new sandmark.util.ConfigProperties
               (null,sandmark.Console.getConfigProperties());
       }
       return sConfigProps;
   }

    public sandmark.util.ConfigProperties getConfigProperties() {
        return getProperties();
    }

    /**
     *  Specifies the description given in the About page for Watermarking.
     *  @return an HTML formatted description of the algorithm.
     */
    public static java.lang.String getAboutHTML(){
	return 
	    "<HTML><BODY>" +
	    "<CENTER><B>List of Watermarking Algorithms</B></CENTER>" +
	    "</BODY></HTML>";
    }

    /**
     *  Specifies the URL of the Help page for Watermarking.
     *  @return the Help page URL
     */
    public static java.lang.String getHelpURL(){
	return "sandmark/watermark/doc/watermarking.html";
    }


}

