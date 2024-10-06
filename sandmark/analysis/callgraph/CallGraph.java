package sandmark.analysis.callgraph;

/** This class is a graph of method calls within an application.
 *  The nodes of this graph will be all the sandmark.program.Method objects 
 *  in every class of the application. The edges represent the fact that
 *  one method calls (or might call) another. That is, there will be an edge from 
 *  M1 to M2 if there is some invoke instruction in M1 that could invoke M2. 
 *  All the edges of this graph are CallGraphEdges, and each one contains the
 *  handles of the instructions where the invoke was performed. That is,
 *  if there is an edge E=(M1,M2), then E will also contain all the instruction
 *  handles in M1 that might invoke M2. 
 *  Since the only nodes in this graph are methods defined in the given application,
 *  no java library methods are represented. Therefore whenever a method makes a call 
 *  that might invoke a java library method, there is an edge from that method to
 *  the static constant object JAVA_LIB_METHOD (which is just an Object, and not a
 *  sandmark.program.Method). Also, if any method in the application overrides a java
 *  library method, then it could potentially be invoked by some other java library method 
 *  via dynamic method invocation. Thus, for each such method M there will be an edge
 *  (JAVA_LIB_METHOD, M) in the graph, which contains no instruction handles.
 */
public class CallGraph extends sandmark.util.newgraph.MutableGraph{
   public static final Object JAVA_LIB_METHOD = new Object();

   public CallGraph(CallGraph graph){
      super();
      
      for (java.util.Iterator nodes=graph.nodes();nodes.hasNext();)
         addNode(nodes.next());
      
      for (java.util.Iterator eiter=graph.edges();eiter.hasNext();)
         addEdge(new CallGraphEdge((CallGraphEdge)eiter.next()));
   }

   public CallGraph(sandmark.program.Application app) 
   	throws sandmark.analysis.classhierarchy.ClassHierarchyException {
      addNode(JAVA_LIB_METHOD);

      sandmark.analysis.classhierarchy.ClassHierarchy ch = app.getHierarchy();

      for (java.util.Iterator citer=app.classes();citer.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
         for (java.util.Iterator miter=clazz.methods();miter.hasNext();){
            sandmark.program.Method meth = (sandmark.program.Method)miter.next();
            addNode(meth);
         }
      }
      // I've put in the nodes, now build the edges

      for (java.util.Iterator citer = app.classes();citer.hasNext(); ){
         sandmark.program.Class clazz = (sandmark.program.Class)citer.next();

         for (java.util.Iterator miter = clazz.methods();miter.hasNext();){
            sandmark.program.Method caller = (sandmark.program.Method)miter.next();

            // check to see if I override any library method 
            // (and hence might be called by a library method)

            if (!caller.isPrivate() && !caller.isAbstract() && !caller.isStatic() && !caller.getName().equals("<init>")){
               sandmark.program.Class[] superClasses = ch.superClasses(clazz);
               for (int i=1;i<superClasses.length;i++){
                  sandmark.program.Method superMethod = superClasses[i].getMethod(caller.getName(), caller.getSignature());
                  if (superMethod!=null){
                     if (!superMethod.isPrivate() && !superMethod.isFinal() && 
                         (superClasses[i] instanceof sandmark.program.LibraryClass)){
                        addEdge(new CallGraphEdge(JAVA_LIB_METHOD, caller));
                        break;
                     }else
                        break;
                  }
               }
            }

            // check to see what other methods I call
            
            org.apache.bcel.generic.InstructionList ilist = caller.getInstructionList();
            if (ilist==null) 
               continue;
            
            org.apache.bcel.generic.InstructionHandle[] handles = ilist.getInstructionHandles();
            for (int i=0;i<handles.length;i++){
               if (handles[i].getInstruction() instanceof org.apache.bcel.generic.InvokeInstruction){
                  Object[] callees = getPotentialTargets((org.apache.bcel.generic.InvokeInstruction)handles[i].getInstruction(), clazz, app);
                  for (int j=0;j<callees.length;j++){
                     if (!hasEdge(caller, callees[j]))
                        addEdge(new CallGraphEdge(caller, callees[j]));
                     
                     for (java.util.Iterator edgeiter = outEdges(caller); edgeiter.hasNext();){
                        sandmark.util.newgraph.Edge edge = (sandmark.util.newgraph.Edge)edgeiter.next();
                        if (edge.sinkNode()==callees[j]){
                           // exactly one edge should satisfy this test
                           CallGraphEdge cgedge = (CallGraphEdge)edge;
                           cgedge.addHandle(handles[i]);
                        }
                     }
                  }
               }
            }
         }
      }
   }


