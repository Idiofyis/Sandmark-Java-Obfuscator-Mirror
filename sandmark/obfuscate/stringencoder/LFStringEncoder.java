package sandmark.obfuscate.stringencoder;

public class LFStringEncoder  extends sandmark.obfuscate.AppObfuscator{

   public void mapLDC(sandmark.program.Class cls, 
		      java.util.Map indexMap) {   
         sandmark.program.Method methods[] = cls.getMethods();
               
         for(int j = 0; j < methods.length; j++) {
            sandmark.program.Method mg = methods[j];
            org.apache.bcel.generic.InstructionList il = 
	       mg.getInstructionList();  
            org.apache.bcel.generic.InstructionHandle ihs[] = 
               il.getInstructionHandles();
	    
            for(int k = 1; k < ihs.length; k++){
	       org.apache.bcel.generic.Instruction insn = 
		  ihs[k].getInstruction();
	       
	       if (insn instanceof org.apache.bcel.generic.LDC ||
		   insn instanceof org.apache.bcel.generic.LDC2_W) {
		  int oldIndex = 
		     ((org.apache.bcel.generic.CPInstruction)insn).getIndex();
		  Integer newIndex = 
		     (Integer)indexMap.get(new Integer(oldIndex));
		  if (newIndex != null) {                           
		     org.apache.bcel.generic.Instruction newInsn = 
			new org.apache.bcel.generic.LDC(newIndex.intValue());
		     org.apache.bcel.generic.InstructionHandle newIH = 
			il.append(ihs[k], newInsn);
		     try {
			if (ihs[k].hasTargeters()) {
			    org.apache.bcel.generic.InstructionTargeter it[] =
				    ihs[k].getTargeters();
			    for (int jj = 0; jj < it.length; jj++)
				it[jj].updateTarget(ihs[k],newIH);
			}
			il.delete(ihs[k]);                           
		     }
		     catch(org.apache.bcel.generic.TargetLostException e) {
		        throw new Error("just removed all targeters...");
		     }
		  }
		  
	       }
	    }

	    mg.setMaxStack();
	    mg.mark();
         }
   }
   
   
   public void encodeConstantPool(sandmark.program.Application app) {
      java.util.Iterator it = app.classes();
	 
      while (it.hasNext()) {
	 sandmark.program.Class cls = (sandmark.program.Class)it.next();
	 org.apache.bcel.generic.ConstantPoolGen cpg = cls.getConstantPool();
	 int size = cpg.getSize();
	 java.util.Map indexMap = new java.util.HashMap();

	 for(int i = 0; i < size; i++) {
	    org.apache.bcel.classfile.Constant c = cpg.getConstant(i);
	    
	    if (c != null) {                  
	       if (c instanceof org.apache.bcel.classfile.ConstantUtf8) {
		  org.apache.bcel.classfile.ConstantUtf8 u =
		     (org.apache.bcel.classfile.ConstantUtf8)c;
		  int newIndex = cpg.addUtf8(encode(u.getBytes()));
		  indexMap.put(new Integer(i), new Integer(newIndex));
	       }
	       else if (c instanceof org.apache.bcel.classfile.ConstantString) {
		  org.apache.bcel.classfile.ConstantString s =
		     (org.apache.bcel.classfile.ConstantString)c;
		  org.apache.bcel.classfile.ConstantPool cp = 
		     cpg.getFinalConstantPool();
		  int newIndex = cpg.addString(encode(s.getBytes(cp)));
		  indexMap.put(new Integer(i), new Integer(newIndex));
               }
	    }
	 }

	 mapLDC(cls, indexMap);
	 cls.mark();
      }
   }
   
   public static String encode(String s) {
      int i, n, Reg = 0;
      char []Result = new char[s.length() + 4];
      char Mask, temp;
      String StringResult;
      
      Reg = 0xABCDABCD;
      
      Result[0] = (char) ((Reg >> 24)& 0xff);
      Result[1] = (char) ((Reg >> 16) & 0xff);
      Result[2] = (char) ((Reg >> 8) & 0xff);
      Result[3] = (char) (Reg & 0xff);

      for(i = 0; i < s.length(); ++i) {
         temp = 0x0000;
         Mask = 0x8000;
         
         for(n = 0; n < 16; ++n) {
            if((Reg & 0x00000001) == 1) {
	       Reg = ((Reg ^ 0x80000057) >> 1) | 0x80000000;
	       temp = (char) (temp | Mask);
            }
            else {
	       Reg = Reg >> 1;
            }
	    
            Mask >>= 1;
         }
	 
         Result[i + 4] = (char) (s.charAt(i) ^ temp);
      }
      
      StringResult = new String(Result);
      
      return(StringResult);
   }

