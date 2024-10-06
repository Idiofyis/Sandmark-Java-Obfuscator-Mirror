package sandmark.watermark.execpath;

public class EPW extends sandmark.watermark.DynamicWatermarker {
    private static final int METHOD_LENGTH_LIMIT = 10000;
    private static final float LOOP_WM_RATIO = .9f;
    private static class Insertion {
	WMCodeGen codeGen;
	String bits;
	Insertion(WMCodeGen c,String b) { codeGen = c ; bits = b; }
    }
    private TraceGetter mTG;
    private sandmark.util.ConfigProperties mConfigProps;
    private java.io.File mTraceFile;

    public void startTracing(sandmark.watermark.DynamicTraceParameters params) 
	throws sandmark.util.exec.TracingException {
	try {
	    new Tracer(params.app,true);
	    params.app.save(params.appFile);

	    System.out.println("done instrumenting");
        
	    String cmdLine = "";
	    for(int i = 0 ; i < params.programCmdLine.length ; i++)
	        cmdLine += params.programCmdLine[i] + " ";

	    System.out.println(cmdLine);
	    
	    mTG = new TraceGetter(cmdLine,params.traceFile);
	    mTG.startTracing();
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new sandmark.util.exec.TracingException();
	}
    }

    public void endTracing() {
	try {
	    mTG.kill();
	} catch(Exception e) {
	    sandmark.util.Log.message(0,"Tracing failed!");
	}
	System.out.println("done tracing");
    }

    public void stopTracing() {
	try {
	    mTG.kill();
	} catch(Exception e) {}
	System.out.println("done tracing");
    }

    public void embed(sandmark.watermark.DynamicEmbedParameters params) {
	TraceIndexer index = null;
	try { index = new TraceIndexer(params.traceFile); }
	catch(java.io.IOException e) {
	    throw new RuntimeException();
	}

	System.out.println("got index");

	java.util.Hashtable threadLengths = index.getThreadLengths();
	String mainThread = null;
	{ 
	    int maxLength = 0;
	    for(java.util.Iterator it = threadLengths.keySet().iterator() ; 
		it.hasNext() ; ) {
		String threadName = (String)it.next();
		int length = ((Integer)threadLengths.get(threadName)).intValue();
		if(length > maxLength) {
		    maxLength = length;
		    mainThread = threadName;
		}
	    }
	}
	    
	if(mainThread == null)
	    throw new RuntimeException("no traced threads");

	System.out.println("got main thread");

	java.util.List tracePoints = index.getTracePoints(mainThread);
	float weights[] = new float[tracePoints.size()];
	{
	    float weightsum = 0.0f;
	    for(int i = 0 ; i < weights.length ; i++)
		weightsum += weights[i] = 
		    1.0f / index.getOffsetList
		    ((TraceIndexer.TracePoint)tracePoints.get(i)).size();
	    for(int i = 0 ; i < weights.length ; i++)
		weights[i] /= weightsum;
	    float prev = 0.0f,sum = 0.0f;
	    for(int i = 0 ; i < weights.length ; i++) {
		prev = weights[i];
		weights[i] = sum;
		sum += prev;
	    }
	}

	System.out.println("got weights");

	java.math.BigInteger parts[] = null;
	try {
	    javax.crypto.KeyGenerator kg = 
		javax.crypto.KeyGenerator.getInstance
		(sandmark.util.splitint.CRTSplitter.getAlgorithm());
	    javax.crypto.SecretKey w = kg.generateKey();
	    java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream
		(new java.io.FileOutputStream(getConfigProperties().getProperty("Key File")));
	    oo.writeObject(w);
	    parts = 
		new sandmark.util.splitint.SlowCRTSplitter(128,50,w).split
		(new java.math.BigInteger(params.watermark));
	} catch(java.security.NoSuchAlgorithmException e) {
	    throw new RuntimeException("crt splitter uses a bad alg");
	} catch(java.io.IOException e) {
	    throw new RuntimeException();
	}
	String partStrs[] = getStrs(parts);
	java.util.LinkedList list = new java.util.LinkedList();
	for(int i = 0 ; i < partStrs.length ; i++) {
	    while(true) {
		try {
		    float insertionProb = 
			sandmark.util.Random.getRandom().nextFloat();
		    int insertionPos;
		    for(insertionPos = 0 ; insertionPos < weights.length - 1 ; 
			insertionPos++)
			if(insertionProb > weights[insertionPos] &&
			   insertionProb <= weights[insertionPos + 1])
			   break;
		    System.out.println
			("inserting at " + insertionPos + " " + 
			 tracePoints.get(insertionPos));
		    NodeIterator ni = new NodeIterator
			(index.getOffsetList((TraceIndexer.TracePoint)
					     tracePoints.get(insertionPos)),
			 params.traceFile);

		    WMCodeGen cg; 
		    if(sandmark.util.Random.getRandom().nextFloat() > 
		       LOOP_WM_RATIO)
			cg = new ContextCodeGen(params.app,ni);
		    else
			cg = new LoopCodeGen(params.app,ni);

		    list.add(new Insertion(cg,partStrs[i]));
		    break;
		} catch(WMCodeGen.CodeGenException e) {
		} catch(java.io.IOException e) {}
	    }
	}
	for(java.util.Iterator it = list.iterator() ; it.hasNext() ; ) {
	    Insertion ins = (Insertion)it.next();
	    //System.out.println(ins.list);
	    if(ins.codeGen.mMethod.getInstructionList().getByteCode().length > 
	       METHOD_LENGTH_LIMIT) {
		System.out.println("skipping insertion");
		continue;
	    }
	    ins.codeGen.insert(ins.bits);
	    //System.out.println("inserted into " + ins.codeGen.mMethod + " at " +
	    //ins.codeGen.mIH);
	}

	System.out.println("embed done");
    }

