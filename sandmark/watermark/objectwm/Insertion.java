package sandmark.watermark.objectwm;

/*  This class implements all the watermark vector frequency 
 *  increment procedures.
 *      
 *      @author  :   tapas@cs.arizona.edu
 */
public class Insertion 
{
    private boolean DEBUG = false;

    private ObjectUtil util = null;
    private ObjectHelper helper = null;
    private VectorUpdateCtrl vecObj = null;
    private Config config = null;
 
    private SubstitutionUtil substUtil = null;
    private InstructionEmbedUtil instrEmbedUtil = null;
    private MethodCopyUtil methodcopyUtil = null;
 
    private int numClasses;
    private java.util.Vector numMethods = new java.util.Vector(20, 10);
    private int numInstr[][] = new int[10][50];
 
    private int numberOfSubstitutions = 0;
    private int numberOfNewInstructionEmbed = 0;
    private int numberOfMethodCopying = 0;
 
    private static int methodOverloadOption[] = null;
    private static int feasibleCounter = 0;
 
    /*  Constructor 
     */
    public Insertion() 
    {
        util = new ObjectUtil();
        helper =  new ObjectHelper();
        config = new Config();
  
        substUtil = new SubstitutionUtil(util);
        instrEmbedUtil = new InstructionEmbedUtil(util);
        methodcopyUtil = new MethodCopyUtil(util);
  
        CodeBook cb = new CodeBook();
        methodOverloadOption = new int[CodeBook.wmarkLength];
        for(int k=0; k<methodOverloadOption.length; k++)
            methodOverloadOption[k] = 0;
    }
 

    /*  This method inserts previously non-existing codes; 
     *  new code inserted to increase the vector frequency; 
     *  @substInstr --> 'raw' instructions fom codeBook 
     *  @numInstr -->  number of instructions to be embedded 
     *  @codeBookembedIndex --> group number from which the nullify instructions are to be taken 
     *  Returns 'true' on success, else returns 'false' 
     */
    private boolean newInstructionEmbed(String substInstr[], int numInstr, int codeBookembedIndex)
    {
        int classIterations = util.getNumberOfClasses(ObjectWatermark.myApp);
        sandmark.program.Method[] methodsInsert = null;
  
        int maxTry = config.getMaxTry();
        int tryCount = 0;
  
        while(true){
            int classIndex = helper.getRandomValue(0, classIterations);
            java.util.Iterator iterClasses = null;
            iterClasses = ObjectWatermark.myApp.classes();
   
            if(classIndex>0)
                while(--classIndex!=0)
                    iterClasses.next();
   
            /* insert code at the 'iterClasses.next()' class */
            sandmark.program.Class classObj = (sandmark.program.Class)iterClasses.next();
            util.setTargetClassObject(classObj);
            util.setTargetClassName(classObj.getName());
   
            methodsInsert = util.classObj.getMethods();
   
            if(methodsInsert.length==0){
                if(tryCount++>maxTry)
                    return false;
                 continue;
            }
            else
                break;
        }
  
        int methodIndex;
        tryCount = 0;
        while(true){
            if(tryCount++ > maxTry)
                return false;
            methodIndex = helper.getRandomValue(0, methodsInsert.length);

            sandmark.program.Method selectmg = methodsInsert[methodIndex];
   
            /* skip <constructor> and <static> method */
            if((selectmg.getName()).equals("<init>"))
                continue;
            if((selectmg.getName()).equals("<clinit>"))
                continue;
   
            org.apache.bcel.generic.InstructionList selectList = selectmg.getInstructionList();
            if(selectList==null)
                continue;
   
            org.apache.bcel.generic.InstructionHandle selectIh[] = selectList.getInstructionHandles();
            
            if(selectIh.length>config.getMethodEmbedThreshold())
                break;
        }
  
        util.methodObj = methodsInsert[methodIndex];
        util.instrListObj = util.methodObj.getInstructionList();
        org.apache.bcel.generic.InstructionHandle[] instrHandles =
            util.instrListObj.getInstructionHandles();
  
   
        /* IMPORTANT NOTE: 'instrEmbedUtil.newInstrIndexObj' is the point where we are going to
         * insert the new instructions: initially it is set to 0 ie. First instruction in the method */
        instrEmbedUtil.newInstrIndexObj = 0;
        instrEmbedUtil.ihNewInstrEmbedObj = instrHandles[instrEmbedUtil.newInstrIndexObj];
   
        int result = instrEmbedUtil.substituteNewCode(substInstr, numInstr, codeBookembedIndex, vecObj);
        if(result==1) {
           if(DEBUG)
            System.out.println("methodmark -> "+ util.methodObj.getClassName() + ":" + util.methodObj.getName());
            return true;
	}
        return false;
    }

 
 
