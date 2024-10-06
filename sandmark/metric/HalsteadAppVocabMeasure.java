package sandmark.metric;

/** This class implements the Halstead's 'vocabulary' measure at application level.
 *  Extends from 'ApplicationMetric' class.
 */
public class HalsteadAppVocabMeasure extends ApplicationMetric
{
    private static final HalsteadAppVocabMeasure singleton =
        new HalsteadAppVocabMeasure();

    public String getName(){
        return "Halstead Vocab";
    }

    public float getLowerBound(){return 15;}
    public float getUpperBound(){return 15000;}
    public float getStdDev(){return 2500;}

    public static HalsteadAppVocabMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application myAppObj){
        int complexityMeasure = 0;

        java.util.Iterator classes = myAppObj.classes();
        while(classes.hasNext()){
            sandmark.program.Class clazz =
                (sandmark.program.Class)(classes.next());
            complexityMeasure +=
                HalsteadClassVocabMeasure.getInstance().getMeasure(clazz);
        }
        return complexityMeasure;
    }
}



