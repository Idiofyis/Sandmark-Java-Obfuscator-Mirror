package sandmark.util;





/**
 * Reorders the parameters of a method. To use the class use the 
 * "apply" method defined by its super class (@see 
 * sandmark.util.MethodSignatureChanger)
 *
 * @author Srinivas Visvanathn
 *
 */

public class ParamReorder extends sandmark.util.MethodSignatureChanger
{
    private org.apache.bcel.generic.Type oldAT[];   //old param types
    private org.apache.bcel.generic.Type newAT[];   //new param types

    int oldIndices[];	//old local var indices of args
    int newIndices[];	//new local var indices of args

    //mapping from old to new/new to old of local var indices
    java.util.Map old2new;
    java.util.Map new2old;
    
    public static void main(String args[])
    {
	try {
	    ParamReorder p = new ParamReorder();
	    
	    sandmark.program.Application app = new sandmark.program.Application(args[0]);
	    java.util.Iterator cit = app.classes();
	    while (cit.hasNext()) {
		sandmark.program.Class cls = (sandmark.program.Class)cit.next();
		java.util.Iterator mit = cls.methods();
		while (mit.hasNext()) {
		    sandmark.program.Method meth = (sandmark.program.Method)mit.next();
		    p.apply(meth);
		}
	    }
	    app.save("out.jar");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    protected boolean customInit(sandmark.program.Method meth)
    {
	oldAT = meth.getArgumentTypes();
	if (oldAT.length < 2)	//can't reorder unless we have at least 2 args
	    return false;

	//shuffle the args and compute info needed for reordering
	computeReordering(meth);

	//ensure method with new signature doesn't already exist
	String newSig = org.apache.bcel.generic.Type.getMethodSignature(
			meth.getReturnType(),newAT);
	if (meth.getEnclosingClass().getMethod(meth.getName(),newSig) != null)
	    return false;

	return true;
    }
    

    //pop args off and push them in the new order. also fix invoke to refer to
    //the updated method
    protected org.apache.bcel.generic.InstructionHandle
		 fixInvoke( org.apache.bcel.generic.InstructionHandle ih,
			    org.apache.bcel.generic.InstructionList il,
			    org.apache.bcel.generic.InstructionFactory iF,
			    org.apache.bcel.generic.ConstantPoolGen cpg,
			    sandmark.program.Method meth
			  )
    {
	//if method is not static and all values in new/oldIndices will be
	//larger by 1 for the "this" ref
	int ml = meth.getMaxLocals();
	org.apache.bcel.generic.InstructionHandle prevh = ih.getPrev();
	
	//pop all args off into local array beyond max locals
	for (int jj = oldAT.length - 1; jj >= 0; jj--)
	    il.insert(ih,iF.createStore(oldAT[jj],ml + oldIndices[jj]));

	//push args in new order
	for (int jj = 0; jj < newAT.length; jj++) {
	    Integer idx = (Integer)new2old.get(new Integer(newIndices[jj]));
	    il.insert(ih,iF.createLoad(newAT[jj],ml + idx.intValue()));
	}

	//fix invoke instruction
	org.apache.bcel.generic.InvokeInstruction inv = 
	    (org.apache.bcel.generic.InvokeInstruction)ih.getInstruction();
	int index;
	if (inv instanceof org.apache.bcel.generic.INVOKEINTERFACE)
	    index = cpg.addInterfaceMethodref(inv.getClassName(cpg),
		    inv.getMethodName(cpg),
		    org.apache.bcel.generic.Type.getMethodSignature(
			inv.getReturnType(cpg),newAT));
	else
	    index = cpg.addMethodref(inv.getClassName(cpg),
		    inv.getMethodName(cpg),
		    org.apache.bcel.generic.Type.getMethodSignature(
			inv.getReturnType(cpg),newAT));
	inv.setIndex(index);

	//redirect branches to the start of the wrapup sequence
	il.redirectBranches(ih,(prevh == null) ? il.getStart() : prevh.getNext());

	return ih;
    }
   
    protected void fixMethodSignature(sandmark.program.Method meth)
    {
	meth.setArgumentTypes(newAT);
    }

    
    //restore args into the old order in the local var array
    protected void fixMethodCode(sandmark.program.Method meth)
    {
	org.apache.bcel.generic.InstructionList il = meth.getInstructionList();
	if (il == null) return;
	org.apache.bcel.generic.InstructionFactory iF =
	    new org.apache.bcel.generic.InstructionFactory(meth.getCPG());

	for (int jj = 0; jj < oldAT.length; jj++)
	    il.insert(iF.createStore(oldAT[jj],oldIndices[jj]));

	for (int jj = oldAT.length - 1; jj >= 0; jj--) {
	    Integer idx = (Integer)old2new.get(new Integer(oldIndices[jj]));
	    il.insert(iF.createLoad(oldAT[jj],idx.intValue()));
	}
    }
    

    //compute reordering info that will be used by other methods
    private void computeReordering(sandmark.program.Method meth)
    {
	newAT = new org.apache.bcel.generic.Type[oldAT.length];
	System.arraycopy(oldAT,0,newAT,0,oldAT.length);

	oldIndices = computeIndices(oldAT,meth.isStatic());
	newIndices = new int[oldIndices.length];
	for (int jj = 0; jj < newIndices.length; jj++)
	    newIndices[jj] = oldIndices[jj];

	new2old = new java.util.HashMap();
	for (int jj = 0; jj < oldIndices.length; jj++)
	    new2old.put(new Integer(oldIndices[jj]),new Integer(oldIndices[jj]));

	java.util.Random rg = new java.util.Random(System.currentTimeMillis());

	//loop to randomly reorder the args and maintain reordering info
	for (int jj = 0; jj < oldAT.length * oldAT.length; jj++)
	{
	    //will swap arg i and arg (i + 1)
	    int i = rg.nextInt(oldAT.length - 1);

	    //swap types of args i and (i + 1)
	    org.apache.bcel.generic.Type tmpT = newAT[i + 1];
	    newAT[i + 1] = newAT[i];
	    newAT[i] = tmpT;
	    
	    //change indices of args i and (i + 1); only index of arg (i + 1)
	    //actually changes
	    Integer old1 = (Integer)new2old.remove(new Integer(newIndices[i]));
	    Integer old2 = (Integer)new2old.remove(new Integer(newIndices[i + 1]));
	    newIndices[i + 1] = newIndices[i] + tmpT.getSize();

	    //update mapping between old and new indices
	    new2old.put(new Integer(newIndices[i]),old2);
	    new2old.put(new Integer(newIndices[i + 1]),old1);
	}

	//compute old2new based on new2old
	old2new = new java.util.HashMap();
	java.util.Iterator it = new2old.keySet().iterator();
	while (it.hasNext()) {
	    Integer k = (Integer)it.next();
	    old2new.put(new2old.get(k),k);
	}
    }
}
