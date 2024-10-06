package sandmark.watermark.gtw;

public class GTW extends sandmark.watermark.StaticWatermarker {
   public static boolean DEBUG = false;
   public String getShortName() {
      return "Graph Theoretic Watermark";
   }
   public String getLongName() {
      return "Venkatesan's Graph Theoretic Watermarking Algorithm";
   }
   private sandmark.util.ConfigProperties mConfigProps;
   public sandmark.util.ConfigProperties getConfigProperties() {
      if(mConfigProps == null) {
	      String props[][] = new String[][] {
            /*
              {"GTW_GRAPHCODEC","PermutationGraph",
              "What graph codec to use to go from " +
              "watermark number to CFG and back",
              "true","S",},
              {"GTW_CFS","ControlFlowSynthesizer",
              "What CFS to use","true","S",},
              {"GTW_BBM","ParityBlockMarker",
              "What block marker to use","true","S",},
              {"GTW_VALUESPLITTER","Scientific Notation",
              "What value splitter to use","true","S",},
            */
            {"Key File","gtw.key",
             "File in which to store the encryption key",null,"F","SE,SR"},
            {"Use CRT Splitter", "true", "Use the Chinese Remainder Theorem Splitter Algorithm", 
             null, "B", "SE,SR"},

            {"Debug","false","Output Debugging Info",null,"B",},
            {"Dump Dot Graphs","false","Dump DOT graphs?",null,"B",},
	      };	      
         mConfigProps = new sandmark.util.ConfigProperties(props,null);
      }
      return mConfigProps;
   }

   public String getAlgHTML() {
      return "<HTML><BODY>Graph Theoretic Watermark is an implementation of Venkatesan's Software Watermarking Algorithm " +
         "described in <a href=\"http://www.cc.gatech.edu/fac/Vijay.Vazirani/water.ps\"> " +
         "A Graph Theoretic Approach to Software Watermarking </a>. "+
         " The watermark is embedded in the control flow within the" +
         " program." +
         "<TABLE>" +
         "<TR><TD>" +
         "Authors: <a href=\"mailto:ecarter@cs.arizona.edu\">Ed Carter</a>,"+ 
         " <a href=\"mailto:ash@cs.arizona.edu\">Andrew Huntwork</a>" +
         " and <a href=\"mailto:gmt@cs.arizona.edu\">Gregg Townsend</a>" +
         "</TR></TD>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public String getAlgURL() {
      return "sandmark/watermark/gtw/doc/help.html";
   }

   public String getDescription() {
      return "Venkatesan's Graph Theoretic Watermarking Algorithm embeds the " +
         "watermark in control flow graph within the program.";
   }
   public String[] getReferences() {
      return new String[] {};
   }
   public sandmark.config.ModificationProperty[] getMutations() {
      return null;
   }
   public String getAuthor() {
      return "Ed Carter, Andrew Huntwork, and Gregg Townsend";
   }
   public String getAuthorEmail() {
      return "{ecarter,ash,gmt}@cs.arizona.edu";
   }

