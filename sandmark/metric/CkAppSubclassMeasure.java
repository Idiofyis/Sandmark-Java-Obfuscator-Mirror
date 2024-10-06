package sandmark.metric;

/** This class implements the SubClasses measure of CkOO metrics at application level.
 *  Extends from 'ApplicationMetric' class
 */

public class CkAppSubclassMeasure extends ApplicationMetric
{
    private static final CkAppSubclassMeasure singleton =
        new CkAppSubclassMeasure();

    public static CkAppSubclassMeasure getInstance(){
        return singleton;
    }

    public float getUpperBound(){return 700;}
    public float getLowerBound(){return 0;}
    public float getStdDev(){return 150;}
    public String getName(){
        return "Application Subclass Measure";
    }

    protected int calculateMeasure(sandmark.program.Application app){
        int complexityMeasure = 0;

        java.util.Iterator classes = app.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
            complexityMeasure +=
                CkClassSubclassMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }

}

