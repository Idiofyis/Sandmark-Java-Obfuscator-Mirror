package sandmark.metric;

/** This class implements the harrison/magel's metrics at class level.
 *  Extends from 'ClassMetric' class.
 */
public class HarrisonClassMeasure extends ClassMetric
{
    private static final HarrisonClassMeasure singleton =
        new HarrisonClassMeasure();

    public String getName(){
        return "Harrison Measure";
    }
    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 4500000;}
    public float getStdDev(){return 200000;}

    public static HarrisonClassMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){
        int complexityMeasure = 0;
        sandmark.program.Method[] methods = myClassObj.getMethods();
        HarrisonMethodMeasure hmMeasure =
            HarrisonMethodMeasure.getInstance();

        for(int m=0; m<methods.length; m++) {
            complexityMeasure += hmMeasure.getMeasure(methods[m]);
        }

        return complexityMeasure;
    }
}

