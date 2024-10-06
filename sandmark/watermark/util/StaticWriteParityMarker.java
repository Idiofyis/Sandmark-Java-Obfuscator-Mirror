package sandmark.watermark.util;

/**
 * This class encodes one-bit values in basic blocks.  It does so
 * using parity which can be toggled by inserting one of the following
 * instruction sequences into the basic block:<br>
 * <pre>
 * getstatic <i>dummyfield</i>
 * ineg
 * putstatic <i>dummyfield</i>
 * </pre>
 * or
 * <pre>
 * getstatic <i>dummyfield</i>
 * iconst_1
 * iadd
 * putstatic <i>dummyfield</i>
 * </pre>
 * where <i>dummyfield</i> is a field added to the program during
 * marking, and is hence not used anywhere by the program.  For blocks
 * from instance methods, an instance field is used, so the first
 * and last instructions would be getfield and putfield, 
 * respectively.<br>
 * <br>
 * Note that this approach will only work if getstatic, putstatic, getfield,
 * and putfield instructions do not end basic blocks.  Because of
 * that, classed derived from this one should only be used to
 * mark basic blocks from a 
 * {@link sandmark.analysis.controlflowgraph.MethodCFG}
 * where the possibility of a thrown exception does not end
 * a basic block.
 *
 */

public abstract class StaticWriteParityMarker extends BasicBlockMarker {
   private String dummyFieldName;
   private sandmark.program.Class dummyClass;
   private java.util.HashMap instanceFields;
   private boolean increment;

   private class TrivialIterator implements java.util.Iterator {
      private int value;
      private boolean alreadyReturned;

      public TrivialIterator(int _value) {
	 value = _value;
	 alreadyReturned = false;
      }

      public boolean hasNext() {
	 return !alreadyReturned;
      }

      public Object next() {
	 if (alreadyReturned)
	    throw new java.util.NoSuchElementException();

	 alreadyReturned = true;
	 return java.math.BigInteger.valueOf(value);
      }

      public void remove() {
	 throw new UnsupportedOperationException();
      }
   }

   /**
    * Constructs a new marker which will create a new static field in
    * the given class if necessary.  The field is not added until an
    * attempt is made to mark a basic block from a static method.  When
    * a basic block from an instance method is marked, an instance
    * field is added to the class the basic block came from instead.
    *
    * @param clazz class to create a new dummy static field in
    * @param _increment If this argument is true, each added piece of
    *    code will increment a new field by 1.  If it is
    *    false, each piece will negate the new field.  Note that
    *    the negating instruction sequence consists of 3 instructions,
    *    while the incrementing instruction sequence consists of 4.
    */
   protected StaticWriteParityMarker(sandmark.program.Class clazz,
				     boolean _increment) {
      dummyClass = clazz;
      increment = _increment;
      instanceFields = new java.util.HashMap();
   }

   /**
    * Returns either 0 or 1, based on what the parity of the given basic block
    * currently is.  How exactly that parity is computed is determined by each
    * subclass
    */
   protected abstract int getParity(sandmark.analysis.controlflowgraph.BasicBlock b);

   /**
    * Attempts to encode the given value in the given basic block.  The
    * basic block is modified in place.  If the given value is something
    * other than 0 or 1, 
    * an {@link IllegalArgumentException} 
    * will be thrown.
    *
    * @param b basic block to encode data in
    * @param value data to encode
    * @throws IllegalArgumentException if value is neither 0 nor 1
    * @see #getCapacity(sandmark.analysis.controlflowgraph.BasicBlock)
    */
   public final void embed(sandmark.analysis.controlflowgraph.BasicBlock b,
			   java.math.BigInteger value) {
      if (!value.equals(java.math.BigInteger.ZERO) &&
	  !value.equals(java.math.BigInteger.ONE))
	 throw new IllegalArgumentException(value.toString());

      mark(b, value.intValue());
   }

   /**
    * Returns an {@link java.util.Iterator Iterator} over all values 
    * found to be embedded in the
    * given basic block.  Only values embedded using the marking scheme
    * used by this marker will be reported.<br><br>
    * The {@link java.util.Iterator} will iterate over
    * exactly one value, and that value will be either 0 or 1.
    *
    * @param b basic block to search for a mark in
    */
   public final java.util.Iterator recognize(sandmark.analysis.controlflowgraph.BasicBlock b) {
      return new TrivialIterator(getParity(b));
   }

