package sandmark.analysis.controlflowgraph;

public class RegisterAllocator {

   sandmark.analysis.interference.InterferenceGraph ig;
   public static boolean DEBUG = false;

   public RegisterAllocator
       (sandmark.analysis.interference.InterferenceGraph ig) {
      this.ig = ig;
   }

   public void allocate(boolean fullAllocation) {
       java.util.ArrayList nodes = new java.util.ArrayList();
       for(java.util.Iterator nodeIt = ig.nodes() ; nodeIt.hasNext() ; )
	   nodes.add(nodeIt.next());
       java.util.Collections.sort(nodes);

      java.util.Hashtable colors = colorGraph(nodes,fullAllocation);

      assignLocalVariables(nodes,colors);

   }

   private java.util.Hashtable colorGraph(java.util.ArrayList nodes,
                                          boolean fullAllocation){
       java.util.Hashtable webToColor = new java.util.Hashtable();

      java.util.ArrayList preColored = preColor(nodes,webToColor);

      //create a list of uncolored nodes
      java.util.ArrayList uncolored = new java.util.ArrayList(nodes);
      uncolored.removeAll(preColored);

      for(java.util.Iterator nodeIt = uncolored.iterator() ; 
          nodeIt.hasNext() ; ) {
         sandmark.analysis.defuse.DUWeb web = 
	     (sandmark.analysis.defuse.DUWeb) nodeIt.next();
         //System.out.println("the node is: " + n);

         //make sure the node has not yet been colored
         if(webToColor.get(web) != null)
	     throw new RuntimeException("assertion failed");

         //keep track of which colors have been assigned to the nodes
         //conflicting with the node
         java.util.BitSet used = new java.util.BitSet();

	 for(java.util.Iterator it = ig.succs(web) ; it.hasNext() ; ) {
	     sandmark.analysis.defuse.DUWeb succWeb =
		 (sandmark.analysis.defuse.DUWeb)it.next();

	     Integer color = (Integer)webToColor.get(succWeb);
            if(color != null){
               used.set(color.intValue());
               //System.out.println("the color " + s.color + " has been used");
	       if(isWide(succWeb)){
		   used.set(color.intValue() + 1);
	       }
            }

         }

         if(!fullAllocation) {
            boolean skipAllocation = false;
            if(isWide(web) && !used.get(web.getIndex() + 1) && 
               !used.get(web.getIndex()))
               skipAllocation = true;
            if(!isWide(web) && !used.get(web.getIndex()))
               skipAllocation = true;
            if(skipAllocation) {
               webToColor.put(web,new Integer(web.getIndex()));
               //System.out.println("skipping a node because " + web.getIndex() + " is free");
               continue;
            }
         }

         //Find the next available color
         for(int i=0; !webToColor.containsKey(web) ; i++){
            if(! used.get(i)){
               if(isWide(web)){
                  //Wide variables need two colors
                  if(! used.get(i+1)){
		      webToColor.put(web,new Integer(i));
                     //System.out.println("color selected: " + i);
                  }
               }else{

		   webToColor.put(web,new Integer(i));
                  //System.out.println("color selected: " + i);
                  used.set(i);
               }
            }
         }
      }
      return webToColor;
   }

   //we must precolor the nodes that cannot move to another register
   private java.util.ArrayList preColor(java.util.ArrayList nodes, 
					java.util.Hashtable colors) {
      java.util.ArrayList precolored = new java.util.ArrayList();
      
      for(java.util.Iterator it = nodes.iterator() ; it.hasNext() ; ) {
	  sandmark.analysis.defuse.DUWeb web = 
	      (sandmark.analysis.defuse.DUWeb)it.next();
         if(!isMovable(web)) {
	     colors.put(web,new Integer(web.getIndex()));
            precolored.add(web);
         }
      }

      return precolored;
   }

    private boolean isMovable(sandmark.analysis.defuse.DUWeb web) {
	for(java.util.Iterator it = web.defs().iterator() ;
	    it.hasNext() ; )
	    if(!(it.next() instanceof 
		 sandmark.analysis.defuse.InstructionDefWrapper))
		return false;
	return true;
    }

    private boolean isWide(sandmark.analysis.defuse.DUWeb web) {
	if(web.defs().size() == 0)
	    return true;
	for(java.util.Iterator it = web.defs().iterator() ; it.hasNext() ; ) {
	    sandmark.analysis.defuse.DefWrapper def =
		(sandmark.analysis.defuse.DefWrapper)it.next();
	    if(def.getWidth() == 2) {
		System.out.println(def + " is wide");
		return true;
	    }
	}
	return false;
    }


   private void assignLocalVariables(java.util.ArrayList nodes,
				     java.util.Hashtable colors){
       for(java.util.Iterator it = nodes.iterator() ; it.hasNext() ; ) {
	   sandmark.analysis.defuse.DUWeb web = 
	       (sandmark.analysis.defuse.DUWeb)it.next();
	   int newIndex = ((Integer)colors.get(web)).intValue();
	   if(web.getIndex() != newIndex) {
              if(DEBUG)
                 System.out.println("changing index from " + web.getIndex() + 
                                    " to " + newIndex + " for " + web);
	       web.setIndex(newIndex);
               if(DEBUG)
                  System.out.println("result: " + web);
	   }
       }
   }

    public static void main(String argv[]) throws Exception {
	sandmark.program.Application app = new sandmark.program.Application(argv[0]);
	for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; )
	    for(java.util.Iterator methods = ((sandmark.program.Class)classes.next()).methods() ; 
		methods.hasNext() ; ) {
		sandmark.program.Method method = (sandmark.program.Method)methods.next();
		if(method.getInstructionList() == null)
		    continue;
		sandmark.analysis.interference.InterferenceGraph ig =
		    new sandmark.analysis.interference.InterferenceGraph
		    (method);
		new RegisterAllocator(ig);
		System.out.println(method.getInstructionList());
	    }

        app.save(argv[1]);
    }
}//end RegisterAllocator

