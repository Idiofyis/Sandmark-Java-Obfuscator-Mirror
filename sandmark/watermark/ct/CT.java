package sandmark.watermark.ct;

/**
 *  A StaticWatermarker object encapsulates code for running
 *  a particular static watermark algorithm.
 */

public class CT extends sandmark.watermark.DynamicWatermarker{
    
   private sandmark.watermark.DynamicTraceParameters mTraceParams;
    
   /**
    *  Returns this watermarker's short name.
    */
   public String getShortName() {
      return "Collberg/Thomborson";
   }
    
   /**
    *  Returns this watermarker's long name.
    */
   public String getLongName() {
      return "The Collberg-Thomborson Watermarking Algorithm";
   }

   public String getAuthor(){
      return "Christian Collberg";
   }

   public String getAuthorEmail(){
      return "collberg@cs.arizona.edu";
   }

   public String getDescription(){
      return
         "The Collberg-Thomborson software watermarking algorithm is " +
         "a dynamic algorithm that embeds the watermark into the " +
         "toplogy of a graph structure that is built at runtime " +
         "in response to a sequence of special user actions.\n " +
         "To use this algorithm you must first embed calls to " +
         "sandmark.watermark.ct.trace.Annotator.sm$mark() into " +
         "your program. These points represent the locations where " +
         "watermark code can be inserted. Then run a trace with a " +
         "special input sequence, and finally embed your watermark.";
   }

   public sandmark.config.ModificationProperty[] getMutations()
   {
      return null;
   }

