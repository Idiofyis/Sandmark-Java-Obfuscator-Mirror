package sandmark.analysis.stacksimulator;

// Represents the item on the stack at the start of an exception handler.
// It has no IH source, so we must identify it with a CodeExceptionGen instead.
public class CaughtExceptionData extends ReferenceData{
   private org.apache.bcel.generic.CodeExceptionGen exception;

   public CaughtExceptionData(org.apache.bcel.generic.ReferenceType ref,
                              org.apache.bcel.generic.CodeExceptionGen ceg){
      super(ref, null);
      exception = ceg;
   }

   public org.apache.bcel.generic.CodeExceptionGen getExceptionHandler(){
      return exception;
   }

   public boolean equals(Object o){
      if (!(o instanceof CaughtExceptionData))
         return false;
      CaughtExceptionData other = (CaughtExceptionData)o;
      return (other.exception==exception && super.equals(o));
   }

   public StackData undefinedVersion(){
      return new CaughtExceptionData
         ((org.apache.bcel.generic.ReferenceType)getType(), exception);
   }
}
