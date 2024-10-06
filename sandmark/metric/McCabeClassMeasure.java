package sandmark.metric;

/*  This class implements the mcCabe's metrics at class level.
    Extends from 'ClassMetric' class
*/

public class McCabeClassMeasure extends ClassMetric
{

    private static final McCabeClassMeasure singleton =
        new McCabeClassMeasure();

    public static McCabeClassMeasure getInstance(){
        return singleton;
    }

    public String getName(){
        return "McCabe Cyclomatic Complexity";
    }

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 400;}
    public float getStdDev(){return 25;}

    protected int calculateMeasure(sandmark.program.Class myClassObj){
        int complexityMeasure = 0;

        sandmark.program.Method[] methods = myClassObj.getMethods();

        if(methods!=null)
            for(int m=0; m<methods.length; m++) {
                McCabeMethodMeasure mnMeasure = McCabeMethodMeasure.getInstance();
                complexityMeasure += mnMeasure.getMeasure(methods[m]);
            }

        return complexityMeasure;
    }
}


