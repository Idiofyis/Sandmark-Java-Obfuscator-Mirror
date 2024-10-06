package sandmark.wizard.evaluation.swmetric;

public abstract class MetricSummary{

    public static void main(String [] args) throws Throwable{

        //java.util.ArrayList appMetrics = new java.util.ArrayList();
        //java.util.ArrayList classMetrics = new java.util.ArrayList();
        //java.util.ArrayList methodMetrics = new java.util.ArrayList();

        //open up each application and collect the stats
        for(int argNo = 0; argNo < args.length; argNo++){
            sandmark.program.Application app =
                new sandmark.program.Application(args[argNo]);

            System.out.println("Computing statistics for: " + args[argNo]);

            Metrics aMetrics = MetricChange.getAppMetrics(app, false);
            System.out.println("Application metrics complete.");
            Metrics [] classMetrics = MetricChange.getClassMetrics(app, false);
            Metrics cMetrics = MetricChange.foldMean(classMetrics);

            System.out.println("Class metrics complete.");
            Metrics [] methodMetrics = MetricChange.getMethodMetrics(app, false);
            Metrics mMetrics = MetricChange.foldMean(methodMetrics);
            System.out.println("Method metrics complete.");

            Metrics aMetrics_s = MetricChange.getAppMetrics(app, true);
            System.out.println("Application metrics complete.");
            Metrics [] classMetrics_s = MetricChange.getClassMetrics(app, true);
            Metrics cMetrics_s = MetricChange.foldMean(classMetrics_s);

            System.out.println("Class metrics complete.");
            Metrics [] methodMetrics_s = MetricChange.getMethodMetrics(app, true);
            Metrics mMetrics_s = MetricChange.foldMean(methodMetrics_s);
            System.out.println("Method metrics complete.");

            String dataFile = args[argNo].substring(0, args[argNo].length()-4) + ".sta";

            System.out.println(args[argNo] + ", " + aMetrics + ", " + cMetrics +
                               ", " + mMetrics + ", " + aMetrics_s + ", " + cMetrics_s +
                               mMetrics_s);

    }

}

   private static void doBoundries(Metrics [] am, String [] names){
      Metrics appMin = MetricChange.foldMin(am);
      Metrics appMax = MetricChange.foldMax(am);
      Metrics appStd = MetricChange.foldStdDev(am);
      for(int i = 0; i < appMin.measures.length; i++){
         System.out.println(names[i] + ": " + appMin.measures[i] + ", " + appMax.measures[i] + ", " + appStd.measures[i]);
      }
   }

    private static void printArray(Metrics [] m, java.io.PrintStream file){
        for(int i = 0; i < m.length; i++)
            file.println(m[i].toString());
    }
}
