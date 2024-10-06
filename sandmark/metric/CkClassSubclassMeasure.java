package sandmark.metric;

/** This class evaluates the subclass frequency measure.
 * Extends from 'ClassMetric' class
 */
public class CkClassSubclassMeasure extends ClassMetric
{
    private static final boolean DEBUG = false;
    private static final CkClassSubclassMeasure singleton =
        new CkClassSubclassMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 150;}
    public float getStdDev(){return 10;}

    public String getName(){
        return "Class Subclass";
    }

    public static CkClassSubclassMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){
        /* metric 3: finding the number of the children in the class hierarchy */
        return StatsUtil.getNumberOfSubClasses(myClassObj);
    }
}

