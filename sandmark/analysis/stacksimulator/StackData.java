package sandmark.analysis.stacksimulator;

/**
   StackData is the superclass of all first class data elements.
   @author Kelly Heffner (kheffner@cs.arizona.edu)
*/
public abstract class StackData
{
   private org.apache.bcel.generic.InstructionHandle myHandle;

   protected StackData(org.apache.bcel.generic.InstructionHandle h)
   {
      myHandle = h;
   }

   /**
      Specifies the size of this object on the operand stack.
      @return the size of the object in bytes, 1 or 2.
   */
   public abstract int getSize();

   /**
      Specifies the type of the data.
      @return a type object which is the type (or a superclass of the type)
      for the data object
   */
   public abstract org.apache.bcel.generic.Type getType();


   /**
      Tests for equality between any two data elements.  Two
      StackData objects are equal iff they have the same instruction
      handle as their creation point.  (subclasses have more restrictions
      for equality)
      @param o the object to test equality against
      @throws java.lang.ClassCastException if o is not an instance
      of StackData
   */
   public boolean equals(Object o)
   {
      StackData other = (StackData)o;
      return (myHandle == other.myHandle);
   }

   /**
      Specifies the instruction handle for the instruction
      that placed this object on the stack.
   */
   public org.apache.bcel.generic.InstructionHandle getInstruction()
   {
      return myHandle;
   }

   /**
      Returns the same type of stack data as this instance, without
      any definition.  For example, an IntData object would return
      a new IntData object that has undefined value when undefinedVersion
      is called.
      @return A similar instance to this object, with no definition data
   */
   public abstract StackData undefinedVersion();

   public String toString() {
      return getType() + " " + getInstruction();
   }
}


class ArrayReferenceData extends ReferenceData
{
   private int myLength;
   private sandmark.analysis.stacksimulator.PrimitiveData theLengthData;

   public ArrayReferenceData(org.apache.bcel.generic.ReferenceType ref,
                             org.apache.bcel.generic.InstructionHandle h,
                             sandmark.analysis.stacksimulator.PrimitiveData length)
   {
      super(ref, h);
      theLengthData = length;
      myLength = length.hasDefinedValue() ? length.getValue().intValue() : -1;
   }

   public ArrayReferenceData(org.apache.bcel.generic.ReferenceType ref,
                             int length,
                             org.apache.bcel.generic.InstructionHandle h)
   {
      super(ref, h);
      myLength = length;
      theLengthData = new sandmark.analysis.stacksimulator.IntData(length, h);
   }

   /**
      Returns the length of this array if defined.
      @return the array length, else a negative value
   */
   public int getLength()
   {
      return myLength;
   }

   sandmark.analysis.stacksimulator.PrimitiveData getLengthData()
   {
      return theLengthData;
   }

   public String toString()
   {
      return super.toString() + " length " + myLength;
   }

   public boolean equals(Object o)
   {
      if(o instanceof ArrayReferenceData){
         return myLength == ((ArrayReferenceData)o).myLength &&
            super.equals(o);
      }
      else return false;
   }
}

class IntData extends PrimitiveData
{
   public IntData(org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
   }

   public IntData(int value, org.apache.bcel.generic.InstructionHandle h)
   {
      super(new Integer(value), h);
   }

   public IntData(Number value, org.apache.bcel.generic.InstructionHandle h)
   {
      this(value.intValue(), h);
   }

   public int getSize()
   {
      return 1;
   }

   public org.apache.bcel.generic.Type getType()
   {
      return org.apache.bcel.generic.Type.INT;
   }

   public StackData undefinedVersion()
   {
      return new IntData(getInstruction());
   }

}

class CompositeIntData extends IntData
{
   private sandmark.analysis.stacksimulator.PrimitiveData myOp1;
   private sandmark.analysis.stacksimulator.PrimitiveData myOp2;

