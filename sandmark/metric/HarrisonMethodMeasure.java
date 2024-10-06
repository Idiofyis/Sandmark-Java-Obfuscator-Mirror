package sandmark.metric;

/** This class implements the harrison/magel's nesting level metrics.
 *  The evaluations is primarily based on how much nesting(branching)
 *  occurs in a control flow graph.
 *  Extends from 'MethodMetric' class
 */
public class HarrisonMethodMeasure extends MethodMetric
{
    private static final HarrisonMethodMeasure singleton =
        new HarrisonMethodMeasure();
    private static final boolean DEBUG = false;

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 4100000;}
    public float getStdDev(){return 78670;}

    public String getName(){
        return "Harrison Method Measure";
    }

    public static HarrisonMethodMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Method methodgen){
        if(DEBUG) System.out.println(" methodName = "+methodgen.getName());
        sandmark.metric.NestingLevelComplexity nlc =
            new sandmark.metric.NestingLevelComplexity();

        double nestingLevelComplexity = 0.0;
        try {
            nestingLevelComplexity = nlc.evalMeasures(methodgen);
            if(DEBUG) System.out.println("HARRISON MAGEL COMPLEXITY = " + nestingLevelComplexity);
        } catch (java.lang.NullPointerException ex) {
            if(DEBUG) System.out.println("HARRISON MAGEL COMPLEXITY = NIL");
        }

        return (int)nestingLevelComplexity;
    }
}

