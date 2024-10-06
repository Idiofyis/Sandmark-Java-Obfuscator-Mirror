package sandmark.obfuscate.buggycode;

public class BugCode extends sandmark.obfuscate.MethodObfuscator {
   private static final boolean DEBUG = false;
   private static final boolean USE_OP_LIB = true;

   /** Tests to see if any instruction in this BB is the target of a 
    *  CodeExceptionGen. (i.e. if this BB spans the start or end of an 
    *  exception block boundary)
    */
   private boolean containsException(sandmark.analysis.controlflowgraph.BasicBlock blk) {
      for(int i = 0 ; i < blk.getInstList().size() ; i++) {
         org.apache.bcel.generic.InstructionHandle ih =
            (org.apache.bcel.generic.InstructionHandle)blk.getInstList().get(i);
         org.apache.bcel.generic.InstructionTargeter targeters[] =
            ih.getTargeters();
         for(int j = 0 ; targeters != null && j < targeters.length ; j++)
            if(targeters[j] instanceof org.apache.bcel.generic.CodeExceptionGen)
               return true;
      }
	    
      return false;
   }

   /** Applies the buggy code obfuscation. The 'bug' in question is just to 
    *  randomly change the value of an integer local variable. If no integer
    *  locals exist, we add one.
    */
   public void apply(sandmark.program.Method mObj) throws Exception {
      if(mObj.getInstructionList()==null || mObj.getInstructionList().size() == 0) 
         return;

      org.apache.bcel.generic.InstructionList ilist = mObj.getInstructionList();
      sandmark.analysis.controlflowgraph.MethodCFG mcfg = mObj.getCFG();
      sandmark.analysis.controlflowgraph.BasicBlock bb = chooseBB(mObj, mcfg);

      if(bb==null)
         return;

      sandmark.analysis.liveness.Liveness liveness = 
         new sandmark.analysis.liveness.Liveness(mObj);
      sandmark.analysis.defuse.ReachingDefs rd = 
         new sandmark.analysis.defuse.ReachingDefs(mObj);

      int targetVar = -1;
      {// select a local INT variable, store its index in targetVar
         sandmark.analysis.defuse.DUWeb webs[] = rd.defUseWebs();
         for(int j = 0 ; j < webs.length ; j++) {
            if(!liveness.liveAt(webs[j],bb.getIH()))
               continue;
			
            boolean allIntWeb = true;
            for(java.util.Iterator defs = webs[j].defs().iterator(); defs.hasNext() ; )
               if(!((sandmark.analysis.defuse.DefWrapper)defs.next()).getType().equals(org.apache.bcel.generic.Type.INT))
                  allIntWeb = false;
			
            if(allIntWeb) {
               targetVar = webs[j].getIndex();
               break;
            }
         }
      }


      /////////////////////////////////////////////////
      // make testBlock just an opaquely true jump to bb, and redirect
      // all the targeters of bb's first instruction to testBlock.
      // the fallthrough block of the opaquely true IFEQ will be buggy code >:-)

      // testBlock will consist of 'nop, ifeq' and will have stuff 
      // inserted into it later with opaque predicates

      sandmark.analysis.controlflowgraph.BasicBlock testBlock =
         new sandmark.analysis.controlflowgraph.BasicBlock(mcfg);
      mcfg.addNode(testBlock);

      org.apache.bcel.generic.InstructionHandle nophandle = 
         ilist.append(new org.apache.bcel.generic.NOP());
      org.apache.bcel.generic.InstructionHandle ifhandle = 
         ilist.append(new org.apache.bcel.generic.IFEQ(bb.getIH()));
      
      testBlock.addInst(nophandle);
      org.apache.bcel.generic.InstructionHandle pushPredicateValue =
	  ilist.append(org.apache.bcel.generic.InstructionConstants.ICONST_0);
      testBlock.addInst(pushPredicateValue);
      testBlock.addInst(ifhandle);

      {// reset the all branch targeters of this bb to be the new jump
         org.apache.bcel.generic.InstructionTargeter targeters[] =
            bb.getIH().getTargeters();
         for(int i = 0 ; targeters != null && i < targeters.length ; i++)
            targeters[i].updateTarget(bb.getIH(),nophandle);

         ((org.apache.bcel.generic.IFEQ)ifhandle.getInstruction()).setTarget(bb.getIH());
      }
      ////////////////////////////////////////////

      if(targetVar==-1)
         targetVar = initializeNextLocal(mObj,testBlock);
      // this has to be after the def'n of testBlock


      sandmark.analysis.controlflowgraph.BasicBlock copiedBlock = 
         makeCopiedBlock(mObj, mcfg, bb, testBlock);


      /* insert a bug code in the new block copy */
      {
         org.apache.bcel.generic.InstructionHandle ih = copiedBlock.getIH();
         int size = copiedBlock.getInstList().size();
         org.apache.bcel.generic.InstructionHandle[] handles=null;
         
         for (int i=size-1;i>=0;i--){
            handles = getRandomBug(ilist, targetVar);
            for (int j=handles.length-1;j>=0;j--){
               copiedBlock.getInstList().add(i, handles[j]);
            }
         }

         org.apache.bcel.generic.InstructionTargeter targeters[] =
            ih.getTargeters();
         for(int i = 0 ; targeters != null && i < targeters.length ; i++)
            targeters[i].updateTarget(ih,copiedBlock.getIH());
      }

      // cleanup
      mcfg.rewriteInstructionList();
      ilist.setPositions(true);
      mObj.mark();
      mObj.removeLocalVariables();
      mObj.removeLineNumbers();

      if (USE_OP_LIB){
	  sandmark.util.opaquepredicatelib.PredicateFactory predicates[] =
	      sandmark.util.opaquepredicatelib.OpaqueManager.getPredicatesByValue
	      (sandmark.util.opaquepredicatelib.OpaqueManager.PV_FALSE);
	  java.util.HashSet badPreds = new java.util.HashSet();
	  sandmark.util.opaquepredicatelib.OpaquePredicateGenerator predicate = null;
	  while(predicate == null && badPreds.size() != predicates.length) {
	      int which = sandmark.util.Random.getRandom().nextInt() % predicates.length;
	      if(which < 0)
		  which += predicates.length;
	      predicate = predicates[which].createInstance();
	      if(!predicate.canInsertPredicate
		 (mObj,pushPredicateValue,
		  sandmark.util.opaquepredicatelib.OpaqueManager.PV_FALSE)) {
		  badPreds.add(predicates[which]);
		  predicate = null;
	      }
	  }
	  predicate.insertPredicate
	      (mObj,pushPredicateValue,
	       sandmark.util.opaquepredicatelib.OpaqueManager.PV_FALSE);
	  pushPredicateValue.setInstruction(new org.apache.bcel.generic.NOP());
      }
   }


