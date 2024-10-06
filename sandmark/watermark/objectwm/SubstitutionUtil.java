package sandmark.watermark.objectwm;

/**
 *  This class implements all the code substitution embedding features 
 */

public class SubstitutionUtil
{
   private static final boolean DEBUG = false;
    private ObjectUtil util = null;
    private ObjectHelper helper = null;
 
    org.apache.bcel.generic.InstructionHandle[] targetsObj = null;
    private int updateTargetersFlag = 0;
 
    /*
     *  Constructor 
     */
    public SubstitutionUtil(ObjectUtil objUtil)
    {
        util = objUtil;
        helper = new ObjectHelper();
    }
 
    /*
     *  This method is the first procedure for vector increment.
     *  Pure code substitution is done in this ie. we find match of codegroup instruction 
     *   and substitute it by equivalent set of instructions;
     *  @param insertih contains the point where the insertion is to be done.
     *   (remember, the old instructions are already deleted!) 
     *  @param subInstr contains the instructions obtained from codeBook along with the 
     *   parameters of the old instructions 
     *  @param numInstr contains the number of instructions that are being substituted 
     */
    public void substituteCode(org.apache.bcel.generic.InstructionHandle insertih,
                               String subInstr[], int numInstr)
    {
        org.apache.bcel.generic.Instruction instruc = null;
        org.apache.bcel.generic.BranchInstruction binstruc = null;
  
        org.apache.bcel.generic.InstructionHandle insertHandle = insertih;

        org.apache.bcel.generic.InstructionHandle insertHandles[] =
            util.instrListObj.getInstructionHandles();
  
        org.apache.bcel.generic.InstructionHandle new_target = null;
  
        for(int i=0; i<numInstr; i++){
            if(helper.isOfTypeBranch(subInstr[i])){
                binstruc =
                    (org.apache.bcel.generic.BranchInstruction)(util.extractInstrType(subInstr[i]));
                util.instrListObj.insert(insertHandle, binstruc);
            }
            else{
                instruc =  util.extractInstrType(subInstr[i]);
                if((i==0)&&(updateTargetersFlag==1))
                    /* we save the target to redirect all the targeters of the 
                     * deleted point to the first subst instruction embedded */
                    new_target = util.instrListObj.insert(insertHandle, instruc);
                else
                    util.instrListObj.insert(insertHandle, instruc);
            }
        }

	//util.instrListObj.setPositions();
  
        if(updateTargetersFlag==1){
            updateTargetersFlag = 0;
            for(int t=0; t < targetsObj.length; t++){
                org.apache.bcel.generic.InstructionTargeter[] targeters =
                    targetsObj[t].getTargeters();
  
                for(int j=0; j<targeters.length; j++)
                    targeters[j].updateTarget(targetsObj[t], new_target);
            }
        }

	util.instrListObj.setPositions();
	util.methodObj.mark();

	util.methodObj.setInstructionList(util.instrListObj);
        util.methodObj.setMaxStack(); /* needed for one vector group ! */
        return;
    }
 
 
 
    /*  This method retrieves the point where code is to be substituted,
     *  provided we found the codebook instruction group in the app. code,
     *  else null is returned;
     *  @param instrGrp 
     *  @param numInstr -> input parameters
     *  @param origCodeStr -> output parameter 
     */
    public org.apache.bcel.generic.InstructionHandle getCodeSubstPoint(String instrGrp[],
                                                     int numInstr, String origCodeStr[])
    {
        java.util.Iterator codeClasses = ObjectWatermark.myApp.classes();
  
        String cmpStr[] = new String[20];
        String instrStrGrp[] = new String[20];
  
        for(int l=0; l<numInstr; l++)
            instrStrGrp[l] = instrGrp[l];
  
        while(codeClasses.hasNext()){
            sandmark.program.Class classObj = (sandmark.program.Class)codeClasses.next();
            util.setTargetClassObject(classObj);
            util.setTargetClassName(classObj.getName());

            sandmark.program.Method[] methods = util.classObj.getMethods();
   
            int methodIndex = 0;
            while(methodIndex<methods.length){
                util.methodObj = methods[methodIndex++];
                String methodName = util.methodObj.getName();
                if( methodName.equals("<init>") )
                    continue;
    
                String methodSig = util.methodObj.getSignature();
    
                org.apache.bcel.generic.InstructionList instrList =
                    util.methodObj.getInstructionList();
                if(instrList==null)
                    continue;
                org.apache.bcel.generic.InstructionHandle[] instrHandles =
                    instrList.getInstructionHandles();
    
                int i,offset=0,matchFlg=0;
                if(DEBUG)
		System.out.println("meth -> "+util.methodObj.getClassName()+":"+util.methodObj.getName());
    
                for(i=0; i< instrHandles.length-numInstr; i++){
                    for(offset=0; offset<numInstr; offset++){
                        org.apache.bcel.generic.InstructionHandle iHandle = instrHandles[i+offset];
                        org.apache.bcel.generic.Instruction instr = iHandle.getInstruction();
			org.apache.bcel.classfile.ConstantPool cp =
			    (util.classObj.getConstantPool()).getConstantPool();
                        cmpStr[offset] = helper.getOpcode(instr.toString(cp));
                    }
     
                    if(helper.codeMatch(instrStrGrp, cmpStr, numInstr)){
                        matchFlg = 1;
                        /* NOTE: here we are assuming we have max. one branch instruction in 
                         * the codeBook code; we always have max. one ! */
                        if(DEBUG)
      			System.out.println("methodmark -> "+ util.classObj.getName() +
					   ":" + util.methodObj.getName());

                        for(int k=0; k<numInstr; k++){
                            org.apache.bcel.generic.Instruction brchInstr = instrHandles[i+k].getInstruction();
                            if(helper.isOfTypeBranch(brchInstr)){
                                util.targetHandleObj =
                                    ((org.apache.bcel.generic.BranchInstruction)brchInstr).getTarget();
                                break;
                            }
                        }
                        break;
                    }
                }
    
                if(matchFlg!=1)
                    /* did not found a code match; look in the next method */
                    continue;
    
                /* saving the matched instructions into origCodeStr[] for later substitution */
                for(int k=0; k<numInstr; k++)
                    origCodeStr[k] = (instrHandles[i+k].getInstruction()).toString(
				            (util.classObj.getConstantPool()).getConstantPool());
    
                try{
                    instrList.delete(instrHandles[i], instrHandles[i+numInstr-1] );
                }catch(org.apache.bcel.generic.TargetLostException e){
                    targetsObj = e.getTargets();
                    if(targetsObj.length>1){
                       if(DEBUG)
                        System.out.println(" multiple targeters affected: CHECK CODE! ");
                        return null;
                    }
                    updateTargetersFlag = 1;
                }

		//instrList.setPositions();
        	util.methodObj.mark();
                //util.methodObj.setInstructionList(instrList);
                util.instrListObj = instrList;
	        util.methodObj.setInstructionList(util.instrListObj);

                int deletePoint = i;
                instrHandles = instrList.getInstructionHandles();
                return(instrHandles[deletePoint]);
            }
        }
        return null;
    }
}

