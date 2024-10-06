package sandmark.watermark.objectwm;

/** Implementing Stern's Algorithm for Robust Object Watermarking **/

public class ObjectWatermark extends sandmark.watermark.StaticWatermarker 
{
    private boolean DEBUG = false;

    private Config config = null;
    private ObjectHelper helper = null;
    private VectorExtraction vecExtract = null;

    static sandmark.program.Application myApp;

    public static String inputJarFile;
    public static String outputJarFile;

    private static java.math.BigInteger wmBigInteger;
    private static String wmString;
    private String myWatermark;
    private String origJarFile = null;


    /*  Constructs a watermarker */
    public ObjectWatermark()
    {
        config = new Config();
        helper = new ObjectHelper();
        vecExtract = new VectorExtraction();
    }

    /*  Returns this watermarker's short name.  */
    public String getShortName() {
        return "Stern";
    }

    /* Returns this watermarker's long name.  */
    public String getLongName() {
       return " Robust Object Watermarking ";
    }

    public sandmark.config.ModificationProperty[] getMutations() {
        return null;
    }

    /* Returns this watermark's configuration properties. */
    private sandmark.util.ConfigProperties mConfigProps;
    public sandmark.util.ConfigProperties getConfigProperties() {
        if(mConfigProps == null) {
	      String props[][] = new String[][] {
		  {"Original File","","The original jar file",null,"J","SR",},
	      };	      
	      mConfigProps = new sandmark.util.ConfigProperties(props,null);
        }
	return mConfigProps;
    }

    /*  Get the HTML codes of the About page for ConstantString */
    public java.lang.String getAlgHTML(){
       return 
          "<HTML><BODY>\n" +
          "Robust Object Watermarking is a watermarking algorithm that " +
          "embeds a static watermark spread throughout the body of the " +
          "code as the frequency of occurrence of identified groups of " +
          "instructions. " +
          "<table>\n" +
          "<TR><TD>\n" +
          " Author: <a href=\"mailto:tapas@cs.arizona.edu\">Tapas R. Sahoo</a> and <a href=\"mailto:balamc@cs.arizona.edu\">Balamurgan Chirstabesan</a>\n" +
          "</TR></TD>\n" +
          "</table>\n" +
          "</BODY></HTML>\n"; 
    }

    /*  Get the URL of the Help page for ConstantString */
    public java.lang.String getAlgURL(){
        return "sandmark/watermark/objectwm/doc/help.html";
    }

    /* Specifies the author of this algorithm.*/
    public java.lang.String getAuthor()
    {
        return "Balamurugan Chirtsabesan and Tapas Sahoo";
    }

    /* Specifies the author's email address.  */
    public java.lang.String getAuthorEmail()
    {
        return "balamc@cs.arizona.edu and tapas@cs.arizona.edu";
    }

    /* Specifies what this algorithm does. */
    public java.lang.String getDescription()
    {
        return "This algorithm (by Stern et. al.) embeds a static watermark " +
               "spread throughout the body of the " +
               "code as the frequency of occurrence of identified groups of " +
               "instructions. See Help for restrictions on input.";
    }

    public sandmark.config.RequisiteProperty[] getPostprohibited()
    {
        return null;
    }

    public sandmark.config.RequisiteProperty[] getPostrequisities()
    {
        return null;
    }

    public sandmark.config.RequisiteProperty[] getPostsuggestions()
    {
        return null;
    }

    public sandmark.config.RequisiteProperty[] getPreprohibited()
    {
        return null;
    }

    public sandmark.config.RequisiteProperty[] getPrerequisities()
    {
        return null;
    }

    public sandmark.config.RequisiteProperty[] getPresuggestions()
    {
        return null;
    }

    public java.lang.String[] getReferences()
    {
        return null;
    }


