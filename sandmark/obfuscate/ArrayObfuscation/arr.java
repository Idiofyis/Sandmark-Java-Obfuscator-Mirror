package sandmark.obfuscate.ArrayObfuscation;

/** This is a helper class which has methods that call Kelly's Stack Simulator to
  * simulate the stack and get the necessary instruction handles
  */
public class arr {

   private static boolean DEBUG = false;

   public static boolean chekifarrayinreturn(org.apache.bcel.generic.InstructionHandle ih1,
    					   int lvi,sandmark.analysis.stacksimulator.StackSimulator ss)
    {
	sandmark.analysis.stacksimulator.Context c = ss.getInstructionContext(ih1);
	int size=c.getStackSize();

	for(int i=0;i<size;i++)
	{
		sandmark.analysis.stacksimulator.StackData []sd = c.getStackAt(i);
		org.apache.bcel.generic.InstructionHandle ih = sd[0].getInstruction();
  		
		if(DEBUG)
		System.out.println("At stack posn " + i +" " +ih.getInstruction().toString());

		org.apache.bcel.generic.Instruction in=ih.getInstruction();
		if(in instanceof org.apache.bcel.generic.ALOAD
			&& ((org.apache.bcel.generic.ALOAD)in).getIndex()==lvi)
		{
			if(DEBUG)
			System.out.println(" Returning False ");
			return false;
		}

	}
	if(DEBUG)
		System.out.println(" Returning True ");
	return true;
    }
    public static boolean chekifarrayinstaticmtd(org.apache.bcel.generic.InstructionHandle ih1,
    					   int lvi,int num_args,sandmark.analysis.stacksimulator.StackSimulator ss)
    {
	sandmark.analysis.stacksimulator.Context c = ss.getInstructionContext(ih1);
	int size=c.getStackSize();
	
	if(DEBUG)
	System.out.println("No of parameters for the method : "+size+" but number of args ="+num_args);
	
	for(int i=0;i<num_args;i++)
	{
		sandmark.analysis.stacksimulator.StackData []sd = c.getStackAt(i);
		org.apache.bcel.generic.InstructionHandle ih = sd[0].getInstruction();
  		
		if(DEBUG)
		System.out.println("At stack posn " + i +" " +ih.getInstruction().toString());
		
		org.apache.bcel.generic.Instruction in=ih.getInstruction();
		if(in instanceof org.apache.bcel.generic.ALOAD
			&& ((org.apache.bcel.generic.ALOAD)in).getIndex()==lvi)
		{
			if(DEBUG)
			System.out.println(" Returning False ");
			return false;
		}

	}
	
	if(DEBUG)
	System.out.println(" Returning True ");

	return true;
    }

    public static boolean chekifarrayinobjectsmtd(org.apache.bcel.generic.InstructionHandle ih1,
    					   int lvi,int num_args,sandmark.analysis.stacksimulator.StackSimulator ss)
    						/*org.apache.bcel.generic.MethodGen mg,
						org.apache.bcel.generic.ConstantPoolGen cpg)*/
    {

	sandmark.analysis.stacksimulator.Context c = ss.getInstructionContext(ih1);
	int size=c.getStackSize();

	for(int i=0;i<num_args;i++)
	{
		sandmark.analysis.stacksimulator.StackData []sd = c.getStackAt(i);
		org.apache.bcel.generic.InstructionHandle ih = sd[0].getInstruction();
  		if(DEBUG)
		System.out.println("At stack posn " + i +" " +ih.getInstruction().toString());
		org.apache.bcel.generic.Instruction in=ih.getInstruction();
		if(in instanceof org.apache.bcel.generic.ALOAD
			&& ((org.apache.bcel.generic.ALOAD)in).getIndex()==lvi)
		{
			
			if(DEBUG)
			System.out.println(" Returning False ");

			return false;
		}

	}

	if(DEBUG)
	System.out.println(" Returning True ");
	
	return true;
    }



    public static org.apache.bcel.generic.InstructionHandle  getNameofArray(org.apache.bcel.generic.InstructionHandle ih2,	
									    sandmark.program.Method mg)
									
    {
	mg.mark();
	sandmark.analysis.stacksimulator.StackSimulator ss = mg.getStack();
	sandmark.analysis.stacksimulator.Context c = ss.getInstructionContext(ih2);
	if(c==null)
	{
	    try{
		mg.getApplication().save("Ashtest_obf.jar");	    
		//mg.getApplication().finalize();
	    }catch(Exception e)
		{
		    e.printStackTrace();
		}
	}
	if(DEBUG)
	{
		System.out.println(" Instruction Handle : (DEBUG) "+ ih2.toString());
		System.out.println(" *********** CONTEXT ::::::::::::: (DEBUG)");
		System.out.println(c.toString());
	}
	sandmark.analysis.stacksimulator.StackData []sd = c.getStackAt(2);
	
	if(DEBUG)
	{
		for(int i=0;i<sd.length;i++)
		{
		System.out.println("*********** STACKDATA *********** (DEBUG)");
		System.out.println(sd[i].toString());
		}
	}

	org.apache.bcel.generic.InstructionHandle ih = sd[0].getInstruction();

	if(DEBUG)
	System.out.println(ih.getInstruction().toString());

	return ih;
    }
    public static org.apache.bcel.generic.InstructionHandle  getIndexInstructions(org.apache.bcel.generic.InstructionHandle ih2,
										  sandmark.program.Method mg)

    {
	mg.mark();
	sandmark.analysis.stacksimulator.StackSimulator ss =mg.getStack();
	sandmark.analysis.stacksimulator.Context c = ss.getInstructionContext(ih2);
	if(DEBUG)
	{	
		System.out.println(" Instruction Handle : (DEBUG) "+ ih2.toString());
		System.out.println(" *********** CONTEXT ::::::::::::: (DEBUG)");
		System.out.println(c.toString());
	}

	sandmark.analysis.stacksimulator.StackData []sd = c.getStackAt(1);

	if(DEBUG)
	{
		for(int i=0;i<sd.length;i++)
		{
		System.out.println("*********** STACKDATA *********** (DEBUG)");
		System.out.println(sd[i].toString());
		}
	}

	org.apache.bcel.generic.InstructionHandle ih = sd[0].getInstruction();
       	
	if(DEBUG)
	System.out.println(ih.getInstruction().toString());

	return ih;
    }

    public static org.apache.bcel.generic.InstructionHandle[]  getValueInstructions(org.apache.bcel.generic.InstructionHandle ih1,
										    org.apache.bcel.generic.InstructionHandle ih2,
										    sandmark.program.Method mg)
    {
	mg.mark();
	sandmark.analysis.stacksimulator.StackSimulator ss = mg.getStack();

	sandmark.analysis.stacksimulator.Context c = ss.getInstructionContext(ih2);
	sandmark.analysis.stacksimulator.StackData []sd1 = c.getStackAt(0);

	sandmark.analysis.stacksimulator.StackData []sd2 = c.getStackAt(1);
	org.apache.bcel.generic.InstructionHandle []ih =
	    new org.apache.bcel.generic.InstructionHandle[2];
	ih[1]= sd1[0].getInstruction();
	ih[0] = sd2[0].getInstruction();
	if(DEBUG)
	{
		System.out.println(" VALUE ::::::::::" + ih[1].getInstruction().toString());
       		System.out.println(ih[0].getInstruction().toString());
		System.out.println(ih[1].getNext().getInstruction().toString());
	}
	return ih;
    }

}



