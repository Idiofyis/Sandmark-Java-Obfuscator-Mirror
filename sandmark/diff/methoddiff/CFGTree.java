package sandmark.diff.methoddiff;

public class CFGTree extends sandmark.util.newgraph.GraphImpl {    
    
    private static final boolean DEBUG = false;   
    private static sandmark.analysis.controlflowgraph.MethodCFG cfg; //for building
    private int numLevels;

    public CFGTree(java.util.Iterator nodes, java.util.Iterator edges){
        super(nodes, edges);       
        setNumLevels();
    }

    public CFGTree(sandmark.analysis.controlflowgraph.MethodCFG cfgraph){
        cfg = cfgraph;
        sandmark.analysis.controlflowgraph.BasicBlock root = null;        
        if(cfg != null && cfg.roots().hasNext())
            root = 
	       (sandmark.analysis.controlflowgraph.BasicBlock)cfg.source();
        build(root);        
        setNumLevels();
        //if(DEBUG) System.out.println(this);
    }

    private void setNumLevels(){
        numLevels = 0;
        java.util.Iterator it = nodes();
        while(it.hasNext())
            numLevels = Math.max(numLevels,
                                 ((CFGTreeNode)it.next()).getLevel()+1);
    }
        
    //Basically convert the trimmed cfg to a newgraph.Graph            
    private void build(sandmark.analysis.controlflowgraph.BasicBlock root){
        /*if(DEBUG){
            graph.print();
            System.out.println(graph.nodeCount() + "/" + graph.edgeCount());
        }*/                              
        if(cfg == null || root == null)
            return;           
                
        CFGTreeNode tn = new CFGTreeNode(0, root); 

	java.util.Iterator succIter = cfg.succs(root);
	sandmark.analysis.controlflowgraph.BasicBlock bb = null;
        //Find the first real block
	while (succIter.hasNext() && !tn.hasInstructions()) {
	   bb = (sandmark.analysis.controlflowgraph.BasicBlock)succIter.next();
	   tn = new CFGTreeNode(0, bb);
	}
	if (tn.hasInstructions()) {
	   _addNode(tn);
	   build(bb, tn, 1);
	}       
    }              
                
    private void build
        (sandmark.analysis.controlflowgraph.BasicBlock prev, 
         CFGTreeNode prevTN, 
         int level){                       
        java.util.HashMap bb2tn = new java.util.HashMap();

	java.util.Iterator succIter = prev.graph().succs(prev);
	while (succIter.hasNext()) {
	   sandmark.analysis.controlflowgraph.BasicBlock bb =
	      (sandmark.analysis.controlflowgraph.BasicBlock)succIter.next();
	   CFGTreeNode curr = new CFGTreeNode(level, bb);

	   if (curr.hasInstructions() && !bb2tn.containsValue(curr)) {
               _addEdge(new sandmark.util.newgraph.EdgeImpl(prevTN, curr));
              _addNode(curr);	    
              bb2tn.put(bb, curr);              
	   }
	}
        java.util.Iterator keys = bb2tn.keySet().iterator();
        while(keys.hasNext()){
            sandmark.analysis.controlflowgraph.BasicBlock bb =
                (sandmark.analysis.controlflowgraph.BasicBlock)keys.next();
	   build(bb,(CFGTreeNode)bb2tn.get(bb),level+1);  
        }
    }                     
    
    public int getNumLevels(){
        return numLevels;
    }

    public int rootValue(){       
        if(roots() != null && roots().hasNext())
            return ((CFGTreeNode)roots().next()).getValue();
        else 
            return -1;
    }   

    public static boolean isomorphic(CFGTree A, CFGTree B){   
        boolean flag = mark(A,B);       
        /*if(DEBUG && A.nodeCount() == B.nodeCount()) 
            System.out.println(A +"\n" + B + "\n" +
                               A.edgeCount() + "/" + B.edgeCount() + " " +
                               A.getNumLevels() + "/" + B.getNumLevels());*/
        return flag && A.rootValue() == B.rootValue();              
    }

    private static boolean mark(CFGTree A, CFGTree B){       
        if(A.nodeCount() != B.nodeCount() ||
           A.edgeCount() != B.edgeCount() ||
           A.getNumLevels() != B.getNumLevels())
            return false;
        //Mark all leaves with a zero, make a list of leaves at last level
        java.util.LinkedList L1 = new java.util.LinkedList();
        java.util.LinkedList L2 = new java.util.LinkedList();

        java.util.Iterator it = A.nodes();
        while(it.hasNext()){
            CFGTreeNode tn = (CFGTreeNode)it.next();
            if(!A.outEdges(tn).hasNext()){
                tn.setValue(0);
                //System.out.println(tn.getLevel() + "/" + A.getNumLevels());
                if(tn.getLevel() == A.getNumLevels()-1)
                    L1.add(tn);
            }
        }
        it = B.nodes();
        while(it.hasNext()){
            CFGTreeNode tn = (CFGTreeNode)it.next();
            if(!B.outEdges(tn).hasNext()){
                tn.setValue(0);
                if(tn.getLevel() == B.getNumLevels()-1)
                    L2.add(tn);
            }
        }             
        int levels = Math.max(A.getNumLevels(), B.getNumLevels());       
        for(int i = levels-2; i >= 0; i--){                                   
            if(!assignInts(i, L1, L2,  A, B))
                return false;                     
            //if(DEBUG) System.out.println(A+"$$$\n"+B);              
        }
        return true;
    }
    
