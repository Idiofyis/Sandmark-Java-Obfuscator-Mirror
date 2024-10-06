package sandmark.metric;

public class ClassComparator implements java.util.Comparator{

    private sandmark.metric.ClassMetric myMetric;

    public ClassComparator(sandmark.metric.ClassMetric metric){
        myMetric = metric;
    }

    public int compare(java.lang.Object o1, java.lang.Object o2){
        if(!(o1 instanceof sandmark.program.Class) ||
           !(o2 instanceof sandmark.program.Class))
            throw new IllegalArgumentException
                ("Class Comparator can only compare " +
                 "sandmark.program.Class objects");

        int m1 = myMetric.getMeasure((sandmark.program.Class)o1);
        int m2 = myMetric.getMeasure((sandmark.program.Class)o2);
        return m1 - m2;
    }

    public boolean equals(java.lang.Object o1, java.lang.Object o2){
        return compare(o1, o2) == 0;
    }
}
