package sandmark.watermark.arboit;

public class DynamicAA extends sandmark.watermark.DynamicWatermarker{

   private static boolean DEBUG = false;
   private static boolean EVAL = false;
   private sandmark.watermark.DynamicRecognizeParameters mRecognizeParams;
   private sandmark.watermark.DynamicTraceParameters mTraceParams;

   /**
    * Returns this watermarker's short name.
    */
   public String getShortName(){
      return "Dynamic Arboit";
   }

   /**
    * Returns this watermarker's long name.
    */
   public String getLongName() {
        return "A Dynamic Version of Genevieve Arboit's Watermarking Algorithm";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return 
         "DynamicAA is a watermarking algorithm that embeds the watermark "
         + "by appending opaque predicates to branches chosen throughout the" +
           " application. Each opaque predicate encodes a portion of the" +
           " watermark. This algorithm is based on Genevieve Arboit's" +
           " watermarking algorithm as described in A Method for" +
           " Watermarking Java Programs via Opaque Predicates.";
   }

   public sandmark.config.ModificationProperty[] getMutations()
    {
        return null;
    }

   /*
    * Get the properties of the DynamicAA algorithm.
    */
   private sandmark.util.ConfigProperties mConfigProps;
   public sandmark.util.ConfigProperties getConfigProperties(){
      if(mConfigProps == null) {
         String[][] props = {
            //{"MIN_WM_PARTS",
            // "5",
            // "An integer describing the number of parts the watermark should " + 
            // "be broken up into.",
            // null, "I", "DE",
            //},
            {"Encode as constants",
             "true",
             "Encode the watermark either as constants in the opaque predicate "
             + "or using the rank.",
             "true", "B", "DE,DR",
            },
            {"Use opaque methods",
             "true",
             "Encode the watermark either as an inserted opaque method or " +
             "as an inserted opaque predicate.",
             "true", "B", "DE,DR",
            },
            {"Reuse methods",
             "false",
             "Opaque methods can be reused when rank is used to encode the " + 
             "value of the watermark.",
             "false", "B", "DE,DR",
            },
            {"Key",
             "", "", null, "S", "N"
            },
            {"DWM_AA_AnnotatorClass",
             "sandmark.watermark.arboit.trace.Annotator",
             "The class which the user should make calls to when annotating a program." +
             "This property should not have to be changed.",
             null,"S","N",
            },
         };
         mConfigProps = new sandmark.util.ConfigProperties(props,null);
      }
      return mConfigProps;
   }

   /*
    *  Get the HTML codes of the About page for DynamicAA
    */
   public java.lang.String getAlgHTML(){
      return
           "<HTML><BODY>\n" +
           "The DynamicAA software watermarking algorithm is " +
           "a dynamic algorithm that embeds the watermark using opaque "+
           "predicates and runtime information. This algorithm is based on" +
           " Genevieve Arboit's watermarking " + 
           "algorithm as described in A Method for Watermarking Java " +
           "Programs via Opaque Predicates." +
           "<table>\n" +
           "<TR><TD>\n" +
           "   Authors: <a href=\"mailto:mylesg@cs.arizona.edu\">Ginger Myles</a><BR>\n" +
           "</TR></TD>\n" +
           "</table>\n" +
           "</BODY></HTML>\n";
   }

   /*
    *  Get the URL of the Help page for DynamicAA
    */
   public java.lang.String getAlgURL(){
      return "sandmark/watermark/arboit/doc/help.html";
   }
/*
   public void waitForProgramExit(){}

   private void annotate(java.util.Properties props) {
      try{
         if(props.getProperty("DWM_AA_AnnotatedJar").equals("")){
            String annotatedJarName =
               sandmark.Console.constructOutputFileName(
               props.getProperty("Input File"), "anno");
            System.out.println("annotated Jar Name: " + annotatedJarName);
            props.setProperty("DWM_AA_AnnotatedJar", annotatedJarName);
         }
         sandmark.watermark.arboit.trace.Annotate anno =
            new sandmark.watermark.arboit.trace.Annotate(props);
         anno.annotate();
         anno.save();
         String newClassPath = sandmark.Console.getSystemClassPath() +
            java.io.File.pathSeparatorChar + props.getProperty("Class Path") +
            java.io.File.pathSeparatorChar +
            props.getProperty("DWM_AA_AnnotatedJar");
         props.setProperty("Computed Class Path", newClassPath);
      } catch (Exception e) {
         e.printStackTrace();
         sandmark.util.Log.message(0, "Annotation failed", e);
      }
*/
   private void annotate(sandmark.program.Application app,
                         java.io.File appFile,
                         sandmark.util.ConfigProperties props) 
       throws java.io.IOException {
       sandmark.watermark.arboit.trace.Annotate anno =
           new sandmark.watermark.arboit.trace.Annotate(app,props);
       anno.annotate();
       anno.save(appFile);

   }

   sandmark.watermark.arboit.trace.Tracer tracer = null;

   public void startTracing(sandmark.watermark.DynamicTraceParameters params)
      throws sandmark.util.exec.TracingException {
       try {
           annotate(params.app,params.appFile,getConfigProperties());
           tracer = new sandmark.watermark.arboit.trace.Tracer
           (params.programCmdLine,getConfigProperties());
           tracer.run();
       } catch(java.io.IOException e) {
           throw new sandmark.util.exec.TracingException();
       }
   }

