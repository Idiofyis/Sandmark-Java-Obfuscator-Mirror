package sandmark.metric;

/** 'Vocabulary' is the total number of distinct operators and operands.
 *  Extends from 'MethodMetric' class
 */
public class HalsteadMethodVocabMeasure extends MethodMetric
{
    private static final boolean DEBUG = false;
    private static final HalsteadMethodVocabMeasure singleton =
        new HalsteadMethodVocabMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 300;}
    public float getStdDev(){return 15;}

    public String getName(){
        return "Halstead Method Vocab";
    }

    public static HalsteadMethodVocabMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Method methodgen){

        HalsteadUtil util = new HalsteadUtil(methodgen);
        java.util.Vector metricVector = util.evalMeasures();
        if(metricVector==null) {
            return 0;
        }

        int numOperators = ((java.lang.Integer)metricVector.elementAt(0)).intValue();
        int numDisOperators = ((java.lang.Integer)metricVector.elementAt(1)).intValue();
        int numOperands = ((java.lang.Integer)metricVector.elementAt(2)).intValue();
        int numDisOperands = ((java.lang.Integer)metricVector.elementAt(3)).intValue();

        /* CALCULATION OF THE DERIVED METRICS */
        return numDisOperators + numDisOperands;

    }
}

