package sandmark.watermark.ct.trace.callforest;

public class Forest {

sandmark.watermark.ct.trace.TracePoint annotationPoints[];
sandmark.analysis.classhierarchy.ClassHierarchy classHierarchy = null;
sandmark.newstatistics.Stats nstatistics = null;
sandmark.util.ConfigProperties props;

java.util.Vector forest = null;

/**
 * Construct a forest of call graphs from a set
 * of annotation points. 
 *
 * Each call graph is rooted in a 'unique entry point.'
 * A unique entry point is an application call frame generated
 * by Java runtime system. For example, there will be one
 * call graph rooted in the call frame for "main". There may
 * be other call graphs rooted in "action"-methods, for example
 * methods invoked by the run-time system in response to a
 * button click by the user.
 *
 * Consider the following program:
 * <PRE>
 *   1:      P() {
 *   2:          sm$mark();
 *   3:      }
 *   4:      
 *   5:      Q() {
 *   6:         sm$mark();
 *   7:         P();
 *   8:      }
 *   9:      
 *  10:      main() {
 *  11:         Q();
 *  12:         Q();
 *  13:      }
 * </PRE>
 * <P>
 * We get the call graph below. "Q:2/6" means
 * "call frame #2 for method Q, line number 6".
 * <PRE>
 *
 *                  mark:5                   mark:9
 *                   ^                          ^
 *                   |                          |
 *                   |                          |
 *                   V                          V
 *         mark:3   P:4/7            mark:7   P:8/7
 *          ^        ^                 ^        ^
 *          |        |                 |        |
 *          |        |                 |        |
 *          V        V                 V        V
 *        Q:2/6---->Q:2               Q:6/6---->Q:6
 *        ^                            ^
 *        |                            |
 *        |                            |
 *        V                            V
 *      main:1/11 ----------------> main:1/12
 *        ^
 *        |
 *       root
 * </PRE>
 *
 * The nodes of the graphs are stack frames. 
 * The root of each call  graph is the frame of 
 * whatever run() or main()  method started the thread. 
 * The leaves are stack frames of sm$mark() method calls. 
 * The edge Q:2/6---->Q:2 above indicates that there is
 * a way to pass information from mark:3 to mark:5, but
 * not the other way.
 * Each interior node is labeled by the stack frame 
 * (a sandmark.util.StackFrame object) it corresponds to.
 * Each leaf represents a sm$mark() call and is labeled
 * with the sm$mark(...) value given at that location.
 *
 *   @param annotationPoints the set of locations found
 *                           during tracing.
 *   @param classHierarchy   the class hierarchy
 *   @param stats            static statistics about the program
 *   @param props            properties
 */
public Forest(
    sandmark.watermark.ct.trace.TracePoint annotationPoints[],
    sandmark.analysis.classhierarchy.ClassHierarchy classHierarchy,
    sandmark.newstatistics.Stats stats,
    sandmark.util.ConfigProperties props){
   this.annotationPoints = annotationPoints;
   this.classHierarchy = classHierarchy;
   this.nstatistics = stats;
   this.props = props;

   construct();
}

/**
 * Build the information flow graph.
 */
void construct() {
   sandmark.watermark.ct.trace.callforest.Build b =
      new sandmark.watermark.ct.trace.callforest.Build(annotationPoints, classHierarchy, props);
   forest = b.build();
   sandmark.watermark.ct.trace.callforest.Decorate d =
      new sandmark.watermark.ct.trace.callforest.Decorate(forest, classHierarchy, nstatistics, props);
   d.decorate();
}



/**
 * Given two stack frames, returns an array of method ids
 * which require an additional parameter to pass information
 * from the first to the second.
 * @param from the source stack frame
 * @param to   the sink method
 */
public sandmark.util.MethodID[] getFlowPath (
   sandmark.util.StackFrame from,
   sandmark.util.MethodID to ) {
   return null;
}

/************************************************************************/
/*
 * Return a string representation of the forest.
 */
public String toString() {
    String S = "";
    java.util.Enumeration enum = forest.elements();
    while(enum.hasMoreElements()) {
       sandmark.util.newgraph.MutableGraph graph = 
	  (sandmark.util.newgraph.MutableGraph) enum.nextElement();
       S += graph.toString() + "\n";
    }
    return S;
}

/*
 * Return a dot representation of the forest.
 */
public String[] toDot() {
    String[] S = new String[forest.size()];
    java.util.Enumeration enum = forest.elements();
    int i = 0;
    while(enum.hasMoreElements()) {
       sandmark.util.newgraph.MutableGraph graph = 
	  (sandmark.util.newgraph.MutableGraph) enum.nextElement();
       S[i] = sandmark.util.newgraph.Graphs.toDot(graph) + "\n";
       i++;
    }
    return S;
}

/**
 * View a graphical representation of the forest
 */
public void addToGraphViewer() {
    String[] S = new String[forest.size()];
    java.util.Enumeration enum = forest.elements();
    int i = 0;
    while(enum.hasMoreElements()) {
       sandmark.util.newgraph.MutableGraph graph = 
	  (sandmark.util.newgraph.MutableGraph) enum.nextElement();
       sandmark.util.graph.graphview.GraphList.instance().add(graph, "Call Graph(" + i + ")");
       i++;
    }
}

/************************************************************************/
/**
 * Return a vector of call graphs.
 * Each node in the graph has a sandmark.util.StackFrame
 * as its data.
 */
public java.util.Vector getForest() {
   return forest;
}

/**
 * Return the number of graphs in the forest.
 */
public int size() {
    return forest.size();
}

/**
 * Return the i:th graph in the forest.
 */
public sandmark.util.newgraph.MutableGraph getCallGraph(int i) {
   return (sandmark.util.newgraph.MutableGraph)forest.get(i);
}


} // class Forest

