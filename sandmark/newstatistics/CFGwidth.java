package sandmark.newstatistics;

/** Calculates the width of a method CFG i.e. how far can the execution path digress from 
 *  the simplest sequential execution.
 */

public class CFGwidth {

    private static boolean BUG = true;
    private static boolean DEBUG = false;

    private sandmark.analysis.controlflowgraph.MethodCFG mcfg = null;
    private sandmark.program.Method meth = null;

    private int finalWidth;

    /* traversed in dfs sequence; */
    private java.util.Stack edgeStack = new java.util.Stack(); 
    private java.util.Stack rangeStack = new java.util.Stack();
    
    public CFGwidth(sandmark.program.Method mObj)
    {
        meth = mObj;
        mcfg = new sandmark.analysis.controlflowgraph.MethodCFG(meth,false);
    }

    private boolean isBackEdge(sandmark.util.newgraph.EdgeImpl edge, 
                               java.util.ArrayList backedges)
    {
        sandmark.analysis.controlflowgraph.BasicBlock src = 
            (sandmark.analysis.controlflowgraph.BasicBlock)edge.sourceNode();
        sandmark.analysis.controlflowgraph.BasicBlock dest = 
            (sandmark.analysis.controlflowgraph.BasicBlock)edge.sinkNode();
        for(int k=0; k<backedges.size(); k+=2)
            if(src.equals((sandmark.analysis.controlflowgraph.BasicBlock)backedges.get(k)) &&
               dest.equals((sandmark.analysis.controlflowgraph.BasicBlock)backedges.get(k+1)))
                return true;
        return false;
    }

    private boolean isEndOfRange(sandmark.analysis.controlflowgraph.BasicBlock tempblk,
								 Range currRange)
    {
		if(currRange==null) {
			if(DEBUG)System.out.println("currRange is null");
			return false;
		}
		if(false) {
		if(DEBUG)System.out.println("firstIH -> "+tempblk.getIH());
		if(DEBUG)System.out.println("lastIH -> "+tempblk.getLastInstruction());
		if(DEBUG)System.out.println("firstIHR -> "+currRange.eblk.getIH());
		if(DEBUG)System.out.println("lastIHR -> "+currRange.eblk.getLastInstruction());
		}
		
        if(tempblk.equals(currRange.eblk))
            return true;
        return false;
    }

    private boolean edgeListEmpty(Range currRange)
    {
		if(DEBUG)System.out.println("edgelist.size()-> "+currRange.edgelist.size());
        if(currRange.edgelist.size()==0)
            return true;
        return false;
    }