    public void startRecognition(sandmark.watermark.DynamicRecognizeParameters params) 
	throws sandmark.util.exec.TracingException {
	try { 
	    mTraceFile = java.io.File.createTempFile("smk",".tra");
	    mTraceFile.deleteOnExit();
	    //System.out.println("tracing");
	    new Tracer(params.app,false);
	    //System.out.println("done");
	    params.app.save(params.appFile.toString());
	} catch(Exception e) { 
	    throw new sandmark.util.exec.TracingException(); 
	}

	System.out.println("done recog tracing");
	String cmdLine = "";
	for(int i = 0 ; i < params.programCmdLine.length ; i++)
	    cmdLine += params.programCmdLine[i]+" ";

	try {
	    mTG = new TraceGetter(cmdLine,mTraceFile);
	    mTG.startTracing();
	} catch(Exception e) {
      e.printStackTrace();
	    throw new sandmark.util.exec.TracingException();
	}
    }

    public java.util.Iterator watermarks() {
	try {
	    mTG.kill();
	} catch(Exception e) {}
	try {
	    System.out.println("about to start recog index");
	    TraceIndexer index = new TraceIndexer(mTraceFile);

	    System.out.println("done recognition index");

	    java.util.Hashtable threadLengths = index.getThreadLengths();
	    String mainThread = null;
	    { 
		int maxLength = 0;
		for(java.util.Iterator it = threadLengths.keySet().iterator() ; 
		    it.hasNext() ; ) {
		    String threadName = (String)it.next();
		    int length = ((Integer)threadLengths.get(threadName)).intValue();
		    if(length > maxLength) {
			maxLength = length;
			mainThread = threadName;
		    }
		}
	    }
	    
	    if(mainThread == null)
		throw new RuntimeException("no traced threads");

	    System.out.println("about to get bit sequence");
	    
	    String bits = Analyzer.getBitSequence
	        (index,mainThread,new RecognitionIterator(mTraceFile));

	    javax.crypto.SecretKey w = (javax.crypto.SecretKey)
		new java.io.ObjectInputStream
		(new java.io.FileInputStream
		 (getConfigProperties().getProperty("Key File"))).readObject();
	    java.util.HashSet list = new java.util.HashSet();
	    for(int i = 0 ; i < bits.length() - 64 ; i++)
		list.add(getInt(bits.substring(i,i + 64)));
	    System.out.println("combining " + list.size() + " parts");
	    java.math.BigInteger wm = 
		new sandmark.util.splitint.SlowCRTSplitter(128,50,w).combine
		((java.math.BigInteger [])list.toArray
		 (new java.math.BigInteger[0]));
	    java.util.List wml = new java.util.LinkedList();
	    wml.add(wm);
	    return wml.iterator();
	} catch(Exception e) { e.printStackTrace(); throw new RuntimeException(); }
    }

    public void stopRecognition() {
	try {
	    mTG.kill();
	} catch(Exception e) {}
	
    }

    public void waitForProgramExit() {
	mTG.waitForExit();
    }

    private static String [] getStrs(java.math.BigInteger parts[]) {
	String strs[] = new String[parts.length];
	for(int i = 0 ; i < parts.length ; i++) {
	    //System.out.println(parts[i]);
	    //System.out.println("part " + parts[i].toString(2));
	    byte bytes[] = parts[i].toByteArray();
	    if(bytes.length > 8)
		throw new RuntimeException();
	    int signbytes = 8 - bytes.length;
	    char sign = (bytes[0] & (1 << 7)) == 0 ? '0' : '1';
	    StringBuffer buf = new StringBuffer(bytes.length * 8);
	    for(int j = 0 ; j < 64 ; j++)
		buf.append(sign);
	    for(int j = 0 ; j < bytes.length ; j++)
		for(int k = 7 ; k >= 0 ; k--) {
		    buf.setCharAt
			(8 * (signbytes + j) + 8 - k - 1,(bytes[j] & ( 1 << k)) == 0 ? '0' : '1');
		}
	    strs[i] = buf.toString();
	    //System.out.println(strs[i]);
	    java.math.BigInteger newpart = getInt(strs[i]);
	    if(!parts[i].equals(newpart))
		throw new RuntimeException(parts[i] + " != " + newpart);
	}
	return strs;
    }
    public static java.math.BigInteger getInt(String str) {
	byte bytes[] = new byte[8];
	for(int i = 0 ; i < str.length() ; i++)
	    bytes[i / 8] |= 
		((str.charAt(i) == '0') ? 0 : 1) << (8 - 1 - (i % 8));
	return new java.math.BigInteger(bytes);
    }