   // returns an instruction list that performs a random manipulation on an integer local.
   // each one will load it, mess with it, then store it.
   private org.apache.bcel.generic.InstructionHandle[] getRandomBug(org.apache.bcel.generic.InstructionList ilist, 
                                                                    int localindex){
      final int CHOICES = 4;
      java.util.Random random = sandmark.util.Random.getRandom();

      int which = random.nextInt() % CHOICES;
      which = (which+CHOICES)%CHOICES;

      org.apache.bcel.generic.InstructionHandle[] handles = null;

      switch(which){
      case 0:
         handles = new org.apache.bcel.generic.InstructionHandle[4];
         handles[0] = ilist.append(new org.apache.bcel.generic.ILOAD(localindex));
         handles[1] = ilist.append(new org.apache.bcel.generic.ICONST(((random.nextInt()%7)+7)%7 - 1));
         handles[2] = ilist.append(new org.apache.bcel.generic.IMUL());
         handles[3] = ilist.append(new org.apache.bcel.generic.ISTORE(localindex));
         return handles;

      case 1:
         handles = new org.apache.bcel.generic.InstructionHandle[4];
         handles[0] = ilist.append(new org.apache.bcel.generic.ILOAD(localindex));
         handles[1] = ilist.append(new org.apache.bcel.generic.ICONST(((random.nextInt()%7)+7)%7 - 1));
         handles[2] = ilist.append(new org.apache.bcel.generic.IADD());
         handles[3] = ilist.append(new org.apache.bcel.generic.ISTORE(localindex));
         return handles;

      case 2:
         handles = new org.apache.bcel.generic.InstructionHandle[4];
         handles[0] = ilist.append(new org.apache.bcel.generic.ILOAD(localindex));
         handles[1] = ilist.append(new org.apache.bcel.generic.ICONST(((random.nextInt()%7)+7)%7 - 1));
         handles[2] = ilist.append(new org.apache.bcel.generic.ISUB());
         handles[3] = ilist.append(new org.apache.bcel.generic.ISTORE(localindex));
         return handles;

      case 3:
         handles = new org.apache.bcel.generic.InstructionHandle[4];
         handles[0] = ilist.append(new org.apache.bcel.generic.ILOAD(localindex));
         handles[1] = ilist.append(new org.apache.bcel.generic.ICONST(((random.nextInt()%7)+7)%7 - 1));
         handles[2] = ilist.append(new org.apache.bcel.generic.IDIV());
         handles[3] = ilist.append(new org.apache.bcel.generic.ISTORE(localindex));
         return handles;
      }
      return null;
   }



