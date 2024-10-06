package sandmark.metric;

/** This class implements the Halstead's 'volume' measure at class level.
 *  Extends from 'ClassMetric' class.
 */
public class HalsteadClassVolumeMeasure extends ClassMetric
{
    private boolean DEBUG = false;
    private static final HalsteadClassVolumeMeasure singleton =
        new HalsteadClassVolumeMeasure();

    public String getName(){
        return "Halstead Class Volume";
    }

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 30000;}
    public float getStdDev(){return 2000;}

    public static HalsteadClassVolumeMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj)
    {
        int complexityMeasure = 0;
        sandmark.program.Method[] methods = myClassObj.getMethods();
        if(methods==null)
            return 0;

        for(int m=0; m<methods.length; m++) {
            HalsteadMethodVolumeMeasure hmMeasure =
                HalsteadMethodVolumeMeasure.getInstance();

            complexityMeasure += hmMeasure.getMeasure(methods[m]);
        }
        return complexityMeasure;
    }
}

