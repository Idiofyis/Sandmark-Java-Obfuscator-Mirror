package sandmark.metric;

public class ApplicationComparator implements java.util.Comparator{

    private sandmark.metric.ApplicationMetric myMetric;

    public ApplicationComparator(sandmark.metric.ApplicationMetric metric){
        myMetric = metric;
    }

    public int compare(java.lang.Object o1, java.lang.Object o2){
        if(!(o1 instanceof sandmark.program.Application) ||
           !(o2 instanceof sandmark.program.Application))
            throw new IllegalArgumentException
                ("Application Comparator can only compare " +
                 "sandmark.program.Application objects");

        int m1 = myMetric.getMeasure((sandmark.program.Application)o1);
        int m2 = myMetric.getMeasure((sandmark.program.Application)o2);
        return m1 - m2;
    }

    public boolean equals(java.lang.Object o1, java.lang.Object o2){
        return compare(o1, o2) == 0;
    }
}
