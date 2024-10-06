package sandmark.util.stealth;

public class Stealth
{
    private static boolean DEBUG = false;
    private sandmark.program.Application myApp = null;

    /*  Constructor
     */
    public Stealth(sandmark.program.Application app)
    {
         myApp = app;
    }

    /*  Returns the currently implemented method level complexity metrices
     */
    private java.util.Vector getMetricObjects(sandmark.program.Method m)
    {
        java.util.Vector vec = new java.util.Vector(5,1);
        sandmark.metric.HalsteadMethodMeasure hm = sandmark.metric.HalsteadMethodMeasure.getInstance();
        vec.addElement(hm);
        sandmark.metric.McCabeMethodMeasure mc = sandmark.metric.McCabeMethodMeasure.getInstance();
        vec.addElement(mc);
        sandmark.metric.MunsonMethodMeasure mn = sandmark.metric.MunsonMethodMeasure.getInstance();
        vec.addElement(mn);
        return vec;
    }


    /*  This function evaluates the normalcy for the method 'm' with respect to
     *  the benchmark methods already defined and the already evaluated cluster 'clusterObj'
     */
    public float evaluateNormalcy(sandmark.util.stealth.Cluster clusterObj, sandmark.program.Method m)
    {
        /* Compute the complexity of this methid 'm' */
        java.util.Vector metricObjects = this.getMetricObjects(m);

        sandmark.metric.HalsteadMethodMeasure hm = sandmark.metric.HalsteadMethodMeasure.getInstance();
        //hm.evaluateMetric();

        float measure=0;
        for(int k=0; k<metricObjects.size(); k++){
            sandmark.metric.MethodMetric metr = (sandmark.metric.MethodMetric)metricObjects.elementAt(k);
            //metr.evaluateMetric();
            measure += metr.getMeasure(m);
        }
        if(DEBUG) System.out.println(" Global Measure = "+ measure);

        /* Check in which cluster does it fit in */
        float clusters[][] = clusterObj.getClusters();
        float min_sim = 99999999;
        int reference_cluster = -1;
        int totalmethods = 0;

        for(int k=0; k<clusterObj.getNumberOfClusters(); k++) {
            float na = clusters[k][1];
            float nb = 1;
            float a_centroid = clusters[k][0];
            float b_centroid = measure;
            float sim = ((na*nb)/(na+nb))*(float)java.lang.Math.pow((double)(a_centroid-b_centroid),(double)2);
            if(DEBUG) System.out.println(" sim -> " + sim + " min_sim -> "+ min_sim);
            if(sim<min_sim){
                reference_cluster = k;
                min_sim = sim;
            }
            if(k==0){
                reference_cluster = 0;
                min_sim = sim;
            }
            totalmethods += clusters[k][1];
        }
        if(DEBUG) System.out.print(" reference_cluster = "+ reference_cluster);
        float normalcy = (float)clusters[reference_cluster][1]/(float)totalmethods;

        if(DEBUG) System.out.println(" normalcy = " + normalcy + "\n");
        return normalcy;
    }


    /*  This function evaluates the global stealth of an application by comparing it with the
     *  benchmark method clusters
     */
    public float evaluateGlobalStealth(sandmark.util.stealth.Cluster clusterObj,
                                       sandmark.program.Application app)
    {
        float normalcy = 0;
        int totalmethods = 0;
        java.util.Iterator classItr = app.classes();
        while(classItr.hasNext()){
            sandmark.program.Class classObj = (sandmark.program.Class)classItr.next();
            sandmark.program.Method methods[] = classObj.getMethods();
            if(methods!=null){
                for(int k=0; k<methods.length; k++)
                    normalcy += this.evaluateNormalcy(clusterObj, methods[k]);
                totalmethods += methods.length;
            }
        }
        return normalcy/(float)totalmethods;
    }


    /*  This functions evaluates the average normalcy of the watermarked methods when compared to the
     *  average normalcy of the other methods in the target application
     */
    public float evaluateLocalStealth(sandmark.util.stealth.Cluster clusterObj,
                                      java.util.Vector wmMethodObjects)
    {
        float normalcy=0;
        for(int k=0; k<wmMethodObjects.size(); k++)
            normalcy +=
                this.evaluateNormalcy(clusterObj, (sandmark.program.Method)wmMethodObjects.elementAt(k));

        return normalcy/(float)wmMethodObjects.size();
    }

}


