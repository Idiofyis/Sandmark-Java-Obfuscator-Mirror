package sandmark.analysis.initialized;

public class Initialized {
    sandmark.analysis.defuse.ReachingDefs mRD;
   public Initialized(sandmark.program.Method method) {
       mRD = new sandmark.analysis.defuse.ReachingDefs(method,true);
   }
    public Initialized(sandmark.analysis.defuse.ReachingDefs rd) {
       if(!rd.findUninitializedVars())
          throw new Error("inappropriate ReachingDefs object");
	mRD = rd;
    }
   public boolean initializedAt
      (int lvnum,org.apache.bcel.generic.InstructionHandle ih) {
       java.util.Set defs = mRD.defs(lvnum,ih);
       java.util.Iterator it = defs.iterator();

       if(!it.hasNext())
	   return false;

       while(it.hasNext())
	   if(it.next() instanceof 
	      sandmark.analysis.defuse.UninitializedDefWrapper)
	       return false;

       return true;
   }
    public static void main(String argv[]) throws Exception {
	sandmark.program.Application app = 
	    new sandmark.program.Application(argv[0]);
	for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
	    sandmark.program.Class clazz = 
		(sandmark.program.Class)classes.next();
	    for(java.util.Iterator methods = clazz.methods() ; 
		methods.hasNext() ; ) {
		sandmark.program.Method method = 
		    (sandmark.program.Method)methods.next();
		if(method.getInstructionList() == null)
		    continue;
		System.out.println(clazz.getName() + "::" + method.getName());
		Initialized l = new Initialized(method);
                for(org.apache.bcel.generic.InstructionHandle ih =
                       method.getInstructionList().getStart() ; 
                    ih != null ; ih = ih.getNext()) {
		    String initted = "";
                   for(int i = 0,lvs = method.getMaxLocals() ; i < lvs ; i++)
                      initted += l.initializedAt(i,ih) ? 1 : 0;
		   System.out.println(ih + " " + initted);
		}
	    }
	}
    }
}
