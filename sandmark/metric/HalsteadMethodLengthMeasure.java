package sandmark.metric;

/** 'Length' is calculated based on the number of operands and operators
 *  Extends from 'MethodMetric' class
 */
public class HalsteadMethodLengthMeasure extends MethodMetric
{
   private static final boolean DEBUG = false;
   private static final HalsteadMethodLengthMeasure singleton =
      new HalsteadMethodLengthMeasure();

   public float getLowerBound(){return 0;}
   public float getUpperBound(){return 7100;}
   public float getStdDev(){return 186;}

   public String getName(){
      return "Halstead Method Length";
   }

   public static HalsteadMethodLengthMeasure getInstance(){
      return singleton;
   }

   protected int calculateMeasure(sandmark.program.Method methodgen){
      HalsteadUtil util = new HalsteadUtil(methodgen);
      java.util.Vector metricVector = util.evalMeasures();
      if(metricVector==null) {
         return 0;
      }

      int numOperators = ((java.lang.Integer)metricVector.elementAt(0)).intValue();
      int numDisOperators = ((java.lang.Integer)metricVector.elementAt(1)).intValue();
      int numOperands = ((java.lang.Integer)metricVector.elementAt(2)).intValue();
      int numDisOperands = ((java.lang.Integer)metricVector.elementAt(3)).intValue();

      /* CALCULATION OF THE DERIVED METRICS */
      return numOperators + numOperands;
   }
}