    public sandmark.util.ConfigProperties getConfigProperties() {
	if(mConfigProps == null) {
	    String props[][] = {
		{"Key File","execpath.key",
		 "File in which to store the encryption key",null,"F","DE,DR"},		 
	    };
	    mConfigProps = new sandmark.util.ConfigProperties(props,null);
	}
	return mConfigProps;
    }

    public sandmark.config.ModificationProperty [] getMutations() { return null; }

    public String getShortName() { return "Execution Path"; }

    public String getLongName() { return "Execution Path Watermark"; }

    public String getAlgHTML() { return 
       "<HTML><BODY>" +
       "EPW embeds a bitstring into the sequences of " +
       "branches taken and not taken during the execution " +
	    "of a program on a particular input sequence." +
       "<TABLE>" +
       "<TR><TD>" +
      "Authors: {ash,ecarter,steppm}@cs.arizona.edu\n" +
      "</TR></TD>" +
      "</TABLE>" +
      "</BODY></HTML>"; 
    }

    public String getAlgURL() { 
        return "sandmark/watermark/execpath/doc/help.html"; }

    public String getAuthorEmail() { return "{ash,ecarter,steppm}@cs.arizona.edu"; }

    public String getAuthor() { return "Andrew Huntwork, Ed Carter, and Mike Stepp"; }

    public String getDescription() { 
	return "EPW embeds a bitstring into the sequences of " +
	    "branches taken and not taken during the execution " +
	    "of a program on a particular input sequence.";
    }
    public static void main(String argv[]) throws Exception {
	String inputFile = argv[0];
	String wm = argv[1];
	String outputFile = 
	    sandmark.Console.constructOutputFileName(inputFile,"wm");
	String inputTrace = inputFile + ".tra";
	String outputTrace = outputFile + ".tra";
	String keyFile =
	    sandmark.Console.constructOutputFileName(inputFile,"key");
	EPW e = new EPW();
	sandmark.util.ConfigProperties dwmConfig = 
	    sandmark.watermark.DynamicWatermarker.getProperties();
	if(!new java.io.File(inputTrace).exists()) {
	    dwmConfig.setProperty("Input File",inputFile);
	    dwmConfig.setProperty("Class Path","sandmark.jar");
	    dwmConfig.setProperty("Trace File",inputTrace);
	    e.startTracing
	        (sandmark.watermark.DynamicWatermarker.getTraceParams
	         (new sandmark.program.Application(inputFile)));
	    e.mTG.waitForExit();
	    e.endTracing();
	    e.stopTracing();
	    e = new EPW();
	} else {
	    System.out.println("skipping tracing: trace file " + 
			       inputTrace + " exists");
	}
	dwmConfig.setProperty("Trace File",inputTrace);
	e.getConfigProperties().setProperty("Key File",keyFile);
	dwmConfig.setProperty("Watermark",wm);
	sandmark.program.Application app = 
	    new sandmark.program.Application(inputFile);
	e.embed(sandmark.watermark.DynamicWatermarker.getEmbedParams(app));
	app.save(outputFile);
	e = new EPW();
	/*
	  sandmark.program.Application app = 
	  new sandmark.program.Application(inputFile);
	  sandmark.program.Class cls = (sandmark.program.Class)app.classes().next();
	  sandmark.program.Method method = 
	  cls.getMethod("main","([Ljava/lang/String;)V");
	  java.util.LinkedList list = new java.util.LinkedList();
	  list.add(new TraceNode("{}:234:" + cls.getName() + ":" +
	  method.getName() + method.getSignature() +
	  ":0:1:::",null));			       
	  new LoopCodeGen(app,list.iterator()).insert("010110111011111011111111");
	  app.save("foo.jar");
	*/
	dwmConfig.setProperty("Input File",outputFile);
	dwmConfig.setProperty("Class Path","sandmark.jar");
	e.getConfigProperties().setProperty("Key File",keyFile);
	dwmConfig.setProperty("Trace File",outputTrace);
	e.startRecognition
	    (sandmark.watermark.DynamicWatermarker.getRecognizeParams
	     (new sandmark.program.Application(outputFile)));
	e.mTG.waitForExit();
	for(java.util.Iterator wms = e.watermarks() ; wms.hasNext() ; ) {
	    String found = wms.next().toString();
	    System.out.println("watermark: " + found);
	    if(wm.equals(found)) {
		System.out.println("FOUND!");
		break;
	    }
	}
	e.stopRecognition();
    }
}
