package sandmark.watermark.arboit;

public class AlgOP {

   static boolean DEBUG = false;
   static boolean EVAL = false;

   protected int EVEN_OP = 0;
   protected int ODD_OP = 1;

   //used for determining whether we are using the rank of the OP or the
   //constants contained in the constructed code
   protected int USE_CONSTS = 0;
   protected int USE_RANK = 1;

   //There are currently 10 opaque predicates in this class
   final int OP_1 = 0;   //this one requires two local variables
   final int OP_2 = 1;
   final int OP_3 = 2;
   final int OP_4 = 3;
   final int OP_5 = 4;
   final int OP_6 = 5;
   final int OP_7 = 6;
   final int OP_8 = 7;
   final int OP_9 = 8;

   protected int OP_1_CONSTS = 8;
   protected int OP_2_CONSTS = 4;
   protected int OP_3_CONSTS = 3;
   protected int OP_4_CONSTS = 0;
   protected int OP_5_CONSTS = 6;
   protected int OP_6_CONSTS = 8;
   protected int OP_7_CONSTS = 88;
   protected int OP_8_CONSTS = 27;
   protected int OP_9_CONSTS = 6;

   protected int NUM_OPS = 9;

   protected int[] EVEN_OPS = {OP_1, OP_2, OP_4, OP_5, OP_6, OP_7, OP_9};
   protected int[] ODD_OPS = {OP_3, OP_8};
  
   sandmark.util.Random op_generator;
   sandmark.util.Random nameGenerator;

   int op_used;
   int[] method_refs;

   public AlgOP(boolean recognition){
       if(!recognition) {
           op_generator = nameGenerator = sandmark.util.Random.getRandom();
           op_used = 0;
           method_refs = new int[NUM_OPS];
           for(int i=0; i < method_refs.length; i++){
              method_refs[i] = -1;
           }           
       }
   }

   public int numParamNeeded(){

     if(op_used == OP_1)
        return 2;
     else
        return 1; 

   }

   public int lastOPUsed(){
      return op_used;
   }

   public boolean insertOpaquePredicate(sandmark.program.Method m,
      java.util.ArrayList usableVars, int ifIndex, int wm, 
      sandmark.util.ConfigProperties props)
      throws sandmark.watermark.WatermarkingException {

      //String consts_or_rank = props.getProperty("ENCODE_AS_CONSTS");
      
      org.apache.bcel.generic.InstructionList insertList = null;
      java.util.ArrayList params = getParams(usableVars);
      String opaque_methods = props.getProperty("Use opaque methods");
      if(opaque_methods.equals("true")){
         int methodRef = makeOpaqueMethod(props, wm, m.getEnclosingClass(),
            params);
         if(methodRef == -1)
            return false;
         insertList = makeInsertList(params, ifIndex, methodRef, m);
      }else
         insertList = makeInsertList(props, wm, params, ifIndex, m);

      if(insertList == null)
         return false;

      org.apache.bcel.generic.InstructionList il = m.getInstructionList();
      org.apache.bcel.generic.InstructionHandle ifHandle =
         UtilFunctions.findIfHandle(m, ifIndex);
      il.append(ifHandle, insertList);
      return true;
   }

   private int makeOpaqueMethod(sandmark.util.ConfigProperties props, int wm,
      sandmark.program.Class cls, java.util.ArrayList params)
      throws sandmark.watermark.WatermarkingException{

      int methodRef = -1;

      int wm_type = UtilFunctions.getWatermarkType(props);
      org.apache.bcel.generic.InstructionList methodIL =
         createOpaqueMethodIL(wm, wm_type);
      if(methodIL == null)
         return methodRef;
      if(props.getProperty("Reuse methods").equals("true")){
         if(method_refs[lastOPUsed()] == -1){
            methodRef = makeTheMethod(cls, methodIL, params);
            method_refs[lastOPUsed()] = methodRef;
         }else
            methodRef = method_refs[lastOPUsed()];
      }else
         methodRef = makeTheMethod(cls, methodIL, params);
      return methodRef;
   }

