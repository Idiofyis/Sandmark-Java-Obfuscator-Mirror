package sandmark.util.newexprtree;

/** This class is the abstract parent of all expression tree classes.
 *  An expression is roughly equivalent to a java bytecode instruction,
 *  but some of the details have been abstracted away. Some expressions
 *  may be thought of as having a 'value' or 'result'. These expressions
 *  are represented by the class 'ValueExpr' and its subclasses. 
 *  All other expressions do not have a value, but their sub-expressions do.
 *  Hence, only the root of an expression tree can be a non-ValueExpr.
 */
public abstract class Expr implements org.apache.bcel.Constants{
   /** Abstract method to reproduce java bytecode from this expression tree.
    *  The InstructionFactory must be valid for the intended method.
    *  @param factory a factory using the CPG of the intended destination method of these instructions
    *  @return an ArrayList full of Instruction objects. If any are branches, their targets will be null.
    */
   public abstract java.util.ArrayList emitBytecode
      (org.apache.bcel.generic.InstructionFactory factory);
}
