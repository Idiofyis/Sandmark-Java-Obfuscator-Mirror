package sandmark.obfuscate.localvar;

/**
        VariableReassigner optimizes a method by using
        sandmark.analysis.controlflowgraph.RegisterAllocator to reallocate
        the local variable usage.
        @author Kelly Heffner (kheffner@cs.arizona.edu)
*/
public class VariableReassigner extends sandmark.optimise.MethodOptimizer
{
    public static final boolean DEBUG = false;


   public void apply(sandmark.program.Method meth) throws java.io.IOException {
        
        if(meth == null)
            throw new IllegalArgumentException("No such method " +
                                               " to optimize.");

        if(meth.isInterface() || meth.isAbstract())
            return;

        if(DEBUG)System.out.println("Contructing interference graph");
        sandmark.analysis.interference.InterferenceGraph interGraph = meth.getIFG();
        if(DEBUG)System.out.println("Register Allocating");
        new sandmark.analysis.controlflowgraph.RegisterAllocator(interGraph).allocate(true);
	meth.removeLocalVariables();
    }

    public String getAlgHTML()
    {
        return
            "<HTML><BODY>" +
            "VariableReassigner is a method optimizer that rearranges the local " +
            "variable table usage. Local variables that used to share a slot " +
            "in the table may not anymore, and new variables may share space.\n" +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href = \"mailto:kheffner@cs.arizona.edu\">Kelly Heffner</a>\n" +
            "</TR></TD>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }

    public String getAlgURL()
    {
        return "sandmark/obfuscate/localvar/doc/help.html";
    }
    public String getAuthor()
    {
        return "Kelly Heffner";
    }
    public String getAuthorEmail()
    {
        return "kheffner@cs.arizona.edu";
    }

    public String getDescription()
    {
        return  "VariableReassigner is a method optimizer that rearranges the local " +
                        "variable table usage. Local variables that used to share a slot " +
                        "in the table may not anymore, and new variables may share space.";
    }

    public String getLongName()
    {
        return "Local variable reassigner";
    }

    public sandmark.config.ModificationProperty[] getMutations()
    {
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_MODIFY_METHOD_CODE,
            sandmark.config.ModificationProperty.I_CHANGE_LOCAL_VARIABLES
        };
    }

    public String getShortName()
    {
        return "Variable Reassigner";
    }

    public static void main(String [] args) throws java.lang.Exception
    {
        if(args.length != 1) {
            System.out.println("Usage: java sandmark.obfuscate.localvar.VariableReassigner <jarfile>");
            System.exit(0);
        }


        sandmark.program.Application app = new
           sandmark.program.Application(args[0]);

        sandmark.obfuscate.localvar.VariableReassigner splitter =
            new sandmark.obfuscate.localvar.VariableReassigner();

        java.util.Iterator classIter = app.classes();
        while(classIter.hasNext()){
            sandmark.program.Class cls = (sandmark.program.Class)classIter.next();
            sandmark.program.Method[] methods = cls.getMethods();
            for(int i = 0; i < methods.length; i++){
                sandmark.program.Method meth = methods[i];
                splitter.apply(meth);
            }
        }
        app.save("VAR_REASSIGN.jar");
    }
}

