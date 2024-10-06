package sandmark.watermark.objectwm;

/*
 *  This class builds all the functionalities required for embedding a new instruction
 *  group to increase the vector frequency
 */

public class InstructionEmbedUtil
{
   private static boolean DEBUG = false;
    ObjectUtil util = null;
    ObjectHelper helper = null;
 
    public org.apache.bcel.generic.InstructionHandle ihNewInstrEmbedObj = null;
    public int newInstrIndexObj = -1;
 
    private org.apache.bcel.generic.FieldInstruction putstaticObj = null;
    private org.apache.bcel.generic.FieldInstruction getstaticObj = null;
    private org.apache.bcel.generic.NEWARRAY naObj = null;
    private org.apache.bcel.generic.Instruction bipushObj = null;
 
    private org.apache.bcel.generic.InstructionHandle nullifyEmbedObj = null;
    private org.apache.bcel.generic.InstructionHandle nullifyBranchObj = null;
 
    private java.util.Vector localtable = new java.util.Vector(5,1);
 
    private static int methodNameSuffix = 0;
    private static int statVarSuffix = 0;
 
    private int localInitArray[] = new int[5];
    private int branchNullifyAbort = 0;
 
 
    /*  
     *  Constructor 
     */
    public InstructionEmbedUtil(ObjectUtil objUtil)
    {
        util = objUtil;
        helper = new ObjectHelper();
        Config config = new Config();
        branchNullifyAbort = config.getBranchNullifyAbortThreshold()+1;
    }
 
