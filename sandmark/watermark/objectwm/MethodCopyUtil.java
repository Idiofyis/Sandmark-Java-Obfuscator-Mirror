package sandmark.watermark.objectwm;

/*
 *  This class implements the method overloading features for embedding the watermark 
 *  vector instruction groups 
 */
public class MethodCopyUtil
{
   private boolean DEBUG = false;

   ObjectUtil util = null;
   ObjectHelper helper = null;
   Config config = null;
 
   public int copyMethodOption0 = 0;
   public int copyMethodOption1 = 0;
   public int copyMethodOption2 = 0;
   public int methodInvocationCount = 0;

   private static int parameterSuffix = 0;
   private org.apache.bcel.generic.InstructionHandle[] targetsObj = null;

   private org.apache.bcel.generic.Type mcopy_arg_types[] = null;
   private String mcopy_arg_names[] = null;
   private org.apache.bcel.generic.InstructionList mcopy_instrList = null;
   private org.apache.bcel.generic.InstructionHandle mcopy_instrHandles[] = null;
   org.apache.bcel.generic.Type deleteVartype = null;
   int randomDelete = -1;

   public MethodCopyUtil(ObjectUtil objUtil)
   {
      util = objUtil;
      helper = new ObjectHelper();
      config = new Config();
   }


   /*
    *  This method checks that the sideEffects of overloading a method is within a 
    *  particular threshold limit 
    */
   private boolean remVecfreqUpdatesInThreshold(int numVectors,
		                               int currVecIndex,
                                               VectorUpdateCtrl vecObj,
					       org.apache.bcel.generic.InstructionList instrList)
   {
      CodeBook codeBook = new CodeBook();

      int vecOccur[] = new int[numVectors];
      String  instrStrGrp[] = new String[20];
      String  cmpStr[] = new String[20];

      for(int v=0; v< numVectors; v++)
         vecOccur[v] = 0;

      if( instrList == null )
         return false;

      org.apache.bcel.generic.InstructionHandle[] instrHandles =
         instrList.getInstructionHandles();

      int i=0, offset=0, matchFlag=0;

      for(int v=0; v< numVectors; v++) {
         int updatesReqd = vecObj.getElementAt(v);


         /* get the instructions corresponding to this vector */
         int numCmpInstr = codeBook.elemsVectorGrp[v];
         for(int k=0; k< numCmpInstr; k++)
            instrStrGrp[k] = codeBook.vectorGrp[v][k];

         for(i=0; i< instrHandles.length-numCmpInstr; i++) {
            for(offset=0; offset<numCmpInstr; offset++) {

               org.apache.bcel.generic.InstructionHandle iHandle = instrHandles[i+offset];
               org.apache.bcel.generic.Instruction instr = iHandle.getInstruction();
               /* to ensure we dont touch util.cpObj right now, we use 'getOpcodeFromInstr' */
               cmpStr[offset] = instr.toString();
            }

            if( helper.codeMatch(cmpStr, instrStrGrp, numCmpInstr) )
               vecOccur[v]++;
         }

         /* break from the while if vecOccur exceeds the "threshold" */
         if( vecOccur[v] > 0 )
            if( (vecOccur[v] > updatesReqd) || (updatesReqd==0) ) 
               /* Cannot proceed with the update; threshold update " + 
                                  " exceeded for a vector .. */
               return false;
      }

      if( vecOccur[currVecIndex] == 0 )
         return false;

      for(int v=0; v< numVectors; v++)
         vecObj.updateFrequencyCounterInThreshold(v, vecOccur[v]);

      /* At this point the vecOccur[] contains the no. of sideeffects for the entire
         vector elements groups; store it; need not be recalculated */

      return true;
   }



   /** Creates a local Var and initializes it; used while deleting a paramter from the
    *  method and replicating it **/

