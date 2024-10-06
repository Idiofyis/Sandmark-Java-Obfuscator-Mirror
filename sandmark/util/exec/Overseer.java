package sandmark.util.exec;

/**
 * The sandmark.util.exec.Overseer class contains methods for running
 * another program using the Java Debugger Interface (JDI).
 * <P>
 * To use this  class you would typically extend it, and override
 * one or more of the methods onMethodEntry, onMethodExit, and
 * onProgramExit:
 *  <P> 
 * <PRE>
 * public class MyOverseer extends sandmark.util.exec.Overseer {
 *
 *  public MyOverseer(
 *     java.util.Properties props) {
 *     super(includeClasses,excludeClasses, constructArgv(props));
 *  }
 *
 *  public void onMethodEntry (
 *    sandmark.util.exec.MethodCallData data) {
 *   }
 *
 *  public void onMethodExit (
 *    sandmark.util.exec.MethodCallData data) {
 *   }
 *
 *   public void onProgramExit (
 *      com.sun.jdi.VirtualMachine vm) {
 *   }
 * }
 * </PRE> 
 * <P>
 * The methods 'run' and 'waitToComplete' are used to start the
 * program and wait for it to exit:
 * <PRE> 
 *     MyOverseer O = new MyOverseer(includeClasses,excludeClasses,argv);
 *     O.run();
 *     ...
 *     O.waitToComplete();
 * </PRE> 
 */

public class Overseer {

   protected com.sun.jdi.VirtualMachine vm;
   protected String[] excludeClasses = {};
   protected String[] includeClasses = {};
   protected java.util.List breakPoints = new java.util.LinkedList();
   String[] argv;
   sandmark.util.exec.Output out =  null;
   sandmark.util.exec.EventThread eventThread = null;



   /**
    * Prepare to run a program under JDI debugging. 
    *
    *  @param includeClasses         Classes which should be traced.
    *  @param excludeClasses         Classes which should not be traced.
    *  @param argv                   Command-line arguments to the program 
    *                                to be executed.
    *
    *  <P> Note: Only one of includeClasses and excludeClasses can 
    *      be non-empty!
    */
   public Overseer (String[] includeClasses,
                    String[] excludeClasses,
                    String[] argv) {
      this.excludeClasses = excludeClasses;
      this.includeClasses = includeClasses;
      this.argv = argv;
   }
   
   public Overseer (String[] argv) {
      this.argv = argv;
   }
   
   public void registerBreakpoint(sandmark.util.exec.Breakpoint bp) {
      breakPoints.add(bp);
   }

   /**
    * Override this method if you want some action to take place
    * when a method is called. 
    *
    *  @param data         Information about which method was called,
    *                      where it's declared, who called it, etc.
    *
    */
   protected void onMethodEntry (sandmark.util.exec.MethodCallData data) { }

   protected void onDisconnect(){}


   /**
    * Override this method if you want some action to take place
    * when a method is exiting. 
    *
    *  @param data         Information about which method is exiting,
    *                      where it was declared from, who called it, etc.
    *
    */   
   protected void onMethodExit (sandmark.util.exec.MethodCallData data) { }

   /**
    * Override this method if you want some action to take place
    * when the program is exiting. 
    *
    *  @param vm         A handle to the virtual machine running the program.
    *
    */   

   private boolean mExited = false;
   final synchronized void onExit() {
      if(!mExited)
         onProgramExit(vm);
      try{
         vm.dispose();
      }catch(Exception e){}
      mExited = true;
      notifyAll();
   }

   public synchronized boolean exited() { return mExited; }

   protected void onProgramExit (com.sun.jdi.VirtualMachine vm) { }

   private com.sun.jdi.connect.Connector findConnector(String name) {
      java.util.List connectors = 
         com.sun.jdi.Bootstrap.virtualMachineManager().allConnectors();
      java.util.Iterator iter = connectors.iterator();
      while (iter.hasNext()) {
         com.sun.jdi.connect.Connector connector = 
            (com.sun.jdi.connect.Connector)iter.next();
         if (connector.name().equals(name)) {
            return connector;
         }
      }
      return null;
   }

   private void createVM(String argv[]) 
      throws sandmark.util.exec.TracingException {
      try {
         int traceFlags = com.sun.jdi.VirtualMachine.TRACE_NONE;
         String connectSpec = "com.sun.jdi.CommandLineLaunch";
         com.sun.jdi.connect.Connector connector = findConnector(connectSpec);

         if (connector == null) 
            throw new sandmark.util.exec.TracingException("No connector named: " +  connectSpec);

         java.util.Map connectorArgs = connector.defaultArguments();
         com.sun.jdi.connect.Connector.Argument argument = 
            (com.sun.jdi.connect.Connector.Argument)connectorArgs.get("main");

         String cmdLine = "";
         for (int i=0; i < argv.length; i++)
            cmdLine += argv[i] + " ";
         argument.setValue(cmdLine);
         sandmark.util.Log.message(0,"Running 'java " + cmdLine + "'");

         com.sun.jdi.connect.LaunchingConnector launcher = 
            (com.sun.jdi.connect.LaunchingConnector)connector;

         vm = launcher.launch(connectorArgs);
         vm.setDebugTraceMode(traceFlags);

      } catch (java.io.IOException e) {
         throw new sandmark.util.exec.TracingException("Can't launch VM: " + e.getMessage());
      } catch (com.sun.jdi.connect.VMStartException e) {
         throw new sandmark.util.exec.TracingException("Can't start VM: " + e.getMessage());
      } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException e) {
         throw new sandmark.util.exec.TracingException("Can't connect to VM: " + e.getMessage());
      }
   }
   
   private void startVM() {
      java.lang.Process process = vm.process();
      out = new sandmark.util.exec.Output(process);
      eventThread = new sandmark.util.exec.EventThread(vm, this, breakPoints); 
      eventThread.start();
      vm.resume();
   }   

   /**
    * Wait for the program to finish executing. 
    * Shutdown begins when event thread terminates
    */   
   public void waitToComplete() {
      try {
         eventThread.join();
         out.waitOutputComplete();
      } catch (InterruptedException exc) {
         sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + exc );
      }
   } 


   /**
    * Start running the program. 
    *
    */     
   public void run() throws sandmark.util.exec.TracingException {
      createVM(argv);
      startVM();
   }

   /**
    * Stop the running program. 
    *
    */
   public void STOP() {
      eventThread.STOP();
      eventThread.interrupt();
      try {vm.exit(0);} catch (Exception e) {
         sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
      }
      
      try{vm.dispose();} catch(Exception e){}

      waitToComplete();
   }
} // class Overseer

