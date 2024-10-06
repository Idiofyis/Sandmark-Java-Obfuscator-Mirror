package sandmark.analysis.stacksimulator;

public class ReturnaddressData extends StackData {
   private org.apache.bcel.generic.InstructionHandle target;

   public ReturnaddressData(org.apache.bcel.generic.InstructionHandle h,
                            org.apache.bcel.generic.InstructionHandle _target) {
      super(h);
      target = _target;
   }

   public int getSize() {
      return 1;
   }

   public org.apache.bcel.generic.Type getType() {
      return new org.apache.bcel.generic.ReturnaddressType(target);
   }

   public org.apache.bcel.generic.InstructionHandle getTarget(){
      return target;
   }

   public boolean equals(java.lang.Object o) {
      if (o instanceof ReturnaddressData){
         ReturnaddressData d = (ReturnaddressData)o;
         if (d.target==target)
            return super.equals(o);
      }
      return false;
   }

   public StackData undefinedVersion() {
      return new ReturnaddressData(getInstruction(), target);
   }
}