    /*  method copied to increase the vector frequency of the vector groups present 
     *  in that method. 
     *  @instrGrpInstr -> the vector instruction group which is to be incremented 
     *  @numInstr -> number of instructions in this group 
     *  @numVectors -> ie. the watermark length 
     *  @currVecIndex -> the present vector index which is being incremented; 
     *  Returns the number of 'vecOccurence' of the vector 'vIndex' on succes,
     *  else returns -1 
     */
    private int copyMethodEmbed(String instrGrpInstr[], int numInstr,
                                int numVectors, int currVecIndex)
    {
        int vecOccurence = 0;
        java.util.Iterator codeClasses = ObjectWatermark.myApp.classes();
  
        String cmpStr[] = new String[config.getMaxCodeInstructions()];
  
        while(codeClasses.hasNext()) {
            sandmark.program.Class classObj = (sandmark.program.Class)codeClasses.next();
            util.setTargetClassName(classObj.getName());
            util.setTargetClassObject(classObj);
  
            sandmark.program.Method consMethods[] = util.classObj.getMethods();
            if(consMethods==null)
                continue;

	    /* Storing the constructor method information for this class; 
	     * this is used by the dummy invokation of the copy method, specifically 
	     * when we need to create the object for this class */
            sandmark.program.Method consmg = null;
            org.apache.bcel.generic.Type consTypes[] = null;
            for(int p=0; p<consMethods.length; p++) {
                consmg = consMethods[p];
                if((consmg.getName()).equals("<init>")) {
                    consTypes = consmg.getArgumentTypes();
                    break;
                }
            }
            if(util.skipMethod(consmg))
                continue;
  
            sandmark.program.Method[] methods = util.classObj.getMethods();
            int methodIndex = 0;
  
            while(methodIndex < methods.length){
                util.methodObj =  methods[methodIndex++];
                String methodName = util.methodObj.getName();
                String methodSig = util.methodObj.getSignature();
    
                /* check if argument type is not of desired type */
                if(util.skipMethod(util.methodObj)) 
                    continue;
    
                vecOccurence = util.getNumberOfInstanceOfGroup(util.methodObj, instrGrpInstr, numInstr);
                if(vecOccurence<=0)
                    continue;
    
                int selectModifyOption;
                if((util.methodObj.getArgumentNames()).length==0)
                    selectModifyOption = 0;// only insertion of parameter possible 
                else{
                    String methodModifyOption[] =
                        {"insertParameter", "deleteParameter1", "deleteParamater2"};
                    selectModifyOption = helper.getRandomValue(0, methodModifyOption.length);
                }

                sandmark.program.Method copymg =
                    methodcopyUtil.createCloneMethod(util.methodObj, selectModifyOption, 
				    numVectors, currVecIndex, vecObj);
		if(copymg==null)
	            continue;

                methodcopyUtil.reInitializeParameters(copymg);
    
                /*  [ Done Along with createCloneMethod now (due to this rewrite) .. see above ]
		 *  Next step is to insert an additional parameter/delete one parameter;
                 *
                 *  INSERTION: create a new parameter; can be anything; initialize the parameter;
                 *  introduce some bogus codes that accesses this parameter at some targeter point 
                 *  in the clone method ; this bogusCode can also be the vectorInstruction group, 
                 *  but keep track of the freqency update too; do we need to be careful abt the 
                 *  local reuse ?? since this method maynot be ever invoked; 
                 *
                 *  DELETION: delete all the accesses to this parameter in the method; for each 
                 *  access we have to delete all the instructions till the either side of closest
                 *  targeters; check for vector frequency difference; if it decreases a *measure* 
                 *  then abort, else keep it.
                 */
    
                /*int numInstructions = util.getNumberOfInstructionsInMethod(copymg);
                if((numInstructions < config.getMethodCopyLowerThreshold()) ||
                   (numInstructions > config.getMethodCopyUpperThreshold()))
                    continue; TBD: in createCloneMethod */
          
                if(selectModifyOption==0) methodcopyUtil.copyMethodOption0++;
                if(selectModifyOption==1) methodcopyUtil.copyMethodOption1++;
                if(selectModifyOption==2) methodcopyUtil.copyMethodOption2++;
                if(DEBUG)
                System.out.println("methodmark -> "+util.classObj.getName()+":"+copymg.getName());
		//util.classObj.setConstantPool((util.classObj.getConstantPool()).getFinalConstantPool());
		util.classObj.mark();

                /* Create an invocation to this method; 
                 * bypass the invocation thru an opaque predicate 
                 * step1 : get the method parameters and the return parameter( if any ) 
                 * step2 : select class/method insertion point 
                 * step3 : create new locals based on requirement 
                 * step4 : create opaque constructs 
                 * step5 : do final invocation code insertion 
                 */
    
                int retVal = methodcopyUtil.createMethodInvocation(copymg, consTypes, consmg);
                if(retVal!= -1)
                    util.updateJarFileInfo();
               
                return(vecOccurence);
            }
        }
        return -1;
    }
 