    /*  Checks if there is a integer variable & returns true/false;
     *  it also stores the first assignment to the local variable in 
     *  an array 'localInitArray[]' 
     */
    private boolean varTypeIsInt(int varIndex)
    {
        String cmp[] = new String[5];
        Integer val = new Integer(varIndex);
        String id = new String(val.toString());
        if(varIndex<=3){
            cmp[0] = "iload_"+id;
            cmp[1] = "istore_"+id;
        }
        else{
            cmp[0] = "iload " + id;
            cmp[1] = "istore " + id;
        }
  
        org.apache.bcel.generic.InstructionHandle[] ihCmp = util.instrListObj.getInstructionHandles();
        if((ihCmp==null)||true) // test_purpose:
            return false;
  
        for(int k=0; k< ihCmp.length; k++){
            org.apache.bcel.generic.Instruction instr = ihCmp[k].getInstruction();
            if(instr==null)
                return false;
	    org.apache.bcel.classfile.ConstantPool cp = (util.classObj.getConstantPool()).getConstantPool();
            if(cmp[0].equals(instr.toString(cp))|| cmp[1].equals(instr.toString(cp))){
                if( this.localIndexReuse(varIndex) )
                    continue;
                localInitArray[localtable.size()] = k;
                return true;
            }
        }
        return false;
    }
 
 
    private boolean localIndexReuse(int localIndex)
    {
        org.apache.bcel.generic.InstructionHandle[] tempIh = util.instrListObj.getInstructionHandles();
        String cmp[] = new String[5];
        Integer val = new Integer(localIndex);
        String id = new String(val.toString());
        if(localIndex<=3){
            cmp[0] = "load_"+id;
            cmp[1] = "store_"+id;
        }
        else{
            cmp[0] = "load " + id;
            cmp[1] = "store " + id;
        }
        for(int k=0; k< tempIh.length; k++){
            String str = (tempIh[k].getInstruction()).toString(
			    (util.classObj.getConstantPool()).getConstantPool());
            if((str.substring(1)).startsWith(cmp[0])||(str.substring(1)).startsWith(cmp[1]))
                if(!str.startsWith("i"))
                    return true;
        }
        return false;
    }
 
 
    /*  This function checks whether the 'localInitPoint' dominates the 'embedPoint' 
     */
    private boolean checkInitPointDominates(int localInitPoint, int embedPoint)
    {
        org.apache.bcel.generic.InstructionHandle[] ihs = util.instrListObj.getInstructionHandles();
  
        for(int k=0; k< util.instrListObj.size(); k++){
            if(k>=localInitPoint) // &&(k<embedPoint))
                break; //continue;//TBVerified:
            org.apache.bcel.generic.Instruction instr = ihs[k].getInstruction();
   
            if(!helper.isOfTypeBranch(instr))
                continue;
   
            org.apache.bcel.generic.InstructionHandle targethandle =
                ((org.apache.bcel.generic.BranchHandle)ihs[k]).getTarget();
   
            int id = util.getIndexOfHandle(targethandle);
   
            if((id>localInitPoint)&&(id<=embedPoint))
                /* localInitPoint doesnot dominate the embedPoint */
                return false;
   
            int worklist[] = new int[100];
            int count=0;
            if(id>embedPoint){
                /* test for backward jumps into the local-embed range ;
                 * maintain a 'worklist to analyze the loops recursively */
                worklist[count++] = id;
                while(count--!= 0){
                    for(int r=worklist[count]; r<util.instrListObj.size(); r++){
                        org.apache.bcel.generic.Instruction ins = ihs[r].getInstruction();
                        if(!helper.isOfTypeBranch(ins))
                            continue;
                        targethandle = ((org.apache.bcel.generic.BranchHandle)ihs[r]).getTarget();
                        int tIndex = util.getIndexOfHandle(targethandle);
                        if((tIndex > r)||(tIndex<localInitPoint))
                            continue;
                        if((tIndex>localInitPoint)&&(tIndex<embedPoint))
                            return false;
                        if((tIndex>=embedPoint) && (tIndex<worklist[count]))
                            worklist[count++] = tIndex;
                    }
                }
            }
        }
        return true;
    }
 
 
    /*
     *  This function computes and returns the point in the method code where 
     *  the local variable with index 'varIndex' is initialized (or first used); 
     *  Incase it is not initialized, -1 is returned  
     */
    private int getLocalVarInitPoint(int varIndex)
    {
        String cmp[] = new String[5];
        Integer val = new Integer(varIndex);
        String id = new String(val.toString());
        if(varIndex <= 3){
            cmp[0] = "iload_"+id;
            cmp[1] = "istore_"+id;
        }
        else{
            cmp[0] = "iload " + id;
            cmp[1] = "istore " + id;
        }
        org.apache.bcel.generic.InstructionHandle tempIh[] = util.instrListObj.getInstructionHandles();
        if(tempIh==null)
            return -1;
        for(int k=0; k< tempIh.length; k++){
            org.apache.bcel.generic.Instruction instr = tempIh[k].getInstruction();

	    org.apache.bcel.classfile.ConstantPool cp =
		(util.classObj.getConstantPool()).getConstantPool();
            if(cmp[0].equals(instr.toString(cp))||cmp[1].equals(instr.toString(cp)))
                return k;
        }

        throw new Error(" Error @ getLocalVarInitPoint .. localVar uninitialized ");
    }
 
 
    /*
     *  Checks in the 'localtable' if the local variable is already there along with its 
     *  corresponding index; localtable is of the form --> X12 ;
     *  Returns 'true' on occurence, else returns 'false' 
     */
    private boolean localAlreadyUsed(int localIndex)
    {
        for(int k=0; k<localtable.size(); k++){
            String str = (String)localtable.elementAt(k);
            if((str.substring(1)).equals((new Integer(localIndex)).toString()))
                return true;
        }
        return false;
    }
 
 
    /*
     *  This functions extracts a reusuable local variable; incase no local variables
     *  in the method can be reused, it creates a new local variable at returns its index; 
     *  methodsInvoked: localAlreadyUsed(), varTypeIsInt(int)  
     */
    private int getLocalVarIndex_CreateIndex(int instrEmbedIndex)
    {
        int maxlocals = util.methodObj.getMaxLocals();
        if(maxlocals > 1){
            for(int localIndex=0; localIndex<maxlocals; localIndex++){
                if(this.localAlreadyUsed(localIndex))
                    /* this is to ensure that 2 different 'raw' variables are not assigned 
                     * the same actual local no.; creates problem during nullifying */
                    continue;
                /* call a function to return true if local 'localIndex' is int, else return false */
                if(this.varTypeIsInt(localIndex))
                    return(localIndex);
            }
        }
  
        /* Else create a new local variable; return its index */
        Integer suff = new Integer(methodNameSuffix++);
        String locVar = "VAR" + suff.toString();
        org.apache.bcel.generic.LocalVariableGen newlocal =
            util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.INT, null, null);
        int localIndex = newlocal.getIndex();
  
        newlocal.setIndex(localIndex);  /* not required, i suppose */
  
        localInitArray[localtable.size()] = 1;
  
        /* shift the rest of inits in localInitArray[] by 2 to accomodate this new initialization
         * instructions at the beginning of the code */
  
        for(int r=0; r<localtable.size(); r++)
            localInitArray[r]+=2;
  
        org.apache.bcel.generic.Instruction init = new org.apache.bcel.generic.ISTORE(localIndex);
        util.instrListObj.insert(init);
        init = org.apache.bcel.generic.InstructionConstants.ICONST_2;
        util.instrListObj.insert(init);
  

        newInstrIndexObj += 2; /*IMP: TBD: recheck: takes care of next use local problem ; so that this
        included instructions are not considered for next use */
  
        util.instrListObj.setPositions();
	util.methodObj.mark();

	util.methodObj.setInstructionList(util.instrListObj);
        util.methodObj.setMaxLocals();
        util.methodObj.setMaxStack(); 

