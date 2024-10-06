package sandmark.util.stealth;

/* This class builds clusters for the methods in the benchmark application
 * based on the complexity metrics
 */
public class Cluster
{
    private static boolean DEBUG = false;
    private sandmark.program.Application myApp = null;

    private static int DEFAULT_BENCHCLUSTER_SIZE = 10;
    private static int DEFAULT_APPCLUSTER_SIZE = 5;

    private java.util.Vector methodMeasure = new java.util.Vector(20,5);

    private int numberOfClusters = 0;
    private float finalCluster[][] = null;

    public Cluster(sandmark.program.Application app)
    {
         myApp = app;
    }

    /*  Evaluates the methodlevel complexity for all methods in 'myApp' for
     *  the metric m and returns a vector of complexities
     */
    public java.util.Vector evaluateMethods(String metricName)
    {
        methodMeasure.setSize(0);
        java.util.Iterator classItr = myApp.classes();
        while(classItr.hasNext()) {
            sandmark.program.Class classObj = (sandmark.program.Class)classItr.next();
            sandmark.program.Method methods[] = classObj.getMethods();
            if(methods==null)
                continue;
            for(int k=0; k<methods.length; k++){
                if(metricName.equals("halstead")) {
                    sandmark.metric.HalsteadMethodMeasure m =
                        sandmark.metric.HalsteadMethodMeasure.getInstance();
                    //m.evaluateMetric();
                    methodMeasure.addElement(new Integer(m.getMeasure(methods[k])));
                }
                if(metricName.equals("mcCabe")) {
                    sandmark.metric.McCabeMethodMeasure m =
                        sandmark.metric.McCabeMethodMeasure.getInstance();
                    //m.evaluateMetric();
                    methodMeasure.addElement(new Integer(m.getMeasure(methods[k])));
                }
                if(metricName.equals("munson")) {
                    sandmark.metric.MunsonMethodMeasure m =
                        sandmark.metric.MunsonMethodMeasure.getInstance();
                    //m.evaluateMetric();
                    methodMeasure.addElement(new Integer(m.getMeasure(methods[k])));
                }
            }
        }
        return methodMeasure;
    }


    /*  normal quadratic sort; can implement quicksort or likewise to make it faster
     */
    public float[] bubblesort(float data[], int num)
    {
        for(int k=0; k<num-1; k++)
            for(int m=k+1; m<num; m++)
                if(data[k]>data[m]){
                    float temp = data[k];
                    data[k] = data[m];
                    data[m] = temp;
                }

        return data;
    }



