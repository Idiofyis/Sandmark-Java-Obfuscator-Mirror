package sandmark.metric;

/** This class implements the Halstead's  measures (5 submeasures) at application level.
 *  Extends from 'ApplicationMetric' class.
 */
public class HalsteadAppMeasure extends ApplicationMetric
{
    private static final HalsteadAppMeasure singleton =
        new HalsteadAppMeasure();

    public String getName(){
        return "Halstead Measure";
    }

    public float getStdDev(){return 100;}
    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 23058;}

    public static HalsteadAppMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;
        HalsteadClassMeasure hcMeasure =
            HalsteadClassMeasure.getInstance();
        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class classObj =
                (sandmark.program.Class)classes.next();

            complexityMeasure += hcMeasure.getMeasure(classObj);
            }

        return complexityMeasure;
    }
}



