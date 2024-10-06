package sandmark.obfuscate.methodmadness;

/* 
 *  The main running class of the Method2RMadness app obfuscator.
 *
 *  @author Martin Stepp (<a href="mailto:stepp">stepp</a>), 
 *  @author Kelly Heffner (<a href="mailto:kheffner@cs.arizona.edu">kheffner@cs.arizona.edu</a>)
 *  @version 1.0, May 8, 2002
 * 
 *  rewritten using sandmark.program Objects  - tapas@cs.arizona.edu
 *  @version 1.0, May 8, 2003  ( that's a coincidence !!! exactly one year ! )
 */
public class Method2RMadness extends sandmark.obfuscate.AppObfuscator
{
    /* Turn to true to print debugging messages. */
    private static final boolean DEBUG = false;

    /* Turn to true to print status output messages as the obfuscator runs. */
    private static final boolean OUTPUT = false;

    /* Runs a quick test of this obfuscator. */
    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.err.println("Usage: Method2RMadness JAR_FILE [upto_step]");
            System.exit(1);
        }
        sandmark.program.Application app = new sandmark.program.Application(args[0]);
	int upto_step = 7;
	if (args.length == 2) {
	    upto_step = Integer.valueOf(args[1]).intValue();	
	    if (upto_step < 1 || upto_step > 7) {
		System.err.println("upto_step must lie in 1..7");
		System.exit(1);
	    }
	}
	
	//step 1: use static split to create private static versions of methods
	sandmark.util.StaticSplit ssp = new sandmark.util.StaticSplit();
	java.util.Iterator citr = app.classes();
	while (citr.hasNext())
	    ssp.apply((sandmark.program.Class)citr.next());

	if (upto_step == 1) {
	    app.save("out.jar");
	    return;
	}
			
	//apply primitive promotion, param reordering, signature bludgeoning
	//to all the private static methods
	sandmark.util.MethodSignatureChanger msc[] =
		    {	new sandmark.util.primitivepromotion.ParamPromoter(), //3
			new sandmark.util.primitivepromotion.ReturnPromoter(),//4
			new sandmark.util.ParamReorder(),	//5
			new sandmark.util.SignatureBludgeoner() }; //6
	citr = app.classes();
	while (citr.hasNext())
	{
	    java.util.Iterator mitr = ((sandmark.program.Class)citr.next()).methods();
	    while (mitr.hasNext())
	    {
		sandmark.program.Method meth = (sandmark.program.Method)mitr.next();
		if (!meth.isPrivate() || !meth.isStatic())
		    continue;
		//step 2: local promotion
		sandmark.util.primitivepromotion.LocalPromoter.iPromote(meth);
		sandmark.util.primitivepromotion.LocalPromoter.fPromote(meth);
		sandmark.util.primitivepromotion.LocalPromoter.lPromote(meth);
		sandmark.util.primitivepromotion.LocalPromoter.dPromote(meth);

		if (upto_step == 2)
		    continue;

		for (int jj = 0; jj < upto_step - 2 && jj < msc.length; jj++)
		    msc[jj].apply(meth);
	    }
	}
	if (upto_step < 7) {
	    app.save("out.jar");
	    return;
	}

	//step 7: apply method merger to merge the private static methods
	sandmark.util.MethodMerger mm = new sandmark.util.MethodMerger();
	citr = app.classes();
	while (citr.hasNext())
	    mm.apply((sandmark.program.Class)citr.next());

	app.save("out.jar");
    }

    /* Makes public the fields/methods in this Application object's classes. */
    public void apply(sandmark.program.Application app) throws Exception
    {
	//use static split to create private static versions of methods
	sandmark.util.StaticSplit ssp = new sandmark.util.StaticSplit();
	java.util.Iterator citr = app.classes();
	while (citr.hasNext())
	    ssp.apply((sandmark.program.Class)citr.next());

			
	//apply primitive promotion, param reordering, signature bludgeoning
	//to all the private static methods
	sandmark.util.MethodSignatureChanger msc[] =
		    {	new sandmark.util.primitivepromotion.ParamPromoter(),
			new sandmark.util.primitivepromotion.ReturnPromoter(),
			new sandmark.util.ParamReorder(),
			new sandmark.util.SignatureBludgeoner() };
	citr = app.classes();
	while (citr.hasNext())
	{
	    java.util.Iterator mitr = ((sandmark.program.Class)citr.next()).methods();
	    while (mitr.hasNext())
	    {
		sandmark.program.Method meth = (sandmark.program.Method)mitr.next();
		if (!meth.isPrivate() || !meth.isStatic())
		    continue;
		sandmark.util.primitivepromotion.LocalPromoter.iPromote(meth);
		sandmark.util.primitivepromotion.LocalPromoter.fPromote(meth);
		sandmark.util.primitivepromotion.LocalPromoter.lPromote(meth);
		sandmark.util.primitivepromotion.LocalPromoter.dPromote(meth);

		for (int jj = 0; jj < msc.length; jj++)
		    msc[jj].apply(meth);
	    }
	}

	//apply method merger to merge the private static methods
	sandmark.util.MethodMerger mm = new sandmark.util.MethodMerger();
	citr = app.classes();
	while (citr.hasNext())
	    mm.apply((sandmark.program.Class)citr.next());
    }
    /*{
        // create a class hierarchy for later seeing if methods are renamable

        // 1. make all fields/methods public
        if(OUTPUT) System.out.println("Step 1) Publicizer; making fields/methods all public...");
        (new sandmark.obfuscate.methodmadness.Publicizer()).apply(app);
		app.save("TEMP1.jar");
        System.gc();
        System.runFinalization();

        // 2. replace nonstatic methods with static methods
        if(OUTPUT) System.out.println("Step 2) StaticSplit; moving dynamic method bodies into static methods...");
		//app = new sandmark.program.Application("TEMP1.jar");
        java.util.Iterator itr = app.classes();
        sandmark.obfuscate.methodmadness.StaticSplit split =
            new sandmark.obfuscate.methodmadness.StaticSplit();
        while(itr.hasNext())
            split.apply((sandmark.program.Class)itr.next());
        split = null;
		app.save("TEMP2.jar");
        System.gc();
        System.runFinalization();


        // 3. promote all primitives into wrapper objects
        if(OUTPUT) System.out.println("Step 3) PrimitivePromoter; wrapping all primitives into object wrappers...");
		//app = new sandmark.program.Application("TEMP2.jar");
        (new sandmark.obfuscate.methodmadness.PrimitivePromoter()).apply(app);
		app.save("TEMP3.jar");
        System.gc();
        System.runFinalization();


        // 4. reorder arguments of static methods
        if(OUTPUT) System.out.println("Step 4) ParameterReorder; shuffling method argument orders...");
		//app = new sandmark.program.Application("TEMP3.jar");
        (new sandmark.obfuscate.methodmadness.ParameterReorderer()).apply(app);
		app.save("TEMP4.jar");
        System.gc();
        System.runFinalization();


        // 5. convert arguments into Object[]
        if(OUTPUT) System.out.println("Step 5) SignatureBludgeoner; making all static methods take Object[]...");
		//app = new sandmark.program.Application("TEMP4.jar");
        (new sandmark.obfuscate.methodmadness.SignatureBludgeoner()).apply(app);
		app.save("TEMP5.jar");
        System.gc();
        System.runFinalization();



        // 6. merge static methods into master method
        if(OUTPUT) System.out.println("Step 6) MethodMerger; combining each class's static methods...");
		//app = new sandmark.program.Application("TEMP5.jar");
        (new sandmark.obfuscate.methodmadness.MethodMerger()).apply(app);
		app.save("TEMP6.jar");
        System.gc();
        System.runFinalization();

    }*/

    /*  Returns the URL at which you can find information about this obfuscator. */
    public java.lang.String getAlgURL()
    {
        return "sandmark/obfuscate/methodmadness/doc/help.html";
    }

    /*  Returns an HTML description of this obfuscator. */
    public java.lang.String getAlgHTML()
    {
        return 
        "<HTML><BODY>" +
        "Mthod2RMadness hides information hidden in methods by disrupting their " + 
        "signatures, argument orders, and moving/combining them.\n" +
        "<TABLE>" +
        "<TR><TD>" +
        "Author: <a href =\"mailto:stepp\">Martin Stepp</a> and " +
        "<a href = \"mailto:kheffner@cs.arizona.edu\">Kelly Heffner</a>\n" +
        "</TR></TD>" +
        "</TABLE>" +
        "</BODY></HTML>";
    }

    /* Returns a long description of this obfuscator's name. */
    public java.lang.String getLongName()
    {
        return "Method2RMadness.  Hides information hidden in methods by "
        + "disrupting their signatures, arg. orders, and moving/merging them.";
    }

    /* Returns a short description of this obfuscator's name. */
    public java.lang.String getShortName()
    {
        return "Method Madness";
    }

    public java.lang.String getAuthor()
    {
        return "Martin Stepp and Kelly Heffner";
    }

    public java.lang.String getAuthorEmail()
    {
        return "stepp and kheffner@cs.arizona.edu";
    }

    public java.lang.String getDescription()
    {
        return "Hides information hidden in methods by " +
               "disrupting their signatures, arg. orders, and merging them.";
    }

    public sandmark.config.ModificationProperty[] getMutations()
    {
        //we can do this since the properties are all singletons
        java.util.HashSet retVal = new java.util.HashSet();

        sandmark.config.ModificationProperty [] temp; 
        /*sandmark.config.ModificationProperty [] temp =
            new sandmark.obfuscate.methodmadness.Publicizer().getMutations();
        for(int i = 0; i < temp.length; i++)
            retVal.add(temp[i]);*/

        temp = new sandmark.obfuscate.methodmadness.StaticSplit().getMutations();
        for(int i = 0; i < temp.length; i++)
            retVal.add(temp[i]);

        temp = new sandmark.obfuscate.methodmadness.PrimitivePromoter().getMutations();
        for(int i = 0; i < temp.length; i++)
            retVal.add(temp[i]);

        temp = new sandmark.obfuscate.methodmadness.ParamReorder().getMutations();
        for(int i = 0; i < temp.length; i++)
            retVal.add(temp[i]);

        temp = new sandmark.obfuscate.methodmadness.SignatureBludgeoner().getMutations();
        for(int i = 0; i < temp.length; i++)
            retVal.add(temp[i]);

        temp = new sandmark.obfuscate.methodmadness.MethodMerger().getMutations();
        for(int i = 0; i < temp.length; i++)
            retVal.add(temp[i]);

        return (sandmark.config.ModificationProperty[])
            retVal.toArray(new sandmark.config.ModificationProperty[]{});
    }

}

