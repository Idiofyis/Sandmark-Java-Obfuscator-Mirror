package sandmark.util;


/**
 * MethodMerger can be applied on a class to merge all the private static
 * methods with the same signature into a common method. The common method
 * will have an extra integer argument to select which block of code from the
 * source methods should be executed.
 *
 * @author Srinivas Visvanathan 
 *
 */

public class MethodMerger
{
    private java.util.Random rand;

    public MethodMerger()
    {
	rand = new java.util.Random(System.currentTimeMillis());
    }

    
    public void apply(sandmark.program.Class cls)
    {
	//collect merge info
	java.util.Map info = collectMergeInfo(cls);

	//MethodID of old method -> 
	//  (Method object of merged method, int value to select the old code)
	java.util.Map old2new = new java.util.HashMap();

	//do merge
	java.util.Set ks = info.keySet();
	java.util.Iterator it = ks.iterator();
	while (it.hasNext()) {
	    String sig = (String)it.next();
	    java.util.List list = (java.util.List)info.get(sig);
	    if (list.size() > 1)    //no point merging one method
		merge(cls,sig,list,old2new);
	}

	//fix invokations to the old methods
	fixInvokes(cls,old2new);
    }


    private void fixInvokes(sandmark.program.Class cls, java.util.Map old2new)
    {
	//scan through all the methods
	java.util.Iterator it = cls.methods();
	while (it.hasNext())
	{
	    sandmark.program.Method meth = (sandmark.program.Method)it.next();
	    
	    org.apache.bcel.generic.InstructionList il = meth.getInstructionList();
	    if (il == null) continue;
	    org.apache.bcel.generic.InstructionHandle ih = il.getStart();
	    org.apache.bcel.generic.ConstantPoolGen cpg = meth.getCPG();
	    org.apache.bcel.generic.InstructionFactory iF =
		new org.apache.bcel.generic.InstructionFactory(cpg);

	    //scan thru instructions of the method
	    for (; ih != null; ih = ih.getNext())
	    {
		org.apache.bcel.generic.Instruction in = ih.getInstruction();

		//only looking for invoke static
		if (!(in instanceof org.apache.bcel.generic.INVOKESTATIC))
		    continue;
		org.apache.bcel.generic.INVOKESTATIC inv = 
		    (org.apache.bcel.generic.INVOKESTATIC)in;

		//check if the method being invoked was merged
		sandmark.util.MethodID mID = 
		    new sandmark.util.MethodID(inv.getName(cpg),
			    inv.getSignature(cpg),inv.getClassName(cpg));
		Object[] val = (Object[])old2new.get(mID);
		if (val == null) continue;
		
		//was merged, add a load and fix the invoke
		sandmark.program.Method dest = (sandmark.program.Method)val[0];
		il.insert(ih,iF.createConstant((Integer)val[1]));
		int index = cpg.addMethodref(inv.getClassName(cpg),
			dest.getName(),
			org.apache.bcel.generic.Type.getMethodSignature(
			    dest.getReturnType(),dest.getArgumentTypes()));
		inv.setIndex(index);

		//redirect branches to the iload
		il.redirectBranches(ih,ih.getPrev());
	    }
	}

    }


