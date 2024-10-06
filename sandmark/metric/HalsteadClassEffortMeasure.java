package sandmark.metric;

/** This class implements the Halstead's 'Effort' measure at class level.
 *  Extends from 'ClassMetric' class.
 */
public class HalsteadClassEffortMeasure extends ClassMetric
{
    private boolean DEBUG = false;
    private static final HalsteadClassEffortMeasure singleton =
        new HalsteadClassEffortMeasure();

    public String getName(){
        return "Class Effort";
    }
    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 2100000;}
    public float getStdDev(){return 100000;}

    public static HalsteadClassEffortMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){

        int complexityMeasure = 0;
        sandmark.program.Method[] methods = myClassObj.getMethods();
        if(methods==null)
            return 0;

        for(int m=0; m<methods.length; m++) {
            HalsteadMethodEffortMeasure hmMeasure =
                HalsteadMethodEffortMeasure.getInstance();
            complexityMeasure += hmMeasure.getMeasure(methods[m]);
        }
        return complexityMeasure;
    }
}