   /*
    *  Get the properties of CT algorithm
    */
   private sandmark.util.ConfigProperties mConfigProps;
   public sandmark.util.ConfigProperties getConfigProperties(){
      if(mConfigProps == null) {
         String[][] props = {
            {"Numeric Watermark",
             "false",
             "",null,"B","DE,DR",
             "Pure numeric watermarks are encoded more efficiently than watermarks that " +
             "can be arbitrary strings."},		 
            {"DWM_CT_AnnotatorClass",
             "sandmark.watermark.ct.trace.Annotator",
             "The class which the user should make calls to when annotating a program." +
             "This property should not have to be changed.",
             null,"S","N",
            },
            {"DWM_CT_Encode_ParentClass",
             "java.lang.Object",
             "The class which the Watermark.java class inherits from." +
             "This property should not have to be changed.",
             null,"S","N",
            },
            {"DWM_CT_Encode_ClassName",
             "Watermark",
             "The name of the class that builds the watermark. " +
             "This property should not have to be changed.",
             null,"S","N",
            },
            {"DWM_CT_Encode_Package",
             "",
             "The name of the package in which the watermark class should be declared. ",
             null,"S","N",
            },
            {"Node Class",
             "Watermark",
             "The name of the class that defines watermark graph nodes. " +
             "This property is calculated by SandMark itself if"+
             "it is left to default and Replace Watermark Class is set to true ",
             null,"S","DE",
            },
            {"DWM_CT_Encode_AvailableEdges",
             "",
             "The out-edges usable in Node Class to store graph edges. " +
             "This property should normally be calculated by SandMark itself.",
             null,"S","N",
            },
            {"Storage Policy",
             "root",
             "Either 'root' or 'all'. 'root' means that only roots of subgraphs " +
             "are stored globally (or passed around in formal parametrs." +
             "'all' means that all graph nodes are stored.",
             null,"S","DE",
            },
            {"Storage Method",
             "array:vector:hash",
             "A colon-separated list of 'vector', 'array', 'pointer', and 'hash'. " +
             "These are the types of storage containers in which subgraph nodes " +
             "are stored. " +
             "'vector' means 'java.util.Vector' " +
             "'array' means 'Watermark[]'. " +
             "'hash' means 'java.util.Hashtable. " +
             "'pointer' means 'Watermark n1,n2,.... " +
             "NOTE: 'pointer' currently doesn't work!",
             null,"S","DE",
            },
            {"Storage Location",
             "formal",
             "Either 'global' or 'formal'. " +
             "'global' means that subgraph nodes are stored in global static " +
             "variables. " +
             "'formal' means that subgraph nodes are passed around in method parameters." +
             "NOTE: 'formal' currently doesn't work!",
             null,"S","DE",
            },
            {"Protection Method",
             "if:safe:try",
             "Colon-separated list of 'if', 'safe', 'try'. " +
             "'if' means that we protect against null pointers using 'if(n!=null)...'. " +
             "'safe' means that we use 'n=(n!=null)?n:new Watermark'. " +
             "'try' means that we use 'try{...}catch(...){}'.",
             null,"S","DE",
            },
            {"Graph Type",
             "*",
             "Which graph codec to use.  '*' means choose randomly.  Otherwise, " +
             "use a complete package and class name for a class that implements " +
             "sandmark.util.newgraph.codec.GraphCodec.",
             null,"S","DE,DR",
            },
            {"Use Cycle Graph",
             "true",
             "To protect against node-splitting attacks, transform the underlying " +
             "graph such that every node becomes a 3-cycle. Any node split will " +
             "just expand the length of the cycle. During recognition, the cycles " +
             "are contracted to generate the original graph.",
             null,"B","DE,DR",
            },
            {"Subgraph Count",
             "2",
             "An integer describing the number of subgraphs the graph should be broken up into. " +
             "This property should normally be calculated by SandMark itself.",
             null,"I","DE",
            },
            {"DWM_CT_Encode_IndividualFixups",
             "false",
             "'true' or 'false' depending on whether fixup-methods are generated or not. " +
             "'true' means that the code that binds two subgraphs together is  " +
             "residing in a separate procedure, otherwise it is inlined." +
             "This property will likely go away in a future version of SandMark.",
             null,"B","N",
            },
				{"Inline Code",
				 "false",
				 "Either 'true' or 'false'. 'true' means the methods for creating " +
				 "watermark graph are inlined at the  call point" +
				 "'false' means they are not inlined",
				 null,"B","DE",
				},
				{"Replace Watermark Class",
				 "false",
				 "Either 'true' or 'false'. 'true' means sandmark automatically " +
				 "chooses a class that would best represent a watermark graph node" +
				 "'false' means it uses the default class Watermark",
				 null,"B","DE",
				},

            {"Dump Intermediate Code",
             "false",
             "Print out the intermediate code.",
             null,"B","DE",
            },
            {"DWM_CT_Trace_PreprocessedJar",
             "","",
             null,"J","N",
            },
            {"Debug","false","",null,"B","N"},
            {"Date",new java.util.Date().toString(),"",null,"S","N"},
         };
         mConfigProps = new sandmark.util.ConfigProperties(props,null);
         java.util.List choices = new java.util.ArrayList();
         choices.add("*");
         choices.addAll
            (sandmark.util.classloading.ClassFinder.getClassesWithAncestor
             (sandmark.util.classloading.IClassFinder.GRAPH_CODEC));
         choices.removeAll
            (sandmark.util.classloading.ClassFinder.getClassesWithAncestor
             (sandmark.util.classloading.IClassFinder.WRAPPER_CODEC));
         mConfigProps.setChoices("Graph Type",true,choices);

         java.util.List prot = new java.util.ArrayList();
         prot.add("if:safe:try");
         prot.add("if:try");
         prot.add("if:safe");
         prot.add("safe:try");
         prot.add("if");
         prot.add("safe");
         prot.add("try");
         mConfigProps.setChoices("Protection Method",true,prot);

         java.util.List store = new java.util.ArrayList();
         store.add("array:vector:hash");
         store.add("array:vector");
         store.add("array:hash");
         store.add("vector:hash");
         store.add("array");
         store.add("vector");
         store.add("hash");
         mConfigProps.setChoices("Storage Method",true,store);

         java.util.List loc = new java.util.ArrayList();
         loc.add("formal");
         loc.add("global");
         mConfigProps.setChoices("Storage Location",true,loc);
      }
      return mConfigProps;
   }