   private int makeTheMethod(sandmark.program.Class cls,
      org.apache.bcel.generic.InstructionList methodIL,
      java.util.ArrayList params){

      String method_name = "M" + Math.abs(nameGenerator.nextInt());
      int method_access_flags = org.apache.bcel.Constants.ACC_PRIVATE |
         org.apache.bcel.Constants.ACC_STATIC;
      org.apache.bcel.generic.Type return_type = 
         org.apache.bcel.generic.Type.BOOLEAN;
      String[] arg_names = new String[numParamNeeded()]; 
      org.apache.bcel.generic.Type[] arg_types =
         new org.apache.bcel.generic.Type[numParamNeeded()];
      for(int i=0; i < numParamNeeded(); i++){
         arg_names[i] = "A" + Math.abs(nameGenerator.nextInt());
         org.apache.bcel.generic.LocalVariableInstruction lvi =
            (org.apache.bcel.generic.LocalVariableInstruction)params.get(i);
         arg_types[i] = lvi.getType(cls.getConstantPool());
      }
      sandmark.program.LocalMethod newMethod =
         new sandmark.program.LocalMethod(cls, method_access_flags, 
         return_type, arg_types, arg_names, method_name, methodIL);
      newMethod.setMaxStack();
      newMethod.setMaxLocals();
      int newMethodRef = (cls.getConstantPool()).addMethodref(cls.getName(),
         newMethod.getName(), newMethod.getSignature());
      return newMethodRef;
   }


   private org.apache.bcel.generic.InstructionList makeInsertList(
      java.util.ArrayList params, int ifIndex, int methodRef,
      sandmark.program.Method m){

      org.apache.bcel.generic.InstructionList il = null;

      org.apache.bcel.generic.InstructionHandle target =
         UtilFunctions.getIfTarget(m, ifIndex);

     
      il = makeMethodCallInst(params, methodRef, target);

      return il;

   }

   private org.apache.bcel.generic.InstructionList makeMethodCallInst(
      java.util.ArrayList params, int methodRef,
      org.apache.bcel.generic.InstructionHandle target){

      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();
      
      int param1 = ((org.apache.bcel.generic.LocalVariableInstruction)
         params.get(0)).getIndex();
      il.append(new org.apache.bcel.generic.ILOAD(param1));
      if(numParamNeeded() == 2){
         int param2 = ((org.apache.bcel.generic.LocalVariableInstruction)
            params.get(1)).getIndex();
         il.append(new org.apache.bcel.generic.ILOAD(param2));
      }
      il.append(new org.apache.bcel.generic.INVOKESTATIC(methodRef));
      il.append(new org.apache.bcel.generic.IFEQ(target));

      return il;

   }

   private java.util.ArrayList getParams(java.util.ArrayList usableVars){
      java.util.ArrayList params = new java.util.ArrayList();
      params.add((org.apache.bcel.generic.LocalVariableInstruction)usableVars.get(0));
      //if(numParamNeeded() == 2){
         if(usableVars.size() < 2) //numParamNeeded())
            params.add((org.apache.bcel.generic.LocalVariableInstruction)usableVars.get(0));
         else
            params.add((org.apache.bcel.generic.LocalVariableInstruction)usableVars.get(1));
      //}

      return params;
   }

   private org.apache.bcel.generic.InstructionList makeInsertList(
           sandmark.util.ConfigProperties props, int wm, java.util.ArrayList params, 
      int ifIndex, sandmark.program.Method m)
      throws sandmark.watermark.WatermarkingException {

      org.apache.bcel.generic.InstructionList il = null;

      int wm_type = UtilFunctions.getWatermarkType(props);
      il = createOpaqueInsts(wm, params, wm_type);
      if(il == null)
         return il;

      UtilFunctions.fixTarget(il, m, ifIndex);

      return il;
   }

