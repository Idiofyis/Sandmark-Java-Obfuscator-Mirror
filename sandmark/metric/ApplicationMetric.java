package sandmark.metric;

/** An ApplicationMetric object encapsulates code for obtaining
 *  an application level complexity measure.
 */
public abstract class ApplicationMetric extends Metric
{
    public String getDescription()
    {
        return "application level metrics";
    }

    public String getThresholdInfo()
    {
        return null;
    }

    public final int getMeasure(sandmark.program.Application app){
        Integer cached = (Integer)app.retrieve(this.getClass());
        if(cached != null)
            return cached.intValue();

        int value = calculateMeasure(app);
        app.cache(this.getClass(), new Integer(value));
        return value;
    }

    protected abstract int calculateMeasure(sandmark.program.Application app);
}

