package sandmark.watermark.objectwm;

/**
 *  This class implements all the APIs required by the main insertion methods 
 */

public class ObjectUtil  
{
   private String myTargetClassName;

   public sandmark.program.Class classObj = null;
   public sandmark.program.Method methodObj = null;
   public org.apache.bcel.generic.InstructionList instrListObj = null;
   public org.apache.bcel.generic.InstructionHandle targetHandleObj = null;

   private ObjectHelper helper = null;
   private Config config = null;
   private CodeBook codeBook = null;

   public ObjectUtil()
   {
      helper = new ObjectHelper();
      config = new Config();
      codeBook = new CodeBook();
   }

   public void setTargetClassName(String className)
   {
      myTargetClassName = className;
   }

   public void setTargetClassObject(sandmark.program.Class cObj)
   {
      classObj = cObj;
   }

   public String getTargetClassName()
   {
      return myTargetClassName;
   }


   /*
    *  This method takes the opcode of the instruction as a String and returns the 
    *  corresponding 'Instruction' object which is to be then used for insertion; 
    *  'instr' is complete ie. contains all the actual end parameters 
    */
   public org.apache.bcel.generic.Instruction extractInstrType( String instr )
   {
      int op1=0, op2=0;
      String opcode, operand1=null, operand2=null;

      int cmdIndex1 = instr.indexOf(" ");
      if (cmdIndex1 == -1) {
         opcode = instr;
         cmdIndex1 = instr.length()-1;
      }
      else
         opcode = instr.substring(0,cmdIndex1);

      String param = instr.substring(cmdIndex1+1,instr.length());

      if (!param.equals("")) {
         int cmdIndex2 = param.indexOf(" ");
         if (cmdIndex2 == -1) {
            cmdIndex2 = param.length();
            operand1 = param.substring(0,cmdIndex2);
            if (!opcode.equals("getstatic") )
               op1 = Integer.parseInt(operand1);
         }
         else {
            operand1 = param.substring(0,cmdIndex2);
            if (!opcode.equals("getstatic") && !operand1.equals("->") )
               op1 = Integer.parseInt(operand1);
            operand2 = param.substring(cmdIndex2+1);
            if (!opcode.equals("getstatic") && !operand1.equals("->") )
               op2 = Integer.parseInt(operand2);
         }
      }

      byte b1=(byte)op1, b2 = (byte)op2;
      org.apache.bcel.generic.Instruction incn = null;

      /* NOTE: more cases to be added as & when instructions are updated in the codeBook */

      if( opcode.equals("iload_0"))
         incn = org.apache.bcel.generic.InstructionConstants.ILOAD_0;
      if( opcode.equals("iload_1"))
         incn = org.apache.bcel.generic.InstructionConstants.ILOAD_1;
      if( opcode.equals( "iload_2"))
         incn = org.apache.bcel.generic.InstructionConstants.ILOAD_2;
      if( opcode.equals( "iload_3"))
         incn = new org.apache.bcel.generic.ILOAD((byte)3);

      if( opcode.equals( "istore_0"))
         incn = org.apache.bcel.generic.InstructionConstants.ISTORE_0;
      if( opcode.equals( "istore_1"))
         incn = org.apache.bcel.generic.InstructionConstants.ISTORE_1;
      if( opcode.equals( "istore_2"))
         incn = org.apache.bcel.generic.InstructionConstants.ISTORE_2;
      if( opcode.equals( "istore_3"))
         incn = new org.apache.bcel.generic.ISTORE((byte)3);

      if( opcode.equals( "iconst_0"))
         incn = org.apache.bcel.generic.InstructionConstants.ICONST_0;
      if( opcode.equals( "iconst_1"))
         incn = org.apache.bcel.generic.InstructionConstants.ICONST_1;
      if( opcode.equals( "iconst_2"))
         incn = org.apache.bcel.generic.InstructionConstants.ICONST_2;
      if( opcode.equals( "iconst_3"))
         incn = org.apache.bcel.generic.InstructionConstants.ICONST_3;
      if( opcode.equals( "iconst_4"))
         incn = org.apache.bcel.generic.InstructionConstants.ICONST_4;
      if( opcode.equals( "iconst_5"))
         incn = org.apache.bcel.generic.InstructionConstants.ICONST_5;

      if( opcode.equals( "iconst_m1"))
         incn = org.apache.bcel.generic.InstructionConstants.ICONST_M1;

      /*
      if( opcode.equals( "lconst_0"))
         incn = org.apache.bcel.generic.InstructionConstants.LCONST_0;
      if( opcode.equals( "lconst_1"))
         incn = org.apache.bcel.generic.InstructionConstants.LCONST_1;
       
      if( opcode.equals( "fconst_0"))
         incn = org.apache.bcel.generic.InstructionConstants.FCONST_0;
      if( opcode.equals( "fconst_1"))
         incn = org.apache.bcel.generic.InstructionConstants.FCONST_1;
      if( opcode.equals( "fconst_2"))
         incn = org.apache.bcel.generic.InstructionConstants.FCONST_2;

      if( opcode.equals( "dconst_0"))
         incn = org.apache.bcel.generic.InstructionConstants.DCONST_0;
      if( opcode.equals( "dconst_1"))
         incn = org.apache.bcel.generic.InstructionConstants.DCONST_1;
      */

      if( opcode.equals( "iload"))
         incn = new org.apache.bcel.generic.ILOAD(op1);
      if( opcode.equals( "istore"))
         incn = new org.apache.bcel.generic.ISTORE(op1);

      if( opcode.equals( "iadd"))
         incn = org.apache.bcel.generic.InstructionConstants.IADD;
      if( opcode.equals( "isub"))
         incn = org.apache.bcel.generic.InstructionConstants.ISUB;
      if( opcode.equals( "imul"))
         incn = org.apache.bcel.generic.InstructionConstants.IMUL;
      if( opcode.equals( "idiv"))
         incn = org.apache.bcel.generic.InstructionConstants.IDIV;
      if( opcode.equals( "ineg"))
         incn = org.apache.bcel.generic.InstructionConstants.INEG;
      if( opcode.equals( "ixor"))
         incn = org.apache.bcel.generic.InstructionConstants.IXOR;

      if( opcode.equals( "bipush"))
         incn = new org.apache.bcel.generic.BIPUSH(b1);
      if( opcode.equals( "sipush"))
         incn = new org.apache.bcel.generic.SIPUSH(b1);
      if( opcode.equals( "iinc"))
         incn = new org.apache.bcel.generic.IINC(op1, op2);
      if( opcode.equals( "dup"))
         incn = org.apache.bcel.generic.InstructionConstants.DUP;
      if( opcode.equals( "pop"))
         incn = new org.apache.bcel.generic.POP();
      if( opcode.equals( "pop2"))
         incn = new org.apache.bcel.generic.POP2();
      if( opcode.equals( "swap"))
         incn = new org.apache.bcel.generic.SWAP();
      if( opcode.equals( "iaload"))
         incn = new org.apache.bcel.generic.IALOAD();
      if( opcode.equals( "iastore"))
         incn = new org.apache.bcel.generic.IASTORE();

      if( opcode.equals( "getstatic")) {
         org.apache.bcel.generic.InstructionFactory f =
            new org.apache.bcel.generic.InstructionFactory(classObj.getConstantPool());

         int idx = operand1.indexOf(".");
         String cName = operand1.substring(0,idx);
         String name = operand1.substring(idx+1);
         org.apache.bcel.generic.Type t =
             org.apache.bcel.generic.Type.getType(operand2);
         incn =
	    f.createFieldAccess( cName, name, t, org.apache.bcel.Constants.GETSTATIC);
      }

      if( opcode.equals( "if_icmpge")) {
         org.apache.bcel.generic.IF_ICMPGE bInsr =
	    new org.apache.bcel.generic.IF_ICMPGE(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "if_icmplt")) {
         org.apache.bcel.generic.IF_ICMPLT bInsr =
	    new org.apache.bcel.generic.IF_ICMPLT(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "if_icmpgt")) {
         org.apache.bcel.generic.IF_ICMPGT bInsr =
	    new org.apache.bcel.generic.IF_ICMPGT(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "if_icmple")) {
         org.apache.bcel.generic.IF_ICMPLE bInsr =
	    new org.apache.bcel.generic.IF_ICMPLE(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "if_icmpne")) {
         org.apache.bcel.generic.IF_ICMPNE bInsr =
	    new org.apache.bcel.generic.IF_ICMPNE(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "if_icmpeq")) {
         org.apache.bcel.generic.IF_ICMPEQ bInsr =
	    new org.apache.bcel.generic.IF_ICMPEQ(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "if_acmpne")) {
         org.apache.bcel.generic.IF_ACMPNE bInsr =
	    new org.apache.bcel.generic.IF_ACMPNE(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "if_acmpeq")) {
         org.apache.bcel.generic.IF_ACMPEQ bInsr = new org.apache.bcel.generic.IF_ACMPEQ(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }


      if( opcode.equals( "ifgt")) {
         org.apache.bcel.generic.IFGT bInsr = new org.apache.bcel.generic.IFGT(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "iflt")) {
         org.apache.bcel.generic.IFLT bInsr = new org.apache.bcel.generic.IFLT(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "ifeq")) {
         org.apache.bcel.generic.IFEQ bInsr = new org.apache.bcel.generic.IFEQ(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "ifne")) {
         org.apache.bcel.generic.IFNE bInsr = new org.apache.bcel.generic.IFNE(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
         return(bInsr);
      }

      if( opcode.equals( "goto")) {
         org.apache.bcel.generic.GOTO bInsr = new org.apache.bcel.generic.GOTO(null);
	 if( targetHandleObj != null )
            bInsr.setTarget(targetHandleObj);
	 return(bInsr);
      }

      return(incn);
   }


   /* 
    *  skips the method whose parameter type is outside the range of types implemented 
    */
   public boolean skipMethod(sandmark.program.Method mg)
   {
      if( mg == null)
         return true;

      if( (mg.getName()).equals("<clinit>") )
          return true;

      org.apache.bcel.generic.Type retType = mg.getReturnType();

      if( !(
         (retType.toString()).equals("int") ||
         (retType.toString()).equals("short") ||
         (retType.toString()).equals("char") ||
         (retType.toString()).equals("boolean") ||
         (retType.toString()).equals("byte") ||
         (retType.toString()).equals("long") ||
         (retType.toString()).equals("double") ||
         (retType.toString()).equals("float") ||
         (retType.toString()).equals("java.lang.String")
         ))
         return true;


      org.apache.bcel.generic.Type atypes[] = mg.getArgumentTypes();
      int skipFlag = 0;
      for(int ar=0; ar<atypes.length; ar++) {
         String sig = atypes[ar].getSignature();

         if( !(
            (atypes[ar].toString()).equals("int") ||
            (atypes[ar].toString()).equals("short") ||
            (atypes[ar].toString()).equals("char") ||
            (atypes[ar].toString()).equals("boolean") ||
            (atypes[ar].toString()).equals("byte") ||
            (atypes[ar].toString()).equals("long") ||
            (atypes[ar].toString()).equals("double") ||
            (atypes[ar].toString()).equals("float") ||
            (atypes[ar].toString()).equals("java.lang.String")
            ) || sig.startsWith("[")
         ) {
            skipFlag = 1;
            break;
         }
      }

      if( skipFlag == 1 )
         return true;
      else
         return false;
   }



   /* 
    *  Returns the number of instances of a group of instrcutions in a particular method 
    */
   public int getNumberOfInstanceOfGroup(sandmark.program.Method mg,
                                         String instrStrGrp[],
					 int numInstr)
   {
      org.apache.bcel.generic.InstructionList instrList = mg.getInstructionList();
      if( instrList == null )
         return 0;
      org.apache.bcel.generic.InstructionHandle[] instrHandles =
         instrList.getInstructionHandles();
  
      int vecOccurence=0 ;

      String cmpStr[] = new String[numInstr];

      for(int i=0; i< instrHandles.length-numInstr; i++) {
         int offset = 0;
         for(; offset<numInstr; offset++) {

            org.apache.bcel.generic.InstructionHandle iHandle = instrHandles[i+offset];
            org.apache.bcel.generic.Instruction instr = iHandle.getInstruction();
	    /* to ensure we dont touch cpObj right now, we use 'getOpcodeFromInstr' */
            cmpStr[offset] = instr.toString(); //  helper.getOpcodeFromInstr( instr.toString() );
         }

         if( helper.codeMatch(cmpStr, instrStrGrp, numInstr) )  {
            // org.apache.bcel.generic.InstructionHandle posHandle = instrHandles[i+offset+1];
            vecOccurence++;
         }
      }
      
      return vecOccurence;
   }


   /*
    *  Returns the targeter indices within a given range of indices 
    */
   public int[] getTargeterPointsInRange(int low,
		                         int high,
                                         org.apache.bcel.generic.InstructionList instrList)
   {
      java.util.Vector targeters = new java.util.Vector(5,1);

      org.apache.bcel.generic.InstructionHandle[] tempIh = instrList.getInstructionHandles();
      
      for(int k=low; k<high; k++)
         if( tempIh[k].hasTargeters() ) {
            org.apache.bcel.generic.Instruction instr = tempIh[k].getInstruction();
            if( ((instr.toString()).substring(1)).startsWith("return") )
               continue;
            if( ((instr.toString()).substring(1)).startsWith("store") )
               continue;
            targeters.addElement(new Integer(k));
	 }

      
      if( targeters.size() == 0 )
         return null;
      else {
         int tg[] = new int[targeters.size()];
         for(int k=0; k< targeters.size(); k++) 
            tg[k] = ((Integer)targeters.elementAt(k)).intValue();
         return tg;
      }
   }



   /*
    *  Retrieves the index of a given instructionHandle 
    */
   public int getIndexOfHandle(org.apache.bcel.generic.InstructionHandle instrHandle)
   {
      org.apache.bcel.generic.InstructionHandle tempIh[] =
         instrListObj.getInstructionHandles();

      for(int k=0; k<tempIh.length; k++) 
         if( instrHandle.equals(tempIh[k]) )
            return k;

      throw new Error(" ERROR @ fn. getIndexOfHandle ... " + 
		 " index not found for the instructionHandle ");
   }



   /*
    *  This method returns the current targeter points in the code above the 
    *  lowerIndexLimit point ; 
    *  returns the number of points, and the indices are stored in the 
    *  output parameter 'targetIndices'  
    */
   public int getTargerterIndices(int lowerIndexLimit, int  targetIndices[])
   {
      int k = 0;
      org.apache.bcel.generic.InstructionHandle[] ihs = instrListObj.getInstructionHandles();

      /* we only take targeters till the insert point of the 'getStatic' */
      for(int i=0; i<lowerIndexLimit; i++) {
         org.apache.bcel.generic.InstructionHandle ih = ihs[i];
	 if( ih.hasTargeters() )
	    targetIndices[k++] = i;
      }

      return k;
   }


   /*
    *  Returns the number of instructions in the method 
    */
   public int getNumberOfInstructionsInMethod(sandmark.program.Method mg)
   {
      org.apache.bcel.generic.InstructionList instrlist = mg.getInstructionList();
      return instrlist.size();
   }


   public int getNumberOfClasses(sandmark.program.Application app)
   {
      
      int classIterations = 0;
      java.util.Iterator randomClasses = null;

      /* getting the number of classes in the .jar file */
      randomClasses = app.classes();
	        
      while( randomClasses.hasNext() ) {
         randomClasses.next();
         classIterations++;
      }

      return classIterations;
   }


   /*
    *  Committing the final changes into the jar file 
    */
   public void updateJarFileInfo()
   {
      classObj.mark();
      return;
   }

}