   private int createInitLocalVar(org.apache.bcel.generic.Type deleteVartype,
                                  sandmark.program.Method mg)
   {
      org.apache.bcel.generic.InstructionList instrList = mg.getInstructionList();

      org.apache.bcel.generic.LocalVariableGen newlocal =
         mg.addLocalVariable("newlocal", deleteVartype, null, null);
      mg.mark();

      int localIndex = newlocal.getIndex();

      String vartype = deleteVartype.toString();

      org.apache.bcel.generic.Instruction newlocalstore = null;
      org.apache.bcel.generic.Instruction initInstr = null;

      if( vartype.equals("int") ) {
         initInstr = new org.apache.bcel.generic.ICONST(0);
         newlocalstore = new org.apache.bcel.generic.ISTORE(localIndex);
      }
      else if( vartype.equals("float") ) {
         initInstr = new org.apache.bcel.generic.FCONST(0);
         newlocalstore = new org.apache.bcel.generic.FSTORE(localIndex);
      }
      else if( vartype.equals("short") ) {
         initInstr = new org.apache.bcel.generic.ICONST(0);
         newlocalstore = new org.apache.bcel.generic.ISTORE(localIndex);
      }
      else if( vartype.equals("char") ) {
         initInstr = new org.apache.bcel.generic.ICONST(0);
         newlocalstore = new org.apache.bcel.generic.ISTORE(localIndex);
      }
      else if( vartype.equals("boolean") ) {
         initInstr = new org.apache.bcel.generic.ICONST(0);
         newlocalstore = new org.apache.bcel.generic.ISTORE(localIndex);
      }
      else if( vartype.equals("byte") ) {
         byte b = 5;
         initInstr = new org.apache.bcel.generic.BIPUSH(b);
         newlocalstore = new org.apache.bcel.generic.ISTORE(localIndex);
      }
      else if( vartype.equals("long") ) {
         long l = 0;
         initInstr = new org.apache.bcel.generic.LCONST(l);
         newlocalstore = new org.apache.bcel.generic.LSTORE(localIndex);
      }
      else if( vartype.equals("double") ) {
         double d = 0.0;
         initInstr = new org.apache.bcel.generic.DCONST(d);
         newlocalstore = new org.apache.bcel.generic.DSTORE(localIndex);
      }
      else if( vartype.equals("java.lang.String") ) {
         String str = "hello world";
         int sIndex = (util.classObj.getConstantPool()).addString(str);
         initInstr = new org.apache.bcel.generic.LDC(sIndex);
         newlocalstore = new org.apache.bcel.generic.ASTORE(localIndex);
      }
      else {
         throw new Error(" type --> " + vartype + " not supported in implementation ");
      }

      instrList.insert(newlocalstore);
      instrList.insert(initInstr);

      instrList.setPositions();
      mg.mark();

      mg.setInstructionList(instrList);
      mg.setMaxStack();
      mg.setMaxLocals();
      
      return localIndex;
   }


   /* 
    * Changes the local access index of the deleted parameter to the index of the new parameter
    * which replicates it 
    */
   private void changeLocalAccessIndex(sandmark.program.Method mg,
		                       int oldVarIndex,
                                       int  newVarIndex)
   {
      org.apache.bcel.generic.InstructionList instrList = mg.getInstructionList();
      org.apache.bcel.generic.InstructionHandle[] iHandles = instrList.getInstructionHandles();

      for(int k=0; k<iHandles.length; k++) {
         String instrStr = (iHandles[k].getInstruction()).toString();

         if(oldVarIndex != helper.getArgumentValInInstruction(instrStr))
            continue;
         org.apache.bcel.generic.IndexedInstruction indexInstr =
            (org.apache.bcel.generic.IndexedInstruction)iHandles[k].getInstruction();

         indexInstr.setIndex(newVarIndex);
      }

      mg.mark();
      mg.setInstructionList(instrList);
      mg.setMaxLocals(); // added : verify 
      return;
   }


   /*
    *  Third approach of overloading method, deleting a parameter and then creating 
    *  a new variable to replicate the deleted parameter.
    */
   public int deleteMethodParameter2(sandmark.program.Method mg)
   {
      String oldArgNames[] = mg.getArgumentNames();
      org.apache.bcel.generic.Type oldArgtypes[] = mg.getArgumentTypes();


      int count = 0;
      while(true) {
         randomDelete = helper.getRandomValue(0, oldArgtypes.length);
         mcopy_arg_types = new org.apache.bcel.generic.Type[oldArgtypes.length-1];
         mcopy_arg_names = new String[oldArgNames.length-1];

         int j=0;
         for(int k=0; k<oldArgtypes.length; k++) {
            if(k==randomDelete)
               continue;
            mcopy_arg_types[j] = oldArgtypes[k];
            mcopy_arg_names[j++] = oldArgNames[k];
         }

         if(!this.methodNameSigConflict(mg.getName(), mcopy_arg_types) &&
            !this.methodAlreadyOverloaded(mg.getName()))
             break;

         if(count++ >5)
             return -1;
      }
      return 1;
   }



   /*
    *  Second approach of overloading method, deleting a parameter and then removing 
    *  all the instructions that access the deleted parameter.
    */
   public int deleteMethodParameter1(sandmark.program.Method mg)
   {
      String oldArgNames[] = mg.getArgumentNames();
      org.apache.bcel.generic.Type oldArgtypes[] = mg.getArgumentTypes();

      int count = 0;
      while(true) {
         randomDelete = helper.getRandomValue(0, oldArgtypes.length);
         mcopy_arg_types = new org.apache.bcel.generic.Type[oldArgtypes.length-1];
         mcopy_arg_names = new String[oldArgNames.length-1];

         int j=0;
         for(int k=0; k<oldArgtypes.length; k++) {
            if(k==randomDelete)
               continue;
            mcopy_arg_types[j] = oldArgtypes[k];
            mcopy_arg_names[j++] = oldArgNames[k];
         }

         if(!this.methodNameSigConflict(mg.getName(), mcopy_arg_types)&&
            !this.methodAlreadyOverloaded(mg.getName()))
            break;
 
         if(count++ > 5)
            return -1;
      }
      return 1;
   }


