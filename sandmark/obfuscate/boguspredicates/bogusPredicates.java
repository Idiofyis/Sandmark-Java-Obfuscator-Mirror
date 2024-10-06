 /** Algorithm Implemented by : Ashok Purushotham & RathnaPrabhu
    emails : ashok@cs.arizona.edu
             prabhu@cs.arizona.edu

    This algorithm implements simple boolean identities  and add them to the user's code.
 */
package sandmark.obfuscate.boguspredicates;

//import java.io.*;
//import java.util.*;
//import java.util.zip.*;


public class bogusPredicates extends  sandmark.obfuscate.ClassObfuscator
{	
    public static final boolean DEBUG = false;
    java.util.ArrayList pred_list = new java.util.ArrayList();
    
    public bogusPredicates(){
	
	/* A list of carefully chosen  predicates are maintained as a list */
	java.util.ArrayList predicates[] = new java.util.ArrayList[3];
	
	org.apache.bcel.generic.IADD iadd =
	    new org.apache.bcel.generic.IADD();
	org.apache.bcel.generic.ISUB isub =
	    new org.apache.bcel.generic.ISUB();
	org.apache.bcel.generic.IMUL imul =
	    new org.apache.bcel.generic.IMUL();
	org.apache.bcel.generic.IREM irem =
	    new org.apache.bcel.generic.IREM();
	org.apache.bcel.generic.ICONST push0 =
	    new org.apache.bcel.generic.ICONST(0);
	org.apache.bcel.generic.ICONST push1 =
	    new org.apache.bcel.generic.ICONST(1);
	org.apache.bcel.generic.ICONST push2 =
	    new org.apache.bcel.generic.ICONST(2);
	org.apache.bcel.generic.ICONST push3 =
	    new org.apache.bcel.generic.ICONST(3);
	org.apache.bcel.generic.DUP dup =
	    new org.apache.bcel.generic.DUP();
	org.apache.bcel.generic.IDIV idiv =
	    new org.apache.bcel.generic.IDIV();
	

       	
	predicates[0] = new java.util.ArrayList();
	predicates[0].add(dup);
	predicates[0].add(dup);
	predicates[0].add(dup);
	predicates[0].add(imul);
	predicates[0].add(imul);
	predicates[0].add(isub);
	predicates[0].add(push3);
	predicates[0].add(irem);
	pred_list.add(predicates[0]) ;
	predicates[1] = new java.util.ArrayList();
	predicates[1].add(dup);
	predicates[1].add(push1);
	predicates[1].add(iadd);
	predicates[1].add(imul);
	predicates[1].add(push2);
	predicates[1].add(irem);
	pred_list.add(predicates[1]);
	predicates[2] = new java.util.ArrayList();
	predicates[2].add(dup);
	predicates[2].add(imul);
	predicates[2].add(push2);
	predicates[2].add(idiv);
	predicates[2].add(push2);
	predicates[2].add(irem);
	pred_list.add(predicates[2]);
	
	
    }

    public String getShortName() {
	return "Simple Opaque Predicates";
    }

    public String getLongName() {
	return "Adds bogus predicates to conditional expressions ";
    }

    public java.lang.String getAlgHTML(){
	return
	    "<HTML><BODY>" +
	    "BogusPredicates implements simple boolean identities and add them to the user's code.\n" +
	    "<TABLE>" +
	    "<TR><TD>" +
	    "Author: <a href =\"mailto:ashok@cs.arizona.edu\">Ashok Venkatraj</a> and " +
	    "<a href = \"mailto:prabhu@cs.arizona.edu\">RathnaPrabhu</a>\n" +
	    "</TD></TR>" +
	    "</TABLE>" +
	    "</BODY></HTML>";
    }

    public java.lang.String getAlgURL(){
	return "sandmark/obfuscate/boguspredicates/doc/help.html";
    }

    public java.lang.String getAuthor(){
		return "Ashok Purushotham and RathnaPrabhu";
    }

    public java.lang.String getAuthorEmail(){
	return "ashok@cs.arizona.edu and prabhu@cs.arizona.edu";
    }
    
