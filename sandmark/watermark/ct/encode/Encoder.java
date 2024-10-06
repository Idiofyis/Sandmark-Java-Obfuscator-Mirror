package sandmark.watermark.ct.encode;
/**
 *  Main class for the encoder. Takes a sandmark.util.newgraph.MutableGraph
 *  as input and returns a Java class ('Watermark.java') that
 *  builds this graph as output. 
*/

public class Encoder {

private sandmark.util.newgraph.MutableGraph graph;
private sandmark.util.newgraph.MutableGraph origGraph;
private sandmark.util.newgraph.MutableGraph[] subGraphs;
private sandmark.util.newgraph.MutableGraph componentGraph;
private sandmark.util.ConfigProperties props;
private sandmark.watermark.ct.encode.ir.Build ir;
private sandmark.util.javagen.Java code;
private java.util.Hashtable fieldsRequiringCasts;
 
/**
 * Called like this:
 * <PRE>
 *     sandmark.watermark.ct.encode.Encoder encoder = new sandmark.watermark.ct.encode.Encoder(graph,props);
 *     encoder.encode();
 *     String javaFile = encoder.getSource();
 *     org.apache.bcel.generic.ClassGen byteCode = encoder.getByteCode();
 *     try {
 *        String ClassName = props.getProperty("DWM_CT_Encode_ClassName");
 *        cg.getJavaClass().dump(ClassName + ".class");
 *     } catch (Exception e) {
 *     }
 * </PRE>
 * <P>
 *  The following properties should be defined:
 *  <BR> Node Class           - Name of the watermark class.
 *  <BR> DWM_CT_Encode_ParentClass        - What the watermark class should inherit from.
 *  <BR> DWM_CT_Encode_ClassName          - Name of the watermark class.
 *  <BR> DWM_CT_Encode_AvailableEdges     - Colon-separated list of field names that
 *                                     we can use to encode graph edges.
 *  <BR> Storage Policy          - Which nodes should we store in global
 *                                     variables? 'root' (only sub-graph roots)
 *                                     or 'all' (all nodes).
 *  <BR> Storage Method       - Colon-separated list of '{vector,array,pointer,hash}'
 *                                     describing where we may store global sub-graph pointers.
 *  <BR> Protection Method  - Colon-separated list of '{if,safe,try}' 
 *                                     describing what methods we may use to 
 *                                     protect against null pointers.
 *  <BR> Storage Location      - Where should we store sub-graph pointers?
 *                                     This property is either 'global' or
 *                                     'formal'. 'Global' means that we're storing
 *                                     pointers in some static global variables,
 *                                     'formal' means that these pointers get 
 *                                     passed around from method to method as
 *                                     arguments to these methods.
 *  <BR> DWM_CT_Encode_IndividualFixups   - "false"
 *  <BR> Graph Type           - "radix" or "perm"
 *  <BR> Subgraph Count         - Number of sub-graphs.
 *  <BR> Watermark          - Value to be encoded
 *  <BR> DWM_CT_Encode_Package            - Name of package in which the watermark class
 *                                     should be declared.
 */
public Encoder (
   sandmark.util.newgraph.MutableGraph graph,
   sandmark.util.ConfigProperties props,
   java.util.Hashtable fieldsRequiringCasts) {
   this.props = props;
   this.origGraph = graph;
   this.fieldsRequiringCasts = fieldsRequiringCasts;
}

/**
 * Performs the actual encoding, by
 * <OL>
 *   <LI> splitting the graph in Subgraph Count number of components
 *   <LI> converting the components to intermediate code
 *   <LI> converting the intermediate code to Java bytecode.
 * </OL>
 */
public void encode() throws Split.SplitException {
   graph = origGraph; //TamperProof.graph(origGraph, props);
   split();
   graph2IR(fieldsRequiringCasts);
   // System.out.println(ir.toString());
   IR2Java();
}

/**
 * Return a string which is the source code of a
 *  Watermark.java class.
 */
public String getSource() {
   String source = code.toString();
   return source;
}

/**
 * Return the bytecode of the Watermark.java class.
 */
public sandmark.program.Class getByteCode(sandmark.program.Application app) {
   return ((sandmark.util.javagen.Class)code).toCode(app);
}

/**
 * Return the watermark graph.
 */
public sandmark.util.newgraph.MutableGraph getGraph() {
   return graph;
}

/**
 * Return the watermark subgraphs.
 */
public sandmark.util.newgraph.MutableGraph[] getSubGraphs() {
   return subGraphs;
}

/**
 * Return the names of the Create_graphX methods, in the
 * order of which they should be called. We return an
 * array of MethodID objects.
 */
public sandmark.util.MethodID[] getCreateMethods() {
    java.util.LinkedList L = new java.util.LinkedList();
    java.util.Iterator iter = ir.creators.iterator();
    String className = props.getProperty("DWM_CT_Encode_ClassName");
    java.util.HashSet names = new java.util.HashSet();
    while (iter.hasNext()) {
       sandmark.watermark.ct.encode.ir.Create f = (sandmark.watermark.ct.encode.ir.Create) iter.next();
       sandmark.util.MethodID S = new sandmark.util.MethodID(f.name(),f.signature(),className);
       if(names.contains(S))
	   throw new Error("Duplicate method name!");
       names.add(S);
       L.add(S);
    }
    sandmark.util.MethodID[] S = new sandmark.util.MethodID[L.size()];
    return (sandmark.util.MethodID[]) L.toArray(S);
}

/**
 * Return the names and types of the CreateStorage_X methods.
 * We're returning an array of String quadrupes
 * <PRE>
 *         (methodName,returnType,localName, GLOBAL/LOCAL).
 * </PRE>
 * methodName is the name of the method to call, returnType is 
 * the name of the storage type ("java.util.Vector") it will return,
 * localName is the name of the local variable into which
 * the return value should be stored.
 */
public String[][] getCreateStorageMethods() {
    java.util.LinkedList L = new java.util.LinkedList();
    java.util.Iterator iter = ir.storageCreators.iterator();
    while (iter.hasNext()) {
       sandmark.watermark.ct.encode.ir.CreateStorage f = (sandmark.watermark.ct.encode.ir.CreateStorage) iter.next();
       String global = (f.isGlobal())?"GLOBAL":"FORMAL";
       String S[] = {f.name(),f.type(),f.varName(), global};
       L.add(S);
    }
    String[][] S = new String[L.size()][4];
    return (String[][]) L.toArray(S);
}


/**
 * Split the graph into Subgraph Count number of subgraphs.
 * 'componentGraph' encodes the dependencies between these
 * subgraphs.
 */
private void split() throws Split.SplitException {
   int components = java.lang.Integer.parseInt(props.getProperty("Subgraph Count"));
   sandmark.watermark.ct.encode.Split split = new sandmark.watermark.ct.encode.Split(graph, components);
   split.split();
   subGraphs = split.subGraphs;
   componentGraph = split.componentGraph;
}

void dumpIR(String header) {
    if (props.getProperty("Dump Intermediate Code").equals("true")) 
	System.out.println("<<<" + header + ">>>\n" + ir.toString());
}

/**
 * Convert the graph to intermediate code. This is done in several steps:
 * <OL>
 *    <LI> generate 'stright-forward' intermediate code,
 *    <LI> add code to save component root nodes in some global structure,
 *    <LI> add formal parameters, if necessary,
 *    <LI> add class fields to encode outgoing graph edges,
 *    <LI> inline fixup routines (methods that contain code that
 *         add edges between graph components),
 *    <LI> clean up the code by removing redundancies,
 *    <LI> construct "destructors" (methods that destroy the
 *         watermark graph),
 *    <LI> clean up the code by removing redundancies,
 *    <LI> add exception handlers and the like to protect
 *         against null pointer exceptions,
 *    <LI> add debugging code that prints out a run-time trace,
 *    <LI> build testing methods that call the component
 *         methods.
 * </OL>
 */
private void graph2IR (java.util.Hashtable fieldsRequiringCasts) {
   sandmark.watermark.ct.encode.storage.GlobalStorage storage = 
      new sandmark.watermark.ct.encode.storage.GlobalStorage (graph,subGraphs,props);

   ir = sandmark.watermark.ct.encode.Graph2IR.gen(graph, subGraphs, componentGraph, storage);

   dumpIR("Original IR code");
   sandmark.watermark.ct.encode.ir2ir.Transformer s = new 
      sandmark.watermark.ct.encode.ir2ir.SaveNodes(ir, storage, props);
   ir = s.mutate();

   dumpIR("IR code after 'SaveNodes'");

   sandmark.watermark.ct.encode.ir2ir.Transformer i = new 
      sandmark.watermark.ct.encode.ir2ir.AddFields(ir, props);
   ir = i.mutate();

   dumpIR("IR code after 'AddFields'");

   sandmark.watermark.ct.encode.ir2ir.Transformer t = new 
      sandmark.watermark.ct.encode.ir2ir.InlineFixups(ir, props);
   ir = t.mutate();

   dumpIR("IR code after 'InlineFixups'");

   sandmark.watermark.ct.encode.ir2ir.Transformer c1 = new 
      sandmark.watermark.ct.encode.ir2ir.CleanUp(ir, props);
   ir = c1.mutate();

   dumpIR("IR code after 'CleanUp'");

   sandmark.watermark.ct.encode.ir2ir.Transformer f = new 
      sandmark.watermark.ct.encode.ir2ir.AddFormals(ir, props, getCreateStorageMethods());
   ir = f.mutate();

   dumpIR("IR code after 'AddFormals'");
   
   sandmark.watermark.ct.encode.ir2ir.Transformer c2 = new 
      sandmark.watermark.ct.encode.ir2ir.CleanUp(ir, props);
   ir = c2.mutate();

   dumpIR("IR code after 'CleanUp'");

   sandmark.watermark.ct.encode.ir2ir.Transformer p = new 
      sandmark.watermark.ct.encode.ir2ir.Protect(ir, props);
   ir = p.mutate();

   dumpIR("IR code after 'Protect'");

   if(props.getProperty("Debug").equals("true")) {
      sandmark.watermark.ct.encode.ir2ir.Transformer debug = new 
         sandmark.watermark.ct.encode.ir2ir.Debug(ir, props);
      ir = debug.mutate();
   }
   dumpIR("IR code after 'Debug'");

   sandmark.watermark.ct.encode.ir2ir.Transformer b = new 
      sandmark.watermark.ct.encode.ir2ir.Builder(ir, props);
   ir = b.mutate();

   dumpIR("IR code after 'Builder'");

   sandmark.watermark.ct.encode.ir2ir.Transformer a = new 
      sandmark.watermark.ct.encode.ir2ir.AddCasts(ir, props, 
						  fieldsRequiringCasts);
   ir = a.mutate();

   dumpIR("IR code after 'Builder'");

}

/**
 * Convert the intermediate code to Java.
 */
    private void IR2Java () {
   code = ir.toJava(props);
   //System.out.println(code);
}

}


