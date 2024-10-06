package sandmark.watermark.util;

public class MD5Marker extends BasicBlockMarker {
   private String dummyFieldName;
   private sandmark.program.Class dummyClass;
   private java.util.HashMap instanceFields;
   private int k;
   private java.security.MessageDigest d;
   private java.math.BigInteger key;

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

   public MD5Marker(sandmark.program.Class clazz,
		    int bits, java.math.BigInteger key) {
      dummyClass = clazz;
      k = bits;
      this.key = key;
      instanceFields = new java.util.HashMap();
      try {
	 d = java.security.MessageDigest.getInstance("MD5");
      }
      catch (java.security.NoSuchAlgorithmException e) {
	 throw new java.lang.RuntimeException("MD5 not found");
      }
   }

   public MD5Marker(sandmark.program.Class clazz) {
      this(clazz, 2, new java.math.BigInteger("12345678"));
   }

   public final void embed(sandmark.analysis.controlflowgraph.BasicBlock b,
			   java.math.BigInteger value) {
      if (!value.equals(java.math.BigInteger.ZERO) &&
	  !value.equals(java.math.BigInteger.ONE))
	 throw new IllegalArgumentException(value.toString());

      mark(b, value.intValue());
   }

   public final java.util.Iterator recognize(sandmark.analysis.controlflowgraph.BasicBlock b) {
      return new TrivialIterator(read(b.getInstList()));
   }

   public final int getCapacity(sandmark.analysis.controlflowgraph.BasicBlock b) {
      return 1;
   }
 
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

   private org.apache.bcel.generic.InstructionHandle 
      insert(org.apache.bcel.generic.Instruction insn,
	     org.apache.bcel.generic.InstructionList il,
	     java.util.ArrayList l,
	     org.apache.bcel.generic.InstructionHandle last) {

      org.apache.bcel.generic.InstructionHandle rval =
	 il.insert(last, insn);
      l.add(l.size() - 1, rval);
      return rval;
   }

   private void mark(sandmark.analysis.controlflowgraph.BasicBlock b,
		     int desiredValue) {
      java.util.ArrayList l = b.getInstList();
      if (read(l) != desiredValue) {
	 org.apache.bcel.generic.InstructionHandle newIH = null;
	 org.apache.bcel.generic.InstructionHandle ih = 
	    (org.apache.bcel.generic.InstructionHandle)l.get(l.size()-1);
	 org.apache.bcel.generic.InstructionTargeter [] targeters =
	     ih.getTargeters();
	 sandmark.program.Method method = findMethod(b);
	 org.apache.bcel.generic.InstructionList il = 
	     method.getInstructionList();
	 int index = addFieldref(b);
	 int cval = 1;

	 org.apache.bcel.generic.Instruction loadInsn =
	    (method.isStatic() || method.getName().equals("<init>")) ? 
	     (org.apache.bcel.generic.Instruction)new org.apache.bcel.generic.GETSTATIC(index) : 
	     (org.apache.bcel.generic.Instruction)new org.apache.bcel.generic.ALOAD(0);
	 org.apache.bcel.generic.InstructionHandle newTarget = 
	    insert(loadInsn, il, l, ih);
	 if(!method.isStatic() && !method.getName().equals("<init>")) {
	    insert(new org.apache.bcel.generic.DUP(), il, l, ih);
	    insert(new org.apache.bcel.generic.GETFIELD(index), il, l, ih);
	 }
	 newIH = insert(pushconst(cval++), il, l, ih);
	 insert(new org.apache.bcel.generic.IADD(), il, l, ih);
	 org.apache.bcel.generic.Instruction storeInsn =
	    (method.isStatic() || method.getName().equals("<init>")) ? 
	     (org.apache.bcel.generic.Instruction)new org.apache.bcel.generic.PUTSTATIC(index) : 
	     (org.apache.bcel.generic.Instruction)new org.apache.bcel.generic.PUTFIELD(index);
	 insert(storeInsn, il, l, ih);

	 method.getInstructionList().redirectBranches(ih,newTarget);
	 org.apache.bcel.generic.CodeExceptionGen cgs[] = 
	     method.getExceptionHandlers();
	 for(int i = 0 ; i < cgs.length ; i++)
	     if(cgs[i].getStartPC() == ih)
		 cgs[i].setStartPC(newTarget);
	     else if(cgs[i].getHandlerPC() == ih)
		 cgs[i].setHandlerPC(newTarget);

	 while (read(l) != desiredValue)
	    newIH.setInstruction(pushconst(cval++));
      }
   }

   private org.apache.bcel.generic.Instruction pushconst(int c) {
      if (c >= -1 && c <= 5)
	 return new org.apache.bcel.generic.ICONST(c);
      else if (c >= -128 && c <= 127)
	 return new org.apache.bcel.generic.BIPUSH((byte)c);
      else if (c >= -32768 && c <= 32767)
	 return new org.apache.bcel.generic.SIPUSH((short)c);
      else
	 return null;
   }

   private void addNumber(java.lang.Number n) {
      if (n instanceof java.lang.Byte)
	 d.update(n.byteValue());
      else if (n instanceof java.lang.Short) {
	 d.update((byte)(n.shortValue() >>> 8));
	 d.update((byte)n.shortValue());
      }
      else if (n instanceof java.lang.Integer) {
	 d.update((byte)(n.intValue() >>> 24));
	 d.update((byte)(n.intValue() >>> 16));
	 d.update((byte)(n.intValue() >>> 8));
	 d.update((byte)n.intValue());
      }
      else if (n instanceof java.lang.Long) {
	 d.update((byte)(n.longValue() >>> 56));
	 d.update((byte)(n.longValue() >>> 48));
	 d.update((byte)(n.longValue() >>> 40));
	 d.update((byte)(n.longValue() >>> 32));
	 d.update((byte)(n.longValue() >>> 24));
	 d.update((byte)(n.longValue() >>> 16));
	 d.update((byte)(n.longValue() >>> 8));
	 d.update((byte)n.longValue());
      }
   }

   private synchronized int read(java.util.ArrayList l) {
      d.update(key.toByteArray());
      for (java.util.Iterator i = l.iterator(); i.hasNext(); ) {
	 org.apache.bcel.generic.Instruction insn =
	    ((org.apache.bcel.generic.InstructionHandle)i.next()).getInstruction();
	 if (!(insn instanceof org.apache.bcel.generic.BREAKPOINT)) {
	    short opcode = insn.getOpcode();
	    d.update((byte)(opcode >>> 8));
	    d.update((byte)opcode);
	    if (insn instanceof org.apache.bcel.generic.BIPUSH)
	       addNumber(((org.apache.bcel.generic.BIPUSH)insn).getValue());
	    else if (insn instanceof org.apache.bcel.generic.ICONST)
	       addNumber(((org.apache.bcel.generic.ICONST)insn).getValue());
	    else if (insn instanceof org.apache.bcel.generic.LCONST)
	       addNumber(((org.apache.bcel.generic.LCONST)insn).getValue());
	    else if (insn instanceof org.apache.bcel.generic.SIPUSH)
	       addNumber(((org.apache.bcel.generic.SIPUSH)insn).getValue());
	 }
      }
      byte digest[] = d.digest();
      int rval = 1;
      for (int index = 0; 8*index < k; index++)
	 for (int j = 0; j < 8 && 8*index+j < k; j++)
	    rval &= digest[index] >>> j;
      return rval;
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

