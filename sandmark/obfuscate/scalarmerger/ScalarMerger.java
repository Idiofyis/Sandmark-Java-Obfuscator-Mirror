package sandmark.obfuscate.scalarmerger;



/**
 * Provides a method obfuscator that combines two int variables
 * into a single long, making access to either more confusing.
 *
 * @author Gregg Townsend
 *    (<a href="mailto:gmt@cs.arizona.edu">gmt@cs.arizona.edu</a>)
 * @version 1.0, August 29, 2003
 */

public class ScalarMerger extends sandmark.obfuscate.MethodObfuscator {



   private static boolean DEBUG = false;



   /**
    * Applies this obfuscation to a single method.
    */
   public void apply(sandmark.program.Method meth) throws Exception {

      if (meth.isInterface() || meth.isAbstract() || meth.isNative()) {
         return;                        // nothing to do
      }

      // Choose two local int variables (ix1 and ix2) for merging.

      int[] vscores = tally(meth);      // tally integer accesses
      skipArgs(meth, vscores);          // invalidate entries of incoming args
      int ix1 = best(vscores);          // get best integer candidate
      int ix2 = best(vscores);          // get next-best candidate
      if (ix1 < 0 || ix2 < 0) {         // if couldn't find two candidates
         return;
      }

      // Allocate a new long variable and initilize it to 1,
      // effectively initializing the two ints to 0 and 1.
      // The actual value doesn't matter because the translation
      // of the old code will reinitialize each half separately.

      int lx = vscores.length;          // index of new long variable

      org.apache.bcel.generic.InstructionList il = meth.getInstructionList();
      org.apache.bcel.generic.InstructionHandle ih =
         il.insert(new org.apache.bcel.generic.LCONST(1));
      il.append(ih, new org.apache.bcel.generic.LSTORE(lx));

      // trace actions, if enabled

      if (DEBUG) {
         System.err.println("ScalarMerger: (" + ix1 + "," + ix2 + ")->" + lx +
            " in " + meth.getClassName() + "." + meth.getName());
      }

      // Replace instructions that operate on the ints

      fixup(il, ix1, ix2, lx);

      // Clean up and exit.

      meth.setInstructionList(il);
      meth.setMaxLocals();
      meth.setMaxStack();
   }



   /**
    * Checks all local variable instructions in an instruction list and
    * tallies variable usage.
    * Returns an array of scores corresponding to local variables.
    * A positive score counts the number of integer accesses.
    * A negative score indicates a variable that is used for a
    * non-integer value.
    */
   private int[] tally(sandmark.program.Method meth) {

      int n = meth.getMaxLocals();
      int[] vscores = new int[n];

      org.apache.bcel.generic.Instruction[] ilist =
         meth.getInstructionList().getInstructions();

      for (int i = 0; i < ilist.length; i++) {
         org.apache.bcel.generic.Instruction ins = ilist[i];
         if (ins instanceof org.apache.bcel.generic.LocalVariableInstruction) {
            int v = ((org.apache.bcel.generic.LocalVariableInstruction)(ins))
               .getIndex();
            if (ins instanceof org.apache.bcel.generic.ILOAD
            ||  ins instanceof org.apache.bcel.generic.IINC
            ||  ins instanceof org.apache.bcel.generic.ISTORE) {
               if (vscores[v] >= 0) {
                  vscores[v]++;
               }
            } else {
               vscores[v] = -1;
            }
         }
      }
      return vscores;
   }



   /**
    * Invalidates tally entries that correspond to method arguments.
    */
   private void skipArgs(sandmark.program.Method meth, int[] vscores) {
      org.apache.bcel.generic.Type[] types = meth.getArgumentTypes();
      int n = meth.isStatic() ? 0 : 1;
      for (int i = 0; i < types.length; i++) {
         n += types[i].getSize();
      }
      for (int i = 0; i < n; i++) {
         vscores[i] = -1;
      }
   }



