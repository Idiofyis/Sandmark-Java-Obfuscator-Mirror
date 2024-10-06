package sandmark.analysis.controlflowgraph;

public class ProgramCFG extends sandmark.util.newgraph.MutableGraph {
   public ProgramCFG(java.util.Collection methods)  {
      java.util.Hashtable methodNameToCFG = 
	 buildNameMap(methods);
      addNodes(methodNameToCFG);
      addCallEdges(methodNameToCFG);
   }
   public void addNode(BasicBlock block,MethodCFG cfg) {
      cfg.addBlock(block);
      addNode(block);
   }
   private java.util.Hashtable buildNameMap(java.util.Collection methods) {
      java.util.Iterator cfgIt = methods.iterator();
      java.util.Hashtable nameToCFG = 
	 new java.util.Hashtable();
      while(cfgIt.hasNext()) {
	 MethodCFG cfg = (MethodCFG)cfgIt.next();
	 String methodName = fieldOrMethodName(cfg);
	 nameToCFG.put(methodName,cfg);
	 //System.out.println("mapping " + methodName + 
	 //" to cfg for " + cfg.methodGen());
      }
      return nameToCFG;
   }
   private void addNodes(java.util.Hashtable methodNameToCFG) {
      java.util.Iterator methodCFGIt = methodNameToCFG.values().iterator();
      while(methodCFGIt.hasNext()) {
	 MethodCFG cfg = (MethodCFG)methodCFGIt.next();
         addNode(cfg);
      }
   }
   private void addCallEdges(java.util.Hashtable methodNameToCFG) {
      java.util.Iterator methodCFGIt = methodNameToCFG.values().iterator();
      while(methodCFGIt.hasNext()) {
	 MethodCFG cfg = (MethodCFG)methodCFGIt.next();
	 java.util.Iterator nodeIt = cfg.nodes();
	 while(nodeIt.hasNext()) {
	    BasicBlock node = (BasicBlock)nodeIt.next();
	    java.util.Iterator instrIt = node.getInstList().iterator();
	    while(instrIt.hasNext()) {
	       org.apache.bcel.generic.InstructionHandle ih =
		  (org.apache.bcel.generic.InstructionHandle)instrIt.next();
	       org.apache.bcel.generic.Instruction instr = ih.getInstruction();
	       if(instr instanceof org.apache.bcel.generic.InvokeInstruction) {
		  org.apache.bcel.generic.InvokeInstruction inv =
		     (org.apache.bcel.generic.InvokeInstruction)instr;
		  org.apache.bcel.generic.ConstantPoolGen cpg =
		      cfg.method().getConstantPool();
		  String invName = fieldOrMethodName(inv.getClassName(cpg), 
						     inv.getName(cpg),
						     inv.getSignature(cpg));
		  MethodCFG methodCFG = 
		     (MethodCFG)methodNameToCFG.get(invName);
		  if(methodCFG != null) {
		     addEdge(cfg, methodCFG);
                     addEdge(methodCFG,cfg);
		  }
	       }
	    }
	 }
      }
   }
   public static String fieldOrMethodName(MethodCFG cfg) {
      return fieldOrMethodName(cfg.method());
   }
   public static String fieldOrMethodName(sandmark.program.Method mg) {
      return fieldOrMethodName(mg.getClassName(),mg.getName(),
			       mg.getSignature());
   }
   public static String fieldOrMethodName(sandmark.program.Field field) {
      return fieldOrMethodName(field.getClass().getName(),field.getName(),
			       field.getSignature());
   }
   public static String fieldOrMethodName
      (org.apache.bcel.generic.MethodGen mg) {
      return fieldOrMethodName(mg,mg.getClassName());
   }
   public static String fieldOrMethodName
      (org.apache.bcel.generic.FieldGenOrMethodGen fm,
       String className) {
      return fieldOrMethodName(className,fm.getName(),
			       fm.getSignature());
   }
   public static String fieldOrMethodName
      (org.apache.bcel.generic.FieldOrMethod fm,
       org.apache.bcel.generic.ConstantPoolGen cpg) {
      return fieldOrMethodName(fm.getClassName(cpg),
			       fm.getName(cpg),
			       fm.getSignature(cpg));
   }
   public static String fieldOrMethodName(String className,
					  String fieldOrMethodName,
					  String signature) {
      return className + "." + fieldOrMethodName + signature;
   }
}

