package sandmark.metric;

/** This class implements the Halstead's 'Volume' measure at application level.
 *  Extends from 'ApplicationMetric' class.
 */
public class HalsteadAppVolumeMeasure extends ApplicationMetric
{
    private static final HalsteadAppVolumeMeasure singleton =
        new HalsteadAppVolumeMeasure();

    public String getName(){
        return "Halstead Volume";
    }

    public float getLowerBound(){return 50;}
    public float getUpperBound(){return 10000;}
    public float getStdDev(){return 2700;}

    public static HalsteadAppVolumeMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;

        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
            complexityMeasure +=
                HalsteadClassVolumeMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }
}



