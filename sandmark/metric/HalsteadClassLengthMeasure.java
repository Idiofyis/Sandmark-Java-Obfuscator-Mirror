package sandmark.metric;

/** This class implements the Halstead's 'Length' measure at class level.
 *  Extends from 'ClassMetric' class.
 */
public class HalsteadClassLengthMeasure extends ClassMetric
{
    private boolean DEBUG = false;
    private static final HalsteadClassLengthMeasure singleton =
        new HalsteadClassLengthMeasure();

    public String getName(){
        return "Halstead Class Length";
    }
    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 8000;}
    public float getStdDev(){return 718;}


    public static HalsteadClassLengthMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){
        int complexityMeasure = 0;
        sandmark.program.Method[] methods = myClassObj.getMethods();
        if(methods==null)
            return 0;

        for(int m=0; m<methods.length; m++) {
            HalsteadMethodLengthMeasure hmMeasure =
                HalsteadMethodLengthMeasure.getInstance();

            complexityMeasure += hmMeasure.getMeasure(methods[m]);
        }
        return complexityMeasure;
    }

}


