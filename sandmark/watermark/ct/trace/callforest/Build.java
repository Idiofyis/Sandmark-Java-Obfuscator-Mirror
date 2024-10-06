package sandmark.watermark.ct.trace.callforest;

public class Build {

sandmark.watermark.ct.trace.TracePoint annotationPoints[];
sandmark.util.ConfigProperties props;
String annotatorClass;
sandmark.analysis.classhierarchy.ClassHierarchy classHierarchy;
sandmark.watermark.ct.encode.storage.StorageClass[] storageClasses;

java.util.Vector forest = null;
java.util.Hashtable root2Graph = null;

static final int DEFAULT_WEIGHT = 0;

public Build(
    sandmark.watermark.ct.trace.TracePoint annotationPoints[],
    sandmark.analysis.classhierarchy.ClassHierarchy classHierarchy,
    sandmark.util.ConfigProperties props){
   this.annotationPoints = annotationPoints;
   this.classHierarchy = classHierarchy;
   this.props = props;
   annotatorClass = props.getProperty(
        "DWM_CT_AnnotatorClass",
        "sandmark.watermark.ct.trace.Annotator");

   root2Graph = new java.util.Hashtable();
   forest = new java.util.Vector();
   storageClasses = sandmark.watermark.ct.encode.storage.GlobalStorage.getStorageClasses();
}


/**************************************************************************************/

static class Data {
   sandmark.util.newgraph.MutableGraph graph;
   sandmark.watermark.ct.trace.callforest.Node enterNode;
   sandmark.watermark.ct.trace.callforest.Node exitNode;
   sandmark.watermark.ct.trace.callforest.Node callNode;
   sandmark.watermark.ct.trace.callforest.Node returnNode;

   public String toString() {
      String S = "";
      S += "enter=" + ((enterNode!=null)?""+enterNode.nodeNumber():"null");
      S += "; exit=" + ((exitNode!=null)?""+exitNode.nodeNumber():"null");
      S += "; call=" + ((callNode!=null)?""+callNode.nodeNumber():"null");
      S += "; return=" + ((returnNode!=null)?""+returnNode.nodeNumber():"null");
      return S;
   }

