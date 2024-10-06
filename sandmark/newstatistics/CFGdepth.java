package sandmark.newstatistics;

/* Calculates the maximum loop nesting level in the method CFG */

public class CFGdepth {

    private sandmark.analysis.controlflowgraph.MethodCFG mcfg = null;
    private sandmark.program.Method meth = null;

    private int maxNestingLevel;
    // traversed in dfs sequence ...
    private java.util.Stack edgeStack = new java.util.Stack(); 
    // list of 'Scope' objects present till now ...
    private java.util.ArrayList currloopscope = new java.util.ArrayList();
    
    public CFGdepth(sandmark.program.Method mObj)
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

    private void updateScope(sandmark.util.newgraph.EdgeImpl edge)
    {
        boolean modify=false;
        sandmark.analysis.controlflowgraph.BasicBlock src = 
            (sandmark.analysis.controlflowgraph.BasicBlock)edge.sourceNode();
        sandmark.analysis.controlflowgraph.BasicBlock dest = 
            (sandmark.analysis.controlflowgraph.BasicBlock)edge.sinkNode();

        for(int k=0; k<currloopscope.size(); k++) {
            Scope tempscope = (Scope)currloopscope.get(k);
            // check if this edge encapsulates this 'tempscope'
            if(mcfg.dominates(dest, tempscope.sblk) && mcfg.postDominates(tempscope.eblk, src)) {
                if(dest.equals(tempscope.sblk) && src.equals(tempscope.eblk))
                    continue;
                // else, extend this scope, incr the nesting level count and set 'modify' flag to true
                tempscope.setScope(dest,src);
                tempscope.setLevel(tempscope.getLevel()+1);
                currloopscope.set(k, tempscope);
                modify=true;
            }
        }
        if(!modify) {
            // create a new scope for this back edge
            Scope newscope = new Scope(1, dest, src);
            currloopscope.add(newscope);
        }
        return;
    }

    public void evaluate()
    {
        java.util.ArrayList backedges = mcfg.getBackedges();

        sandmark.analysis.controlflowgraph.BasicBlock srcblk = mcfg.source();
        sandmark.analysis.controlflowgraph.Edge edge = null;
        java.util.Iterator edgeIter = mcfg.outEdges(srcblk);
        while(edgeIter.hasNext())
            edgeStack.push((sandmark.util.newgraph.EdgeImpl)edgeIter.next());
        
        while(!edgeStack.empty()) {
            // pop the top element from stack; if stack empty, break;
            sandmark.util.newgraph.EdgeImpl tempedge = 
                (sandmark.util.newgraph.EdgeImpl)edgeStack.pop();

            // check if this is back edge, then update currloopscope,
            // dont push this blk into stack; and continue;
            if(this.isBackEdge(tempedge, backedges)) {
                this.updateScope(tempedge);
                continue;
            }

            // get all successors and push into stack;continue;
            sandmark.analysis.controlflowgraph.BasicBlock tempblk = 
                (sandmark.analysis.controlflowgraph.BasicBlock)tempedge.sinkNode();
            edgeIter = mcfg.outEdges(tempblk);
            while(edgeIter.hasNext())
                edgeStack.push(edgeIter.next());
        }
        this.setFinalNestingLevel();
        return;
    }
    
    private void setFinalNestingLevel()
    {
        for(int k=0; k<currloopscope.size(); k++)
            if(maxNestingLevel < ((Scope)currloopscope.get(k)).getLevel())
                maxNestingLevel = ((Scope)currloopscope.get(k)).getLevel();
        return;
    }

    public int getMaxNestingLevel()
    {
        return maxNestingLevel;
    }

    // container for all loop scope related information; including current depth level
    public class Scope {
        private int nestlevel;
        public sandmark.analysis.controlflowgraph.BasicBlock sblk;
        public sandmark.analysis.controlflowgraph.BasicBlock eblk;

        public Scope(int level, sandmark.analysis.controlflowgraph.BasicBlock from,
                         sandmark.analysis.controlflowgraph.BasicBlock to)
        {
            nestlevel = level;
            sblk = from;
            eblk = to;
        }

        public int getLevel()
        {
            return nestlevel;
        }
        public void setLevel(int newlevel)
        {
            nestlevel = newlevel;
        }

        public void setScope(sandmark.analysis.controlflowgraph.BasicBlock from, 
                        sandmark.analysis.controlflowgraph.BasicBlock to)
        {
            sblk = from;
            eblk = to;
        }
    }

}

