package sandmark.metric;

/** This class implements the Ck Object Oriented metrics at application level.
 *  Extends from 'ApplicationMetric' class.
 */

public class CkAppMeasure extends ApplicationMetric
{
    private static final CkAppMeasure singleton =
        new CkAppMeasure();

    public String getName(){
        return "Ck Measure";
    }

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 10;}
    public float getStdDev(){return 1;}

    public static CkAppMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;

        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz = (sandmark.program.Class)(classes.next());
            complexityMeasure += CkClassMeasure.getInstance().getMeasure(clazz);
       }
        return complexityMeasure;
    }
}