        return(localIndex);
    }
 
 
    /*
     *  Converts the received code to the actual code to be inserted by placing 
     *  appropriate local variable indices etc etc; 
     *  methodsInvoked: inLocalTable(String)
     */
    public String transformCode(String subInstr, String className)
    {
        if( subInstr.startsWith( "iload ") || subInstr.startsWith( "istore ") )
            return( this.insertlocalValFromTable(subInstr)  );
  
        if( subInstr.startsWith( "bipush ") ) {
            char ch = subInstr.charAt(subInstr.indexOf(' ') + 1);
            if (!Character.isUpperCase(ch))
                return(subInstr);
  
            String opCode = subInstr.substring(0, subInstr.indexOf(' '));
            int param = this.inLocalTable( subInstr.substring( subInstr.indexOf(' ') + 1 ));
  
            int newEntryflag = 0;
  
            if(param == -1) {
                param = helper.getRandomValue(1, 10);
                newEntryflag = 1;
            }
  
            Integer id = new Integer(param*10);
            String sparam = id.toString();
  
            if( newEntryflag == 1 ) {
                /* add this to local table */
                String localchar =
                     subInstr.substring(subInstr.indexOf(' ') + 1, subInstr.indexOf(' ') + 2);
                localchar += sparam;
                localtable.addElement(localchar);
            }
            return( opCode + " " + sparam);
        }
  
        if( subInstr.startsWith( "getstatic") ){
            org.apache.bcel.generic.Type t = org.apache.bcel.generic.Type.getType("[I");
            int accFlags = org.apache.bcel.Constants.ACC_STATIC;
   
            Integer suff = new Integer(statVarSuffix++);
            String staticVar = "newStaticArr" + suff.toString();
            org.apache.bcel.generic.FieldGen fg =
                new org.apache.bcel.generic.FieldGen(accFlags, t, staticVar, util.classObj.getConstantPool());
   
            org.apache.bcel.classfile.Field f = fg.getField();
	    sandmark.program.LocalField localf = new sandmark.program.LocalField(util.classObj, f);
            //util.classObj.addField(f); // cgObj
	    util.classObj.mark();
   
            naObj = new org.apache.bcel.generic.NEWARRAY(org.apache.bcel.generic.Type.INT);
   
            org.apache.bcel.generic.InstructionFactory fc =
                new org.apache.bcel.generic.InstructionFactory(util.classObj.getConstantPool());
   
            putstaticObj =
                fc.createFieldAccess(className, staticVar, t, org.apache.bcel.Constants.PUTSTATIC);
            getstaticObj =
                fc.createFieldAccess(className, staticVar, t, org.apache.bcel.Constants.GETSTATIC);
   
            int randomByte = helper.getRandomValue(10,80);
            byte pushVal = (byte)randomByte;
            bipushObj = new org.apache.bcel.generic.BIPUSH(pushVal);
   
            /* we have bipushObj, naObj & putstaticObj to be inserted */
            subInstr = "getstatic " + className + "." + staticVar + " " + "[I";
            return(subInstr);
        }
  
        if( helper.isOfTypeBranch(subInstr)) {
           util.targetHandleObj = ihNewInstrEmbedObj;
           if( subInstr.indexOf(' ') != -1 )
              return(subInstr.substring(0, subInstr.indexOf(' ')));
        }
  
        return(subInstr);
    }
 
 
    /*
     *  This method checks whether the code to be inserted splits any existing 
     *  vector groups; returns 'true' if splits, else returns 'false' 
     */
    public boolean checkSplitVectorGrp(int instrIndex,
                                       org.apache.bcel.generic.InstructionHandle[] instrHandles)
    {
        CodeBook codeBook = new CodeBook();
  
        for(int v=0; v<codeBook.numVectorGroups; v++){
            int numInstr = codeBook.elemsVectorGrp[v];
            String cmpStr[] = new String[numInstr];
            for(int startpoint=instrIndex-numInstr+1; startpoint<instrIndex; startpoint++){
                if(startpoint<0)
                    continue;
                if((startpoint+numInstr) > instrHandles.length)
                    break;
                for(int offset=0; offset<numInstr; offset++){
                    org.apache.bcel.generic.InstructionHandle iHandle =
                        instrHandles[startpoint+offset];
                    org.apache.bcel.generic.Instruction instr = iHandle.getInstruction();
	            org.apache.bcel.classfile.ConstantPool cp =
		        (util.classObj.getConstantPool()).getConstantPool();
                    cmpStr[offset] = helper.getOpcode( instr.toString(cp) );
                }
    
                if( helper.codeMatch(codeBook.vectorGrp[v], cmpStr, numInstr) ){
                   if(DEBUG)
                    System.out.println(" Splits the vector group --> " + v);
                    return true;
                }
            }
        }
  
        // checking nullifyInstr split now ...
        for(int v=0; v<codeBook.numVectorGroups; v++){
            if( !codeBook.isBranchEmbed(v) )
                continue;
   
            int numInstr = codeBook.numNullifyInstr[v];
            String cmpStr[] = new String[numInstr];
            for(int startpoint=instrIndex-numInstr; startpoint<instrIndex; startpoint++){
                if(startpoint<0)
                    continue;
                if( (startpoint+numInstr) > instrHandles.length )
                    break;
                for(int offset=0; offset<numInstr; offset++) {
                    org.apache.bcel.generic.InstructionHandle iHandle =
                        instrHandles[startpoint+offset];
                    org.apache.bcel.generic.Instruction instr = iHandle.getInstruction();
	            org.apache.bcel.classfile.ConstantPool cp =
		        (util.classObj.getConstantPool()).getConstantPool();
                    cmpStr[offset] = helper.getOpcode( instr.toString(cp) );
                    if( cmpStr[offset].startsWith("goto") )
                        return true;
                }
                if( helper.codeMatch(codeBook.nullifyInstr[v], cmpStr, numInstr) )
                    return true;
            }
        }
        return false;
    }
 
 
 
    /*
     *  This function checks the place after 'newInstrIndexObj' till the end of the code 
     *  where the localVar ( which has index 'localIndex' ) is next used; 
     *  it then selects a *safe* random point in between this range & returns it; 
     *  incase the local is not reused anymore, it returns -1 ; 
     *  Also, make sure that the return index is before any jump into this range!! 
     */
    private int getNullifyInsertPoint()
    {
       org.apache.bcel.generic.InstructionHandle[] tempIh =
          util.instrListObj.getInstructionHandles();
 
       java.util.Vector accessInstr = new java.util.Vector(10,1);
 
       for(int k=0; k<localtable.size(); k++) {
 
           String str = (String)localtable.elementAt(k);
           int localVal = (new Integer(str.substring(1))).intValue();
 
           if(localVal < 3) {
              accessInstr.addElement("iload_"+localVal);
              accessInstr.addElement("istore_"+localVal);
           }
           else {
              accessInstr.addElement("iload "+localVal);
              accessInstr.addElement("istore "+localVal);
           }
           accessInstr.addElement("iinc "+localVal);
           /*  TBD: anymore opcodes possible that change the local value? */
       }
 
       int k;
       int nextUseFlag = 0;
       for(k=newInstrIndexObj; k<tempIh.length; k++) {
          org.apache.bcel.generic.Instruction instruc = tempIh[k].getInstruction();
          nextUseFlag = 0;
          for(int i=0; i<accessInstr.size(); i++) {
	     org.apache.bcel.classfile.ConstantPool cp =
	        (util.classObj.getConstantPool()).getConstantPool();
             if( (instruc.toString(cp)).startsWith((String)accessInstr.elementAt(i)) ) {
                nextUseFlag = 1;
                break;
             }
	  }
 
          if( nextUseFlag == 1 )
             break;
       }
 
       /* takes care of basic block problem */
       int basicblockEndPoint = this.getBlockEndPoint(newInstrIndexObj);
 
       if( basicblockEndPoint == -1 ) {
          throw new Error(" basicblockEndPoint = -1 " );
       }
 
       int upperRange;
 
       /* make the upper range the one which is closest ie. end of the basicblock in 
        * which the new instruction is embedded, or the point of next use of the local */
       if( k < basicblockEndPoint)
          upperRange = k;
       else
          upperRange = basicblockEndPoint;
 
       org.apache.bcel.generic.InstructionHandle[] tempIh4 =
          util.instrListObj.getInstructionHandles();
 
       /*  takes care of virtual machine stack problem  */
       int targeterPointsInRange[] =
          util.getTargeterPointsInRange(newInstrIndexObj+1, upperRange, util.instrListObj);
 
 
       /* if no targeters, then select a random point b/w 'newInstrIndexObj' & 'upperRange',
        * else, take a random targeter point and make that the point for insertion of the 
        * nullifying code  TBD: recheck consequences */
       if( targeterPointsInRange == null )
          /* No targeters in between embedPoint and nextSafeNullifyPoint: */
          return helper.getRandomValue(newInstrIndexObj, upperRange);
       else
          return targeterPointsInRange[ helper.getRandomValue(0,targeterPointsInRange.length) ];
    }


 
 
    /*
     *  This method computes and returns the point in the method code where the branch nullify
     *  instruction  is to be inserted ; note this is different from finding out the insertion 
     *  point for the normal nullify instructions *  methodsInvoked: util.getTargeterPointsInRange()
     */
    private int getBranchNullifyInsertPoint()
    {
       Config config = new Config();
       if( branchNullifyAbort > config.getBranchNullifyAbortThreshold() )
          /* Returning prematurely from fn. getBranchNullifyInsertPoint val */
          return -999;
 
       org.apache.bcel.generic.InstructionHandle tempIh[] = util.instrListObj.getInstructionHandles();
 
       int targeterPoints[] =
          util.getTargeterPointsInRange(0, util.instrListObj.size(), util.instrListObj);
 
       int initPoint = 0;
       int initArray[] = new int[localtable.size()];
 
       for(int k=0; k<localtable.size(); k++) {
          String str = (String)localtable.elementAt(k);
          int localVal = (new Integer(str.substring(1))).intValue();
 
          int tempInitPoint = this.getLocalVarInitPoint(localVal);
          initArray[k] = tempInitPoint;
 
          if( tempInitPoint == -1 ) {
             throw new Error(" 'tempInitPoint' for localVal --> " + localVal +
                                " not initialized, but used by a branch nullify group ");
          }
 
          if( tempInitPoint > initPoint )
             initPoint = tempInitPoint;
       }
 
       /* now select the targeters that do not conflict */
 
       int selectTargeterPoints[] = new int[targeterPoints.length];
 
       int p=0;
       for(int k=0; k<targeterPoints.length; k++)
          if(targeterPoints[k] > newInstrIndexObj)
             selectTargeterPoints[p++] = targeterPoints[k];
 
       int id = -1;
 
       for(int m=0; m<p; m++) {
          id = selectTargeterPoints[m];
          boolean jumpAcross = this.checkAssignmentInRange(newInstrIndexObj, id);
          if( jumpAcross ) {
             id = -1;
             continue;
          }
          boolean split = this.checkSplitVectorGrp(id, tempIh);
          if( split ) {
             id = -1;
             continue;
          }
 
          int nextSelect = 0;
          for(int k=0; k<localtable.size(); k++) {
             boolean dom = this.checkInitPointDominates(initArray[k], id);
             if( !dom ) {
                nextSelect = 1;
                break;
             }
          }
          if( nextSelect == 0 )
             break;
 
          id = -1; /* loop around and fetch next 'selectTargeter' point */
       }
       return id;
    }
 
 
    /*
     *  Checks whether there is any assignment within the instruction indices 'src' and 'dest' 
     */
    private boolean checkAssignmentInRange(int src, int dest)
    {
       org.apache.bcel.generic.InstructionHandle tempIh[] = util.instrListObj.getInstructionHandles();
 
       for(int k=src; k<dest; k++) {
          org.apache.bcel.generic.Instruction instr = tempIh[k].getInstruction();
          String str = instr.toString();
          if( (str.substring(1)).startsWith("store") )
             return true;
       }
 
       return false;
    }
 
 
 
    /*
     *  Used while finding the basic blocks in the method 
     */
    private boolean inLeaderGroup(int index, int numleaders, int leaders[])
    {
       for(int k=0; k<numleaders; k++)
          if( leaders[k] == index )
             return true;
 
       return false;
    }
 
 
    /*
     *  This method returns the end point of the basic block where the reference
     *  'index' instruction lies; 
     *  @param index is the position where the bogus new Vector instruction is 
     *  embedded methodsInvoked: inLeaderGroup() 
     */
    private int getBlockEndPoint(int index)
    {
       org.apache.bcel.generic.InstructionHandle tempIh[] =
          util.instrListObj.getInstructionHandles();
 
       org.apache.bcel.generic.InstructionHandle targetHandles[] =
          new org.apache.bcel.generic.InstructionHandle[tempIh.length];
 
       int leaders[] = new int[tempIh.length];
 
       int numleaders = 0;
       leaders[numleaders++] = 0;
 
       for(int k=0; k<tempIh.length; k++)
          if( helper.isOfTypeBranch( tempIh[k].getInstruction() ) ) {
             if( !inLeaderGroup(k+1, numleaders, leaders) )
                leaders[numleaders++] = k+1;
 
             org.apache.bcel.generic.InstructionHandle iHandle =
                ((org.apache.bcel.generic.BranchHandle)tempIh[k]).getTarget();
 
             int id = util.getIndexOfHandle(iHandle);
             if( !inLeaderGroup(id, numleaders, leaders) )
                leaders[numleaders++] = id;
          }
 
       if( !inLeaderGroup(tempIh.length-1, numleaders, leaders) )
          leaders[numleaders++] = tempIh.length-1;
 
       /* sort leaders */
       for(int k=0; k<numleaders-1; k++)
          for(int m=k+1; m<numleaders; m++)
             if( leaders[k] > leaders[m] ) {
                int temp = leaders[k];
                leaders[k] = leaders[m];
                leaders[m] = temp;
             }
 
       for(int k=0; k<numleaders-1; k++)
          if( (leaders[k] <= index) && (leaders[k+1]>= index) )
             return leaders[k+1];
 
       throw new Error(" ERROR @ fn. getBlockEndPoint ... index should lie within a range ");
    }
 
 
    /*
     *  displaying leader information of the method cfg 
     */
    void displayLeaders(int numleaders, int leaders[])
    {
       System.out.println(" Leaders[] --> ");
       for(int k=0; k<numleaders; k++)
          System.out.println(leaders[k]);
       return;
    }
 
 
    /*
     *  returns the local variable index if it is in localtable, else returns -1 
     */
    private int inLocalTable(String localchar)
    {
       for(int k=0; k<localtable.size(); k++) {
          String str = (String)localtable.elementAt(k);
          if( localchar.equals(str.substring(0,1)))
             return (new Integer(str.substring(1))).intValue();
       }
 
       return -1;
    }
 
 
    /*
     *  displays the current local table
     */
    private void printlocalTable()
    {
       System.out.println(" localTable : ");
       for(int k=0; k<localtable.size(); k++)
          System.out.println((String)localtable.elementAt(k));
       return;
    }
 
 
    /*
     *  Fetches the last initialization point of the local variables reused/created
     *  for embedding instructions
     */
    private int getLastLocalInitPosition()
    {
       int init = -999;
       for(int k=0; k<localtable.size(); k++) {
          if( localInitArray[k] > init )
             init = localInitArray[k];
       }
       return init;
    }
 
 
    /* 
     *  This function evaluates and returns the point to embed the new instruction group;
     *  returns -1 if no possible point exists 
     */
    private int getNewEmbedPoint()
    {
       /* IMPORTANT NOTE: get a new instruction insert point ie. 'newInstrIndexObj' */
 
       int lastLocalInitPosition = this.getLastLocalInitPosition();
       org.apache.bcel.generic.InstructionHandle[] tempIh = util.instrListObj.getInstructionHandles();
       int targeterPointsInRange[] =
          util.getTargeterPointsInRange(lastLocalInitPosition,
                          util.instrListObj.size(), util.instrListObj);
       if( targeterPointsInRange == null )
          /* no targeters in Range  ( fn. getNewEmbedPoint )  : */
          return -1;
 
       int tryCount = 0;
       int tempNewInstrIndex = 0;
       boolean split = true;
       boolean dom = false;
 
       Config config = new Config();
       int maxTry = config.getMaxTry();
       while(split || !dom) {
          if(tryCount++ > maxTry)
             return -1;
          tempNewInstrIndex =
             targeterPointsInRange[helper.getRandomValue(0, targeterPointsInRange.length)];
 
          for(int k=0; k<localtable.size(); k++) {
             split = this.checkSplitVectorGrp(tempNewInstrIndex, tempIh);
             if(split)
                break;
             dom = this.checkInitPointDominates(localInitArray[k], tempNewInstrIndex);
             if( !dom )
                break;
          }
       }
       newInstrIndexObj = tempNewInstrIndex;
       ihNewInstrEmbedObj = tempIh[newInstrIndexObj];
       return 1;
    }
 
 
 
    /*
     *  This method is invoked from the newCode Insertion option in modifyCode(): 
     *  @param substInstr --> 'raw' instructions fom codeBook 
     *  @param numInstr -->  number of instructions to be embedded 
     *  @param codeBookembedIndex --> group number from which the nullify instructions 
     *  are to be taken; same as 'vIndex' in Insertion.class  
     */
    public int substituteNewCode( String subInstr[], int numInstr, int codeBookembedIndex,
                                  VectorUpdateCtrl vecObj)
    {
       /* First step: transform each instruction; insert local variable values in it */
       CodeBook codeBook = new CodeBook();
 
       /* NOTE: 'ihNewInstrEmbedObj' will change depending on the localVar available */
       org.apache.bcel.generic.Instruction instruc = null;
       org.apache.bcel.generic.BranchInstruction binstruc = null;
 
       localtable.removeAllElements();  /* init */
       for(int i=0; i<numInstr; i++)
          if( subInstr[i].startsWith("iload ") || subInstr[i].startsWith("istore ") ||
              subInstr[i].startsWith("iinc") ) {
 
             String localChar =
                subInstr[i].substring(subInstr[i].indexOf(' ')+1, subInstr[i].indexOf(' ')+2);
 
             if( this.inLocalTable(localChar) != -1 )
                /* Already assigned a local val( new/old ), skip  */
                continue;
 
             int newVal = this.getLocalVarIndex_CreateIndex(codeBookembedIndex);
             localtable.addElement(localChar + (new Integer(newVal)).toString());
          }
 
       // this.printlocalTable();
 
       /* At this point we have selected the local vars to be reused/created */
 
       if( this.getNewEmbedPoint() == -1 )
          return -1;
 
       int nullifyEmbedPoint = -1;
 
       /* Index location where we'll be inserting the 'nullifying' code */
       if( codeBook.isBranchEmbed(codeBookembedIndex) )
          nullifyEmbedPoint = this.getBranchNullifyInsertPoint();
       else
          nullifyEmbedPoint = this.getNullifyInsertPoint();
 
       org.apache.bcel.generic.InstructionHandle[] tempIh = util.instrListObj.getInstructionHandles();
 
       if( nullifyEmbedPoint == -999) {
          /* Abort overridden for branch target  */
       }
       else if( nullifyEmbedPoint != -1 )
          nullifyEmbedObj = tempIh[nullifyEmbedPoint];
       else {
          /* Couldnt get a possible 'nullifyEmbedPoint':  */
          branchNullifyAbort++;
          return -1;
       }
 
       org.apache.bcel.generic.BranchHandle finalEmbedBranchHandle = null;
 
       for(int i=0; i<numInstr; i++){
          subInstr[i] = this.transformCode(subInstr[i], util.getTargetClassName() );
 
          if( subInstr[i].startsWith("getstatic") ) {
             /* have to create a new static each time ... CHECK THIS !! TBD: */
 
             /** do the getstatic init insertion in some safe position in between
                 (0-ihNewInstrEmbedObj) ie. newInstrIndexObj, but it should be in a
                 targeter point to ensure the stack size is proper **/
 
             org.apache.bcel.generic.InstructionHandle[] newIh =
                util.instrListObj.getInstructionHandles();
 
             int targeterIndices[] = new int[newIh.length];
             int numTargeterIndices = util.getTargerterIndices(newInstrIndexObj, targeterIndices);
 
             /* pick a random targeter point from targetIndices */
             int staticInitIndex = helper.getRandomValue(0, newInstrIndexObj);
 
             org.apache.bcel.generic.InstructionHandle initInsertHandle = newIh[staticInitIndex];
             util.instrListObj.insert(initInsertHandle, bipushObj);
             util.instrListObj.insert(initInsertHandle, naObj);
             util.instrListObj.insert(initInsertHandle, putstaticObj);
 
             /* the rest of the code insertion is done at 'ihNewInstrEmbedObj' */
             util.instrListObj.insert(ihNewInstrEmbedObj, getstaticObj);
             org.apache.bcel.generic.Instruction icn =
                org.apache.bcel.generic.InstructionConstants.ICONST_0;
             util.instrListObj.insert(ihNewInstrEmbedObj, icn);
             util.instrListObj.insert(ihNewInstrEmbedObj, bipushObj);
             icn = new org.apache.bcel.generic.IASTORE();
             util.instrListObj.insert(ihNewInstrEmbedObj, icn);
          }
 
          if( helper.isOfTypeBranch(subInstr[i])) {
             util.targetHandleObj = ihNewInstrEmbedObj;
             binstruc =
                (org.apache.bcel.generic.BranchInstruction)(util.extractInstrType(subInstr[i]));
 
             finalEmbedBranchHandle = util.instrListObj.insert(ihNewInstrEmbedObj, binstruc);
          }
          else {
             instruc = util.extractInstrType(subInstr[i]);
             util.instrListObj.insert(ihNewInstrEmbedObj, instruc);
          }
       }
 
       util.instrListObj.setPositions();
       util.methodObj.mark();

       util.methodObj.setInstructionList(util.instrListObj);
       util.methodObj.setMaxLocals();
       util.methodObj.setMaxStack();
 
 
       /* if not embed branch reqd., then return */
       if(nullifyEmbedPoint == -999) {
          this.clearLocalTable();
          return 1;
       }
 
       if( codeBook.isBranchEmbed(codeBookembedIndex) )
          /* store the insert embed poition to jump back from the 'nullify' code */
          nullifyBranchObj = ihNewInstrEmbedObj;
 
       org.apache.bcel.generic.InstructionHandle branchEmbedJumpTarget = null;
 
       if(nullifyEmbedPoint!= -1) {
          branchEmbedJumpTarget = this.insertNullifyCode(codeBookembedIndex, nullifyEmbedPoint);
          if( codeBook.nullifyEffect[codeBookembedIndex] != -1 )
             vecObj.setSubstSearch( codeBook.nullifyEffect[codeBookembedIndex] );
       }
 
       if( codeBook.isBranchEmbed(codeBookembedIndex) )
          if(branchEmbedJumpTarget==null) {
             throw new Error(" branchEmbedJumpTarget not assigned value .. null .. ERROR ");
          }
          else if( finalEmbedBranchHandle == null ) {
             throw new Error(" finalEmbedBranchHandle not assigned value .. null .. ERROR ");
          }
          else
             finalEmbedBranchHandle.setTarget(branchEmbedJumpTarget);

       util.instrListObj.setPositions();
       util.methodObj.mark();
       util.methodObj.setInstructionList(util.instrListObj);

       this.clearLocalTable();
       return 1;
    }
 
 
 
    /*
     *  Removes all elements from the local table 
     */
    private void clearLocalTable()
    {
       localtable.removeAllElements();
       return;
    }
 
 
    /*
     *  This method inserts the nullifying code at the point 'nullifyEmbedPoint' 
     *  methodsInvoked: addFromLocalTable(), util.extractInstrType(), codeBook.isBranchEmbed() 
     */
    private org.apache.bcel.generic.InstructionHandle insertNullifyCode(int codeBookembedIndex,
                                                                         int nullifyEmbedPoint)
    {
       org.apache.bcel.generic.InstructionHandle embedTarget = null;
       org.apache.bcel.generic.Instruction instruc = null;
       org.apache.bcel.generic.BranchInstruction binstruc = null;
 
       CodeBook codeBook = new CodeBook();
 
       if( codeBook.numNullifyInstr[codeBookembedIndex] == 0 )
          return null;
 
       /* else insert the instruction at nullifyEmbedPoint*/
 
       /* addFromLocalTable performs almost the same function as 'transformCode' */
       String nullifyInstr[] = this.addFromLocalTable(codeBookembedIndex);
 
       /* nullifyInstr now contains the final instructions with appropriate locals to be
          substituted at 'nullifyEmbedPoint' */
 
       for(int i=0; i<nullifyInstr.length; i++) {
 
          if( helper.isOfTypeBranch(nullifyInstr[i])) {
 
             if( nullifyInstr[i].startsWith("goto") )
                util.targetHandleObj = nullifyEmbedObj;
             else
                util.targetHandleObj = nullifyBranchObj;
 
             binstruc =
                (org.apache.bcel.generic.BranchInstruction)(util.extractInstrType(nullifyInstr[i]));
          }
          else
             instruc = util.extractInstrType(nullifyInstr[i]);
 
 
          /*  DEBUGGING : checking final insert point  */
          org.apache.bcel.generic.Instruction insFinal = nullifyEmbedObj.getInstruction();
 
          if( helper.isOfTypeBranch(nullifyInstr[i])) {
             util.instrListObj.insert(nullifyEmbedObj, binstruc);
          }
          else  {
             if( (i==1) && codeBook.isBranchEmbed(codeBookembedIndex) )
                embedTarget = util.instrListObj.insert(nullifyEmbedObj, instruc);
             else
                util.instrListObj.insert(nullifyEmbedObj, instruc);
          }
          util.instrListObj.setPositions();
          util.methodObj.mark();

          util.methodObj.setInstructionList(util.instrListObj);
          util.methodObj.setMaxLocals();
          util.methodObj.setMaxStack();
       }
 
       if( codeBook.isBranchEmbed(codeBookembedIndex) )
          return embedTarget;
 
       return null;
    }
 
 
    /*
     *  Extracts the local index from the 'localtable' and returns the newly formed string 
     */
    private String[] addFromLocalTable(int embedIndex)
    {
       CodeBook codeBook = new CodeBook();
       int numInstr = codeBook.numNullifyInstr[embedIndex];
       String instr[] = codeBook.nullifyInstr[embedIndex];
 
       String newInstr[] = new String[numInstr];
       for(int k=0; k<numInstr; k++) {
          int flag = 0;
          String str = instr[k];
          char localchars[] = {'X', 'Y'};

          for(int m=0; m<localchars.length; m++) {
             int varPosition = str.indexOf(localchars[m]);
             if( varPosition == -1 )
                continue;
             int n;

             for(n=0; n<localtable.size(); n++) {
                String localStr = (String)localtable.elementAt(n);
                if( localStr.indexOf(localchars[m]) != 0 )
                   continue;
                newInstr[k] =
                   str.substring(0, varPosition-1) + " " + localStr.substring(1);
                if(varPosition != (str.length()-1) )
                   newInstr[k]+= str.substring(varPosition+1);
                flag = 1;
                break;
             }
             if( n == localtable.size() ) {
                throw new Error(" Error: missing substitution in localtable Vector!!!");
             }
          }
          if( flag == 0 )
             newInstr[k] = instr[k];
       }
 
       return newInstr;
    }
 
 
    /*
     *  Fetches the local variable value allocated; creates the instruction string and
     *  returns it
     */
    private String insertlocalValFromTable(String str)
    {
       int id = str.indexOf(' ');
       String localchar = str.substring(id+1, id+2);
 
       for(int k=0; k<localtable.size(); k++)  {
          String tableStr = (String)localtable.elementAt(k);
          if( localchar.equals( tableStr.substring(0,1) )) {
             String newStr = str.substring(0,id);
             newStr += " " + tableStr.substring(1);
             return(newStr);
          }
       }
 
       throw new Error("Error: localtable not defined properly : entry missing .... ");
    }

}



