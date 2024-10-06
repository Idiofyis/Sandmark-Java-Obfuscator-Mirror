package sandmark.visualize.heap;

/**
 *  Heap.java -- 
 *  @author Jasvir Nagra <jas@cs.auckland.ac.nz>
 *  Created On      : Tue Apr 29 11:40:10 2003
 *  Last Modified   : <03/05/27 15:35:08 jas>
 *  Description     : Allows visualization of the heap
 *  Keywords        : heap visualization agave sandmark
 *  PURPOSE
 *  	| Sandmark project |
 */

// This is in flux - you probably shouldn't be using this. -- jas

public class HeapVisualizer extends sandmark.util.exec.Overseer {

    java.util.LinkedList buffer;
    
    class InitBreakpoint extends sandmark.util.exec.Breakpoint{
	public InitBreakpoint () {
	    super("<init>", sandmark.util.exec.Breakpoint.standardExclude);
	    
	}
	com.sun.jdi.ObjectReference root;
	int i;
	public void Action(sandmark.util.exec.MethodCallData data) {
	    buffer.add(data.getObject());
	    for ( java.util.Iterator i = buffer.iterator(); i.hasNext(); ) {
		System.out.println( "---8<---" );
		sandmark.util.exec.Heap.print(vm, (com.sun.jdi.ObjectReference)i.next());
		System.out.println( "--->8---" );
	    }
	    SQL.sqlInFile ( getHeap(),"/tmp/heap" + i++ + ".dot" );
	}
    }

    public HeapVisualizer(sandmark.watermark.DynamicRecognizeParameters params) {
	super(params.programCmdLine);
	buffer = new java.util.LinkedList();
	registerBreakpoint(new InitBreakpoint());
    }

    public sandmark.util.newgraph.Graph getHeap () {
	sandmark.util.exec.Heap.print(vm);
	return sandmark.watermark.ct.recognize.Heap2Graph.unpack(vm);
    }    
    
    public static void main ( String args[] ) throws Exception {
        sandmark.util.ConfigProperties p =
            sandmark.watermark.DynamicWatermarker.getProperties();
        p.setProperty("Class Path","sandmark.jar:/usr/local/j2sdk1.4.0/jre/lib/jce.jar:/usr/local/j2sdk1.4.0/jre/lib/rt.jar:/usr/local/j2sdk1.4.0/lib/tools.jar:/scratch/jas/smextern3/BCEL.jar:/scratch/jas/smextern3/bloat-1.0.jar:/scratch/jas/smextern3/grappa1_2.jar:/home/jas/src/sort/");
        p.setProperty("Main Class","Store");
        p.setProperty("Arguments","hello world");
        sandmark.watermark.DynamicRecognizeParameters drp = 
            sandmark.watermark.DynamicWatermarker.getRecognizeParams
            (new sandmark.program.Application());
        sandmark.visualize.heap.HeapVisualizer recognizer = 
            new sandmark.visualize.heap.HeapVisualizer(drp);
        recognizer.run();
	//	Thread.sleep ( 5000 );
	//recognizer.STOP();
    }

} // class HeapVisualizer


//  Emacs bunkum
//  Local Variables:
//  mode: jde
//  time-stamp-start: "Last Modified[ \t]*:[ 	]+\\\\?[\"<]+"
//  time-stamp-end:   "\\\\?[\">]"
//  End:

