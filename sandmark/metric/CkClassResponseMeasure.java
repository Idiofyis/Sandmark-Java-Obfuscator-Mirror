package sandmark.metric;

/** This class evaluates the response of a class i.e. the number of
 *  methods possibly invoked from this class.
 *  Extends from 'ClassMetric' class
 */
public class CkClassResponseMeasure extends ClassMetric
{
    private static final boolean DEBUG = false;
    private static final CkClassResponseMeasure singleton =
        new CkClassResponseMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 900;}
    public float getStdDev(){return 300;}

    public String getName(){
        return "Class Response";
    }

    public static CkClassResponseMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){
        /*metric 5: response of a class; basically the number of methods possibly
          invoked from this class. */

        int numberOfmethodsInScope = 0;
        int numPublicMethods=0;
        sandmark.program.Method mgens[] = myClassObj.getMethods();
        if(mgens!=null)
           for(int k=0; k<mgens.length; k++)
              if( mgens[k].isPublic() )
                 numPublicMethods++;

        int numMethodsInherited = StatsUtil.getApplicationMethodsInherited(myClassObj).size();
        int totalPublicMethods = StatsUtil.getNumberOfTotalPublicMethods(myClassObj.getApplication());
        numberOfmethodsInScope = totalPublicMethods+numMethodsInherited-numPublicMethods;

        return numberOfmethodsInScope;
    }
}