   public void scanClass(sandmark.program.Application app) {
      org.apache.bcel.generic.Type[] argTypes = 
	 {org.apache.bcel.generic.Type.STRING};
      
      java.util.Iterator it = app.classes();

      while (it.hasNext()) {
	 sandmark.program.Class cls = (sandmark.program.Class)it.next();
	 org.apache.bcel.generic.ConstantPoolGen cpg = cls.getConstantPool();
	 
	 sandmark.program.Method[] methods = cls.getMethods();
	 
	 for (int j=0; j<methods.length; j++) {
	    sandmark.program.Method mg = methods[j];
	    org.apache.bcel.generic.InstructionList il = 
	       mg.getInstructionList();                  
	    org.apache.bcel.generic.InstructionFactory factory = 
	       new org.apache.bcel.generic.InstructionFactory(cpg); 
	    if (il == null)
	       continue;
	    org.apache.bcel.generic.InstructionHandle ihs[] = 
	       il.getInstructionHandles();
	    for (int k=1; k<ihs.length; k++){
	       org.apache.bcel.generic.Instruction insn = 
		  ihs[k].getInstruction();
	       
	       if (insn instanceof org.apache.bcel.generic.LDC ||
		   insn instanceof org.apache.bcel.generic.LDC2_W) {
		  org.apache.bcel.generic.CPInstruction cpinsn =
		     (org.apache.bcel.generic.CPInstruction)insn;
		  int index = cpinsn.getIndex();
		  org.apache.bcel.classfile.Constant c = 
		     cpg.getConstant(index);
		  if (c instanceof org.apache.bcel.classfile.ConstantString ||
		      c instanceof org.apache.bcel.classfile.ConstantUtf8) {
		     org.apache.bcel.generic.Instruction invoke = 
			factory.createInvoke("Obfuscator",
					     "DecodeString",
					     org.apache.bcel.generic.Type.STRING,
					     argTypes, 
					     org.apache.bcel.Constants.INVOKESTATIC);
		     il.append(ihs[k], invoke);
		  }
	       }
	    }
	    
	    mg.setMaxStack();
	    mg.mark();
	 }
      }
   }

   
   private void InsertDecodeInstructions(sandmark.program.Class cls, org.apache.bcel.generic.InstructionList il) {
      
      org.apache.bcel.generic.InstructionFactory _factory; 
      org.apache.bcel.generic.ConstantPoolGen _cp = cls.getConstantPool();
      _factory = new org.apache.bcel.generic.InstructionFactory(_cp);  

 //     il.append(_factory.createPrintln("decoding..."));
         il.append(new org.apache.bcel.generic.PUSH(_cp, 0));
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 3));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.String", "length", org.apache.bcel.generic.Type.INT, org.apache.bcel.generic.Type.NO_ARGS, org.apache.bcel.Constants.INVOKEVIRTUAL));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 4));
    il.append(org.apache.bcel.generic.InstructionConstants.ISUB);
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 4));
    il.append(_factory.createNew("java.lang.StringBuffer"));
    il.append(org.apache.bcel.generic.InstructionConstants.DUP);
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.String", "length", org.apache.bcel.generic.Type.INT, org.apache.bcel.generic.Type.NO_ARGS, org.apache.bcel.Constants.INVOKEVIRTUAL));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 4));
    il.append(org.apache.bcel.generic.InstructionConstants.ISUB);
    il.append(_factory.createInvoke("java.lang.StringBuffer", "<init>", org.apache.bcel.generic.Type.VOID, new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, org.apache.bcel.Constants.INVOKESPECIAL));
    il.append(_factory.createStore(org.apache.bcel.generic.Type.OBJECT, 5));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 0));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 0));
    il.append(_factory.createInvoke("java.lang.String", "charAt", org.apache.bcel.generic.Type.CHAR, new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, org.apache.bcel.Constants.INVOKEVIRTUAL));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 24));
    il.append(org.apache.bcel.generic.InstructionConstants.ISHL);
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 0));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 1));
    il.append(_factory.createInvoke("java.lang.String", "charAt", org.apache.bcel.generic.Type.CHAR, new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, org.apache.bcel.Constants.INVOKEVIRTUAL));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 255));
    il.append(org.apache.bcel.generic.InstructionConstants.IAND);
    il.append(new org.apache.bcel.generic.PUSH(_cp, 16));
    il.append(org.apache.bcel.generic.InstructionConstants.ISHL);
    il.append(org.apache.bcel.generic.InstructionConstants.IOR);
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 0));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 2));
    il.append(_factory.createInvoke("java.lang.String", "charAt", org.apache.bcel.generic.Type.CHAR, new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, org.apache.bcel.Constants.INVOKEVIRTUAL));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 255));
    il.append(org.apache.bcel.generic.InstructionConstants.IAND);
    il.append(new org.apache.bcel.generic.PUSH(_cp, 8));
    il.append(org.apache.bcel.generic.InstructionConstants.ISHL);
    il.append(org.apache.bcel.generic.InstructionConstants.IOR);
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 0));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 3));
    il.append(_factory.createInvoke("java.lang.String", "charAt", org.apache.bcel.generic.Type.CHAR, new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, org.apache.bcel.Constants.INVOKEVIRTUAL));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 255));
    il.append(org.apache.bcel.generic.InstructionConstants.IAND);
    il.append(org.apache.bcel.generic.InstructionConstants.IOR);
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 3));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 4));
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 1));
        org.apache.bcel.generic.BranchInstruction goto_72 = _factory.createBranchInstruction(org.apache.bcel.Constants.GOTO, null);
    il.append(goto_72);
    org.apache.bcel.generic.InstructionHandle ih_75 = il.append(new org.apache.bcel.generic.PUSH(_cp, 0));
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 7));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 32768));
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 6));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 0));
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 2));
        org.apache.bcel.generic.BranchInstruction goto_84 = _factory.createBranchInstruction(org.apache.bcel.Constants.GOTO, null);
    il.append(goto_84);
    org.apache.bcel.generic.InstructionHandle ih_87 = il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 3));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 1));
    il.append(org.apache.bcel.generic.InstructionConstants.IAND);
    il.append(new org.apache.bcel.generic.PUSH(_cp, 1));
        org.apache.bcel.generic.BranchInstruction if_icmpne_91 = _factory.createBranchInstruction(org.apache.bcel.Constants.IF_ICMPNE, null);
    il.append(if_icmpne_91);
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 3));
    il.append(new org.apache.bcel.generic.PUSH(_cp, -2147483561));
    il.append(org.apache.bcel.generic.InstructionConstants.IXOR);
    il.append(new org.apache.bcel.generic.PUSH(_cp, 1));
    il.append(org.apache.bcel.generic.InstructionConstants.ISHR);
    il.append(new org.apache.bcel.generic.PUSH(_cp, -2147483648));
    il.append(org.apache.bcel.generic.InstructionConstants.IOR);
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 3));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 7));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 6));
    il.append(org.apache.bcel.generic.InstructionConstants.IOR);
    il.append(org.apache.bcel.generic.InstructionConstants.I2C);
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 7));
        org.apache.bcel.generic.BranchInstruction goto_112 = _factory.createBranchInstruction(org.apache.bcel.Constants.GOTO, null);
    il.append(goto_112);
    org.apache.bcel.generic.InstructionHandle ih_115 = il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 3));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 1));
    il.append(org.apache.bcel.generic.InstructionConstants.ISHR);
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 3));
    org.apache.bcel.generic.InstructionHandle ih_119 = il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 6));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 1));
    il.append(org.apache.bcel.generic.InstructionConstants.ISHR);
    il.append(org.apache.bcel.generic.InstructionConstants.I2C);
    il.append(_factory.createStore(org.apache.bcel.generic.Type.INT, 6));
    il.append(new org.apache.bcel.generic.IINC(2, 1));
    org.apache.bcel.generic.InstructionHandle ih_129 = il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 2));
    il.append(new org.apache.bcel.generic.PUSH(_cp, 16));
        org.apache.bcel.generic.BranchInstruction if_icmplt_132 = _factory.createBranchInstruction(org.apache.bcel.Constants.IF_ICMPLT, ih_87);
    il.append(if_icmplt_132);
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 5));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 0));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 1));
    il.append(_factory.createInvoke("java.lang.String", "charAt", org.apache.bcel.generic.Type.CHAR, new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT }, org.apache.bcel.Constants.INVOKEVIRTUAL));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 7));
    il.append(org.apache.bcel.generic.InstructionConstants.IXOR);
    il.append(org.apache.bcel.generic.InstructionConstants.I2C);
    il.append(_factory.createInvoke("java.lang.StringBuffer", "append", org.apache.bcel.generic.Type.STRINGBUFFER, new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.CHAR }, org.apache.bcel.Constants.INVOKEVIRTUAL));
    il.append(org.apache.bcel.generic.InstructionConstants.POP);
    il.append(new org.apache.bcel.generic.IINC(1, 1));
    org.apache.bcel.generic.InstructionHandle ih_153 = il.append(_factory.createLoad(org.apache.bcel.generic.Type.INT, 1));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.String", "length", org.apache.bcel.generic.Type.INT, org.apache.bcel.generic.Type.NO_ARGS, org.apache.bcel.Constants.INVOKEVIRTUAL));
        org.apache.bcel.generic.BranchInstruction if_icmplt_158 = _factory.createBranchInstruction(org.apache.bcel.Constants.IF_ICMPLT, ih_75);
    il.append(if_icmplt_158);
    il.append(_factory.createNew("java.lang.String"));
    il.append(org.apache.bcel.generic.InstructionConstants.DUP);
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 5));
    il.append(_factory.createInvoke("java.lang.String", "<init>", org.apache.bcel.generic.Type.VOID, new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.STRINGBUFFER }, org.apache.bcel.Constants.INVOKESPECIAL));
    il.append(_factory.createStore(org.apache.bcel.generic.Type.OBJECT, 8));
    il.append(_factory.createLoad(org.apache.bcel.generic.Type.OBJECT, 8));
    il.append(_factory.createReturn(org.apache.bcel.generic.Type.OBJECT));
    goto_72.setTarget(ih_153);
    goto_84.setTarget(ih_129);
    if_icmpne_91.setTarget(ih_115);
    goto_112.setTarget(ih_119);

      }
   
      
    public sandmark.program.Class AddClass(sandmark.program.Application app,
       String ClassName) {
      int ClassFlags = org.apache.bcel.Constants.ACC_PUBLIC;
      int MethodFlags = org.apache.bcel.Constants.ACC_PUBLIC | org.apache.bcel.Constants.ACC_STATIC;
      String[] ArgNames = { "arg0" };      String Interfaces[] = {};
      sandmark.program.LocalClass NewClass;
      sandmark.program.LocalMethod NewMethod;
      org.apache.bcel.generic.Type ReturnType = org.apache.bcel.generic.Type.STRING;
      org.apache.bcel.generic.Type[] ArgTypes =  {org.apache.bcel.generic.Type.STRING};      org.apache.bcel.generic.InstructionList InstrList;
      
      NewClass = new sandmark.program.LocalClass(app, ClassName, 
         "java.lang.Object", ClassName + ".java", ClassFlags, Interfaces);
      
      InstrList = new org.apache.bcel.generic.InstructionList();
      InsertDecodeInstructions(NewClass, InstrList);

      NewMethod = new sandmark.program.LocalMethod(NewClass, MethodFlags, 
         ReturnType, ArgTypes, ArgNames, "DecodeString", InstrList);

      NewMethod.setMaxStack();
      NewMethod.setMaxLocals();
  
      return(NewClass);
   }   
   
    public String getAuthor()
    {
        return "David Leventhal";
    }

    public String getAuthorEmail()
    {
        return "collberg@cs.arizona.edu";
    }

    public String getDescription()
    {
        return "This obfuscator replaces strings by 'encrypted' versions.";
    }

    public sandmark.config.ModificationProperty [] getMutations()
    {
        return new sandmark.config.ModificationProperty[]{};
    }

    public String getShortName()
    {
        return "String Encoder";
    }

    public String getLongName()
    {
        return "String Encoder";
    }

    public String getAlgHTML()
    {
        return
            "<HTML><BODY>" +
            "StringEncoder replaces literal strings with calls to a method that generates them." +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href =\"mailto:collberg@cs.arizona.edu\">David Leventhal</a>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }

    public String getAlgURL()
    {
       return "sandmark/obfuscate/stringencoder/doc/help.html";
    }


   public void ObfuscateJar(String JarFile) {
      sandmark.program.Application app;
      
      try {
         
         app = new sandmark.program.Application(JarFile);
   
         
         encodeConstantPool(app);
         scanClass(app);

         AddClass(app, "Obfuscator");
      }
      catch(Exception e) {
         
         //System.out.println(e.getMessage());
      }
   }



   
   // Added by CC.
   public void apply(sandmark.program.Application app) throws Exception {
      encodeConstantPool(app);
      scanClass(app);
      
      AddClass(app, "Obfuscator");
   }

   public static void main (String[] args)	{
      
      LFStringEncoder Obfuscator = new LFStringEncoder();

      System.out.println("Processing Jar File \"" + args[0] + "\"");

      Obfuscator.ObfuscateJar(args[0]);
   }
}
   