    /*  Displays the current vector frequencies in the application 
     */
    private void displayCurrentVectorFrequency()
    {
        VectorExtraction vecExtract = new VectorExtraction();
        java.util.Iterator codeClasses = null;
        codeClasses = ObjectWatermark.myApp.classes();
  
        java.util.Vector tempVector = null;
        java.util.Vector finalVector = new java.util.Vector(10,1);

        for(int k=0; k<8; k++)
            finalVector.addElement(new Integer(0));
  
        while(codeClasses.hasNext()){
            tempVector = vecExtract.extractVector((sandmark.program.Class)codeClasses.next());
            if(tempVector == null)
                continue;
  
            Integer tempVal = null;
            for(int v=0; v< finalVector.size(); v++) {
                int newfreq = ((Integer)(finalVector.elementAt(v))).intValue() +
                              ((Integer)tempVector.elementAt(v)).intValue();
  
                tempVal = new Integer(newfreq);
                finalVector.setElementAt(tempVal, v);
            }
        }
        ObjectHelper.display_VectorInfo( finalVector, "CURR VECTOR FREQ");
        return;
    }
 
 
 
 
    /*  This method is the first procedure for vector increment. 
     *  Pure code substitution is done in this ie. we find match of 
     *  codegroup instruction and substitute it by equivalent set of instructions; 
     *  @vIndex contains the vector group whose frequency is to be incremented; 
     *  Returns 1 on success, else returns -1 
     */
    private int codeSubstitution(int vIndex)
    {
        /* get corresponding CodeGroup from the VectorGroup */
        String origCodeStr[] = new String[config.getMaxCodeInstructions()];
        int numOrigCodeInstr = 0;
  
        String subInstr[] = new String[config.getMaxCodeInstructions()];
        int numSubInstr = 0;
  
        int setId = -1;
        org.apache.bcel.generic.InstructionHandle ihSubst = null;
  
        CodeBook codeBook = new CodeBook();
  
        for(int j=0; j<codeBook.numSets[vIndex]; j++){
            numOrigCodeInstr = codeBook.numInInstr[vIndex][j];
            ihSubst = substUtil.getCodeSubstPoint(codeBook.inInstr[vIndex][j],
                                                  numOrigCodeInstr, origCodeStr);
            if(ihSubst!=null){
               setId = j;
               break;
            }
        }
        if(setId==-1)
            return -1;
  
        numSubInstr = codeBook.getInstructionFromCodeBook(origCodeStr, numOrigCodeInstr,
                                                          vIndex, setId, subInstr );
        /* 'subInstr' has the set of instructions to be substituted */
  
        substUtil.substituteCode(ihSubst, subInstr, numSubInstr);
  
        vecObj.updateFrequencyCounter(vIndex);
        util.updateJarFileInfo();
        numberOfSubstitutions++;
        return 1;
    }
 


