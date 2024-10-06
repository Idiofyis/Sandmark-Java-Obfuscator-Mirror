package sandmark.wizard.evaluation.swmetric;

public class MetricChange implements sandmark.wizard.evaluation.Evaluator,
                                     sandmark.wizard.ChoiceRunListener
{    
    private java.util.HashSet mListeners = new java.util.HashSet();
    public void addEvaluationListener
       (sandmark.wizard.evaluation.EvaluationListener l) { mListeners.add(l); }
    public void removeEvaluationListener
       (sandmark.wizard.evaluation.EvaluationListener l) { mListeners.remove(l); }
    public void init(sandmark.wizard.modeling.Model m,
                     sandmark.wizard.ChoiceRunner r) {
       r.addRunListener(this);
    }
    public void ranChoice(sandmark.wizard.modeling.Choice c) {
       float obfLevel = evaluateObfuscationLevel(c.getTarget());
       for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
          sandmark.wizard.evaluation.EvaluationListener l =
             (sandmark.wizard.evaluation.EvaluationListener)it.next();
          l.valueUpdated(c.getTarget(),obfLevel,1.0f);
       }
    }

    public static sandmark.wizard.evaluation.swmetric.Metrics
        getMetrics(sandmark.program.Object obj){

        sandmark.wizard.evaluation.swmetric.Metrics metrics = null;

        if(obj instanceof sandmark.program.Application)
            metrics = getMetrics((sandmark.program.Application)obj, true);
        else if(obj instanceof sandmark.program.Class)
            metrics = getMetrics((sandmark.program.Class)obj, true);
        else if(obj instanceof sandmark.program.Method)
            metrics = getMetrics((sandmark.program.Method)obj, true);
        else throw new RuntimeException("Lacking definition for " +
                                         obj.getClass() + " metrics");
        return metrics;
    }

    public static sandmark.wizard.evaluation.swmetric.Metrics
        getMetrics(sandmark.program.Application app, boolean normalize)
    {
        sandmark.metric.Metric [] appMetrics = sandmark.newstatistics.Stats.getApplicationMetrics();

        float [] measures = new float[appMetrics.length];

        for(int i = 0; i < appMetrics.length; i++){
            sandmark.metric.ApplicationMetric am =
                (sandmark.metric.ApplicationMetric) appMetrics[i];
            //am.evaluateMetric();
            if(normalize)
                measures[i] =  am.getNormalizedMeasure(am.getMeasure(app));
            else
                measures[i] = am.getMeasure(app);
        }

        return new sandmark.wizard.evaluation.swmetric.Metrics(measures);
    }

    public static sandmark.wizard.evaluation.swmetric.Metrics
        getMetrics(sandmark.program.Method m, boolean normalize)
    {
        //sandmark.newstatistics.Stats stats = m.getApplication().getStatistics();
        //java.util.Iterator classes = app.classes();


        sandmark.metric.Metric [] metrics =
            sandmark.newstatistics.Stats.getMethodMetrics();

        float [] measures = new float[metrics.length];

        for(int i = 0; i < metrics.length; i++){
            sandmark.metric.MethodMetric mm =
                (sandmark.metric.MethodMetric) metrics[i];
            //mm.evaluateMetric();
            if(normalize)
                measures[i] = mm.getNormalizedMeasure(mm.getMeasure(m));
            else
                measures[i] = mm.getMeasure(m);
        }
        return new sandmark.wizard.evaluation.swmetric.Metrics(measures);
    }

    public static sandmark.wizard.evaluation.swmetric.Metrics
        getMetrics(sandmark.program.Class cls, boolean normalize)
    {
        //sandmark.newstatistics.Stats stats = cls.getApplication().getStatistics();

        sandmark.metric.Metric [] classMetrics =
            sandmark.newstatistics.Stats.getClassMetrics();

        float [] measures = new float[classMetrics.length];

        for(int i = 0; i < classMetrics.length; i++){
            sandmark.metric.ClassMetric cm =
                (sandmark.metric.ClassMetric)classMetrics[i];
            //cm.evaluateMetric();
            if(normalize)
                measures[i] = cm.getNormalizedMeasure(cm.getMeasure(cls));
            else
                measures[i] = cm.getMeasure(cls);
        }
        return new sandmark.wizard.evaluation.swmetric.Metrics(measures);
    }

    public static Metrics getAppMetrics(sandmark.program.Application app, boolean norm){
        return getMetrics(app, norm);
    }

    public static Metrics[] getClassMetrics(sandmark.program.Application app, boolean norm){
        sandmark.program.Class [] classes = app.getClasses();
        sandmark.wizard.evaluation.swmetric.Metrics [] measures =
            new sandmark.wizard.evaluation.swmetric.Metrics[classes.length];
        for(int i = 0; i < classes.length; i++){
            measures[i] = getMetrics(classes[i], norm);
        }
        return measures;
    }

    public static Metrics[] getMethodMetrics(sandmark.program.Application app, boolean norm){
        sandmark.program.Class [] classes = app.getClasses();
        java.util.ArrayList measures =
            new java.util.ArrayList();

        for(int i = 0; i < classes.length; i++){
            sandmark.program.Method [] methods = classes[i].getMethods();
            for(int j = 0; j < methods.length; j++){
                measures.add(getMetrics(methods[j], norm));
            }
        }

        sandmark.wizard.evaluation.swmetric.Metrics [] measureArray =
            (sandmark.wizard.evaluation.swmetric.Metrics[])
            measures.toArray(new sandmark.wizard.evaluation.swmetric.Metrics[measures.size()]);
        return measureArray;
    }

    public static float getAverage
        (sandmark.wizard.evaluation.swmetric.Metrics m){
        float sum = 0;
        float [] measures = m.measures;
    for(int i = 0; i < measures.length; i++){
        sum += measures[i];
    }
    if(measures.length == 0)
        return 0;

    return sum/measures.length;
    }

    public static float getSum
        (sandmark.wizard.evaluation.swmetric.Metrics m){
        float sum = 0;
        float [] measures = m.measures;
        for(int i = 0; i < measures.length; i++){
            sum += measures[i];
        }
        if(measures.length == 0)
            return 0;

        return sum;
    }
    
    public float evaluateObfuscationLevel(sandmark.program.Object o) {
       return getSum(new Metrics(getMetrics(o).measures));
    }
    
    public float evaluatePerformanceLevel(sandmark.program.Object o) {
       return 1.0f;
    }
    public static float computeChange(sandmark.wizard.evaluation.swmetric.Metrics met1,
                                      sandmark.wizard.evaluation.swmetric.Metrics met2){

        float [] m1 = met1.measures, m2 = met2.measures;


        if(m1.length != m2.length) throw new IllegalArgumentException
                                       ("Sets of metrics must be the same size");

        float [] difference = new float[m1.length];

        for(int i = 0; i < m1.length; i++){
            difference[i] = Math.abs(m1[i] - m2[i]);
        }


        //fix this when the metric scaling gets fixed!
        return getSum(new Metrics(difference));
    }

    public static sandmark.wizard.evaluation.swmetric.Metrics foldMean
        (sandmark.wizard.evaluation.swmetric.Metrics [] metrics){
        if(metrics.length == 0)
            return new sandmark.wizard.evaluation.swmetric.Metrics(new float[]{});

        float [] folded = new float[metrics[0].measures.length];

        for(int i = 0; i < folded.length; i++){
            float sum = 0;
            for(int m = 0; m < metrics.length; m++){
                sum += metrics[m].measures[i];
            }
            folded[i] = sum/metrics.length;
        }
        return new Metrics(folded);
    }


    public static sandmark.wizard.evaluation.swmetric.Metrics foldMax
        (sandmark.wizard.evaluation.swmetric.Metrics [] metrics){
        if(metrics.length == 0)
            return new sandmark.wizard.evaluation.swmetric.Metrics(new float[]{});

        float [] folded = new float[metrics[0].measures.length];

        for(int i = 0; i < folded.length; i++){
            float max = metrics[0].measures[i];
        for(int m = 1; m < metrics.length; m++){
            max = Math.max(max, metrics[m].measures[i]);
        }
        folded[i] = max;
        }
        return new Metrics(folded);
    }

    public static sandmark.wizard.evaluation.swmetric.Metrics foldStdDev
        (sandmark.wizard.evaluation.swmetric.Metrics [] metrics){
        if(metrics.length == 0)
            return new sandmark.wizard.evaluation.swmetric.Metrics(new float[0]);

        sandmark.wizard.evaluation.swmetric.Metrics means = foldMean(metrics);

        float folded [] = new float[metrics[0].measures.length];
        for(int i = 0; i < folded.length; i++){
            float mean = means.measures[i];
            float sum = 0;
            for(int m = 0; m < metrics.length; m++){
                //one value from a method is metrics[m].measures[i]
                sum += (float)Math.pow(metrics[m].measures[i] - mean, 2);
            }

            if(metrics.length <= 1)
                folded[i] = (float)Math.sqrt(sum);
            else
                folded[i] = (float)Math.sqrt(sum/(metrics.length-1));
        }
        return new sandmark.wizard.evaluation.swmetric.Metrics(folded);
    }

    public static sandmark.wizard.evaluation.swmetric.Metrics foldMin
        (sandmark.wizard.evaluation.swmetric.Metrics [] metrics){
        if(metrics.length == 0)
            return new sandmark.wizard.evaluation.swmetric.Metrics(new float[]{});

        float [] folded = new float[metrics[0].measures.length];

        for(int i = 0; i < folded.length; i++){
            float min = metrics[0].measures[i];
            for(int m = 1; m < metrics.length; m++){
                min = Math.min(min, metrics[m].measures[i]);
            }
            folded[i] = min;
        }
        return new Metrics(folded);
    }

    public static void main(String [] args){
        String [] names = sandmark.newstatistics.Stats.getMetricNames();
        for(int i = 0; i < names.length; i++)
            System.out.println(names[i]);

        names = sandmark.newstatistics.Stats.getClassMetricNames();
        for(int i = 0; i < names.length; i++)
            System.out.println(names[i]);

        names = sandmark.newstatistics.Stats.getMethodMetricNames();
        for(int i = 0; i < names.length; i++)
            System.out.println(names[i]);
    }

}