   /*
    *  Creates an intial copy of an existing method which is to be overloaded and 
    *  returns the sandmark.program.Method object of the created method  
    */

   public sandmark.program.Method createCloneMethod(sandmark.program.Method mg, int selectModifyOption, 
		   int numVectors, int currVecIndex, VectorUpdateCtrl vecObj)
   {
      int method_access_flags = -1;
      selectModifyOption = 0; // test_purpose:

      if(mg.isStatic())
         method_access_flags = org.apache.bcel.Constants.ACC_PUBLIC |
                               org.apache.bcel.Constants.ACC_STATIC;
      else
         method_access_flags = org.apache.bcel.Constants.ACC_PUBLIC;

      mcopy_instrList = mg.getInstructionList();

      if(selectModifyOption==0) {
          if(this.insertMethodParameter(mg)==-1)
	      return null;
      }
      else if(selectModifyOption==1) {
          if(this.deleteMethodParameter1(mg)==-1)
	      return null;
      }
      else if(selectModifyOption==2) {
          if(this.deleteMethodParameter2(mg)==-1)
	      return null;
      }
      else {
         if(DEBUG)
          System.out.println(" Invalid methodModifyOption selected .... check code ");
          return null;
      }

      if(selectModifyOption!=1) {
	  /* we are modifying option 1 and doing check later ( since we have to delete some 
	   * targeter ranges */
          if(!this.remVecfreqUpdatesInThreshold(numVectors, currVecIndex, vecObj, mcopy_instrList))
	      return null;
      }

      org.apache.bcel.generic.MethodGen mgen = 
         new org.apache.bcel.generic.MethodGen(method_access_flags, mg.getReturnType(), mcopy_arg_types,
              mcopy_arg_names, mg.getName(), util.classObj.getName(),
	          mcopy_instrList, util.classObj.getConstantPool());

      org.apache.bcel.classfile.Method meth = mgen.getMethod();
      sandmark.program.LocalMethod copymg = new sandmark.program.LocalMethod(util.classObj, meth);

      if(selectModifyOption==1) {
          if(!copymg.isStatic()) 
             randomDelete++;//check TBD: change made ... 
          mcopy_instrHandles = null;
          boolean change = true;
          while(change){
             change = false;
	     mcopy_instrList = copymg.getInstructionList();
             mcopy_instrHandles = mcopy_instrList.getInstructionHandles();
    
             for(int k=0; k<mcopy_instrHandles.length; k++) {
                int argVal = 
	           helper.getArgumentValInInstruction((mcopy_instrHandles[k].getInstruction()).toString());
                if(argVal==randomDelete) {
                   this.deleteTargeterRange(copymg, mcopy_instrList, k);
                   change = true;
                   break;
                }
             }
	  }
          //util.classObj.setConstantPool(util.classObj.getFinalConstantPool());
    	  util.classObj.mark();
          if(!this.remVecfreqUpdatesInThreshold(numVectors, currVecIndex, vecObj, mcopy_instrList)) {
              util.classObj.removeMethod(copymg);
              util.classObj.mark();
	      return null;
	  }
      }

      if(selectModifyOption==2) {
          // create & initialize a local Var depending on the type deleted 
          int newlocalIndex = this.createInitLocalVar(deleteVartype, copymg);
          // change all accesses to localVar 'randomDelete' to 'newlocalIndex' 
          if(copymg.isStatic())
              this.changeLocalAccessIndex(copymg, randomDelete, newlocalIndex); // TBD : TBchecked ...+1
          else
              this.changeLocalAccessIndex(copymg, randomDelete+1, newlocalIndex); // TBD : TBchecked ...+1
      }

      return (sandmark.program.Method)copymg;
   }




   /*
    *  Re-initializes the parameters of the newly created method 
    */