   /** This returns a new CallGraph that is a subgraph of the original,
    *  which contains only 'method' and all other nodes in the graph that
    *  have a path to 'method' (i.e. all the methods that directly or indirectly
    *  invoke 'method')
    */
   public sandmark.analysis.callgraph.CallGraph callerSubgraph(sandmark.program.Method method){
      sandmark.analysis.callgraph.CallGraph newgraph = 
         new sandmark.analysis.callgraph.CallGraph(this);
      
      java.util.HashSet visited = new java.util.HashSet();
      java.util.LinkedList queue = new java.util.LinkedList();
      queue.add(method);
      
      while(!queue.isEmpty()){
         Object next = queue.removeFirst();
         if (visited.contains(next))
             continue;
         visited.add(next);
         for (java.util.Iterator edges=newgraph.inEdges(next); edges.hasNext();)
            queue.add(((sandmark.util.newgraph.Edge)edges.next()).sourceNode());
      }

      for (java.util.Iterator nodes=newgraph.nodes();nodes.hasNext();){
         Object next=nodes.next();
         if (!visited.contains(next))
            newgraph.removeNode(next);
      }
      return newgraph;
   }



   /** Returns an iterator containing sandmark.program.Method objects,
    *  and possibly also the static constant JAVA_LIB_METHOD.
    *  (note that the method itself may be in this list, if it is recursive)
    */
   public java.util.Iterator getCallers(sandmark.program.Method m){
      return preds(m);
   }

   /** Returns an iterator containing sandmark.program.Method objects,
    *  and possibly also the static constant JAVA_LIB_METHOD.
    *  (note that the method itself may be in this list, if it is recursive)
    */
   public java.util.Iterator getCallees(sandmark.program.Method m){
      return succs(m);
   }

   /** This will return a set of InstructionHandles of the invoke
    *  instructions where caller might invoke callee. (it just returns 
    *  the contents of CallGraphEdge.getHandles() for the corresponding
    *  edge in the callgraph)
    */
   public java.util.Set getInvokeHandles(sandmark.program.Method caller,
                                         sandmark.program.Method callee){

      for (java.util.Iterator edgeiter=inEdges(callee);edgeiter.hasNext();){
         CallGraphEdge edge = (CallGraphEdge)edgeiter.next();
         if (edge.sourceNode()==caller){
            // exactly one edge should satisfy this test
            return edge.getHandles();
         }
      }
      return new java.util.HashSet();
   }