    /*  Builds cluster in a bottom-up approach starting with single element
     *  clusters; uses WARD's clustering technique
     */
    public void buildcluster(java.util.Vector data)
    {
        int csize = data.size();
        if(DEBUG) System.out.println(" data size = " + data.size());
        float tempCluster[] = new float[data.size()];
        float clusterSize[] = new float[data.size()];
        for(int k=0; k<data.size(); k++) {
            tempCluster[k] = ((Float)data.elementAt(k)).floatValue();
            clusterSize[k] = 1;
        }

        int done_flag=0;
        int scanCount=0;

        while(true){

            // just for optimizing
            int count=0;
            int init_ref=0;
            while(true) {
                if(init_ref==data.size())
                    break;
                if(clusterSize[init_ref]!=-1)
                    count++;
                if(count==2)
                    break;
                init_ref++;
            }
            if(DEBUG) System.out.println("init_ref = " + init_ref);
            for(int k=init_ref; k<data.size()-1; k++) {
                /*
                 * a = reference; b = left cluster; c = right cluster
                 * b <---> a <---> c
                 * a is always correctly positioned;
                 * scan left to the first b with clusterSize!=-1
                 * scan right to the first c with clusterSize!=-1
                 *
                 */

                /* setting the pointers for a,b,c */
                int a=k;
                int scanend_flag=0;
                while(clusterSize[a]==-1){
                    a++;
                    if(a==data.size()) {
                        scanend_flag=1;
                        break;
                    }
                }
                if(scanend_flag==1)
                    break;
                int b=k-1;
                while(clusterSize[b]==-1){
                    b--;
                    if(b<0){
                        System.out.println(" Error in scanning 'b' ....\n");
                        System.exit(0);
                    }
                }
                int c=a+1;
                if(DEBUG) System.out.println(" c = " + c + " a = " + a + " data.size() = " + data.size());
                if(c==data.size())
                    break;
                while(clusterSize[c]==-1){
                    c++;
                    if(c==data.size()) {
                        scanend_flag=1;
                        break;
                    }
                }
                if(scanend_flag==1)
                    break;
                if(DEBUG) System.out.println("\n a= " + a + " b = " + b + " c = "+ c +
                           " ::: tempCluster[] = "+tempCluster[a]);

                float na = clusterSize[a];
                float nb = clusterSize[b];
                float nc = clusterSize[c];
                float a_centroid = tempCluster[a];
                float b_centroid = tempCluster[b];
                float c_centroid = tempCluster[c];
                float sim1 =
                    ((na*nb)/(na+nb))*(float)java.lang.Math.pow((double)(a_centroid-b_centroid),(double)2);
                float sim2 =
                    ((na*nc)/(na+nc))*(float)java.lang.Math.pow((double)(a_centroid-c_centroid),(double)2);

                if(DEBUG) {
                        System.out.println(" na = " + na + " nb = "+ nb + " nc = "+nc);
                        System.out.println(" a_centroid = "+a_centroid+" b_centroid = "+b_centroid+" c_centroid = "+c_centroid);
                        System.out.println(" sim1 = " + sim1 + " sim2 = "+ sim2);
                }
                if(sim1<=sim2){
                    // merge cluster {a,b} --> {b}
                    if(DEBUG) System.out.println("left merge");
                    float new_centroid = (a_centroid*na + b_centroid*nb)/(na+nb);
                    tempCluster[b] = new_centroid;
                    clusterSize[b] = na+nb;
                    clusterSize[a] = -1;
                }
                else{
                    // merge cluster {a,c} --> {a}
                    if(DEBUG) System.out.println("right merge");
                    float new_centroid = (a_centroid*na + c_centroid*nc)/(na+nc);
                    tempCluster[a] = new_centroid;
                    clusterSize[a] = na+nc;
                    clusterSize[c] = -1;
                }

                csize--;
                if(DEBUG) System.out.println(" current number of clusters = "+ csize);
                if(csize==this.getNumberOfClusters()) {
                    done_flag = 1;
                    break;
                }
            } /* end of one scan ... for */
            if(DEBUG) System.out.println("End of scan -> " + (++scanCount));
            if(done_flag==1)
                break;
        }

        /* read the clusters into int array and return */
        finalCluster = new float[this.getNumberOfClusters()][2];

        int cnt=0;
        for(int k=0; k<data.size(); k++) {
            if(clusterSize[k]!=-1){
                if(cnt==this.getNumberOfClusters()) {
                    System.out.println(" Invalid number of final clusters ... ");
                    System.exit(0);
                }
                finalCluster[cnt][0] = tempCluster[k];
                finalCluster[cnt++][1] = clusterSize[k];
            }
        }
        if(cnt!=this.getNumberOfClusters()) {
            System.out.println(" Invalid number of final clusters ... " + cnt);
            System.out.println(" clusters required  ... " + this.getNumberOfClusters());
            System.exit(0);
        }
        return;
    }

    /*  Returns the clusters built; dimenstion ->[k][2]
     *  float[][0] contains the cluster centroid
     *  float[][1] contains the cluster size
     */
    public float[][] getClusters()
    {
        return finalCluster;
    }


    /*  Sets the number of clusters to be generated
     */
    public void setNumberOfClusters(int numClusters)
    {
        numberOfClusters = numClusters;
    }

    /*  Returns the number of clusters
     */
    public int getNumberOfClusters()
    {
        return numberOfClusters;
    }




