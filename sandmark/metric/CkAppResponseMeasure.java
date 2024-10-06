package sandmark.metric;

/** This class implements the response measure of Ck Object Oriented metrics
 *  at application level. Extends from 'ApplicationMetric' class
 */

public class CkAppResponseMeasure extends ApplicationMetric
{
    private static final CkAppResponseMeasure singleton =
        new CkAppResponseMeasure();

    public static CkAppResponseMeasure getInstance(){
        return singleton;
    }

    public float getUpperBound(){return 15000;}
    public float getLowerBound(){return 0;}
    public float getStdDev(){return 33000;}
    public String getName(){
        return "CK Application Response";
    }

    protected int calculateMeasure(sandmark.program.Application app){
        int complexityMeasure = 0;
        java.util.Iterator classes = app.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
            complexityMeasure +=
                CkClassResponseMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }

}

