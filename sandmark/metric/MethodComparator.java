package sandmark.metric;

public class MethodComparator implements java.util.Comparator{

    private sandmark.metric.MethodMetric myMetric;

    public MethodComparator(sandmark.metric.MethodMetric metric){
        myMetric = metric;
    }

    public int compare(java.lang.Object o1, java.lang.Object o2){
        if(!(o1 instanceof sandmark.program.Method) ||
           !(o2 instanceof sandmark.program.Method))
            throw new IllegalArgumentException
                ("Method Comparator can only compare " +
                 "sandmark.program.Method objects");

        int m1 = myMetric.getMeasure((sandmark.program.Method)o1);
        int m2 = myMetric.getMeasure((sandmark.program.Method)o2);
        return m1 - m2;
    }

    public boolean equals(java.lang.Object o1, java.lang.Object o2){
        return compare(o1, o2) == 0;
    }
}
