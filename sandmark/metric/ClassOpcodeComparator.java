package sandmark.metric;

public class ClassOpcodeComparator implements java.util.Comparator{

    private String myOpcode;

    public ClassOpcodeComparator(String opcode){
        myOpcode = opcode;
    }

    public int compare(java.lang.Object o1, java.lang.Object o2){
        if(!(o1 instanceof sandmark.program.Class) ||
           !(o2 instanceof sandmark.program.Class))
            throw new IllegalArgumentException
                ("Class Opcode Comparator can only compare " +
                 "sandmark.program.Class objects");

        int m1 = sandmark.metric.StatsUtil.getNumberOfOpcodesInClass
            ((sandmark.program.Class)o1, myOpcode);
        int m2 = sandmark.metric.StatsUtil.getNumberOfOpcodesInClass
            ((sandmark.program.Class)o2, myOpcode);
        return m1 - m2;
    }

    public boolean equals(java.lang.Object o1, java.lang.Object o2){
        return compare(o1, o2) == 0;
    }
}
