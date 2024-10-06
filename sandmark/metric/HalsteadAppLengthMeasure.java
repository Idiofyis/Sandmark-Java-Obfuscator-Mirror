package sandmark.metric;

/** This class implements the Halstead's 'length' measure at application level.
 *  Extends from 'ApplicationMetric' class.
 */
public class HalsteadAppLengthMeasure extends ApplicationMetric
{
    private static final HalsteadAppLengthMeasure singleton =
        new HalsteadAppLengthMeasure();

    public String getName(){
        return "Halstead Length";
    }
    public float getLowerBound(){return 30;}
    public float getUpperBound(){return 40000;}
    public float getStdDev(){return 9800;}

    public static HalsteadAppLengthMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;
        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
            complexityMeasure +=
                HalsteadClassLengthMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }
}