    public void evaluate()
    {
        int currwidth=0;
        int maxwidth=0;
        java.util.ArrayList backedges = mcfg.getBackedges();

		
		if(DEBUG) {
			if(backedges==null)
				System.out.println("\n\n #### back edges = 0");
			else
				System.out.println("\n\n #### back edges = "+backedges.size());
		}

        sandmark.analysis.controlflowgraph.BasicBlock fromblk = mcfg.source();
		if(DEBUG)System.out.println("firstIH -> "+fromblk.getIH());
		//if(DEBUG)System.out.println("lastIH -> "+fromblk.getLastInstruction());
        java.util.Iterator edgeIter = mcfg.outEdges(fromblk);
        int succCount=0;
        while(edgeIter.hasNext()) {
            succCount++;
            edgeStack.push((sandmark.util.newgraph.EdgeImpl)edgeIter.next());
        }
        /* traverse ahead until branch node detected; */
		if(DEBUG)System.out.println("succCount ="+succCount);
        while(succCount==1) {
			if(DEBUG)System.out.println("in here");
            succCount=0;
            sandmark.util.newgraph.EdgeImpl tempedge = 
                (sandmark.util.newgraph.EdgeImpl)edgeStack.pop();
            fromblk=(sandmark.analysis.controlflowgraph.BasicBlock)tempedge.sinkNode();
            edgeIter = mcfg.outEdges(fromblk);
            while(edgeIter.hasNext()) {
                edgeStack.push((sandmark.util.newgraph.EdgeImpl)edgeIter.next());
                succCount++;
            }
        }
        if(succCount==0) {
            if(!fromblk.equals(mcfg.sink()))
                if(BUG)System.out.println("BUG: Search ended in non-sink node ");
            return;
        }
		maxwidth=1;
		currwidth=1;

		if(DEBUG)System.out.println("succCount ="+succCount);
        Range currRange = 
            new Range(currwidth, fromblk, mcfg.getPostDominator(fromblk), mcfg.outEdges(fromblk));
		if(DEBUG)System.out.println(" CREATING NEW RANGE OBJECT ");
        rangeStack.push(currRange);
		if(DEBUG&&false) {		
			System.out.println("firstIHR -> "+ fromblk);
			System.out.println("firstIHR -> "+ mcfg.getPostDominator(fromblk));
			System.out.println("firstIHR -> "+currRange.eblk.getIH());
			System.out.println("lastIHR -> "+currRange.eblk.getLastInstruction());
		}
        Range dummyRange = null;

		int term=0;
        while(!edgeStack.empty()) {
			if(DEBUG)System.out.println("\n\n in loop");

            /* pop the top element from stack; */
            sandmark.util.newgraph.EdgeImpl tempedge = 
                (sandmark.util.newgraph.EdgeImpl)edgeStack.pop();

            /* delete this edge from range object list at top of stack,if present.
             * IMP: currwidth to range object width! */
			currRange = (Range)rangeStack.peek();
            for(int m=0; m<currRange.edgelist.size(); m++)
                if(tempedge.equals(currRange.edgelist.get(m))) {
					if(DEBUG)System.out.println(" deleting edge from rangeobj list ");
                    currRange.edgelist.remove(m);
                    currwidth = currRange.getWidth();
					/* also pop and push this rangeObject into stack */
					dummyRange = (Range)rangeStack.pop();
					if(DEBUG)System.out.println(" UPDATING RANGE OBJECT ");
					rangeStack.push(currRange);
					if(DEBUG) {		
						System.out.println("firstIHR -> "+currRange.eblk.getIH());
						System.out.println("lastIHR -> "+currRange.eblk.getLastInstruction());
					}
						
                    break;
                }

            /* check if this is back edge; then discard; continue; */
            if(this.isBackEdge(tempedge, backedges)) {
				if(DEBUG)System.out.println(" -> is a back edge");
                continue;
			}
			else 
				if(DEBUG)System.out.println(" -> not a back edge");

            fromblk = (sandmark.analysis.controlflowgraph.BasicBlock)tempedge.sinkNode();
			if(DEBUG)System.out.println("fromIH ->> "+fromblk.getIH());
			if(DEBUG)System.out.println("fromtoIH ->> "+fromblk.getLastInstruction());
            
            /*  
             *  if(this block has reached the end of the innermost range)
             *     if(no edges left to be traversed at head of this range)
             *         pop out that range from stack;
             *         Update 'currwidth';
             *
             *     Take next path (from edgeStack);
             */
			boolean proceedflag=false;
            if(this.isEndOfRange(fromblk, currRange)) {
				if(DEBUG)System.out.println("1. End of Range ...");
                if(this.edgeListEmpty(currRange)) {
                    dummyRange = (Range)rangeStack.pop();
					if(DEBUG)System.out.println(" DELETING RANGE OBJECT ");
                    if(!rangeStack.empty()) {
                        currRange = (Range)rangeStack.peek();
                        currwidth = currRange.getWidth();
                    }
                    else {
                        currRange = null;
                        currwidth = 0;
                    }
					/* also. push the outgoing edges from end(oldrange) into stack */ 
					if(currRange!=null)
						if((dummyRange.eblk).equals(currRange.eblk))
							continue;
					fromblk = dummyRange.eblk;
					proceedflag=true;
                }
				if(!proceedflag)
                	continue;
            }

            edgeIter = mcfg.outEdges(fromblk);
            succCount=0;
            while(edgeIter.hasNext()) {
                edgeStack.push(edgeIter.next());
				if(DEBUG)System.out.println("1.pushing edge");
                succCount++;
            }

            while(succCount==1) {
				if(DEBUG)System.out.println("loopstart---");
                succCount=0;

				if(edgeStack.isEmpty()) {
					if(currRange!=null)
						if((dummyRange.eblk).equals(currRange.eblk)) {
							if(DEBUG)System.out.println("break1");
							break; // with succCount value as 0; so continues at top
						}
					fromblk = dummyRange.eblk;
            		edgeIter = mcfg.outEdges(fromblk);
                	while(edgeIter.hasNext()) {
                    	edgeStack.push((sandmark.util.newgraph.EdgeImpl)edgeIter.next());
						if(DEBUG)System.out.println("4.pushing edge");
                	}
					if(DEBUG)System.out.println("break1");
                   	break; // with succCount value as 0; so continues at top
				}

                tempedge = (sandmark.util.newgraph.EdgeImpl)edgeStack.pop();
				if(this.isBackEdge(tempedge, backedges)) {
					if(DEBUG)System.out.println(" -> is a back edge");
					continue;
				}
				
				if(!rangeStack.isEmpty()) {
					Range dRange;
					currRange = (Range)rangeStack.peek();
            		for(int m=0; m<currRange.edgelist.size(); m++)
                		if(tempedge.equals(currRange.edgelist.get(m))) {
							if(DEBUG)System.out.println(" deleting edge from rangeobj list ");
                    		currRange.edgelist.remove(m);
                    		currwidth = currRange.getWidth();
							/* also pop and push this rangeObject into stack */
							dRange = (Range)rangeStack.pop();
							if(DEBUG)System.out.println(" UPDATING RANGE OBJECT ");
							rangeStack.push(currRange);
                    		break;
                		}
				}

                fromblk=(sandmark.analysis.controlflowgraph.BasicBlock)tempedge.sinkNode();
                if(this.isEndOfRange(fromblk, currRange)) {
					if(DEBUG)System.out.println("2. End of Range ...");
                    if(this.edgeListEmpty(currRange)) {
                        dummyRange = (Range)rangeStack.pop();
						if(DEBUG)System.out.println(" DELETING RANGE OBJECT ");
                        if(!rangeStack.empty()) {
                            currRange = (Range)rangeStack.peek();
                            currwidth = currRange.getWidth();
                        }
                        else { 
                            currRange = null;
                            currwidth = 0;
                        }
						/* also. push the outgoing edges from end(oldrange) into stack */ 
						boolean contFlag=false;
						while(currRange!=null) {
							if((dummyRange.eblk).equals(currRange.eblk))
								if(!edgeStack.isEmpty()) {
									contFlag=true;
									break; 
								}
								dummyRange=(Range)rangeStack.pop();
                        		if(!rangeStack.empty()) {
                            		currRange = (Range)rangeStack.peek();
                            		currwidth = currRange.getWidth();
                        		}
                        		else { 
                            		currRange = null;
                            		currwidth = 0;
                        		}
						}
						if(contFlag)
							continue;
						fromblk = dummyRange.eblk;
            			edgeIter = mcfg.outEdges(fromblk);
            			while(edgeIter.hasNext()) {
                			edgeStack.push(edgeIter.next());
							if(DEBUG)System.out.println("2.pushing edge");
							succCount++;
            			}
						if(succCount>1)
							break;
                    }
					succCount=1;
                    continue;
                }

                // if end of cfg reached, exit ...
                if(fromblk.equals((sandmark.analysis.controlflowgraph.BasicBlock)mcfg.sink())) {
                    if(edgeStack.empty()) {
						if(DEBUG)System.out.println(" EXITING ......... ");
						this.setMaxWidth(maxwidth);
                        return;
					}
                    else  {
						if(DEBUG)System.out.println(" reached sync but not EXITING ......... ");
                        continue;
					}
                }

                edgeIter = mcfg.outEdges(fromblk);
                while(edgeIter.hasNext()) {
                    edgeStack.push((sandmark.util.newgraph.EdgeImpl)edgeIter.next());
					if(DEBUG)System.out.println("3.pushing edge");
                    succCount++;
                }
            }// wont exit this loop unless a branch node is detected; range STACK is modified inside.

			if(DEBUG)System.out.println("Extension: succCount = "+succCount);
			if(succCount==0) {
				continue;
			}

            currwidth++;
			if(DEBUG)System.out.println("maxwidth = "+maxwidth);
			if(DEBUG)System.out.println("currwidth = "+currwidth);
            /* update 'maxwidth' */
            if(maxwidth<currwidth)
                maxwidth=currwidth;

            /* new brach node detected; push a range object corresponding to it into stack */
            Range newrange = 
                new Range(currwidth, fromblk, mcfg.getPostDominator(fromblk), mcfg.outEdges(fromblk));
			if(DEBUG)System.out.println(" CREATING NEW RANGE OBJECT ");
            rangeStack.push(newrange);
            currRange = newrange;
        }
        this.setMaxWidth(maxwidth);
        return;
    }

    
    private void setMaxWidth(int width)
    {
        finalWidth = width;
    }
    public int getMaxWidth()
    {
        return finalWidth;
    }

    /** Container for all width range related information at branching nodes
     */
    public class Range {
        private int width;
        public sandmark.analysis.controlflowgraph.BasicBlock sblk;
        public sandmark.analysis.controlflowgraph.BasicBlock eblk;
        public java.util.ArrayList edgelist = new java.util.ArrayList();

        public Range(int w,
                     sandmark.analysis.controlflowgraph.BasicBlock from,
                     sandmark.analysis.controlflowgraph.BasicBlock to,
                     java.util.Iterator edgeIter)
        {
            width = w;
            sblk = from;
            eblk = to;
            while(edgeIter.hasNext())
                edgelist.add((sandmark.util.newgraph.EdgeImpl)edgeIter.next());
        }

        public int getWidth()
        {
            return width;
        }
    }
}
