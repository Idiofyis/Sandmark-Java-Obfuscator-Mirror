package sandmark.watermark.ct.embed;
/* This class is used to  remove the methods from the Watermark class
and inline the code at the point of call
*/

public class Inliner{

private static final boolean Debug=true;
/**
 * @param app the program to be watermarked
 * @param props         global property list
 */

public static void doInline(sandmark.program.Application app,
        sandmark.util.ConfigProperties props)
{
   String watermarkClassName=props.getProperty("DWM_CT_Encode_ClassName");
   java.util.Iterator classes = app.classes();
   while(classes.hasNext()){
      sandmark.program.Class cls = (sandmark.program.Class) classes.next();
      String className = cls.getName();
      if (!className.equals(watermarkClassName)) {

         sandmark.program.Method[] methods = cls.getMethods();
          for(int i=0;i<methods.length;i++)
          {

             sandmark.program.Method mg = methods[i];
             inlineMethods(cls, mg,watermarkClassName);
  	 	  }

      }

   }

   sandmark.program.Class cls=app.getClass(watermarkClassName);
   sandmark.program.Method[] methods = cls.getMethods();
   for(int i=0;i<methods.length;i++)
   {	if(!methods[i].getName().equals("<init>"))
	   methods[i].delete();
   }

}


/*  Inline all the method invocation in the method mg that
	are calls to methods in class watermarkClassName */
static boolean inlineMethods (
   sandmark.program.Class ec,
   sandmark.program.Method mg, String watermarkClassName) {
   if(ec.getName().equals(watermarkClassName)) return false;

   if (mg.isNative() || mg.isAbstract()) return false;

   boolean changed = false;
   org.apache.bcel.generic.InstructionList il = mg.getInstructionList();
   org.apache.bcel.generic.InstructionHandle[] ihs = il.getInstructionHandles();
   for(int i=0; i < ihs.length; i++) {
      org.apache.bcel.generic.InstructionHandle ih = ihs[i];
      org.apache.bcel.generic.Instruction instr = ih.getInstruction();
      if (instr instanceof org.apache.bcel.generic.InvokeInstruction) {
          org.apache.bcel.generic.InvokeInstruction call =
             (org.apache.bcel.generic.InvokeInstruction) instr;
           //  System.out.println("Evaluating inline"+call);
	  					changed = inlineMethod(ec,mg,ih,watermarkClassName);

      }
   }
   mg.setMaxStack();
   mg.mark();
   return changed;
}


/* Inline a given invoke call */
static boolean inlineMethod(sandmark.program.Class ec,
						sandmark.program.Method mg,
   org.apache.bcel.generic.InstructionHandle call,String watermarkClassName) {
	boolean changed = false;
	org.apache.bcel.generic.InvokeInstruction callinstruction=
		(org.apache.bcel.generic.InvokeInstruction)call.getInstruction();


	  String className  = callinstruction.getClassName(ec.getConstantPool());
	  if(className.equals(watermarkClassName))
	  { changed=true;
	  	String methodName = callinstruction.getName(ec.getConstantPool());
	  	String methodSig  = callinstruction.getSignature(ec.getConstantPool());
	  	if(Debug)
	  		System.out.println("Inlining="+className+":"+methodName+":"+methodSig);
	  	sandmark.program.Class calleeclass=ec.getApplication().getClass(className);
	  	sandmark.program.Method calleemethod=calleeclass.getMethod(methodName,methodSig);
	  	sandmark.util.Inliner inliner=new sandmark.util.Inliner(mg);
	  	inliner.inline(calleemethod,call);

  	  }
	return changed;

}

}
