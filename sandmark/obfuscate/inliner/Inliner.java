package sandmark.obfuscate.inliner;

public class Inliner extends sandmark.optimise.MethodOptimizer {
    public void apply(sandmark.program.Method method) throws Exception {
        int inlineCount = 0;
	new sandmark.util.Publicizer().apply(method.getApplication());
        sandmark.program.Class clazz = method.getEnclosingClass();
	
        if(method.getInstructionList() == null)
           return;
        sandmark.util.Inliner inliner = new sandmark.util.Inliner(method);
        java.util.HashSet inlinedMethods = new java.util.HashSet();
        org.apache.bcel.generic.InstructionHandle ihs[];
        int i;
        for(ihs = method.getInstructionList().getInstructionHandles(),
               i = 0 ; i < ihs.length ; i++) {
           if(!(ihs[i].getInstruction() instanceof
                org.apache.bcel.generic.INVOKESTATIC))
              continue;
           org.apache.bcel.generic.INVOKESTATIC inv =
              (org.apache.bcel.generic.INVOKESTATIC)
              ihs[i].getInstruction();

           sandmark.program.Class invokedClass = 
              method.getApplication().getClass(inv.getClassName(clazz.getConstantPool()));
           if(invokedClass == null)
              continue;
                    
           sandmark.program.Method invokedMethod =
              invokedClass.getMethod
              (inv.getName(clazz.getConstantPool()),
               inv.getSignature(clazz.getConstantPool()));
           if(invokedMethod == null || 
              invokedMethod.getInstructionList() == null)
              continue;

           if(invokedMethod.getInstructionList().getByteCode().length +
              method.getInstructionList().getByteCode().length > 30000)
              continue;

           if(inlinedMethods.contains(invokedMethod))
              continue;

           if(containsBadInvokes(invokedMethod))
              continue;

           if(stackContainsUninitialized(method,ihs[i]))
              continue;

           inliner.inline(invokedMethod,ihs[i]);
           inlinedMethods.add(invokedMethod);
           ihs = method.getInstructionList().getInstructionHandles();
           i--;
           inlineCount++;
        }
        method.removeLocalVariables();
        //System.out.println("methods inlined: " + inlineCount);
    }

    public static boolean stackContainsUninitialized
	(sandmark.program.Method method,
	 org.apache.bcel.generic.InstructionHandle ih) {
	sandmark.analysis.stacksimulator.StackSimulator ss = 
	    method.getStack();
	sandmark.analysis.stacksimulator.Context cx =
	    ss.getInstructionContext(ih);
	for(int i = 0 ; i < cx.getStackSize() ; i++) {
	    sandmark.analysis.stacksimulator.StackData sd[] =
		cx.getStackAt(i);
	    for(int j = 0 ; j < sd.length ; j++)
		if(sd[j].getType() instanceof 
		   org.apache.bcel.verifier.structurals.UninitializedObjectType)
		    return true;
	}
	return false;
    }

    public static boolean containsBadInvokes(sandmark.program.Method method) {
        for(org.apache.bcel.generic.InstructionHandle ih = 
                method.getInstructionList().getStart() ; 
            ih != null ; ih = ih.getNext()) {
            if(!(ih.getInstruction() instanceof 
                 org.apache.bcel.generic.INVOKESPECIAL))
                continue;

            org.apache.bcel.generic.INVOKESPECIAL inv =
                (org.apache.bcel.generic.INVOKESPECIAL)ih.getInstruction();

            if(!inv.getName(method.getConstantPool()).equals("<init>"))
                return true;
        }
        return false;
    }


    public String getShortName() {
        return "Inliner";
    }
    public String getLongName() {
        return "Inline static method calls";
    }
    public String getAlgHTML() {
        return
            "<HTML><BODY>" +
            "Inliner inlines static methods\n" +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <A HREF =\"mailto:ash@cs.arizona.edu\">Andrew Huntwork</A>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }
    public String getAlgURL() {
        return "sandmark/obfuscate/inliner/doc/help.html";
    }

    public String getAuthor(){
        return "Andrew Huntwork";
    }

    public String getAuthorEmail(){
        return "ash@cs.arizona.edu";
    }

    public String getDescription(){
        return "Inliner inlines static methods.";
    }
    public String[] getReferences() {
        return new String[] {};
    }
    public sandmark.config.ModificationProperty[] getMutations() {
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
            sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE,
            sandmark.config.ModificationProperty.I_PUBLICIZE_FIELDS,
            sandmark.config.ModificationProperty.I_PUBLICIZE_METHODS,
        };
    }
}

