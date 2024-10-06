package sandmark.analysis.controlflowgraph;



/**
 * A VoidMethodSynthesizer generates code having a control structure
 * that matches a supplied graph.  The generated code is a static
 * void method with no arguments.
 */
public abstract class VoidMethodSynthesizer extends ControlFlowSynthesizer {


/**
 * Creates a MethodGen for a public static void no-argument method
 * with a random name.
 */
protected sandmark.program.Method makeMethod(
      org.apache.bcel.generic.InstructionList inslist,
      sandmark.program.Class clazz) {

    return new sandmark.program.LocalMethod(
      clazz,
      org.apache.bcel.Constants.ACC_PUBLIC |
         org.apache.bcel.Constants.ACC_STATIC,  // access flags
      org.apache.bcel.generic.Type.VOID,        // return type
      org.apache.bcel.generic.Type.NO_ARGS,     // argument types
      null,                                     // argument names
      "M" + (int)(1e9 * rng.nextDouble()),      // random method name
      inslist);                                  // instruction list
}

} // class VoidMethodSynthesizer

