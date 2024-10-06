package sandmark.metric;

public class MethodOpcodeComparator implements java.util.Comparator{

    private String myOpcode;

    public MethodOpcodeComparator(String opcode){
        myOpcode = opcode;
    }

    public int compare(java.lang.Object o1, java.lang.Object o2){
        if(!(o1 instanceof sandmark.program.Method) ||
           !(o2 instanceof sandmark.program.Method))
            throw new IllegalArgumentException
                ("Method Opcode Comparator can only compare " +
                 "sandmark.program.Method objects");

        int m1 = sandmark.metric.StatsUtil.getNumberOfOpcodesInMethod
            ((sandmark.program.Method)o1, myOpcode);
        int m2 = sandmark.metric.StatsUtil.getNumberOfOpcodesInMethod
            ((sandmark.program.Method)o2, myOpcode);

        return m1 - m2;
    }

    public boolean equals(java.lang.Object o1, java.lang.Object o2){
        return compare(o1, o2) == 0;
    }
}
