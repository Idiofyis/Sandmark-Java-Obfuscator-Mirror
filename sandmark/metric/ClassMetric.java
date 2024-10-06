package sandmark.metric;

/** A ClassMetric object encapsulates code for obtaining
 *  a class level complexity measure.
 *  Extends from sandmark.metric.Metric class
 *  Any metric that inherits from ClassMetric should contain a
 *  public constructor that takes a
 *  {@link sandmark.program.Class sandmark.program.Class} as
 *  the sole parameter in order to be loaded dynamically.
 */
public abstract class ClassMetric extends Metric
{
    public String getDescription()
    {
        return "class level metrics";
    }

    public String getThresholdInfo()
    {
    return null;
    }

    public int getMeasure(sandmark.program.Class clazz){
        Integer cached = (Integer)clazz.retrieve(this.getClass());
        if(cached != null)
            return cached.intValue();

        int value = calculateMeasure(clazz);
        clazz.cache(this.getClass(), new Integer(value));
        return value;
    }

    protected abstract int calculateMeasure(sandmark.program.Class clazz);
}