   public void reInitializeParameters(sandmark.program.Method mg)
   {
      org.apache.bcel.generic.Type argtypes[] = mg.getArgumentTypes();

      String argnames[] = mg.getArgumentNames();
      int numArgs =  argtypes.length;
      if( numArgs > 0 ) {
         org.apache.bcel.generic.InstructionList arList = mg.getInstructionList();

         int extraval = 0; /* for long and double */
         for(int id=0; id< numArgs; id++) {
            String paramType = argtypes[id].toString();
            int newid = -1;
            if( mg.isStatic() )
               newid = id;
            else
               newid = id+1;

            newid = newid + extraval;

            /* TBD: instead of inserting the initialization code at the beginining of code,
               we can insert in anywhere prior to its use; to be safe, at targeter points */

            if( paramType.equals("int") || paramType.equals("short") ||
               paramType.equals("char") || paramType.equals("boolean") ||
               paramType.equals("byte") ) {
               org.apache.bcel.generic.Instruction storeInstr =
                  new org.apache.bcel.generic.ISTORE(newid);
               arList.insert(storeInstr);

               org.apache.bcel.generic.Instruction consInstr =
               org.apache.bcel.generic.InstructionConstants.ICONST_0;
               arList.insert(consInstr);
            }
            else if( paramType.equals("float") ) {
               float val = 0;
               int fIndex = util.classObj.getConstantPool().addFloat(val);
               org.apache.bcel.generic.Instruction fstoreInstr =
                  new org.apache.bcel.generic.FSTORE(newid);
               arList.insert(fstoreInstr);

               org.apache.bcel.generic.Instruction consInstr =
                  new org.apache.bcel.generic.LDC(fIndex);
               arList.insert(consInstr);
            }
            else if( paramType.equals("long") ) {
               long val = 0;
               int lIndex = util.classObj.getConstantPool().addLong(val);
               org.apache.bcel.generic.Instruction lstoreInstr =
                  new org.apache.bcel.generic.LSTORE(newid);
               arList.insert(lstoreInstr);
               extraval++;

               org.apache.bcel.generic.Instruction consInstr =
                 new org.apache.bcel.generic.LDC2_W(lIndex);
               arList.insert(consInstr);
            }
            else if( paramType.equals("double") ) {
               double val = 0;
               int dIndex = util.classObj.getConstantPool().addDouble(val);
               org.apache.bcel.generic.Instruction dstoreInstr =
                  new org.apache.bcel.generic.DSTORE(newid);
               arList.insert(dstoreInstr);
               extraval++;

               org.apache.bcel.generic.Instruction consInstr =
                 new org.apache.bcel.generic.LDC2_W(dIndex);
               arList.insert(consInstr);
            }
            else if( paramType.equals("java.lang.String") ) {
               String val = "here is the watermark! .. just kiddin'!!!";
               int sIndex = util.classObj.getConstantPool().addString(val);

               org.apache.bcel.generic.Instruction astoreInstr =
                  new org.apache.bcel.generic.ASTORE(newid);
               arList.insert(astoreInstr);

               org.apache.bcel.generic.Instruction consInstr =
                 new org.apache.bcel.generic.LDC(sIndex);
               arList.insert(consInstr);
            }
         }

	 mg.mark();
	 mg.setInstructionList(arList);
	 mg.setMaxStack();
         mg.setMaxLocals();
      }
      return;
   }


   /* 
    *  First approach of overloading method, inserting a new  parameter 
    */
   public int insertMethodParameter(sandmark.program.Method mg)
   {
      /* Insertion of new parameter */
      String oldArgumentNames[] = mg.getArgumentNames();
      org.apache.bcel.generic.Type oldArgumentTypes[] = mg.getArgumentTypes();

      mcopy_arg_names = new String[oldArgumentNames.length + 1];
      org.apache.bcel.generic.Type newArgumentTypes[] = null;

      String paramTypes[] = {"int", "double",  "float", "long", "boolean", "String"};

      Integer suffix = new Integer(parameterSuffix++);
      String parameterName = "newParam" + suffix.toString();

      int trycount = 0;
      while(true){
         String paramtype = paramTypes[helper.getRandomValue(0, paramTypes.length)];
         org.apache.bcel.generic.Type newtype = null;
         if(paramtype.equals("int"))
            newtype = org.apache.bcel.generic.Type.INT;
         else if(paramtype.equals("double"))
            newtype = org.apache.bcel.generic.Type.DOUBLE;
         else if(paramtype.equals("float"))
            newtype = org.apache.bcel.generic.Type.FLOAT;
         else if(paramtype.equals("long"))
            newtype = org.apache.bcel.generic.Type.LONG;
         else if(paramtype.equals("boolean"))
            newtype = org.apache.bcel.generic.Type.BOOLEAN;
         else if(paramtype.equals("String"))
            newtype = org.apache.bcel.generic.Type.STRING;
         else  {
            if(DEBUG)
            System.out.println(" code doesnot support any other parameter type ");
            return -1;
         }

         mcopy_arg_types = new org.apache.bcel.generic.Type[oldArgumentTypes.length+1];

         int x;
         for(x=0; x<oldArgumentTypes.length; x++)
            mcopy_arg_types[x] = oldArgumentTypes[x];
         mcopy_arg_types[x] = newtype;

         if(!this.methodNameSigConflict(mg.getName(), mcopy_arg_types) &&
            !this.methodAlreadyOverloaded(mg.getName()))
            break;

         if( trycount++ > 5 )
            return -1;
      }

      int x;
      for(x=0; x<oldArgumentNames.length; x++)
         mcopy_arg_names[x] = oldArgumentNames[x];
      mcopy_arg_names[x] = parameterName;

      //util.classObj.setConstantPool(util.classObj.getFinalConstantPool());
      util.classObj.mark();

      return 1;
   }




