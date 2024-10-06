package sandmark.wizard.modeling;

public class Choice
{
    public static final Choice DONE = new Choice(null, null);
    public static final java.util.Iterator COMPLETE = null;

    private sandmark.Algorithm myAlg;
    private sandmark.program.Object myTarget;

    public Choice(sandmark.Algorithm alg, sandmark.program.Object targ){
        myAlg = alg;
        myTarget = targ;
    }
 
    public sandmark.Algorithm getAlg(){return myAlg;}

    public String toString(){
        if (this == DONE)
            return "Complete";
        return myAlg +" on " + myTarget;
    }

    public sandmark.program.Object getTarget(){return myTarget;}
}
