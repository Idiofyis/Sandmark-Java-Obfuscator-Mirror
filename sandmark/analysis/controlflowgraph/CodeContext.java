package sandmark.analysis.controlflowgraph;



/**
 * A CodeContext encapsulates an InstructionHandle and an InstructionList
 * to simplify the generation of sequences of code.  Instance methods
 * of the CodeContext update the InstructionHandle (the "code pointer")
 * when called to insert or append new instructions in the underlying
 * InstructionList.
 */

public class CodeContext {

   private org.apache.bcel.generic.InstructionList inslist;
   private org.apache.bcel.generic.InstructionHandle handle;



/**
 * Constructs a CodeContext for the given InstructionList
 * and initializes the code pointer to null.
 */
public CodeContext(org.apache.bcel.generic.InstructionList l) {
   this(l, null);
}



/**
 * Constructs a CodeContext for the given InstructionList
 * and initializes the code pointer to the given value.
 */
public CodeContext(org.apache.bcel.generic.InstructionList l,
      org.apache.bcel.generic.InstructionHandle h) {

   inslist = l;
   handle = h;
}



/**
 * Returns the code pointer.
 */
public org.apache.bcel.generic.InstructionHandle getHandle() {
   return handle;
}



/**
 * Sets the code pointer.
 */
public void getHandle(org.apache.bcel.generic.InstructionHandle h) {
   handle = h;
}



/**
 * Appends an instruction at the code pointer, and updates it.
 */
public void append(org.apache.bcel.generic.Instruction ins) {
   if (handle == null) {
      handle = inslist.append(ins);
   } else if (ins instanceof org.apache.bcel.generic.BranchInstruction) {
      handle = inslist.append(
         handle, (org.apache.bcel.generic.BranchInstruction) ins);
   } else {
      handle = inslist.append(handle, ins);
   }
}



/**
 * Inserts an instruction at the code pointer, and updates it.
 */
public void insert(org.apache.bcel.generic.Instruction ins) {
   if (handle == null) {
      handle = inslist.insert(ins);
   } else if (ins instanceof org.apache.bcel.generic.BranchInstruction) {
      handle = inslist.insert(
         handle, (org.apache.bcel.generic.BranchInstruction) ins);
   } else {
      handle = inslist.insert(handle, ins);
   }
}



/**
 * Returns a concise string representation of this CodeContext.
 */
public String toString() {
   return "CodeContext(inslist," + handle + ")";
}



} // class CodeContext

