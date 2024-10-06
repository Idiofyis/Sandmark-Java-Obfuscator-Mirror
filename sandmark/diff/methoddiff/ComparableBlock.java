package sandmark.diff.methoddiff;

/**Wrap a basic block into a Comparable. Blocks are ordered
 * by instruction. DMDiffAlgorithm, DM watermarker both use
 * this to sort basic blocks to improve efficiency.
 * @author Zach Heidepriem
 */

public class ComparableBlock implements Comparable {
	
    sandmark.analysis.controlflowgraph.BasicBlock block;  
	
    public ComparableBlock(sandmark.analysis.controlflowgraph.BasicBlock b){
	block = b;
    }
	
    public int compareTo(Object o){
	ComparableBlock x = (ComparableBlock)o;
	      
	int min = Math.min(x.size(), size());
	for(int i = 0; i < min; i++){
	    int lhs = getOpcode(i);
	    int rhs = x.getOpcode(i);
	    if(lhs < rhs)
		return -1;
	    else if(lhs > rhs)
		return 1;
	}
	return 0;
    }

    public int size(){
	return block.getInstList().size();
    }

    public int getOpcode(int index){
        org.apache.bcel.generic.InstructionHandle ih = 
            (org.apache.bcel.generic.InstructionHandle)
            (block.getInstList().get(index));
        org.apache.bcel.generic.Instruction inst = ih.getInstruction();
        sandmark.diff.InstructionWrapper iw =
            new sandmark.diff.InstructionWrapper(inst);            
        return iw.getOpcode();
    }

    public org.apache.bcel.generic.InstructionHandle getInst(int index){
	return ((org.apache.bcel.generic.InstructionHandle)
                block.getInstList().get(index));	
    }

    public String toString(){
	String s = "[";
	for(int i = 0; i < size()-1; i++)
	    s += getOpcode(i)+ ",";	    
	return s + getOpcode(size()-1) + "]";
    }

    public boolean equals(Object o){
	ComparableBlock x = (ComparableBlock)o;	    
	return compareTo(o) == 0;
    }

    public void printInstrs(){
	//for(int i = 0; i < block.getInstList().size(); i++)
	//System.out.println(block.getInstList().get(i));
    }
}