   /*
    *  Get the HTML codes of the About page for CT
    */
   public java.lang.String getAlgHTML(){
      return
         "<HTML><BODY>\n" +
         "The Collberg-Thomborson software watermarking algorithm is\n" +
         "a dynamic algorithm that embeds the watermark into the\n" +
         "toplogy of a graph structure that is built at runtime\n" +
         "in response to a sequence of special user actions.\n" +
         "<table>\n" +
         "<TR><TD>\n" +
         "   Authors: <a href=\"mailto:collberg@cs.arizona.edu\">Christian Collberg</a><BR>\n" +
         "            <a href=\"mailto:gmt@cs.arizona.edu\">Gregg Townsend</a><BR>\n" +
         "            <a href=\"mailto:jas@cs.auckland.ac.nz\">Jasvir Nagra</a>\n" +
         "</TR></TD>\n" +
         "</table>\n" +
         "</BODY></HTML>\n";
   }

   /*
    *  Get the URL of the Help page for CT
    */
   public java.lang.String getAlgURL(){
      return "sandmark/watermark/ct/doc/help.html";
   }


   /***********************************************************************/
   /*                                Tracing                              */
   /***********************************************************************/
   /*
    * Before running the trace we process the input jar file
    * in order to get accurate stack frame information
    * during the actual trace. Return a new properties
    * set where the class path includes this new
    * jar file.
    */
   private static void preprocess(sandmark.watermark.DynamicTraceParameters params,
                                  sandmark.util.ConfigProperties props) throws java.io.IOException {
      sandmark.util.Log.message(0,"Preprocessing input Jar file.");
      sandmark.watermark.ct.trace.Preprocessor pre =
         new sandmark.watermark.ct.trace.Preprocessor(params.app,props);
      pre.preprocess();
      pre.save(params.appFile);
   }

   sandmark.watermark.ct.trace.Tracer tracer = null;

   /**
    * Start a tracing run of the program. Return an iterator
    * object that will generate the trace points encountered
    * by the program.
    */
   public void startTracing(sandmark.watermark.DynamicTraceParameters params) 
      throws sandmark.util.exec.TracingException {

      mTraceParams = params;
      try { preprocess(params,getConfigProperties()); }
      catch(java.io.IOException e) { 
         throw new sandmark.util.exec.TracingException(e.toString());
      }
      tracer = new sandmark.watermark.ct.trace.Tracer
         (params.programCmdLine,getConfigProperties());
      tracer.run();
   }

   /**
    * This routine is called when the tracing run has
    * completed. tracePoints is an array of trace points
    * generated by sandmark.watermark.ct.trace.Tracer.
    * In our case, these are of type
    *     sandmark.watermark.ct.trace.TracePoint
    */
   public void endTracing()
      throws sandmark.util.exec.TracingException {

      sandmark.watermark.ct.trace.TracePoint annotationPoints[] =
         (sandmark.watermark.ct.trace.TracePoint[])
         tracer.getTracePoints().toArray(new sandmark.watermark.ct.trace.TracePoint[0]);

      try {
         sandmark.watermark.ct.trace.TracePoint.write(mTraceParams.traceFile, annotationPoints);
         sandmark.util.Log.message(0,"Trace points written to file: '" + mTraceParams.traceFile + "'.");
      } catch (Exception e) {
         sandmark.util.Log.message(0,"Failed to write the trace file: '" + mTraceParams.traceFile + "'.", e);
      }

      String traceLogFile = "TracePoints.txt";
      try {
         String traceLog = sandmark.watermark.ct.trace.TracePoint.toString(annotationPoints);
         

         sandmark.util.Misc.writeToFile(traceLogFile, traceLog);
         sandmark.util.Log.message(0,"A trace point log has been written to file: '" + traceLogFile + "'.");
      } catch (Exception e) {
         sandmark.util.Log.message(0,"Failed to write the trace log file: '" + traceLogFile + "'.", e);
      }
   }

   /**
    * Force an end to a tracing run of the program.
    */
   public void stopTracing() throws sandmark.util.exec.TracingException {
      tracer.STOP();
   }

