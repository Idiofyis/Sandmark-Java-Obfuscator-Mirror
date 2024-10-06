package sandmark.visualize.heap;

/**
 *  SQL.java -- 
 *  @author Jasvir Nagra <jas@cs.auckland.ac.nz>
 *  Created On      : Thu May 22 21:44:29 2003
 *  Last Modified   : <03/05/22 22:42:10 jas>
 *  Description     : Generate SQL from sandmark.util.newgraph.Graph
 *  Keywords        : sql sandmark
 *  PURPOSE
 *  	| Sandmark project |
 */

public class SQL {

    public static String toSql(sandmark.util.newgraph.Graph g) {
	return toString ( g );
    }
    
    public static void sqlInFile(sandmark.util.newgraph.Graph g,String filename){
        try{
            new java.io.PrintStream
		(new java.io.FileOutputStream
		 (filename)).println(toSql(g));
        }catch(java.io.IOException ioe){
            System.out.println("Error printing graph to SQL file " + filename + ":" + ioe);
        }
    }
    public static String toSql(sandmark.util.newgraph.MutableGraph g) { 
	return toSql(g.graph()); 
    }
    public static void sqlInFile
	(sandmark.util.newgraph.MutableGraph g,String f) { 
	sqlInFile(g.graph(),f);
    }

    public static String toString ( sandmark.util.newgraph.Graph graph ) {
	String result = "" + createNodesTable();
	for ( java.util.Iterator it = graph.nodes(); it.hasNext(); ) {
	    sandmark.util.newgraph.Node n = (sandmark.util.newgraph.Node)it.next();
	    result = result + toString(n);
	}
	result = result + "\n" + createEdgesTable();
	for ( java.util.Iterator it = graph.edges(); it.hasNext(); ) {
	    sandmark.util.newgraph.Edge e = (sandmark.util.newgraph.Edge)it.next();
	    result = result + toString(e);
	}
	return result + "\n";
    }

    public static String toString ( sandmark.util.newgraph.Node node ) {
	String result = "insert into RawNodes values ("
	    + node.nodeNumber() + ", "
	    + node.name() + ", "
// 	    + (node instanceof sandmark.util.exec.HeapNode 
// 	       ? ((sandmark.util.exec.HeapNode)node).timestamp()
// 	       : 0) 
            + 0
	    + ");\n";
	return result;
    }

    public static String toString ( sandmark.util.newgraph.Edge edge ) {
	String result = "insert into RawEdges values ("
	    + ((sandmark.util.newgraph.Node)edge.sourceNode()).nodeNumber() + ", "
	    + ((sandmark.util.newgraph.Node)edge.sinkNode()).nodeNumber() 
	    + ");\n";
	return result;
    }

    private static String createNodesTable () {
	return 
	    "create table RawNodes (" + 
	    " ID int unsigned," + 
	    " NAME varchar(255)," +
	    " TIME int unsigned);\n";
    }

    private static String createEdgesTable () {
	return 
	    "create table RawEdges (" + 
	    " ID int unsigned," + 
	    " SOURCE int unsigned," +
	    " SINK int unsigned);\n";
    }

}

//  Emacs bunkum
//  Local Variables:
//  mode: jde
//  time-stamp-start: "Last Modified[ \t]*:[ 	]+\\\\?[\"<]+"
//  time-stamp-end:   "\\\\?[\">]"
//  End:
