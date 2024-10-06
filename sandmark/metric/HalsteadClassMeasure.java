package sandmark.metric;

/** This class implements the Halstead's (all 5 submeasures) measure at class level.
 *  Extends from 'ClassMetric' class.
 */
public class HalsteadClassMeasure extends ClassMetric
{
    private static final HalsteadClassMeasure singleton =
        new HalsteadClassMeasure();

    public String getName(){
        return "Halstead Measure";
    }
    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 23058;}
    public float getStdDev(){return 100;}

    public static HalsteadClassMeasure getInstance(){
        return singleton;
    }
    protected int calculateMeasure(sandmark.program.Class myClassObj){

        int complexityMeasure = 0;
        sandmark.program.Method[] methods = myClassObj.getMethods();
        if(methods==null)
            return 0;
        HalsteadMethodMeasure hmMeasure =
            HalsteadMethodMeasure.getInstance();
        for(int m=0; m<methods.length; m++) {
            complexityMeasure += hmMeasure.getMeasure(methods[m]);
        }
        return complexityMeasure;
    }
}

