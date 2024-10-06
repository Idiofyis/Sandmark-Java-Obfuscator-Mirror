package sandmark.watermark.ct.recognize;

public class RecognizeData {

/**
 *  The encoding (radix, permutation-graph,...) that the
 *  watermark was encoded with.
 */
   private sandmark.util.newgraph.codec.GraphCodec codec;

/**
 * The graph that was found on the heap.
 */
   private sandmark.util.newgraph.Graph graph;

/**
 *  The decoded watermark.
 */
   private java.math.BigInteger wm;

/**
 *  The watermark, decoded as a string.
 */
   private String wm_String;

/**
 *  The permutation of the children of a graph node.
 */
//    public int kidMap[];

/**
 *  The 'pointer' (uniqueID) of the root in the heap graph.
 */
    private Long root;
    
    private static int graphCounter;
    private int graphNum = graphCounter++;

/**
 *  RecognizeData-objects are returned by the Recognizer.
 *  They contain information about the watermarks
 *  that have been found. Fields are public and should 
 *  be treated as read-only.
 */

  public RecognizeData(
     sandmark.util.newgraph.codec.GraphCodec codec,
     sandmark.util.newgraph.Graph graph,
     java.math.BigInteger wm,
     String wm_String,
     Long root) {
     this.codec = codec;
     this.wm = wm;
     this.wm_String = wm_String;
     this.graph = graph;
     this.root = root;
     String S = codec.toString() + " decoding of the heap graph\n";
     S += graph;
     S += "with root=" + root + "\n";
     S += "yields the watermark '" + wm.toString() + "' (\"" + wm_String + "\").\n";
     sandmark.util.Log.message(0,S);
  }

    private String formatSet (int[] set) {
	String S = "{";
        for(int i=0; i<set.length; i++) {
	    if (i>0) S += ",";
            S += set[i];
	}
	S += "}";
	return S;
    }

/**
 *  Format a RecognizeData-object.
 */
  public String toString() {
      return wm_String;
  }

/**
 * Write the dot file for the watermark class.
 **/
  public void saveGraph () {
     sandmark.util.newgraph.Graphs.dotInFile
        (graph,"RecognizedWM_" + graphNum + ".dot");
}

  public String toShortString() {
    return "'" + wm.toString() + "' (\"" + wm_String + "\") [" + codec + "]";
  }

/**
 * Add all graphs to the graph viewer.
 **/
public void  addToGraphViewer(){
   sandmark.util.graph.graphview.GraphList.instance().add
      (graph, "Extracted graph(" + graphNum +")");
}
}

