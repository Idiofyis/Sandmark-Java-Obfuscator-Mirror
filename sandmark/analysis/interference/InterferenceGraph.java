package sandmark.analysis.interference;

public class InterferenceGraph extends sandmark.util.newgraph.MutableGraph {
    public InterferenceGraph(sandmark.program.Method method) {
	sandmark.analysis.defuse.ReachingDefs rds = 
	    new sandmark.analysis.defuse.ReachingDefs(method);
	sandmark.analysis.liveness.Liveness liveness = 
	    new sandmark.analysis.liveness.Liveness(method);
	sandmark.analysis.defuse.DUWeb webs[] = rds.defUseWebs();
	for(int i = 0 ; i < webs.length ; i++)
	    addNode(webs[i]);
	for(int i = 0 ; i < webs.length ; i++)
	    for(int j = 0 ; j < webs.length ; j++) {
            if(j == i)
               continue;
            
		for(java.util.Iterator defs = webs[i].defs().iterator() ; 
		    defs.hasNext() ; ) {
		    sandmark.analysis.defuse.DefWrapper def =
			(sandmark.analysis.defuse.DefWrapper)defs.next();
		    org.apache.bcel.generic.InstructionHandle ih = null;
		    if(def instanceof
		       sandmark.analysis.defuse.InstructionDefWrapper)
			ih = ((sandmark.analysis.defuse.InstructionDefWrapper)
			      def).getIH();
		    else
			ih = method.getInstructionList().getStart();
		    if(liveness.liveAt(webs[j],ih)) {
			if(!hasEdge(webs[i],webs[j]))
			    addEdge(webs[i],webs[j]);
			if(!hasEdge(webs[j],webs[i]))
			    addEdge(webs[j],webs[i]);
		    }
		}
	    }
    }
    public static void main(String argv[]) throws Exception {
	sandmark.program.Application app =
	    new sandmark.program.Application(argv[0]);
	for(java.util.Iterator it = app.classes() ; it.hasNext() ; ) {
	    for(java.util.Iterator it2 = 
		    ((sandmark.program.Class)it.next()).methods() ; 
		it2.hasNext() ; ) {
		sandmark.program.Method method = 
		    (sandmark.program.Method)it2.next();
		method.getIFG();
	    }
	}
	    
    }
}