   public static Data[][] createDataArray(
      sandmark.watermark.ct.trace.TracePoint annotationPoints[]) {
      Data[][] data = new Data[annotationPoints.length][];
      for (int j=0; j < annotationPoints.length; j++) {
         data[j] = new Data[annotationPoints[j].stack.length];
         for (int i=0; i<annotationPoints[j].stack.length; i++)
            data[j][i] = new Data();
      }
      return data;
  }
}

/************************************************************************/

/**
  * Build the information flow graph.
  * Add the following edges to the graph:
  *      callerEntryNode   ->  callerCallNode
  *      callerCallNode    ->  calleeEntryNode
  *      calleeExitNode    ->  callerReturnNode
  *      callerReturnNode  ->  callerExitNode
  *      calleeEnterNode   ->  calleeExitNode
  *
  * The top of the stack is in stack[0]. This holds the sm$mark()-call.
  *
  * The array of stacks looks like this:
  * <PRE>
  *      j==0              j==1
  * =============================================================
  *                      +-------+
  *                    0 |sm$mark|   <--- topFrame
  *    +-------+         +-------+                              ^^
  *  0 |sm$mark|       1 |       |                              ||
  *    +-------+         +-------+                              ||
  *  1 |       |       2 |callee |   <--- calleeFrame  <-- i    ||
  *    +-------+         +-------+                              ||
  *  2 |       |       3 |caller |   <--- callerFrame           ||
  *    +-------+         +-------+                              ||
  *  3 |       |       4 |       |                              ||
  *    +-------+         +-------+
  *  4 | main  |       5 | main  |   <-- bottomFrame
  *    +-------+         +-------+
  *    lastStack        currentStack
  * </PRE>
  * Note that, for some retarded reason, sm$mark is at the bottom of
  * the stack, not the top. In other words, stacks grow from high
  * indices to low. This means that the lastStack and the currentStack
  * are indexed differently.
  * <P>
  * <code>lastStackCurrentFrameIndex</code> holds the index of the frame
  * in the last stack corresponding to the current frame in the
  * current stack.
  * <P>
  * <code>data</code> is a matrix that holds data for each stack
  * frame. data[j][i] holds the nodes created for frame i in stack j.
  */
java.util.Vector build() {
    Data[][] data = Data.createDataArray(annotationPoints);

    for (int j=0; j < annotationPoints.length ; j++) {
        sandmark.util.StackFrame[] lastStack = (j>0)?annotationPoints[j-1].stack:null;
        sandmark.util.StackFrame[] currentStack = annotationPoints[j].stack;
        sandmark.util.newgraph.MutableGraph graph = 
	   newGraph(currentStack[currentStack.length-1]);

        for (int i=currentStack.length-1; i>=0; i--) {
            sandmark.util.StackFrame calleeFrame = currentStack[i];

            boolean bottomFrame = i==(currentStack.length-1);
            boolean markFrame   = i==0;

            Data calleeFrameData = data[j][i];
            calleeFrameData.graph = graph;

            int lastStackCurrentFrameIndex = -1;
            if (lastStack!=null)
               lastStackCurrentFrameIndex = lastStack.length-(currentStack.length-i);

            if ((j==0) ||
                (!((lastStackCurrentFrameIndex>=0) &&
                    sameStackFrame(lastStack[lastStackCurrentFrameIndex], calleeFrame)))) {
               calleeFrameData.enterNode = addNode(graph, calleeFrame, sandmark.watermark.ct.trace.callforest.Node.ENTER);
               calleeFrameData.exitNode = addNode(graph, calleeFrame, sandmark.watermark.ct.trace.callforest.Node.EXIT);
               addEdge(graph, calleeFrameData.enterNode, calleeFrameData.exitNode);
               //               System.out.println("CALLEE: " + calleeFrameData.toString());

               if (bottomFrame)
                  graph.setRoot(calleeFrameData.enterNode);

               if (!bottomFrame) {
                  sandmark.util.StackFrame callerFrame = currentStack[i+1];
                  Data callerFrameData = data[j][i+1];
                  sandmark.watermark.ct.trace.callforest.Node lastCallerReturnNode = callerFrameData.returnNode;
                  callerFrameData.callNode = addNode(graph, callerFrame, sandmark.watermark.ct.trace.callforest.Node.CALL);
                  callerFrameData.returnNode = addNode(graph, callerFrame, sandmark.watermark.ct.trace.callforest.Node.RETURN);

                  //                     System.out.println("CALLER: " + callerFrameData.toString());
                  //                     System.out.println("lastCallerReturnNode: " +
                  //      ((lastCallerReturnNode!=null)?""+lastCallerReturnNode.nodeNumber():"null"));

                  addEdge(graph, callerFrameData.enterNode, callerFrameData.callNode);
                  if (okToAddStorageArgument(calleeFrameData.enterNode.getMethod())) {
                     addEdge(graph, callerFrameData.callNode, calleeFrameData.enterNode);
                     addEdge(graph, calleeFrameData.exitNode, callerFrameData.returnNode);
                  }
                  addEdge(graph, callerFrameData.returnNode, callerFrameData.exitNode);

                  if (lastCallerReturnNode != null)
                     addEdge(graph, lastCallerReturnNode, callerFrameData.callNode);
                }
            } else {
               calleeFrameData.enterNode = data[j-1][lastStackCurrentFrameIndex].enterNode;
               calleeFrameData.exitNode = data[j-1][lastStackCurrentFrameIndex].exitNode;
               calleeFrameData.callNode = data[j-1][lastStackCurrentFrameIndex].callNode;
               calleeFrameData.returnNode = data[j-1][lastStackCurrentFrameIndex].returnNode;
            }
        }
    }
    return forest;
}

/************************************************************************/
/**
 * Create a new graph (if necessary) corresponding to
 * the bottom (root) stack frame of a thread.
 *    @param root The first (bottom) call frame of a thread
 */
sandmark.util.newgraph.MutableGraph newGraph (
   sandmark.util.StackFrame root ) {
    sandmark.watermark.ct.trace.callforest.CallFrame cf =
       new sandmark.watermark.ct.trace.callforest.CallFrame(
           root.getLocation().getMethod(), root.getThreadID(), root.getFrameID());

    if (!root2Graph.containsKey(cf)) {
        sandmark.util.newgraph.MutableGraph graph = 
	   new sandmark.util.newgraph.MutableGraph();
        root2Graph.put(cf,graph);
        forest.add(graph);
        graph.setHeader("Call graph for: " + cf);
        return graph;
    } else {
       //        System.out.println( "Cached newGraph: "+cf );
        return (sandmark.util.newgraph.MutableGraph)root2Graph.get(cf);
    }
}

/**
 * Return true if method is one of the mark()-methods in the annotator class.
 * @param method the method
 */
boolean isMarkMethod(
   sandmark.util.MethodID method) {
   return method.getName().equals("sm$mark"); // && method.sourceName.equals(annotatorClass);
}

/**
 * Add a node to the call graph.
 * @param graph the call graph
 * @param data  the information to be added to the node
 * @param kind  the kind of node to create
 * sandmark.watermark.ct.trace.callforest.Node lists the possible node kinds.
 */
private sandmark.watermark.ct.trace.callforest.Node addNode(
   sandmark.util.newgraph.MutableGraph graph,
   sandmark.util.StackFrame frame,
   int kind) {

   sandmark.util.MethodID method = frame.getLocation().getMethod();
   sandmark.watermark.ct.trace.callforest.Node node =
      new sandmark.watermark.ct.trace.callforest.Node (
          frame.getLocation(), frame.getThreadID(), frame.getFrameID(), 
		  -1, kind, isMarkMethod(method));

   graph.addNode(node);
   return node;
}

/**
 * Add an edge of a particular weight to the call graph.
 * @param graph  the call graph
 * @param from   the source node
 * @param to     the sink node
 * sandmark.watermark.ct.trace.callforest.Node lists the possible node kinds.
 */
private void addEdge(
   sandmark.util.newgraph.MutableGraph graph,
   sandmark.watermark.ct.trace.callforest.Node from,
   sandmark.watermark.ct.trace.callforest.Node to) {
   sandmark.watermark.ct.trace.callforest.Edge edge =
      new sandmark.watermark.ct.trace.callforest.Edge(from, to, 
						      "stupid_label", 
						      DEFAULT_WEIGHT);
   if (!graph.hasEdge(from,to)) {
      //      System.out.println("Adding edge: " + from.nodeNumber() + "-->" + to.nodeNumber());
      graph.addEdge(edge);
   }
}

/**
 * Return true if it OK to add a storage argument to this method.
 * At this point we don't know exactly which type
 * of storage we are going to use, so we'll try all of
 * them.
 * @param method  the method
 */
boolean okToAddStorageArgument(
   sandmark.util.MethodID method) {
   if (classHierarchy == null)
      return false;

   for(int i=0; i<storageClasses.length; i++) {
      String typeName = storageClasses[i].typeName(props);
      String typeSig = org.apache.bcel.classfile.Utility.getSignature(typeName);

      String newSignature = addArgumentLast(method.getSignature(),typeSig);

      sandmark.util.MethodID newMethod =
         new sandmark.util.MethodID(method.getName(), newSignature, method.getClassName());

      try {
         // System.out.println(">>>>>>>>>> methodRenameOK(" + method + "," + newMethod + ")" );
         if (!classHierarchy.methodRenameOK(method,newMethod))
            return false;
      }catch(sandmark.analysis.classhierarchy.ClassHierarchyException exc) {
         System.out.println(exc.toString());
      }
   }
   return true;
}

    String addArgumentLast(String signature,String newType) {
	org.apache.bcel.generic.Type returnType = 
	    org.apache.bcel.generic.Type.getReturnType(signature);
	org.apache.bcel.generic.Type[] argTypes = 
	    org.apache.bcel.generic.Type.getArgumentTypes(signature);
	
	org.apache.bcel.generic.Type[] newArgTypes = 
	    new org.apache.bcel.generic.Type[argTypes.length+1];
	
	java.lang.System.arraycopy(argTypes, 0, newArgTypes, 0, argTypes.length);
	
	newArgTypes[newArgTypes.length-1] = 
	    org.apache.bcel.generic.Type.getType(newType);
	
	String newSig = org.apache.bcel.generic.Type.getMethodSignature(returnType, newArgTypes);
	return newSig;
    }

boolean sameStackFrame(
   sandmark.util.StackFrame s1,
   sandmark.util.StackFrame s2) {
   return s1.getFrameID() == s2.getFrameID();
}

} // class Build


