package sandmark.diff.methoddiff;

    /** Compares basic blocks of CFGs
     *  The algorithm can be applied by calling run()
     *  explicitly or creating a new thread: 
     *  <code>new Thread(myAlgorithm).start()</code>.
     *  After applying the algorithm, call <code>getResults()</code>
     *  @author Zach Heidepriem
     */

public class DMDiffAlgorithm 
    extends sandmark.diff.methoddiff.MethodDiffAlgorithm {   
    private static boolean DEBUG = false; //true;  
    
    /** Creates a DMDiffAlgorithm 
     *  @param a the first Application
     *  @param b the second Application
     *  @param o the set of DiffOptions to use when diffing
     */   
   public DMDiffAlgorithm(sandmark.program.Application a,
      sandmark.program.Application b,
      sandmark.diff.DiffOptions o){
	super(a,b, o);
	current = 0;
	taskLength = -1;
    }    
     /** @return the name of this algorithm
     */ 
    public String getName(){
	return "Basic Block diff";  
    }
    /** @return a short description of this algorithm
     */ 
    public String getDescription(){
	return "Compares and displays pairs of methods based " +
            "on their basic blocks";
    }     
    
    /** Diff two given objects using this algorithm
     *  @param m1 the first method
     *  @param m2 the second method
     *  @return a Result object generated by this algorithm
     */
    public sandmark.diff.Result diffMethods(sandmark.program.Method m1,
                                            sandmark.program.Method m2){
        sandmark.analysis.controlflowgraph.MethodCFG cfg1 = 
            new sandmark.analysis.controlflowgraph.MethodCFG(m1);
        sandmark.analysis.controlflowgraph.MethodCFG cfg2 = 
            new sandmark.analysis.controlflowgraph.MethodCFG(m2);
        double similarity = getSimilarity(cfg1, cfg2);		    
        if(sandmark.diff.DiffUtil.sameNames(m1,m2) || 
           similarity >= options.getFilter())
            return new sandmark.diff.Result(m1, m2, similarity);        
        return null;
    }    

    public static java.util.ArrayList getBlocksInOrder
        (sandmark.analysis.controlflowgraph.MethodCFG cfg){
        java.util.ArrayList list = (java.util.ArrayList)
            cfg.getBlockList().clone();
        //Sort the blocks by bytecode position
        java.util.Comparator comparator = new java.util.Comparator() {
                public int compare(Object o1, Object o2){
                    sandmark.analysis.controlflowgraph.BasicBlock bb1 = 
                        (sandmark.analysis.controlflowgraph.BasicBlock)o1;
                    sandmark.analysis.controlflowgraph.BasicBlock bb2 = 
                        (sandmark.analysis.controlflowgraph.BasicBlock)o2;
                    if(bb1.getIH() == null && bb2.getIH() != null)
                        return -1;
                    if(bb1.getIH() != null && bb2.getIH() == null)
                        return 1;
                    if(bb1.getIH() == null && bb2.getIH() == null)
                        return 0;
                    int a = bb1.getIH().getPosition();
                    int b = bb2.getIH().getPosition();
                    if(a < b) 
                        return -1;
                    if(a > b) 
                        return 1;
                    return 0;
                }
            };
        java.util.Collections.sort(list, comparator);
        return list;
    }
    
    /** @param r The Result object to color
     *  @return The sandmark.diff.Coloring for r using this algorithm
     */
    public sandmark.diff.Coloring[] color(sandmark.diff.Result r){
	sandmark.diff.Coloring[] result = new sandmark.diff.Coloring[2];
        sandmark.program.Method m1 = (sandmark.program.Method)
            r.getObject1();
        sandmark.program.Method m2 = (sandmark.program.Method)
            r.getObject2();
	org.apache.bcel.generic.Instruction[] il1 = 
	    m1.getInstructionList().getInstructions();
	org.apache.bcel.generic.Instruction[] il2 = 
	    m2.getInstructionList().getInstructions();

	sandmark.analysis.controlflowgraph.MethodCFG a = 
		    new sandmark.analysis.controlflowgraph.MethodCFG(m1);
	sandmark.analysis.controlflowgraph.MethodCFG b = 
		    new sandmark.analysis.controlflowgraph.MethodCFG(m2);
	java.util.ArrayList list1 = getBlocksInOrder(a);
        java.util.ArrayList list2 = getBlocksInOrder(b);      
        //Make some arrays to hold the sorted blocks
	ComparableBlock[] blocks1 = 
	   new ComparableBlock[list1.size()];
	ComparableBlock[] blocks2 = 
	   new ComparableBlock[list2.size()];
	//and fill them
        for(int i = 0; i < list1.size(); i++)
            blocks1[i] =
               new ComparableBlock
                ((sandmark.analysis.controlflowgraph.BasicBlock)list1.get(i));
	
        for(int i = 0; i < list2.size(); i++){
	    ComparableBlock cb = 
                new ComparableBlock
                ((sandmark.analysis.controlflowgraph.BasicBlock)list2.get(i));
	    blocks2[i] = cb;	   	    
	}        
       
        //Make a vector so we call indexOf
	java.util.Vector vec1 = new java.util.Vector();
	for(int i = 0; i < blocks1.length; i++)
	    vec1.add(blocks1[i]);
	int ctr = 0;
	//Make the first Result
        result[0] = new sandmark.diff.Coloring(il1.length,
                                               m1.getClassName() + 
                                               "." +
                                               m1.getName());
	for(int i = 0; i < blocks1.length; i++){
	    for(int j = 0; j < blocks1[i].size(); j++){
		result[0].add(ctr, blocks1[i].getInst(j).toString(), i+1);
		ctr++;
	    }
	}
	ctr = 0;
        //Make the second result
	result[1] = new sandmark.diff.Coloring(il2.length,
                                               m2.getClassName() + 
                                               "." +
                                               m2.getName());
	for(int i = 0; i < blocks2.length; i++){
	    int color = vec1.indexOf(blocks2[i]);
	    for(int j = 0; j < blocks2[i].size(); j++){
		result[1].add(ctr, blocks2[i].getInst(j).toString(), color+1);
		ctr++;
	    }
	}
	return result;
    }  

    private double getSimilarity
        (sandmark.analysis.controlflowgraph.MethodCFG a, 
         sandmark.analysis.controlflowgraph.MethodCFG b){

	java.util.Iterator it1 = a.basicBlockIterator();
	java.util.Iterator it2 = b.basicBlockIterator();

	ComparableBlock[] blocks1 = 
	   new ComparableBlock[a.nodeCount()-2];
	ComparableBlock[] blocks2 = 
	   new ComparableBlock[b.nodeCount()-2];
	int ctr = 0;
	while(it1.hasNext())
	    blocks1[ctr++] = 
              new ComparableBlock
                ((sandmark.analysis.controlflowgraph.BasicBlock)it1.next());
	ctr = 0;
	while(it2.hasNext())
	    blocks2[ctr++] = 
              new ComparableBlock
                ((sandmark.analysis.controlflowgraph.BasicBlock)it2.next());

	ComparableBlock[] sorted1 = (ComparableBlock[])blocks1.clone();
	ComparableBlock[] sorted2 = (ComparableBlock[])blocks2.clone();
	java.util.Arrays.sort(sorted1);
	java.util.Arrays.sort(sorted2);

	int matchCount = 0, i = 0, j = 0;
	boolean flag = true;
	while(i < sorted1.length){	    
	    flag = true;
	    while(j < sorted2.length && flag){
		if(sorted1[i].compareTo(sorted2[j]) == 0){
		    matchCount++;
		    j++;
		    flag = false;
		}
		else if(sorted1[i].compareTo(sorted2[j]) > 0) j++;
		else flag = false;
	    }	
	    i++;
	}
	return (double)matchCount / 
            (double)Math.max(blocks1.length, blocks2.length);
    } 
}
