package sandmark.metric;

/** This class implements the harrison/magel's metrics at application level.
 *  Extends from 'ApplicationMetric' class
 */
public class HarrisonAppMeasure extends ApplicationMetric
{
    private static final HarrisonAppMeasure singleton =
        new HarrisonAppMeasure();

    public String getName(){
        return "Harrison Measure";
    }

    public float getLowerBound(){return 250;}
    public float getUpperBound(){return 5000000;}
    public float getStdDev(){return 1500000;}

    public static HarrisonAppMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;

        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
           complexityMeasure +=
               HarrisonClassMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }
}

