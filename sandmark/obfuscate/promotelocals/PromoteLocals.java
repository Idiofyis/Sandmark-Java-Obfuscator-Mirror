package sandmark.obfuscate.promotelocals;

/*
** Program: PromoteLocals.java
** Authors: Danny Mandel, Anna Segurson
** Purpose: Implements algorithm 3.8 for CSc 620's Project 2
*/

public class PromoteLocals extends sandmark.obfuscate.MethodObfuscator
{
    /*
    ** Constructor
    */
    public PromoteLocals() {}

    /*
    ** Returns this obfuscator's short name.
    */
    public java.lang.String getShortName() {
	return "Promote Primitive Registers";
    }
    /*
    ** Returns this obfuscator's long name.
    */
    public java.lang.String getLongName() {
	return "Promote all the local variables in a method to objects.";
    }
    public String getAuthor()
    {
	return "Danny Mandel and Anna Segurson";
    }
    public String getAuthorEmail()
    {
	return "dmandel@cs.arizona.edu and segurson@cs.arizona.edu";
    }
    public String getDescription()
    {
	return "Promote all the local variables in a method to objects.";
    }
    public sandmark.config.ModificationProperty [] getMutations()
    {
	return null;
    }
    public sandmark.config.RequisiteProperty[] getPostprohibited()
    {
	return new sandmark.config.RequisiteProperty[] { 
			new sandmark.config.AlgorithmProperty(this)};
    }

    /*
    **  Get the HTML codes of the About page for SplitClass
    */
    public java.lang.String getAlgHTML()
    {
	return	"<HTML><BODY>" +
		"PromoteLocals is a method obfuscator." +
      " The algorithm promotes all the local variables in a method to objects." +
		"<TABLE>" +
		"<TR><TD>" +
		"Authors: <A HREF = \"mailto:dmandel@cs.arizona.edu\">Danny Mandel</A> and " +
		"<A HREF=\"mailto:segurson@cs.arizona.edu\">Anna Segurson</A>\n" +
		"</TR></TD>" +
		"</TABLE>" +
		"</BODY></HTML>";
    }
    
    /*
    **  Get the URL of the Help page for PromoteLocals
    */
    public java.lang.String getAlgURL()
    {
	/*
	** I don't know what this should be -Anna-
	*/
	return "sandmark/obfuscate/promotelocals/doc/help.html";
    }

    public void apply(sandmark.program.Method meth) throws Exception 
    {
	/* now uses primitivepromotion.LocalPromoter which does the same thing */
	sandmark.util.primitivepromotion.LocalPromoter.iPromote(meth);
	sandmark.util.primitivepromotion.LocalPromoter.fPromote(meth);
	sandmark.util.primitivepromotion.LocalPromoter.lPromote(meth);
	sandmark.util.primitivepromotion.LocalPromoter.dPromote(meth);
    }
}
