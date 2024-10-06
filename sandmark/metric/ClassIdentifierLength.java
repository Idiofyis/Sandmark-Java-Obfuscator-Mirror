package sandmark.metric;

/**
   Measures the average length of identifiers within a class
   (method and field names).
   @author Kelly Heffner (kheffner@cs.arizona.edu)
*/
public class ClassIdentifierLength extends ClassMetric{

    private static final ClassIdentifierLength singleton =
        new ClassIdentifierLength();

    public String getName(){
        return "Method/Field Identifier Length";
    }

    public float getLowerBound(){return 0;}
    public float getUpperBound(){return 20;}
    public float getStdDev(){return 3;}

    public static ClassIdentifierLength getInstance(){

        return singleton;
    }

    public int calculateMeasure(sandmark.program.Class myClass){

        int identifierCount = 0;
        int identifierLength = 0;

        //Get the length of method names
        sandmark.program.Method[] methods = myClass.getMethods();
        for(int i = 0; i < methods.length; i++){
            identifierLength += methods[i].getName().length();
        }
        identifierCount += methods.length;

        //Get the length of field names
        sandmark.program.Field[] fields = myClass.getFields();
        for(int i = 0; i < fields.length; i++){
            identifierLength += fields[i].getName().length();
        }
        identifierCount += fields.length;

        if(identifierCount == 0)
            return 0;
        else
            return Math.round
                ((float)identifierLength/(float)identifierCount);
    }

}