   public void endTracing()
      throws sandmark.util.exec.TracingException {

      sandmark.watermark.arboit.trace.TracePoint annotationPoints[] =
         (sandmark.watermark.arboit.trace.TracePoint[])
         tracer.getTracePoints().toArray(
         new sandmark.watermark.arboit.trace.TracePoint[0]);
      try {
         sandmark.watermark.arboit.trace.TracePoint.write(mTraceParams.traceFile,
            annotationPoints);
         sandmark.util.Log.message(0, "Trace points written to file: '" +
            mTraceParams.traceFile + "'.");
      } catch (Exception e) {
         sandmark.util.Log.message(0, "Failed to write the trace file: '" +
            mTraceParams.traceFile + "'.", e);
      }

      String traceLogFile = "TracePoints.txt";
      try {
         String traceLog = sandmark.watermark.arboit.trace.TracePoint.toString(annotationPoints);
         sandmark.util.Misc.writeToFile(traceLogFile, traceLog);
         sandmark.util.Log.message(0,"A trace point log has been written to file: '" + traceLogFile + "'.");
     } catch (Exception e) {
         sandmark.util.Log.message(0,"Failed to write the trace log file: '" + traceLogFile + "'.", e);
     }


   }

   public void stopTracing() throws sandmark.util.exec.TracingException {
      tracer.STOP();
   }

/***********************************************************************/
/*                              Embedding                              */
/***********************************************************************/
   public void embed(sandmark.watermark.DynamicEmbedParameters params) {

       java.io.File traceFile = params.traceFile;

      sandmark.watermark.arboit.trace.TracePoint annotationPoints[] = null;
      try{
         annotationPoints =
            sandmark.watermark.arboit.trace.TracePoint.read(traceFile);
         if(DEBUG){
            for(int i=0; i < annotationPoints.length; i++){
               System.out.println(annotationPoints[i]);
            }
         }
      } catch (Exception e) {
         sandmark.util.Log.message(0, "Could not open trace-file '" + traceFile
            + "'", e);
         return;
      }
      if(annotationPoints.length == 0 || annotationPoints.length == 1){
         sandmark.util.Log.message(0, "Please re-run the trace to generate at" +
            " least two trace points.");
         return;
      }

      //embed
      boolean success = false;
      try{
         success = UtilFunctions.watermark(params.app, params, getConfigProperties(), annotationPoints);
      }catch (Exception e){
         sandmark.util.Log.message(0, "Unable to watermark.");
         e.printStackTrace();
      }
 
      if(!success)
         sandmark.util.Log.message(0,
            "This watermark is too long for this application. Embedding failed.");
      //remove annotation points
      //UtilFunctions.removeAnnotations(app);
   }//end embed

/***********************************************************************/
/*                            Recognition                              */
/***********************************************************************/
   sandmark.watermark.arboit.trace.Tracer tracer2 = null;
   public void startRecognition (sandmark.watermark.DynamicRecognizeParameters params) 
       throws sandmark.util.exec.TracingException {
       try {
           annotate(params.app,params.appFile,getConfigProperties()); 
           tracer = new sandmark.watermark.arboit.trace.Tracer
           (params.programCmdLine,getConfigProperties());
           tracer.run();
       } catch(java.io.IOException e) {
           throw new sandmark.util.exec.TracingException();
       }
   }

   private java.util.Vector result = new java.util.Vector();

   private void recover(sandmark.program.Application app,
                        java.io.File traceFile,sandmark.util.ConfigProperties props) {

      sandmark.watermark.arboit.trace.TracePoint annotationPoints[] = null;
      try{
         annotationPoints =
            sandmark.watermark.arboit.trace.TracePoint.read(traceFile);
         if(DEBUG){
            for(int i=0; i < annotationPoints.length; i++){
               System.out.println(annotationPoints[i]);
            }
         }
      } catch (Exception e) {
         sandmark.util.Log.message(0, "Could not open trace-file '" + traceFile
            + "'", e);
         return;
      }
      if(annotationPoints.length == 0 || annotationPoints.length == 1){
         sandmark.util.Log.message(0, "Please re-run the trace to generate at" +
            " least two trace points.");
         return;
      }

      //recover
      String watermark = UtilFunctions.recover(app, props, annotationPoints);
      if(EVAL)System.out.println("watermark: " + watermark);

      result.add(watermark);

      app.close(); 
   }

   public java.util.Iterator watermarks() {
      java.util.Iterator wms = result.iterator();
      if(DEBUG)System.out.println("has next: " + wms.hasNext());
      return wms;

   }

   public void stopRecognition() throws sandmark.util.exec.TracingException {
      tracer.STOP();
      
      try {
          java.io.File traceFile = java.io.File.createTempFile("sm",".tra");
          
          endTracing();
          recover(mRecognizeParams.app,traceFile,getConfigProperties());
      } catch(java.io.IOException e) {
          throw new sandmark.util.exec.TracingException();
      }
   }

   public void waitForProgramExit() {
      if(tracer == null)
	 return;
      
      synchronized(tracer) {
	 while(!tracer.exited())
	    try { tracer.wait(); }
	    catch(InterruptedException e) {}
      }
   }
}//end class DynamicAA
