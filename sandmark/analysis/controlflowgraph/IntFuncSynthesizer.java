package sandmark.analysis.controlflowgraph;



/**
 * An IntFuncSynthesizer generates code having a control structure
 * that matches a supplied graph.  The generated code is a static
 * int(int) method -- an integer function of one argument.
 */

public abstract class IntFuncSynthesizer extends ControlFlowSynthesizer {



/*
 * probability of choosing a longer instruction than necessary
 */
private static double VERBOSE_CHANCE = 0.1;


   // unspecialized load/store instructions

   private class ILOAD_UNSPEC extends org.apache.bcel.generic.ILOAD {
      ILOAD_UNSPEC(int n) {
         super(47);
         super.n = n;
      }
   }

   private class ISTORE_UNSPEC extends org.apache.bcel.generic.ISTORE {
      ISTORE_UNSPEC(int n) {
         super(47);
         super.n = n;
      }
   }



/**
 * Creates a MethodGen for a public static int(int) method
 * with a random name.
 */
sandmark.program.Method makeFunc(
      org.apache.bcel.generic.InstructionList inslist,
      sandmark.program.Class clazz) {

   org.apache.bcel.generic.Type argtypes[] =
      { org.apache.bcel.generic.Type.INT };

   return new sandmark.program.LocalMethod(
      clazz,
      org.apache.bcel.Constants.ACC_PUBLIC |
         org.apache.bcel.Constants.ACC_STATIC,  // access flags
      org.apache.bcel.generic.Type.INT,         // return type
      argtypes,                                 // argument types
      null,                                     // argument names
      "I" + (int)(1e9 * rng.nextDouble()),      // random method name
      inslist);
               
}

/**
 * Generates either ILOAD_n or ILOAD n.  If both are possible,
 * chooses the longer form with probability VERBOSE_CHANCE.
 */
void genLoad(CodeContext cx, int n) {
   if (VERBOSE_CHANCE < rng.nextDouble()) {
      cx.append(new org.apache.bcel.generic.ILOAD(n));
   } else {
      cx.append(new ILOAD_UNSPEC(n));
   }
}



/**
 * Generates either ISTORE_n or ISTORE n.  If both are possible,
 * chooses the longer form with probability VERBOSE_CHANCE.
 */
void genStore(CodeContext cx, int n) {
   if (VERBOSE_CHANCE < rng.nextDouble()) {
      cx.append(new org.apache.bcel.generic.ISTORE(n));
   } else {
      cx.append(new ISTORE_UNSPEC(n));
   }
}



/**
 * Generates ICONST, BIPUSH, or SIPUSH to load a small constant value.
 */
void genPush(CodeContext cx, int v) {
   if (-1 <= v && v <= 5 && VERBOSE_CHANCE < rng.nextDouble()) {
      cx.append(new org.apache.bcel.generic.ICONST(v));
   } else if ((byte) v == v && VERBOSE_CHANCE < rng.nextDouble()) {
      cx.append(new org.apache.bcel.generic.BIPUSH((byte) v));
   } else if ((short) v == v) {
      cx.append(new org.apache.bcel.generic.SIPUSH((short) v));
   } else {
      throw new IllegalArgumentException(Integer.toString(v));
   }
}



} // class IntFuncSynthesizer