   /*
    *  Checks if the method is already overloaded more that a 'threshold' number of times 
    */
   private boolean methodAlreadyOverloaded(String methodName)
   {
      sandmark.program.Method[] methods = util.classObj.getMethods();
      if(methods==null)
	  return false;

      int methodIndex = 0;
      int overloadCount = 0;
      while( methodIndex < methods.length )
      {
         String cmpMethodName = methods[methodIndex++].getName();
         if( !methodName.equals(cmpMethodName) )
            continue;

         overloadCount++;
         if( overloadCount > 1 )
            return true;
      }
      return false;
   }


   /*
    *  Checks whether the name and signature of the method created  conflicts with any of 
    *  the existing methods' name and signature 
    */
   private boolean methodNameSigConflict(String methodName,
		                         org.apache.bcel.generic.Type newArgTypes[])
   {
      sandmark.program.Method[] methods = util.classObj.getMethods();
      if(methods==null)
	  return false;

      int methodIndex = 0;
      while(methodIndex < methods.length){

         String cmpMethodName = methods[methodIndex].getName();
         org.apache.bcel.generic.Type cmpArgTypes[] = methods[methodIndex++].getArgumentTypes();

         if( (newArgTypes.length != cmpArgTypes.length) || !methodName.equals(cmpMethodName) )
            continue;

         int diffFlag = 0;
         for(int k=0; k<newArgTypes.length; k++)
            if( !(newArgTypes[k].toString()).equals(cmpArgTypes[k].toString()) ) {
               diffFlag = 1;
               break;
            }
         if(diffFlag == 0)
            /* <methodNameSig conflict> */
            return true;
      }

      return false;
   }



   /* 
    *  Deletes the instructions within 2 consecutive targeters in which the instruction 
    *  referenced by 'index' lies
    */
   public void deleteTargeterRange(sandmark.program.Method mg,
                                   org.apache.bcel.generic.InstructionList instrList,
				   int index)
   {
      org.apache.bcel.generic.InstructionHandle new_target = null;

      org.apache.bcel.generic.InstructionHandle ih[] =
         instrList.getInstructionHandles();
      int numtempTargeterPoints[] = util.getTargeterPointsInRange(0, ih.length-1, instrList);

      int numTargeterPoints[] = null;
      int x=0;

      if( numtempTargeterPoints[0] == 0 )
         numTargeterPoints =  new int[numtempTargeterPoints.length+1];
      else {
         numTargeterPoints =  new int[numtempTargeterPoints.length+2];
         numTargeterPoints[x++] = 0;
      }

      for(int k=0; k<numtempTargeterPoints.length; k++)
         numTargeterPoints[x++] = numtempTargeterPoints[k];

      numTargeterPoints[x] = ih.length;

      for(int k=0; k<numTargeterPoints.length; k++) {
         int upperTargeter = numTargeterPoints[k+1];
         int lowerTargeter = numTargeterPoints[k];

         if( (lowerTargeter <= index) && (upperTargeter > index) ) {

            if(k != numTargeterPoints.length-1)
               new_target = ih[upperTargeter];
            else if( numTargeterPoints.length > 3 )
               new_target = ih[numTargeterPoints[k-1]];
            else
               new_target = null;

            try {
               instrList.delete(ih[lowerTargeter], ih[upperTargeter-1] );
            } catch (org.apache.bcel.generic.TargetLostException e) {
               // System.out.println("TargetLostException caught HERE: " + e);
               targetsObj = e.getTargets();

               if(targetsObj.length > 1 ) {
                  if(DEBUG)
                  System.out.println(" multiple targeters affected ! ");
                  ///System.exit(1);
               }

               for(int t=0; t < targetsObj.length; t++) {
                  org.apache.bcel.generic.InstructionTargeter[] targeters =
                     targetsObj[t].getTargeters();
                  for(int j=0; j<targetsObj.length; j++)
                     targeters[j].updateTarget(targetsObj[t], new_target);
               }
            } /* end of catch block */

	    mg.mark();
            mg.setInstructionList(instrList);
            break;
         }
      }

      return;
   }


