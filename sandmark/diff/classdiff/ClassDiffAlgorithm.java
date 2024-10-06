package sandmark.diff.classdiff;

/** A ClassDiffAlgorithm will perform a comparison of any two class files,
 *  reporting similarities and/or differences.
 *  This is an abstract class that implements the run() method. This method
 *  is used to extract the elements to be diffed from the Application objects.
 *  We also implement the getCurrent() and getTaskLength() methods in order 
 *  to keep track of the progress of run()
 *  @author Zach Heidepriem
 */

public abstract class ClassDiffAlgorithm extends sandmark.diff.DiffAlgorithm {   
    protected int current, taskLength;
    protected boolean stop;
    
    /** Construct a DiffAlgorithm
     *  @param a the first Application
     *  @param b the second Application
     *  @param o the set of DiffOptions to use when diffing
     */
    public ClassDiffAlgorithm(sandmark.program.Application a,
                              sandmark.program.Application b,
                              sandmark.diff.DiffOptions o){
       super(a, b, o); 
       current = 0; 
       taskLength = -1;      
    }     

    /**The run method creates and fills the results for this algorithm.
     * After calling run(), call getResults() to retrieve the results.
     * Compare classes using this object's diffClasses() method.
     */
    public void run(){
        stop = false;
        current = 0;
	taskLength = -1;
	java.util.Vector r = new java.util.Vector();        
        runHelper(r);       
        results = new sandmark.diff.Result[r.size()];
        java.util.Collections.sort(r);
        for(int i = 0; i < r.size(); i++)
            results[i] = (sandmark.diff.Result)r.get(i);
    }      
 
    private void runHelper(java.util.Vector r){
	sandmark.program.Class[] classes1 = app1.getClasses();
	sandmark.program.Class[] classes2 = app2.getClasses();
	taskLength = classes1.length * classes2.length;
	for(int i = 0; i < classes1.length; i++){	   
            sandmark.program.Class c1 = classes1[i];   
	    for (int j = 0; j < classes2.length; j++){
		if(stop)
                    return;
                current++;	
                sandmark.program.Class c2 = classes2[j];
                //Do some preliminary checks based on options
                if(sandmark.diff.DiffUtil.check(c1, c2, options)){
                    sandmark.diff.Result result = diffClasses(c1,c2);
                     if(result != null)                 
                            r.add(result);
                }             
            }            
        }
    }

    /** @return an array of Result objects generated by this algorithm.
     */  
    public sandmark.diff.Result[] getResults(){
        return results;                
    }

    /** Diff two given class objects using this algorithm
     *  @param o1 the first class object
     *  @param o2 the second class object
     *  @return a Result object generated by this algorithm
     */
    public sandmark.diff.Result diff(sandmark.program.Object o1, 
                                     sandmark.program.Object o2){   
        return diffClasses((sandmark.program.Class)o1, 
                           (sandmark.program.Class)o2);               
    }

    /** All ClassDiffAlgorithms must implement this method
     *  @param c1 the first class
     *  @param c2 the second class
     *  @return a Result object generated by this algorithm
     */
    public abstract sandmark.diff.Result diffClasses(sandmark.program.Class c1, 
                                                     sandmark.program.Class c2);

    /** @return a measure of the expected time to apply the algorithm. 
     *  See <code>getCurrent()</code>
     */
    public int getTaskLength(){ 
	return taskLength;
    }    
   
    /** Intended to be called while <code>run()</code> is being run in a separate thread.
     *  @return a measure of how much diffing the alorithm has completed.  
     */
    public int getCurrent(){
	return current;
    } 
    /** Stop this from running
     */
    public void stop(){ 
        stop = true;
    }
}