   /***********************************************************************/
   /*                              Embedding                              */
   /***********************************************************************/
   /**
    * Embed a watermark value into the program. The props argument
    * holds at least the following properties:
    *  <UL>
    *     <LI> Watermark: The watermark value to be embedded.
    *     <LI> Trace File: The name of the file containing trace data.
    *     <LI> Input File: The name of the file to be watermarked.
    *     <LI> Output File: The name of the jar file to be constructed.
    *     <LI> DWM_ct_Encode_ClassName: The name of the Java file that builds the watermark.
    *  </UL>
    */
   public void embed(sandmark.watermark.DynamicEmbedParameters params) {
      sandmark.util.ConfigProperties props = getConfigProperties();

      String watermark         = params.watermark;
      java.io.File traceFile   = params.traceFile;
      String sourceFileName    = props.getProperty("DWM_CT_Encode_ClassName") + ".java";
      String dotFileName       = props.getProperty("DWM_CT_Encode_ClassName") + ".dot";
      String callGraphBaseName = "TraceForest";

      sandmark.watermark.ct.trace.TracePoint annotationPoints[] = null;
      try {
         annotationPoints = sandmark.watermark.ct.trace.TracePoint.read(traceFile);
      } catch (Exception e) {
         sandmark.util.Log.message(0, "Could not open trace-file '" + traceFile + "'", e);
         return;
      }
      if (annotationPoints.length==0) {
         sandmark.util.Log.message(0,"Please re-run the trace to generate at least one trace point.");
         return;
      }

      sandmark.util.Log.message(0,
                                "Embedding watermark '" + watermark + "'" +
                                " using trace data from '" + traceFile  + "'.");

      try{
         // props.setProperty("Storage Location", "formal");
         sandmark.watermark.ct.embed.Embedder embedder =
            new sandmark.watermark.ct.embed.Embedder(params.app,params,props,annotationPoints);
         embedder.saveByteCode();
         //embedder.saveSource(sourceFileName);
         embedder.addToGraphViewer();
         embedder.saveGraph(dotFileName);
         embedder.saveCallForest(callGraphBaseName);
         sandmark.util.Log.message(0,"Done embedding the watermark!");
         sandmark.util.Log.message(0, "Watermark class source saved to '" + sourceFileName + "'.");
         sandmark.util.Log.message(0, "Watermark graph saved to '" + dotFileName + "'");
         sandmark.util.Log.message(0, "Tracing call-forest graphs saved to '" + callGraphBaseName + "*.dot'.");
         sandmark.util.Log.message(0, "The command 'dot -Tps file.dot>file.ps' converts a dot-file to postscript.");
      } catch (Exception e) {
         sandmark.util.Log.message(0, "Embedding failed", e);
         e.printStackTrace();
      }
   }


   /***********************************************************************/
   /*                            Recognition                              */
   /***********************************************************************/

   sandmark.watermark.ct.recognize.Recognizer recognizer = null;

   /**
    * Start a recognition run of the program.
    */
   public void startRecognition (sandmark.watermark.DynamicRecognizeParameters params) 
      throws sandmark.util.exec.TracingException {
      recognizer = new sandmark.watermark.ct.recognize.Recognizer(params,getConfigProperties());
      recognizer.run();
   }

   /**
    * Return an iterator object that will generate
    * the watermarks found in the program.
    */
   public java.util.Iterator watermarks() {
      return recognizer.watermarks();
   }

   /**
    * Force the end to a tracing run of the program.
    */
   public void stopRecognition() throws sandmark.util.exec.TracingException {
      recognizer.STOP();
   }

   public void waitForProgramExit() {
      if(recognizer == null &&
         tracer == null)
         return;
      sandmark.util.exec.Overseer os = (recognizer == null) ? 
         (sandmark.util.exec.Overseer)tracer : 
         (sandmark.util.exec.Overseer)recognizer;

      synchronized(os) {
         while(!os.exited())
            try { os.wait(); }
            catch(InterruptedException e) {}
      }
   }

} // class DynamicWatermarker

