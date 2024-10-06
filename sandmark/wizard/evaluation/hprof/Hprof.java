package sandmark.wizard.evaluation.hprof;

/**
    Hprof objects are an abstraction of information contained in
    the output file from the Java profiler (hprof).  At the present,
    this file assumes that hprof was run with the options
    <code>cpu=times,heap=sites</code>.  At the time this code was
    written, no known implementation of an abstraction for hprof
    data existed.  The hprof output format is defined, but not
    finalized; changes to the output format should be reflected in
    this code.
    @author Kelly Heffner
    @since 3.3.0
*/

public class Hprof implements sandmark.wizard.evaluation.Evaluator,
                              sandmark.wizard.ChoiceRunListener
{

    private static final String CPU_TIME_START = "CPU SAMPLES BEGIN";
    private static final String CPU_TIME_END = "CPU SAMPLES END";
    private static final String CPU_TIME_START2 = "CPU TIME (ms) BEGIN";
    private static final String CPU_TIME_END2 = "CPU TIME (ms) END";

    //Hash on method --> data, data is sortable by rank
    private java.util.HashMap mCPUInfo;
    private String mJavaArgs;
    /**
        Creates an Hprof object out of the given output file.
    */
    public Hprof(String javaArgs) 
       throws InterruptedException,java.io.IOException {
       mJavaArgs = javaArgs;
       mCPUInfo = parse(doProfile(javaArgs));
    }
    
    public float evaluatePerformanceLevel(sandmark.program.Object o) {
       java.util.HashMap profileData = getProfileData();
       return profileData == null ? 0.0f : getCpuUsage(profileData,o);
    }
    
    public float evaluateObfuscationLevel(sandmark.program.Object o) { 
       return 0.0f;
    }
    
    public void ranChoice(sandmark.wizard.modeling.Choice c) {
       getProfileData();
    }
    
    public void init(sandmark.wizard.modeling.Model model,
                     sandmark.wizard.ChoiceRunner runner) {
       runner.addRunListener(this);
    }
    
    private java.util.HashSet mListeners = new java.util.HashSet();
    public void addEvaluationListener
       (sandmark.wizard.evaluation.EvaluationListener l) { mListeners.add(l); }
    public void removeEvaluationListener
       (sandmark.wizard.evaluation.EvaluationListener l) { mListeners.remove(l); }
    
    
    private java.util.HashMap getProfileData() {
       if(mCPUInfo != null)
          return mCPUInfo;
       try { return mCPUInfo = parse(doProfile(mJavaArgs)); }
       catch(java.io.IOException e) { return null; }
       catch(InterruptedException e) { return null; }
    }
    
    private java.util.HashMap parse(java.io.File hprofOutput) throws java.io.IOException {
       java.util.HashMap cpuInfo = new java.util.HashMap();

        java.io.BufferedReader inFile = new java.io.BufferedReader
            (new java.io.FileReader(hprofOutput));

        //skip over all of the file until the CPU usage information is found
        //and read it in

        String currLine;
        while(inFile.ready()){
            currLine = inFile.readLine();
            //System.out.println("##"+currLine);
            if(currLine.startsWith(CPU_TIME_START) || currLine.startsWith(CPU_TIME_START2)){
                parseCPU(cpuInfo,inFile);
            }
        }
        
        return cpuInfo;
    }

    public String toString(){
        return mCPUInfo.toString();
    }

    //sample format
    //rank   self  accum   count trace method
    //1 33.44% 33.44%       1   305 Matrix.matinv
    //2 24.41% 57.86% 24157828   303 Matrix.get_elem
    //3 15.72% 73.58% 16000000   301 Matrix.get_elem
    //4 14.38% 87.96%       1   304 Matrix.matmul
    //5 12.04% 100.00% 12177828   302 Matrix.set_elem
    //rank percent_in_trace cummulative count_in_trace method_of_trace
    private void parseCPU(java.util.HashMap cpuInfo,java.io.BufferedReader inFile)
        throws java.io.IOException{
        //read the table header and eat it
        String currLine = inFile.readLine();

        while(inFile.ready()){
            currLine = inFile.readLine();
            if(currLine.startsWith(CPU_TIME_END) || currLine.startsWith(CPU_TIME_END2)){
                //all done
                break;
            }
            //rank, percent, cummulative, count, trace number, methodname
            java.util.StringTokenizer entry =
                new java.util.StringTokenizer(currLine, " %");
            if(entry.countTokens() != 6)
                throw new java.io.IOException("CPU usage format incorrect");


            /*int rank = Integer.parseInt(*/entry.nextToken();
            float percent = Float.parseFloat(entry.nextToken());
            entry.nextToken(); //eat cummulative
            int count = Integer.parseInt(entry.nextToken());
            int traceNo = Integer.parseInt(entry.nextToken());
            String method = entry.nextToken();

            //there are multiple entries per method (from different call
            //locations) - add the percent usage, count,
            if(cpuInfo.containsKey(method)){
                CPU_Data data = (CPU_Data)(cpuInfo.get(method));

                data.percent += percent;
                data.count += count;
                data.traceNumbers.add(new Integer(traceNo));
                //keep method
            }
            else{
                CPU_Data data = new CPU_Data(percent, count, traceNo, method);
                System.out.println("HPROF: " + data);
                cpuInfo.put(method, data);
            }

        }
    }
    
    public float getCpuUsage(java.util.HashMap cpuInfo,
                             sandmark.program.Object object) {
       if(object instanceof sandmark.program.Application)
          return getCpuUsage(cpuInfo,(sandmark.program.Application)object);
       if(object instanceof sandmark.program.Class)
          return getCpuUsage(cpuInfo,(sandmark.program.Class)object);
       if(object instanceof sandmark.program.Method)
          return getCpuUsage(cpuInfo,(sandmark.program.Method)object);
       assert false;
       return 0.0f;
    }

    public float getCpuUsage(java.util.HashMap cpuInfo,sandmark.program.Method m){
        String key = m.getClassName() + "." + m.getName();
        //System.out.println(key);
        CPU_Data data = (CPU_Data)(cpuInfo.get(key));

        if(data == null)
            return 0;

        return data.percent;
    }

    public float getCpuUsage(java.util.HashMap cpuInfo,sandmark.program.Class c){
        float retVal = 0;
        //System.out.println("getting for class: " + c);
        sandmark.program.Method [] methods = c.getMethods();
        for(int m = 0; m < methods.length; m++)
            retVal += getCpuUsage(cpuInfo,methods[m]);
        return retVal;
    }

    public float getCpuUsage(java.util.HashMap cpuInfo,sandmark.program.Application a){
        float retVal = 0;
        //System.out.println("getting for app");
        sandmark.program.Class [] classes = a.getClasses();
        for(int c = 0; c < classes.length; c++)
            retVal += getCpuUsage(cpuInfo,classes[c]);
        return retVal;
    }

    private class CPU_Data implements java.lang.Comparable{
        //public int rank;
        public float percent;
        public int count;
        public java.util.ArrayList traceNumbers;
        public String method;

        public CPU_Data(float p, int c, int t, String m){
            percent = p;
            count = c;
            traceNumbers = new java.util.ArrayList();
            traceNumbers.add(new Integer(t));
            method = m;
        }

        //ideally if needed later, a bunch of Comparators could be provided
        //if you wanted to sort the data differently
        public int compareTo(Object o){
            CPU_Data other = (CPU_Data)o;
            return this.percent>other.percent?-1:
                   this.percent==other.percent?0:
                   1;
        }

        public String toString(){
            return ""+percent;
        }

    }

    private java.io.File doProfile(String javaArgs)
        throws java.lang.InterruptedException, java.io.IOException{
       java.io.File tmpFile = java.io.File.createTempFile("smk",".jar");
       tmpFile.deleteOnExit();
       String cmdLine = "/cs/linux/j2sdk1.4.2_02/bin/java " + 
          "-Xrunhprof:cpu=samples,heap=sites,file=" + tmpFile + " " + javaArgs;
        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        java.lang.Process proc = rt.exec(cmdLine);

        proc.waitFor();

        return tmpFile;
    }
}