    /* Embed a watermark value into the program. The props argument
     * holds at least the following properties:
     *  <UL>
     *     <LI> Input File: The name of the file to be watermarked.
     *     <LI> Output File: The name of the jar file to be constructed.
     *  </UL>
     */
    public void embed(sandmark.watermark.StaticEmbedParameters params)
        throws sandmark.watermark.WatermarkingException
    {
        String watermark = params.watermark;
        config.setWatermarkValue(watermark);
        myApp = params.app;
     
        java.util.Iterator codeClasses = null;
        codeClasses = params.app.classes();
  
        java.util.Vector tempVector = new java.util.Vector(10,2);
        Integer tempVal;
     
        CodeBook codeBook = new CodeBook();
        int vectorLength = codeBook.numVectorGroups;
     
        if( config.origVector.size() > 0 )
            config.origVector.removeAllElements();
        for(int v=0; v< vectorLength; v++) {
            Integer elem = new Integer(0);
            config.origVector.addElement(elem);
        }
  
        if(!codeClasses.hasNext())
            throw new sandmark.watermark.WatermarkingException(" " + 
                "There must be at least one class to watermark.");
     
        while(codeClasses.hasNext()){
	    
            sandmark.program.Class cObj = (sandmark.program.Class)codeClasses.next();
            tempVector = vecExtract.extractVector(cObj);
            if( tempVector == null) 
                continue;
            
            for(int v=0; v< config.origVector.size(); v++) {
                int newfreq = ((Integer)(config.origVector.elementAt(v))).intValue() +
                              ((Integer)tempVector.elementAt(v)).intValue(); 
      
                tempVal = new Integer(newfreq);
                config.origVector.setElementAt(tempVal, v); 
            }
        }
        /* <config.origVector> now contains the initial vector frequencies **/
  
        if(DEBUG) ObjectHelper.display_VectorInfo(config.origVector, "<initial>");
         
        /* Parsing input to get the wmVector */
        int wmInt = -1;
        try{
            wmInt = Integer.parseInt(config.getWatermarkValue()); 
                                 /* obtained from commmand line */
            if(wmInt < 100000000) 
                wmString = config.getWatermarkValue();
            else
                throw new java.lang.NumberFormatException(); 
        }catch(java.lang.NumberFormatException e){
            wmBigInteger =
                sandmark.util.StringInt.encode( config.getWatermarkValue() );
            wmString = wmBigInteger.toString();
        }
  
        java.util.Vector wmVector = new java.util.Vector(10,1);
  
        for(int l=0; l<wmString.length(); l++){
            int k = Integer.parseInt((wmString.substring(l,l+1)));
            wmVector.addElement(new Integer(k));
        }
  
        if(DEBUG) ObjectHelper.display_VectorInfo(wmVector, "<CHECKED_IN watermark>");
  
        if(wmString.length() < codeBook.numVectorGroups){
            int pad = codeBook.numVectorGroups - wmString.length(); 
            for(int n=0; n< pad; n++) 
                wmVector.addElement(new Integer(0));
        }
        else{
            /* trim to size -> codeBook.numVectorGroups */
            wmVector.setSize(codeBook.numVectorGroups);
        }
  
        /* After extracting into the wmVector --> this is to be embedded */
        if(DEBUG) ObjectHelper.display_VectorInfo(wmVector, "< watermark >");
        
        Insertion insObj = new Insertion(); 
        insObj.modifyCode(wmVector);
    }
 
 
 
    /*  Recognition procedure begins ...
 
        IMPORTANT NOTE: 
        # The 'result' vector contains the watermark retrieved in the first index position.
        The second index of the 'result' vector contains 1 or 0 depeding on whether the 
        watermark was found or not.
 
        # Currently, the watermarked code is passed as parameter, 
          and the watermark ( to be recognized ) is passed  in the 'Key' field 
          A strict assumption is made that if the watermarked code is of the form <A_wm.jar>,
          then the original code is of the form <A.jar>
     */
    public class Recognizer implements java.util.Iterator 
    {
        java.util.Vector result = new java.util.Vector();
        String jarInput;
        int current = 0;
        Integer tempVal = null;

        public Recognizer(sandmark.watermark.StaticRecognizeParameters params)
        {
            generate(params);
        }