   private org.apache.bcel.generic.InstructionList createOpaqueInsts(
      int wmValue, java.util.ArrayList usableVars, int wm_type) 
      throws sandmark.watermark.WatermarkingException{

      if(DEBUG)System.out.println("creating opaque insts");
      int odd_or_even;
      int chosen_op;
      int index;
      org.apache.bcel.generic.InstructionList il = null;
      int[] varNums;

      if(wm_type == USE_CONSTS){
         if(DEBUG)System.out.println("wm: " + wmValue);
         odd_or_even = wmValue % 2;
         if(DEBUG)System.out.println("odd_or_even: " + odd_or_even);
         if(odd_or_even == EVEN_OP){
            //chose a random op value from the even array
            int even_array_len = EVEN_OPS.length;
            index = Math.abs(op_generator.nextInt()) % (even_array_len - 1);
            chosen_op = EVEN_OPS[index];
         }else{
            //chose a random op value from the odd array
            int odd_array_len = ODD_OPS.length;
            index = op_generator.nextInt() % (odd_array_len - 1);
            chosen_op = ODD_OPS[index];
         }
      }else{
         chosen_op = wmValue;
         if(chosen_op > NUM_OPS){
            throw new sandmark.watermark.WatermarkingException(
               "Watermark values are too large. Cannot watermark.");
         }
      }

      if(chosen_op == OP_1){
         varNums = new int[2];
         varNums[0] =
            ((org.apache.bcel.generic.LocalVariableInstruction)
            usableVars.get(0)).getIndex();
         varNums[1] = 
            ((org.apache.bcel.generic.LocalVariableInstruction)
            usableVars.get(1)).getIndex();
      }else{
         varNums = new int[1];
         varNums[0] =
            ((org.apache.bcel.generic.LocalVariableInstruction)
            usableVars.get(0)).getIndex();
      }

      il = createIL(varNums, chosen_op, wmValue, wm_type);

      return il;
   }

   public org.apache.bcel.generic.InstructionList createOpaqueMethodIL(
      int wmValue, int wm_type) throws sandmark.watermark.WatermarkingException {

      int odd_or_even;
      int chosen_op;
      int index;
      org.apache.bcel.generic.InstructionList il = null;
      int[] varNums;
      
      if(wm_type == USE_CONSTS){
         odd_or_even = wmValue % 2;
         if(odd_or_even == EVEN_OP){
            //chose a random op value from the even array
            int even_array_len = EVEN_OPS.length;
            index = Math.abs(op_generator.nextInt()) % (even_array_len - 1);
            chosen_op = EVEN_OPS[index];
         }else{
            //chose a random op value from the odd array
            int odd_array_len = ODD_OPS.length;
            index = op_generator.nextInt() % (odd_array_len - 1);
            chosen_op = ODD_OPS[index];
         }
      }else{
         chosen_op = wmValue;
         if(chosen_op > NUM_OPS){
            throw new sandmark.watermark.WatermarkingException(
               "Watermark values are too large. Cannot watermark.");
         }
      }
      if(chosen_op == OP_1){
         varNums = new int[2];
         varNums[0] = 0;
         varNums[1] = 1;
      }else{
         if(DEBUG)System.out.println("initializing varNums");
         varNums = new int[1];
         varNums[0] = 0;
      }
      
      il = createIL(varNums, chosen_op, wmValue, wm_type);
      if(il == null)
         return il;

      org.apache.bcel.generic.InstructionHandle lastIH = il.getEnd();
      org.apache.bcel.generic.Instruction lastInst = lastIH.getInstruction();
      org.apache.bcel.generic.IfInstruction ifInst = null;
      if(lastInst instanceof org.apache.bcel.generic.IfInstruction)
         ifInst = (org.apache.bcel.generic.IfInstruction)lastInst;
      
      il.append(new org.apache.bcel.generic.ICONST(1));
      //org.apache.bcel.generic.InstructionHandle extraTargetHandle = il.getEnd();
      il.append(new org.apache.bcel.generic.IRETURN());
      il.append(new org.apache.bcel.generic.ICONST(0));
      org.apache.bcel.generic.InstructionHandle targetHandle = il.getEnd();
      ifInst.setTarget(targetHandle);
      il.append(new org.apache.bcel.generic.IRETURN());

      return il;
   }

