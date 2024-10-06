package sandmark.analysis.slicingtools;

public class ForwardMethodSlice extends MethodSlice {

   boolean DEBUG =false ;
   boolean DEBUG_GEN = false;
   boolean DEBUG_KILL = false;
   boolean DEBUG_USES = false;
   boolean DEBUG_CLOSURE = false;
   boolean DEBUG_SORT = false;

   /**
    * Constructor.
    */
   public ForwardMethodSlice(sandmark.program.Method method,
      org.apache.bcel.generic.InstructionHandle startHandle,
      boolean staticSlice)
      throws RuntimeException {
   
      super(method, startHandle);
      if(staticSlice)
         computeStaticSlice();
      else
         computeDynamicSlice();

            
   }


   //PRIVATE METHODS

   /**
    * Computes the static slice of a method starting at the startHandle.
    */
   protected void computeStaticSlice(){

      for(int i=0; i < analysisBlockList.size(); i++){
         computeGen((AnalysisBlock)analysisBlockList.get(i));
         computeKill((AnalysisBlock)analysisBlockList.get(i));
      }

      computeReachableUses();
      computeTransitiveClosure();
      setSlice();
      computeAffectedVars();
   }

   /**
    * Computes the dynamic slice of a method starting at the startHandle.
    */
   protected void computeDynamicSlice(){

      //needs to throw a not implemented exception.
      throw new RuntimeException("Method not yet implemented");
   }

   /**
    * Computes the gen of a basic block. The gen is the set of all uses in the
    * block.
    */
   protected void computeGen(AnalysisBlock aBlock){ 

      if(DEBUG_GEN)System.out.println("computing gen of " + aBlock.getBB().getIH());
      java.util.ArrayList genList = new java.util.ArrayList();
      java.util.ArrayList etList = aBlock.getExprTrees();
      if(DEBUG_GEN)System.out.println("etList size: " + etList.size());
      if(etList != null){
         for(int i=0; i < etList.size(); i++){
            sandmark.util.newexprtree.ExprTree exprTree =
               (sandmark.util.newexprtree.ExprTree)etList.get(i);
            java.util.ArrayList useList = exprTree.getUses();
            if(DEBUG_GEN)System.out.println("useList size: " + useList.size());
            FOUND_USE:
            for(int j=0; j < useList.size(); j++){
               org.apache.bcel.generic.InstructionHandle use =
                  (org.apache.bcel.generic.InstructionHandle)useList.get(j);
               org.apache.bcel.generic.Instruction inst = use.getInstruction();
               if(DEBUG_GEN)System.out.println("use: " + inst);
               if(inst instanceof org.apache.bcel.generic.LocalVariableInstruction){
                  if(DEBUG_GEN)System.out.println("instance of LV");
                     genList.add(exprTree);
                     if(DEBUG_GEN)System.out.println("added tree");
                     break FOUND_USE;
               }//end if
            }//end for
         }//end for

      }//end if
      
      aBlock.setGen(genList);
      if(DEBUG_GEN)System.out.println("aBlock gen: " + aBlock.getGen());
   }//end computeGen

   /**
    * Computes the kill of a basic block. The kill is the set of all uses that
    * are killed by the block.
    */
   protected void computeKill(AnalysisBlock aBlock){

      if(DEBUG_KILL)System.out.println("computing kill of " + aBlock.getBB().getIH());
      java.util.ArrayList killList = new java.util.ArrayList();
      java.util.ArrayList etList = aBlock.getExprTrees();
      if(DEBUG_KILL)System.out.println("etList size: " + etList.size());
      if(etList != null){
         for(int i=0; i < etList.size(); i++){
            sandmark.util.newexprtree.ExprTree exprTree =
               (sandmark.util.newexprtree.ExprTree)etList.get(i);
            java.util.ArrayList useList = exprTree.getUses();
            for(int j=0; j < useList.size(); j++){
               org.apache.bcel.generic.InstructionHandle useHandle =
                  (org.apache.bcel.generic.InstructionHandle)useList.get(j);
               org.apache.bcel.generic.Instruction useInst =
                  useHandle.getInstruction();
               org.apache.bcel.generic.LocalVariableInstruction useLVI =
                  (org.apache.bcel.generic.LocalVariableInstruction)useInst;
                  FOUND_DEF:
                  for(int k=j; k < etList.size(); k++){
                     sandmark.util.newexprtree.ExprTree exprTree2 =
                        (sandmark.util.newexprtree.ExprTree)etList.get(k);
                     java.util.ArrayList defList = exprTree2.getDefs();
                     for(int l=0; l < defList.size(); l++){
                        org.apache.bcel.generic.InstructionHandle defHandle =
                           (org.apache.bcel.generic.InstructionHandle)defList.get(l);
                        org.apache.bcel.generic.Instruction defInst =
                           defHandle.getInstruction();
                        int usePos = useHandle.getPosition();
                        int defPos = defHandle.getPosition();
                        if(defPos >= usePos){
                           killList.add(exprTree);
                           if(DEBUG_KILL)System.out.println("added tree");
                           break FOUND_DEF;
                        }//end if
                     }//end for
                  }//end for
            }//end for
         }//end for
      }//end if
      aBlock.setKill(killList);
      if(DEBUG_KILL)System.out.println("aBlock kill: " + aBlock.getKill());
   }//end computeKill


