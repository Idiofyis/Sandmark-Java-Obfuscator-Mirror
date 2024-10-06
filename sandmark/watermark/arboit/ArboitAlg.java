package sandmark.watermark.arboit;

/**
 * This algorithm embeds a watermark by inserting opaque predicates in selected
 * branch statements. The algorithm is based on Genevieve Arboit's Algorithms 1
 * and 2 from "A Method for Watermarking Java Programs via Opaque Predicates." 
 */

public class ArboitAlg extends sandmark.watermark.StaticWatermarker {

   private static boolean EVAL = false;
   static int USE_CONSTS = 0;
   static int USE_RANK = 1;

   public ArboitAlg() {}

   /**
    *  Returns this watermarker's short name.
    */
   public String getShortName() {
      return "Static Arboit";
   }
    
   /**
    *  Returns this watermarker's long name.
    */
   public String getLongName() {
	   return "Embeds a watermark by inserting opaque predicates";
   }


   private sandmark.util.ConfigProperties mConfigProps;
   public sandmark.util.ConfigProperties getConfigProperties(){

      if(mConfigProps == null){
         String props[][] = new String[][] {
            //{"MIN_WM_PARTS",
            // "5", 
            // "An integer describing the number of parts the watermark should " + 
             //"be broken up into. There must be atleast 2",
             //"5", "S", "SE",
            //},
            //{"ENCODE_AS_CONSTS",
            {"Encode as constants",
             "true",
             "Encode the watermark either as constants in the opaque predicate "
             + "or using the rank.",
             "true", "B", "SE,SR",
            },
            //{"USE_OPAQUE_METHODS",
            {"Use opaque methods",
             "true",
             "Encode the watermark either as an inserted opaque method or " +
             "as an inserted opaque predicate.",
             "true", "B", "SE,SR",
            },
            //{"REUSE_METHODS",
            {"Reuse methods",
             "false",
             "Opaque methods can be reused when rank is used to encode the " + 
             "value of the watermark.",
             "false", "B", "SE",
            }};
         mConfigProps = new sandmark.util.ConfigProperties(props,null);
      }
      return mConfigProps;
   }


   /*
    *  Get the HTML codes of the About page.
    */
   public java.lang.String getAlgHTML(){
	   return 
           "<HTML><BODY>\n" +
           "Arboit is a watermarking algorithm that embeds the watermark by" +
           " appending opaque predicates to branches chosen throughout the" +
           " application. Each opaque predicate encodes part of the watermark" +
           " either through the constants in the predicate or a rank assigned" +
           " to the predicate. This algorithm is based on Genevieve Arboit's" +
           " watermarking algorithm as described in A Method for" +
           " Watermarking Java Programs via Opaque Predicates." +
           "<table>\n" +
	    "<TR><TD>\n" +
	    "   Author: <a href=\"mailto:mylesg@cs.arizona.edu\">Ginger Myles</a>\n" +
	    "</TR></TD>\n" +
           "</table>\n" +
           "</BODY></HTML>\n";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return "Arboit is a watermarking algorithm that embeds the watermark "
         + "by appending opaque predicates to branches chosen throughout the" +
           " application. Each opaque predicate encodes a portion of the" +
           " watermark. This algorithm is based on Genevieve Arboit's" +
           " watermarking algorithm as described in A Method for" +
           " Watermarking Java Programs via Opaque Predicates.";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
         sandmark.config.ModificationProperty.I_ADD_METHODS};
      return properties;
   }

   public sandmark.config.RequisiteProperty[] getPostprohibited(){
      sandmark.config.RequisiteProperty[] properties = {
         sandmark.config.ModificationProperty.I_CHANGE_METHOD_SIGNATURES,
         sandmark.config.ModificationProperty.I_REMOVE_METHODS,
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
	   };
      return properties;
   }

    /*
     *  Get the URL of the Help page
     */
   public java.lang.String getAlgURL(){
	   return "sandmark/watermark/arboit/doc/help.html";
   }


/*************************************************************************/
/*                               Embedding                               */
/*************************************************************************/

   public void embed(sandmark.watermark.StaticEmbedParameters params)  
      throws sandmark.watermark.WatermarkingException {

      //check if this app is valid to watermark  
      if(params.app == null || !UtilFunctions.isAppValid(params.app))
         throw new sandmark.watermark.WatermarkingException(
            "Embedding Failed. There must be at least one class to watermark.");

/*
      sandmark.util.ConfigProperties mycp = null;
      if(mConfigProps == null){
         System.out.println("in here");
         mycp = getConfigProperties();
         //props.setProperty("MIN_WM_PARTS", mycp.getProperty("MIN_WM_PARTS"));
         props.setProperty("Encode as constants", mycp.getProperty("Encode as constants"));
         props.setProperty("Use opaque methods", mycp.getProperty("Use opaque methods"));
         props.setProperty("Reuse methods", mycp.getProperty("Reuse methods"));
*/
/*
          
          java.util.Enumeration e = mycp.keys();
          while(e.hasMoreElements()){
             String nextKey = (String)e.nextElement();
             props.setProperty(nextKey, (String)mycp.getProperty(nextKey));
          }
*/
/*
      }
      boolean success = UtilFunctions.watermark(app, props);
*/
      boolean success = UtilFunctions.watermark(params.app, params.watermark, 
                                                params.key, getConfigProperties());

      if(!success)
         throw new sandmark.watermark.WatermarkingException(
            "This watermark is too long for this application. Embedding failed.");
   }//end embed

/*************************************************************************/
/*                              Recognition                              */
/*************************************************************************/

   /* An iterator which generates the watermarks
    * found in the program.
    */
   class Recognizer implements java.util.Iterator {
      java.util.Vector result = new java.util.Vector();
      String consts_or_rank;
      int current = 0;

      public Recognizer(sandmark.watermark.StaticRecognizeParameters params) {
         generate(params);
      }

      public void generate(sandmark.watermark.StaticRecognizeParameters params) {
         String watermark;

         if(!UtilFunctions.isAppValid(params.app))
            watermark = "";
         else
            watermark = UtilFunctions.recover(params.app, getConfigProperties());

         if(EVAL)System.out.println("watermark recovered: " + watermark);
         result.add(watermark);
      } //end generate()

      public boolean hasNext() {
         return current < result.size();
      }

      public java.lang.Object next() {
         return result.get(current++);
      }

      public void remove() {}
   } //end class Recognizer

   /* Return an iterator which generates the watermarks
    * found in the program. The props argument
    * holds at least the following properties:
    *  <UL>
    *     <LI> WM_Recognize_JarInput: The name of the file to be watermarked.
    *  </UL>
    */
   public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
     throws sandmark.watermark.WatermarkingException {
      return new Recognizer(params);
   }


} // class ArboitAlg
