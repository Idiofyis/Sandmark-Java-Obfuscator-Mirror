package sandmark.watermark.ct.trace.callforest;

public class Decorate {

sandmark.analysis.classhierarchy.ClassHierarchy classHierarchy = null;
sandmark.newstatistics.Stats nstatistics = null;
sandmark.util.ConfigProperties props;
String annotatorClass;
sandmark.watermark.ct.encode.storage.StorageClass[] storageClasses;

java.util.Vector forest = null;

final static int EDGE_WEIGHT = 10;
final static int EDGE_CALL_WEIGHT = 50;

final static int ADD_PARAM_STEALTHY_WEIGHT  = 100;
final static int ADD_RETURN_STEALTHY_WEIGHT = 50;

final static int NODE_MARK_WEIGHT   = 200;
final static int NODE_STEALTHY_WEIGHT   = 100;
final static int NODE_UNSTEALTHY_WEIGHT = 0;
final static int NODE_JAVA_WEIGHT = -1000;
final static int NODE_CANTMODIFY_WEIGHT = -1000;

final static double TREE_ORDER_WEIGHT = 1.2;

public Decorate(
    java.util.Vector forest,
    sandmark.analysis.classhierarchy.ClassHierarchy classHierarchy,
    sandmark.newstatistics.Stats stats,
    sandmark.util.ConfigProperties props){
   this.forest = forest;
   this.classHierarchy = classHierarchy;
   this.nstatistics = stats;
   this.props = props;
   storageClasses = sandmark.watermark.ct.encode.storage.GlobalStorage.getStorageClasses();
}

/**
 * Add node and edge weights to the information flow graph.
 */
void decorate() {
   decorateNodes();
   decorateEdges();
}

/*******************************************************************/

/**
 * Add weights to all the nodes in all the call graphs.
 */
void decorateNodes() {
   long maxID = -1;
   long minID = 10000;
   java.util.Enumeration enum1 = forest.elements();
   while(enum1.hasMoreElements()) {
      sandmark.util.newgraph.MutableGraph graph =
	 (sandmark.util.newgraph.MutableGraph) enum1.nextElement();
      java.util.Iterator nodeEnum = graph.nodes();
      while (nodeEnum.hasNext()) {
         sandmark.watermark.ct.trace.callforest.Node node =
            (sandmark.watermark.ct.trace.callforest.Node) nodeEnum.next();
         long ID = node.frameID();
         maxID = java.lang.Math.max(maxID,ID);
         minID = java.lang.Math.min(minID,ID);
      }
   }

   java.util.Enumeration enum2 = forest.elements();
   while(enum2.hasMoreElements()) {
      sandmark.util.newgraph.MutableGraph graph = 
	 (sandmark.util.newgraph.MutableGraph) enum2.nextElement();
      java.util.Iterator nodeEnum = graph.nodes();
      while (nodeEnum.hasNext()) {
         sandmark.watermark.ct.trace.callforest.Node node =
            (sandmark.watermark.ct.trace.callforest.Node) nodeEnum.next();
         decorateNode(graph,node,minID,maxID);
      }
   }
}

/**
 * Construct the weight of this node.
 * A mark node gets the weight NODE_MARK_WEIGHT.
 * A node that calls a mark node represents a
 * method into which we may embed the watermark.
 * If the watermark code fits in, the node's
 * weight will be NODE_STEALTHY_WEIGHT * P,
 * where 0<=P<=1 is a measure of how stealthy
 * the method is.
 * @param graph  the call graph
 * @param node   the node
 */
void decorateNode(
   sandmark.util.newgraph.MutableGraph graph,
   sandmark.watermark.ct.trace.callforest.Node node,
   long minID,
   long maxID) {

   int weight = 0;
   if (node.isMarkNode())
      weight = NODE_MARK_WEIGHT;
   else {
      sandmark.util.MethodID method = node.getMethod();

      if (node.isCallNode()) {
         java.util.Iterator enum = graph.outEdges(node);
         while (enum.hasNext()) {
            sandmark.watermark.ct.trace.callforest.Edge edge =
               (sandmark.watermark.ct.trace.callforest.Edge) enum.next();
            sandmark.watermark.ct.trace.callforest.Node callee =
               (sandmark.watermark.ct.trace.callforest.Node) edge.sinkNode();
            if (callee.isMarkNode())
               weight += (int)(NODE_STEALTHY_WEIGHT*stealthyToHoldWatermark(method));
               //weight+=NODE_STEALTHY_WEIGHT;
         }
      }
   }
   long ID = node.frameID();
   double order = 1.0;
   if (minID != maxID)
      order = 1.0 + ((double)ID-(double)minID)/((double)maxID-(double)minID);
   weight *= order;
   //   weight = (int)((double)weight * java.lang.Math.pow((double)treeNumber,TREE_ORDER_WEIGHT));

   node.setWeight(weight);
}

/*************************************************************************/

/**
 * Return a number that is a measure of how stealthy it would be
 * to insert watermarking code in this method.
 *    @param method  the method
 *    @return a number between 0.0 and 1.0 where 1.0 is a perfect fit.
 * <p>
 * The bytecodes we insert have approximately this distribution:
 * <PRE>
 *     bytecode     percent
 *     --------------------
 *     aload*          29
 *     putfield        10
 *     astore*          8
 *     new              7
 *     invokespecial    7
 *     dup              7
 *     getstatic        6
 *     invokevirtual    6
 *     iconst*|ldc      5
 *     ifnull           3
 *     pop              2
 *     return           2
 *     getfield         2
 *     checkcast        1
 *     goto             1
 * </PRE>
 */
double stealthyToHoldWatermark(
   sandmark.util.MethodID method) {

   if (nstatistics == null) return 0.0;
   java.util.Hashtable usage = nstatistics.getByteCodeUsage(
      method.getClassName(), method.getName(), method.getSignature());
   if (usage == null) return 0.0;
   int numberOfBytecodes = nstatistics.getByteCode(
      method.getClassName(), method.getName(), method.getSignature()).size();
   if (numberOfBytecodes == 0) return 0.0;

   double S = 0.0;
   S += addStats(new String[]{"aload","aload_0","aload_1","aload_2","aload_3"},
                 numberOfBytecodes, 29.0, usage);
   S += addStats(new String[]{"putfield"}, numberOfBytecodes, 10.0, usage);
   S += addStats(new String[]{"astore","astore_0","astore_1","astore_2","astore_3"},
                 numberOfBytecodes, 8.0, usage);
   S += addStats(new String[]{"new"}, numberOfBytecodes, 7.0, usage);
   S += addStats(new String[]{"invokespecial"}, numberOfBytecodes, 7.0, usage);
   S += addStats(new String[]{"dup"}, numberOfBytecodes, 7.0, usage);
   S += addStats(new String[]{"getstatic"}, numberOfBytecodes, 6.0, usage);
   S += addStats(new String[]{"invokevirtual"}, numberOfBytecodes, 6.0, usage);
   S += addStats(new String[]{"iconst_0","iconst_1","iconst_2","iconst_3",
                              "iconst_4","iconst_5","ldc"},
                 numberOfBytecodes, 6.0, usage);
   S += addStats(new String[]{"ifnull"}, numberOfBytecodes, 3.0, usage);
   S += addStats(new String[]{"pop"}, numberOfBytecodes, 2.0, usage);
   S += addStats(new String[]{"return"}, numberOfBytecodes, 2.0, usage);
   S += addStats(new String[]{"getfield"}, numberOfBytecodes, 2.0, usage);
   S += addStats(new String[]{"checkcast"}, numberOfBytecodes, 1.0, usage);
   S += addStats(new String[]{"goto"}, numberOfBytecodes, 1.0, usage);

   S /= 15.0;

   return S;
}

/**
 * Compute the similarity of a set of bytecodes in a method
 * to the type of code we insert.
 * @param ops                a set of bytecodes
 * @param numberOfBytecodes  total number of bytecodes in this method
 * @param wm_percent         frequency of these bytecodes in the
 *                           code we insert
 * @param usage              bytecode counts for this method.
 * @return                   the ratio.
 * <p>
 * @return a number between 0.0 and 1.0 where 1.0 is a perfect fit.
 */
double addStats (
   String[] ops,
   int numberOfBytecodes,
   double wm_percent,
   java.util.Hashtable usage){

   int cnt=0;
   for(int i=0; i<ops.length; i++)
      if (usage.containsKey(ops[i]))
         cnt += ((java.lang.Integer)usage.get(ops[i])).intValue();

   double code_percent = 100.0*((double) cnt)/((double) numberOfBytecodes);
   double ratio = code_percent/wm_percent;
   if (ratio > 1.0)
      ratio = wm_percent/code_percent;
   return ratio;
}

/*************************************************************************/

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
         //            System.out.println(">>>>>>>>>> methodRenameOK(" + method + "," + newMethod + ")" );
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

/**
 * Return true if it would be stealthy to add a storage argument
 * to this method. The stealthiness of adding an argument
 * depends on
 * <UL>
 *   <LI> the number of arguments the method has,
 *   <LI> the types of these arguments.
 * </UL>
 * In general, adding yet another argument to a method that already
 * has several should be good. Also, since we're adding a reference
 * argument it's probably stealthy if the method already has one
 * or more reference parameters.
 * @param method  the method
 * @return a number between 0.0 and 1.0 describing how stealthy
 *         it would be to add another reference formal parameter.
 */
double stealthyToAddStorageArgument(
   sandmark.util.MethodID method) {
   org.apache.bcel.generic.Type returnType =
      org.apache.bcel.generic.Type.getReturnType(method.getSignature());
   org.apache.bcel.generic.Type[] argTypes =
      org.apache.bcel.generic.Type.getArgumentTypes(method.getSignature());

   int numberOfArgs = argTypes.length;

   int numberOfReferenceArgs = 0;
   for(int i=0; i<numberOfArgs; i++)
      if (argTypes[i] instanceof org.apache.bcel.generic.ReferenceType)
         numberOfReferenceArgs++;

   double changeInNumberOfArgs = 1.0;
   if (numberOfArgs>0)
      changeInNumberOfArgs = (double)numberOfArgs/((double)(numberOfArgs+1));

   double changeInNumberOfReferenceArgs = 1.0;
   if (numberOfReferenceArgs>0)
      changeInNumberOfReferenceArgs = (double)numberOfReferenceArgs/((double)(numberOfReferenceArgs+1));

   double stealth = 1.0 - (changeInNumberOfArgs + changeInNumberOfReferenceArgs)/2.0;

   return stealth;
}

/**
 * Return 1.0 if it would be stealthy to add a return
 * value to this method.
 * @param method  the method
 * @return a number between 0.0 and 1.0 describing how stealthy
 *         it would be to add a return value.
 */
double stealthyToAddReturnValue(
   sandmark.util.MethodID method) {
   org.apache.bcel.generic.Type returnType =
      org.apache.bcel.generic.Type.getReturnType(method.getSignature());

   if (returnType.equals(org.apache.bcel.generic.Type.VOID))
      return 1.0;
   else
      return 0.0;
}

/*******************************************************************/

/**
 * Add weights to all the edges in all the call graphs.
 */
void decorateEdges() {
   java.util.Enumeration enum = forest.elements();
   while(enum.hasMoreElements()) {
      sandmark.util.newgraph.MutableGraph graph = 
	 (sandmark.util.newgraph.MutableGraph) enum.nextElement();
      java.util.Iterator edgeEnum = graph.edges();
      while (edgeEnum.hasNext()) {
         sandmark.watermark.ct.trace.callforest.Edge edge =
            (sandmark.watermark.ct.trace.callforest.Edge) edgeEnum.next();
         decorateEdge(graph,edge);
      }
   }
}

/**
 * Construct the weight of this edge.
 * @param graph  the call graph
 * @param edge   the edge
 */
void decorateEdge(
   sandmark.util.newgraph.MutableGraph graph,
   sandmark.watermark.ct.trace.callforest.Edge edge) {

   sandmark.watermark.ct.trace.callforest.Node from =
      (sandmark.watermark.ct.trace.callforest.Node)edge.sourceNode();
   sandmark.watermark.ct.trace.callforest.Node to =
      (sandmark.watermark.ct.trace.callforest.Node)edge.sinkNode();
  sandmark.util.MethodID fromMethod = from.getMethod();
  sandmark.util.MethodID toMethod = to.getMethod();

   int weight = 0;
   if (from.isCallNode() && to.isEnterNode()) {
      if (to.isMarkNode())
         weight = 0;
      else {
         weight += ADD_PARAM_STEALTHY_WEIGHT * stealthyToAddStorageArgument(toMethod);
         weight += ADD_RETURN_STEALTHY_WEIGHT * stealthyToAddReturnValue(toMethod);
      }
   } else if (from.isEnterNode() && to.isExitNode())
      weight = EDGE_WEIGHT;
   else if (from.isEnterNode() && to.isCallNode())
      weight = EDGE_WEIGHT;
   else if (from.isReturnNode() && to.isExitNode())
      weight = EDGE_WEIGHT;
   else if (from.isReturnNode() && to.isCallNode())
      weight = EDGE_WEIGHT;
   else if (from.isExitNode() && to.isReturnNode())
      weight = EDGE_WEIGHT;

   edge.setWeight(weight);
}


} // class Decorate