   public int createMethodInvocation(sandmark.program.Method mg, 
		                     org.apache.bcel.generic.Type consArgTypes[],
				     sandmark.program.Method consmg)
   {
      int numClasses =  util.getNumberOfClasses(ObjectWatermark.myApp);
      sandmark.program.Method[] methodsInsert = null;
      int maxTry = config.getMaxTry();
      int tryCount = 0;

      while(true) {
         int classIndex = helper.getRandomValue(0, numClasses);
         java.util.Iterator iterClasses = ObjectWatermark.myApp.classes();

         if( classIndex > 0 )
            while( --classIndex != 0 )
               iterClasses.next();

         /* pick method at the 'iterClasses.next()' class */
	 sandmark.program.Class classObj = (sandmark.program.Class)iterClasses.next();
	 util.setTargetClassObject(classObj);
         util.setTargetClassName(classObj.getName());

         methodsInsert = util.classObj.getMethods();
         if(methodsInsert==null){
            if(tryCount++>maxTry){
               return -1;
            }
            continue;
         }
         else
            break;
      }
      int methodIndex;
      tryCount = 0;
      while(true) {
         if(tryCount++ > maxTry)
            return -1;
         methodIndex = helper.getRandomValue(0, methodsInsert.length);
         if( (methodsInsert[methodIndex].getName()).equals("<init>") )
            continue;
         if( (methodsInsert[methodIndex].getName()).equals(mg.getName()) )
            continue;


         org.apache.bcel.generic.InstructionList selectList =
            methodsInsert[methodIndex].getInstructionList();
         if( selectList == null )
            continue;
         org.apache.bcel.generic.InstructionHandle selectIh[] = selectList.getInstructionHandles();
         if(selectIh.length > 20)        // TBD: config.getMethodEmbedThreshold()) .. check threshold 
            break;
      }

      util.methodObj = methodsInsert[methodIndex]; 


      /*-----  at this point, all objects 'obj' are populated to do the insertion  ------------*/


      org.apache.bcel.generic.Type returnType = mg.getReturnType();
      String className = mg.getClassName();
      String methodName = mg.getName();
      int numlocals = mg.getMaxLocals();
      org.apache.bcel.generic.Type argTypes[] = mg.getArgumentTypes();
      /* NOTE:  method always public; */

      
      /* initialize parameters and return var( if any ) */
      int localIndices[] = this.initializeParameters(argTypes, returnType);
      if(localIndices == null)
         return -1;


      /* get random Insertion point */
      util.instrListObj = util.methodObj.getInstructionList();
      org.apache.bcel.generic.InstructionHandle[] tempIh = util.instrListObj.getInstructionHandles();
      int targeterPointsInRange[] = 
         util.getTargeterPointsInRange(localIndices.length*2, util.instrListObj.size(), util.instrListObj);

      if( targeterPointsInRange == null )
         return -1;

      int insertIndex = targeterPointsInRange[helper.getRandomValue(0, targeterPointsInRange.length)];


      org.apache.bcel.generic.InstructionHandle ih[] = util.instrListObj.getInstructionHandles();
      org.apache.bcel.generic.InstructionHandle insertHandle = ih[insertIndex];


      /* insert trivial  'opaque' jump across the invocation */
      util.instrListObj.insert(insertHandle, org.apache.bcel.generic.InstructionConstants.ICONST_1);
      util.instrListObj.insert(insertHandle, new org.apache.bcel.generic.IFNE(insertHandle));


      /* get Constructor parameters, and initialize parameter if any  */

      int consIndices[] = this.initializeParameters(consArgTypes, null);
      if(consIndices == null)
         return -1;


      /* create 'invoke object'  depending on whether the fn. is 'static' or 'virtual' */
      int objectIndex = -1;
      if( !mg.isStatic() )
         objectIndex = this.createInvokeObject(insertHandle, consIndices, consArgTypes,  consmg, org.apache.bcel.generic.Type.VOID);


      /* load localVars created into the stack */
      this.loadLocalVarInstr(insertHandle, localIndices, argTypes, returnType);


      /* insert invoke instructions; save to return localVar( if necessary ) */
      this.insertMethodInvoke(insertHandle, mg, localIndices[localIndices.length-1], returnType, objectIndex);
      if(DEBUG)
      System.out.println("methodmark -> "+ util.classObj.getName() + ":" + util.methodObj.getName());
      methodInvocationCount++;
      return 1;
   }



