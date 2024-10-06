package sandmark.metric;

/** This class is the base class for all the metrics implementation.
 *  The different implementations - {ApplicationMetric, ClassMetric, MethodMetric}
 *  extend from this Metric class.
 */
public abstract class Metric
{
    //Added by kheffner on 11/3/03 - raising normalization methods and
    //variables up one inheritance level from AppMetric, MethodMetric, etc..
    //to the basic Metric class.
    //Modified by kheffner on 11/15/03 - changes from protected variables to
    //public methods
    public abstract float getUpperBound();
    public abstract float getLowerBound();
    public abstract float getStdDev();
        public abstract String getName();

    public float getNormalizedMeasure(float rawValue){
       return normalizeByScaling(rawValue);
    }

    protected float normalizeByScaling(float rawValue){
        float metricUpperBound = getUpperBound();
        float metricLowerBound = getLowerBound();

        if(rawValue > metricUpperBound)
            return 1;
        else if(rawValue < metricLowerBound)
         return 0;
        else if(metricUpperBound <= metricLowerBound)
            return 1;
        else
            return (rawValue-metricLowerBound)/(metricUpperBound-metricLowerBound);
    }

    abstract public String getDescription();
    abstract public String getThresholdInfo();

    public final String toString(){
                return getName();
        }
}

