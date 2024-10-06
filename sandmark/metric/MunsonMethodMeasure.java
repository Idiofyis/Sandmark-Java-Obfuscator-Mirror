package sandmark.metric;

/** This class implements the munson's data structure metrics.
 *  The measure is obtained by taking into account the number of
 *  scalars(non-array variables), the number of vectors(array
 *  variables) and the number of dimensions of each vector.
 *  Extends from 'MethodMetric' class
 */
public class MunsonMethodMeasure extends MethodMetric
{
    private static final boolean DEBUG = false;

    private static final MunsonMethodMeasure singleton =
        new MunsonMethodMeasure();

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 550;}
    public float getStdDev(){return 15;}

    public String getName(){
        return "Munson Method Measure";
    }

    public static MunsonMethodMeasure getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Method methodgen){
        int complexityMeasure = 0;
        if(DEBUG) System.out.println(" methodName = "+methodgen.getName());
        int mscalars = StatsUtil.getNumberOfScalarLocals(methodgen);
        int mvectors = StatsUtil.getNumberOfVectorLocals(methodgen);

        int dims[] = null;
        if(mvectors>0)
            dims = StatsUtil.getMethodVectorDimensions(methodgen);


        /* C(scalar) = 2;
           C(array) = 3 + 2*n + C(arrayElement) */

        complexityMeasure = 0;
        if(dims != null) {
            for(int i=0; i<dims.length; i++)
                complexityMeasure += (3 + 2*dims[i] + 2) * mvectors;
        }
        complexityMeasure+= 2*mscalars;

        if(DEBUG) System.out.println(" measure -> "+complexityMeasure);
        return complexityMeasure;
    }
}

