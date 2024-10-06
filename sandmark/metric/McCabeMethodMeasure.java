package sandmark.metric;

/** This class implements the mcCabe's data structure metrics.
 * Extends from 'MethodMetric' class
 */

public class McCabeMethodMeasure extends MethodMetric
{
    private static final boolean DEBUG = false;
    private static final McCabeMethodMeasure singleton =
        new McCabeMethodMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 117;}
    public float getStdDev(){return (float)4.3;}

    public String getName(){
        return "McCabe Method Measure";
    }

    public static McCabeMethodMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Method methodgen){
        if(DEBUG) System.out.println(" methodName = "+methodgen.getName());
        return StatsUtil.getNumberOfConditionalStats(methodgen);
    }
}

