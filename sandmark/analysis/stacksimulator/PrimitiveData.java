package sandmark.analysis.stacksimulator;

// A quote from the JVM Spec version 2:
// A small number of Java virtual machine instructions (the dup instructions
// and swap) operate on runtime data areas as raw values without regard to
// their specific types; these instructions are defined in such a way that
// they cannot be used to modify or break up individual values. These
// restrictions on operand stack manipulation are enforced through class
// file verification.
// In other words, it is infeasable to split a long or a double on the
// stack, so we do not need to represent them by 2 seperate parts.

abstract class PrimitiveData extends StackData
{
   private java.lang.Number myValue;

   public PrimitiveData(java.lang.Number n,
                        org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
      myValue = n;
   }

   public PrimitiveData(org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
      myValue = null;
   }

   public abstract int getSize();
   public abstract org.apache.bcel.generic.Type getType();

   public boolean isComposite()
   {
      return false;
   }

   public boolean hasDefinedValue()
   {
      return myValue != null;
   }

   public java.lang.Number getValue()
   {
      if(!hasDefinedValue())
         throw new AssertionError((Object)"Data value is undefined");
      else return myValue;
   }

   public boolean equals(Object o)
   {
      if(o instanceof PrimitiveData){
         PrimitiveData other = (PrimitiveData)o;
         if(myValue == null || other.myValue == null)
            return (myValue == null && 
                    other.myValue == null &&
                    super.equals(o));
         return myValue.equals(other.myValue) && super.equals(o);
      }
      else
         return false;
   }
   
   public String toString()
   {
      return super.toString() + " " +
         (hasDefinedValue() ? getValue().toString() :
          "Undefined");
   }
}
