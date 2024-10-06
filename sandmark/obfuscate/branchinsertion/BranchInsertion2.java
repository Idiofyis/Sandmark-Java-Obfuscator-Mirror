package sandmark.obfuscate.branchinsertion;

public class BranchInsertion2 extends sandmark.obfuscate.MethodObfuscator 
implements sandmark.util.ConfigPropertyChangeListener {
    private double mRatio = 1;
    public void apply(sandmark.program.Method method) {
        //System.out.println("RATIO: " + mRatio);
        if(method.getInstructionList() == null)
            return;
        
        int branchCount = 0;
        int instrCount = 0;
        for(org.apache.bcel.generic.InstructionHandle ih =
            method.getInstructionList().getStart() ; ih != null ; 
            ih = ih.getNext(),instrCount++)
            if(ih.getInstruction() instanceof 
                    org.apache.bcel.generic.IfInstruction ||
                    ih.getInstruction() instanceof
                    org.apache.bcel.generic.Select)
                branchCount++;
            
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
        
        int freeLoc = method.calcMaxLocals();
        org.apache.bcel.generic.InstructionList il = method.getInstructionList();
        int insertedBranches = 0;
        for(org.apache.bcel.generic.InstructionHandle ih = 
            il.getStart() ; ih != null ; ih = ih.getNext()) {
            if(rnd.nextDouble() > ratio)
                continue;
            sandmark.util.opaquepredicatelib.PredicateFactory preds[] =
                sandmark.util.opaquepredicatelib.OpaqueManager.getPredicatesByType
                (sandmark.util.opaquepredicatelib.OpaqueManager.PT_ALGEBRAIC,
                        sandmark.util.opaquepredicatelib.OpaqueManager.getPredicatesByValue
                        (sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE));
            sandmark.util.opaquepredicatelib.OpaquePredicateGenerator pred =
                preds[0].createInstance();
            if(pred.canInsertPredicate
                    (method,ih,
                            sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE)) {
                pred.insertPredicate
                (method,ih,
                        sandmark.util.opaquepredicatelib.OpaqueManager.PV_TRUE);
                il.insert(ih,new org.apache.bcel.generic.IFEQ(ih));
                il.insert(ih,new org.apache.bcel.generic.IINC(freeLoc,1));
                method.mark();
                insertedBranches++;
            } else
                System.out.println("can't insert here");
        }
        il.insert(new org.apache.bcel.generic.ISTORE(freeLoc));
        il.insert(new org.apache.bcel.generic.ICONST(0));
        method.mark();
        //System.out.println("inserted " + insertedBranches + " branches");
    }
    private sandmark.util.ConfigProperties mProps;
    public sandmark.util.ConfigProperties getConfigProperties() {
        if(mProps == null) {
            String [][] props = {
                    { "Ratio","1","Ratio",null,"D","O", },
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
    public String getShortName() { return "Opaque Branch Insertion"; }
    public String getLongName() { return "Opaque Branch Insertion"; }
    public String getAlgHTML() { return 
      "<HTML><BODY>" +
      "Opaque Branch Insertion inserts an empty if block before a configurable " +
      "fraction of the instructions in a method.  The inserted test is opaquely " +
      "false because the opaque predicate library is used.<TABLE>" +
      "<TR><TD>" +
      "Author: <a href=\"mailto:ash@cs.arizona.edu\">Andrew Huntwork</a>\n" +
      "</TR></TD>" +
      "</TABLE>" +
      "</BODY></HTML>"; }
    public String getAlgURL() { 
       return "sandmark/obfuscate/branchinsertion/doc/help2.html"; 
    }
    public String getAuthor() { return "Andrew Huntwork"; }
    public String getAuthorEmail() { return "ash@huntwork.net"; }
    public String getDescription() { 
        return "Opaque Branch Insertion inserts an empty if block before a configurable " +
        "fraction of the instructions in a method.  The inserted test is opaquely " +
        "false because the opaque predicate library is used.";
    }
    public String[] getReferences() { return null; }
    public sandmark.config.ModificationProperty[] getMutations() { return null; }
}
