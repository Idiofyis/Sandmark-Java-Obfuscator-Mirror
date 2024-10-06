package sandmark.metric;

/** This class evaluates the depth of the class in the inheritance tree.
 * Extends from 'ClassMetric' class
 */
public class CkClassInheritanceMeasure extends ClassMetric
{
    private static final boolean DEBUG = false;
    private static CkClassInheritanceMeasure singleton =
        new CkClassInheritanceMeasure();

    public float getLowerBound(){return 1;}
    public float getUpperBound(){return 10;}
    public float getStdDev(){return (float)1.45;}

    public String getName(){
        return "Class Inheritance";
    }

    public static CkClassInheritanceMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){
        /* metric 2: finding the depth of the class in the inheritance tree */
        return StatsUtil.getClassHierarchyLevel(myClassObj);
    }
}

