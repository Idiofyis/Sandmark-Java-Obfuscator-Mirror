package sandmark.wizard.modeling.dfa;


/**
   This class performs the computations to decide the algorithm strength, and aids in
   computing performance degradation.
*/
public class WeightPrimer
{
    public static void main(String [] args) throws Exception
    {
        //String [] obfuscatorNames = sandmark.obfuscate.Obfuscator.getAllObfuscatorNames();
        java.util.ArrayList throwUp = new java.util.ArrayList();

        sandmark.obfuscate.GeneralObfuscator [] obfuscators;
        if(args[0].equals("-o")){
            //args[1] contains obfuscator to use
            sandmark.obfuscate.GeneralObfuscator obf =
		(sandmark.obfuscate.GeneralObfuscator)
		Class.forName(args[1]).newInstance();
            obfuscators = new sandmark.obfuscate.GeneralObfuscator[]{obf};
            String[] args2 = new String[args.length-2];
            System.arraycopy(args, 2, args2, 0, args.length-2);
            args = args2;
        }
        else {
            obfuscators =
                (sandmark.obfuscate.GeneralObfuscator[])
                sandmark.util.classloading.ClassFinder.getClassesWithAncestor
                (sandmark.util.classloading.IClassFinder.GEN_OBFUSCATOR).toArray
                (new sandmark.obfuscate.GeneralObfuscator[0]);
	}


        float [] goodness = new float[obfuscators.length];
        float [] timing = new float[obfuscators.length];

        //args contains jar files to prime on
        for(int i = 0; i < args.length; i++){
            System.out.println("Opening: " + args[i]);
            String jarfile = args[i];
            String jarfilePrefix = jarfile.substring(0, jarfile.length()-4) + "_";

            sandmark.program.Application app =
                new sandmark.program.Application(jarfile);

            System.out.println("Getting initial application metrics");
            sandmark.wizard.evaluation.swmetric.Metrics initialApp = 
               sandmark.wizard.evaluation.swmetric.MetricChange.getAppMetrics
               (app, true);

            System.out.println("Getting initial class metrics");
            sandmark.wizard.evaluation.swmetric.Metrics initialClass = 
               sandmark.wizard.evaluation.swmetric.MetricChange.foldMean
                (sandmark.wizard.evaluation.swmetric.MetricChange.getClassMetrics
                 (app, true));

            System.out.println("Getting initial method metrics");
            sandmark.wizard.evaluation.swmetric.Metrics initialMethod = 
               sandmark.wizard.evaluation.swmetric.MetricChange.foldMean
                (sandmark.wizard.evaluation.swmetric.MetricChange.getMethodMetrics
                 (app, true));

            app.save(jarfilePrefix + "orig.jar");
            app.close();

            java.util.ArrayList jarNames = new java.util.ArrayList();
            java.util.ArrayList succObfs = new java.util.ArrayList();
            //now run the obfuscators
            for(int o = 0; o < obfuscators.length; o++){
                app = new sandmark.program.Application(jarfile);
                sandmark.Algorithm obf = obfuscators[o];
                System.out.println("Running " + obf + "(" + obf.getClass() + ")");

                boolean success= true;
                try{
                    sandmark.obfuscate.Obfuscator.runObfuscation(app, obf);
                }catch (Throwable e){
                    throwUp.add(obf);
                    System.err.println("Running " + obf.getShortName() + " threw up:" + e);
                    e.printStackTrace();
                    success = false;
                }

                if(!success)
                    continue;
                String outName = (jarfilePrefix + obfuscators[o].getShortName() + ".jar");

                app.save(outName);

                jarNames.add(outName);
                succObfs.add(obfuscators[o]);

                System.err.println("Rebuilding stats after:" + obfuscators[o]);
                sandmark.wizard.evaluation.swmetric.Metrics finalApp = 
                   sandmark.wizard.evaluation.swmetric.MetricChange.getAppMetrics
                   (app, true);
                sandmark.wizard.evaluation.swmetric.Metrics finalClass = 
                   sandmark.wizard.evaluation.swmetric.MetricChange.foldMean
                    (sandmark.wizard.evaluation.swmetric.MetricChange.getClassMetrics
                     (app, true));
                sandmark.wizard.evaluation.swmetric.Metrics finalMethod = 
                   sandmark.wizard.evaluation.swmetric.MetricChange.foldMean
                    (sandmark.wizard.evaluation.swmetric.MetricChange.getMethodMetrics
                     (app, true));

                //calculate the difference and stick it in goodness

                if(obf instanceof sandmark.obfuscate.AppObfuscator)
                    goodness[o] += 
                       sandmark.wizard.evaluation.swmetric.MetricChange.computeChange
                       (finalApp, initialApp);
                else if(obf instanceof sandmark.obfuscate.ClassObfuscator)
                    goodness[o] += 
                       sandmark.wizard.evaluation.swmetric.MetricChange.computeChange
                       (finalClass, initialClass);
                else if(obf instanceof sandmark.obfuscate.MethodObfuscator)
                    goodness[o] += 
                       sandmark.wizard.evaluation.swmetric.MetricChange.computeChange
                       (finalMethod, initialMethod);

                app.close();
            }

            float [] timingApp = 
               getTiming((sandmark.obfuscate.GeneralObfuscator[])succObfs.toArray
                         (new sandmark.obfuscate.GeneralObfuscator[0]), 
                         jarfilePrefix + "orig.jar", 
                         (String[])jarNames.toArray(new String[0]));
            for(int o = 0; o < obfuscators.length; o++){
                int index = succObfs.indexOf(obfuscators[o]);
                if(succObfs.contains(obfuscators[o]))
                   timing[o] += timingApp[index];
            }
        }

        //normalize
        float high = goodness[0];
        float low = goodness[0];
        for(int i = 0; i < goodness.length; i++){
            if(goodness[i] < low)
                low = goodness[i];
            if(goodness[i] > high)
                high = goodness[i];
        }


        float timingHigh = timing[0];
        float timingLow = timing[0];
        for(int i = 0; i < timing.length; i++){
            if(timing[i] < timingLow)
                timingLow = timing[i];
            if(timing[i] > timingHigh)
                timingHigh = timing[i];
        }
        for(int i = 0; i < timing.length; i++){
            timing[i] =1 -( (timing[i] - timingLow)/(timingHigh-timingLow));
        }


        System.out.println("ERRORS: " + throwUp);




        for(int i = 0; i < goodness.length; i++){
            System.out.println(obfuscators[i].getShortName() + ":" + 
                  ((goodness[i] - low)/high - low) + ":" + timing[i]);
        }
    }


