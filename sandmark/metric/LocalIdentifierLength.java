package sandmark.metric;

/**
   Measures the average length of identifiers within a method
   (from the local variable name table).
   @author Kelly Heffner (kheffner@cs.arizona.edu)
*/
public class LocalIdentifierLength extends MethodMetric{

    private static final LocalIdentifierLength singleton =
        new LocalIdentifierLength();

    public String getName(){
        return "Local Identifier Length";
    }

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 10;}
    public float getStdDev(){return (float)0.8;}

    public static LocalIdentifierLength getInstance(){
        return singleton;
    }

    public int calculateMeasure(sandmark.program.Method myMethod){

        int identifierCount = 0;
        int identifierLength = 0;

	// This code is extraneous and counts variable lengths twice
	// localvariabletable already contains argumentnames
//        String [] argNames = myMethod.getArgumentNames();
//        if(argNames != null)
//            for(int i = 0; i < argNames.length; i++){
//                identifierLength += argNames[i].length();
//                identifierCount++;
//            }

        org.apache.bcel.classfile.LocalVariableTable locals =
            myMethod.getLocalVariableTable();

        if(locals != null){
	    int trueTableLength = locals.getTableLength();
            for(int i = 0; i < trueTableLength; i++){
                org.apache.bcel.classfile.LocalVariable local = locals.getLocalVariable(i);
                if(local != null){
                    String name = local.getName();
                    if(name != null){
                        identifierLength += name.length();
                        identifierCount++;
                    }
                } else
		    trueTableLength++;
            }
        }
        if(identifierCount == 0)
            return 0;
        else
            return Math.round((float)identifierLength/(float)identifierCount);
    }

}