   private void insertMethodInvoke(org.apache.bcel.generic.InstructionHandle ih,
                                   sandmark.program.Method mg,
				   int returnLocalIndex,
                                   org.apache.bcel.generic.Type returnType,
				   int objectIndex)
   {
      if( (mg.isStatic() && (objectIndex != -1)) ||
          (!mg.isStatic() && (objectIndex == -1)) ) {
         if(DEBUG)
         System.out.println(" objectIndex contains wrong value ... check code ");
	 ///System.exit(1);
      }


      /*if(objectIndex != -1)
	 util.instrListObj.insert(ih, new org.apache.bcel.generic.ALOAD(objectIndex)); */


      org.apache.bcel.generic.InstructionFactory f =
         new org.apache.bcel.generic.InstructionFactory(util.classObj.getConstantPool());
      org.apache.bcel.generic.Instruction invokeInstr = null;
      if( mg.isStatic() )
         invokeInstr =
            f.createInvoke(mg.getClassName(), mg.getName(), returnType, mg.getArgumentTypes(),
                            org.apache.bcel.Constants.INVOKESTATIC);
      else 
         invokeInstr =
            f.createInvoke(mg.getClassName(), mg.getName(), returnType, mg.getArgumentTypes(),
                            org.apache.bcel.Constants.INVOKEVIRTUAL);
      util.instrListObj.insert(ih, invokeInstr);

      org.apache.bcel.generic.Instruction storeInstr = null;

      if(returnType != null ) {
         String paramType = returnType.toString();


         if( paramType.equals("int") || paramType.equals("short") ||
             paramType.equals("char") || paramType.equals("boolean") ||
             paramType.equals("byte") )
            storeInstr = new org.apache.bcel.generic.ISTORE(returnLocalIndex);
         else if( paramType.equals("float") )
            storeInstr = new org.apache.bcel.generic.FSTORE(returnLocalIndex);
         else if( paramType.equals("long") )
            storeInstr = new org.apache.bcel.generic.LSTORE(returnLocalIndex);
         else if( paramType.equals("double") )
            storeInstr = new org.apache.bcel.generic.DSTORE(returnLocalIndex);
         else if( paramType.equals("java.lang.String") )
            storeInstr = new org.apache.bcel.generic.ASTORE(returnLocalIndex);


	 if(!paramType.equals("void"))
            util.instrListObj.insert(ih, storeInstr);
      }

      util.methodObj.mark();
      util.methodObj.setInstructionList(util.instrListObj);
      util.methodObj.setMaxStack();
      return;
   }



   private int createInvokeObject(org.apache.bcel.generic.InstructionHandle ih,
		                  int localIndices[],
				  org.apache.bcel.generic.Type argTypes[], 
				  sandmark.program.Method mg,
				  org.apache.bcel.generic.Type returnType)
   {
      org.apache.bcel.generic.InstructionFactory f =
         new org.apache.bcel.generic.InstructionFactory(util.classObj.getConstantPool());

      org.apache.bcel.generic.ObjectType objType = 
         new org.apache.bcel.generic.ObjectType(mg.getClassName());

      org.apache.bcel.generic.Instruction newInstr = f.createNew(objType);
      org.apache.bcel.generic.Instruction dupInstr = org.apache.bcel.generic.InstructionConstants.DUP;

      org.apache.bcel.generic.Instruction invokespecialInstr = 
         f.createInvoke(mg.getClassName(), mg.getName(), returnType, mg.getArgumentTypes(), 
                         org.apache.bcel.Constants.INVOKESPECIAL);

      org.apache.bcel.generic.LocalVariableGen lg =
         util.methodObj.addLocalVariable("newObj", objType, null, null);

      int localIndex = lg.getIndex();
      org.apache.bcel.generic.Instruction storeInstr =
         new org.apache.bcel.generic.ASTORE(localIndex);


      util.instrListObj.insert(ih, newInstr );
      util.instrListObj.insert(ih, dupInstr);

      int lIndex;
      for(int k=0; k<localIndices.length-1; k++) {
         lIndex = localIndices[k];
         String paramType = argTypes[k].toString();
         org.apache.bcel.generic.Instruction loadInstr = null;

         if( paramType.equals("int") || paramType.equals("short") ||
            paramType.equals("char") || paramType.equals("boolean") ||
            paramType.equals("byte") )
            loadInstr = new org.apache.bcel.generic.ILOAD(lIndex);
         else if( paramType.equals("float") ) 
            loadInstr = new org.apache.bcel.generic.FLOAD(lIndex);
         else if( paramType.equals("long") ) 
            loadInstr = new org.apache.bcel.generic.LLOAD(lIndex);
         else if( paramType.equals("double") )
            loadInstr = new org.apache.bcel.generic.DLOAD(lIndex);
         else if( paramType.equals("java.lang.String") )
            loadInstr = new org.apache.bcel.generic.ALOAD(lIndex);

         util.instrListObj.insert(ih, loadInstr);
      }

      util.instrListObj.insert(ih, invokespecialInstr);
      util.instrListObj.insert(ih, storeInstr);
      util.instrListObj.insert(ih, new org.apache.bcel.generic.ALOAD(localIndex));
      util.methodObj.mark();

      util.methodObj.setInstructionList(util.instrListObj);
      util.methodObj.setMaxStack();
      return localIndex;
   }




   private void loadLocalVarInstr(org.apache.bcel.generic.InstructionHandle ih,
		                  int localIndices[], 
				  org.apache.bcel.generic.Type[] argTypes,
				  org.apache.bcel.generic.Type returnType)
   {
      for(int k=0; k<localIndices.length-1; k++) {
         int localIndex = localIndices[k];
         String paramType = argTypes[k].toString();
         org.apache.bcel.generic.Instruction loadInstr = null;

         if( paramType.equals("int") || paramType.equals("short") ||
            paramType.equals("char") || paramType.equals("boolean") ||
            paramType.equals("byte") )
            loadInstr = new org.apache.bcel.generic.ILOAD(localIndex);
         else if( paramType.equals("float") ) 
            loadInstr = new org.apache.bcel.generic.FLOAD(localIndex);
         else if( paramType.equals("long") ) 
            loadInstr = new org.apache.bcel.generic.LLOAD(localIndex);
         else if( paramType.equals("double") )
            loadInstr = new org.apache.bcel.generic.DLOAD(localIndex);
         else if( paramType.equals("java.lang.String") )
            loadInstr = new org.apache.bcel.generic.ALOAD(localIndex);

         util.instrListObj.insert(ih, loadInstr);
      }
      util.methodObj.mark();

      util.methodObj.setInstructionList(util.instrListObj);
      util.methodObj.setMaxStack();
      return;
   }



