package sandmark.metric;

/** This class implements the Ck Object Oriented metrics at application level.
 *  Extends from 'ApplicationMetric' class.
 */

public class CkAppCouplingMeasure extends ApplicationMetric
{
    private static final CkAppCouplingMeasure singleton =
        new CkAppCouplingMeasure();

    public static CkAppCouplingMeasure getInstance(){
        return singleton;
    }

    public String getName(){
        return "Application Coupling";
    }

    public float getUpperBound(){return 8000;}
    public float getLowerBound(){return 0;}
    public float getStdDev(){return 1200;}

    protected int calculateMeasure(sandmark.program.Application app){
        int measure = 0;

        java.util.Iterator classes = app.classes();
        while(classes.hasNext()){
            sandmark.program.Class classObj =
                (sandmark.program.Class)classes.next();
            CkClassCouplingMeasure ckMeasure =
                CkClassCouplingMeasure.getInstance();
            measure += ckMeasure.getMeasure(classObj);
        }
        return measure;
    }
}