    private static float [] getTiming(sandmark.obfuscate.GeneralObfuscator [] obfuscators,
                                      String jar1, String []jar2) throws Exception
    {
        float [] timing = new float[obfuscators.length];
        Runtime runtime = Runtime.getRuntime();
        String javaDir = System.getProperty("java.home");

        //make sure that any caching done by runtime isnt recorded
        java.lang.Process p = runtime.exec(javaDir + "/bin/java -jar " + jar1);
        p.waitFor();
        p = runtime.exec(javaDir + "/bin/java -jar " + jar1);
        p.waitFor();
        p = runtime.exec(javaDir + "/bin/java -jar " + jar1);
        p.waitFor();

        long sum = 0;
        for(int t = 0; t < 3; t++){
            long time1jar1 = System.currentTimeMillis();
            p = runtime.exec(javaDir + "/bin/java -jar " + jar1);
            p.waitFor();
            //p.getInputStream();
            long time2jar1 = System.currentTimeMillis();
            sum += time2jar1 - time1jar1;
        }

        long preTime = Math.round(sum/3.0);

        System.out.println( "Total time of application " + jar1 + 
           " execution before obfuscation: "  + (preTime));

        for(int i = 0; i < timing.length; i++){
            sum = 0;
            for(int t = 0; t < 3; t++){
                long time1jar2 = System.currentTimeMillis();
                p = runtime.exec(javaDir + "/bin/java -jar " + jar2[i]);
                p.waitFor();
                long time2jar2 = System.currentTimeMillis();
                sum += time2jar2 - time1jar2;
            }
            long postTime = Math.round(sum/3.0);

            System.out.println("Total time of application execution after obfuscation " +
                               obfuscators[i].getShortName() + ": " + postTime);

            timing[i] = ((float)Math.max(postTime - preTime, 0));
            //System.out.println("Raw timing: " + timing[i]);

        }

        return timing;
    }

    /*private static float getClassMetrics(sandmark.program.Application app)
    {
        sandmark.newstatistics.Stats stats = app.getStatistics();
        java.util.Iterator classes = app.classes();
        float count = 0;
        float sum = 0;
        while(classes.hasNext()){
            sandmark.program.Class cls =
                (sandmark.program.Class)classes.next();

            sandmark.metric.Metric [] classMetrics =
                stats.getClassMetrics(cls);

            for(int i = 0; i < classMetrics.length; i++){
               sandmark.metric.Metric cm = classMetrics[i];
                //cm.evaluateMetric();
                sum += cm.getNormalizedMeasure(cm.getMeasure());
                count++;
            }

        }
        if(count == 0)
            return 0;
        else
            return sum/count;
    }

    private static float getMethodMetrics(sandmark.program.Application app)
    {
        sandmark.newstatistics.Stats stats = app.getStatistics();
        java.util.Iterator classes = app.classes();

        float count = 0;
        float sum = 0;
        while(classes.hasNext()){
            sandmark.program.Class cls =
                (sandmark.program.Class)classes.next();

            java.util.Iterator methods = cls.methods();
            while(methods.hasNext()){
                sandmark.program.Method m =
                (sandmark.program.Method)methods.next();

                sandmark.metric.Metric [] metrics =
                    stats.getMethodMetrics(m);

                for(int i = 0; i < metrics.length; i++){

                    sandmark.metric.MethodMetric mm =
                        (sandmark.metric.MethodMetric) metrics[i];
                        //mm.evaluateMetric();
                    sum += mm.getNormalizedMeasure(mm.getMeasure());
                    count++;
                }
            }
        }
        if(count == 0) return 0;

        return sum/count;
    }*/
}