    /*  Entry procedure for this 'Insertion' class.
     */
    public void modifyCode(java.util.Vector wmVector)
    {
        java.util.Vector codeList = new java.util.Vector(50, 10);
        vecObj = new VectorUpdateCtrl(wmVector);
        if(DEBUG) vecObj.displayVectorFreq("modifyCode->wmVectorfrequency: ");
  
        /* ITERATE TILL ALL THE DESIRED FREQUENCY HAS BEEN REACHED */
        while( !vecObj.allUpdatesDone()){
            if(feasibleCounter++ > config.getEmbedEffortCount()){
                sandmark.util.Log.message(0, "WATERMARK embedding not completely done ...");
                sandmark.util.Log.message(0, " ... not enough embedding scope in the application ");
                return;
            }
   
            for(int vIndex=0; vIndex<wmVector.size(); vIndex++){
                if(DEBUG) vecObj.displayVectorFreq("each iter ___ currVINDEX -> " + vIndex + "\n");
                if(vecObj.zerofreqState(vIndex))
                    continue;
                if(vecObj.markState(vIndex))
                    continue;
    
                int result = -1;
    
                /* if there is scope for substitution, do it */
                if( vecObj.getSubstSearch(vIndex) == 1 ) {
                   if(DEBUG)
	            System.out.println(" trying code substitution ... \n");
                    result = this.codeSubstitution(vIndex);
		}
    
                if(result==1){
                    if(DEBUG){
                        System.out.println(" substitution process ");
                        this.displayCurrentVectorFrequency();
              		    System.out.println(" code subst. embed done : vIndex = "+vIndex);
                    }
                    continue;
                }
    
                /* set the 'substSearch' marker to 0, ie. no more searching for 
		 * substitution unless it is set to 1 by some other substitution;
		 * again a dependency sort of matrix is created and 
                 * stored for reference */
                vecObj.unsetSubstSearch(vIndex);
    
                /* Else, choose an instruction embedding techinque from the pool */
                int embedType = helper.getRandomValue(0, config.getNumberOfEmbeddingOptions());
                if(methodOverloadOption[vIndex] > config.getMaxMethodOverloads())
                    embedType = 0;
    
                CodeBook codeBook = new CodeBook();
                switch(embedType){
                    case 0:
                       if(DEBUG)
	            	    System.out.println(" trying new instruction embed ... \n");
                            int numOrigCodeInstr = codeBook.numEmbedInstr[vIndex];
                            String updateBook[] = new String[config.getMaxCodeInstructions()];
                            updateBook = codeBook.embedInstr[vIndex];
    
                            boolean embedSuccess =
                                this.newInstructionEmbed(updateBook, numOrigCodeInstr, vIndex);
                            if(embedSuccess){
                               if(DEBUG)
				System.out.println(" new instr embed done : vIndex = "+vIndex);
                                util.updateJarFileInfo();
                                vecObj.updateFrequencyCounter(vIndex);
                                numberOfNewInstructionEmbed++; 
                                if(DEBUG){
                                    System.out.println(" new instruction embed process ");
                                    this.displayCurrentVectorFrequency();
                                }
                            }
                            break;
                   case 1:   
                      if(DEBUG)
	            	    System.out.println(" trying method copy embed ... \n");
                            methodOverloadOption[vIndex]++;
    
                            int numVecInstr = codeBook.elemsVectorGrp[vIndex];
                            String vecInstr[] = new String[numVecInstr];
                            for(int cp=0; cp<numVecInstr; cp++)
                                vecInstr[cp] = codeBook.vectorGrp[vIndex][cp];
    
                            int numCopyUpdates =
                                this.copyMethodEmbed(vecInstr, numVecInstr, wmVector.size(), vIndex);
                            if(numCopyUpdates>0){
                               if(DEBUG)
				System.out.println(" method copy embed done : vIndex = "+vIndex);
                                if(DEBUG){
                                    System.out.println(" method overloading process ");
                                    this.displayCurrentVectorFrequency();
                                }
                                numberOfMethodCopying++;
                            }
                            break;
                }
            }/* for each vector element */
        }/* freq iteration.(while)*/
  
        if(DEBUG){
            System.out.println("sub -> " + numberOfSubstitutions);
            System.out.println("Ins -> " + numberOfNewInstructionEmbed);
            System.out.println("MC -> " + numberOfMethodCopying);
        }
        return;
    }
}