        private int correlate(String oldWmark, String newWmark)
        {
            if(DEBUG) {
                System.out.println("oldWmark -> " + oldWmark);
                System.out.println("newWmark -> " + newWmark);
            }
   
            if(oldWmark.length()!=newWmark.length()) {
                //System.out.println(" Error: wmark length mismatch : check code");
                //System.exit(1);
            }
   
            double distance1=0.0, distance2=0.0;
            double wmark1[] = new double[oldWmark.length()];
            double wmark2[] = new double[oldWmark.length()];
   
            for(int k=0; k<oldWmark.length(); k++) {
                wmark1[k] = (double)Integer.parseInt(oldWmark.substring(k,k+1));
                //wmark2[k] = (double)Integer.parseInt(newWmark.substring(k,k+1));
   
                wmark2[k] = (double)Integer.parseInt(newWmark.substring(0, newWmark.indexOf('.')));
                if(k<(oldWmark.length()-1))
                    newWmark = newWmark.substring(newWmark.indexOf('.')+1);
                distance1+=(wmark1[k]*wmark1[k]);
                distance2+=(wmark2[k]*wmark2[k]);
            }
   
            distance1= java.lang.Math.sqrt(distance1);
            distance2= java.lang.Math.sqrt(distance2);
            double rel = 0.0;
            for(int k=0; k<oldWmark.length(); k++) {
                wmark1[k] = wmark1[k]/distance1;
                wmark2[k] = wmark2[k]/distance2;
                rel+= wmark1[k]*wmark2[k];
            }
            rel*=100;
            if(rel > config.getRecognitionThreshold()){
                if(DEBUG) System.out.println("correlation ratio -> " + rel);
                return 1;
            }
            else{
                if(DEBUG) System.out.println("correlation ratio -> " + rel);
                return 0;
            }
        }
  

