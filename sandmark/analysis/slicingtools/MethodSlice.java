package sandmark.analysis.slicingtools;

abstract class MethodSlice {

   boolean DEBUG = false;

   sandmark.program.Method method;
   sandmark.util.newexprtree.MethodExprTree met;
   org.apache.bcel.generic.InstructionHandle startHandle; 
   org.apache.bcel.classfile.LocalVariable lv;
   java.util.ArrayList metBlockList;   //list of blocks in the cfg
   java.util.ArrayList analysisBlockList;  //list of analysis blocks in the cfg
   java.util.ArrayList slice; //list of instruction handles that comprise slice
   java.util.ArrayList affectedVars; //list of local variables in the slice
   int lvIndex;
   java.util.HashMap analysisBlockMap;
   sandmark.util.newexprtree.ExprTreeBlock startBlock;
   java.util.ArrayList sliceTrees;

   public MethodSlice(sandmark.program.Method method,
      org.apache.bcel.generic.InstructionHandle startHandle)
      throws RuntimeException {

      this.method = method;
      this.startHandle = startHandle;
      analysisBlockList = new java.util.ArrayList();
      slice = new java.util.ArrayList();
      affectedVars = new java.util.ArrayList();
      sliceTrees = new java.util.ArrayList();
      analysisBlockMap = new java.util.HashMap();

      //make sure the startHandle is valid
      org.apache.bcel.generic.Instruction startInst =
         startHandle.getInstruction();
      if(startInst instanceof org.apache.bcel.generic.LocalVariableInstruction){
         org.apache.bcel.generic.LocalVariableInstruction lvi = 
            (org.apache.bcel.generic.LocalVariableInstruction)startInst;
         lvIndex = lvi.getIndex();
         if(DEBUG)System.out.println("lvIndex: " + lvIndex);
      }else{

         throw new RuntimeException("Illegal start instruction. Must be a local" +
            " variable instruction"); 
     } 

      //get the methodExprTree
      org.apache.bcel.generic.ConstantPoolGen cp = method.getCPG();
      met = new sandmark.util.newexprtree.MethodExprTree(method, false);
      metBlockList = met.getExprTreeBlocks();

      //need to find the exprtreeblock that contains the startHandle
      sandmark.analysis.controlflowgraph.BasicBlock startBB =
         met.getBlock(startHandle); 

      if(startBB == null){
         throw new RuntimeException("Cannot compute the slice of for this" +
               "instruction");
      }

      startBlock = met.getExprTreeBlock(startBB);

      //cast all of the ExprTreeBlocks to AnalysisBlocks
      for(int i=2; i < metBlockList.size(); i++){
         sandmark.util.newexprtree.ExprTreeBlock etb =
            (sandmark.util.newexprtree.ExprTreeBlock)metBlockList.get(i);
         AnalysisBlock aBlock = new AnalysisBlock(met, etb);
         analysisBlockList.add(aBlock);
         analysisBlockMap.put(etb, aBlock); 
      }

   }//end constructor


   //PRIVATE METHODS
   protected abstract void computeStaticSlice();

   protected abstract void computeDynamicSlice();

   protected abstract void computeGen(AnalysisBlock aBlock);

   protected abstract void computeKill(AnalysisBlock aBlock);

   protected abstract void computeTransitiveClosure();

   /**
    * sets the list of instruction handles that comprise the slice.
    */
   protected void setSlice(){
      sortSliceTrees();
      for(int i=0; i < sliceTrees.size(); i++){
         sandmark.util.newexprtree.ExprTree et =
            (sandmark.util.newexprtree.ExprTree)sliceTrees.get(i);
         java.util.ArrayList etInstList = et.getInstructionList();
         for(int j=0; j < etInstList.size(); j++)
            slice.add((org.apache.bcel.generic.InstructionHandle)etInstList.get(j));
      }
   
   }


