package sandmark.smash;

public class SandmarkCLI {

   private static boolean DEBUG = false;
   private SandmarkCLI() {}
   
   private static String optSpec[] = new String[] {
      "-U","usage",
      "-H algname","algorithm description",
      "-E","embed a watermark",
      "-R","recognize a watermark",
      "-O","obfuscate",
      "-S","list static watermarkers",
      "-D","list dynamic watermarkers",
      "-F","list obfuscators",
      "-T","trace the execution of an application",
      "-A algname","the algorithm name",
      "-i input","the input file",
      "-o output","the output file",
      "-w watermark","the watermark",
      "-k key","the key",
      "-t tracefile","the trace file",
      "-c classpath","class path additions",
      "-m mainclass","the main class",
      "-a arguments","arguments to the program",
      "-d property_values","extra algorithm parameters",
   };


  public static void main(String[] args) {
     sandmark.util.Options opts = 
        new sandmark.util.Options(optSpec,"SandmarkCLI",args);
     
     String options = opts.getWhich();
     String actions = "[UHEROSDFT]";
     java.util.regex.Pattern actionPat = 
        java.util.regex.Pattern.compile(actions);
     java.util.regex.Matcher matcher = actionPat.matcher(options);
     if(!matcher.find()) {
        opts.usage(System.out,"SandmarkCLI");
        System.exit(1);
     }
     int action = matcher.group().charAt(0);
     
     try {
        switch(action) {
        case 'O':
           doObfuscate(opts);
           break;
        case 'E':
           doEmbed(opts);
           break;
        case 'R':
           doRecognize(opts);
           break;
        case 'T':
           doTrace(opts);
           break;
        case 'H':
           showAlgHelp(opts.getValue('H'));
           break;
        case 'S':
           showSWMs();
           break;
        case 'D':
           showDWMs();
           break;
        case 'F':
           showObfs();
           break;
        case 'U':
           opts.usage(System.out,"SandmarkCLI");
           break;
        default:
           opts.usage(System.out,"SandmarkCLI");
        System.exit(1);
        }
     } catch(Throwable t) {
        System.err.println("ERROR: " + t);
        t.printStackTrace();
     }
  }
  
  private static void doEmbed(sandmark.util.Options opts) throws Exception {
     sandmark.Algorithm alg = getAlg(opts);
     if(alg == null || 
        !(alg instanceof sandmark.watermark.GeneralWatermarker)) {
        System.out.println("Please specify a Watermarker");
        System.exit(1);
     }
     setExtraProperties(alg,opts);
     
     sandmark.program.Application app;
     java.io.File outputFile;
     try {
        String inputFileName = opts.getValue('i');
        app = new sandmark.program.Application(inputFileName);
        String outputFileName = opts.getValue('o');
        outputFile = new java.io.File(outputFileName);
     } catch(Exception e) {
        System.out.println("Please specify an input and output file.");
        System.exit(1);
        return;
     }
     
     String wm = opts.getValue('w');
     if(wm == null) {
        System.out.println("Please specify a watermark.");
        System.exit(1);
     }
     
     if(alg instanceof sandmark.watermark.DynamicWatermarker)
        doDynamicEmbed((sandmark.watermark.DynamicWatermarker)alg,
                       app,outputFile,wm,opts);
     else if(alg instanceof sandmark.watermark.StaticWatermarker)
        doStaticEmbed((sandmark.watermark.StaticWatermarker)alg,
                      app,outputFile,wm,opts);
     else
        assert false : alg.getClass();
  }
  
  private static void doDynamicEmbed
     (sandmark.watermark.DynamicWatermarker alg,sandmark.program.Application app,
      java.io.File output,String wm,sandmark.util.Options opts) {
     sandmark.util.ConfigProperties cp = 
        sandmark.watermark.DynamicWatermarker.getProperties();
     
     cp.setValue("Watermark",wm);
     
     java.io.File traceFile;
     try {
        String traceFileName = opts.getValue('t');
        traceFile = new java.io.File(traceFileName);
        if(!traceFile.exists())
           throw new java.io.FileNotFoundException();
     } catch(java.io.IOException e) {
        System.out.println("Please specify a trace file");
        System.exit(1);
        return;
     }
     cp.setValue("Trace File",traceFile);
     
     sandmark.watermark.DynamicEmbedParameters params =
        sandmark.watermark.DynamicWatermarker.getEmbedParams(app);
     
     alg.embed(params);
     try {
        app.save(output);
     } catch(java.io.IOException e) {
        System.out.println("Please specify a valid output file.");
        System.exit(1);
     }
  }
  
  private static void doStaticEmbed
     (sandmark.watermark.StaticWatermarker alg,sandmark.program.Application app,
      java.io.File output,String wm,sandmark.util.Options opts) 
     throws sandmark.watermark.WatermarkingException {
     sandmark.util.ConfigProperties cp =
        sandmark.watermark.StaticWatermarker.getProperties();
     
     cp.setValue("Watermark",wm);
     
     String key = opts.getValue('k');
     cp.setValue("Key",key);
     
     sandmark.watermark.StaticEmbedParameters params = 
        sandmark.watermark.StaticWatermarker.getEmbedParams(app);
     
     alg.embed(params);
     
     try {
        app.save(output);
     } catch(Exception e) {
        System.out.println("Please specify a valid output file.");
        System.exit(1);
     }
  }
  
