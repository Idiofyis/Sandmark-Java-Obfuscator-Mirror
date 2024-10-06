package sandmark.config;

/**
 *  An AlgorithmProperty encapsulates information about dependencies between
 *  each obfuscation and watermarking algorithm.  Specifically, it encapsulates
 *  another Sandmark algorithm that is a dependency.
 *  @author Kelly Heffner <a href="mailto:kheffner@cs.arizona.edu">kheffner@cs.arizona.edu</a>
 */
public class AlgorithmProperty extends sandmark.config.RequisiteProperty
{
    private sandmark.Algorithm myAlg;

    /**
     *  Constructs an AlgorithmProperty from the specified Sandmark algorithm.
     *  @param alg an instance of a Sandmark algorithm
     */
    public AlgorithmProperty(sandmark.Algorithm alg)
    {
                myAlg = alg;
    }

    public Class getAlgorithm()
    {
                return myAlg.getClass();
    }

    public boolean equals(Object o)
    {
        if(o instanceof AlgorithmProperty){
            AlgorithmProperty other = (AlgorithmProperty)o;
            return other.getAlgorithm().equals(this.getAlgorithm());
        }
        else
            return false;
    }

    public String toString()
    {
        return "AlgorithmProperty: " + myAlg;
    }

    public int hashCode()
    {
        return myAlg.hashCode();
    }
}