    private static boolean assignInts(int index, java.util.LinkedList L1, 
                                      java.util.LinkedList L2,
                                      CFGTree A,
                                      CFGTree B){
             
        java.util.ListIterator it = L1.listIterator(0);
        java.util.Vector S1 = new java.util.Vector();

        while(it.hasNext()){
            CFGTreeNode tn = (CFGTreeNode)it.next();          
            int value = tn.getValue();
            java.util.Iterator in = A.preds(tn); //should be 1 at most
            CFGTreeNode parent = null;
            if(in.hasNext())
                parent = (CFGTreeNode)in.next();
            else //its the root
                break;
            parent.append(value);
            S1.add(parent);
        }
        it = L2.listIterator(0);
        java.util.Vector S2 = new java.util.Vector();

        while(it.hasNext()){
            CFGTreeNode tn = (CFGTreeNode)it.next();          
            int value = tn.getValue();
            java.util.Iterator in = B.preds(tn); //should be 1 at most
            CFGTreeNode parent = null;
            if(in.hasNext())
                parent = (CFGTreeNode)in.next();
            else //its the root
                break;
            parent.append(value);
            S2.add(parent);
        }  

        //if(DEBUG) System.out.println(L1 + "\n" + L2);
        //if(DEBUG) System.out.println(S1 + "\n" + S2);
        //Compare tuples of S1 and S2
        TupleList S1tuples = new TupleList();
        TupleList S2tuples = new TupleList();
        for(int i = 0; i < S1.size(); i++)
            S1tuples.add(((CFGTreeNode)S1.get(i)).getTuple());
        for(int i = 0; i < S2.size(); i++)
            S2tuples.add(((CFGTreeNode)S2.get(i)).getTuple());
        
        if(S1tuples.compareTo(S2tuples) != 0)
            return false;
        else {
            L1.clear();
            L2.clear();
            buildLists(index, S1, S2, L1, L2, A, B);  
            return true;
        }        
    }
    
    private static void buildLists(int level,
                                   java.util.Vector S1,
                                   java.util.Vector S2,
                                   java.util.LinkedList L1,
                                   java.util.LinkedList L2,
                                   CFGTree A,
                                   CFGTree B){      
        if((S1 == null || S1.size() == 0) ||
           (S2 == null || S2.size() == 0))
            return;          
        //Set the values and update L1
        int ctr = 1;
        CFGTreeNode last = (CFGTreeNode)S1.firstElement();                
        L1.add(last);
        last.setValue(ctr);
        for(int k = 1; k < S1.size(); k++){
            CFGTreeNode curr = (CFGTreeNode)S1.get(k);            
            if(curr.getTuple().compareTo(last.getTuple()) != 0)
                ctr++;
            curr.setValue(ctr);          
            L1.add(curr);
            last = curr;
        }       
        //Add all the leaves from Level level to L
        java.util.Iterator it = A.nodes();
        while(it.hasNext()){
            CFGTreeNode tn = (CFGTreeNode)it.next();
            if(!A.outEdges(tn).hasNext() &&
               tn.getLevel() == level)
                L1.addFirst(tn);
        }     
        //Do the same for L2
        ctr = 1;
        last = (CFGTreeNode)S2.firstElement();        
        L2.add(last);
        last.setValue(ctr);
        for(int k = 1; k < S2.size(); k++){
            CFGTreeNode curr = (CFGTreeNode)S2.get(k);            
            if(curr.getTuple().compareTo(last.getTuple()) != 0)
                ctr++;
            curr.setValue(ctr);
            L2.add(curr);
            last = curr;
        }               
        //Add all the leaves from Level level to L
        it = B.nodes();
        while(it.hasNext()){
            CFGTreeNode tn = (CFGTreeNode)it.next();
            if(!B.outEdges(tn).hasNext() &&
               tn.getLevel() == level)
                L2.addFirst(tn);        
        }     
    }       

    public CFGTree getSubtree(Object o){
        java.util.HashMap map = new java.util.HashMap();
        java.util.Iterator it1 = depthFirst(o);
        int rootLevel = ((CFGTreeNode)o).getLevel();
        //Make a bunch of new nodes and put them in a map
        while(it1.hasNext()){
            CFGTreeNode node = (CFGTreeNode)it1.next();
            int level = node.getLevel()-rootLevel;
            map.put(node, new CFGTreeNode(level, node.getData()));
        }
        it1 = map.keySet().iterator();
        //get all the edges from the nodes and copy into vector
        java.util.Vector edges = new java.util.Vector();
        while(it1.hasNext()){
            Object node  = it1.next();
            java.util.Iterator it2 = outEdges(node);
            while(it2.hasNext()){
                sandmark.util.newgraph.Edge edge = 
                    (sandmark.util.newgraph.Edge)it2.next();
                edges.add(new sandmark.util.newgraph.EdgeImpl
                          (map.get(edge.sourceNode()),
                           map.get(edge.sinkNode())));
            }
        }
        //now the map has all the nodes, put them in a Collection
        return new CFGTree(map.values().iterator(), edges.iterator());
    }         

    public boolean contains(sandmark.analysis.controlflowgraph.BasicBlock bb){
        //Compares the block id's, level doesn't matter
        java.util.Iterator it = nodes();
        while(it.hasNext())
            if(it.next().equals(new CFGTreeNode(-1,bb)))
                return true;
        return false;
        //return hasNode(new CFGTreeNode(-1, bb));
    }

    public String toString(){        
        return toString(false);   
    }
    
    public String toString(boolean printEdges){
        return toString(roots().next(), printEdges);
    }
    
    public String toString(Object root, boolean printEdges){        
        String s = "";
        java.util.Iterator it = depthFirst(root);        
        while(it.hasNext()){
            Object o = it.next();
            s += o + "\n";     
            if(printEdges){                   
                java.util.Iterator it2 = succs(o);
                while(it2.hasNext())
                    s += "\t->" + it2.next() +"\n";
            }
        }
        return s;
    }
}
