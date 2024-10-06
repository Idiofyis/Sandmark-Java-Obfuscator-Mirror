package sandmark.diff;
/** 
 *  A class that provide utilites to DiffAlgorithms
 *  @author Zach Heidepriem
 */
public class DiffUtil {

    /** Converts a constant pool to an array of Strings
     *  @param cpg the constant pool to convert
     *  @return the constant pool copied into an array of Strings
     */
    public static String[] cpToArray(org.apache.bcel.generic.ConstantPoolGen cpg){
        java.util.Vector v = new java.util.Vector();              
        for(int i = 0; i < cpg.getSize(); i++){               
            org.apache.bcel.classfile.Constant c1 = cpg.getConstant(i);        
            if(c1 != null)
                v.add(c1);
        }
        String[] array = new String[v.size()];
        for(int i = 0; i < v.size(); i++)
            array[i] = v.get(i).toString();
        
        return array;
    }
   /**  Determine if two method bodies are equal.
     *  @param a the first method
     *  @param b the second method
     *  @return true iff the input methods have identical bodies
     */
    public static boolean methodsAreEqual(sandmark.program.Method a,
                                          sandmark.program.Method b){
        if(a.getInstructionList() == null ||
           b.getInstructionList() == null)
           return a.getInstructionList() == b.getInstructionList();
        int[] array1 = getOpsAndArgs(
                   a.getInstructionList().getInstructionHandles());        
        int[] array2 = getOpsAndArgs(
                   b.getInstructionList().getInstructionHandles());           
        return array1.length == array2.length &&
            LCS.getLength(array1, array2) == array1.length ;
            /*&&
            a.getName().equals(b.getName())  &&
            a.getClassName().equals(b.getClassName()) &&
            a.getSignature().equals(b.getSignature());*/
    }
   
    /** Determine if two classes are equal; i.e., the sets of method bodies 
     *  are equivalent, and the sets of constants in the constant pools are
     *  equivalent.
     *  @param c1 the first class
     *  @param c2 the second class
     *  @return true iff the classes are equal
     */
    public static boolean classesAreEqual(sandmark.program.Class c1, 
                                          sandmark.program.Class c2){
        sandmark.program.Method[] methods1 = c1.getMethods();
        sandmark.program.Method[] methods2 = c2.getMethods();
        if(methods1.length != methods2.length)
            return false;
        boolean flag = true;        
        for(int i = 0; i < methods1.length; i++){
            for(int j = 0; j < methods2.length && flag; j++){
                if(methodsAreEqual(methods1[i], methods2[j]))                    
                    flag = false;                                            
            }            
            if(flag)
                return false;            
        }            
        String[] cp1 = cpToArray(c1.getConstantPool());
        String[] cp2 = cpToArray(c2.getConstantPool());
        if(cp1.length != cp2.length)
            return false;
        for(int i = 0; i < cp1.length; i++)
            if(!cp1[i].equals(cp2[i]))
                return false;
        return true;               
    }

    public static boolean sameNames(sandmark.program.Method m1, 
                                    sandmark.program.Method m2){
        //System.out.println(m1.getSignature() + "|" + m1.getName() + "|" + m1.getClassName());
        //System.out.println(m2.getSignature() + "|" + m2.getName() + "|" + m2.getClassName());

        return  m1.getName().equals(m2.getName()) &&
            m1.getSignature().equals(m2.getSignature()) &&
            m1.getClassName().equals(m2.getClassName());
    }
   
    /**  Determine if two methods should be compared based on a set of DiffOptions.
     *  @param m1 the first method
     *  @param m2 the second method
     *  @param options the options
     *  @return true iff the methods should be compared
     */
    public static boolean check(sandmark.program.Method m1, 
                                sandmark.program.Method m2,
                                sandmark.diff.DiffOptions options){
        if(options.getFilterBodies() &&
           methodsAreEqual(m1,m2))
            return false;     

        if(options.getFilterNames() && 
           sameNames(m1,m2))          
            return false;   

        if(sameNames(m1,m2))
            return true;

        if(m1.getInstructionList() == null ||
           m2.getInstructionList() == null)
            return false;
        
        if(options.getObjectCompare() == 
           sandmark.diff.DiffOptions.COMPARE_BY_NAME)         
           return false;                  
        
        if(m1.getInstructionList().size() < options.getIgnoreLimit() ||
           m2.getInstructionList().size() < options.getIgnoreLimit())
            return false;
        return true;                                        
    }
    
    /**  Determine if two classes should be compared based on a set of DiffOptions.
     *  @param c1 the first class
     *  @param c2 the second class
     *  @param options the options
     *  @return true iff the classes should be compared
     */
    public static boolean check(sandmark.program.Class c1, 
                                sandmark.program.Class c2,
                                sandmark.diff.DiffOptions options){
       
        if(options.getFilterNames() && 
           c1.getName().equals(c2.getName()))
            return false;

        if(options.getFilterBodies() &&
           classesAreEqual(c1,c2))
            return false; 

        if(c1.getName().equals(c2.getName()))
           return true;

        if(options.getObjectCompare() == sandmark.diff.DiffOptions.COMPARE_BY_NAME)
            return false;
                          
        return true;
    }
    /** Put the opcodes of an array of InstructionHandles into an array
     *  @param instrs the instructions to put in the array
     *  @return an array of opcodes corresponding to the input instructions
     */
    public static int[] getOps(org.apache.bcel.generic.InstructionHandle[] instrs){
	int[] a = new int[instrs.length];
	for(int i = 0; i < instrs.length; i++)
	    a[i] = instrs[i].getInstruction().getOpcode();
	return a;
    }
    /** Put the bytecode of an array of InstructionHandles into an int array
     *  @param instrs the instructions to put in the array
     *  @return an array of ints corresponding to the input instructions
     */
    public static int[] getOpsAndArgs
        (org.apache.bcel.generic.InstructionHandle[] instrs){
	java.util.Vector v = new java.util.Vector();
	for(int i = 0; i < instrs.length; i++){
	    int op = instrs[i].getInstruction().getOpcode();
	    int arg = 0;
	    InstructionWrapper wrap = 
                new InstructionWrapper(instrs[i].getInstruction());
	    //for iload_<n>
	    try{
		if(instrs[i] instanceof org.apache.bcel.generic.IndexedInstruction){
		    arg = 
                        ((org.apache.bcel.generic.IndexedInstruction)
                         instrs[i]).getIndex();
   		}
		else if(wrap.getArgType() == InstructionWrapper.LV_INDEX)
		    arg = wrap.getLVIndex();
		else if(wrap.getArgType() == InstructionWrapper.CONST)
		    arg = wrap.getConstArg().intValue();
		else if(wrap.getArgType() == InstructionWrapper.OFFSET)
		    arg = wrap.getOffset();
		//if(op == 54) //System.out.println("istore: " + arg);
	    }catch(Exception e){ System.out.println("error getting arg"); }
	    v.add(new Integer(pack(op, arg)));
	}
	//Convert v to an array
	int[] a = new int[v.size()];
	for(int i = 0; i < a.length; i++)
	    a[i] = ((Integer)v.get(i)).intValue();
	return a;	
    }
 
    public static int pack(int opcode, int index){
	return index*1000 + opcode;
    }

    private static int unpack(int i){
	return i % 1000;
    }


}
