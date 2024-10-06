package sandmark.obfuscate.methodmadness;



public class PrimitivePromoter extends sandmark.obfuscate.MethodObfuscator
{
    public void apply(sandmark.program.Method meth) throws Exception
    {
        //promote locals
        sandmark.util.primitivepromotion.LocalPromoter.iPromote(meth);
        sandmark.util.primitivepromotion.LocalPromoter.fPromote(meth);
        sandmark.util.primitivepromotion.LocalPromoter.dPromote(meth);
        sandmark.util.primitivepromotion.LocalPromoter.lPromote(meth);

        //promote params
        new sandmark.util.primitivepromotion.ParamPromoter().apply(meth);

        //promote return type
        new sandmark.util.primitivepromotion.ReturnPromoter().apply(meth);
    }

    public java.lang.String getAlgURL()
    {
        return "sandmark/obfuscate/methodmadness/doc/helpprimitivepromoter.html";
    }

    public java.lang.String getAlgHTML()
    {
        return
            "<HTML><BODY>" +
            "PrimitivePromoter is an obfuscator that makes all primitives into their respective wrapper objects.\n" +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href =\"mailto:stepp\">Martin Stepp</a> and " +
            "<a href = \"mailto:kheffner@cs.arizona.edu\">Kelly Heffner</a>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }

    public java.lang.String getLongName()
    {
        return "Primitive Promoter; Makes all primitives in a method's body into " +
               "their respective wrapper objects.";
    }

    public java.lang.String getShortName()
    {
        return "Promote Primitive Types";
    }

    public java.lang.String getAuthor()
    {
        return "Kelly Heffner and Martin Stepp";
    }

    public java.lang.String getAuthorEmail()
    {
        return "kheffner@cs.arizona.edu";
    }

    public java.lang.String getDescription()
    {
        return "Changes all primitives every method into instances of " +
            "the respective wrapper classes.";
    }

    public sandmark.config.ModificationProperty [] getMutations()
    {
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
            sandmark.config.ModificationProperty.I_CHANGE_METHOD_SIGNATURES
        };
    }

    public sandmark.config.RequisiteProperty[] getPostprohibited(){
        return new sandmark.config.RequisiteProperty[]{
            new sandmark.config.AlgorithmProperty(this)
        };
    }
}