   private int[] initializeParameters(org.apache.bcel.generic.Type argTypes[],
                                      org.apache.bcel.generic.Type returnType)
   {
      org.apache.bcel.generic.Type argtypes[] = new org.apache.bcel.generic.Type[argTypes.length+1];

      int k;
      for(k=0; k<argTypes.length; k++)
         argtypes[k] = argTypes[k];
      argtypes[k] = returnType;

      int localIndices[] = new int[argTypes.length+1]; 

      String argnames[] = util.methodObj.getArgumentNames();
      int numArgs =  argtypes.length;
      if( numArgs > 0 ) {
         org.apache.bcel.generic.InstructionList arList = util.methodObj.getInstructionList();
         org.apache.bcel.generic.LocalVariableGen newlocal = null;
         int localIndex = -1;

         for(int id=0; id< numArgs; id++) {

            if( argtypes[id] == null ) {
               localIndices[id] = -1;
               continue;
            }

            String paramType = argtypes[id].toString();

	    if( paramType.equals("void") )
               continue;

            Integer suff = new Integer(parameterSuffix++);
            String locVar = "VAR" + suff.toString();

            if( paramType.equals("int") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.INT, null, null);
            if( paramType.equals("short") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.SHORT, null, null);
            if( paramType.equals("char") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.CHAR, null, null);
            if( paramType.equals("boolean") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.BOOLEAN, null, null);
            if( paramType.equals("byte") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.BYTE, null, null);
            if( paramType.equals("float") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.FLOAT, null, null);
            if( paramType.equals("long") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.LONG, null, null);
            if( paramType.equals("double") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.DOUBLE, null, null);
            if( paramType.equals("java.lang.String") )
               newlocal = util.methodObj.addLocalVariable(locVar, org.apache.bcel.generic.Type.STRING, null, null);

	    if(DEBUG) {
		    System.out.println(" paramType --> " + paramType);
		    System.out.println(" returnType --> " + returnType);
	    }
            localIndex = newlocal.getIndex();
            newlocal.setIndex(localIndex);  /* not required, i suppose */

            org.apache.bcel.generic.Instruction storeInstr = null;
            org.apache.bcel.generic.Instruction consInstr = null;

            if( paramType.equals("int") || paramType.equals("short") ||
               paramType.equals("char") || paramType.equals("boolean") ||
               paramType.equals("byte") ) {
               storeInstr = new org.apache.bcel.generic.ISTORE(localIndex);
               consInstr = org.apache.bcel.generic.InstructionConstants.ICONST_0;
            }
            else if( paramType.equals("float") ) {
               float val = 0;
               int fIndex = util.classObj.getConstantPool().addFloat(val);
               storeInstr = new org.apache.bcel.generic.FSTORE(localIndex);
               consInstr = new org.apache.bcel.generic.LDC(fIndex);
            }
            else if( paramType.equals("long") ) {
               long val = 0;
               int lIndex = util.classObj.getConstantPool().addLong(val);
               storeInstr = new org.apache.bcel.generic.LSTORE(localIndex);
               consInstr = new org.apache.bcel.generic.LDC2_W(lIndex);
            }
            else if( paramType.equals("double") ) {
               double val = 0;
               int dIndex = util.classObj.getConstantPool().addDouble(val);
               storeInstr = new org.apache.bcel.generic.DSTORE(localIndex);
               consInstr = new org.apache.bcel.generic.LDC2_W(dIndex);
            }
            else if( paramType.equals("java.lang.String") ) {
               String val = "here is the watermark! .. just kiddin'!!!";
               int sIndex = util.classObj.getConstantPool().addString(val);
               storeInstr = new org.apache.bcel.generic.ASTORE(localIndex);
               consInstr = new org.apache.bcel.generic.LDC(sIndex);
            }
	    else
               return null; // TBD: object return support ... 

            arList.insert(storeInstr);
            arList.insert(consInstr); // NOTE: inserted in opposite order 

            localIndices[id] = localIndex;
         }
      }
      util.methodObj.mark();

      util.methodObj.setInstructionList(util.instrListObj);
      util.methodObj.setMaxLocals();
      util.methodObj.setMaxStack();

      return localIndices;
   }

}


