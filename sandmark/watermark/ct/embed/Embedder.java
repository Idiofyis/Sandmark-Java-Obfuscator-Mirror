package sandmark.watermark.ct.embed;

/**
 *  The sandmark.watermark.ct.embed.Embedder class contains methods for
 *  adding watermarking code to Java bytecode.
 */

public class Embedder {
sandmark.watermark.DynamicEmbedParameters params;
sandmark.util.ConfigProperties props;
sandmark.program.Application app;
sandmark.watermark.ct.encode.Encoder encoder;
sandmark.watermark.ct.trace.callforest.Forest callForest;
sandmark.newstatistics.Stats nstatistics;
sandmark.watermark.ct.embed.Distribute dt;
sandmark.watermark.ct.embed.ReplaceWMClass rep;
 sandmark.util.newgraph.MutableGraph graph;

/**
 *
 *  @param props         global property list
 *  @param traceData     a vector of annotation points that
 *                       were hit during tracing
 *
 *  <P> Reads properties:
 *  <BR> Input File:		name of Jar file to input
 *  <BR> Output File:		name of Jar file to output
 *  <BR> Encode_....:	                various options affecting the graph encoder
 *
 *  <P> Sets properties:
 *  none
 *  <P>
 *  We add watermarking code to the program by:
 * <OL>
 *    <LI> opening the program as a class file collection,
 *    <LI> computing static information about the program,
 *    <LI> building the call forest from the tracing
 *         points found during tracing,
 *    <LI> construct the graph whose topology embeds the watermark, and
 *    <LI> convert the graph to java bytecode which constructs the graph.
 * </OL>
 **/
public Embedder (sandmark.program.Application app,
   sandmark.watermark.DynamicEmbedParameters params,
   sandmark.util.ConfigProperties props,
   sandmark.watermark.ct.trace.TracePoint[] traceData) throws Exception {
    this.props = props;

    this.app = app;
    this.nstatistics = app.getStatistics();

    java.util.Hashtable fieldsRequiringCasts;
    if(props.getProperty("Replace Watermark Class").equals("true"))
	{   rep=new sandmark.watermark.ct.embed.ReplaceWMClass(app,props);
	    fieldsRequiringCasts = rep.findReplaceClass();

	}
	else {
		props.setProperty("Node Class",props.getProperty("DWM_CT_Encode_ClassName"));
		props.setProperty("DWM_CT_Encode_AvailableEdges","");
		fieldsRequiringCasts = new java.util.Hashtable();
		}

    // System.out.println("Node Class: " + props.getProperty("Node Class"));


    this.graph = constructGraph(params.watermark,props);
    encoder = new sandmark.watermark.ct.encode.Encoder(graph,props,fieldsRequiringCasts);
    encoder.encode();
    sandmark.util.MethodID[] creators = encoder.getCreateMethods();

	dt=new sandmark.watermark.ct.embed.Distribute(props,traceData,app,creators);
	this.callForest=dt.getCallForest();
}



/**
 * Add calls to the graph-building routines to the
 * program to be watermarked.
 * <OL>
 *   <LI> Get the bytecode that encodes the watermark,
 *   <LI> find the trace locations in the program where
 *        calls to the watermark-building code should be
 *        inserted,
 *   <LI> embed the calls at the appropriate locations, and
 *   <LI> save the resulting new, watermarked, program.
 * </OL>
 **/
public void saveByteCode() throws Exception {
    sandmark.program.Class cls = encoder.getByteCode(app);
    sandmark.watermark.ct.embed.EmbedData[] embedData = dt.findEmbedding();
    sandmark.util.MethodID[] methods = dt.allMethods();
    embedClass(cls, embedData, methods);
}

/**
 * Compute the locations where calls to the watermarking routines
 * should be inserted.
 **/
public sandmark.watermark.ct.embed.EmbedData[] getEmbedding() throws Exception {
    sandmark.program.Class cls = encoder.getByteCode(app);
    return dt.findEmbedding();
}

/**
 * Get the Java source for the watermark class.
 **/
public String source() {
     return encoder.getSource();
}

/**
 * Write the Java source for the watermark class.
 * <P>
 * @param sourceFileName The name of the file to be written.
 **/
public void saveSource (
    String sourceFileName) throws Exception {
    sandmark.util.Misc.writeToFile(sourceFileName, encoder.getSource());
}

/**
 * Write a dot file for the watermark class.
 * <P>
 * @param dotFileName The name of the file to be written.
 **/
public void saveGraph (
    String dotFileName) throws Exception {
    String S = sandmark.util.newgraph.Graphs.toDot(encoder.getGraph());
    sandmark.util.newgraph.MutableGraph[] subGraphs = encoder.getSubGraphs();
    for(int i=0; i<subGraphs.length; i++)
	S += "\n" + sandmark.util.newgraph.Graphs.toDot(subGraphs[i]);
    sandmark.util.Misc.writeToFile(dotFileName, S);
}

/**
 * Write the dot files for the call forest graphs.
 * <P>
 * @param dotFileBaseName The base name (without the .dot extension)
 *                        of the files to be written.
 **/
public void saveCallForest (
    String dotFileBaseName) throws Exception {
    String[] S = callForest.toDot();
    for(int i=0; i<S.length; i++)
       sandmark.util.Misc.writeToFile(dotFileBaseName + i + ".dot", S[i]);
}

 
/**
 * Add all graphs to the graph viewer.
 **/
public void  addToGraphViewer(){
  callForest.addToGraphViewer();
   sandmark.util.graph.graphview.GraphList.instance().add(graph, "Watermark graph");
   sandmark.util.newgraph.MutableGraph[] subgraphs = encoder.getSubGraphs();
   for(int i=0; i<subgraphs.length; i++)
      sandmark.util.graph.graphview.GraphList.instance().add(subgraphs[i], "Watermark subgraph("+i+")");
}

/**
 * Add the watermark class to the app, and embed calls to it
 * based on the embedding information in 'embedData'. There are
 * several steps:
 * <OL>
 *   <LI> Replace the 'mark()'-calls in the original program
 *        with calls to the watermark-building methods. Only
 *        some 'mark()'-calls will be replaced, namely those
 *        indicated by 'embedData'.
 *   <LI> In case we are passing graph handles in extra
 *        method arguments we need to create those formal arguments
 *        and add the extra actual arguments.
 *   <LI> We need to create storage (arrays, vectors, hashtables, etc)
 *        to store the handles to watermark graph components.
 *   <LI> We delete all remaining 'mark()'-calls.
 *   <LI> Finally, we add the watermark class to class file collection.
 * </OL>
 *
 * <p>
 *    @param app            The application to be watermarked.
 *    @param watermarkClass The class that builds the watermark graph.
 *    @param embedData      Where in the app calls to watermarkClass
 *                          should be embedded.
 *    @param methods        The methods to which formal parameters should
 *                          be added to pass around storage containers.
 */
void embedClass(
   sandmark.program.Class watermarkClass,
   sandmark.watermark.ct.embed.EmbedData[] embedData,
   sandmark.util.MethodID[] methods) throws Exception {

    String[][] storageCreators = encoder.getCreateStorageMethods();

    sandmark.watermark.ct.embed.ReplaceMarkCalls rc =
       new sandmark.watermark.ct.embed.ReplaceMarkCalls(app, props, embedData);
    rc.insert();


/* Pass the node where the calls to storageCreators
should be embedded*/
    sandmark.watermark.ct.embed.InsertStorageCreators is =
         new sandmark.watermark.ct.embed.InsertStorageCreators(
             app, props, storageCreators,dt.getStorageNode());
    is.insert();

       sandmark.watermark.ct.embed.AddParameters ap =
          new sandmark.watermark.ct.embed.AddParameters(app, props, storageCreators,
          							methods,dt.getStorageNode());
			ap.add();

		if(props.getProperty("Inline Code").equals("true"))
		   	sandmark.watermark.ct.embed.Inliner.doInline(app,props);

		if(props.getProperty("Replace Watermark Class").equals("true"))
			{   	rep.doReplace();}


    sandmark.watermark.ct.embed.DeleteMarkCalls dmc =
       new sandmark.watermark.ct.embed.DeleteMarkCalls(app,props);
    dmc.delete();
}


/*************************************************************************/
/**
 *  Construct and return a graph embedding the value
 *  Watermark using encoding Graph Type.
 * <p>
 *    @param props The property collection.
 * <p>
 * props contains at least:
 *    <BR> Watermark:  The integer to be encoded.
 *    <BR> Graph Type:   "radix", "perm", ..., or "*".
                               With "*" a random codec is selected.
 *    <BR> Subgraph Count: the number of graph components to construct
 */
public static sandmark.util.newgraph.MutableGraph constructGraph(
       String watermarkString,sandmark.util.ConfigProperties props) throws Exception{
    try{
    java.math.BigInteger value = props.getProperty("Numeric Watermark").equals
	("true") ? new java.math.BigInteger(watermarkString) : 
	sandmark.util.StringInt.encode(watermarkString);

    String name = props.getProperty("Graph Type");
    java.lang.Class codecClass = null;
    if (name.equals("*")) {
       Class wrapperCodec = sandmark.util.newgraph.codec.WrapperCodec.class;
       java.util.ArrayList codecNames = new java.util.ArrayList
	  (sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	   (sandmark.util.classloading.IClassFinder.GRAPH_CODEC));
       java.util.HashSet badNdxs = new java.util.HashSet();
       while(codecClass == null && badNdxs.size() != codecNames.size()) {
	  int ndx;
	  while(true) {
	     ndx = sandmark.util.Random.getRandom().nextInt() % 
		codecNames.size();
	     if(ndx < 0)
		ndx += codecNames.size();
	     if(!badNdxs.contains(new Integer(ndx)))
		break;
	  }
	  String codecName = (String)codecNames.get(ndx);
	  try { codecClass = Class.forName(codecName); }
	  catch(Exception e) {}
	  if(codecClass != null && wrapperCodec.isAssignableFrom(codecClass))
	     codecClass = null;
	  if(codecClass == null)
	     badNdxs.add(new Integer(ndx));
       }
       if(codecClass == null)
	  throw new sandmark.watermark.WatermarkingException
	     ("Can't find graph codec randomly");
    } else {
       try {
	  codecClass = Class.forName(name);
       } catch(Exception e) {
	  try {
	     codecClass = Class.forName("sandmark.util.newgraph.codec." + name);
	  } catch(Exception e2) {
	     throw new sandmark.watermark.WatermarkingException
		("Unknown graph codec " + name);
	  }
       }
       if(sandmark.util.newgraph.codec.WrapperCodec.class.isAssignableFrom
	  (codecClass))
	  throw new sandmark.watermark.WatermarkingException
	     ("Graph codec " + name + " must wrap another codec");
    }
    
    java.lang.Class[] consArgs = {};
    java.lang.reflect.Constructor cons = codecClass.getConstructor(consArgs);
    java.lang.Object[] args = {};
    sandmark.util.newgraph.codec.GraphCodec codec = 
       (sandmark.util.newgraph.codec.GraphCodec) cons.newInstance(args);
    if (props.getProperty("Use Cycle Graph").equals("true"))
       codec = new sandmark.util.newgraph.codec.CycleAndPathWrapper(codec);
    
    sandmark.util.Log.message(0,"Using " + codecClass.getName() + " graph codec" +
			      " wrapped with " + codec.getClass().getName());
    String s = props.getProperty("DWM_CT_Encode_AvailableEdges");
    sandmark.util.newgraph.NodeFactory f = new sandmark.util.newgraph.NodeFactory() {
	  private int num = 0;

	  public synchronized java.lang.Object createNode() {
	     return new sandmark.util.newgraph.Node(num++);
	  }
       };
       
       sandmark.util.newgraph.Graph rawGraph = codec.encode(value,f);
    sandmark.util.newgraph.Graph labeledGraph =
       sandmark.util.newgraph.Graphs.labelEdges(rawGraph,
						availableEdges(s,rawGraph.maxOutDegree()));
    return new sandmark.util.newgraph.MutableGraph(labeledGraph);
    } catch (Exception e){
       //	System.out.println("constructGraph " + e + " : " + e.getMessage());
	throw e;
    }  
}

public static String[] availableEdges(String available,int needed) {
    java.util.StringTokenizer S = new java.util.StringTokenizer(available,":");
    String[] res = new String[needed];
    java.util.HashSet fields = new java.util.HashSet();
    int i;
    for(i = 0 ; i < res.length && S.hasMoreTokens() ; i++)
        res[i] = S.nextToken();
    for( int nextSalt = 0 ; i < res.length ; i++) {
       for( ; fields.contains("x$" + nextSalt) ; nextSalt++)
          ;
       res[i] = "x$" + nextSalt++;
    }
    return res;
}


} // class Embedder










