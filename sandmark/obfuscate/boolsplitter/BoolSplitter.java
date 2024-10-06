package sandmark.obfuscate.boolsplitter;

public class BoolSplitter extends sandmark.obfuscate.MethodObfuscator
   implements org.apache.bcel.Constants{

   sandmark.program.Method mg;
   public sandmark.analysis.controlflowgraph.MethodCFG cfg;

   org.apache.bcel.generic.InstructionList il;
   org.apache.bcel.generic.InstructionHandle[] ih;
   org.apache.bcel.generic.InstructionHandle ihandle;
   java.util.ArrayList boollist,nonboollist,unknownlist;
   sandmark.analysis.stacksimulator.Context cn;
   sandmark.analysis.stacksimulator.StackSimulator st;
   boolean endflag;
   java.util.HashMap indexmap;
   
   private static boolean dynamicRandom = true;

   public String getShortName(){
      return "Boolean Splitter";
   }

   public sandmark.config.ModificationProperty[] getMutations() {
      return null;
   }

   public String getLongName() {
      return "Boolean Splitter";
   }

   public void setConfigProperties(sandmark.util.ConfigProperties props){}

   public java.lang.String getAlgHTML()
   {
      return
         "<HTML><BODY>" +
         "Boolean Splitter is a class obfuscator." +
         " The algorithm detects boolean variables and arrays" +
         " and modifies all uses and definitions of these variables." +
         " Every boolean variable or array element is split into 2 variables"+
         " or array elements, and the state of the original variable is" +
         " reflected in the combination state of these 2 variables or array"+ 
         " elements." +
         "<TABLE>" +
         "<TR><TD>" +
         "Author: <a href =\"mailto:kamlesh@cs.arizona.edu\">Kamlesh Kantilal</a>\n" +
         "</TD></TR>" +
         "</TABLE>" +
         "</BODY></HTML>";
   }

   public java.lang.String getAlgURL(){
      return "sandmark/obfuscate/boolsplitter/doc/help.html";
   }

   public java.lang.String getAuthor(){
      return "Kamlesh Kantilal";
   }

   public java.lang.String getAuthorEmail(){
      return "kamlesh@cs.arizona.edu";
   }

   public java.lang.String getDescription(){
      return "This algorithm detects boolean variables and arrays and modifies all uses and definitions of these variables.";
   }

   public void apply(sandmark.program.Method meth) throws Exception{
      mg = meth;

      il=mg.getInstructionList();
      if(il==null)
         return;
      cfg=mg.getCFG();
      boollist=new java.util.ArrayList();
      nonboollist=new java.util.ArrayList();
      unknownlist=new java.util.ArrayList();
      indexmap=new java.util.HashMap();
      ih=il.getInstructionHandles();
      st=new sandmark.analysis.stacksimulator.StackSimulator(mg);
      detectBoolVariables();  
      splitBoolVariables();
      //      replBoolArrays();

      mg.setMaxStack();
      mg.setMaxLocals();
   }

   ////////////////////////////////////////////////////////////////////////////


   private void replBoolArrays(){
      org.apache.bcel.generic.Instruction inst;
      org.apache.bcel.generic.NEWARRAY insttemp = 
         new org.apache.bcel.generic.NEWARRAY(org.apache.bcel.generic.Type.BOOLEAN);

      sandmark.analysis.stacksimulator.StackData[] sd;

      for(int parse=0;parse<ih.length;parse++){
         ihandle=ih[parse];
         inst = ihandle.getInstruction();

         if(inst instanceof org.apache.bcel.generic.NEWARRAY){
            if(((org.apache.bcel.generic.NEWARRAY)inst).getType().equals(insttemp.getType())){
               il.insert(ihandle,new org.apache.bcel.generic.ICONST(1));
               il.insert(ihandle,new org.apache.bcel.generic.ISHL());
            }
         }
         else if(inst instanceof org.apache.bcel.generic.BASTORE){
            cn=st.getInstructionContext(ihandle);
            sd=cn.getStackAt(2);
            if(isboolarray(sd)){
               // array, index, value
               il.insert(ihandle,new org.apache.bcel.generic.DUP2_X1());
               // index, value, array, index, value
               il.insert(ihandle,new org.apache.bcel.generic.POP());
               // index, value, array, index
               il.insert(ihandle,new org.apache.bcel.generic.DUP_X1());
               // index, value, index, array, index
               il.insert(ihandle,new org.apache.bcel.generic.POP());
               // index, value, index, array
               il.insert(ihandle,new org.apache.bcel.generic.DUP());
               // index, value, index, array, array
               il.insert(ihandle,new org.apache.bcel.generic.DUP2_X1());
               // index, value, array, array, index, array, array
               il.insert(ihandle,new org.apache.bcel.generic.POP());
               // index, value, array, array, index, array
               il.insert(ihandle,new org.apache.bcel.generic.POP());
               // index, value, array, array, index
               il.insert(ihandle,new org.apache.bcel.generic.ICONST(1));
               // index, value, array, array, index, 1
               il.insert(ihandle,new org.apache.bcel.generic.ISHL());
               // index, value, array, array, (index<<1)
               il.insert(ihandle,new org.apache.bcel.generic.DUP());
               // index, value, array, array, (index<<1), (index<<1)
               il.insert(ihandle,new org.apache.bcel.generic.DUP_X2());
               // index, value, array, (index<<1), array, (index<<1), (index<<1)
               il.insert(ihandle,new org.apache.bcel.generic.POP());
               // index, value, array, (index<<1), array, (index<<1)
               il.insert(ihandle,new org.apache.bcel.generic.ICONST(1));
               // index, value, array, (index<<1), array, (index<<1), 1
               il.insert(ihandle,new org.apache.bcel.generic.IADD());
               // index, value, array, (index<<1), array, (index<<1)+1
               il.insert(ihandle,new org.apache.bcel.generic.ICONST(0));
               // index, value, array, (index<<1), array, (index<<1)+1, 0
               il.insert(ihandle,new org.apache.bcel.generic.BASTORE());
               // index, value, array, (index<<1)        // stored 0 into array[(index<<1)+1]
               il.insert(ihandle,new org.apache.bcel.generic.DUP_X2());
               // index, (index<<1), value, array, (index<<1)
               il.insert(ihandle,new org.apache.bcel.generic.POP());
               // index, (index<<1), value, array
               il.insert(ihandle,new org.apache.bcel.generic.DUP_X2());
               // index, array, (index<<1), value, array
               il.insert(ihandle,new org.apache.bcel.generic.POP());
               // index, array, (index<<1), value
               /* BASTORE */
               // index     // stored value into array[index<<1]
               il.append(ihandle,new org.apache.bcel.generic.POP());
               // <empty>
            }
         }
         else if(inst instanceof org.apache.bcel.generic.BALOAD){
            cn=st.getInstructionContext(ihandle);
            sd=cn.getStackAt(1);
            if(isboolarray(sd)){
               // array, index
               il.insert(ihandle,new org.apache.bcel.generic.ICONST(1));
               // array, index, 1
               il.insert(ihandle,new org.apache.bcel.generic.ISHL());
               // array, (index<<1)
               il.insert(ihandle,new org.apache.bcel.generic.DUP2());
               // array, (index<<1), array, (index<<1)
               il.insert(ihandle,new org.apache.bcel.generic.ICONST(1));
               // array, (index<<1), array, (index<<1), 1
               il.insert(ihandle,new org.apache.bcel.generic.IADD());
               // array, (index<<1), array, (index<<1)+1
               il.insert(ihandle,new org.apache.bcel.generic.BALOAD());
               // array, (index<<1), array[(index<<1)+1]
               il.insert(ihandle,new org.apache.bcel.generic.DUP_X2());
               // array[(index<<1)+1], array, (index<<1), array[(index<<1)+1]
               il.insert(ihandle,new org.apache.bcel.generic.POP());
               // array[(index<<1)+1], array, (index<<1)
               /* BALOAD */
               // array[(index<<1)+1], array[index<<1]
               il.append(ihandle,new org.apache.bcel.generic.IXOR());
               // array[(index<<1)+1]^array[index<<1]
            }
         }
         else if(inst instanceof org.apache.bcel.generic.ARRAYLENGTH){
            cn=st.getInstructionContext(ihandle);
            sd=cn.getStackAt(0);
            if(isboolarray(sd)){
               il.append(ihandle,new org.apache.bcel.generic.ISHR());
               il.append(ihandle,new org.apache.bcel.generic.ICONST(1));
            }
         }
         
      }
      il.setPositions(true);
      il.update();
      mg.setInstructionList(il);
      mg.setInstructionList(il);
      mg.setMaxStack();
   }

   private void detectBoolVariables()
   {
      org.apache.bcel.generic.Type lv[] = 
         mg.getArgumentTypes();
      sandmark.analysis.stacksimulator.StackData[] sd;
      endflag=false;
      int ct=0;

      if(!mg.isStatic())
         ct=1;

      for(int parse=0;parse<lv.length;parse++){
         if(lv[parse].equals(org.apache.bcel.generic.Type.BOOLEAN))
            addbool(ct);
         else
            addnonbool(ct);
         ct+=lv[parse].getSize();
      }
      
      if(lv.length>0)
         for(int parse=ct;parse<mg.getMaxLocals();parse++)
            addunknownbool(parse);

      do{
         endflag=true;
         for(int parse=0;parse<ih.length;parse++){
            ihandle=ih[parse];
            org.apache.bcel.generic.Instruction inst = 
               ihandle.getInstruction();
            
            if(inst instanceof org.apache.bcel.generic.LocalVariableInstruction){
               int index=((org.apache.bcel.generic.LocalVariableInstruction)inst).getIndex();
               if(inst instanceof org.apache.bcel.generic.ISTORE){
                  cn=st.getInstructionContext(ihandle);
                  if(cn.getClass().getName().equals("sandmark.analysis.stacksimulator.EmptyContext"))
		  	removebool(index);
		  else {
		  	sd=cn.getStackAt(0);
		  	if(!acceptable(sd))
                     	  removebool(index);
                  }
               }
               else if(inst instanceof org.apache.bcel.generic.IINC){
                  removebool(index);
               }
               else if(inst instanceof org.apache.bcel.generic.StoreInstruction)
                  removebool(index);
            }
         }
      }while(!endflag);
      
      for(int parse=0;parse<unknownlist.size();parse++)
         addbool(((Integer)unknownlist.get(parse)).intValue());
   }
   
   private void splitBoolVariables()
   {
      org.apache.bcel.generic.Type lv[] = 
         mg.getArgumentTypes();
      int count = mg.getMaxLocals();
      int ct=0;

      if(!mg.isStatic())
         ct=1;

      for(int parse=0;parse<boollist.size();parse++){
         count++;
         indexmap.put(boollist.get(parse),new Integer(count));
      }
     
      //put random boolean value into the new local slots corresponding to parameter booleans
      for(int parse=0;parse<lv.length;parse++){
         if(lv[parse].equals(org.apache.bcel.generic.Type.BOOLEAN)){
            int index1=ct;
            Integer temp=new Integer(index1);
            int index2=((Integer)(indexmap.get(temp))).intValue();	    
            il.insert(new org.apache.bcel.generic.ISTORE(index1));
            il.insert(new org.apache.bcel.generic.IXOR());	    
            il.insert(new org.apache.bcel.generic.ILOAD(index2));	    
            il.insert(new org.apache.bcel.generic.ILOAD(index1));
            il.insert(new org.apache.bcel.generic.ISTORE(index2));
            if(dynamicRandom) insertDynamicRandom();
	    else il.insert(new org.apache.bcel.generic.ICONST(((int)(Math.random() * 2))));
         }
         ct+=lv[parse].getSize();
      }
      
      for(int parse=0;parse<ih.length;parse++){
         ihandle=ih[parse];
         org.apache.bcel.generic.Instruction inst =
            ihandle.getInstruction();
         
         if(inst instanceof org.apache.bcel.generic.ISTORE){
            int index1=((org.apache.bcel.generic.LocalVariableInstruction)inst).getIndex();
            Integer temp=new Integer(index1);
            if(boollist.contains(temp)){
	       if(dynamicRandom) insertDynamicRandom(ihandle);
	       else il.insert(ihandle,new org.apache.bcel.generic.ICONST(((int)(Math.random() * 2))));
               int index2=((Integer)(indexmap.get(temp))).intValue();
               il.insert(ihandle,new org.apache.bcel.generic.ISTORE(index2));
               il.insert(ihandle,new org.apache.bcel.generic.ILOAD(index2));
	       il.insert(ihandle,new org.apache.bcel.generic.IXOR());
	       il.setPositions(true);
               il.update();
            }
         }
         else if(inst instanceof org.apache.bcel.generic.ILOAD){
            int index1=((org.apache.bcel.generic.LocalVariableInstruction)inst).getIndex();
            Integer temp=new Integer(index1);
            if(boollist.contains(temp)){
               int index2=((Integer)(indexmap.get(temp))).intValue();
               il.append(ihandle,new org.apache.bcel.generic.IXOR());
               il.append(ihandle,new org.apache.bcel.generic.ILOAD(index2));
            }
         }
      }
      
      //Now we need to fix any BranchInstructions whose target is an ISTORE
      for(int parse=0;parse<ih.length;parse++){
         ihandle=ih[parse];
         org.apache.bcel.generic.Instruction inst =
            ihandle.getInstruction();
         
         if(inst instanceof org.apache.bcel.generic.BranchInstruction){
	    org.apache.bcel.generic.BranchInstruction binstr = ((org.apache.bcel.generic.BranchInstruction)inst);
	    org.apache.bcel.generic.Instruction storeInstr = binstr.getTarget().getInstruction();
	    if(storeInstr instanceof org.apache.bcel.generic.ISTORE) {
            	int index1=((org.apache.bcel.generic.LocalVariableInstruction)storeInstr).getIndex();
            	Integer temp=new Integer(index1);
		if(boollist.contains(temp)){
			org.apache.bcel.generic.InstructionHandle newTarget = binstr.getTarget().getPrev();
			if(dynamicRandom) {
			  while(!(newTarget.getInstruction() instanceof org.apache.bcel.generic.INVOKESTATIC))
			  	newTarget = newTarget.getPrev();
			}
			else {
			  while(!(newTarget.getInstruction() instanceof org.apache.bcel.generic.ICONST))
			  	newTarget = newTarget.getPrev();
			}
			binstr.setTarget(newTarget);	
		}
	    }
	 }
      }
      il.setPositions(true);
      il.update();
      mg.setInstructionList(il);
      mg.setInstructionList(il);
      mg.setMaxStack();
   }

   private void insertDynamicRandom()
   {
      org.apache.bcel.generic.ConstantPoolGen cpg = mg.getEnclosingClass().getConstantPool();
      int doubleIndex = cpg.addDouble(2.0);
      int methIndex = cpg.addMethodref("java/lang/Math", "random", "()D");
      il.insert(new org.apache.bcel.generic.D2I());
      il.insert(new org.apache.bcel.generic.DMUL());
      il.insert(new org.apache.bcel.generic.LDC2_W(doubleIndex));      
      il.insert(new org.apache.bcel.generic.INVOKESTATIC(methIndex));
   }   

   private void insertDynamicRandom(org.apache.bcel.generic.InstructionHandle ih)
   {
      org.apache.bcel.generic.ConstantPoolGen cpg = mg.getEnclosingClass().getConstantPool();
      int doubleIndex = cpg.addDouble(2.0);
      int methIndex = cpg.addMethodref("java/lang/Math", "random", "()D");
      il.insert(ihandle,new org.apache.bcel.generic.INVOKESTATIC(methIndex));
      il.insert(ihandle,new org.apache.bcel.generic.LDC2_W(doubleIndex));
      il.insert(ihandle,new org.apache.bcel.generic.DMUL());
      il.insert(ihandle,new org.apache.bcel.generic.D2I());
   }

/*
   private void replace(sandmark.analysis.stacksimulator.StackData[] sd)
   {
      org.apache.bcel.generic.InstructionHandle tempih;
      java.util.ArrayList llist=new java.util.ArrayList();

      for(int parse=0;parse<sd.length;parse++){
         tempih=sd[parse].getInstruction();
         if(tempih!=null && !llist.contains(tempih)){
            //il.append(tempih,new org.apache.bcel.generic.ICONST(0));
	    llist.add(tempih);
         }
      }
   }
*/

   private boolean isboolarray(sandmark.analysis.stacksimulator.StackData[] sd)
   {
      org.apache.bcel.generic.Type arraytype = 
         org.apache.bcel.generic.Type.getType("[Z");

      for(int parse=0;parse<sd.length;parse++){
         if(!sd[parse].getType().equals(arraytype))
            return false;
      }
      return true;
   }


   private boolean acceptable(sandmark.analysis.stacksimulator.StackData[] sd)
   {
      for(int parse=0;parse<sd.length;parse++){
         if (sd[parse].getInstruction()==null){
            if (!sd[parse].getType().equals(org.apache.bcel.generic.Type.BOOLEAN))
               return false;
            else
               continue;
         }

         org.apache.bcel.generic.Instruction inst =
            sd[parse].getInstruction().getInstruction();

         if(inst instanceof org.apache.bcel.generic.ICONST){
            int val=((org.apache.bcel.generic.ICONST)inst).getValue().intValue();
            if((val!=0) && (val!=1))
               return false;
         }
         else if(inst instanceof org.apache.bcel.generic.ILOAD){
            int val=((org.apache.bcel.generic.ILOAD)inst).getIndex();
            if(nonboollist.contains(new Integer(val)))
               return false;
         }
         else if (inst instanceof org.apache.bcel.generic.InvokeInstruction){
            org.apache.bcel.generic.Type returnType = 
               ((org.apache.bcel.generic.InvokeInstruction)inst).getReturnType(mg.getConstantPool());
            if (!returnType.equals(org.apache.bcel.generic.Type.BOOLEAN))
               return false;
         }
         else if (inst instanceof org.apache.bcel.generic.INSTANCEOF){
            // this is ok too
         }
         else 
            return false;
      }
      return true;
   }

   private void addbool(int index){
      Integer i = new Integer(index);
      if(boollist.contains(i))
         return;
      boollist.add(i);
   }

   private void addnonbool(int index){
      Integer i = new Integer(index);
      if(nonboollist.contains(i))
         return;
      nonboollist.add(i);
   }

   private void addunknownbool(int index){
      Integer i = new Integer(index);
      if(unknownlist.contains(i))
         return;
      unknownlist.add(i);
   }

   private void removebool(int index){
      Integer i = new Integer(index);
      if(unknownlist.contains(i)){
         unknownlist.remove(i);
         endflag=false;
      }

      if(nonboollist.contains(i))
         return;

      endflag=false;
      addnonbool(index);
   }
}
