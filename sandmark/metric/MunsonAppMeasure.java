package sandmark.metric;

/** This class implements the munson's metrics at application level.
 *  Extends from 'ApplicationMetric' class
 */
public class MunsonAppMeasure extends ApplicationMetric
{
    private static final MunsonAppMeasure singleton =
        new MunsonAppMeasure();

    public String getName(){
        return "Munson Measure";
    }

    public float getLowerBound(){return 10;}
    public float getUpperBound(){return 4000;}
    public float getStdDev(){return 885;}

    public static MunsonAppMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;

        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz = (sandmark.program.Class)(classes.next());
            complexityMeasure += MunsonClassMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }
}

