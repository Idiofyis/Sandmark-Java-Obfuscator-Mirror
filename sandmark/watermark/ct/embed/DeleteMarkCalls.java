package sandmark.watermark.ct.embed;

public class DeleteMarkCalls {
    sandmark.util.ConfigProperties props = null;
sandmark.program.Application app = null;
String watermarkClass;

//-----------------------------------------------------------
//               Constructor and Main Routine
//-----------------------------------------------------------

public DeleteMarkCalls(
   sandmark.program.Application app,
   sandmark.util.ConfigProperties props){
   this.props = props;
   this.app = app;
   watermarkClass = props.getProperty("DWM_CT_Encode_ClassName");
}
  
/**
 * Delete all the calls to sandmark.watermark.ct.trace.Annotator.sm$mark(*)
 * in every class in the program.
 * This is the main entry point to this class. 
 */
void delete() throws java.io.IOException{
    //System.out.println("DeleteMarkCalls:deleteMarkCalls1:1");
   java.util.Iterator classes = app.classes();
   while(classes.hasNext()){
      sandmark.program.Class cls = (sandmark.program.Class) classes.next();
      String className = cls.getName();
      if (!className.equals(watermarkClass)) {
         boolean classChanged = deleteMarkCalls(cls);
      }
   }  
}  
    
/**
 * Delete all the calls to sandmark.watermark.ct.trace.Annotator.sm$mark(*)
 * in every method in the given class.
 * @param ec the class to be edited
 * Return 'true' if the class was changed.
 */
boolean deleteMarkCalls (
   sandmark.program.Class ec) {
    //System.out.println("DeleteMarkCalls:deleteMarkCalls2:1");
   java.util.Iterator methods = ec.methods();
   while (methods.hasNext()){
       sandmark.program.Method mg = (sandmark.program.Method) methods.next();
       boolean changed = deleteMarkCalls(ec,mg);
       if (changed)
          mg.mark();
   }
   return true;
}

/**
 * Delete every call to sandmark.watermark.ct.trace.Annotator.sm$mark(*)
 * in the given method. Return 'true' if the method was changed.
 * @param ec the class to be edited
 * @param mg the method to be edited
 */
boolean deleteMarkCalls (
   sandmark.program.Class ec,
   sandmark.program.Method mg) {
   if (mg.isNative() || mg.isAbstract()) return false;

   boolean changed = false;
   org.apache.bcel.generic.InstructionList il = mg.getInstructionList();
   org.apache.bcel.generic.InstructionHandle[] ihs = il.getInstructionHandles();
   for(int i=0; i < ihs.length; i++) {
      org.apache.bcel.generic.InstructionHandle ih = ihs[i];
      org.apache.bcel.generic.Instruction instr = ih.getInstruction();
      if (instr instanceof org.apache.bcel.generic.INVOKESTATIC) {
         org.apache.bcel.generic.INVOKESTATIC call =
           (org.apache.bcel.generic.INVOKESTATIC) instr;
         boolean c = deleteMarkCall(ec, mg, call, ih);
         changed = changed || c;
      }
   }
   return changed;
}

/**
 * Check if the given instruction is a call to 
 *        sandmark.watermark.ct.trace.Annotator.sm$mark(*)
 * If it is, replace it with a "NOP".
 * This way we don't have to worry about jumps to the removed
 * instructions. The NOPs should be removed later.
 * "sm$mark(String)" and "sm$mark(long)" are instead replaced by "POP". 
 * Ideally, we'd like to delete them too, but that is hard. Consider,
 * for example, "sm$mark(k+x*4)".
 * @param ec   the class to be edited
 * @param mg   the method to be edited
 * @param call the call instruction to be deleted
 * @param ih   the instruction handle of the call instruction
 * Return 'true' if the method was changed.
 */
boolean deleteMarkCall(
   sandmark.program.Class ec,
   sandmark.program.Method mg,
   org.apache.bcel.generic.INVOKESTATIC call,
   org.apache.bcel.generic.InstructionHandle ih) {
   String methodName = call.getClassName(ec.getConstantPool()) + "." + call.getName(ec.getConstantPool());
   String methodSig  = call.getSignature(ec.getConstantPool());
   String annotatorClass = props.getProperty("DWM_CT_AnnotatorClass","sandmark.watermark.ct.trace.Annotator");
   String markMethod = annotatorClass + ".sm$mark";

   if (!methodName.equals(markMethod)) return false;

   if (methodSig.equals("()V")) {
       ih.setInstruction(new org.apache.bcel.generic.NOP());
       //       System.out.println("DeleteMarkCalls: removing call '" + methodName + ":" + methodSig);
   } else if (methodSig.equals("(J)V")) {
       ih.setInstruction(new org.apache.bcel.generic.POP2());
       //       System.out.println("DeleteMarkCalls: removing call '" + methodName + ":" + methodSig);
   } else if (methodSig.equals("(Ljava/lang/String;)V")) {
      ih.setInstruction(new org.apache.bcel.generic.POP());
      //      System.out.println("DeleteMarkCalls: removing call '" + methodName + ":" + methodSig);
   } else
      return false;
   String caller = mg.getParent().getName() + "." + mg.getName() + ":" + mg.getSignature();
   String callee = methodName + ":" + methodSig;
   sandmark.util.Log.message(0,"Removing call to " + callee + " in " + caller);
   mg.setMaxStack();
   return true;
}
}








