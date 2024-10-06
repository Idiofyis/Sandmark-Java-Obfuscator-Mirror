package sandmark.obfuscate.modifyifelse;


public class ModifyIfElse extends  sandmark.obfuscate.MethodObfuscator{

  org.apache.bcel.generic.ConstantPoolGen cp;


  sandmark.program.Class cg;
  sandmark.program.Method mg;
  sandmark.analysis.controlflowgraph.MethodCFG cfg;
  sandmark.analysis.controlflowgraph.BasicBlock endblk;
  sandmark.analysis.controlflowgraph.BasicBlock block;
  sandmark.analysis.controlflowgraph.BasicBlock curr;
  sandmark.analysis.controlflowgraph.BasicBlock left;
  sandmark.analysis.controlflowgraph.BasicBlock right;
  java.util.ArrayList rlist;
  java.util.ArrayList llist;
  java.util.ArrayList ldom;
  java.util.ArrayList rdom;



		public void initialize(sandmark.program.Method meth) {
        try{

                  mg = meth;
                  cfg = mg.getCFG();
                  modifyifelse();




        }catch(Exception e){e.printStackTrace(); }

        }

        public String getShortName()
    {
        return "Branch Inverter";
    }

        public sandmark.config.ModificationProperty [] getMutations() {
              return null;
    }

    public String getLongName() {
        return "Exchange the If and the Else part";
    }

    public sandmark.config.RequisiteProperty[] getPostprohibited()
    {
        return new sandmark.config.RequisiteProperty[]{
            new sandmark.config.AlgorithmProperty(this)
        };
    }

