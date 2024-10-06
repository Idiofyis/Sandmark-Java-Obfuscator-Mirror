package sandmark.metric;

/* A MethodMetric object encapsulates code for obtaining
 * a method level complexity measure
 */
public abstract class MethodMetric extends sandmark.metric.Metric
{
    public String getDescription()
    {
        return "method level metrics";
    }

    public String getThresholdInfo()
    {
        return null;
    }

    public int getMeasure(sandmark.program.Method method){
        Integer cached = (Integer)method.retrieve(this.getClass());
        if(cached != null)
            return cached.intValue();

        int value = calculateMeasure(method);
        method.cache(this.getClass(), new Integer(value));
        return value;
    }

    protected abstract int calculateMeasure(sandmark.program.Method method);
}
