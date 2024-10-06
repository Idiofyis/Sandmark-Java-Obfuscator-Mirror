package sandmark.program.util;

/**
 * Represents an instruction that references a program object
 * such as a class, method, or field.
 * Also provides static methods for locating and modifying
 * such references.
 * <STRONG>The methods in this class have been specified but
 * not yet implemented.</STRONG>
 */

public class Reference {


   private sandmark.program.Method meth;
   private org.apache.bcel.generic.InstructionList ilist;
   private org.apache.bcel.generic.InstructionHandle handle;



   /**
    * Constructs a reference for an instruction in a method.
    *
    * @param m the method containing the instruction
    * @param il the instruction list that contains the instruction
    * @param ih the instruction handle for the instruction
    */
   public Reference(sandmark.program.Method m,
         org.apache.bcel.generic.InstructionList il,
         org.apache.bcel.generic.InstructionHandle ih) {

      meth = m;
      ilist = il;
      handle = ih;
   }



   /**
    * Returns the method that contains this instruction.
    *
    * @return the enclosing method of this reference
    */
   public sandmark.program.Method getMethod() {
      return meth;
   }


   /**
    * Returns the instruction list that contains this instruction.
    *
    * @return the enclosing instruction list for this reference
    */
   public org.apache.bcel.generic.InstructionList getInstructionList() {
      return ilist;
   }



   /**
    * Returns the instruction handle that encapsulates this instruction.
    *
    * @return the instruction handle for this reference
    */
   public org.apache.bcel.generic.InstructionHandle getInstructionHandle() {
      return handle;
   }



   /**
    * Returns an iterator over all references to the given class.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param c the class to find references for
    * @return an iterator that on each call to <code>next()</code> will
    * produce a new <code>sandmark.program.util.Reference</code> object.
    */
   public static java.util.Iterator references(sandmark.program.Class c) {
      throw new java.lang.UnsupportedOperationException("unimplemented");
   }



   /**
    * Returns an iterator over all references to the given field.
    * Note: this method does not guarantee that the iterator will
    * return "hardwired" references to a field.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param f the field to find references for
    * @return an iterator that on each call to <code>next()</code> will
    * produce a new <code>sandmark.program.util.Reference</code> object.
    */
   public static java.util.Iterator references(sandmark.program.Field f) {
      throw new java.lang.UnsupportedOperationException("unimplemented");
   }



   /**
    * Returns an iterator over all references to the given method.
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param m the method to find references for
    * @return an iterator that on each call to <code>next()</code> will
    * produce a new <code>sandmark.program.util.Reference</code> object.
    */
   public static java.util.Iterator references(sandmark.program.Method m) {
      throw new java.lang.UnsupportedOperationException("unimplemented");
   }



   /**
    * Removes all references to a method.
    * Each invoke instruction is replaced by code that pops the arguments,
    * then pushes 0 or null if the method has a return value (the default
    * value for the return type).
    * <STRONG>Not yet implemented.</STRONG>
    *
    * @param m the method to remove reference to
    */
   public static void deleteAll(sandmark.program.Method m) {
      throw new java.lang.UnsupportedOperationException("unimplemented");
   }
}

