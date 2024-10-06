package sandmark.metric;

/** This class implements the munson's metrics at class level.
 *  Extends from 'ClassMetric' class
 */
public class MunsonClassMeasure extends ClassMetric
{
    private static final MunsonClassMeasure singleton =
        new MunsonClassMeasure();

    public String getName(){
        return "Munson Measure";
    }

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 1000;}
    public float getStdDev(){return 75;}

    public static MunsonClassMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){
        int complexityMeasure = 0;
        sandmark.program.Method[] methods = myClassObj.getMethods();
        if(methods!=null)
            for(int m=0; m<methods.length; m++) {
                MunsonMethodMeasure mnMeasure = MunsonMethodMeasure.getInstance();
                //mnMeasure.evaluateMetric();
                complexityMeasure += mnMeasure.getMeasure(methods[m]);
            }

        return complexityMeasure;
    }

}