   /**
    * Scans an array of tallies and returns the index of the most active
    * integer local variable after resetting that variable's score to zero.
    * Ties are broken in favor of lower numbered variables.
    * Returns -1 if there is no local variable with a positive score.
    */
   private int best(int[] vscores) {
      int leader = -1;
      int peak = 0;
      for (int i = 0; i < vscores.length; i++) {
         if (vscores[i] > peak) {
            leader = i;
            peak = vscores[i];
         }
      }
      if (leader >= 0) {
         vscores[leader] = 0;
      }
      return leader;
   }



   /**
    *  Scans the instruction list and replaces instructions
    *  that operate on the chosen integer locals.
    */
   private void fixup(
         org.apache.bcel.generic.InstructionList il, int ix1, int ix2, int lx) {

      org.apache.bcel.generic.InstructionHandle[] hlist =
         il.getInstructionHandles();
      for (int i = 0; i < hlist.length; i++) {
         org.apache.bcel.generic.InstructionHandle ih = hlist[i];
         org.apache.bcel.generic.Instruction ins = ih.getInstruction();
         if (ins instanceof org.apache.bcel.generic.LocalVariableInstruction) {
            int ivx = 
               ((org.apache.bcel.generic.LocalVariableInstruction)ins)
               .getIndex();
            boolean lefthalf = (ivx == ix1);
            boolean righthalf = (ivx == ix2);
            if (lefthalf || righthalf) {
               if (ins instanceof org.apache.bcel.generic.ILOAD) {
                  fixLoad(il, ih, lx, lefthalf);
               } else if (ins instanceof org.apache.bcel.generic.ISTORE) {
                  fixStore(il, ih, lx, lefthalf);
               } else if (ins instanceof org.apache.bcel.generic.IINC) {
                  fixIncr(il, ih, lx, lefthalf);
               } else {
                  throw new java.lang.Error("non-int access to local " + ivx);
               }
            }
         }
      }
   }



   /**
    *  Replaces an ILOAD with a load from one half of lx.
    *  The generated sequence is as follows:
    *  <PRE>
    *
    *   left half   right half  comments
    *   ---------   ----------  ---------------------------------------------
    *   LLOAD lx    LLOAD lx    load combined value
    *               BIPUSH 32   push shift count
    *               LSHR        position to low 32 bits
    *   L2I         L2I         convert low 32 bits to int
    *   
    *  </PRE>
    */
   private void fixLoad(
         org.apache.bcel.generic.InstructionList il,
         org.apache.bcel.generic.InstructionHandle ih,
         int lx, boolean lefthalf) {

      ih.setInstruction(new org.apache.bcel.generic.LLOAD(lx));
      if (lefthalf) {
         ih = il.append(ih, new org.apache.bcel.generic.BIPUSH((byte) 32));
         ih = il.append(ih, new org.apache.bcel.generic.LSHR());
      }
      ih = il.append(ih, new org.apache.bcel.generic.L2I());
   }