    /* TESTRUN
     * Usage:-  eg. Cluster <bench.jar> <A.jar> <A_wm.jar> <mark_file>
     *
     * arg[0] -> benchmark jar file(eg. specJVM: bench.jar)
     * arg[1] -> original application jar file  for which to evaluate the global stealth
     * arg[2] -> watermarked application jar file for which to evaluate the global stealth
     * arg[3] -> fileName of the markedMethods obtained from your watermarker to evaluate local stealth
     */
    public static void main(String [] args) throws Exception
    {
        System.out.println(" Starting Cluster_test  ... \n\n");
        sandmark.metric.Metric allmetrics[] = null;

        System.out.println("Running test suite: " + args[0]);
        sandmark.program.Application appObj =
            new sandmark.program.Application(args[0]);

        Cluster testCluster = new Cluster(appObj);

        if(DEBUG||true) System.out.println("HalsteadMeasure ... ");
        java.util.Vector measure1 = testCluster.evaluateMethods("halstead");
        float measure[] = new float[measure1.size()];
        for(int k=0; k<measure1.size(); k++)
            measure[k] += ((Integer)measure1.elementAt(k)).intValue();

        if(DEBUG||true) System.out.println("MunsonMeasure ... ");
        java.util.Vector measure2 = testCluster.evaluateMethods("munson");
        for(int k=0; k<measure1.size(); k++)
            measure[k] += ((Integer)measure2.elementAt(k)).intValue();

        if(DEBUG||true) System.out.println("McCabeMeasure ... ");
        java.util.Vector measure3 = testCluster.evaluateMethods("mcCabe");
        for(int k=0; k<measure1.size(); k++)
            measure[k] += ((Integer)measure3.elementAt(k)).intValue();

        if(DEBUG) {
            System.out.println(" MethodMeasureBeforeSort ...");
            for(int k=0; k<measure1.size(); k++) {
                System.out.print(" "+measure[k]);
            }
        }
        if(DEBUG) System.out.println(" Sorting complexites ... ");
        float sortMeasure[] = testCluster.bubblesort(measure, measure1.size());
        if(DEBUG) {
            System.out.println(" MethodMeasureAfterSort ...");
            for(int k=0; k<measure1.size(); k++) {
                System.out.print(" "+sortMeasure[k]);
            }
        }

        java.util.Vector vec = new java.util.Vector(measure1.size(), 10);
        for(int k=0; k<measure1.size(); k++)
            vec.addElement(new Float(sortMeasure[k]));

        testCluster.setNumberOfClusters(DEFAULT_BENCHCLUSTER_SIZE);
        if(DEBUG||true) System.out.println(" Building clusters of size " + testCluster.getNumberOfClusters()+ "  ... ");
        testCluster.buildcluster(vec);

        if(DEBUG||true) {
            System.out.println(" -------------- TestClusters  ------------- ");
            for(int k=0; k<testCluster.getNumberOfClusters(); k++)
                System.out.println(k+ " : " +testCluster.finalCluster[k][0] + " : " + testCluster.finalCluster[k][1]);
        }



        if(DEBUG||true) System.out.println(" Evaluating global stealth .................... ");

        String orig_jar = args[1];
        String wm_jar = args[2];
        String markfile = args[3];

        if(DEBUG) System.out.println(" orig_jar -> " + orig_jar);
        sandmark.program.Application orig_app = new sandmark.program.Application(orig_jar);
        sandmark.util.stealth.Stealth orig_st_obj = new sandmark.util.stealth.Stealth(orig_app);
        float orig_global_stealth = orig_st_obj.evaluateGlobalStealth(testCluster, orig_app);
        if(DEBUG||true) System.out.println(" ### orig_global_stealth = "+ orig_global_stealth);

        if(DEBUG) System.out.println("\n wm_jar -> " + wm_jar);
        sandmark.program.Application wm_app = new sandmark.program.Application(wm_jar);
        sandmark.util.stealth.Stealth wm_st_obj = new sandmark.util.stealth.Stealth(wm_app);
        float wm_global_stealth = wm_st_obj.evaluateGlobalStealth(testCluster, wm_app);
        if(DEBUG||true) System.out.println(" ### wm_global_stealth = "+ wm_global_stealth);


        if(DEBUG||true) System.out.println(" Evaluating local stealth .................. ");

        java.io.File f = new java.io.File(markfile);
        java.io.FileReader fr = new java.io.FileReader(f);
        int c=0;
        java.util.Vector methodName = new java.util.Vector(10,1);
        java.lang.StringBuffer strbuf = new StringBuffer();
        while((c=fr.read())!=-1){
            if(DEBUG||true) System.out.print((char)c+ " ");
            if(((char)c)=='\n') {
                int dup_flag = 0;
                for(int k=0; k<methodName.size(); k++)
                    if(((String)methodName.elementAt(k)).equals(strbuf.toString())) {
                        dup_flag = 1;
                        break;
                    }
                if(dup_flag==0)
                    methodName.addElement(strbuf.toString());
                strbuf.setLength(0);
                continue;
            }
            strbuf.append((char)c);
        }
        try {
            fr.close();
        }catch(java.io.IOException e){
            System.out.println(" Exception : "+ e);
            System.exit(0);
        }

        if(DEBUG){
            System.out.println("\n Marked methods ::: ");
            for(int k=0; k<methodName.size(); k++)
                System.out.println((String)methodName.elementAt(k));
        }

        /* Evaluate the local stealth for the orig_application */
        java.util.Iterator orig_classitr = orig_app.classes();
        java.util.Vector orig_mObjs = new java.util.Vector(10,2);
        while(orig_classitr.hasNext()) {
            sandmark.program.Class orig_classObj = (sandmark.program.Class)orig_classitr.next();
            sandmark.program.Method orig_methods[] = orig_classObj.getMethods();
            if(orig_methods!=null)
                for(int p=0; p<orig_methods.length; p++)
                    orig_mObjs.addElement(orig_methods[p]);
        }

        java.util.Vector orig_ref_Obj = new java.util.Vector(10,1);
        for(int k=0; k<methodName.size(); k++) {
            String fullname = (String)methodName.elementAt(k);
            String cName = fullname.substring(0, fullname.lastIndexOf('/'));
            String mName = fullname.substring(fullname.lastIndexOf('/')+1);
            for(int p=0; p<orig_mObjs.size(); p++) {
                sandmark.program.Method tempmObj = (sandmark.program.Method)orig_mObjs.elementAt(p);
                if(cName.equals(tempmObj.getClassName()) && mName.equals(tempmObj.getName())) {
                    if(DEBUG) System.out.println(" match found for watermarked method ... ##");
                    orig_ref_Obj.addElement(tempmObj);
                    break;
                }
            }
        }

        if(DEBUG||true) System.out.println(" Creating clusters from target application methods ... ");
        sandmark.util.stealth.Cluster appCluster = new sandmark.util.stealth.Cluster(orig_app);
        if(DEBUG||true) System.out.println("HalsteadMeasure ... ");
        measure1 = appCluster.evaluateMethods("halstead");
        float msr[] = new float[measure1.size()];
        for(int k=0; k<measure1.size(); k++)
            msr[k] += ((Integer)measure1.elementAt(k)).intValue();

        if(DEBUG||true) System.out.println("MunsonMeasure ... ");
        measure2 = appCluster.evaluateMethods("munson");
        for(int k=0; k<measure1.size(); k++)
            msr[k] += ((Integer)measure2.elementAt(k)).intValue();

        if(DEBUG||true) System.out.println("McCabeMeasure ... ");
        measure3 = appCluster.evaluateMethods("mcCabe");
        for(int k=0; k<measure1.size(); k++)
            msr[k] += ((Integer)measure3.elementAt(k)).intValue();

        if(DEBUG){
            System.out.println(" MethodMeasureBeforeSort ...");
            for(int k=0; k<measure1.size(); k++) {
                System.out.print(" "+msr[k]);
            }
        }
        if(DEBUG) System.out.println(" Sorting complexites ... ");
        sortMeasure = appCluster.bubblesort(msr, measure1.size());
        if(DEBUG) {
            System.out.println(" MethodMeasureAfterSort ...");
            for(int k=0; k<measure1.size(); k++) {
                System.out.print(" "+sortMeasure[k]);
            }
        }
        vec.setSize(0);
        for(int k=0; k<measure1.size(); k++)
            vec.addElement(new Float(sortMeasure[k]));
    if(DEBUG) System.out.println(" number of methods for clustering = "+vec.size());

    if(vec.size()<=1) {
        System.out.println(" Single or no method in Application: no clustering done \n");
        System.exit(1);
    }

    if(vec.size()<DEFAULT_APPCLUSTER_SIZE)
        DEFAULT_APPCLUSTER_SIZE = vec.size()-1;
        appCluster.setNumberOfClusters(DEFAULT_APPCLUSTER_SIZE);

        if(DEBUG||true) System.out.println(" Building clusters of size " +
                                    appCluster.getNumberOfClusters()+ "  ... ");
        appCluster.buildcluster(vec);
        if(DEBUG||true) {
            System.out.println(" ----------------- Target Application Clusters  --------------");
            for(int k=0; k<appCluster.getNumberOfClusters(); k++)
                System.out.println(k+ " : " +appCluster.finalCluster[k][0] +
                                   " : " + appCluster.finalCluster[k][1]);
        }


        float orig_local_stealth = orig_st_obj.evaluateLocalStealth(appCluster, orig_ref_Obj);
        if(DEBUG||true) System.out.println(" ### orig_local_stealth = "+ orig_local_stealth);

        /* Evaluate the local stealth for the wm_application */
        java.util.Iterator wm_classitr = wm_app.classes();
        java.util.Vector wm_mObjs = new java.util.Vector(10,2);
        while(wm_classitr.hasNext()) {
            sandmark.program.Class wm_classObj = (sandmark.program.Class)wm_classitr.next();
            sandmark.program.Method wm_methods[] = wm_classObj.getMethods();
            if(wm_methods!=null)
                for(int p=0; p<wm_methods.length; p++)
                    wm_mObjs.addElement(wm_methods[p]);
        }
        java.util.Vector wm_ref_Obj = new java.util.Vector(10,1);
        for(int k=0; k<methodName.size(); k++) {
            String fullname = (String)methodName.elementAt(k);
            String cName = fullname.substring(0, fullname.lastIndexOf('/'));
            String mName = fullname.substring(fullname.lastIndexOf('/')+1);
            for(int p=0; p<wm_mObjs.size(); p++) {
                sandmark.program.Method tempmObj = (sandmark.program.Method)wm_mObjs.elementAt(p);
                if(cName.equals(tempmObj.getClassName()) && mName.equals(tempmObj.getName())) {
                    if(DEBUG) System.out.println(" match found for watermarked method ...");
                    wm_ref_Obj.addElement(tempmObj);
                    break;
                }
            }
        }
        float wm_local_stealth = wm_st_obj.evaluateLocalStealth(testCluster, wm_ref_Obj);
        if(DEBUG||true) System.out.println(" ### wm_local_stealth = "+ wm_local_stealth);

    }/* end of main_test */
}