   private static Object[] getPotentialTargets(org.apache.bcel.generic.InvokeInstruction invoke,
                                               sandmark.program.Class referent,
                                               sandmark.program.Application app) 
   	throws sandmark.analysis.classhierarchy.ClassHierarchyException {

      sandmark.analysis.classhierarchy.ClassHierarchy ch = app.getHierarchy();
      org.apache.bcel.generic.ConstantPoolGen cpg = referent.getConstantPool();
      java.util.Vector targets = new java.util.Vector(10);

      String classname = invoke.getClassName(cpg);
      String methodname = invoke.getMethodName(cpg);
      String methodsig = invoke.getSignature(cpg);
      
      sandmark.program.Method resolvedMethod = null;
      sandmark.program.Class calleeClass = app.findClass(classname);
      if (calleeClass==null)
	  throw new Error("Referenced class cannot be found");
      
      if (calleeClass.isInterface()){
         resolvedMethod = ch.resolveInterfaceMethodReference(new sandmark.util.MethodID(methodname, methodsig, classname), referent);
      }else{
         resolvedMethod = ch.resolveMethodReference(new sandmark.util.MethodID(methodname, methodsig, classname), referent);
      }
      if (resolvedMethod==null)
         throw new Error("Given method cannot be found");

      if (resolvedMethod.getEnclosingClass() instanceof sandmark.program.LibraryClass)
         targets.add(JAVA_LIB_METHOD);

      if (invoke instanceof org.apache.bcel.generic.INVOKESPECIAL){
         resolvedMethod = ch.findInvokeSpecialTarget(new sandmark.util.MethodID(methodname, methodsig, classname), referent);
         if (resolvedMethod==null)
            throw new Error("Cannot find invokespecial target");
         if (!(resolvedMethod.getEnclosingClass() instanceof sandmark.program.LibraryClass))
            targets.add(resolvedMethod);
      }else if (invoke instanceof org.apache.bcel.generic.INVOKESTATIC){
         if (!(resolvedMethod.getEnclosingClass() instanceof sandmark.program.LibraryClass))
            targets.add(resolvedMethod);
      }else if (invoke instanceof org.apache.bcel.generic.INVOKEVIRTUAL){
         if (resolvedMethod.isPrivate()){
            targets.add(resolvedMethod);
         }else{
            for (java.util.Iterator citer=app.classes();citer.hasNext();){
               sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
               if (clazz.getMethod(methodname, methodsig)==null)
                  continue;
               
               if (clazz.getName().equals(resolvedMethod.getEnclosingClass().getName()) || 
                   (ch.classExtends(clazz, resolvedMethod.getEnclosingClass()) && !resolvedMethod.getName().equals("<init>"))){
                  targets.add(clazz.getMethod(methodname, methodsig));
               }
            }
         }
      }else if (invoke instanceof org.apache.bcel.generic.INVOKEINTERFACE){
         for (java.util.Iterator citer=app.classes();citer.hasNext();){
            sandmark.program.Class clazz = (sandmark.program.Class)citer.next();
            if (clazz.getMethod(methodname, methodsig)==null)
               continue;

            if (ch.reachable(resolvedMethod.getEnclosingClass(), clazz))
               targets.add(clazz.getMethod(methodname, methodsig));
         }
      }
      return targets.toArray(new Object[0]);
   }


   public static void main(String args[]) throws Exception{
      if (args.length<1) return;

      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);

      sandmark.analysis.callgraph.CallGraph callgraph = 
         new sandmark.analysis.callgraph.CallGraph(app);

      System.out.println("done building callgraph");

      if (args.length>1)
      for (java.util.Iterator iter=callgraph.edges();iter.hasNext();){
         CallGraphEdge edge = (CallGraphEdge)iter.next();
         String source = null, sink=null;
         org.apache.bcel.generic.ConstantPoolGen cpg = null;

         if (edge.sourceNode() instanceof sandmark.program.Method){
            sandmark.program.Method m = (sandmark.program.Method)edge.sourceNode();
            source = m.getEnclosingClass().getName()+"."+m.getName()+m.getSignature();
            cpg = m.getConstantPool();
         }else{
            source = "JAVA_LIB_METHOD";
         }

         if (edge.sinkNode() instanceof sandmark.program.Method){
            sandmark.program.Method m = (sandmark.program.Method)edge.sinkNode();
            sink = m.getEnclosingClass().getName()+"."+m.getName()+m.getSignature();
         }else{
            sink = "JAVA_LIB_METHOD";
         }
         String handles = "";
         for (java.util.Iterator hiter = edge.getHandles().iterator();hiter.hasNext();){
            org.apache.bcel.generic.InstructionHandle ih = 
               (org.apache.bcel.generic.InstructionHandle)hiter.next();
            org.apache.bcel.generic.InvokeInstruction invoke = 
               (org.apache.bcel.generic.InvokeInstruction)ih.getInstruction();
            
            handles += "{ "+ih.getPosition() + " " + invoke.getName() + " " + invoke.getClassName(cpg) + "."+invoke.getMethodName(cpg)+invoke.getSignature(cpg) +" } ";
         }

         System.out.println("("+source+", "+handles+", "+sink+")");
      }
   }
}
