package sandmark.metric;

/** This class evaluates the coupling between classes
 *  i.e. number of methods in the other classes that lies in the
 *  scope of this class, (and are "actively" invoked from this class).
 *  Extends from 'ClassMetric' class
 */
public class CkClassCouplingMeasure extends ClassMetric
{
    private static final boolean DEBUG = false;
    private static final CkClassCouplingMeasure singleton =
        new CkClassCouplingMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 650;}
    public float getStdDev(){return 57;}

    public String getName(){
        return "Class Coupling";
    }

    public static CkClassCouplingMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){

        /* metric 4: coupling between object classes;
           number of methods in the other classes that lies in the scope of this
           class, (and are "actively" invoked from this class). */

        sandmark.program.Method mgens[] = myClassObj.getMethods();
        int numberOfmethodsInvoked = 0;
        if(mgens!=null)
            for(int k=0; k<mgens.length; k++)
                numberOfmethodsInvoked += StatsUtil.getApplicationCallCount(mgens[k]);

        return numberOfmethodsInvoked;
    }
}

