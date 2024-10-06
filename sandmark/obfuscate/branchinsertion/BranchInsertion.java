package sandmark.obfuscate.branchinsertion;

public class BranchInsertion extends sandmark.obfuscate.MethodObfuscator 
   implements sandmark.util.ConfigPropertyChangeListener {
   private double mRatio = .1;
   public void apply(sandmark.program.Method method) {
       if(method.getInstructionList() == null)
	   return;

       int branchCount = 0;
       int instrCount = 0;
       for(java.util.Iterator classes = method.getApplication().classes() ;
	   classes.hasNext() ; ) {
	   sandmark.program.Class cls = (sandmark.program.Class)classes.next();
	   for(java.util.Iterator methods = cls.methods() ; 
	       methods.hasNext() ; ) {
	       sandmark.program.Method m = 
		   (sandmark.program.Method)methods.next();
	       if(m.getInstructionList() == null)
		   continue;
	       for(org.apache.bcel.generic.InstructionHandle ih =
		       m.getInstructionList().getStart() ; ih != null ; 
		   ih = ih.getNext(),instrCount++)
		   if(ih.getInstruction() instanceof 
		      org.apache.bcel.generic.IfInstruction ||
		      ih.getInstruction() instanceof
		      org.apache.bcel.generic.Select)
		       branchCount++;
	   }
       }

       double ratio = branchCount * mRatio / instrCount;
      java.util.Random rnd = sandmark.util.Random.getRandom();
      if(method.getEnclosingClass().getField("foo","Ljava/lang/Object;") == null) {
	  int flags = org.apache.bcel.Constants.ACC_STATIC |
	      org.apache.bcel.Constants.ACC_PUBLIC;
	  if(method.getEnclosingClass().isInterface())
	      flags |= org.apache.bcel.Constants.ACC_FINAL;
	  new sandmark.program.LocalField
	      (method.getEnclosingClass(),flags,
	       org.apache.bcel.generic.Type.OBJECT,"foo");
      }
      org.apache.bcel.generic.InstructionFactory factory =
	  new org.apache.bcel.generic.InstructionFactory(method.getConstantPool());

      org.apache.bcel.generic.InstructionList il = method.getInstructionList();
      int insertedBranches = 0;
      for(org.apache.bcel.generic.InstructionHandle ih = 
             method.getInstructionList().getStart() ; ih != null ; 
          ih = ih.getNext()) {
         if(rnd.nextDouble() > ratio)
            continue;
         il.insert(ih,factory.createGetStatic
		   (method.getEnclosingClass().getName(),"foo",
		    org.apache.bcel.generic.Type.OBJECT));
         il.insert(ih,new org.apache.bcel.generic.IFNULL(ih));
	 il.insert(ih,factory.createNew("java.lang.Object"));
	 il.insert(ih,new org.apache.bcel.generic.DUP());
	 il.insert(ih,factory.createInvoke
		   ("java.lang.Object","<init>",org.apache.bcel.generic.Type.VOID,
		    org.apache.bcel.generic.Type.NO_ARGS,
		    org.apache.bcel.Constants.INVOKESPECIAL));
         il.insert(ih,factory.createPutStatic
		   (method.getEnclosingClass().getName(),"foo",
		    org.apache.bcel.generic.Type.OBJECT));
	 insertedBranches++;
      }
      //System.out.println("inserted " + insertedBranches + " branches");
   }
   private sandmark.util.ConfigProperties mProps;
   public sandmark.util.ConfigProperties getConfigProperties() {
      if(mProps == null) {
         String [][] props = {
            { "Ratio",".1","Ratio",null,"D","O", },
         };
         mProps = new sandmark.util.ConfigProperties
            (props,null);
         mProps.addPropertyChangeListener("Ratio",this);
      }
      return mProps;
   }
   public void propertyChanged
      (sandmark.util.ConfigProperties props,String propertyName,
       Object oldValue,Object newValue) {
      mRatio = ((Double)newValue).doubleValue();
   }
   public String getShortName() { return "Transparent Branch Insertion"; }
   public String getLongName() { return "Transparent Branch Insertion"; }
   public String getAlgHTML() { return 
      "<HTML><BODY>" +
      "Transparent Branch Insertion inserts an empty if block before a configurable " +
      "fraction of the instructions in a method.  The inserted test is transparently " +
      "false.<TABLE>" +
      "<TR><TD>" +
      "Author: <a href=\"mailto:ash@cs.arizona.edu\">Andrew Huntwork</a>\n" +
      "</TR></TD>" +
      "</TABLE>" +
      "</BODY></HTML>"; }
   public String getAlgURL() { 
      return "sandmark/obfuscate/branchinsertion/doc/help.html"; 
   }
   public String getAuthor() { return "Andrew Huntwork"; }
   public String getAuthorEmail() { return "ash@huntwork.net"; }
   public String getDescription() { 
      return "Transparent Branch Insertion inserts an empty if block before a configurable " +
      "fraction of the instructions in a method.  The inserted test is transparently " +
      "false.";
   }
   public String[] getReferences() { return null; }
   public sandmark.config.ModificationProperty[] getMutations() { return null; }
}
