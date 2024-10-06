package sandmark.diff;

/** A wrapper class for BCEL's Instruction class.
 *  Used by various diff algorithms.
 *  @author Zach Heidepriem   
 */

 public class InstructionWrapper{
     org.apache.bcel.generic.Instruction inst;
     short type, argtype;
     public static final short  BIPUSH = 0, SIPUSH = 1, DCONST = 2, FCONST = 3, 
         ICONST = 4, LCONST = 5;
     public static final short BranchInstruction = 6, CPInstruction = 7, 
         LocalVariableInstruction = 8, RET = 9;
     public static final short CP_INDEX = 0, LV_INDEX = 1, CONST = 2, OFFSET = 3; 
    
     private final static int ILOAD = 21, ILOAD_0 = 26, ILOAD_1 = 27, 
         ILOAD_2 = 28, ILOAD_3 = 29;
     private final static int ISTORE = 54, ISTORE_0 = 59, ISTORE_1 = 60, 
         ISTORE_2 = 61, ISTORE_3 = 62;
     private final static int DLOAD = 24, DLOAD_0 = 38, DLOAD_1 = 39, 
         DLOAD_2 = 40, DLOAD_3 = 41;
     private final static int DSTORE = 57, DSTORE_0 = 71, DSTORE_1 = 72, 
         DSTORE_2 = 73, DSTORE_3 = 74;
     private final static int FLOAD = 23, FLOAD_0 = 34, FLOAD_1 = 35, 
         FLOAD_2 = 36, FLOAD_3 = 37;
     private final static int FSTORE = 56, FSTORE_0 = 67, FSTORE_1 = 68, 
         FSTORE_2 = 69, FSTORE_3 = 70;
     private final static int LLOAD = 22, LLOAD_0 = 30, LLOAD_1 = 31, 
         LLOAD_2 = 32, LLOAD_3 = 33;
     private final static int LSTORE = 55, LSTORE_0 = 63, LSTORE_1 = 64, 
         LSTORE_2 = 65, LSTORE_3 = 66;

     public org.apache.bcel.generic.Instruction getInst(){
	 //System.out.println(inst);
	 return inst;
     }

     public int getOpcode(){              
         int opcode = inst.getOpcode();
         if(opcode == ILOAD_0 || 
            opcode == ILOAD_1 ||
            opcode == ILOAD_2 ||
            opcode == ILOAD_3)
             opcode = ILOAD;
         else if(opcode == ISTORE_0 || 
            opcode == ISTORE_1 ||
            opcode == ISTORE_2 ||
            opcode == ISTORE_3)
             opcode = ISTORE;
         else if(opcode == DSTORE_0 || 
            opcode == DSTORE_1 ||
            opcode == DSTORE_2 ||
            opcode == DSTORE_3)
             opcode = DSTORE;
         else if(opcode == DLOAD_0 || 
            opcode == DLOAD_1 ||
            opcode == DLOAD_2 ||
            opcode == DLOAD_3)
             opcode = DLOAD;
         else if(opcode == FLOAD_0 || 
            opcode == FLOAD_1 ||
            opcode == FLOAD_2 ||
            opcode == FLOAD_3)
             opcode = FLOAD;
         else if(opcode == FSTORE_0 || 
            opcode == FSTORE_1 ||
            opcode == FSTORE_2 ||
            opcode == FSTORE_3)
             opcode = FSTORE;
         else if(opcode == LLOAD_0 || 
            opcode == LLOAD_1 ||
            opcode == LLOAD_2 ||
            opcode == LLOAD_3)
             opcode = LLOAD;
         else if(opcode == LSTORE_0 || 
            opcode == LSTORE_1 ||
            opcode == LSTORE_2 ||
            opcode == LSTORE_3)
             opcode = LSTORE;
         return opcode;
     }
	
	public InstructionWrapper(org.apache.bcel.generic.Instruction i){
	    inst = i;
	    if(inst instanceof org.apache.bcel.generic.BIPUSH){
		type = BIPUSH; 
		argtype = CONST; 
	    }
	    else if(inst instanceof org.apache.bcel.generic.SIPUSH){
		type = SIPUSH;
		argtype = CONST;
	    }
	    else if(inst instanceof org.apache.bcel.generic.DCONST){
		type = DCONST;
		type = CONST;
	    }
	    else if(inst instanceof org.apache.bcel.generic.FCONST){
		type = FCONST;
		argtype = CONST;
	    }
	    else if(inst instanceof org.apache.bcel.generic.ICONST){
		type = ICONST;
		argtype = CONST;
	    }
	    else if(inst instanceof org.apache.bcel.generic.LCONST){
		type = LCONST;
		argtype = CONST;
	    }
	    else if(inst instanceof org.apache.bcel.generic.BranchInstruction){
		type = BranchInstruction;
		argtype = OFFSET;
	    }
	    else if(inst instanceof org.apache.bcel.generic.CPInstruction){
		type = CPInstruction;
		argtype = CP_INDEX;
	    }
	    else if(inst instanceof org.apache.bcel.generic.LocalVariableInstruction){
		type = LocalVariableInstruction; 		
		argtype = LV_INDEX;
	    }
	    else if(inst instanceof org.apache.bcel.generic.RET){
		type = RET; 
		argtype = LV_INDEX;
	    }
	    else{
		type = -1;
		argtype = -1;
	    }
	}
	
	public short getArgType(){
	    return argtype;
	}

	public Number getConstArg() throws Exception {
	    switch(type){
	    case BIPUSH: { return ((org.apache.bcel.generic.BIPUSH)inst).getValue(); }
	    case SIPUSH: { return ((org.apache.bcel.generic.SIPUSH)inst).getValue(); }
	    case DCONST: { return ((org.apache.bcel.generic.DCONST)inst).getValue(); }
	    case FCONST: { return ((org.apache.bcel.generic.FCONST)inst).getValue(); }
	    case ICONST: { return ((org.apache.bcel.generic.ICONST)inst).getValue(); }
	    case LCONST: { return ((org.apache.bcel.generic.LCONST)inst).getValue(); }
	    default: { throw new Exception(); }
	    }
	}

	public int getOffset() throws Exception {
	    if(type == BranchInstruction)
		return ((org.apache.bcel.generic.BranchInstruction)inst).getIndex();
	    else throw new Exception();
	}
	
	public int getLVIndex() throws Exception {
	    int op = inst.getOpcode();
	    if(op == ILOAD_0 || op == ILOAD_1 || op == ILOAD_2 || op == ILOAD_3)
		return op - ILOAD_0;			    
	    else if(op == ISTORE_0 || op == ISTORE_1 || op == ISTORE_2 || op == ISTORE_3)
		return  op - ISTORE_0;
            else if(op == DLOAD_0 || op == DLOAD_1 || op == DLOAD_2 || op == DLOAD_3)
		return  op - DLOAD_0;
            else if(op == DSTORE_0 || op == DSTORE_1 || op == DSTORE_2 || op == DSTORE_3)
		return  op - DSTORE_0;	       
            else if(op == FLOAD_0 || op == FLOAD_1 || op == FLOAD_2 || op == FLOAD_3)
		return  op - FLOAD_0;
            else if(op == FSTORE_0 || op == FSTORE_1 || op == FSTORE_2 || op == FSTORE_3)
		return  op - FSTORE_0;
            else if(op == LLOAD_0 || op == LLOAD_1 || op == LLOAD_2 || op == LLOAD_3)
		return  op - LLOAD_0;
            else if(op == LSTORE_0 || op == LSTORE_1 || op == LSTORE_2 || op == LSTORE_3)
		return  op - LSTORE_0;       

	    if(type == LocalVariableInstruction)
		return ((org.apache.bcel.generic.LocalVariableInstruction)inst).getIndex();
	    else if(type == RET)
		return ((org.apache.bcel.generic.RET)inst).getIndex();
	    else throw new Exception();	       
	}

	public int getCPIndex() throws Exception {
	    if(type == CPInstruction)
		return ((org.apache.bcel.generic.CPInstruction)inst).getIndex();
	    else throw new Exception();
	}

	public void setIndex(int index){
	    if(inst instanceof org.apache.bcel.generic.LocalVariableInstruction)
		((org.apache.bcel.generic.LocalVariableInstruction)inst).setIndex(index); 
	    else if(inst instanceof org.apache.bcel.generic.CPInstruction)	    
		((org.apache.bcel.generic.CPInstruction)inst).setIndex(index);
	}
 }


