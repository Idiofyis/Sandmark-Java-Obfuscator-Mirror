package sandmark.util.newexprtree;

/** Represents the entire range of arithmetic java instructions. There
 *  are 2 subclasses: BinaryArithmeticExpr and NegateArithmeticExpr 
 *  (negation is the only unary operation). The integer constants defined
 *  in this class are used in BinaryArithmeticExpr.
 */
public abstract class ArithmeticExpr extends ValueExpr{
   public static final int ADD  = 0;
   public static final int SUB  = 1;
   public static final int MUL  = 2;
   public static final int DIV  = 3;
   public static final int REM  = 4;
   public static final int NEG  = 5;
   public static final int AND  = 6;
   public static final int OR   = 7;
   public static final int XOR  = 8;
   public static final int SHL  = 9;
   public static final int SHR  = 10;
   public static final int USHR = 11;
   
   /** This will be one of the predefined constants in this class.
    */
   private int optype;
   
   /** Creates an ArithmeticExpr.
    *  @param _type the result type of this operation.
    *  @param _optype one of the integer constants defined in this class.
    */
   public ArithmeticExpr(org.apache.bcel.generic.BasicType _type,
                         int _optype){
      super(_type);
      optype = _optype;
      if (optype<ADD || optype>USHR)
         throw new RuntimeException("Bad operator type: "+optype);
   }

   /** Returns the operator type (one of the constants defined in this class).
    */
   public final int getOperatorType(){
      return optype;
   }
}