    public java.lang.String getAlgHTML()
    {
        return
            "<HTML><BODY>" +
            "ModifyIfElse is a class obfuscator." +
            " The algorithm exchanges the \"if\" and the \"else\"" +
            " part of an if-else statement. It also negates the if instruction"+
            " so that the semantics are preserved." +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href =\"mailto:kamlesh@cs.arizona.edu\">Kamlesh Kantilal</a>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }

    public java.lang.String getAlgURL(){
        return "sandmark/obfuscate/modifyifelse/doc/help.html";
    }

    public java.lang.String getAuthor(){
                        return "Kamlesh Kantilal";
        }

        public java.lang.String getAuthorEmail(){
                        return "kamlesh@cs.arizona.edu";
        }

        public java.lang.String getDescription(){
                        return "This algorithm negates the if instruction in the ifelse statement" +
                                "and exchanges the if and else part of the body";
        }




    public void apply(sandmark.program.Method meth) throws Exception
	{
	         initialize(meth);
    }


   public void display() {
      java.util.Iterator nodeIter = cfg.nodes();
      while (nodeIter.hasNext()) {
	 System.out.println("START");
	 sandmark.analysis.controlflowgraph.BasicBlock block =
	    (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter.next();
	 System.out.println(block);
	 java.util.Iterator succIter = cfg.succs(block);
	 while (succIter.hasNext()) {
	    sandmark.analysis.controlflowgraph.BasicBlock succ =
	       (sandmark.analysis.controlflowgraph.BasicBlock)succIter.next();
	    System.out.println("succ=" + succ.getIH());
	 }
	 System.out.println("END");
      }
   }

   private void prepareList() {
      rlist = new java.util.ArrayList();
      rlist.add(right);
      
      llist = new java.util.ArrayList();
      llist.add(left);

      java.util.Iterator nodeIter = cfg.nodes();
      while (nodeIter.hasNext()) {
	 block = (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter.next();
	 if (block==left||block==right||block==endblk||block==curr)
	    continue;
	 if (cfg.dominates(curr,block) && cfg.postDominates(endblk,block)) {
	    if(cfg.dominates(left,block))
	       llist.add(block);
	    if(cfg.dominates(right,block))
	       rlist.add(block);
	 }
      }
   }

   private void preparePdomList() {
      ldom = new java.util.ArrayList();
      block = cfg.getPostDominator(left);
      
      while(block!=null && block.getIH()!=null) {
	 ldom.add(block);
	 block=cfg.getPostDominator(block);
      }
      
      rdom = new java.util.ArrayList();
      block = cfg.getPostDominator(right);
      while(block!=null && block.getIH()!=null) {
	 rdom.add(block);
	 block=cfg.getPostDominator(block);
      }
   }

private int getEndBlock()
{
        endblk=null;
        int k;
              for(int i = 0; i < rdom.size(); i++)
              {         for(k=0; k<  ldom.size(); k++)
                                if(rdom.get(i)==ldom.get(k)) break;
                        if(k!=ldom.size()){
                        endblk =(sandmark.analysis.controlflowgraph.BasicBlock)rdom.get(i);
                        break;
                        }
              }
      if(endblk==null) return 0;
      return 1;
}

   private int checkList() {
      //All the predecessors & successors must be in the same list

      for(int i = 0; i < llist.size(); i++) {
	 block = (sandmark.analysis.controlflowgraph.BasicBlock)llist.get(i);
	 java.util.Iterator predIter = block.graph().preds(block);
	 while (predIter.hasNext()) {
	    java.lang.Object pred = predIter.next();
	    if (pred == curr)
	       continue;
	    if (llist.indexOf(pred) == -1)
	       return 0;
	 }
      }


      for(int i = 0; i < llist.size(); i++) {
	 block = (sandmark.analysis.controlflowgraph.BasicBlock)llist.get(i);
	 java.util.Iterator succIter = block.graph().succs(block);
	 while (succIter.hasNext()) {
	    java.lang.Object succ = succIter.next();
	    if (succ == endblk)
	       continue;
	    if (llist.indexOf(succ) == -1)
	       return 0;
	 }
      }

      for(int i = 0; i < rlist.size(); i++) {
	 block = (sandmark.analysis.controlflowgraph.BasicBlock)rlist.get(i);
	 java.util.Iterator predIter = block.graph().preds(block);
	 while (predIter.hasNext()) {
	    java.lang.Object pred = predIter.next();
	    if (pred == curr)
	       continue;
	    if (rlist.indexOf(pred) == -1)
	       return 0;
	 }
      }


      for(int i = 0; i < rlist.size(); i++) {
	 block = (sandmark.analysis.controlflowgraph.BasicBlock)rlist.get(i);
	 java.util.Iterator succIter = block.graph().succs(block);
	 while (succIter.hasNext()) {
	    java.lang.Object succ = succIter.next();
	    if (succ == endblk)
	       continue;
	    if (rlist.indexOf(succ) == -1)
	       return 0;
	 }
      }
      

      return 1;

   }



public void modifyifelse()
{
  org.apache.bcel.generic.InstructionList il=mg.getInstructionList();
  int k;
  //parse over all basic blocks

  java.util.Iterator nodeIter = cfg.nodes();
  while (nodeIter.hasNext()) {
     curr = (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter.next();
     if (cfg.numSuccs(curr) != 2) 
	continue;

     java.util.ArrayList currInstList = curr.getInstList();
     int length = currInstList.size();
     if(length==0) continue;
     org.apache.bcel.generic.InstructionHandle lastInstHandle =
        (org.apache.bcel.generic.InstructionHandle) currInstList.get(length-1);
     org.apache.bcel.generic.Instruction lastInst =
	lastInstHandle.getInstruction();

     //check if the last instruction is a if instruction else continue
     if(!(lastInst instanceof org.apache.bcel.generic.IfInstruction))
	continue;
     org.apache.bcel.generic.IfInstruction iInst =
	(org.apache.bcel.generic.IfInstruction)lastInst;
     
     org.apache.bcel.generic.InstructionHandle nextHandle =
	lastInstHandle.getNext();
     org.apache.bcel.generic.InstructionHandle thandle =
	iInst.getTarget();

     //System.out.println(" FOUND IF");

     //find the block that is the target block make it right
     block = null;
     java.util.Iterator nodeIter2 = cfg.nodes();
     while (nodeIter2.hasNext()) {
	block = (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter2.next();
	if(block.getIH() == thandle)
	   break;
     }
     right = block;

     //find the fallthrough block make it left
     block = null;
     nodeIter2 = cfg.nodes();
     while (nodeIter2.hasNext()) {
	block = (sandmark.analysis.controlflowgraph.BasicBlock)nodeIter2.next();
	if(block.getIH() == nextHandle)
	   break;
     }
     left = block;

     if(cfg.numPreds(left)!=1) continue;
     if(cfg.numPreds(right)!=1) continue;
     if(left==right) continue;
     


    //System.out.println("STEP 1");

    //System.out.println("left="+left.getIH()+"right"+right.getIH());

        preparePdomList();


    /*System.out.println("STEP 2");
    block = null;
            for(int i = 0; i < ldom.size(); i++){
               block = (sandmark.analysis.controlflowgraph.BasicBlock)ldom.get(i);
               System.out.println("L PDOM="+block.getIH());
             }
     block = null;
            for(int i = 0; i < rdom.size(); i++){
               block = (sandmark.analysis.controlflowgraph.BasicBlock)rdom.get(i);
               System.out.println("R PDOM="+block.getIH());
             }
        */

     if(getEndBlock()==0) continue;

     //System.out.println(" STEP 3 endblk="+endblk.getIH());

         prepareList();


                /*      for(int i = 0; i < llist.size(); i++){
               block = (sandmark.analysis.controlflowgraph.BasicBlock)llist.get(i);
               System.out.println("LLIST="+block.getIH());
             }
            for(int i = 0; i < rlist.size(); i++){
               block = (sandmark.analysis.controlflowgraph.BasicBlock)rlist.get(i);
               System.out.println("RLIST="+block.getIH());
             }



        System.out.println(" STEP 4");
        */

    if(checkList()==0) continue;


    //get the highest numbered block in the right list
        sandmark.analysis.controlflowgraph.BasicBlock high=null;
        k=-1;
        for(int i = 0; i < rlist.size(); i++){
                block = (sandmark.analysis.controlflowgraph.BasicBlock)rlist.get(i);
                if(block.getIH().getPosition()>k)
                {       k=block.getIH().getPosition();
                        high=block;
                }
        }
        if(high==null) continue;

        //System.out.println(" STEP 5");
    //The blocks should all be continuous

	nodeIter2 = cfg.nodes();
	while (nodeIter2.hasNext()) {
	   block=(sandmark.analysis.controlflowgraph.BasicBlock)nodeIter2.next();
	   if(block.getIH()==null) continue;
	   if(block.getIH().getPosition()>=right.getIH().getPosition()
	      &&
	      block.getIH().getPosition()<=high.getIH().getPosition())
	      
	      if(rlist.indexOf(block)==-1)
		 break;
	   
        }
        if(nodeIter2.hasNext()) continue;

        //get the highest numbered block in the left list
       sandmark.analysis.controlflowgraph.BasicBlock lhigh=null;
        k=-1;
        for(int i = 0; i < llist.size(); i++){
                block = (sandmark.analysis.controlflowgraph.BasicBlock)llist.get(i);
                if(block.getIH().getPosition()>k)
                {       k=block.getIH().getPosition();
                        lhigh=block;
                }
        }
        if(lhigh==null) continue;



        // The blocks should all be continuous
	nodeIter2 = cfg.nodes();
	while (nodeIter2.hasNext()) {
	   block=(sandmark.analysis.controlflowgraph.BasicBlock)nodeIter2.next();
	   if(block.getIH()==null) continue;
	   if(block.getIH().getPosition()>=left.getIH().getPosition()
	      &&
	      block.getIH().getPosition()<=lhigh.getIH().getPosition())
	      if(llist.indexOf(block)==-1)
		 break;
	   
        }
        if(nodeIter2.hasNext()) continue;
	org.apache.bcel.generic.InstructionHandle lendih=null;

	lendih=(org.apache.bcel.generic.InstructionHandle)
	   lhigh.getInstList().get(lhigh.getInstList().size()-1);
	
	if(lendih.getNext()!=right.getIH()) continue;
	org.apache.bcel.generic.CodeExceptionGen[] exceptions =
	   mg.getExceptionHandlers();



        org.apache.bcel.generic.InstructionHandle endih=null;
        org.apache.bcel.generic.InstructionHandle myih=null;
        org.apache.bcel.generic.GOTO  myinst;
	
        endih=(org.apache.bcel.generic.InstructionHandle)
	   high.getInstList().get(high.getInstList().size()-1);
	
	
	for(int i = 0; i < exceptions.length; i++){
	   org.apache.bcel.generic.CodeExceptionGen ceg =
	      exceptions[i];
	   org.apache.bcel.generic.InstructionHandle start =
	      ceg.getStartPC();
	   int startPos = start.getPosition();
	   org.apache.bcel.generic.InstructionHandle end =
	      ceg.getEndPC();
	   int endPos = end.getPosition();
	   if(end==endih && startPos <= curr.getIH().getPosition())
	      
	      ceg.setEndPC(lendih);
	}
	





        il.setPositions(true);
	
        il.redirectBranches(right.getIH(),left.getIH());

        il.setPositions(true);

        il.move(right.getIH(),endih,lastInstHandle);
        il.setPositions(true);

	for(int i = 0; i < rlist.size(); i++){
	   block = (sandmark.analysis.controlflowgraph.BasicBlock)rlist.get(i);
	   if(block.fallthrough()==endblk) {
	      //   System.out.println("Fallthrough="+block.getIH());
	      block.setFallthrough(null);
	      myinst=new
		 org.apache.bcel.generic.GOTO((org.apache.bcel.generic.InstructionHandle)
					      endblk.getInstList().get(0));
	      //endblk.getIH()
	      myih=new org.apache.bcel.generic.InstructionList(myinst).getStart();
	      block.addInst(myih);
	      il.append(endih,myinst);
	   }
	}


        il.setPositions(true);
	
	lastInstHandle.setInstruction
	   ( ((org.apache.bcel.generic.IfInstruction)lastInst).negate());
        lastInst=lastInstHandle.getInstruction();
	
	curr.setFallthrough(right);
        /*
	  System.out.println("target= "+
	  ((org.apache.bcel.generic.IfInstruction)lastInst).getTarget());
	  
	  System.out.println("left="+left.getIH()+"right="+right.getIH());
        */
        il.update();
	
        mg.setInstructionList(il);
	
        cfg = new sandmark.analysis.controlflowgraph.MethodCFG(mg,true);
        il=mg.getInstructionList();
        //display();
  }// End FOR
}

}

