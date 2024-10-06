package sandmark.metric;

/** This class implements the Halstead's 'difficulty' measure at application level.
 *  Extends from 'ApplicationMetric' class.
 */
public class HalsteadAppDifficultyMeasure extends ApplicationMetric
{
    private static final HalsteadAppDifficultyMeasure singleton =
        new HalsteadAppDifficultyMeasure();

    public float getUpperBound(){return 5000;}
    public float getLowerBound(){return 10;}
    public float getStdDev(){return 1000;}

    public String getName()
    {
        return "Halstead Application Difficulty";
    }

    public static HalsteadAppDifficultyMeasure
        getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application app) {
        int complexityMeasure = 0;
        java.util.Iterator classes = app.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
            complexityMeasure +=
                HalsteadClassDifficultyMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }
}



