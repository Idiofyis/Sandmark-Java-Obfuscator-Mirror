package sandmark.metric;

/** This class implements the inheritance measure of CkOO metrics at application level.
 *  Extends from 'ApplicationMetric' class
 */

public class CkAppInheritanceMeasure extends ApplicationMetric
{

    private static final CkAppInheritanceMeasure singleton =
        new CkAppInheritanceMeasure();

    public static CkAppInheritanceMeasure getInstance(){
        return singleton;
    }

    public float getUpperBound(){return 750;}
    public float getLowerBound(){return 1;}
    public float getStdDev(){return 130;}
    public String getName(){
        return "Application Inheritance";
    }

    protected int calculateMeasure(sandmark.program.Application app){

        int complexityMeasure = 0;
        java.util.Iterator classes = app.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz = (sandmark.program.Class)(classes.next());
            complexityMeasure +=
                CkClassInheritanceMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }

}

