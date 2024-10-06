package sandmark.diff.classdiff;

/**A class diff algorithm that performs a literal comparison of constant pools
 *@author Zach Heidepriem 
 */

public class ConstPoolDiff extends sandmark.diff.classdiff.ClassDiffAlgorithm {
    private boolean DEBUG = false;      
   
   /** Construct a DiffAlgorithm
     *  @param a the first Application
     *  @param b the second Application
     *  @param o the set of DiffOptions to use when diffing
     */
    public ConstPoolDiff(sandmark.program.Application a,
                         sandmark.program.Application b,
                         sandmark.diff.DiffOptions o){
	super(a,b,o);
    }

    public String getName(){ return "Const pool diff"; }
    public String getDescription(){ 
        return "Peform a literal comparison of constant pools";            
    }      
 	    
    /** Color the constant pools such that the LCS is displayed in its own color
     *  @param r The Result object to color
     *  @return The sandmark.diff.Coloring for r
     */
    public sandmark.diff.Coloring[] color(sandmark.diff.Result r){
	sandmark.diff.Coloring[] result = new sandmark.diff.Coloring[2];	
        sandmark.program.Class c1 = (sandmark.program.Class)r.getObject1();
        sandmark.program.Class c2 = (sandmark.program.Class)r.getObject2();        
	
        org.apache.bcel.generic.ConstantPoolGen cpg1 = c1.getConstantPool();
        org.apache.bcel.generic.ConstantPoolGen cpg2 = c2.getConstantPool();      
        String[] array1 = sandmark.diff.DiffUtil.cpToArray(cpg1);
        String[] array2 = sandmark.diff.DiffUtil.cpToArray(cpg2);
    
	java.util.Vector[] data = sandmark.diff.LCS.getSubsequence(array1, array2);      
	result[0] = new sandmark.diff.Coloring(array1.length, c1.getName());      
	int ctr = 0;
        for(int i = 0; i < array1.length; i++){         
	    if(data[0].contains(new Integer(i)))                              
		result[0].add(array1[i], 1);            
	    else result[0].add(array1[i]);         
	}
	result[1] = new sandmark.diff.Coloring(array2.length,c2.getName());
	for(int i = 0; i < array2.length; i++){          	    
            if(data[1].contains(new Integer(i)))
		result[1].add(array2[i], 1);
	    else result[1].add(array2[i]);
	}
	return result;
    }
    
    /** Diff two classes using this algorithm
     *  @param c1 the first class
     *  @param c2 the second class
     *  @return a Result object generated by this algorithm
     */
    public sandmark.diff.Result diffClasses(sandmark.program.Class c1, 
                                            sandmark.program.Class c2){        
        
        org.apache.bcel.generic.ConstantPoolGen cpg1 = c1.getConstantPool();
        org.apache.bcel.generic.ConstantPoolGen cpg2 = c2.getConstantPool();      
        String[] array1 = sandmark.diff.DiffUtil.cpToArray(cpg1);
        String[] array2 = sandmark.diff.DiffUtil.cpToArray(cpg2);   
        
        double lcs = (double)sandmark.diff.LCS.getLength(array1, array2);		
        double similarity = lcs / (double)Math.max(array1.length, array2.length);
        if(c1.getName().equals(c2.getName()) || similarity >= options.getFilter())
            return new sandmark.diff.Result(c1, c2, similarity);                
        return null;
    }
}
