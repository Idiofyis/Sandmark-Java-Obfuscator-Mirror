package sandmark.watermark.ct.trace;

public class Preprocessor {
    sandmark.program.Application app;
    sandmark.util.ConfigProperties props;

/** 
 * Name of the local variable stored in each method
 * to record the unique ID of that call frame.
 */
public final static String STACKID      = "sm$stackID";

/**
 * Name of static variable in sandmark.watermark.ct.trace.Annotator
 * that holds the current stack ID count.
*/
public final static String FRAMECOUNTER = "stackFrameNumber";

/**
 * The amount of code we're adding to each method. Use this
 * to compute the actual offset of a mark() call within the
 * unprocessed (original) source. We're adding the following
 * code, whose size adds up to 11:
 * <PRE>
 *   GETSTATIC   X                        3
 *   DUP2                                 1
 *   LCONST_1                             1
 *   LADD                                 1
 *   GETSTATIC   X                        3
 *   LSTORE_1    X   (or LSTORE)          1 (or 2)
 *   (NOP)                                0 (or 1)
 * </PRE>
 */
public static final int ADDEDCODESIZE = 11;

public Preprocessor(sandmark.program.Application app,
      				sandmark.util.ConfigProperties props) {
   this.props = props;
   this.app = app;
}

/**
 * Go through every method in every class of the program and add the code
 * <PRE>
 *    long sm$stackID = sandmark.watermark.ct.trace.Annotator.stackFrameNumber++;
 * </PRE>
 * to the very beginning of the method. This will give every stackframe
 * a unique ID. 
 */
public void preprocess() {
   java.util.Iterator classes = app.classes();
   insertAnnotatorClass();
   while(classes.hasNext()){
      sandmark.program.Class cls = (sandmark.program.Class) classes.next();
      preprocessClass(cls);
   }  
}  

private void insertAnnotatorClass() {
   String annotatorPath = ("sandmark/watermark/ct/trace/Annotator.class");
   java.io.InputStream is = getClass().getClassLoader().getResourceAsStream
   	(annotatorPath);
   if(is == null)
      throw new Error("sandmark doesn't contain Annotator");
   
   try {
      new sandmark.program.LocalClass
      	(app,new org.apache.bcel.classfile.ClassParser
      	 (is,annotatorPath).parse());
   } catch(java.io.IOException e) {
      throw new Error("corrupt annotator in sandmark");
   }
}

/**
 * Save the edited classfiles under a new name.
 */  
public void save(java.io.File appFile) throws java.io.IOException {
    app.save(appFile.toString());
}

    
    
/**
 * Make the modifications to every method of the class.
 * @param ec the class to be edited
 */
void preprocessClass (
   sandmark.program.Class ec) {
   java.util.Iterator methods = ec.methods();
   while (methods.hasNext()){
      sandmark.program.Method mg = (sandmark.program.Method) methods.next();
      preprocessMethod(ec,mg);
   }
}


  /**
    * Create a local variable to hold the stack ID.
    * @param ec the class to be edited
    * @param mg the method to be edited
    * Example:
    * <PRE>
    *    void P() {
    *       ...
    *    }
    * </PRE>
    * The resulting method would look something like this:
    * <PRE>
    *    void P() {
    *       long sm$stackID = sandmark.watermark.ct.trace.Annotator.stackFrameNumber++;
    *       ...
    *    }
    * </PRE>
    * Or, in bytecode:
    * <PRE>
    *   GETSTATIC   sandmark.watermark.ct.trace.Annotator.stackFrameNumber
    *   DUP2
    *   LCONST_1
    *   LADD
    *   GETSTATIC   sandmark.watermark.ct.trace.Annotator.stackFrameNumber
    *   LSTORE      sm$stackID-index
    * </PRE>
    * One complication:
    * <PRE>
    *   LSTORE
    * </PRE>
    * could either be
    * <PRE>
    *      LSTORE_1
    * </PRE>
    * or
    * <PRE>
    *      LSTORE 5
    * </PRE>
    *   In the former case the instruction is 1 byte, in the latter, 2.
    * We want all code sequences we insert to be the same length, so that
    * when mark() is called in the preprocessed bytecode, we can figure
    * out what its "real" offset is (the offset in the unprocessed code).
    * Therefore we add an extra NOP in the case of LSTORE_1.
    */
   void preprocessMethod(
      sandmark.program.Class ec, 
      sandmark.program.Method mg) {
      if (mg.isNative() || mg.isAbstract()) return;

      org.apache.bcel.generic.LocalVariableGen lg = 
         mg.addLocalVariable(STACKID, org.apache.bcel.generic.Type.LONG, null, null);
      int stackIDindex = lg.getIndex();

      String longType = org.apache.bcel.classfile.Utility.getSignature("long");
      String annotatorClass = props.getProperty("DWM_CT_AnnotatorClass","sandmark.watermark.ct.trace.Annotator");
      org.apache.bcel.generic.ConstantPoolGen cp = ec.getConstantPool();
      int frameCounterIndex = cp.addFieldref(annotatorClass, FRAMECOUNTER, longType);

      org.apache.bcel.generic.InstructionList il = new org.apache.bcel.generic.InstructionList();
      il.append(new org.apache.bcel.generic.GETSTATIC(frameCounterIndex));
      il.append(new org.apache.bcel.generic.DUP2());
      il.append(new org.apache.bcel.generic.LCONST(1));
      il.append(new org.apache.bcel.generic.LADD());
      il.append(new org.apache.bcel.generic.PUTSTATIC(frameCounterIndex));
      org.apache.bcel.generic.LSTORE store = new org.apache.bcel.generic.LSTORE(stackIDindex);
      il.append(store);
      if (store.getLength() < 2)
          il.append(new org.apache.bcel.generic.NOP());
          
      org.apache.bcel.generic.InstructionList mil = mg.getInstructionList();
      mil.insert(il);
      mg.setMaxStack();
      mg.mark();
   }


}