   /**
    * Returns the number of bits that can be encoded into the given 
    * basic block.
    * This method always returns 1.
    *
    * @param b basic block to report the bit capacity of
    * @see #embed(sandmark.analysis.controlflowgraph.BasicBlock, 
    *             java.math.BigInteger)
    * @see #embed(sandmark.analysis.controlflowgraph.BasicBlock, long)
    */
   public final int getCapacity(sandmark.analysis.controlflowgraph.BasicBlock b) {
      return 1;
   }
 
   /**
    * Returns true if this class returns the same capacity for any basic block,
    * including null.  Since the capacity of each basic block under this marker
    * is exactly one bit, this method returns true.
    *
    * @see #getCapacity(sandmark.analysis.controlflowgraph.BasicBlock)
    */
   final boolean capacityIsConstant() {
      return true;
   }

   static sandmark.program.Method findMethod(sandmark.analysis.controlflowgraph.BasicBlock b) {
      if (b == null)
	 throw new RuntimeException("null basic block");
      sandmark.analysis.controlflowgraph.MethodCFG cfg =
	 (sandmark.analysis.controlflowgraph.MethodCFG)(b.graph());
      if (cfg == null)
	 throw new RuntimeException("null cfg: " + b);
      return cfg.method();
   }

   private void mark(sandmark.analysis.controlflowgraph.BasicBlock b,
		     int desiredParity) {
      if (getParity(b) != desiredParity) {
	 java.util.ArrayList l = b.getInstList();
	 org.apache.bcel.generic.InstructionHandle ih = 
	    (org.apache.bcel.generic.InstructionHandle)l.get(l.size()-1);
	 org.apache.bcel.generic.InstructionTargeter [] targeters =
	     ih.getTargeters();
	 sandmark.program.Method method = findMethod(b);
	 org.apache.bcel.generic.InstructionList il = 
	     method.getInstructionList();
	 int index = addFieldref(b);

	 org.apache.bcel.generic.InstructionHandle newTarget = 
	     il.insert(ih, (method.isStatic() || method.getName().equals("<init>")) ? 
		       (org.apache.bcel.generic.Instruction)new org.apache.bcel.generic.GETSTATIC(index) :
		       (org.apache.bcel.generic.Instruction)
		       new org.apache.bcel.generic.ALOAD(0));
	 if(!method.isStatic() && !method.getName().equals("<init>")) {
	     il.insert(ih,new org.apache.bcel.generic.DUP());
	     il.insert(ih,new org.apache.bcel.generic.GETFIELD(index));
	 }
	 if (increment) {
	    il.insert(ih, new org.apache.bcel.generic.ICONST(1));
	    il.insert(ih, new org.apache.bcel.generic.IADD());
	 }
	 else
	    il.insert(ih, new org.apache.bcel.generic.INEG());
	 il.insert(ih, (method.isStatic() || method.getName().equals("<init>")) ? 
		   (org.apache.bcel.generic.Instruction)new org.apache.bcel.generic.PUTSTATIC(index) :
		   (org.apache.bcel.generic.Instruction)
		   new org.apache.bcel.generic.PUTFIELD(index));

	 if (targeters != null)
	    for (int i = 0; i < targeters.length; i++)
	       targeters[i].updateTarget(ih, newTarget);
      }
   }

   private String addField(sandmark.program.Class clazz,
			   boolean staticField) {
      String fieldName = "dummyint";
      int suffix = 0;
      while (clazz.containsField(fieldName + suffix,"I") != null)
	 suffix++;
      fieldName = fieldName + suffix;

      int qualifiers = org.apache.bcel.Constants.ACC_PUBLIC;
      if (staticField)
	 qualifiers |= org.apache.bcel.Constants.ACC_STATIC;

      sandmark.program.Field field = 
          new sandmark.program.LocalField(clazz,qualifiers,
                                          org.apache.bcel.generic.Type.INT,
                                          fieldName);

      return fieldName;
   }

   private int addFieldref(sandmark.analysis.controlflowgraph.BasicBlock b) {
      sandmark.program.Method method = findMethod(b);
      int index = 0;

      if (method.isStatic() || method.getName().equals("<init>")) {
	 if (dummyFieldName == null)
	    dummyFieldName = addField(dummyClass, true);
	 index = method.getEnclosingClass().getConstantPool().addFieldref
             (dummyClass.getName(),dummyFieldName, "I");
      }
      else {
	 String fieldName = null;
	 if (!instanceFields.containsKey(method.getEnclosingClass())) {
	    fieldName = addField(method.getEnclosingClass(), false);
	    instanceFields.put(method.getEnclosingClass(), fieldName);
	 }
	 else
	    fieldName = (String)instanceFields.get(method.getEnclosingClass());
	 index = method.getEnclosingClass().getConstantPool().addFieldref
             (method.getEnclosingClass().getName(),fieldName, "I");
      }

      return index;
   }
}