   /**
    *  Replace an ISTORE with a store into one half of lx.
    *  The generated sequence is as follows:
    *  <PRE>
    *
    *   left half   right half  comments
    *   ---------   ----------  ---------------------------------------------
    *   I2L         I2L         convert the new int to a long
    *   BIPUSH 32               push shift count
    *   LSHL                    shift into position
    *   LLOAD lx    LLOAD lx    load the old combined value
    *   DUP2_X2     DUP2_X2     hide a second copy below the new value
    *   LXOR        LXOR        compute bitwise difference to new value
    *   LCONST_1    LCONST_1    load long constant 1
    *   LNEG        LNEG        convert to all-ones mask
    *   BIPUSH 32   BIPUSH 32   push shift count
    *   LSHL   !=!= LUSHR       shift to make half ones, half zeroes
    *   LAND        LAND        select the change bits for the correct half
    *   LXOR        LXOR        change the bits in the combined value
    *   LSTORE lx   LSTORE lx   store new combined value
    *   
    *  </PRE>
    */
   private void fixStore(
         org.apache.bcel.generic.InstructionList il,
         org.apache.bcel.generic.InstructionHandle ih,
         int lx, boolean lefthalf) {

      ih.setInstruction(new org.apache.bcel.generic.I2L());
      if (lefthalf) {
         ih = il.append(ih, new org.apache.bcel.generic.BIPUSH((byte)32));
         ih = il.append(ih, new org.apache.bcel.generic.LSHL());
      }
      ih = il.append(ih, new org.apache.bcel.generic.LLOAD(lx));
      ih = il.append(ih, new org.apache.bcel.generic.DUP2_X2());
      ih = il.append(ih, new org.apache.bcel.generic.LXOR());
      ih = il.append(ih, new org.apache.bcel.generic.LCONST(1));
      ih = il.append(ih, new org.apache.bcel.generic.LNEG());
      ih = il.append(ih, new org.apache.bcel.generic.BIPUSH((byte)32));
      if (lefthalf) {
         ih = il.append(ih, new org.apache.bcel.generic.LSHL());
      } else {
         ih = il.append(ih, new org.apache.bcel.generic.LUSHR());
      }
      ih = il.append(ih, new org.apache.bcel.generic.LAND());
      ih = il.append(ih, new org.apache.bcel.generic.LXOR());
      ih = il.append(ih, new org.apache.bcel.generic.LSTORE(lx));
   }
   /*
    *   This fixStore code was also tested, but it is longer and less confusing:
    *   I2L         I2L         convert store value to long
    *   BIPUSH 32   BIPUSH 32   push shift count
    *   LSHL        LSHL        shift int to top of long
    *               BIPUSH 32   push shift count again
    *               LUSHR       shift back down (clears I2D sign bit extension)
    *   LCONST_1    LCONST_1    load long constant 1
    *   LNEG        LNEG        convert to all-ones mask
    *   BIPUSH 32   BIPUSH 32   push shift count
    *   LUSHR  !=!= LSHL        shift to make half ones, half zeroes
    *   LLOAD lx    LLOAD lx    load old combined value
    *   LAND        LAND        preserve old value of other variable
    *   LADD        LADD        combine with new value for this variable
    *   LSTORE lx   LSTORE lx   store combined long value
    */



   /**
    *  Replaces an IINC with an increment of half of lx.
    *  The generated sequence is as follows:
    *  <PRE>
    *
    *   left half   right half  comments
    *   ---------   ----------  ---------------------------------------------
    *   LLOAD lx    LLOAD lx    load combined value
    *               DUP2        duplicate for later use
    *               DUP2        duplicate for later use
    *   BIPUSH n    BIPUSH n    push increment value
    *   I2L         I2L         convert to long
    *   BIPUSH 32               push shift count
    *   LSHL                    position increment value
    *   LADD        LADD        add increment to long value
    *               LXOR        compute bitwise difference vs. old value
    *               LCONST_1    load long constant 1
    *               LNEG        convert to all-ones mask
    *               BIPUSH 32   push shift count
    *               LUSHR       shift to fill top half with zeroes
    *               LAND        isolate changes to lower half
    *               LXOR        apply changes to original value
    *   LSTORE lx   LSTORE lx   store combined long value
    *   
    *  </PRE>
    */
   private void fixIncr(
         org.apache.bcel.generic.InstructionList il,
         org.apache.bcel.generic.InstructionHandle ih,
         int lx, boolean lefthalf) {

      int k =
         ((org.apache.bcel.generic.IINC)(ih.getInstruction())).getIncrement();
      ih.setInstruction(new org.apache.bcel.generic.LLOAD(lx));
      if (!lefthalf) {
         ih = il.append(ih, new org.apache.bcel.generic.DUP2());
         ih = il.append(ih, new org.apache.bcel.generic.DUP2());
      }
      ih = il.append(ih, new org.apache.bcel.generic.BIPUSH((byte)k));
      ih = il.append(ih, new org.apache.bcel.generic.I2L());
      if (lefthalf) {
         ih = il.append(ih, new org.apache.bcel.generic.BIPUSH((byte)32));
         ih = il.append(ih, new org.apache.bcel.generic.LSHL());
      }
      ih = il.append(ih, new org.apache.bcel.generic.LADD());
      if (!lefthalf) {
         ih = il.append(ih, new org.apache.bcel.generic.LXOR());
         ih = il.append(ih, new org.apache.bcel.generic.LCONST(1));
         ih = il.append(ih, new org.apache.bcel.generic.LNEG());
         ih = il.append(ih, new org.apache.bcel.generic.BIPUSH((byte)32));
         ih = il.append(ih, new org.apache.bcel.generic.LUSHR());
         ih = il.append(ih, new org.apache.bcel.generic.LAND());
         ih = il.append(ih, new org.apache.bcel.generic.LXOR());
      }
      ih = il.append(ih, new org.apache.bcel.generic.LSTORE(lx));
   }



