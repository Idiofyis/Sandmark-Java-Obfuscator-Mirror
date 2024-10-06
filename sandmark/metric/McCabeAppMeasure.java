package sandmark.metric;

/*  This class implements the mcCabe's metrics at method level.
    Extends from 'ApplicationMetric' class
*/

public class McCabeAppMeasure extends ApplicationMetric
{
    private static final McCabeAppMeasure singleton =
        new McCabeAppMeasure();

    public String getName(){
        return "McCabe Cyclomatic Measure";
    }

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 2000;}
    public float getStdDev(){return 450;}

    public static McCabeAppMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){

        int complexityMeasure = 0;

        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz = (sandmark.program.Class)(classes.next());
            complexityMeasure += McCabeClassMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }

}