  private static void doObfuscate(sandmark.util.Options opts) throws Exception {
     sandmark.Algorithm alg = getAlg(opts);
     if(alg == null ||
        !(alg instanceof sandmark.obfuscate.GeneralObfuscator)) {
        System.out.println("Please specify an Obfuscation");
        System.exit(1);
     }
     setExtraProperties(alg,opts);
     
     sandmark.program.Application app;
     java.io.File outputFile;
     try {
        String inputFileName = opts.getValue('i');
        app = new sandmark.program.Application(inputFileName);
        String outputFileName = opts.getValue('o');
        outputFile = new java.io.File(outputFileName);
     } catch(Exception e) {
        System.out.println("Please specify an input and output file");
        System.exit(1);
        return;
     }
     
     sandmark.obfuscate.Obfuscator.runObfuscation(app,alg);
     
     try {
        app.save(outputFile);
     } catch(java.io.IOException e) {
        System.out.println("Please specify a valid output file");
        System.exit(1);
     }
  }
  
  private static void doRecognize(sandmark.util.Options opts) throws Exception {
     sandmark.Algorithm alg = getAlg(opts);
     if(alg == null || 
        !(alg instanceof sandmark.watermark.GeneralWatermarker)) {
        System.out.println("Please specify a Watermarker");
        System.exit(1);
     }
     setExtraProperties(alg,opts);
     
     sandmark.program.Application app;
     try {
        String inputFileName = opts.getValue('i');
        app = new sandmark.program.Application(inputFileName);
     } catch(Exception e) {
        System.out.println("Please specify an input file.");
        System.exit(1);
        return;
     }
     
     if(alg instanceof sandmark.watermark.DynamicWatermarker)
        doDynamicRecognize((sandmark.watermark.DynamicWatermarker)alg,
                           app,opts);
     else if(alg instanceof sandmark.watermark.StaticWatermarker)
        doStaticRecognize((sandmark.watermark.StaticWatermarker)alg,
                          app,opts);
     else
        assert false : alg.getClass();
  }
  
  private static void doDynamicRecognize
     (sandmark.watermark.DynamicWatermarker alg,
      sandmark.program.Application app,sandmark.util.Options opts) throws Exception {
     sandmark.util.ConfigProperties cp = 
        sandmark.watermark.DynamicWatermarker.getProperties();
     
     String mainClass = opts.getValue('m');
     if(mainClass == null || mainClass.equals("")) {
        System.out.println("Please specify a main class");
        System.exit(1);
     }
     cp.setValue("Main Class",mainClass);
     
     String arguments = opts.getValue('a');
     cp.setValue("Arguments",arguments);
     
     String classPath = opts.getValue('c');
     cp.setValue("Class Path",classPath);
     
     sandmark.watermark.DynamicRecognizeParameters params =
        sandmark.watermark.DynamicWatermarker.getRecognizeParams(app);
     
     alg.startRecognition(params);
     alg.waitForProgramExit();
     for(java.util.Iterator it = alg.watermarks() ; it.hasNext() ; )
        System.out.println(it.next());
     alg.stopRecognition();
  }
  
  private static void doStaticRecognize
     (sandmark.watermark.StaticWatermarker alg,
      sandmark.program.Application app,sandmark.util.Options opts) 
     throws Exception {
     sandmark.util.ConfigProperties cp =
        sandmark.watermark.StaticWatermarker.getProperties();
     
     String key = opts.getValue('k');
     cp.setValue("Key",key);
     
     sandmark.watermark.StaticRecognizeParameters params =
        sandmark.watermark.StaticWatermarker.getRecognizeParams(app);
     
     for(java.util.Iterator it = alg.recognize(params) ; it.hasNext() ; )
        System.out.println(it.next());
  }
  
