package sandmark.util.newexprtree;

/** Represents a conditional branch operation.
 *  Conditional branches will take either one or two operands.
 *  If the branch takes only one, it will be considered the 'right'
 *  operand.
 *  Emits: IF_ACMPEQ, IF_ACMPNE, IF_ICMPEQ, IF_ICMPGE, IF_ICMPGT,
 *         IF_ICMPLE, IF_ICMPLT, IF_ICMPNE, IFEQ, IFGE, IFGT, IFLE,
 *         IFLT, IFNE, IFNONNULL, IFNULL.
 */
public class IfExpr extends BranchExpr{
   private ValueExpr left, right;
   private short code;

   private static boolean isOneArg(short code){
      switch(code){
      case IFNULL:
      case IFNONNULL:
      case IFEQ:
      case IFNE:
      case IFGT:
      case IFGE:
      case IFLT:
      case IFLE:
         return true;
      }
      return false;
   }

   private static boolean isTwoArg(short code){
      switch(code){
      case IF_ACMPEQ:
      case IF_ACMPNE:
      case IF_ICMPEQ:
      case IF_ICMPNE:
      case IF_ICMPGT:
      case IF_ICMPGE:
      case IF_ICMPLT:
      case IF_ICMPLE:
         return true;
      }
      return false;
   }


   /*package*/ IfExpr(short _code, ValueExpr _left, ValueExpr _right, 
                      org.apache.bcel.generic.InstructionHandle target){
      super(target);
      code = _code;
      left = _left;
      right = _right;

      if (!(isOneArg(_code) || isTwoArg(_code)))
         throw new IllegalArgumentException("Bad code value: "+_code);

      if (isTwoArg(_code) && _left==null)
         throw new IllegalArgumentException("Two-argument if statement needs two arguments");
      if (_right==null)
         throw new IllegalArgumentException("Null value");
   }

   /*package*/ IfExpr(short _code, ValueExpr _right, 
                      org.apache.bcel.generic.InstructionHandle target){
      this(_code, null, _right, target);
      if (!isOneArg(_code))
         throw new IllegalArgumentException("Bad code value for one-argument if statement");
   }

   /** Constructs an IfExpr for a conditional branch with two operands.
    *  @param _code the type of the if-statement.
    *    (one of IF_ACMPEQ, IF_ACMPNE, IF_ICMPEQ, IF_ICMPGE, IF_ICMPGT, 
    *     IF_ICMPLE, IF_ICMPLT, IF_ICMPNE).
    *  @param _left the 'left' or first operand.
    *  @param _right the 'right' or second operand.
    *  @param target the branch target.
    */
   public IfExpr(short _code, ValueExpr _left, ValueExpr _right, 
                 Expr target){
      super(target);
      code = _code;
      left = _left;
      right = _right;

      if (!(isOneArg(_code) || isTwoArg(_code)))
         throw new IllegalArgumentException("Bad code value: "+_code);

      if (isTwoArg(_code) && _left==null)
         throw new IllegalArgumentException("Two-argument if statement needs two arguments");
      if (_right==null)
         throw new IllegalArgumentException("Null value");
   }

   /** Constructs an IfExpr for a conditional branch with one operand.
    *  @param _code the type of the if-statement.
    *    (one of IFEQ, IFGE, IFGT, IFLE, IFLT, IFNE, IFNONNULL, IFNULL).
    *  @param _value the operand.
    *  @param target the branch target.
    */
   public IfExpr(short _code, ValueExpr value,
                 Expr target){
      this(_code, null, value, target);
      if (!isOneArg(_code))
         throw new IllegalArgumentException("Bad code value for one-argument if statement");
   }

   /** Returns the if-statement type code.
    */
   public short getCode(){
      return code;
   }

   /** Returns the 'right' or second operand.
    *  If this is a single-operand if-statement, 
    *  this returns the single operand.
    */
   public ValueExpr getRightTestValue(){
      return right;
   }

   /** Sets the right operand value.
    */
   public void setRightTestValue(ValueExpr _right){
      right = _right;
   }

   /** Returns the 'left' or first operand.
    *  If this is a singe-operand if-statement,
    *  this returns null.
    */
   public ValueExpr getLeftTestValue(){
      return left;
   }

   /** Sets the left test value
    */
   public void setLeftTestValue(ValueExpr _left){
      left = _left;
   }

   /** Returns the operand of a 1-operand
    *  if-statement. If this is a 2-operand
    *  if-statement, this returns the 'right' operand.
    */
   public ValueExpr getTestValue(){
      return right;
   }

   /** Sets the test value for this if-statement
    */
   public void setTestValue(ValueExpr test){
      right = test;
   }

   public String toString(){
      String result = "IfExpr["+code+",";
      if (left!=null)
         result+=left+",";
      result+=right+"]";
      return result;
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){

      java.util.ArrayList result = new java.util.ArrayList();
      org.apache.bcel.generic.Instruction inst = null;

      switch(code){
      case IF_ACMPEQ:
         inst = new org.apache.bcel.generic.IF_ACMPEQ(null);
      case IF_ACMPNE:
         if(inst==null)
            inst = new org.apache.bcel.generic.IF_ACMPNE(null);
      case IF_ICMPEQ:
         if(inst==null)
            inst = new org.apache.bcel.generic.IF_ICMPEQ(null);
      case IF_ICMPGE:
         if(inst==null)
            inst = new org.apache.bcel.generic.IF_ICMPGE(null);
      case IF_ICMPGT:
         if(inst==null)
            inst = new org.apache.bcel.generic.IF_ICMPGT(null);
      case IF_ICMPLE:
         if(inst==null)
            inst = new org.apache.bcel.generic.IF_ICMPLE(null);
      case IF_ICMPLT:
         if(inst==null)
            inst = new org.apache.bcel.generic.IF_ICMPLT(null);
      case IF_ICMPNE:
         if(inst==null)
            inst = new org.apache.bcel.generic.IF_ICMPNE(null);
         result.addAll(left.emitBytecode(factory));
         break;

      case IFEQ:
         inst = new org.apache.bcel.generic.IFEQ(null);
      case IFGE:
         if(inst==null)
            inst = new org.apache.bcel.generic.IFGE(null);
      case IFGT:
         if(inst==null)
            inst = new org.apache.bcel.generic.IFGT(null);
      case IFLE:
         if(inst==null)
            inst = new org.apache.bcel.generic.IFLE(null);
      case IFLT:
         if(inst==null)
            inst = new org.apache.bcel.generic.IFLT(null);
      case IFNE:
         if(inst==null)
            inst = new org.apache.bcel.generic.IFNE(null);
      case IFNONNULL:
         if(inst==null)
            inst = new org.apache.bcel.generic.IFNONNULL(null);
      case IFNULL:
         if(inst==null)
            inst = new org.apache.bcel.generic.IFNULL(null);
         break;
      }
      result.addAll(right.emitBytecode(factory));
      result.add(inst);
      return result;
   }
}
