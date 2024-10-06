package sandmark.analysis.stacksimulator;

class UninitializedReferenceData extends ReferenceData {
   public UninitializedReferenceData(org.apache.bcel.generic.ObjectType t,
                                     org.apache.bcel.generic.InstructionHandle h) {
      super(new org.apache.bcel.verifier.structurals.UninitializedObjectType(t), h);
   }
   
   public boolean equals(java.lang.Object o) {
      if (o instanceof UninitializedReferenceData) {
         UninitializedReferenceData urd = (UninitializedReferenceData)o;
         return 
            getInstruction() == urd.getInstruction() &&
            super.equals(urd);
      }
      else
         return false;
   }

   public StackData undefinedVersion() {
      org.apache.bcel.generic.ObjectType t = 
         ((org.apache.bcel.verifier.structurals.UninitializedObjectType)getType()).getInitialized();
      return new UninitializedReferenceData(t, getInstruction());
   }

   public ReferenceData initialize(org.apache.bcel.generic.InstructionHandle h) {
      org.apache.bcel.verifier.structurals.UninitializedObjectType t =
         (org.apache.bcel.verifier.structurals.UninitializedObjectType)getType();
      return new ReferenceData(t.getInitialized(), h);
   }
}
