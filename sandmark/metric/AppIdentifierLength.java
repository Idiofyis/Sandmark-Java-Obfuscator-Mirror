package sandmark.metric;

/**
   Measures the average length of identifiers within an application
   (class and interface names)
   @author Kelly Heffner (kheffner@cs.arizona.edu)
*/
public class AppIdentifierLength extends ApplicationMetric{

    private static final sandmark.metric.AppIdentifierLength singleton =
        new sandmark.metric.AppIdentifierLength();

    public float getLowerBound(){return 2;}
    public float getUpperBound(){return 50;}
    public float getStdDev(){return 15;}

    public String getName(){
        return "Identifier Length";
    }

    public static sandmark.metric.AppIdentifierLength getInstance(){
        return singleton;
    }

    protected int calculateMeasure(sandmark.program.Application app){
        int identifierCount = 0;
        int identifierLength = 0;

        int myValue = 0;

        sandmark.program.Class[] classes = app.getClasses();
        for(int i = 0; i < classes.length; i++)
            identifierLength += classes[i].getName().length();

        identifierCount += classes.length;

        if(identifierCount == 0)
            myValue = 0;
        else
            myValue = Math.round
                ((float)identifierLength/(float)identifierCount);

        return myValue;
    }

    protected float normalizeByScaling(int rawValue){
        return 1-super.normalizeByScaling(rawValue);
    }

}