   private sandmark.analysis.controlflowgraph.BasicBlock chooseBB(sandmark.program.Method mObj, 
                                                                  sandmark.analysis.controlflowgraph.MethodCFG mcfg){
      // search for a bb with the following properties:
      // 1. not in a subroutine (doesn't end in RET)
      // 2. not inside an exception handler
      // 3. does not end with an uninitialized object on top of the stack

      java.util.ArrayList blockList = new java.util.ArrayList();
      for(java.util.Iterator iter = mcfg.basicBlockIterator(); iter.hasNext() ; )
         blockList.add(iter.next());
      
      sandmark.analysis.stacksimulator.StackSimulator ss =
         new sandmark.analysis.stacksimulator.StackSimulator(mObj);
      sandmark.analysis.controlflowgraph.BasicBlock bb = null;

      while(bb == null && blockList.size() > 0) {
         int ndx = getRandomValue(blockList.size());
         bb = (sandmark.analysis.controlflowgraph.BasicBlock)blockList.get(ndx);
         if(bb.getLastInstruction().getInstruction() instanceof org.apache.bcel.generic.RET) {
            bb = null;
            blockList.remove(ndx);
            continue;
         }
         if(containsException(bb)) {
            if(DEBUG) System.out.println(" block splits exception range ...");
            bb = null;
            blockList.remove(ndx);
            continue;
         }
         sandmark.analysis.stacksimulator.Context afterLast =
            ss.getInstructionContext(bb.getLastInstruction(),false);
         for(int i = 0 ; i < afterLast.getStackSize() ; i++){
            
            // should this also check getStackAt(i)[j] for all j?
            
            if(afterLast.getStackAt(i)[0].getType() instanceof 
               org.apache.bcel.verifier.structurals.UninitializedObjectType) {
               bb = null;
               blockList.remove(ndx);
               break;
            }
         }
      }
      return bb;
   }


