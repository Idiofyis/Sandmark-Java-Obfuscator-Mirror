package sandmark.metric;

/** This class implements the Halstead's 'difficulty' measure at class level.
 *  Extends from 'ClassMetric' class.
 */
public class HalsteadClassDifficultyMeasure extends ClassMetric
{
    private boolean DEBUG = false;

    private static final HalsteadClassDifficultyMeasure singleton =
        new HalsteadClassDifficultyMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 900;}
    public float getStdDev(){return 60;}

    public String getName(){
        return "Class Difficulty";
    }

    public static HalsteadClassDifficultyMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class clazz)
    {
        sandmark.program.Method[] methods = clazz.getMethods();
        if(methods==null)
            return 0;
        int complexityMeasure = 0;

        for(int m=0; m<methods.length; m++) {
            HalsteadMethodDifficultyMeasure hmMeasure =
                HalsteadMethodDifficultyMeasure.getInstance();
            //hmMeasure.evaluateMetric();
            complexityMeasure += hmMeasure.getMeasure(methods[m]);
        }
        return complexityMeasure;
    }
}


