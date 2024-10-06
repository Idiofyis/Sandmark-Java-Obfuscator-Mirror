package sandmark.metric;

/** 'Difficulty' is calculated based on the number of operands, number of
 *  distinct operands, number of operators and number of distinct operators
 *  in the method.
 *  Extends from 'MethodMetric' class
 */
public class HalsteadMethodDifficultyMeasure extends MethodMetric
{
    private static final boolean DEBUG = false;
    private static final HalsteadMethodDifficultyMeasure singleton =
        new HalsteadMethodDifficultyMeasure();

    public static HalsteadMethodDifficultyMeasure getInstance(){
        return singleton;
    }

    public float getUpperBound(){return 200;}
    public float getLowerBound(){return 0;}
    public float getStdDev(){return 15;}
    public String getName(){
        return "Halstead Method Difficulty";
    }

    protected int calculateMeasure(sandmark.program.Method methodgen){
        int complexityMeasure = 0;
        HalsteadUtil util = new HalsteadUtil(methodgen);
        java.util.Vector metricVector = util.evalMeasures();
        if(metricVector==null) {
            complexityMeasure=0;
            return 0;
        }

        int numOperators = ((java.lang.Integer)metricVector.elementAt(0)).intValue();
        int numDisOperators = ((java.lang.Integer)metricVector.elementAt(1)).intValue();
        int numOperands = ((java.lang.Integer)metricVector.elementAt(2)).intValue();
        int numDisOperands = ((java.lang.Integer)metricVector.elementAt(3)).intValue();

        /* CALCULATION OF THE DERIVED METRICS */
        // check LOG function above base 2 reqd
        int difficulty;
        if (numDisOperands == 0)
            difficulty = 0;
        else
            difficulty = (numDisOperators/2) * (numOperands/numDisOperands);
        complexityMeasure = difficulty;
        return complexityMeasure;
    }

}

