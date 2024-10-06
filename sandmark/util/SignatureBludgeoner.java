package sandmark.util;


/**
 * Modifies a method so that it takes an Object[] as an argument and returns
 * Object. To use the class use the "apply" method defined by its super class 
 * (@see sandmark.util.MethodSignatureChanger)
 *
 * @author Srinivas Visvanathn
 *
 */

public class SignatureBludgeoner extends sandmark.util.MethodSignatureChanger
{
    org.apache.bcel.generic.Type oldAT[];
    org.apache.bcel.generic.Type oldRType;
    int oldIndices[];

    final org.apache.bcel.generic.Type newAT[] = 
		{ new org.apache.bcel.generic.ArrayType("java.lang.Object",1) };
    final org.apache.bcel.generic.Type newRType = 
		new org.apache.bcel.generic.ObjectType("java.lang.Object");
    
    public static void main(String args[])
    {
	try {
	    SignatureBludgeoner p = new SignatureBludgeoner();
	    
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
	//ensure its not main or <init> or <clinit>
	if (meth.getName().equals("main") || meth.getName().equals("<init>") 
		|| meth.getName().equals("<clinit>"))
	    return false;
	
	//ensure args are all of reference type
	oldAT = meth.getArgumentTypes();
	if (oldAT.length < 1)
	    return false; //no args
	for (int jj = 0; jj < oldAT.length; jj++)
	    if ( !(oldAT[jj] instanceof org.apache.bcel.generic.ReferenceType) )
		return false;
	//ensure return type is a reference type or void
	oldRType = meth.getReturnType();
	if (oldRType instanceof org.apache.bcel.generic.BasicType &&
		oldRType.getType() != org.apache.bcel.Constants.T_VOID)
	    return false;

	//ensure method with new signature doesn't already exist
	String newSig = org.apache.bcel.generic.Type.getMethodSignature(newRType,newAT);
	if (meth.getEnclosingClass().getMethod(meth.getName(),newSig) != null)
	    return false;
	
	oldIndices = computeIndices(oldAT,meth.isStatic());    
	
	return true;
    }


    protected void fixMethodSignature(sandmark.program.Method meth)
    {
	meth.setArgumentTypes(newAT);
	meth.setReturnType(newRType);
    }
    
    protected void fixMethodCode(sandmark.program.Method meth)
    {
	org.apache.bcel.generic.InstructionList il = meth.getInstructionList();
	if (il == null) return;
	org.apache.bcel.generic.ConstantPoolGen cpg = meth.getCPG();
	org.apache.bcel.generic.InstructionFactory iF =
	    new org.apache.bcel.generic.InstructionFactory(cpg);

	unwrapArgs(meth,il,cpg,iF);
	//if method used to return void, now should return null
	if (oldRType.getType() == org.apache.bcel.Constants.T_VOID)
	    fixReturns(meth,il,iF);
    }

    private void unwrapArgs(sandmark.program.Method meth,
			org.apache.bcel.generic.InstructionList il,
			org.apache.bcel.generic.ConstantPoolGen cpg,
			org.apache.bcel.generic.InstructionFactory iF)
    {
	il.insert(org.apache.bcel.generic.InstructionConstants.POP);
	for (int jj = oldAT.length - 1; jj >= 0; jj--)
	{
	    il.insert(iF.createStore(oldAT[jj],oldIndices[jj]));
	    il.insert(iF.createCheckCast((org.apache.bcel.generic.ReferenceType)oldAT[jj]));
	    il.insert(org.apache.bcel.generic.InstructionConstants.AALOAD);
	    il.insert(iF.createConstant(new Integer(jj)));
	    il.insert(org.apache.bcel.generic.InstructionConstants.DUP);
	}
	int idx = meth.isStatic() ? 0 : 1;
	il.insert(iF.createLoad(newAT[0],idx));
    }

    private void fixReturns(sandmark.program.Method meth,
			    org.apache.bcel.generic.InstructionList il,
			    org.apache.bcel.generic.InstructionFactory iF)
    {
	org.apache.bcel.generic.InstructionHandle ih;

	//replace returns with an areturn
	for (ih = il.getStart(); ih != null; ih = ih.getNext())
	{
	    org.apache.bcel.generic.Instruction in = ih.getInstruction();

	    //looking for returns
	    if (!(in instanceof org.apache.bcel.generic.RETURN))
		continue;

	    //add instruction to push null and areturn it
	    org.apache.bcel.generic.InstructionHandle newh, endh;
	    newh = il.insert(ih,iF.createNull(newRType));
	    ih.setInstruction(org.apache.bcel.generic.InstructionConstants.ARETURN);
	    il.redirectBranches(ih,newh);
	}
    }


    protected org.apache.bcel.generic.InstructionHandle
	fixInvoke(  org.apache.bcel.generic.InstructionHandle ih,
		    org.apache.bcel.generic.InstructionList il,
		    org.apache.bcel.generic.InstructionFactory iF,
		    org.apache.bcel.generic.ConstantPoolGen cpg,
		    sandmark.program.Method meth
		 )
    {
	int ml = meth.getMaxLocals();

	//create the Object array
	org.apache.bcel.generic.InstructionHandle targeth;
	targeth = il.insert(ih,iF.createConstant(new Integer(oldAT.length)));
	il.insert(ih,iF.createNewArray(newRType,(short)1));
	
	//add instructions to store each of the args in the array
	for (int jj = oldAT.length - 1; jj >= 0; jj--)
	{
	    il.insert(ih,org.apache.bcel.generic.InstructionConstants.DUP_X1);
	    il.insert(ih,iF.createConstant(new Integer(jj)));
	    il.insert(ih,org.apache.bcel.generic.InstructionConstants.DUP2_X1);
	    il.insert(ih,org.apache.bcel.generic.InstructionConstants.POP);
	    il.insert(ih,org.apache.bcel.generic.InstructionConstants.POP);
	    il.insert(ih,org.apache.bcel.generic.InstructionConstants.AASTORE);
	}
			
	//fix the invoke
	org.apache.bcel.generic.InvokeInstruction inv =
	    (org.apache.bcel.generic.InvokeInstruction)ih.getInstruction();
	int index;
	if (inv instanceof org.apache.bcel.generic.INVOKEINTERFACE)
	    index = cpg.addInterfaceMethodref(inv.getClassName(cpg),
		    inv.getMethodName(cpg),
		    org.apache.bcel.generic.Type.getMethodSignature(newRType,newAT));
	else
	    index = cpg.addMethodref(inv.getClassName(cpg),
		    inv.getMethodName(cpg),
		    org.apache.bcel.generic.Type.getMethodSignature(newRType,newAT));
	inv.setIndex(index);
	
	//redirect branches to the start of the wrapup sequence
	il.redirectBranches(ih,targeth);
	
	//if return type was VOID, add pop, else cast result back to actual
	//return type
	org.apache.bcel.generic.InstructionHandle endh;
	if (oldRType.getType() == org.apache.bcel.Constants.T_VOID) {
	    endh = il.append(ih,org.apache.bcel.generic.InstructionConstants.POP);
	} else {
	    endh = il.append(ih,iF.createCheckCast((org.apache.bcel.generic.ReferenceType)oldRType));
	}		

	return endh;
    }
}
