package sandmark.metric;

/** This class implements the Halstead's 'effort' measure at application level.
 *  Extends from 'ApplicationMetric' class.
 */
public class HalsteadAppEffortMeasure extends ApplicationMetric
{
    private static final sandmark.metric.HalsteadAppEffortMeasure singleton =
        new sandmark.metric.HalsteadAppEffortMeasure();

    public String getName(){
        return "Halstead Effort";
    }

    public float getLowerBound(){return 500;}
    public float getUpperBound(){return 3000000;}
    public float getStdDev(){return 800000;}

    public static HalsteadAppEffortMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;
        java.util.Iterator classes = myAppObj.classes();
        HalsteadClassEffortMeasure measure =
            HalsteadClassEffortMeasure.getInstance();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
            complexityMeasure += measure.getMeasure(clazz);
        }
        return complexityMeasure;
    }
}



