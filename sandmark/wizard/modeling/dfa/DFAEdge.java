package sandmark.wizard.modeling.dfa;

public class DFAEdge implements sandmark.util.newgraph.Edge
{
    private DFANode mySource;
    private DFANode myDest;
    private sandmark.Algorithm myAlg;
    private sandmark.program.Object myTarget;

    public DFAEdge(DFANode source,
                   DFANode dest,
                   sandmark.Algorithm alg,
                   sandmark.program.Object targ)
    {
        mySource = source;
        myDest = dest;
        myAlg = alg;
        myTarget = targ;
    }
    
    public Object sourceNode() { return mySource; }
    public Object sinkNode() { return myDest; }
    public sandmark.util.newgraph.Edge clone(Object source,Object Sink) 
       throws CloneNotSupportedException {
       throw new CloneNotSupportedException();
    }

    public DFANode getSource()
    {
        return mySource;
    }

    public DFANode getDestination()
    {
        return myDest;
    }

    public sandmark.Algorithm getAlg()
    {
        return myAlg;
    }

    public sandmark.program.Object getTarget()
    {
        return myTarget;
    }

    public String getCharKey()
    {
        return "(" + myAlg.getShortName() + "," + myTarget.toString() + ")";
    }

}

