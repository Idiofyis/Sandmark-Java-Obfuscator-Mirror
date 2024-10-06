package sandmark.obfuscate.methodmadness;


public class MethodMerger extends sandmark.obfuscate.ClassObfuscator
{
    public void apply(sandmark.program.Class cls) throws Exception
    {
        new sandmark.util.MethodMerger().apply(cls);
    }


    public String getAlgHTML()
    {
        return
            "<HTML><BODY>" +
            "Method Merger merges all of the public static methods " +
            "that have the same signature in each class into one " +
            "large master method.\n" +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href =\"mailto:stepp\">Martin Stepp</a> and " +
            "<a href = \"mailto:kheffner@cs.arizona.edu\">Kelly Heffner</a>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }

    public String getAlgURL()
    {
        return "sandmark/obfuscate/methodmadness/doc/helpmethodmerger.html";
    }

    public String getLongName()
    {
        return "Method Merger";
    }

    public String getShortName()
    {
        return "Method Merger";
    }

    public String getDescription()
    {
        return
            "Method Merger merges all of the public static methods " +
            "that have the same signature in each class into one " +
            "large master method.";
    }

    public String getAuthor()
    {
        return "Kelly Heffner";
    }

    public String getAuthorEmail()
    {
        return "kheffner@cs.arizona.edu";
    }

    public sandmark.config.ModificationProperty [] getMutations()
    {
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_ADD_METHODS,
            sandmark.config.ModificationProperty.I_CHANGE_METHOD_NAMES,
            sandmark.config.ModificationProperty.I_CHANGE_METHOD_SIGNATURES,
            sandmark.config.ModificationProperty.I_REMOVE_METHODS
        };
    }

    public sandmark.config.RequisiteProperty [] getPostsuggestions()
    {
        return new sandmark.config.RequisiteProperty[]{
            sandmark.config.ModificationProperty.I_CHANGE_METHOD_NAMES
        };
    }


    public sandmark.config.RequisiteProperty[] getPostprohibited(){
        return new sandmark.config.RequisiteProperty[]{
            new sandmark.config.AlgorithmProperty(this)
        };
    }

}