   private void computeReachableUses(){

      /* initialization step. For each block in the cfg set in to gen 
         and out to 0.
      */
      for(int i=0; i < analysisBlockList.size(); i++){
         AnalysisBlock aBlock = (AnalysisBlock)analysisBlockList.get(i);
         aBlock.setIn(aBlock.getGen());
      }

      boolean change = true;
      while(change){
         change = false;
         if(DEBUG_USES)System.out.println("blocklist size: " + analysisBlockList.size());
         for(int j=0; j < analysisBlockList.size(); j++){
            if(DEBUG_USES)System.out.println("j: " + j);
            AnalysisBlock block = (AnalysisBlock)analysisBlockList.get(j);
            if(DEBUG_USES)System.out.println("computing in and out of " + block.getBB().getIH());

            java.util.ArrayList newout = new java.util.ArrayList();
	    java.util.Iterator succIter =
	       met.succs(block.getBB());
	    while (succIter.hasNext()) {
	       sandmark.analysis.controlflowgraph.BasicBlock bb =
		  (sandmark.analysis.controlflowgraph.BasicBlock)succIter.next();
               sandmark.util.newexprtree.ExprTreeBlock etb =
                  met.getExprTreeBlock(bb);
               AnalysisBlock s = (AnalysisBlock)analysisBlockMap.get(etb);
               if(DEBUG_USES){
                  if(s != null)System.out.println("s.in: " + s.getIn());
               }
               if(s != null)newout = computeUnion(newout, s.getIn());
            }//end for

            if(compare(block.getOut(), newout) != 0){
               if(DEBUG_USES){
                  System.out.println("there was change");
                  System.out.println("out: " + block.getOut());
                  System.out.println("newout: " + newout);
               }
               
               change = true;
               block.setOut(newout);

               java.util.ArrayList differenceList =
                  computeDifference(block.getOut(), block.getKill());
               java.util.ArrayList unionList =
                  computeUnion(differenceList, block.getGen());
               block.setIn(unionList);
            }else{
               if(DEBUG_USES)System.out.println("no change");
            }
            
         }//end for
      }//end while



   }//end computeReachableUses


   protected void computeTransitiveClosure(){
      java.util.ArrayList checkList = new java.util.ArrayList();
      java.util.ArrayList localVarIndexList = new java.util.ArrayList();
      AnalysisBlock aStartBlock = (AnalysisBlock)analysisBlockMap.get(startBlock);

      checkList.add(aStartBlock);
      Integer localVarIndex = new Integer(lvIndex);
      localVarIndexList.add(localVarIndex);

      boolean change = true;

      while(change){
         if(DEBUG)System.out.println("in trans closure loop");
         change = false;
         java.util.ArrayList newCheckList = new java.util.ArrayList();
         if(DEBUG)System.out.println("checkList size: " + checkList.size());
         for(int i=0; i < checkList.size(); i++){
            AnalysisBlock ablock = (AnalysisBlock)checkList.get(i);
            java.util.ArrayList in = new java.util.ArrayList();
            if(DEBUG)System.out.println("aBlock: " + ablock);
            if(ablock == null)
               break;
            else
               in = ablock.getIn();
            for(int j=0; j < in.size(); j++){
               sandmark.util.newexprtree.ExprTree et =
                  (sandmark.util.newexprtree.ExprTree)in.get(j);
               java.util.ArrayList defs = et.getUses();
               if(DEBUG)System.out.println("defs: " + defs);
               for(int k=0; k < defs.size(); k++){
                  org.apache.bcel.generic.InstructionHandle defHandle =
                     (org.apache.bcel.generic.InstructionHandle)defs.get(k);
                  org.apache.bcel.generic.Instruction defInst =
                     defHandle.getInstruction();
                  if(defInst instanceof
                     org.apache.bcel.generic.LocalVariableInstruction){
                     org.apache.bcel.generic.LocalVariableInstruction lvInst =
                        (org.apache.bcel.generic.LocalVariableInstruction)defInst;
                     int index = lvInst.getIndex();
                     localVarIndex = new Integer(index);
                     if(localVarIndexList.contains(localVarIndex)){
                        sliceTrees.add(et);
                        if(DEBUG)System.out.println("added exprTree to slice");
                        java.util.ArrayList uses = et.getDefs();
                        for(int l=0; l < uses.size(); l++){
                           org.apache.bcel.generic.InstructionHandle useHandle =
                              (org.apache.bcel.generic.InstructionHandle)uses.get(l);
                           org.apache.bcel.generic.Instruction useInst =
                              useHandle.getInstruction();
                           if(useInst instanceof
                              org.apache.bcel.generic.LocalVariableInstruction){
                              org.apache.bcel.generic.LocalVariableInstruction
                                 lvI = 
                                 (org.apache.bcel.generic.LocalVariableInstruction)
                                 useInst;
                              index = lvI.getIndex();
                              localVarIndex = new Integer(index);
                              localVarIndexList.add(localVarIndex);
                           }//end if
                        }//end for
                        sandmark.util.newexprtree.ExprTreeBlock etb =
                           et.getExprTreeBlock();
                        AnalysisBlock ab = 
                           (AnalysisBlock)analysisBlockMap.get(etb);
                        if(ab != null)newCheckList.add(ab);
                        change = true;
                     }//end if
                  }//end if
               }//end for
            }//end for
         }//end for
         checkList = newCheckList;
      }//end while
   }



} //end class