   /** This method creates the bogus buggy fallthrough block, and updates the CFG accordingly.
    *  @returns the new copied buggy block
    */
   private sandmark.analysis.controlflowgraph.BasicBlock 
      makeCopiedBlock(sandmark.program.Method mObj,
                      sandmark.analysis.controlflowgraph.MethodCFG mcfg,
                      sandmark.analysis.controlflowgraph.BasicBlock bb,
                      sandmark.analysis.controlflowgraph.BasicBlock testBlock){
      
      org.apache.bcel.generic.InstructionList ilist = mObj.getInstructionList();

      /* else, fall through for the opaque branch will be the new bug block 
       * create the new block and insert it above the 'start_ih' ie. original block 
       */

      sandmark.analysis.controlflowgraph.BasicBlock copiedBlock =
         new sandmark.analysis.controlflowgraph.BasicBlock(mcfg);
      mcfg.addNode(copiedBlock);
      // copy the contents of bb's instruction list into copiedBlock
      {
         java.util.ArrayList alist = bb.getInstList();
         for(int k = 0 ; k < alist.size() ; k++) {
            org.apache.bcel.generic.InstructionHandle ih = 
               (org.apache.bcel.generic.InstructionHandle)alist.get(k);
            // insert the same instruction for new block;
            if(ih.getInstruction() instanceof org.apache.bcel.generic.BranchInstruction) {
               org.apache.bcel.generic.BranchInstruction orig =
                  (org.apache.bcel.generic.BranchInstruction)ih.getInstruction();
               org.apache.bcel.generic.BranchInstruction newInstr =
                  (org.apache.bcel.generic.BranchInstruction)orig.copy();
               newInstr.setTarget(orig.getTarget()); // redundant?
               copiedBlock.addInst(ilist.append(newInstr));
            } else
               copiedBlock.addInst(ilist.append(ih.getInstruction().copy()));
         }
      }
      
      
      // get the fall through instruction handle (if any) 
      // for the target basic block
      org.apache.bcel.generic.InstructionHandle fallthrough_ih = 
         bb.getLastInstruction().getNext();
      if(fallthrough_ih != null && !mcfg.edgeIsFallthrough(bb,mcfg.getBlock(fallthrough_ih)))
         fallthrough_ih = null;

      // if bb had a fallthrough, add a block after bb to GOTO it
      sandmark.analysis.controlflowgraph.BasicBlock fallthroughBlock = null;
      if(fallthrough_ih != null) {
         fallthroughBlock = 
            new sandmark.analysis.controlflowgraph.BasicBlock(mcfg);
         fallthroughBlock.addInst(ilist.append(new org.apache.bcel.generic.GOTO(fallthrough_ih)));
      }

      
      if(bb.fallthroughFrom() != null)
         bb.fallthroughFrom().setFallthrough(testBlock);
      testBlock.setFallthrough(copiedBlock);
      if(fallthroughBlock == null)
         copiedBlock.setFallthrough(bb);
      else {
         copiedBlock.setFallthrough(fallthroughBlock);
         fallthroughBlock.setFallthrough(bb);
      }

      for(java.util.Iterator preds = mcfg.preds(bb) ; preds.hasNext() ; ) {
         sandmark.analysis.controlflowgraph.BasicBlock pred = 
            (sandmark.analysis.controlflowgraph.BasicBlock)preds.next();
         mcfg.removeEdge(pred,bb);
         if(pred.fallthrough() == testBlock)
            mcfg.addEdge
               (new sandmark.analysis.controlflowgraph.FallthroughEdge
                (pred,testBlock));
         else
            mcfg.addEdge(pred,testBlock);
      }
      mcfg.addEdge(testBlock,bb);
      
      mcfg.addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge
                   (testBlock,copiedBlock));
      if(fallthroughBlock != null) {
         mcfg.addEdge(new sandmark.analysis.controlflowgraph.FallthroughEdge
                      (copiedBlock,fallthroughBlock));
         mcfg.addEdge(fallthroughBlock,mcfg.getBlock(fallthrough_ih));
      }

      for(java.util.Iterator edges = mcfg.outEdges(bb) ; edges.hasNext() ; ) {
         sandmark.util.newgraph.Edge edge =
            (sandmark.util.newgraph.Edge)edges.next();
         if(edge instanceof sandmark.analysis.controlflowgraph.FallthroughEdge)
            continue;
         mcfg.addEdge(copiedBlock,edge.sinkNode());
      }