        public void generate(sandmark.watermark.StaticRecognizeParameters params)
        {
            java.util.Iterator classes = params.app.classes();
            
            java.util.Vector incrVector = new java.util.Vector(10,2);
            CodeBook cb = new CodeBook();
            result.clear();
            /* initialize original vector */
            for(int v=0; v<cb.numVectorGroups; v++){
                Integer elem = new Integer(0);
                result.addElement(elem);
            }
   
            while(classes.hasNext()){
                sandmark.program.Class classObj = (sandmark.program.Class)classes.next();
                
                incrVector = vecExtract.extractVector(classObj);
                if( incrVector == null )
                    continue;
                for(int v=0; v<incrVector.size(); v++) {
                    int newfreq = ((Integer)result.elementAt(v)).intValue() +
                                  ((Integer)incrVector.elementAt(v)).intValue(); 
                    tempVal = new Integer(newfreq);
                    result.setElementAt(tempVal, v); 
                }
            }
   
            if(DEBUG) ObjectHelper.display_VectorInfo( result, "< WATERMARK PLUS CODE >"); 
   
            myApp = null;
            java.util.Iterator origClasses = null;
            try{
                myApp = new sandmark.program.Application(origJarFile);
            }catch(java.lang.Exception e){
                System.out.println(" Error @ Objectwatermark.java -> " + e); 
                sandmark.util.Log.message(0, " Recognition unsuccessful");
                return;
            }

            origClasses = myApp.classes();
   
            java.util.Vector origIncrVector = new java.util.Vector(10,2);
            java.util.Vector origVector = new java.util.Vector(10,2);
   
            /* initialize origVector */
            for(int v=0; v<cb.numVectorGroups; v++) {
                Integer elem = new Integer(0);
                origVector.addElement(elem);
            }
   
            VectorExtraction vE = new VectorExtraction();
            while (origClasses.hasNext()){
                sandmark.program.Class classObj = (sandmark.program.Class)origClasses.next();
   
                origIncrVector = vE.extractVector(classObj);
                if(origIncrVector == null)
                    continue;
            
                for(int v=0; v<origIncrVector.size(); v++){
                    int newfreq = ((Integer)origVector.elementAt(v)).intValue() +
                                  ((Integer)origIncrVector.elementAt(v)).intValue(); 
                    tempVal = new Integer(newfreq);
                    origVector.setElementAt(tempVal, v); 
                }
            }
            if(DEBUG) ObjectHelper.display_VectorInfo( origVector, "< ORIGINAL CODE >");
   
            /* Get 'wmString' from 'Key' */
            int wmInt = -1;
            try{
                wmInt = Integer.parseInt( myWatermark ); /* obtained from 'Key' */
                if (wmInt < 100000000)
                    wmString = myWatermark;
                else
                    throw new java.lang.NumberFormatException(); 
            }catch(java.lang.NumberFormatException e){
                wmBigInteger = sandmark.util.StringInt.encode( myWatermark );
                wmString = wmBigInteger.toString();
            }
      
            java.util.Vector wmVector = new java.util.Vector(10,1);
            for(int l=0; l<wmString.length(); l++) {
                int k = Integer.parseInt((wmString.substring(l,l+1)));
                wmVector.addElement(new Integer(k));
            }
            if(DEBUG) ObjectHelper.display_VectorInfo(wmVector, "< Key watermark >");
      
            if(wmString.length() < cb.numVectorGroups){
                int pad = cb.numVectorGroups-wmString.length(); 
                for(int n=0; n< pad; n++) 
                    wmVector.addElement(new Integer(0));
            }
            else
                /* trim to size -> codeBook.numVectorGroups */
                wmVector.setSize(cb.numVectorGroups);
   
            if(DEBUG) ObjectHelper.display_VectorInfo(wmVector, "< Key (sized) watermark >");
   
   
            /* Vector 'result' contains the code+watermark vector
             * Vector 'origVector' contains the original code vector 
             * result - origVector should give the wmString (ie. watermark vector), incase the 
             * watermark is not destroyed
             */
   
            int totaldiff = 0;
            if(incrVector != null)
                for(int v=0; v< incrVector.size(); v++){
                    int wmfreq = ((Integer)result.elementAt(v)).intValue() -
                                 ((Integer)origVector.elementAt(v)).intValue(); 
                    tempVal = new Integer(wmfreq);
                    result.setElementAt(tempVal, v); 
   
                    int wmStrVal;
                    if(v>=wmString.length()) 
                        wmStrVal = 0;
                    else 
                        wmStrVal = Integer.parseInt(wmString.substring(v,v+1));
               
                    int diff = tempVal.intValue()-wmStrVal;
                    if(diff<0)
                        diff = 0-diff; /* ensuring threshold on either side of the actual value */
                    totaldiff+=diff;
                }
   
            /* Calculate the normalized correlation function */
   
            String watermark = "";
            for(int v=0; v<cb.numVectorGroups; v++)
                watermark = watermark + (result.elementAt(v)).toString() + ".";
   
            int foundflag = this.correlate(wmString, watermark);
            if (foundflag == 1)
                sandmark.util.Log.message(0," WATERMARK FOUND! ");
            else 
                sandmark.util.Log.message(0," WATERMARK NOT FOUND! ");
   
            /* the "result" vector is returned */
            if(DEBUG) ObjectHelper.display_VectorInfo( result, "< FINAL Diff WATERMARK entity >");

            result.clear();
            if(foundflag==0)
                result.add(0," NO_WATERMARK_FOUND");
            else
                result.add(0, myWatermark);
        }
    
        public boolean hasNext() {
            return current<result.size();
        }
    
        public java.lang.Object next() {
            return result.get(current++);
        }
    
        public void remove()
        {}
    }
    
    
    /* Return an vector which contains the watermarks
     * found in the program. 
     *  <UL>
     *  <LI> Input File: The name of the file to be watermarked.
     *  <LI> Original File: The name of the original jar file before
     *       watermaking.
     *  </UL>
     */
    public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
        throws sandmark.watermark.WatermarkingException
    {
        
        java.io.File origJar = 
            (java.io.File)getConfigProperties().getValue("Original File");
        if (origJarFile == null) {
            try {
                String jarInput = params.app.getMostRecentPath().toString();
                origJarFile = jarInput.substring(0, jarInput.lastIndexOf("_") );
                origJarFile += ".jar";
            } catch (Exception e) {
                sandmark.util.Log.message(0,"The original file is unknown.");
            }
        } else
            origJarFile = origJar.toString();

        myWatermark = params.key;
        return new Recognizer(params);
    }
}

