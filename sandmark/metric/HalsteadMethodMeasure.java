package sandmark.metric;

/** This class implements the Halstead's metrics at method level.
 *  (Extends from 'MethodMetric' class)
 *  Contains 5 submetrics : length, vocabulary, volume, difficulty and effort.
 *  Submeasures calculated from 'HalsteadUtil' class
 */
public class HalsteadMethodMeasure extends MethodMetric
{
    private static final boolean DEBUG = false;
    private static final HalsteadMethodMeasure singleton =
        new HalsteadMethodMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 23058;}
    public float getStdDev(){return 1500;}

    public String getName(){
        return "Halstead Method Measure";
    }

    public static HalsteadMethodMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Method methodgen){
        sandmark.metric.HalsteadUtil util =
            new sandmark.metric.HalsteadUtil(methodgen);
        java.util.Vector metricVector = util.evalMeasures();
        if(metricVector==null) {
            return 0;
        }

        int numOperators = ((java.lang.Integer)metricVector.elementAt(0)).intValue();
        int numDisOperators = ((java.lang.Integer)metricVector.elementAt(1)).intValue();
        int numOperands = ((java.lang.Integer)metricVector.elementAt(2)).intValue();
        int numDisOperands = ((java.lang.Integer)metricVector.elementAt(3)).intValue();

        /* CALCULATION OF THE DERIVED METRICS */
        int methodLength = numOperators + numOperands;
        int methodVoc = numDisOperators + numDisOperands;
        int volume = (int)(methodLength * java.lang.Math.log (methodVoc));
        /* check LOG function above base 2 reqd */
        int difficulty;
        if (numDisOperands == 0)
            difficulty = 0;
        else
            difficulty = (numDisOperators/2) * (numOperands/numDisOperands);
        int effort = difficulty * volume;

        return methodLength + methodVoc + volume + difficulty + effort;
    }

}

