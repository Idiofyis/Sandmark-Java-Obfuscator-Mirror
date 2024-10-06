package sandmark.util;


/**
 * Given a method M in class C, MethodReceiver can be used to find all classes in
 * C's hierarchy on which M can be invoked i.e. all classes in C's hierarchy 
 * which can receive message M.
 *
 * @author Srinivas Visvanathan
 * 
*/

public class MethodReceiver
{

    /**
     * Given method mg, in some class cg, this routine finds all classes in cg's
     * hierarchy on which mg can be invoked. The set of classes returned all
     * declare/define mg or have a superclass/interface that declars/defins mg.
     * It uses ClassHierarchy to look for the classes and hence is also limited
     * to what ClassHierarchy offers. If mg is a library method, the routine 
     * returns null
     */
    public static java.util.Set findMethodReceivers(sandmark.program.Method mg)
    {
	sandmark.program.Class cg = mg.getEnclosingClass();
	sandmark.program.Application app = mg.getApplication();
	sandmark.analysis.classhierarchy.ClassHierarchy ch =
	    app.getHierarchy();

	/* if mg overrides some library method, then return null, since there are
	 * classes outside this app which can receive the method call too and we
	 * can't find those classes */
	try {
	    if (ch.overridesLibraryMethod(new sandmark.util.MethodID(mg)))
		return null;
	} catch (sandmark.analysis.classhierarchy.ClassHierarchyException e) {
	    throw new RuntimeException(e.toString());
	}

	java.util.Set bases = findBaseClasses(cg,ch);

	java.util.Set tmp = findReceiverClasses(bases,mg,ch);
	return tmp;
    }


    //returns a set containing the non-library base classes of cg. The method mg
    //will be defined/declared by one of these classes or their descendents.
    //This routine recursively goes up the inheritance tree from cg until it
    //reaches classes/interfaces whose superclasses/interfaces are library
    //classes (e.g. java.lang.Object)
    private static java.util.Set findBaseClasses(sandmark.program.Class cg,
	    sandmark.analysis.classhierarchy.ClassHierarchy ch)
    {
	java.util.Set result = new java.util.HashSet();
	boolean base = true;	//indicates whether cg is a base class

	//scan thru the parents of cg
	java.util.Iterator it = ch.preds(cg);
	while (it.hasNext()) {
	    sandmark.program.Class par = (sandmark.program.Class)it.next();

	    //if par is not a library super class i.e. par is some super class in
	    //app, then cg is not a base class. Instead search thru par's
	    //ancestors
	    if (!ch.isLibraryClass(par)) {
		base = false;
		result.addAll(findBaseClasses(par,ch));
	    }
	}

	//if cg has no pred or all its pred are java.* classes, then its a base
	//class
	if (base)
	    result.add(cg);

	return result;
    }


    //Given the set of base classes (bases), find the highest descendents that
    //declare/define mg. These classes and all their subclasses can receive the
    //message mg
    private static java.util.Set findReceiverClasses(java.util.Set bases,
			sandmark.program.Method mg,
			sandmark.analysis.classhierarchy.ClassHierarchy ch)
    {
	java.util.HashSet result = new java.util.HashSet();
	String mName = mg.getName();
	String mSig = mg.getSignature();

	//for each base class cg
	java.util.Iterator it = bases.iterator();
	while (it.hasNext())
	{
	    sandmark.program.Class cg = (sandmark.program.Class)it.next();
	    
	    //if cg declares/defines mg
	    if (cg.getMethod(mName,mSig) != null)
	    {
		//cg and all its subclasses are potential receivers
		sandmark.program.Class[] subc = ch.subClasses(cg);
		for (int jj = 0; jj < subc.length; jj++)
		    result.add(subc[jj]);
	    }
	    else
	    {
		//cg does not receive mg, but check its descendents
		java.util.Set succ = new java.util.HashSet();
		java.util.Iterator sIter = ch.succs(cg);
		while (sIter.hasNext())
		    succ.add((sandmark.program.Class)sIter.next());	
		result.addAll(findReceiverClasses(succ,mg,ch));
	    }
	}

	return result;
    }
}