    public java.lang.String getDescription(){
	return "This algorithm implements simple boolean identities " +
	    "and adds them to the user's code.";
    }
    
    
    public void apply(sandmark.program.Class cls) throws Exception {
	
	sandmark.program.Class cg =cls;	
	boolean errflag= false;

	if(cg.isAbstract() || cg.isInterface() || cg.isFinal()){
	    if(DEBUG)
		System.out.println("Skipping interface/abstract class" + cls.getName());
	    return;
	}

	sandmark.program.Method[] methods = cg.getMethods();
	
	org.apache.bcel.generic.ICONST push2 = 
	    new org.apache.bcel.generic.ICONST(2);
	org.apache.bcel.generic.ICONST push1 = 
	    new org.apache.bcel.generic.ICONST(1);
	org.apache.bcel.generic.ICONST push0 = 
	    new org.apache.bcel.generic.ICONST(0);
	org.apache.bcel.generic.IADD iadd = 
	    new org.apache.bcel.generic.IADD();
	org.apache.bcel.generic.IMUL imul =
	    new org.apache.bcel.generic.IMUL();
	org.apache.bcel.generic.IREM irem =
	    new org.apache.bcel.generic.IREM();
	
	
	java.util.Random rand = sandmark.util.Random.getRandom(); //new java.util.Random();
	
	for (int k=0; k < methods.length; k++){
	    sandmark.program.Method mgen = methods[k];
	    String methodName = mgen.getName();
	    if(methodName.equals("<init>") || methodName.equals("<clinit>") || mgen.isFinal())
		continue;
		
	    org.apache.bcel.generic.InstructionList il =
		mgen.getInstructionList();
	    
	    org.apache.bcel.generic.InstructionHandle ihs[] = null;
	    if(il != null)
		ihs = il.getInstructionHandles();

   
	    int maxLocals = mgen.getMaxLocals();
	    maxLocals++;
	    mgen.addLocalVariable("lv", org.apache.bcel.generic.Type.INT, maxLocals, null,
				  null);

	    int nextint = rand.nextInt(100);
	    org.apache.bcel.generic.BIPUSH bipush = 
		new org.apache.bcel.generic.BIPUSH((byte)nextint);
	    org.apache.bcel.generic.ISTORE istorex =
		new org.apache.bcel.generic.ISTORE(maxLocals);
          
	    org.apache.bcel.generic.InstructionList tempIL = new
		org.apache.bcel.generic.InstructionList();
	    tempIL.append(bipush);
	    tempIL.append(istorex);
	    il.insert(tempIL);
	    
	    
	    org.apache.bcel.generic.ILOAD loadx = 
		new org.apache.bcel.generic.ILOAD(maxLocals);
	    java.util.Random rand1 = sandmark.util.Random.getRandom();//new java.util.Random();
	    
	    int i=0;
	    int index = 0;
	    org.apache.bcel.generic.InstructionHandle[] instructions = 
		il.getInstructionHandles();
	    while(i < instructions.length){
		instructions = il.getInstructionHandles();
		org.apache.bcel.generic.InstructionHandle ih = instructions[i];
		org.apache.bcel.generic.Instruction instr = ih.getInstruction();
		if(instr instanceof org.apache.bcel.generic.IfInstruction){
		    org.apache.bcel.generic.IfInstruction iinst =
			(org.apache.bcel.generic.IfInstruction)instr;
		    index = i + 1;
		    org.apache.bcel.generic.InstructionHandle target =
			iinst.getTarget();
		    org.apache.bcel.generic.IF_ICMPNE icmp = 
			new org.apache.bcel.generic.IF_ICMPNE(target);
		    int pred_num = rand1.nextInt(3);
		    il.append(iinst, loadx);
		    instructions = il.getInstructionHandles();
		    java.util.ArrayList al = (java.util.ArrayList)pred_list.get(pred_num);
		    int j;
		    org.apache.bcel.generic.InstructionHandle pinst = null;
		    for(j=0; j < al.size(); j++){
			pinst = instructions[index];
			il.append(pinst, (org.apache.bcel.generic.Instruction)al.get(j));
			instructions = il.getInstructionHandles();
			index++;
		    }
		    pinst = instructions[index];
		    il.append(pinst, push0);
		    instructions = il.getInstructionHandles();
		    index++;
		    pinst = instructions[index];
		    il.append(pinst, icmp);
		    instructions = il.getInstructionHandles();
		    index++;
		    i = i + j + 3;
		    
		}
		i++;
	    }//end while
	    
	    
	    mgen.setMaxLocals();
	    mgen.setMaxStack();
	    //mgen.update();
	    //methods[k] = mgen.getMethod();			
	    //methodEditor.commit();
	}     // end of for
	//cg.setMethods(methods);
    }

    public sandmark.config.ModificationProperty [] getMutations()
    {
        return null;
    }

}






