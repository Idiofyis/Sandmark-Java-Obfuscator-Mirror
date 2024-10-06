package sandmark.obfuscate.methodmadness;



public class SignatureBludgeoner extends sandmark.obfuscate.MethodObfuscator
{

    public void apply(sandmark.program.Method meth) throws Exception
    {
        new sandmark.util.SignatureBludgeoner().apply(meth);
    }

    /* Returns the URL at which you can find information about this obfuscator. */
    public java.lang.String getAlgURL()
    {
        return "sandmark/obfuscate/methodmadness/doc/helpsignaturebludgeoner.html";
    }

    /* Returns an HTML description of this obfuscator. */
    public java.lang.String getAlgHTML()
    {
        return
            "<HTML><BODY>" +
            "SignatureBludgeoner is an obfuscator that converts all methods to take Object[] and return Object.\n" +
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
        return "SignatureBludgeoner; Converts all methods to take Object[] and return Object.";
    }

    /* Returns a short description of this obfuscator's name. */
    public java.lang.String getShortName()
    {
        return "Bludgeon Signatures";
    }

    public java.lang.String getDescription()
    {
        return "Converts all static methods to take Object[] and return Object.";
    }

    public java.lang.String getAuthor()
    {
        return "Martin Stepp";
    }

    public java.lang.String getAuthorEmail()
    {
        return "stepp";
    }

    public sandmark.config.ModificationProperty [] getMutations()
    {
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
            sandmark.config.ModificationProperty.I_CHANGE_METHOD_SIGNATURES,
        };
    }
    public sandmark.config.RequisiteProperty[] getPostprohibited(){
        return new sandmark.config.RequisiteProperty[]{
            new sandmark.config.AlgorithmProperty(this)
        };
    }

}