   public CompositeIntData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                           sandmark.analysis.stacksimulator.PrimitiveData op2,
                           org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public CompositeIntData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                           sandmark.analysis.stacksimulator.PrimitiveData op2,
                           int value,
                           org.apache.bcel.generic.InstructionHandle h)
   {
      super(value, h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public boolean equals(Object o)
   {
      if(o instanceof CompositeIntData){
         CompositeIntData other = (CompositeIntData)o;
         return super.equals(o) &&
            (myOp1==null?true:myOp1.equals(other.myOp1)) &&
            (myOp2==null?true:myOp2.equals(other.myOp2));
      }else
         return false;
   }

   public sandmark.analysis.stacksimulator.PrimitiveData getOp1()
   {
      return myOp1;
   }

   public sandmark.analysis.stacksimulator.PrimitiveData getOp2()
   {
      return myOp2;
   }

}

class LongData extends PrimitiveData
{
   public LongData(org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
   }

   public LongData(long value, org.apache.bcel.generic.InstructionHandle h)
   {
      super(new Long(value), h);
   }

   public LongData(Number value, org.apache.bcel.generic.InstructionHandle h)
   {
      this(value.longValue(), h);
   }


   public int getSize()
   {
      return 2;
   }

   public org.apache.bcel.generic.Type getType()
   {
      return org.apache.bcel.generic.Type.LONG;
   }

   public StackData undefinedVersion()
   {
      return new LongData(getInstruction());
   }

}

class BooleanData extends PrimitiveData
{
   public BooleanData(org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
   }

   public BooleanData(boolean value, org.apache.bcel.generic.InstructionHandle h)
   {
      super(new java.lang.Integer(value?1:0), h);
   }

   public BooleanData(Number value, org.apache.bcel.generic.InstructionHandle h)
   {
      this(value.intValue()==0?false:true, h);
   }

   public int getSize()
   {
      return 1;
   }

   public org.apache.bcel.generic.Type getType()
   {
      return org.apache.bcel.generic.Type.BOOLEAN;
   }

   public StackData undefinedVersion()
   {
      return new BooleanData(getInstruction());
   }

}

class DoubleData extends PrimitiveData
{
   public DoubleData(org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
   }

   public DoubleData(double value, org.apache.bcel.generic.InstructionHandle h)
   {
      super(new Double(value), h);
   }

   public DoubleData(Number value, org.apache.bcel.generic.InstructionHandle h)
   {
      this(value.doubleValue(), h);
   }

   public int getSize()
   {
      return 2;
   }

   public org.apache.bcel.generic.Type getType()
   {
      return org.apache.bcel.generic.Type.DOUBLE;
   }

   public StackData undefinedVersion()
   {
      return new DoubleData(getInstruction());
   }

}

class FloatData extends PrimitiveData
{
   public FloatData(org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
   }

   public FloatData(float value, org.apache.bcel.generic.InstructionHandle h)
   {
      super(new Float(value), h);
   }

   public FloatData(Number value, org.apache.bcel.generic.InstructionHandle h)
   {
      this(value.floatValue(), h);
   }

   public int getSize()
   {
      return 1;
   }

   public org.apache.bcel.generic.Type getType()
   {
      return org.apache.bcel.generic.Type.FLOAT;
   }

   public StackData undefinedVersion()
   {
      return new FloatData(getInstruction());
   }
}


class CompositeBooleanData extends BooleanData
{
   private sandmark.analysis.stacksimulator.PrimitiveData myOp1;
   private sandmark.analysis.stacksimulator.PrimitiveData myOp2;

   public CompositeBooleanData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                               sandmark.analysis.stacksimulator.PrimitiveData op2,
                               org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public CompositeBooleanData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                               sandmark.analysis.stacksimulator.PrimitiveData op2,
                               boolean value,
                               org.apache.bcel.generic.InstructionHandle h)
   {
      super(value, h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public boolean equals(Object o)
   {
      if(o instanceof CompositeBooleanData){
         CompositeBooleanData other = (CompositeBooleanData)o;
         return super.equals(o) &&
            (myOp1==null?true:myOp1.equals(other.myOp1)) &&
            (myOp2==null?true:myOp2.equals(other.myOp2));
      }else
         return false;
   }

}

class CompositeLongData extends LongData
{
   private sandmark.analysis.stacksimulator.PrimitiveData myOp1;
   private sandmark.analysis.stacksimulator.PrimitiveData myOp2;

   public CompositeLongData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                            sandmark.analysis.stacksimulator.PrimitiveData op2,
                            org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public CompositeLongData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                            sandmark.analysis.stacksimulator.PrimitiveData op2,
                            long value,
                            org.apache.bcel.generic.InstructionHandle h)
   {
      super(value, h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public boolean equals(Object o)
   {
      if(o instanceof CompositeLongData){
         CompositeLongData other = (CompositeLongData)o;
         return super.equals(o) &&
            (myOp1==null?true:myOp1.equals(other.myOp1)) &&
            (myOp2==null?true:myOp2.equals(other.myOp2));
      }else
         return false;
   }

}

class CompositeDoubleData extends DoubleData
{
   private sandmark.analysis.stacksimulator.PrimitiveData myOp1;
   private sandmark.analysis.stacksimulator.PrimitiveData myOp2;

   public CompositeDoubleData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                              sandmark.analysis.stacksimulator.PrimitiveData op2,
                              org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public CompositeDoubleData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                              sandmark.analysis.stacksimulator.PrimitiveData op2,
                              double value,
                              org.apache.bcel.generic.InstructionHandle h)
   {
      super(value, h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public boolean equals(Object o)
   {
      if(o instanceof CompositeDoubleData){
         CompositeDoubleData other = (CompositeDoubleData)o;
         return super.equals(o) &&
            (myOp1==null?true:myOp1.equals(other.myOp1)) &&
            (myOp2==null?true:myOp2.equals(other.myOp2));
      }else
         return false;
   }

}

class CompositeFloatData extends FloatData
{
   private sandmark.analysis.stacksimulator.PrimitiveData myOp1;
   private sandmark.analysis.stacksimulator.PrimitiveData myOp2;

   public CompositeFloatData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                             sandmark.analysis.stacksimulator.PrimitiveData op2,
                             org.apache.bcel.generic.InstructionHandle h)
   {
      super(h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public CompositeFloatData(sandmark.analysis.stacksimulator.PrimitiveData op1,
                             sandmark.analysis.stacksimulator.PrimitiveData op2,
                             float value,
                             org.apache.bcel.generic.InstructionHandle h)
   {
      super(value, h);
      myOp1 = op1;
      myOp2 = op2;
   }

   public boolean equals(Object o)
   {
      if(o instanceof CompositeFloatData){
         CompositeFloatData other = (CompositeFloatData)o;
         return super.equals(o) &&
            (myOp1==null?true:myOp1.equals(other.myOp1)) &&
            (myOp2==null?true:myOp2.equals(other.myOp2));
      }else
         return false;
   }

}

