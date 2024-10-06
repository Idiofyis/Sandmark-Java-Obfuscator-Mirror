package sandmark.util.exec;

public class EventThread extends Thread {

   private final com.sun.jdi.VirtualMachine vm;   // Running VM
   private final sandmark.util.exec.Overseer handler;
   private java.util.List breakpointRequestList = new java.util.LinkedList();
   private java.util.Hashtable breakpointTable = new java.util.Hashtable();
   private volatile boolean connected = true;  // Connected to VM


   public EventThread(com.sun.jdi.VirtualMachine vm,
                      sandmark.util.exec.Overseer handler,
                      java.util.List breakpointRequests) {
      super("event-handler");
      this.vm = vm;
      this.handler = handler;
      registerBreakpoints(breakpointRequests);
      initExitDetector();
   }

   public void run() { 
      com.sun.jdi.event.EventQueue queue = vm.eventQueue();
      while (connected) {
         try {
            com.sun.jdi.event.EventSet eventSet = queue.remove();
            com.sun.jdi.event.EventIterator it = eventSet.eventIterator();
            while (it.hasNext()) {
               if (!connected) break;
               com.sun.jdi.event.Event event = it.nextEvent();
               handleEvent(event);
            }
            if (!connected) break;
            eventSet.resume();
         } catch (java.lang.InterruptedException exc) {
            sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + exc );
         } catch (com.sun.jdi.VMDisconnectedException discExc) {
            break;
         }
      }
      onExit();
   }

   public void STOP() {
      onExit();
      connected = false;
      interrupt();
   }

   private void handleEvent(com.sun.jdi.event.Event event) {
      if (event instanceof com.sun.jdi.event.MethodEntryEvent) {
         methodEntryEvent((com.sun.jdi.event.MethodEntryEvent)event);
      } else if (event instanceof com.sun.jdi.event.BreakpointEvent) {
         breakpointEvent((com.sun.jdi.event.BreakpointEvent)event);
      } else if (event instanceof com.sun.jdi.event.ClassPrepareEvent) {
         com.sun.jdi.event.ClassPrepareEvent ev = (com.sun.jdi.event.ClassPrepareEvent)event;
         classPrepareEvent((com.sun.jdi.event.ClassPrepareEvent)event);
      } else if (event instanceof com.sun.jdi.event.MethodExitEvent) {
         methodExitEvent((com.sun.jdi.event.MethodExitEvent)event);
      } else if (event instanceof com.sun.jdi.event.VMStartEvent) {
         // do nothing
      } else if (event instanceof com.sun.jdi.event.ThreadDeathEvent) {
         detectProgramExit(((com.sun.jdi.event.ThreadDeathEvent)event).thread());
      } else if ((event instanceof com.sun.jdi.event.VMDeathEvent) ||
                 (event instanceof com.sun.jdi.event.VMDisconnectEvent)) {
         disconnectEvent();
         connected = false;
      } else {
         // throw new Error("Unexpected event type");
      }
   }

   void onExit() { handler.onExit(); }

   //-----------------------------------------------------------------------
   /*
    * When a breakpoint event is generated we run through our
    * list of breakpoint handlers and invoke the ones that
    * pertain to this breakpoint.
    */
   void breakpointEvent(com.sun.jdi.event.BreakpointEvent event) {
      BreakpointEvent bpe = (BreakpointEvent) breakpointTable.get(event.request());
      if (bpe != null) {
         com.sun.jdi.Method method = event.location().method();
         sandmark.util.exec.MethodCallData data = 
            new sandmark.util.exec.MethodCallData(vm, event, method);
         bpe.breakpoint.Action(data);
      }
   }

   void methodExitEvent(com.sun.jdi.event.MethodExitEvent event)  {
      com.sun.jdi.Method method = event.method();
      sandmark.util.exec.MethodCallData data = 
         new sandmark.util.exec.MethodCallData(vm, event, method);
      handler.onMethodExit(data); 

      //  String methodName = method.name();
      //  if (methodName.equals("main"))    // This is wrong.
      //   onExit();
   }

   void disconnectEvent(){
      handler.onDisconnect();
   }

   void methodEntryEvent(com.sun.jdi.event.MethodEntryEvent event)  {
      com.sun.jdi.Method method = event.method();
      sandmark.util.exec.MethodCallData data = 
         new sandmark.util.exec.MethodCallData(vm, event, method);
      handler.onMethodEntry(data); 
   }

   //-----------------------------------------------------------------------
   /*
    * When a class is finally loaded, we can see if there are any
    * pending requests to set breakpoints in this class. If so,
    * we do it. We also keep track of the breakpoints we've set
    * so we can perform the requested actions when a breakpoint
    * is hit.
    */
   class BreakpointEvent {
      public sandmark.util.exec.Breakpoint breakpoint;
      public com.sun.jdi.request.BreakpointRequest request;

      public BreakpointEvent (
                              sandmark.util.exec.Breakpoint breakpoint,
                              com.sun.jdi.request.BreakpointRequest request) {
         this.breakpoint = breakpoint;
         this.request = request; 
      }
   }
   void setBreakpoint(com.sun.jdi.ReferenceType Class, sandmark.util.exec.Breakpoint bp) {
      java.util.List methods = null;
      if (bp.signature.equals("*"))
         methods = Class.methodsByName(bp.methodName);
      else
         methods = Class.methodsByName(bp.methodName,bp.signature);
      com.sun.jdi.request.EventRequestManager mgr = vm.eventRequestManager();
      java.util.Iterator iter = methods.iterator();
      while (iter.hasNext()) {
         com.sun.jdi.Method method = (com.sun.jdi.Method) iter.next();
         if (method.declaringType().equals(Class)) {
            com.sun.jdi.Location start = method.locationOfCodeIndex(0);
            com.sun.jdi.request.BreakpointRequest bpr = mgr.createBreakpointRequest(start);
            bpr.enable();
            breakpointTable.put(bpr, new BreakpointEvent(bp, bpr));
         }
      }
   }

   //-----------------------------------------------------------------------
   /*
    * Register breakpoints which should be set as soon as possible.
    * However, a breakpoint can't be set until the corresponding class
    * has been loaded. So, we have to request an event when the
    * class is loaded, and wait to set the breakpoint until then.
    */
   public void registerBreakpoint (
                                   sandmark.util.exec.Breakpoint bp) {
      if (!bp.className.equals("*")) {
         java.util.List classes = vm.classesByName(bp.className);
         java.util.Iterator iter = classes.iterator();
         if (iter.hasNext()) {
            com.sun.jdi.ClassType theClass = (com.sun.jdi.ClassType)iter.next();
            if (theClass.isPrepared()) {
               setBreakpoint(theClass,bp);
               return;
            }
         }
      }
      com.sun.jdi.request.EventRequestManager mgr = vm.eventRequestManager();
      com.sun.jdi.request.ClassPrepareRequest classReq = mgr.createClassPrepareRequest();
      if (bp.className.equals("*")) {
         for(int i=0; i<bp.excludeClasses.length; i++) 
            classReq.addClassExclusionFilter(bp.excludeClasses[i]);
      } else {
         classReq.addClassFilter(bp.className);
      }
      classReq.enable();
      breakpointRequestList.add(bp);
   }

   public void registerBreakpoints (
                                    java.util.List bpl) {
      java.util.Iterator iter = bpl.iterator();
      while (iter.hasNext()) {
         sandmark.util.exec.Breakpoint bp = (sandmark.util.exec.Breakpoint) iter.next();
         registerBreakpoint(bp);
      }
   }

   //-----------------------------------------------------------------------
   /*
    * When a class is loaded we check to see if there are any pending
    * requests to set breakpoints in this class. If so, we do it.
    */
   void classPrepareEvent(com.sun.jdi.event.ClassPrepareEvent event) {
      com.sun.jdi.ReferenceType rt = event.referenceType();
      String className = rt.name();
      java.util.Iterator iter = breakpointRequestList.iterator();
      while (iter.hasNext()) {
         sandmark.util.exec.Breakpoint bp = (sandmark.util.exec.Breakpoint) iter.next();
         if (bp.className.equals("*") || bp.className.equals(className))
            setBreakpoint(rt, bp);
      }
   }


   //-----------------------------------------------------------------------
   //                          Detect exiting program
   //-----------------------------------------------------------------------
   /*
    * In JDI 1.3 there is no simple way to detect that the debugee is exiting.
    * So, we do it the hard way:
    * <ul>
    *   <li> put a breakpoint on java.lang.Runtime.exit(),
    *   <li> when a thread dies we check if all other threads
    *        are daemon threads.
    * </ul>
    * This doesn't work well. If jdk1.4 is available we use the new
    * VMDeathRequest, if we can.
    */
   void initExitDetector() {
      if (vm.canRequestVMDeathEvent()) {
         com.sun.jdi.request.EventRequestManager mgr = vm.eventRequestManager();
         com.sun.jdi.request.VMDeathRequest req = mgr.createVMDeathRequest();
         req.enable();
      } else 
         oldInitExitDetector();
   }

   void oldInitExitDetector() {
      registerBreakpoint(new ExitBreakpoint("java.lang.Runtime"));
      registerBreakpoint(new ExitBreakpoint("java.lang.System"));
      com.sun.jdi.request.EventRequestManager mgr = vm.eventRequestManager();
      com.sun.jdi.request.ThreadDeathRequest req = mgr.createThreadDeathRequest();
      req.enable();
   }


   /*
    * This method is called whenever a thread dies. If, at this point, all
    * threads are daemon threads, we can conclude that the program is
    * about to exit. We call onExit().
    */
   void detectProgramExit(com.sun.jdi.ThreadReference dyingThread) {
      java.util.Iterator threads = vm.allThreads().iterator();
      while (threads.hasNext()) {
         com.sun.jdi.ThreadReference thread = (com.sun.jdi.ThreadReference)threads.next();
         if(dyingThread.uniqueID() != thread.uniqueID())
            if (!threadIsDaemon(thread)) return;
      }
      onExit();
   }

   /*
    * Return a handle to the method className.methodName. Can be used to
    * call this method using 'invokeMethod'. This method assumes there
    * is no overloading: there's only one class named className and only
    * one method in this class named methodName.
    */
   com.sun.jdi.Method lookupMethod(String className, String methodName) {
      java.util.List classes = vm.classesByName(className);
      com.sun.jdi.ReferenceType Class = (com.sun.jdi.ReferenceType)classes.iterator().next();
      java.util.List methods = Class.methodsByName(methodName);
      return (com.sun.jdi.Method) methods.iterator().next();
   }

   /*
    * Return true if a thread is a daemon thread.
    */
   boolean threadIsDaemon(com.sun.jdi.ThreadReference thread) {
      com.sun.jdi.Method isDaemon = lookupMethod("java.lang.Thread", "isDaemon");
      try{
         com.sun.jdi.BooleanValue res = (com.sun.jdi.BooleanValue) thread.invokeMethod(
                                                                                       thread, 
                                                                                       isDaemon, 
                                                                                       new java.util.LinkedList(), 
                                                                                       com.sun.jdi.ObjectReference.INVOKE_SINGLE_THREADED);
         boolean result = res.value();
         return result;
      } catch (Exception e) {
         sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
      }
      return false;
   }

   /*
    * We set a breakpoint on java.lang.Runtime.exit(). If it is called 
    * we can conclude that the program is about to exit. We call onExit().
    */
   class ExitBreakpoint extends sandmark.util.exec.Breakpoint{
      public ExitBreakpoint (String exitclass) {
         super(exitclass,"exit");
      }
      public void Action(sandmark.util.exec.MethodCallData data) {
         onExit();
      }
   }
}