  private static void doTrace(sandmark.util.Options opts) throws Exception {
     sandmark.Algorithm alg = getAlg(opts);
     if(alg == null || 
        !(alg instanceof sandmark.watermark.DynamicWatermarker)) {
        System.out.println("Please specify a Dynamic Watermarker");
        System.exit(1);
     }
     setExtraProperties(alg,opts);
     sandmark.watermark.DynamicWatermarker dwm =
        (sandmark.watermark.DynamicWatermarker)alg;
     
     sandmark.util.ConfigProperties cp =
        sandmark.watermark.DynamicWatermarker.getProperties();
     
     sandmark.program.Application app;
     java.io.File traceFile;
     try {
        String inputFileName = opts.getValue('i');
        app = new sandmark.program.Application(inputFileName);
        String traceFileName = opts.getValue('t');
        traceFile = new java.io.File(traceFileName);
     } catch(Exception e) {
        System.out.println("Please specify an input and  trace file.");
        System.exit(1);
        return;
     }
     cp.setValue("Trace File",traceFile);
     
     String mainClass = opts.getValue('m');
     if(mainClass == null || mainClass.equals("")) {
        System.out.println("Please specify a main class");
        System.exit(1);
     }
     cp.setValue("Main Class",mainClass);
     
     String classPath = opts.getValue('c');
     cp.setValue("Class Path",classPath);
     
     String arguments = opts.getValue('a');
     cp.setValue("Arguments",arguments);
     
     try {
        sandmark.watermark.DynamicTraceParameters params = 
           sandmark.watermark.DynamicWatermarker.getTraceParams(app);
        
        dwm.startTracing(params);
        dwm.waitForProgramExit();
        dwm.endTracing();
        dwm.stopTracing();
     } catch(ClassNotFoundException e) {
        System.out.println("Please specify a valid main class");
        System.exit(1);
     }
  }
  
  private static void setExtraProperties(sandmark.Algorithm alg,
                                         sandmark.util.Options opts) {
     String props = opts.getValue('d');
     if(props == null || props.equals(""))
        return;
     
     String propVals[] = props.split("[,]");

     for(int i = 0 ; i < propVals.length ; i++) {
        String pv = propVals[i];
        String split_pv[] = pv.split("=");
        if(split_pv.length != 2 || split_pv[0].equals(""))
           continue;
        try {
           alg.getConfigProperties().setProperty(split_pv[0],split_pv[1]);
        } catch(Exception e) {
           //getCP may return null, or the specified property may 
           //not apply to this Alg
        }
     }
  }
  
  private static sandmark.Algorithm getAlg(sandmark.util.Options opts) {
     String algname = opts.getValue('A');
     if(algname == null || algname.equals(""))
        return null;
     
     return getAlg(algname);
  }
  
  private static sandmark.Algorithm getAlg(String algname) {     
     String className = 
        sandmark.util.classloading.ClassFinder.getClassByShortname(algname);
     if(className == null)
        return null;
     try {
        return (sandmark.Algorithm)Class.forName(className).newInstance();
     } catch(Exception e) {
        return null;
     }
  }
  
  private static void showAlgHelp(String algName) {
     sandmark.Algorithm alg = getAlg(algName);
     if(alg == null) {
        System.out.println("Please specify an algorithm");
        System.exit(1);
     }
     
     System.out.println(alg.getDescription());
  }
  
  private static void showSWMs() {
     java.util.Collection swms =
        sandmark.util.classloading.ClassFinder.getClassesWithAncestor
        (sandmark.util.classloading.IClassFinder.STAT_WATERMARKER);
     for(java.util.Iterator it = swms.iterator() ; it.hasNext() ; ) {
        String className = (String)it.next();
        String shortName = 
           sandmark.util.classloading.ClassFinder.getClassShortname(className);
        System.out.println(shortName);
     }
  }
  
  private static void showDWMs() {
     java.util.Collection dwms =
        sandmark.util.classloading.ClassFinder.getClassesWithAncestor
        (sandmark.util.classloading.IClassFinder.DYN_WATERMARKER);
     for(java.util.Iterator it = dwms.iterator() ; it.hasNext() ; ) {
        String className = (String)it.next();
        String shortName = 
           sandmark.util.classloading.ClassFinder.getClassShortname(className);
        System.out.println(shortName);
     }
  }
  
  private static void showObfs() {
     System.out.println("Application obfuscations:");
     java.util.Collection app = 
        sandmark.util.classloading.ClassFinder.getClassesWithAncestor
        (sandmark.util.classloading.IClassFinder.APP_OBFUSCATOR);
     for(java.util.Iterator it = app.iterator() ; it.hasNext() ; ) {
        String className = (String)it.next();
        String shortName = 
           sandmark.util.classloading.ClassFinder.getClassShortname(className);
        System.out.println("\t" + shortName);
     }
     System.out.println("Class obfuscations:");
     java.util.Collection clazz = 
        sandmark.util.classloading.ClassFinder.getClassesWithAncestor
        (sandmark.util.classloading.IClassFinder.CLASS_OBFUSCATOR);
     for(java.util.Iterator it = clazz.iterator() ; it.hasNext() ; ) {
        String className = (String)it.next();
        String shortName = 
           sandmark.util.classloading.ClassFinder.getClassShortname(className);
        System.out.println("\t" + shortName);
     }
     System.out.println("Method obfuscations:");
     java.util.Collection method = 
        sandmark.util.classloading.ClassFinder.getClassesWithAncestor
        (sandmark.util.classloading.IClassFinder.METHOD_OBFUSCATOR);
     for(java.util.Iterator it = method.iterator() ; it.hasNext() ; ) {
        String className = (String)it.next();
        String shortName = 
           sandmark.util.classloading.ClassFinder.getClassShortname(className);
        System.out.println("\t" + shortName);
     }
  }
}