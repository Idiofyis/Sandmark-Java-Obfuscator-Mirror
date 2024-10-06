package sandmark.metric;

public abstract class FakeMetric
{

    public int complexityMeasure;
    protected String metricName;
    protected String shortDescription;
    protected String thresholdInfo;
    protected float metricLowerBound;
    protected float metricUpperBound;
    protected float metricStdDev;

    public int getMeasure()
    {
        return complexityMeasure;
    }

}
