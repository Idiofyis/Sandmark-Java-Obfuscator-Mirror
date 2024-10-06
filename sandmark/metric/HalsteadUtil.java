package sandmark.metric;

/** This class implements the various modules required to evaluate the
 *  Halstead's complexity measure.
 */
public class HalsteadUtil
{
   private int numOperators;
   private int numOperands;
   private int numDistinctOperators;
   private int numDistinctOperands;

   private java.util.Vector operTypeVector;
   private java.util.Vector operandVector;

   private sandmark.program.Method methodgen = null;

   public HalsteadUtil(sandmark.program.Method mg)
   {
      numOperators = 0;
      numOperands = 0;
      numDistinctOperators = 0;
      numDistinctOperands = 0;

      operTypeVector = new java.util.Vector(10,2);
      operandVector = new java.util.Vector(10,2);

      methodgen = mg;
   }


   /**
    *   Returns the value corresponding to the given measure
    *   @param measure the symbolic name of the measure whose value is needed
    *   @return the value corresponding to that measure
    */
   public int getMeasure(String measure)
   {
      if (measure.equals("n1"))
         return(numDistinctOperators);
      if (measure.equals("n2"))
         return(numDistinctOperands);
      if (measure.equals("N1"))
         return(numOperators);
      if (measure.equals("N2"))
         return(numOperands);
      return -1;
   }

   /**
    *   Sets measure with the corresponding value
    *   @param measure the symbolic name of the measure
    *   @param value the value to be set
    */
   public void setMeasure(String measure, int value)
   {
      if (measure.equals("n1"))
         numDistinctOperators = value;
      if (measure.equals("n2"))
         numDistinctOperands = value;
      if (measure.equals("N1"))
         numOperators = value;
      if (measure.equals("N2"))
         numOperands = value;
   }


   /**
    *   Evaluates the Halstead measures n1,n2,N1 and N2
    *   @return resultVector result vector containing the Halstead measures
    *           in the order N1, N2, n1, n2
    */
   public java.util.Vector evalMeasures()
   {
      org.apache.bcel.generic.InstructionList il = 
         methodgen.getInstructionList();

      if (il==null)
         return null;

      return evalMeasures(il.iterator());
   }

   // this method cannot be removed, because NestingLevelComplexity 
   // relies on it!!
   protected java.util.Vector evalMeasures(java.util.Iterator ins){
      java.util.Vector resultVector = new java.util.Vector(10,2);

      if (!ins.hasNext())
         return null;

      while(ins.hasNext()){
         org.apache.bcel.generic.Instruction instruction =
            ((org.apache.bcel.generic.InstructionHandle)ins.next()).getInstruction();

         String operatorType = getOperatorType(instruction);

         if (!operatorType.equals("none")) {
            numOperators++;
            if (isOperatorDistinct(operatorType))
               numDistinctOperators++;
         }

         String operand = getOperand(instruction);

         if(!operand.equals("none")) {
            numOperands++;
            if (isOperandDistinct(operand))
               numDistinctOperands++;
         }
      }

      resultVector.addElement(new Integer(numOperators));
      resultVector.addElement(new Integer(numDistinctOperators));
      resultVector.addElement(new Integer(numOperands));
      resultVector.addElement(new Integer(numDistinctOperands));

      return(resultVector);
   }

