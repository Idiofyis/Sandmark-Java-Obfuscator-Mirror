package sandmark.util.primitivepromotion;


/**
 * Promotes the return type of a method. To use the class use the "apply" method
 * defined by its super class (@see sandmark.util.MethodSignatureChanger)
 *
 * @author Srinivas Visvanathn
 *
 */
public class ReturnPromoter extends sandmark.util.MethodSignatureChanger
{
    private org.apache.bcel.generic.Type pType;		//primitive type
    private org.apache.bcel.generic.ObjectType wType;	//wrapper type
    private org.apache.bcel.generic.Type argTypes[];	//args of method


    public static void main(String args[])
    {
	try {
	    ReturnPromoter p = new ReturnPromoter();
	    
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
	//ensure return type is a primitive type
	pType = meth.getReturnType();
	if ( !(pType instanceof org.apache.bcel.generic.BasicType) ||
		pType.getType() == org.apache.bcel.Constants.T_VOID)
	    return false;
	wType = (org.apache.bcel.generic.ObjectType)PromoterUtil.getWrapperType(pType);
	argTypes = meth.getArgumentTypes();

	//ensure a method with promoted return type doesn't already exist
	String newSig = org.apache.bcel.generic.Type.getMethodSignature(wType,argTypes);
	if (meth.getEnclosingClass().getMethod(meth.getName(),newSig) != null)
	    return false;

	return true;
    }

    protected void fixMethodSignature(sandmark.program.Method meth)
    {
	//change return type
	meth.setReturnType(wType);
    }
    
    protected void fixMethodCode(sandmark.program.Method meth)
    {
	org.apache.bcel.generic.InstructionList il = meth.getInstructionList();
	if (il == null) return;
	org.apache.bcel.generic.InstructionHandle ih = il.getStart();
	org.apache.bcel.generic.InstructionFactory iF =
	    new org.apache.bcel.generic.InstructionFactory(meth.getCPG());

	//scan thru instructions looking for returns
	for (; ih != null; ih = ih.getNext())
	{
	    org.apache.bcel.generic.Instruction in = ih.getInstruction();
	    if ( !(in instanceof org.apache.bcel.generic.ReturnInstruction) )
		continue;

	    //add code to wrap the primitive value into an object
	    org.apache.bcel.generic.InstructionHandle newh;
	    //NEW java.lang.T
	    newh = il.insert(ih,iF.createNew(wType));
	    //2 * DUP_X1/DUP_X2
	    if (pType.getSize() == 2) {
		il.insert(ih,org.apache.bcel.generic.InstructionConstants.DUP_X2);
		il.insert(ih,org.apache.bcel.generic.InstructionConstants.DUP_X2);
	    } else {
		il.insert(ih,org.apache.bcel.generic.InstructionConstants.DUP_X1);
		il.insert(ih,org.apache.bcel.generic.InstructionConstants.DUP_X1);
	    }
	    //POP
	    il.insert(ih,org.apache.bcel.generic.InstructionConstants.POP);
	    //invokespecial T(t)V
	    il.insert(ih,iF.createInvoke(wType.toString(),"<init>",
			org.apache.bcel.generic.Type.VOID,
			new org.apache.bcel.generic.Type[] { pType },
			org.apache.bcel.Constants.INVOKESPECIAL));
	    //ARETURN
	    ih.setInstruction(iF.createReturn(wType));
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
	//make invoke refer to the promoted method
	org.apache.bcel.generic.InvokeInstruction inv =
	    (org.apache.bcel.generic.InvokeInstruction)ih.getInstruction();
	int index;
	if (inv instanceof org.apache.bcel.generic.INVOKEINTERFACE)
	    index = cpg.addInterfaceMethodref(inv.getClassName(cpg),
		    inv.getMethodName(cpg),
		    org.apache.bcel.generic.Type.getMethodSignature(wType,argTypes));
	else
	    index = cpg.addMethodref(inv.getClassName(cpg),
		    inv.getMethodName(cpg),
		    org.apache.bcel.generic.Type.getMethodSignature(wType,argTypes));
	inv.setIndex(index);

	//unwrap the primitive value, after the invoke
	return il.append(ih,iF.createInvoke(wType.toString(),
	    PromoterUtil.getValueMethodName(pType),pType,
	    new org.apache.bcel.generic.Type[] {},
	    org.apache.bcel.Constants.INVOKEVIRTUAL));
    }
}