   private org.apache.bcel.generic.InstructionList createIL(int[] varNums,
      int chosen_op, int wmValue, int wm_type) throws
      sandmark.watermark.WatermarkingException{

      org.apache.bcel.generic.InstructionList il = null;
      int num_con_used = 0;
      int coefficient = 0; 

      if(wm_type == USE_CONSTS){
         if(DEBUG)System.out.println("op: " + chosen_op);
         num_con_used = consts_used(chosen_op);
         if(DEBUG)System.out.println("num_con_used: " + num_con_used);
         int value_left = wmValue - num_con_used;
         coefficient = value_left / 2;
         if(DEBUG)System.out.println("wmValue: " + wmValue);
         if(DEBUG)System.out.println("coefficient: " + coefficient);
         if(DEBUG)System.out.println("(byte)coe: " + (byte)coefficient);
         if(coefficient == 0){
               return il;
         }
      }
      if(EVAL)System.out.println("chosen_op: " + chosen_op); 
      op_used = chosen_op;
      il = createSpecIL(varNums, chosen_op, coefficient, wm_type);

      return il;

   }

   private org.apache.bcel.generic.InstructionList createSpecIL(int[] varNums, 
      int chosen_op, int coefficient, int wm_type) throws
      sandmark.watermark.WatermarkingException{
   
      org.apache.bcel.generic.InstructionList il = null;

      switch(chosen_op){
         case OP_1:
            if(varNums.length < 2)
               break;
            il = createOP1(varNums[0], varNums[1], coefficient, wm_type);
            break;
         case OP_2:
            il = createOP2(varNums[0], coefficient, wm_type);
            break;
         case OP_3:
            il = createOP3(varNums[0], coefficient, wm_type);
            break;
         case OP_4:
            il = createOP4(varNums[0], coefficient, wm_type);
            break;
         case OP_5:
            il = createOP5(varNums[0], coefficient, wm_type);
            break;
         case OP_6:
            il = createOP6(varNums[0], coefficient, wm_type);
            break;
         case OP_7:
            il = createOP7(varNums[0], coefficient, wm_type);
            break;
         case OP_8:
            il = createOP8(varNums[0], coefficient, wm_type);
            break;
         case OP_9:
            il = createOP9(varNums[0], coefficient, wm_type);
            break;
         default:
            throw new sandmark.watermark.WatermarkingException(
               "Watermark values are too large. Cannot watermark.");
      }

      return il;

   }