    private void merge(sandmark.program.Class cls, String sig,
	    java.util.List mlist, java.util.Map old2new)
    {
	//make a new empty method
	sandmark.program.Method dest = createEmptyMethod(sig,cls);

	//compute the local var index of the last int variable
	org.apache.bcel.generic.Type AT[] = dest.getArgumentTypes();
	int idx = 0;
	for (int jj = 0; jj < AT.length - 1; idx += AT[jj].getSize(), jj++);
	
	//collect instruction list of src methods into new method
	org.apache.bcel.generic.InstructionFactory iF =
	    new org.apache.bcel.generic.InstructionFactory(dest.getCPG());
	org.apache.bcel.generic.InstructionList mil = dest.getInstructionList();
	org.apache.bcel.generic.InstructionHandle endh = mil.getEnd();
	
	java.util.Iterator it = mlist.iterator();
	for (int ctr = 0; it.hasNext(); ctr++)
	{
	    sandmark.program.Method src = (sandmark.program.Method)it.next();
	    
	    //add src's instruction list to dest, before the NOP
	    org.apache.bcel.generic.InstructionHandle targeth = 
		mil.insert(endh,src.getInstructionList());

	    //copy src's exceptions to dest
	    org.apache.bcel.generic.CodeExceptionGen exh[] =
		src.getExceptionHandlers();
	    for (int jj = 0; jj < exh.length; jj++)
		dest.addExceptionHandler(exh[jj].getStartPC(),
		   exh[jj].getEndPC(),exh[jj].getHandlerPC(),exh[jj].getCatchType());
	    
	    //keep mapping from name & sig of src to merged method dest
	    sandmark.util.MethodID mID = new sandmark.util.MethodID(src);
	    old2new.put(mID,new Object[] {dest, new Integer(ctr)});
	    
	    //delete the src method
	    cls.removeMethod(src);
	    
	    //if src is the first method to be merged, then we're done
	    if (ctr == 0) continue;

	    //from second method onwards, include instructions to branch to the
	    //appropriate code block depending on the value of the first arg
	    mil.insert(iF.createBranchInstruction(
			org.apache.bcel.Constants.IF_ICMPEQ,targeth));
	    mil.insert(iF.createConstant(new Integer(ctr)));
	    mil.insert(iF.createLoad(org.apache.bcel.generic.Type.INT,idx));
	}
    }

    
    //create an empty method into which all methods with signature sig will be
    //merged
    private sandmark.program.Method 
	createEmptyMethod(String sig, sandmark.program.Class cls)
    {
	//compute argument info of new method
	org.apache.bcel.generic.Type oldAT[] =
	    org.apache.bcel.generic.Type.getArgumentTypes(sig);
	org.apache.bcel.generic.Type rType =
	    org.apache.bcel.generic.Type.getReturnType(sig);
	
	org.apache.bcel.generic.Type newAT[] = 
	    new org.apache.bcel.generic.Type[oldAT.length + 1];
	for (int jj = 0; jj < oldAT.length; jj++)
	    newAT[jj] = oldAT[jj];
	newAT[oldAT.length] = org.apache.bcel.generic.Type.INT;
	
	String argNames[] = new String[oldAT.length + 1];
	for (int jj = 0; jj < argNames.length; jj++)
	    argNames[jj] = new String("arg" + jj);

	//generate name for the new method
	String mName;
	String mSig = org.apache.bcel.generic.Type.getMethodSignature(rType,newAT);
	do
	{
	    int nameSuffix = rand.nextInt();
	    if(nameSuffix < 0)
		continue;
	    mName = "M" + String.valueOf(nameSuffix);
	    sandmark.program.Method tmpMeth = cls.containsMethod(mName,mSig);
	    if (tmpMeth == null) break;
	}
	while (true);
	
	//instruction list with just a NOP; since LocalMethod constructor chokes
	//on a completely empty list
	org.apache.bcel.generic.InstructionList il =
	    new org.apache.bcel.generic.InstructionList(org.apache.bcel.generic.InstructionConstants.NOP);

	return new sandmark.program.LocalMethod(cls,
		org.apache.bcel.Constants.ACC_STATIC |
		org.apache.bcel.Constants.ACC_PRIVATE,
		rType,newAT,argNames,mName,il);
    }


    private java.util.Map collectMergeInfo(sandmark.program.Class cls)
    {
	//to keep a mapping: method sig -> list of methods with that sig
	java.util.Map info = new java.util.HashMap();

	//scan thru methods
	java.util.Iterator it = cls.methods();
	while (it.hasNext())
	{
	    sandmark.program.Method meth = (sandmark.program.Method)it.next();

	    //only interested in private, static methods
	    if (!meth.isStatic() || !meth.isPrivate() || meth.isAbstract() 
		    || meth.isNative() || meth.getName().equals("<clinit>"))
		continue;
	    
	    //store info about method, indexed by its signature
	    String sig = meth.getSignature();
	    java.util.List list = (java.util.List)info.get(sig);
	    if (list != null)
		list.add(meth);
	    else
	    {
		list = new java.util.LinkedList();
		list.add(meth);
		info.put(sig,list);
	    }
	}

	return info;
    }
}
