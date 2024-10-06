package sandmark.wizard.evaluation.swmetric;

/**
    Metrics is a simple wrapper class that bundles together all of the data
    collected from the different statistics.
*/
public class Metrics{
    public float[] measures;

    public Metrics(float[] m)
    {
        measures = m;
    }

    public String toString(){
        if(measures.length == 0)
            return "";
        if(measures.length == 1)
            return ""+measures[0];

        String retVal = "";
        for(int i = 0; i < measures.length-1; i++){
            retVal += measures[i] + ",";
        }
        retVal += measures[measures.length-1];
        return retVal;
    }

    public int length(){
        return measures.length;
    }
}
