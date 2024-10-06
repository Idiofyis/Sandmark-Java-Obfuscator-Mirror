package sandmark.wizard.modeling.dfa;

/**
   A DFANode represents a node in a deterministic finite automata which
   models the dependency relationships between program transformations
   and how they affect the order in which the transformations can
   be applied to different application objects.
   @author Kelly Heffner
*/

public class DFANode
{
    private String myLabel = "";
    private boolean myAccept;
    int tempLabel;
    static int allLabels =0;

    /**
       Constructs a DFA Node with a given label (used for the DOT graph
       representation)
    */
    public DFANode(String label, boolean accept)
    {
        myLabel = label;
        tempLabel = allLabels++;
        myAccept = accept;
    }

    public boolean isAccept()
    {
        return myAccept;
    }

    public void setAccept(boolean a)
    {
        myAccept = a;
    }

    public String getLabel()
    {
        return /*""+tempLabel;//*/myLabel;
    }
}

