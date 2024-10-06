package sandmark.metric;

/** Volume is based on the 'length'and 'vocabulary' measure.
 *  Extends from 'MethodMetric' class
 */
public class HalsteadMethodVolumeMeasure extends MethodMetric
{
    private static final boolean DEBUG = false;
    private static final HalsteadMethodVolumeMeasure singleton =
        new HalsteadMethodVolumeMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 2467;}
    public float getStdDev(){return 56;}

    public String getName(){
        return "Halstead Method Volume";
    }

    public static HalsteadMethodVolumeMeasure getInstance(){
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
        int methodLength = numOperators + numOperands;
        int methodVoc = numDisOperators + numDisOperands;
        return (int)(methodLength * java.lang.Math.log (methodVoc));
    }
}