   protected void sortSliceTrees(){
      java.util.ArrayList tempSliceTrees = new java.util.ArrayList();
      if(sliceTrees.size() >= 1)
         tempSliceTrees.add(sliceTrees.get(0));
      for(int i=0; i < sliceTrees.size(); i++){
         sandmark.util.newexprtree.ExprTree et =
            (sandmark.util.newexprtree.ExprTree)sliceTrees.get(i);
         java.util.ArrayList instList = et.getInstructionList();
         org.apache.bcel.generic.InstructionHandle firstInst =
            (org.apache.bcel.generic.InstructionHandle)instList.get(0);
         int insertInstPos = firstInst.getPosition();
         INSERTED:
         for(int j=0; j < tempSliceTrees.size(); j++){
            sandmark.util.newexprtree.ExprTree et1 =
               (sandmark.util.newexprtree.ExprTree)tempSliceTrees.get(j);
            java.util.ArrayList tempinstList = et1.getInstructionList();
            org.apache.bcel.generic.InstructionHandle tempfirstInst =
               (org.apache.bcel.generic.InstructionHandle)tempinstList.get(0);
            int tempInstPos = tempfirstInst.getPosition();
            if(insertInstPos < tempInstPos){
               tempSliceTrees.add(j, et);
               break INSERTED;
            }else if(j == tempSliceTrees.size()-1){
               tempSliceTrees.add(et);
               break INSERTED;
            }
         }
      }
      sliceTrees = tempSliceTrees;
   }

   
   /**
    * Computes the list of local variables that are in the slice.
    */
   protected void computeAffectedVars(){

      for(int i=0; i < slice.size(); i++){
         org.apache.bcel.generic.InstructionHandle ih = 
            (org.apache.bcel.generic.InstructionHandle)slice.get(i);
         org.apache.bcel.generic.Instruction inst = ih.getInstruction();
         if(inst instanceof org.apache.bcel.generic.LocalVariableInstruction){
            org.apache.bcel.generic.LocalVariableInstruction lvi =
            (org.apache.bcel.generic.LocalVariableInstruction)inst;
            //int index = lvi.getIndex();
            //Integer intIndex = new Integer(index);
            //if(!affectedVars.contains(intIndex))
               //affectedVars.add(intIndex);
            if(!affectedVars.contains(lvi))
               affectedVars.add(lvi);
         }
      }

   }

   protected int compare(java.util.ArrayList current, java.util.ArrayList newone){

      if(current.size() == newone.size()){
         for(int i=0; i < current.size(); i++){
            boolean found = false;
            sandmark.util.newexprtree.ExprTree cet =
               (sandmark.util.newexprtree.ExprTree)current.get(i);
            for(int j=0; j < newone.size(); j++){
               sandmark.util.newexprtree.ExprTree net =
                  (sandmark.util.newexprtree.ExprTree)newone.get(j);
               if(cet.compareTo(net) == 0)
                  found = true;
            }
            if(found == false)
               return -1;
         }
         return 0;
      }else{
         return -1;
      }

   }


   protected java.util.ArrayList computeUnion(java.util.ArrayList list1,
      java.util.ArrayList list2){

      java.util.ArrayList unionList = new java.util.ArrayList();

      //compute the union of the two arrayLists
      unionList.addAll(list1);
      for(int i = 0; i < list2.size(); i++){
         if(!unionList.contains(list2.get(i)))
            unionList.add(list2.get(i));
      }
      return unionList;
   }


   protected java.util.ArrayList computeDifference(java.util.ArrayList list1,
      java.util.ArrayList list2){

      java.util.ArrayList differenceList = new java.util.ArrayList();

      //subtract all elements in list2 from list1
      for(int i=0; i < list1.size(); i++){
         if(!list2.contains(list1.get(i)))
            differenceList.add(list1.get(i));
      }

      return differenceList;
   }


   //PUBLIC METHODS

   public java.util.ArrayList getSlice(){
      return slice;
   }

   public java.util.ArrayList getAffectedVars(){
      return affectedVars;
   }


}//end class
