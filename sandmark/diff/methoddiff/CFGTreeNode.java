package sandmark.diff.methoddiff;

public class CFGTreeNode {
 
    int level, value;        
    sandmark.diff.methoddiff.Tuple tuple;
    Object obj;        

    public CFGTreeNode(int level, Object o){            
        obj = o;
        tuple = new sandmark.diff.methoddiff.Tuple();      
        value = -1;
        this.level = level;            
    }              
 
    public int getValue(){
        return value;
    }

    public void setValue(int val){
        value = val;
    }

    public sandmark.diff.methoddiff.Tuple getTuple(){
        return tuple;
    }
    public void append(int n){
        tuple.add(new Integer(n));
    }  
        
    public String toString(){
        return  ((sandmark.analysis.controlflowgraph.BasicBlock)
            obj).getIH() + ":level: "+level+":"+value+":"+tuple;
    }
    public int getLevel(){
        return level;
    }
    public Object getData(){
        return obj;
    }
    public boolean equals(Object o){
        sandmark.diff.methoddiff.CFGTreeNode tn = 
            (sandmark.diff.methoddiff.CFGTreeNode)o;
        int a = ((sandmark.analysis.controlflowgraph.BasicBlock)obj).getIH().getPosition();
        int b = ((sandmark.analysis.controlflowgraph.BasicBlock)tn.getData()).
            getIH().getPosition();   
        return a == b;
    }
    public boolean hasInstructions(){
        return ((sandmark.analysis.controlflowgraph.BasicBlock)
                obj).getIH() != null;
    }
}  
