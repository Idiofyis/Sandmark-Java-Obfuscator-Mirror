package sandmark.metric;

/** This class implements all the CkOO metric measures at class level.
 *  Extends from 'ClassMetric' class.
 */

public class CkClassMeasure extends ClassMetric
{
    private static final CkClassMeasure singleton =
        new CkClassMeasure();

    public String getName(){
        return "Ck Measure";
    }
    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 10;}
    public float getStdDev(){return 1;}

    public static CkClassMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){

        int complexityMeasure = 0;
        String fullClassName = myClassObj.getName();

        /* metric 2: finding the depth of the class in the inheritance tree */
        int inheritanceDepth = StatsUtil.getClassHierarchyLevel(myClassObj);

        /* metric 3: finding the number of the children in the class hierarchy */
        int numberOfChildren = StatsUtil.getNumberOfSubClasses(myClassObj);

        /* metric 4: coupling between object classes;
           number of methods in the other classes that lies in the scope of this
           class, (and are "actively" invoked from this class). */
        sandmark.program.Method mgens[] = myClassObj.getMethods();
        int numMethodsInvoked = 0;
        if(mgens!=null)
            for(int k=0; k<mgens.length; k++)
                numMethodsInvoked += StatsUtil.getApplicationCallCount(mgens[k]);

        /* metric 5: response of a class; basically the number of methods possibly
           invoked from this class. */
        int numberOfmethodsInScope = 0;
        int numPublicMethods=0;
        if(mgens!=null)
            for(int k=0; k<mgens.length; k++)
                if( mgens[k].isPublic() )
                    numPublicMethods++;
        int numMethodsInherited = StatsUtil.getApplicationMethodsInherited(myClassObj).size();
        int totalPublicMethods = StatsUtil.getNumberOfTotalPublicMethods(myClassObj.getApplication());
        numberOfmethodsInScope = totalPublicMethods+numMethodsInherited-numPublicMethods;

        /* metric 6: lack of cohesion in methods...
           TBD: how to calculate the dissimilarity measure ? */

        complexityMeasure = inheritanceDepth+numberOfChildren+numMethodsInvoked+numberOfmethodsInScope;
        return complexityMeasure;
    }
}

