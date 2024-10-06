package sandmark.util.primitivepromotion;



/**
 * Promotes the types of the parameters of a method. To use the class use the 
 * "apply" method defined by its super class (@see 
 * sandmark.util.MethodSignatureChanger)
 *
 * @author Srinivas Visvanathn
 *
 */
public class ParamPromoter extends sandmark.util.MethodSignatureChanger
{
    org.apache.bcel.generic.Type oldAT[];
    org.apache.bcel.generic.Type newAT[];

    int oldIndices[];
    int newIndices[];

    int first;	//index of first arg that's promoted

    public static void main(String args[])
    {
	try {
	    ParamPromoter p = new ParamPromoter();
	    
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
	//ensure there are enough args and some of them are of primitive type
	oldAT = meth.getArgumentTypes();
	if (oldAT.length < 1)
	    return false;
	int kk;
	for (kk = 0; kk < oldAT.length; kk++)
	    if (oldAT[kk] instanceof org.apache.bcel.generic.BasicType)
		break;
	if (kk == oldAT.length)
	    return false;

	//compute arg types for promoted method
	//also remember the first arg that's promoted
	newAT = new org.apache.bcel.generic.Type[oldAT.length];
	first = -1;
	for (int jj = 0; jj < oldAT.length; jj++)
	{
	    if (oldAT[jj] instanceof org.apache.bcel.generic.ReferenceType)
		newAT[jj] = oldAT[jj];
	    else
	    {
		newAT[jj] = PromoterUtil.getWrapperType(oldAT[jj]);
		if (first == -1) first = jj;
	    }
	}

	//ensure, method with promoted args doesn't already exist
	String newSig = org.apache.bcel.generic.Type.getMethodSignature(meth.getReturnType(),
				    newAT);
	if (meth.getEnclosingClass().getMethod(meth.getName(),newSig) != null)
	    return false;

	//compute local var indices for old and new params
	oldIndices = computeIndices(oldAT,meth.isStatic());
	newIndices = computeIndices(newAT,meth.isStatic());

	return true;
    }

    protected void fixMethodSignature(sandmark.program.Method meth)
    {
	meth.setArgumentTypes(newAT);
    }
    
    //unpacks and unwraps args to what they orignally were
    protected void fixMethodCode(sandmark.program.Method meth)
    {
	org.apache.bcel.generic.InstructionList il = meth.getInstructionList();
	if (il == null) return;
	org.apache.bcel.generic.InstructionFactory iF =
	    new org.apache.bcel.generic.InstructionFactory(meth.getCPG());

	for (int jj = 0; jj < newAT.length; jj++)
	{
	    //param jj was promoted, unwrap/unpack the param
	    if (oldAT[jj].getType() != newAT[jj].getType())
	    {
		il.insert(iF.createStore(oldAT[jj],oldIndices[jj]));
		il.insert(iF.createInvoke(newAT[jj].toString(),
			PromoterUtil.getValueMethodName(oldAT[jj]),
			oldAT[jj],
			new org.apache.bcel.generic.Type[] {},
			org.apache.bcel.Constants.INVOKEVIRTUAL));
		il.insert(iF.createLoad(newAT[jj],newIndices[jj]));
	    }
	    else
	    //param jj wasn't promoted, but it got packed, so unpack
	    if (oldIndices[jj] != newIndices[jj])
	    {
		il.insert(iF.createStore(oldAT[jj],oldIndices[jj]));
		il.insert(iF.createLoad(newAT[jj],newIndices[jj]));
	    }
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
	//remember the instruction that precedes the invoke
	org.apache.bcel.generic.InstructionHandle prevh = ih.getPrev();

	int ml = meth.getMaxLocals() + 1;
	org.apache.bcel.generic.InstructionHandle tmph = ih;

	//scan backwards thru args upto the first promoted arg
	for (int jj = newAT.length - 1; jj > first; jj--)
	{
	    org.apache.bcel.generic.InstructionHandle h1,h2;

	    //have to store this arg and load it back again
	    h1 = il.insert(tmph,iF.createStore(newAT[jj],ml));
	    h2 = il.insert(tmph,iF.createLoad(newAT[jj],ml));

	    //if this arg is promoted then add code to wrap it before the store
	    if (oldAT[jj].getType() != newAT[jj].getType())
	    {
		il.insert(h1,iF.createNew((org.apache.bcel.generic.ObjectType)newAT[jj]));
		if (oldAT[jj].getSize() == 2) {
		    il.insert(h1,org.apache.bcel.generic.InstructionConstants.DUP_X2);
		    il.insert(h1,org.apache.bcel.generic.InstructionConstants.DUP_X2);
		} else {
		    il.insert(h1,org.apache.bcel.generic.InstructionConstants.DUP_X1);
		    il.insert(h1,org.apache.bcel.generic.InstructionConstants.DUP_X1);
		}
		il.insert(h1,org.apache.bcel.generic.InstructionConstants.POP);
		il.insert(h1,iF.createInvoke(newAT[jj].toString(),"<init>",
			    org.apache.bcel.generic.Type.VOID,
			    new org.apache.bcel.generic.Type[] { oldAT[jj] },
			    org.apache.bcel.Constants.INVOKESPECIAL));
	    }
	    
	    //set tmph so that further instructions get added
	    //in between the previous store-load pair
	    tmph = h2;
	    ml++;
	}

	//add code to wrap the first promoted arg
	il.insert(tmph,iF.createNew((org.apache.bcel.generic.ObjectType)newAT[first]));
	if (oldAT[first].getSize() == 2) {
	    il.insert(tmph,org.apache.bcel.generic.InstructionConstants.DUP_X2);
	    il.insert(tmph,org.apache.bcel.generic.InstructionConstants.DUP_X2);
	} else {
	    il.insert(tmph,org.apache.bcel.generic.InstructionConstants.DUP_X1);
	    il.insert(tmph,org.apache.bcel.generic.InstructionConstants.DUP_X1);
	}
	il.insert(tmph,org.apache.bcel.generic.InstructionConstants.POP);
	il.insert(tmph,iF.createInvoke(newAT[first].toString(),"<init>",
		    org.apache.bcel.generic.Type.VOID,
		    new org.apache.bcel.generic.Type[] { oldAT[first] },
		    org.apache.bcel.Constants.INVOKESPECIAL));

	//make the invoke refer to the promoted method
	org.apache.bcel.generic.InvokeInstruction inv =
	    (org.apache.bcel.generic.InvokeInstruction)ih.getInstruction();
	int index;
	if (inv instanceof org.apache.bcel.generic.INVOKEINTERFACE)
	    index = cpg.addInterfaceMethodref(inv.getClassName(cpg),
		    inv.getMethodName(cpg),
		    org.apache.bcel.generic.Type.getMethodSignature(inv.getReturnType(cpg),newAT));
	else
	    index = cpg.addMethodref(inv.getClassName(cpg),
		    inv.getMethodName(cpg),
		    org.apache.bcel.generic.Type.getMethodSignature(inv.getReturnType(cpg),newAT));
	inv.setIndex(index);

	//redirect branches to the start of the wrapup sequence
	il.redirectBranches(ih,(prevh == null) ? il.getStart() : prevh.getNext());
	
	return ih;
    }
}
