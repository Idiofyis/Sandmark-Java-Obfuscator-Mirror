package sandmark.metric;

/** This class implements the Halstead's 'vocabulary' measure at class level.
 *  Extends from 'ClassMetric' class.
 */
public class HalsteadClassVocabMeasure extends ClassMetric
{
    private boolean DEBUG = false;
    private static final HalsteadClassVocabMeasure singleton =
        new HalsteadClassVocabMeasure();

    public String getName(){
        return "Halstead Class Volume";
    }

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 3000;}
    public float getStdDev(){return 120;}

    public static HalsteadClassVocabMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Class myClassObj){

        int complexityMeasure = 0;
        sandmark.program.Method[] methods = myClassObj.getMethods();
        if(methods==null)
            return 0;

        for(int m=0; m<methods.length; m++) {
            HalsteadMethodVocabMeasure hmMeasure =
                HalsteadMethodVocabMeasure.getInstance();

            complexityMeasure += hmMeasure.getMeasure(methods[m]);
        }
        return complexityMeasure;
    }

}