   public void embed(sandmark.watermark.StaticEmbedParameters params)
      throws sandmark.watermark.WatermarkingException {

      boolean useCRT = getConfigProperties().getProperty("Use CRT Splitter").equals("true");

      boolean dumpDots = getConfigProperties().getProperty("Dump Dot Graphs").equals
         ("true");
      {
         if(dumpDots) {
            System.out.println("dumping cfgs");
            int i = 0;
            for(java.util.Iterator classIt = params.app.classes() ; classIt.hasNext() ; ) {
               sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();

               for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; i++) {
                  sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
                  if(method.getInstructionList() == null)
                     continue;
                  sandmark.util.newgraph.Graphs.dotInFile(method.getCFG(),
                                                          "graphs/cfg." + i + "." + method.getName() + ".dot");
                  System.out.println("   cfg " + i);
               }
            }
         }
         int i = 0;
         for(java.util.Iterator classIt = params.app.classes() ; classIt.hasNext() ; ) {
            sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
		
            for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; i++) {
               sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
               if(method.getInstructionList() == null)
                  continue;
               sandmark.util.graph.graphview.GraphList.instance().add(method.getCFG(),
                                                                      "cfg." + i);
            }
         }
      }

      {
         boolean debugProp = getConfigProperties().getProperty("Debug").equals("true");
         DEBUG = debugProp;
         GTWRecognizer.DEBUG = debugProp;
         ClusterGraph.DEBUG = debugProp;
         FunctionClusterGraph.DEBUG = debugProp;
         sandmark.analysis.controlflowgraph.NullNENullCallGenerator.DEBUG = debugProp;
         sandmark.analysis.controlflowgraph.CallingCallGenerator.DEBUG = debugProp;
      }

      java.math.BigInteger key = new java.math.BigInteger
         (params.key == null || 
          params.key.equals("") ? "0" : params.key);
      java.util.Hashtable methodMarkValue = 
         new java.util.Hashtable();
      java.util.ArrayList programMethodCFGs = 
         new java.util.ArrayList();
      java.util.ArrayList wmMethodCFGs =
         new java.util.ArrayList();
      java.util.Random rnd = sandmark.util.Random.getRandom();
      rnd.setSeed(key.longValue());
      for(java.util.Iterator classIt = params.app.classes() ; classIt.hasNext() ; ) {
         sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
            
         for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
            sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
            if(method.getInstructionList() == null || 
               method.getInstructionList().getLength() == 0)
               continue;
            programMethodCFGs.add(method.getCFG());

            if (!useCRT){
               methodMarkValue.put(method,new Integer(0));
            }
         }
      }
      java.math.BigInteger wmark = 
         new java.math.BigInteger(params.watermark);
      if(DEBUG)
         System.out.println("wmark: " + wmark);
      int splitParts = 5 + (((rnd.nextInt() % 10) + 10) % 10);
      
      java.math.BigInteger splitWM[] = null;


      if (useCRT){
         try {
            javax.crypto.KeyGenerator kg = 
               javax.crypto.KeyGenerator.getInstance
               (sandmark.util.splitint.CRTSplitter.getAlgorithm());
            javax.crypto.SecretKey w = kg.generateKey();
            java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream
               (new java.io.FileOutputStream(getConfigProperties().getProperty("Key File")));
            oo.writeObject(w);
            oo.close();
            splitWM = 
               new sandmark.util.splitint.SlowCRTSplitter(128,50,w).split
               (wmark);
         } catch(java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("crt splitter uses a bad alg");
         } catch(java.io.IOException e) {
            throw new RuntimeException();
         }
      }else{
         splitWM = (new sandmark.util.splitint.PartialSumSplitter()).split
            (wmark, splitParts);
      }


      if(DEBUG) {
         System.out.print("wm parts: ");
         for(int i = 0 ; i < splitWM.length ; i++)
            System.out.print(splitWM[i] + " ");
         System.out.println();
      }

      sandmark.program.Class wmClazz = 
         new sandmark.program.LocalClass
         (params.app,"watermark", "java.lang.Object", "watermark", 
          org.apache.bcel.Constants.ACC_PUBLIC
          | org.apache.bcel.Constants.ACC_SUPER,
          null);
      wmClazz.addEmptyConstructor(org.apache.bcel.Constants.ACC_PUBLIC);
      {
         sandmark.analysis.controlflowgraph.ControlFlowSynthesizer cfs =
            new sandmark.analysis.controlflowgraph.PositiveIntSynthesizer();
         sandmark.util.newgraph.codec.GraphCodec codec =
            new sandmark.util.newgraph.codec.ReduciblePermutationGraph();
         for(int i = 0 ; i < splitWM.length ; i++) {
            sandmark.util.newgraph.Graph g = codec.encode(splitWM[i]);
            if(dumpDots) {
               String filename = "graphs/spg." + splitWM[i] + ".dot";
               sandmark.util.newgraph.Graphs.dotInFile(g, filename);
            }
            sandmark.util.graph.graphview.GraphList.instance().add(g, "spg." + splitWM[i]);
            sandmark.program.Method wmMethod =
               cfs.generate(g, wmClazz);
            wmMethod.setName("m" + i);
            if(DEBUG)
               System.out.println(wmMethod.getName());
            if(dumpDots) {
               String filename = "graphs/cfg.spg." + splitWM[i] + "." + wmMethod.getName() + ".dot";
               sandmark.util.newgraph.Graphs.dotInFile(wmMethod.getCFG(),
                                                       filename);
            }
            sandmark.util.graph.graphview.GraphList.instance().add(wmMethod.getCFG(),
                                                                   "cfg.spg." + splitWM[i]);
            wmMethodCFGs.add(wmMethod.getCFG());

            if (!useCRT){
               methodMarkValue.put(wmMethod,new Integer(1));
            }
         }
      }
      {
         if(DEBUG)
            System.out.println("building pcfg");
         java.util.ArrayList allCFGs = new java.util.ArrayList(programMethodCFGs);
         allCFGs.addAll(wmMethodCFGs);
         sandmark.analysis.controlflowgraph.ProgramCFG pCFG =
            new sandmark.analysis.controlflowgraph.ProgramCFG(allCFGs);
         if(dumpDots) {
            if(DEBUG)
               System.out.println("dotting");
            sandmark.util.newgraph.Graphs.dotInFile(pCFG,
                                                    "graphs/pcfg.dot");
         }
         sandmark.util.graph.graphview.GraphList.instance().add(pCFG, "pcfg");
         if(DEBUG)
            System.out.println("done building pcfg");
         ClusterGraph cg = 
            new FunctionClusterGraph(pCFG);
         if(dumpDots)
            sandmark.util.newgraph.Graphs.dotInFile(cg,
                                                    "graphs/fcg.orig.dot");
         sandmark.util.graph.graphview.GraphList.instance().add(cg, "fcg.orig");
         if(DEBUG)
            System.out.println("done building cg");
         double randomEdgeCount = 
            getNumberOfEdgesToAdd(cg,programMethodCFGs,wmMethodCFGs,rnd);
         if(randomEdgeCount < 1.0)
            randomEdgeCount = 1.0;
         if(DEBUG)
            System.out.println("adding " + randomEdgeCount + " edges");
         cg.randomlyWalkAddingEdges(programMethodCFGs, wmMethodCFGs,
                                    (new Double(randomEdgeCount)).intValue());
         if(dumpDots)
            sandmark.util.newgraph.Graphs.dotInFile(cg,
                                                    "graphs/fcg.munged.dot");
         sandmark.util.graph.graphview.GraphList.instance().add(cg, "fcg.munged");
      }
      for(java.util.Iterator it = programMethodCFGs.iterator() ; 
          it.hasNext() ; ) {
         sandmark.analysis.controlflowgraph.MethodCFG cfg = 
            (sandmark.analysis.controlflowgraph.MethodCFG)it.next();
         cfg.rewriteInstructionList();
         cfg.method().removeNOPs();
         cfg.method().setMaxStack();
         cfg.method().mark();
         cfg.method().removeLocalVariables();
         cfg.method().removeLineNumbers();
      }
      for(java.util.Iterator it = wmMethodCFGs.iterator() ; 
          it.hasNext() ; ) {
         sandmark.analysis.controlflowgraph.MethodCFG cfg = 
            (sandmark.analysis.controlflowgraph.MethodCFG)it.next();
         cfg.rewriteInstructionList();
         cfg.method().removeNOPs();
         cfg.method().setMaxStack();
         cfg.method().mark();
         cfg.method().removeLocalVariables();
         cfg.method().removeLineNumbers();
      }
      programMethodCFGs = wmMethodCFGs = null;


      if (!useCRT){
         sandmark.watermark.util.BasicBlockMarker bm =
            new sandmark.watermark.util.MD5Marker(wmClazz,2,key);
         sandmark.watermark.util.MethodMarker mm =
            new sandmark.watermark.util.EveryBlockMarker(bm);
         for(java.util.Iterator methodIt = methodMarkValue.keySet().iterator() ;
             methodIt.hasNext() ; ) {
            sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
            
            if(method.getInstructionList() == null) {
               if(DEBUG)
                  System.out.println("not embedding in abstract method " +
                                     method.getName());
               continue;
            }
            
            int value = ((Integer)methodMarkValue.get(method)).intValue();
            if(DEBUG)
               System.out.println("value for " + method.getName() + " is " + value);
            mm.embed(method, value);
         }
      }

      if(dumpDots) {
         int i = 0;
         for(java.util.Iterator classes = params.app.classes() ; classes.hasNext() ; ) {
            sandmark.program.Class clazz = (sandmark.program.Class)classes.next();
            for(java.util.Iterator methods = clazz.methods() ; methods.hasNext() ; i++) {
               sandmark.program.Method m = (sandmark.program.Method)methods.next();
               sandmark.util.newgraph.Graphs.dotInFile
                  (m.getCFG(),"graphs/cfg.final." + i + "." + m.getName() + ".dot");
            }
         }
      }
   }

       
   public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params) {
      {
         boolean debugProp = getConfigProperties().getProperty("Debug").equals("true");
         DEBUG = debugProp;
         GTWRecognizer.DEBUG = debugProp;
         ClusterGraph.DEBUG = debugProp;
         FunctionClusterGraph.DEBUG = debugProp;
         sandmark.analysis.controlflowgraph.NullNENullCallGenerator.DEBUG = debugProp;
         sandmark.analysis.controlflowgraph.CallingCallGenerator.DEBUG = debugProp;
      }
      try {
         sandmark.program.Application app = params.app;

         java.math.BigInteger key = new java.math.BigInteger
            (params.key == null || 
             params.key.equals("") ? "0" : params.key);
         GTWRecognizer gr = new GTWRecognizer
            (app,
             getConfigProperties(),   //.getProperty("Dump Dot Graphs").equals("true"),
             key);
         return gr;
      } catch(Exception e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   public static void main(String argv[]) throws Exception {
      sandmark.program.Application app = null;
      app = new sandmark.program.Application(argv[0]);
      int wmark = argv.length >=3 ? 
         Integer.decode(argv[2]).intValue() : 17;
      GTW me = new GTW();
      sandmark.watermark.StaticWatermarker.getProperties().setProperty
         ("Watermark",wmark + "");
      me.embed(sandmark.watermark.StaticWatermarker.getEmbedParams(app));
      java.util.Iterator it = 
         me.recognize
         (sandmark.watermark.StaticWatermarker.getRecognizeParams(app));
      while(it.hasNext()) {
         String wmarkStr =  (String)it.next();
         System.out.println("possible watermark: " + wmarkStr);
      }
   }

   private double getNumberOfEdgesToAdd
      (ClusterGraph cg,java.util.ArrayList programCFGs,
       java.util.ArrayList wmCFGs,java.util.Random rnd) {
      double p = programCFGs.size();
      double w = wmCFGs.size();
      double e = cg.edgeCount();
      double qdenom = p + w - 1.0;
      double qp = (p - 1.0) / qdenom;
      double qw = (w - 1.0) / qdenom;
      double m = 4.0 * e * w * (1 - qw) * (1 - qp) /
         (p * (2 - qw) * (1 - qp) - w * (2 - qp) * (1 - qw));
      double randomFactor = w * rnd.nextGaussian() / 4;
      return m + randomFactor;
   }
}