   /**
    *  Gets the operator type of the given instruction
    *  returns 'none' if not an operator instruction
    *  @param opcode the instruction to be analyzed
    *  @return operType the operator type of the instruction
    */
   public String getOperatorType(org.apache.bcel.generic.Instruction opcode)
   {
      String operType = "none";

      if ( ( opcode instanceof org.apache.bcel.generic.IFEQ )
           || ( opcode instanceof org.apache.bcel.generic.IF_ICMPEQ )
           || ( opcode instanceof org.apache.bcel.generic.IF_ACMPEQ ) )
         operType = "condjumpeq";

      else if ( ( opcode instanceof org.apache.bcel.generic.IFNE )
                || ( opcode instanceof org.apache.bcel.generic.IF_ICMPNE )
                || ( opcode instanceof org.apache.bcel.generic.IF_ACMPNE ))
         operType = "condjumpne";

      else if ( ( opcode instanceof org.apache.bcel.generic.IFGE )
                || ( opcode instanceof org.apache.bcel.generic.IF_ICMPGE ))
         operType = "condjumpge";

      else if ( ( opcode instanceof org.apache.bcel.generic.IFGT )
                || ( opcode instanceof org.apache.bcel.generic.IF_ICMPGT ))
         operType = "condjumpgt";

      else if ( ( opcode instanceof org.apache.bcel.generic.IFLE )
                || ( opcode instanceof org.apache.bcel.generic.IF_ICMPLE ))
         operType = "condjumple";

      else if ( ( opcode instanceof org.apache.bcel.generic.IFLT )
                || ( opcode instanceof org.apache.bcel.generic.IF_ICMPLT ))
         operType = "condjumplt";

      else if ( opcode instanceof org.apache.bcel.generic.IFNULL)
         operType = "condjumpnull";

      else if ( opcode instanceof org.apache.bcel.generic.IFNONNULL)
         operType = "condjumpnonnull";

      else if ( opcode instanceof org.apache.bcel.generic.StoreInstruction )
         operType = "store";
      else if ( opcode instanceof org.apache.bcel.generic.DADD ||
                opcode instanceof org.apache.bcel.generic.FADD ||
                opcode instanceof org.apache.bcel.generic.IADD ||
                opcode instanceof org.apache.bcel.generic.LADD)
         operType = "add";
      else if ( opcode instanceof org.apache.bcel.generic.DSUB ||
                opcode instanceof org.apache.bcel.generic.FSUB ||
                opcode instanceof org.apache.bcel.generic.ISUB ||
                opcode instanceof org.apache.bcel.generic.LSUB)
         operType = "sub";
      else if ( opcode instanceof org.apache.bcel.generic.DMUL ||
                opcode instanceof org.apache.bcel.generic.FMUL ||
                opcode instanceof org.apache.bcel.generic.IMUL ||
                opcode instanceof org.apache.bcel.generic.LMUL)
         operType = "mul";
      else if ( opcode instanceof org.apache.bcel.generic.DDIV ||
                opcode instanceof org.apache.bcel.generic.FDIV ||
                opcode instanceof org.apache.bcel.generic.IDIV ||
                opcode instanceof org.apache.bcel.generic.LDIV)
         operType = "div";
      else if ( opcode instanceof org.apache.bcel.generic.DREM ||
                opcode instanceof org.apache.bcel.generic.FREM ||
                opcode instanceof org.apache.bcel.generic.IREM ||
                opcode instanceof org.apache.bcel.generic.LREM)
         operType = "rem";
      else if ( opcode instanceof org.apache.bcel.generic.GotoInstruction)
         operType = "goto";
      else if ( opcode instanceof org.apache.bcel.generic.IINC)
         operType="inc";
      else if ( opcode instanceof org.apache.bcel.generic.PUTSTATIC)
         operType = "putstatic";
      else if ( opcode instanceof org.apache.bcel.generic.PUTFIELD)
         operType = "putfield";
      else if ( opcode instanceof org.apache.bcel.generic.NEW)
         operType = "new";
      else if (opcode instanceof org.apache.bcel.generic.Select)
         operType = "switch";
      else if ( opcode instanceof org.apache.bcel.generic.InvokeInstruction)
         operType = "invoke";

      return(operType);
   }



   /**
    *   Maintains a vector of operator types that are already present
    *   in the method. Returns whether the given operator type is distinct
    *   or not.
    *   @param operType the operator type to be analyzed
    */
   private boolean isOperatorDistinct(String operType)
   {
      if ( !operTypeVector.contains(operType) ) {
         operTypeVector.addElement(operType);
         return true;
      }
      return false;
   }

   /**
    *   Returns the operand of the given 'operand instruction' if present
    *   returns 'none' if not an operand instruction
    *   @param instruc the instruction to be analyzed
    *
    */
   public String getOperand(org.apache.bcel.generic.Instruction instruc)
   {
      String result = "none";
      if ( ((instruc.toString()).indexOf("load") != -1 ) ||
           ((instruc.toString()).indexOf("store") != -1 ) ||
           (instruc instanceof org.apache.bcel.generic.GETFIELD) ||
           (instruc instanceof org.apache.bcel.generic.GETSTATIC) ||
           (instruc instanceof org.apache.bcel.generic.IINC) ) {

         String str = instruc.toString();
         int from = str.indexOf(' ');
         int to = str.lastIndexOf(' ');
         if(from==to)
            result = str.substring(from+1);
         else
            result = str.substring(from+1, to);
      }
      return result;
   }


   /**
    *   Maintains a vector of operands that are already present
    *   in the method. Returns whether the given operand is distinct
    *   or not.
    *   @param operand the operand to be analyzed
    */
   private boolean isOperandDistinct(String operand)
   {
      if ( !operandVector.contains(operand) ) {
         operandVector.addElement(operand);
         return true;
      }
      return false;
   }
}



