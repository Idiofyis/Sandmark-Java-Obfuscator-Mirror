package sandmark.watermark.execpath;

public class TraceGetter extends Thread {
    private com.sun.jdi.VirtualMachine mVM;
    private boolean exited = false;
    private boolean error = false;
    private java.io.File outputFile;
    private boolean didSetFileName = false;
    private com.sun.jdi.event.EventSet exitEvent;
    public TraceGetter(String cmdLine,java.io.File outputFile) throws Exception {
	this.outputFile = outputFile;
	outputFile.delete();
	outputFile.createNewFile();
	com.sun.jdi.connect.LaunchingConnector conn =
	    com.sun.jdi.Bootstrap.virtualMachineManager().defaultConnector();
	java.util.Map args = conn.defaultArguments();
	com.sun.jdi.connect.Connector.Argument main = 
	    (com.sun.jdi.connect.Connector.Argument)args.get("main");
	main.setValue(cmdLine);
	com.sun.jdi.connect.Connector.Argument suspend = 
	    (com.sun.jdi.connect.Connector.Argument)args.get("suspend");
	//suspend.setValue("");
	mVM = conn.launch(args);
	//vm.setDebugTraceMode(~0);
	com.sun.jdi.request.EventRequestManager eventManager =
	    mVM.eventRequestManager();
	com.sun.jdi.request.VMDeathRequest deathRQ =
	    eventManager.createVMDeathRequest();
	deathRQ.setSuspendPolicy(com.sun.jdi.request.EventRequest.SUSPEND_ALL);
	deathRQ.enable();
	com.sun.jdi.request.ClassPrepareRequest prepareRQ =
	    eventManager.createClassPrepareRequest();
	prepareRQ.addClassFilter("sandmark.watermark.execpath.SandmarkListHolder");
	prepareRQ.setSuspendPolicy(com.sun.jdi.request.EventRequest.SUSPEND_ALL);
	prepareRQ.enable();

	new StreamPump(mVM.process().getInputStream()).start();
	new StreamPump(mVM.process().getErrorStream()).start();
    }
    public void startTracing() throws Exception {
	start();
    }
    public void run() {
	mVM.resume();
	com.sun.jdi.event.EventQueue queue = mVM.eventQueue();
	java.util.List trace = null;
	for(boolean done = false,resume = true ; !done ; resume = true) {
	    com.sun.jdi.event.EventSet es;
	    try {
		es = queue.remove();
	    } catch(InterruptedException e) {
		continue;
	    }
	    for(com.sun.jdi.event.EventIterator it = es.eventIterator() ; 
		!done && it.hasNext() ; ) {
		com.sun.jdi.event.Event e = it.nextEvent();
		if(e instanceof com.sun.jdi.event.VMDeathEvent) {
		    exitEvent = es;
		    done = true;
		    resume = false;
		    exited(false);
		} else if(!didSetFileName &&
			  e instanceof com.sun.jdi.event.ClassPrepareEvent) {
		    setFileName((com.sun.jdi.ClassType)
				((com.sun.jdi.event.ClassPrepareEvent)
				 e).referenceType());
		    didSetFileName = true;
		} else if(e instanceof com.sun.jdi.event.VMDisconnectEvent) {
		    exited(true);
		    done = true;
		}
	    }
	    if(resume)
		es.resume();
	}
    }
    private void setFileName(com.sun.jdi.ClassType classType) {
	try {
	    com.sun.jdi.StringReference fn = mVM.mirrorOf(outputFile + "");
	    com.sun.jdi.Field fnf = classType.fieldByName("filename");
	    classType.setValue(fnf,fn);
	} catch(Exception e) {
	    e.printStackTrace();
	    sandmark.util.Log.message(0,"Couldn't set field value: " + e);
	}
    }
    public void kill() {
	mVM.process().destroy();
	waitForExit();
    }
    private synchronized void exited(boolean err) {
	exited = true;
	error = err;
	if(!err)
	    dumpTrace();
	notifyAll();
    }
   public synchronized void waitForExit() {
      while(!exited) try { wait() ; } catch(Exception e) {}
   }
    private void dumpTrace() {
	try {
	    java.util.List classes = mVM.classesByName
		("sandmark.watermark.execpath.SandmarkListHolder");
	    if(classes.size() == 0)
		return;
	    com.sun.jdi.ReferenceType listHolderClass =
		(com.sun.jdi.ReferenceType)classes.iterator().next();
	    com.sun.jdi.Field countField = listHolderClass.fieldByName("count");
	    com.sun.jdi.IntegerValue iv = 
		(com.sun.jdi.IntegerValue)listHolderClass.getValue(countField);
	    if(iv.value() == 0)
		return;
	    java.io.File tf = outputFile;
	    java.io.FileOutputStream fos = new java.io.FileOutputStream(tf,true);
	    java.io.PrintWriter pw = new java.io.PrintWriter(fos);
	    com.sun.jdi.Field listHolderField = listHolderClass.fieldByName("head");
	    com.sun.jdi.ObjectReference listElem =
		(com.sun.jdi.ObjectReference)listHolderClass.getValue(listHolderField);
	    com.sun.jdi.Field nextElemField = listElem == null ? null : 
		listElem.referenceType().fieldByName("next");
	    com.sun.jdi.Field dataField = listElem == null ? null :
		listElem.referenceType().fieldByName("data");
	    while(listElem != null) {
		pw.println(((com.sun.jdi.StringReference)
			    listElem.getValue(dataField)).value());
		listElem = (com.sun.jdi.ObjectReference)
		    listElem.getValue(nextElemField);
	    }
	    pw.close();
	} catch(java.io.IOException e) {
	    sandmark.util.Log.message(0,"Tracing failed!");
	} finally {
	    exitEvent.resume();
	}
    }

    public java.util.Iterator getTrace() throws java.io.IOException {
	try { return new TraceReader(outputFile); }
	catch(java.io.IOException e) { return null; }
    }

    public static void main(String argv[]) throws Exception {
	String outfile = argv[0] + ".trace";
	String wm = "0101011011101111101111111101111111111111011111111111111111111101010110111011111";
	sandmark.program.Application app = 
	    new sandmark.program.Application(argv[0]);
	sandmark.program.Class mainClass = app.getMain();
	sandmark.program.Method mainMethod = 
	    mainClass.getMethod("main","([Ljava/lang/String;)V");
	//new LoopCodeGen(app,new TraceNode[] { 
	//new TraceNode(null,null), //XXXash need a valid line
	//}).insert(wm);
	app.save("foo.jar");
	new Tracer(app,false);
	String newname = "pre." + argv[0];
	app.save(newname);
	String cmdline = "-jar " + newname;
	TraceGetter tg = new TraceGetter(cmdline,new java.io.File(outfile));
	tg.startTracing();
	tg.waitForExit();
	tg.kill();
	java.util.Iterator trace = tg.getTrace();
	Analyzer analyzer = new Analyzer(trace);
	TraceNode nodes[] = analyzer.getTrace("main");
	String bits = null;//XXXash fixme: Analyzer.getBitSequence(nodes);
	System.out.println("0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
	System.out.println(bits);
	System.out.println(bits.indexOf(wm) == -1 ? "didn't find wm" : "found wm");
    }
}