   /*
    * This method implements 7(y*y) - 1 != (x*x) 
    */
   private org.apache.bcel.generic.InstructionList createOP1(int var1, int var2,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();

      il.append(new org.apache.bcel.generic.ILOAD(var2));
      il.append(new org.apache.bcel.generic.ILOAD(var2));
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.BIPUSH((byte)7));
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.ICONST(1));
      il.append(new org.apache.bcel.generic.ISUB());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.IMUL());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IF_ICMPEQ(null));

      return il;
   }

   /*
    * This method implements ((x*x) / 2) % 2 == 0
    */
   private org.apache.bcel.generic.InstructionList createOP2(int var1,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();

      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.ICONST(2));
      il.append(new org.apache.bcel.generic.IDIV());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.ICONST(2));
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IREM());
      il.append(new org.apache.bcel.generic.IFNE(null));

      return il;
   }


   /*
    * This method implements x(x+1) % 2 == 0
    */
   private org.apache.bcel.generic.InstructionList createOP3(int var1,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();

      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ICONST(1));
      il.append(new org.apache.bcel.generic.IADD());
      il.append(new org.apache.bcel.generic.IMUL());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.ICONST(2));
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IREM());
      il.append(new org.apache.bcel.generic.IFNE(null));

      return il;
   }


   /*
    * This method implements (x*x) >= 0 
    */
   private org.apache.bcel.generic.InstructionList createOP4(int var1,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();

      if(DEBUG)System.out.println("var1: " + var1);

      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.IMUL());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.ICONST(0));
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IF_ICMPLT(null));

      return il;
   }

   /*
    * This method implements x(x+1)(x+2) % 3 == 0
    */
   private org.apache.bcel.generic.InstructionList createOP5(int var1,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();

      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ICONST(1));
      il.append(new org.apache.bcel.generic.IADD());
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ICONST(2));
      il.append(new org.apache.bcel.generic.IADD());
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.IMUL());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.ICONST(3));
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IREM());
      il.append(new org.apache.bcel.generic.IFNE(null));

      return il;
   }

   /*
    * This method implements ((x*x) + 1) % 7 != 0
    */
   private org.apache.bcel.generic.InstructionList createOP6(int var1,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();

      if(DEBUG)System.out.println("------------coefficient: " + coefficient);
      if(DEBUG)System.out.println("++++++++++++(byte)coeff: " +
         (byte)coefficient);
      if(DEBUG)System.out.println("************(short)coef: " +
         (short)coefficient);

      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.ICONST(1));
      il.append(new org.apache.bcel.generic.IADD());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.BIPUSH((byte)7));
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IREM());
      il.append(new org.apache.bcel.generic.IFEQ(null));

      return il;
   }

   /*
    * This method implements ((x*x) + x + 7) % 81 != 0
    */
   private org.apache.bcel.generic.InstructionList createOP7(int var1,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();

      if(DEBUG)System.out.println("int co1: " + coefficient);

      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.IADD());
      il.append(new org.apache.bcel.generic.BIPUSH((byte)7));
      il.append(new org.apache.bcel.generic.IADD());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.BIPUSH((byte)81));
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IREM());
      il.append(new org.apache.bcel.generic.IFEQ(null));

      return il;
   }

   /*
    * This method implements (4(x*x) + 4) % 19 != 0
    */
   private org.apache.bcel.generic.InstructionList createOP8(int var1,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il =
         new org.apache.bcel.generic.InstructionList();

      il.append(new org.apache.bcel.generic.ICONST(4));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.ICONST(4));
      il.append(new org.apache.bcel.generic.IADD());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.BIPUSH((byte)19));
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IREM());
      il.append(new org.apache.bcel.generic.IFEQ(null));

      return il;
   }

   /*
    * This method implements ((x*x)(x+1)(x+1)) % 4 == 0
    */
   private org.apache.bcel.generic.InstructionList createOP9(int var1,
      int coefficient, int wm_type){
      org.apache.bcel.generic.InstructionList il = 
         new org.apache.bcel.generic.InstructionList();

      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ICONST(1));
      il.append(new org.apache.bcel.generic.IADD());
      il.append(new org.apache.bcel.generic.IMUL());
      il.append(new org.apache.bcel.generic.ILOAD(var1));
      il.append(new org.apache.bcel.generic.ICONST(1));
      il.append(new org.apache.bcel.generic.IADD());
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.ICONST(4));
      if(wm_type == USE_CONSTS){
         if(coefficient <= 5 && coefficient >= -1)
            il.append(new org.apache.bcel.generic.ICONST(coefficient));
         else if(coefficient < 128 && coefficient >= -128)
					il.append(new org.apache.bcel.generic.BIPUSH((byte)coefficient));
            else
               il.append(new org.apache.bcel.generic.SIPUSH((short)coefficient));
         il.append(new org.apache.bcel.generic.IMUL());
      }
      il.append(new org.apache.bcel.generic.IREM());
      il.append(new org.apache.bcel.generic.IFNE(null));

      return il;
   }



   private int consts_used(int chosen_op){

      switch(chosen_op){
         case OP_1:
            return OP_1_CONSTS;
         case OP_2:
            return OP_2_CONSTS;
         case OP_3:
            return OP_3_CONSTS;
         case OP_4:
            return OP_4_CONSTS;
         case OP_5:
            return OP_5_CONSTS;
         case OP_6:
            return OP_6_CONSTS;
         case OP_7:
            return OP_7_CONSTS;
         case OP_8:
            return OP_8_CONSTS;
         case OP_9:
            return OP_9_CONSTS;
         //case OP_10:
            //return OP_10_CONSTS;
         default:
            return -1;
      }
   }


   public int isOpaque(java.util.ArrayList instructions, int wm_type){
      int value = 0;

      if((value = check1(instructions, wm_type)) != 0)
          return value;
      if((value = check2(instructions, wm_type)) != 0)
          return value;
      if((value = check3(instructions, wm_type)) != 0)
          return value;
      if((value = check4(instructions, wm_type)) != 0)
          return value;
      if((value = check5(instructions, wm_type)) != 0)
          return value;
      if((value = check6(instructions, wm_type)) != 0)
          return value;
      if((value = check7(instructions, wm_type)) != 0)
          return value;
      if((value = check8(instructions, wm_type)) != 0)
          return value;
      if((value = check9(instructions, wm_type)) != 0)
          return value;

      return value;
   }

   private int check1(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;

      if(wm_type == USE_CONSTS){
         if(instLen != 19 && instLen != 15)
            return 0;
         
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
				return 0;
         else{
            org.apache.bcel.generic.BIPUSH biinst = 
               (org.apache.bcel.generic.BIPUSH)inst;
            value += (biinst.getValue()).intValue();
         }  
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ICONST))
				return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ISUB))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(12);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(13);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(14);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IF_ICMPEQ))
            return 0;
      }else{ //we are using rank
         if(instLen != 15 && instLen != 11)
            return 0;

         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ICONST))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ISUB))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IF_ICMPEQ))
            return 0;

         value = OP_1;
      }

      if(EVAL)System.out.println("found op1");
      return value;
   }


   private int check2(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;


      if(wm_type == USE_CONSTS){
         if(instLen != 16 && instLen != 12)
            return 0;
         
       
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IDIV))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFNE))
            return 0;

      }else{//use rank
         if(instLen != 12 && instLen != 8)
            return 0;
     
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IDIV))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFNE))
            return 0;
 
         value = OP_2;
      }

      if(EVAL)System.out.println("found op2");
      return value;
   }

   private int check3(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;

      if(wm_type == USE_CONSTS){
         if(instLen != 16 && instLen != 12)
            return 0;
      
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFNE))
            return 0;

      }else{//use rank
         if(instLen != 12 && instLen != 8)
            return 0;
         
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFNE))
            return 0;

         value = OP_3;
      }

      if(EVAL)System.out.println("found op3");
      return value;
   }

   private int check4(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;

      if(wm_type == USE_CONSTS){
         if(instLen != 13 && instLen != 9)
            return 0;

         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IF_ICMPLT))
            return 0;

      }else{//use rank
         if(instLen != 9 && instLen != 5)
            return 0;
      
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IF_ICMPLT))
            return 0;

         value = OP_4;
      }

      if(EVAL)System.out.println("found op4");
      return value;
   }

   private int check5(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;

      if(wm_type == USE_CONSTS){
         if(instLen != 20 && instLen != 16)
            return 0;

         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(12);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(13);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(14);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(15);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFNE))
            return 0;

      }else{//use rank
         if(instLen != 16 && instLen != 12)
            return 0;
         
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFNE))
            return 0;

         value = OP_5;
      }

      if(EVAL)System.out.println("found op5");
      return value;
   }

   private int check6(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;

      if(wm_type == USE_CONSTS){
         if(instLen != 16 && instLen != 12)
            return 0;

         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
            return 0;
         else{
            org.apache.bcel.generic.BIPUSH biinst =
               (org.apache.bcel.generic.BIPUSH)inst;
            value += (biinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFEQ))
            return 0;

      }else{//use rank
         if(instLen != 12 && instLen != 8)
            return 0;
     
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFEQ))
            return 0;
 
         value = OP_6;
      }

      if(EVAL)System.out.println("found op6");
      return value;
   }

   private int check7(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;

      if(wm_type == USE_CONSTS){
         if(instLen != 18 && instLen != 14)
            return 0;

         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ILOAD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
            return 0;
         else{
            org.apache.bcel.generic.BIPUSH binst =
               (org.apache.bcel.generic.BIPUSH)inst;
            value += (binst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
            return 0;
         else{
            org.apache.bcel.generic.BIPUSH biinst =
               (org.apache.bcel.generic.BIPUSH)inst;
            value += (biinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(12);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(13);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFEQ))
            return 0;

      }else{//use rank
         if(instLen != 14 && instLen != 10)
            return 0;
     
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ILOAD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFEQ))
            return 0;
 
         value = OP_7;
      }

      if(EVAL)System.out.println("found op7");
      return value;
   }

   private int check8(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;

      if(wm_type == USE_CONSTS){
         if(instLen != 18 && instLen != 14)
            return 0;

         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
            return 0;
         else{
            org.apache.bcel.generic.BIPUSH biinst =
               (org.apache.bcel.generic.BIPUSH)inst;
            value += (biinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(12);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(13);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFEQ))
            return 0;

      }else{//use rank
         if(instLen != 14 && instLen != 10)
            return 0;
     
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.BIPUSH))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFEQ))
            return 0;

         value = OP_8;
      }

      if(EVAL)System.out.println("found op8");
      return value;
   }

   private int check9(java.util.ArrayList instructions, int wm_type){
      int value = 0;
      int instLen = instructions.size();

      org.apache.bcel.generic.InstructionHandle ih = null;
      org.apache.bcel.generic.Instruction inst = null;

      if(wm_type == USE_CONSTS){
         if(instLen != 21 && instLen != 17)
            return 0;

         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ILOAD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ILOAD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(12);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         else{
            org.apache.bcel.generic.ICONST cinst =
               (org.apache.bcel.generic.ICONST)inst;
            value += (cinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(13);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ConstantPushInstruction))
            return 0;
         else{
            org.apache.bcel.generic.ConstantPushInstruction cpinst = 
               (org.apache.bcel.generic.ConstantPushInstruction)inst;
            value += (cpinst.getValue()).intValue();
         }
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(14);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IMUL))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(15);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(16);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFNE))
            return 0;

      }else{//use rank
         if(instLen != 17 && instLen != 13)
            return 0;
     
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(0);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(1);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.ILOAD))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(2);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
			ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(3);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ILOAD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(4);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(5);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(6);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IMUL))
				return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(7);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ILOAD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(8);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(9);
			inst = ih.getInstruction();
			if(!(inst instanceof org.apache.bcel.generic.IADD))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(10);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.ICONST))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(11);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IREM))
            return 0;
         ih = (org.apache.bcel.generic.InstructionHandle)instructions.get(12);
			inst = ih.getInstruction();
         if(!(inst instanceof org.apache.bcel.generic.IFNE))
            return 0;

         value = OP_9;
      }

      if(EVAL)System.out.println("found op9");
      return value;
   }


   public boolean isPossible(String sig){
      if(sig.equals("(I)Z") || sig.equals("(II)Z"))
         return true;
      //if(sig.equals("(II)Z"))
         //return true;
      return false;
   }

}//end class
