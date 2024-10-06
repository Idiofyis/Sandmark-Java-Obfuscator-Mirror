package sandmark.analysis.stacksimulator;

/**
   Encapsulates all non-primitive data.
   @author Kelly Heffner (kheffner@cs.arizona.edu)
*/
class ReferenceData extends StackData {
   private org.apache.bcel.generic.ReferenceType myRefType;

   /**
    * Constructs a referencial data object.  If you are not sure
    * what type this data is, use org.apache.bcel.generic.Type.OBJECT.
    * @param ref the type of the object
    * @param h the instruction handle for the instruction that created
    * this object
    */
   public ReferenceData(org.apache.bcel.generic.ReferenceType ref,
                        org.apache.bcel.generic.InstructionHandle h) {
      super(h);
      myRefType = ref;
   }

   public int getSize() {
      return 1;
   }
   
   public org.apache.bcel.generic.Type getType() {
      return myRefType;
   }

   /**
    * Defined by equality method of the superclass.  In addition,
    * a reference data is equal iff the signatures of its type
    * are equal.
    */
   public boolean equals(Object o) {
      if(!(o instanceof ReferenceData))
         return false;

      ReferenceData other = (ReferenceData)o;

      return 
         other.myRefType.getSignature().equals(myRefType.getSignature()) &&
         super.equals(other);
   }

   public StackData undefinedVersion() {
      return new ReferenceData(myRefType, getInstruction());
   }
}
