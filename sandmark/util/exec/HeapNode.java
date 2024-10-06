package sandmark.util.exec;

/**
 *  HeapNode.java -- 
 *  @author Jasvir Nagra <jas@cs.auckland.ac.nz>
 *  Created On      : Thu May 22 11:10:40 2003
 *  Last Modified   : <03/05/21 11:36:08 jas>
 *  Description     : HeapNode
 *  Keywords        : heap sandmark
 *  PURPOSE
 *  	| Sandmark project |
 */

public class HeapNode extends sandmark.util.newgraph.Node {

    protected sandmark.util.exec.HeapData data;

    public HeapNode ( sandmark.util.exec.HeapData data ) {
	setData ( data );
    }

    public void setData ( sandmark.util.exec.HeapData data ) {
	if ( data != null )
	    this.data = data;
    }

    public long timestamp() {
	return data.timestamp;
    }

}
