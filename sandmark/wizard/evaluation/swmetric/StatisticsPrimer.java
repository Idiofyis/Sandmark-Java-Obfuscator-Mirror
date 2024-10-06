package sandmark.wizard.evaluation.swmetric;

public class StatisticsPrimer
{
public static void main(String [] args) throws Exception
{
    java.util.ArrayList appMetrics = new java.util.ArrayList();
    java.util.ArrayList classMetrics = new java.util.ArrayList();
    java.util.ArrayList methodMetrics = new java.util.ArrayList();

    //open up each application and collect the stats
    for(int argNo = 0; argNo < args.length; argNo++){
        sandmark.program.Application app =
            new sandmark.program.Application(args[argNo]);

        System.out.println("Computing statistics for: " + args[argNo]);

        Metrics aMetrics = MetricChange.getAppMetrics(app, false);
        System.out.println("Application metrics complete.");
        Metrics [] cMetrics = MetricChange.getClassMetrics(app, false);
        System.out.println("Class metrics complete.");
        Metrics [] mMetrics = MetricChange.getMethodMetrics(app, false);
        System.out.println("Method metrics complete.");
        String dataFile = args[argNo].substring(0, args[argNo].length()-4) + ".sta";
        java.io.PrintStream file = new java.io.PrintStream(new java.io.FileOutputStream(dataFile));

        file.println(aMetrics + "\n\n");
        printArray(cMetrics, file);
        file.println("\n");
        printArray(mMetrics, file);
        file.close();

        appMetrics.add(aMetrics);
        for(int i = 0; i < cMetrics.length; i++)
            classMetrics.add(cMetrics[i]);
        for(int i = 0; i < mMetrics.length; i++)
            methodMetrics.add(mMetrics[i]);

    }

    Metrics [] am = (Metrics [])appMetrics.toArray(new Metrics[appMetrics.size()]);
    Metrics [] cm = (Metrics [])classMetrics.toArray(new Metrics[classMetrics.size()]);
    Metrics [] mm = (Metrics [])methodMetrics.toArray(new Metrics[methodMetrics.size()]);

    String [] aNames = sandmark.newstatistics.Stats.getMetricNames();
    String [] cNames = sandmark.newstatistics.Stats.getClassMetricNames();
    String [] mNames = sandmark.newstatistics.Stats.getMethodMetricNames();




    System.out.println("application metric boundries:");
    doBoundries(am, aNames);
    System.out.println("\nclass metric boundries:");
    doBoundries(cm, cNames);
    System.out.println("\nmethod metric boundries:");
    doBoundries(mm, mNames);
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
