package sandmark.wizard.modeling.lazydfa;

public class LazyDFAEdge
{
    private sandmark.Algorithm myAlg;
    private sandmark.program.Object myTarget;

    private LazyDFANode mySource;
    private LazyDFANode myLazilyComputedSink;

    public LazyDFAEdge(sandmark.Algorithm alg,
                       sandmark.program.Object target,
                       LazyDFANode source)
    {
        myAlg = alg;
        myTarget = target;
        mySource = source;
    }

    /*package*/ LazyDFANode getLazilyComputedSink()
    {
        return myLazilyComputedSink;
    }

    /*package*/ void setLazilyComputedSink(LazyDFANode sink)
    {
       myLazilyComputedSink = sink;
    }

    public LazyDFANode getSource()
    {
        return mySource;
    }

    public sandmark.Algorithm getAlg()
    {
        return myAlg;
    }

    public sandmark.program.Object getTarget()
    {
        return myTarget;
    }

    public String toString()
    {
        return "(" +myAlg + ", " + myTarget + ")";
    }
}

