package sandmark.metric;

/** This class implements the kafura's metrics at application level.
 *  Extends from 'ApplicationMetric' class
 */
public class KafuraAppMeasure extends ApplicationMetric
{
    private static final KafuraAppMeasure singleton =
        new KafuraAppMeasure();

    public String getName(){
        return "Kafura Measure";
    }
    public float getLowerBound(){return 190;}
    public float getUpperBound(){return 9000000;}
    public float getStdDev(){return 2000000;}

    public static KafuraAppMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;

        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
            complexityMeasure +=
                KafuraClassMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }

}