      return copiedBlock;
   }


   // returns a random value in the range 0--(high-1)
   private int getRandomValue(int high) {
      if( high == 0 )
         return 0;
         
      java.util.Random rNum = sandmark.util.Random.getRandom();
      int rVal = rNum.nextInt() % high;
      if ( rVal < 0 )
         rVal += high;

      return rVal;
   }


   /** This method initializes the next local var (at index method.getMaxLocals())
    *  to be a random integer value. The code to do that will be inserted at the 
    *  start of the given basic block.
    */
   private int initializeNextLocal
      (sandmark.program.Method mg,
       sandmark.analysis.controlflowgraph.BasicBlock bb) {
      if(DEBUG) System.out.println(" \n In method createLocalVars() ... \n\n");

      mg.setMaxLocals();
      int localnum = mg.getMaxLocals();
      
      org.apache.bcel.generic.InstructionHandle startIH = bb.getIH();

      int initVal = getRandomValue(6) - 1;

      bb.getInstList().add
         (0,mg.getInstructionList().append
          (new org.apache.bcel.generic.ICONST(initVal)));
      bb.getInstList().add
         (1,mg.getInstructionList().append
          (new org.apache.bcel.generic.ISTORE(localnum)));

      org.apache.bcel.generic.InstructionTargeter targeters[] = 
         startIH.getTargeters();
      for(int i = 0 ; targeters != null && i < targeters.length ; i++)
         targeters[i].updateTarget(startIH,bb.getIH());

      return localnum;
   }

   public String getShortName() { return "Buggy Code"; }

   public String getLongName() {
      return "Insert a buggy code in the control flow graph";
   }

   public java.lang.String getAlgHTML() {
      return "<HTML><BODY>" +
         "BuggyCode is an obfuscation algorithm which "+
         "makes a copy of a basic block and introduces "+
         "a bug in it. This code is never executed as it " +
         "is jumped over by an opaque predicate.\n"+
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:tapas@cs.arizona.edu\">Tapas Sahoo</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }
    
   public java.lang.String getAlgURL() {
      return "sandmark/obfuscate/buggycode/doc/help.html";
   }

   public java.lang.String getAuthor() { return "Tapas Sahoo"; }

   public java.lang.String getAuthorEmail() { return "tapas@cs.arizona.edu"; }

   public java.lang.String getDescription() {
      return "This algorithm makes a copy of a" +
         "basic block of code and introduces " +
         "a bug in it. The buggy code is present "+
         "to confuse the attacker, but is never " +
         "executed but jumped over by means of an "+
         "opaque predicate.";
   }

   /**
      Returns a list of the changes that this algorithm makes to the code.
   */
   public sandmark.config.ModificationProperty[] getMutations() {
      return mutations;
   }

   private static final sandmark.config.ModificationProperty[] mutations =
      new sandmark.config.ModificationProperty[]{
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
         sandmark.config.ModificationProperty.I_ADD_LOCAL_VARIABLES,
         sandmark.config.ModificationProperty.PERFORMANCE_DEGRADE_LOW
      };

   private static final sandmark.config.RequisiteProperty[] preprohib =
      new sandmark.config.RequisiteProperty[]{
         sandmark.config.ModificationProperty.I_REORDER_INSTRUCTIONS
      };
   public sandmark.config.RequisiteProperty[] getPreprohibited() {
      return preprohib;
   }


            

   public static void main(String[] args) throws Exception {
      if (args.length < 1) {
         System.out.println("Usage: BuggyCode <JAR FILE>.jar");
         System.exit(1);
      }

      sandmark.program.Application app = 
         new sandmark.program.Application(args[0]);
      sandmark.obfuscate.buggycode.BugCode obfuscator =
         new sandmark.obfuscate.buggycode.BugCode();
      for(java.util.Iterator classes = app.classes() ; classes.hasNext() ; ) {
         sandmark.program.Class clazz = (sandmark.program.Class)classes.next();
         for(java.util.Iterator methods = clazz.methods(); methods.hasNext() ; ) 
            obfuscator.apply(((sandmark.program.Method)methods.next()));
      }
      String outputJar = args[0].substring(0, args[0].indexOf(".jar"));
      outputJar += "_obf.jar";
      if(DEBUG) System.out.println("outputJar -> " + outputJar);
      app.save(outputJar);
   }
}


