package sandmark.metric;

/** 'Effort' is the product of 'difficulty' and volume'
 *  Extends from 'MethodMetric' class
 */
public class HalsteadMethodEffortMeasure extends MethodMetric
{
    private static final boolean DEBUG = false;
    private static final HalsteadMethodEffortMeasure singleton =
        new HalsteadMethodEffortMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 770000;}
    public float getStdDev(){return 27634;}

    public static HalsteadMethodEffortMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Method methodgen){

        int complexityMeasure = 0;
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

        int methodLength = numOperators + numOperands;
        int methodVoc = numDisOperators + numDisOperands;
        int volume = (int)(methodLength * java.lang.Math.log (methodVoc));

        // check LOG function above base 2 reqd
        int difficulty;
        if (numDisOperands == 0)
            difficulty = 0;
        else
            difficulty = (numDisOperators/2) * (numOperands/numDisOperands);

        int effort = difficulty * volume;
        complexityMeasure = effort;
        return complexityMeasure;
    }

    public String getName(){
        return "Halstead Method Effort";
    }
}