   /**
    * Returns "Scalar Merger", the short name of this algorithm.
    */
   public java.lang.String getShortName() {
      return "Merge Local Integers";
   }



   /**
    * Returns "Scalar Merger", the long name of this algorithm.
    */
   public java.lang.String getLongName() {
      return "Scalar Merger";
   }



   /**
    * Returns an HTML description of this obfuscator's function.
    */
   public java.lang.String getAlgHTML() {
      return
         "<HTML><BODY>" +
         "Scalar Merger combines two int variables into a single long" +
         "variable, making access to either more confusing.\n" +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:gmt@cs.arizona.edu\">Gregg Townsend</a> " +
         "</TR></TD>" +
         "</TABLE>" +
         "</BODY></HTML>";

   }



   /**
    * Returns the URL within the source tree
    * of an HTML file describing this obfuscator.
    */
   public java.lang.String getAlgURL() {
      return "sandmark/obfuscate/scalarmerger/doc/help.html";
   }



   /**
    * Returns the name of the author of this obfuscator.
    */
   public java.lang.String getAuthor() {
      return "Gregg Townsend";
   }



   /**
    * Returns the e-mail address of the author of this obfuscator.
    */
   public java.lang.String getAuthorEmail() {
      return "gmt@cs.arizona.edu";
   }



   /**
    * Returns a brief description of this obfuscator.
    */
   public java.lang.String getDescription() {
      return
         "Scalar Merger combines two int variables into a single long " +
         "variable, making access to either more confusing.";
   }

   

   /**
    * Returns a list of modification properties characterizing
    * this obfuscator.
    */
   public sandmark.config.ModificationProperty[] getMutations() {
      return new sandmark.config.ModificationProperty[] {
         sandmark.config.ModificationProperty.I_ADD_LOCAL_VARIABLES,
         sandmark.config.ModificationProperty.I_CHANGE_LOCAL_VARIABLES,
         sandmark.config.ModificationProperty.I_CHANGE_METHOD_BODIES,
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
         sandmark.config.ModificationProperty.I_MODIFY_METHOD_CODE,
         sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE,
         sandmark.config.ModificationProperty.PERFORMANCE_DEGRADE_MED,
      };
   }



   /**
    * Applies the obfuscation to every method in the jar file
    * given as the command argument.
    *
    * <P> Usage:  java sandmark.obfuscate.scalarmerger.ScalarMerger file.jar
    *
    * <P> Writes:  CHANGED.jar
    */
   public static void main(String[] args) throws java.lang.Exception {
      sandmark.program.Application app =
         new sandmark.program.Application(args[0]);
      sandmark.obfuscate.scalarmerger.ScalarMerger obfuscator =
         new sandmark.obfuscate.scalarmerger.ScalarMerger();
      java.util.Iterator itr = app.classes();
      while (itr.hasNext()) {
         sandmark.program.Class cls = (sandmark.program.Class) itr.next();
         sandmark.program.Method[] methods = cls.getMethods();
         for (int i = 0;  i < methods.length; i++) {
            obfuscator.apply(methods[i]);
         }
      }
      app.save("CHANGED.jar");
   }



} // class ScalarMerger
