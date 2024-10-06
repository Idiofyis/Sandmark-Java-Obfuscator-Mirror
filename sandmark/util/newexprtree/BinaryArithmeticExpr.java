package sandmark.util.newexprtree;

/** Represents a binary arithmetic operation (i.e. ADD).
 *  If you merely specify the type and the operation class,
 *  this uniquely determines which bytecode instruction to emit later.
 *  Emits: DADD, DSUB, DMUL, DDIV, DREM, FADD, FSUB, FMUL, FDIV, FREM,
 *         IADD, ISUB, IMUL, IDIV, IREM, IAND, IOR, IXOR, ISHL, ISHR,
 *         IUSHR, LADD, LSUB, LMUL, LDIV, LREM, LAND, LOR, LXOR, LSHL,
 *         LSHR, LUSHR.
 */
public class BinaryArithmeticExpr extends ArithmeticExpr{
   private static org.apache.bcel.generic.Instruction[] OPS = {
      org.apache.bcel.generic.InstructionConstants.DADD,
      org.apache.bcel.generic.InstructionConstants.DSUB,
      org.apache.bcel.generic.InstructionConstants.DMUL,
      org.apache.bcel.generic.InstructionConstants.DDIV,
      org.apache.bcel.generic.InstructionConstants.DREM,

      org.apache.bcel.generic.InstructionConstants.FADD,
      org.apache.bcel.generic.InstructionConstants.FSUB,
      org.apache.bcel.generic.InstructionConstants.FMUL,
      org.apache.bcel.generic.InstructionConstants.FDIV,
      org.apache.bcel.generic.InstructionConstants.FREM,

      org.apache.bcel.generic.InstructionConstants.IADD,
      org.apache.bcel.generic.InstructionConstants.ISUB,
      org.apache.bcel.generic.InstructionConstants.IMUL,
      org.apache.bcel.generic.InstructionConstants.IDIV,
      org.apache.bcel.generic.InstructionConstants.IREM,
      org.apache.bcel.generic.InstructionConstants.IAND,
      org.apache.bcel.generic.InstructionConstants.IOR,
      org.apache.bcel.generic.InstructionConstants.IXOR,
      org.apache.bcel.generic.InstructionConstants.ISHL,
      org.apache.bcel.generic.InstructionConstants.ISHR,
      org.apache.bcel.generic.InstructionConstants.IUSHR,

      org.apache.bcel.generic.InstructionConstants.LADD,
      org.apache.bcel.generic.InstructionConstants.LSUB,
      org.apache.bcel.generic.InstructionConstants.LMUL,
      org.apache.bcel.generic.InstructionConstants.LDIV,
      org.apache.bcel.generic.InstructionConstants.LREM,
      org.apache.bcel.generic.InstructionConstants.LAND,
      org.apache.bcel.generic.InstructionConstants.LOR,
      org.apache.bcel.generic.InstructionConstants.LXOR,
      org.apache.bcel.generic.InstructionConstants.LSHL,
      org.apache.bcel.generic.InstructionConstants.LSHR,
      org.apache.bcel.generic.InstructionConstants.LUSHR
   };
   private ValueExpr left, right;
   
   /** Creates a BinaryArithmeticExpr with the given operand types,
    *  operator code, and child expressions.
    *  @param type the type of the operands involved.
    *  @param binoptype the operator code (one of the constants defined in ArithmeticExpr).
    *  @param _left the 'left' or first operand.
    *  @param _right the 'right' or second operand.
    */
   public BinaryArithmeticExpr(org.apache.bcel.generic.BasicType type,
                               int binoptype,
                               ValueExpr _left, ValueExpr _right){
      super(type, binoptype);
      left = _left;
      right = _right;
      if (binoptype==NEG)
         throw new RuntimeException("NEG is not a binary operation");
   }

   /** Returns the 'left' or first operand of this operation.
    */
   public ValueExpr getLeftValue(){
      return left;
   }

   /** Sets the left value
    */
   public void setLeftValue(ValueExpr _left){
      left = _left;
   }

   /** Returns the 'right' or second operand of this operation.
    */
   public ValueExpr getRightValue(){
      return right;
   }

   /** Sets the right value
    */
   public void setRightValue(ValueExpr _right){
      right = _right;
   }

   public String toString(){
      String[] ops = {"+", "-", "*", "/", "%", null, "&", "|", "^", "<<", ">>", ">>>"};
      return "BinaryArithmeticExpr["+left+" " + ops[getOperatorType()]+" "+right+"]";
   }

   public java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory){
      java.util.ArrayList result = new java.util.ArrayList();
      result.addAll(left.emitBytecode(factory));
      result.addAll(right.emitBytecode(factory));

      int opoffset = getOperatorType();
      int typeoffset = 0;
      if (opoffset>NEG) opoffset--;
      switch (getType().getType()){
      case T_DOUBLE:
         typeoffset=0;
         break;
      case T_FLOAT:
         typeoffset = 5;
         break;
      case T_INT:
         typeoffset=10;
         break;
      case T_LONG:
         typeoffset=21;
         break;
      }
      result.add(OPS[opoffset+typeoffset]);
      return result;
   }
}
