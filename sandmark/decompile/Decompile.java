package sandmark.decompile;


public class Decompile  {

    /**
       Decompiles the specified program.
       @param app the application to decompile
       @param classlist the specific class to decompile or empty string
       if you want to decompile the whole program
       @param classpath the classpath of external classes needed for
       decompilation or empty string
    */
    public static String decompile(sandmark.program.Application app,
                                    String classlist, String classpath) {

    java.io.File tempClassDir = null;
    try {
        tempClassDir = new sandmark.util.TempDir("smkOpt");
    } catch(java.io.IOException e) {
        sandmark.util.Log.message(0,"Couldn't create temporary directory.  " +
                                  "Maybe you are out of space.");
    }

    String cmdLine = "jDecompile " + tempClassDir + " " +
        (classlist.equals("")?"ALLCLASSES":classlist) + " " +
        classpath;


    for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
        sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
        try {
            java.io.File classFile = new java.io.File(tempClassDir,clazz.getJarName());
            classFile.getParentFile().mkdirs();
            classFile.createNewFile();

            java.io.FileOutputStream fos = new java.io.FileOutputStream(classFile);
            byte bytes[] = clazz.getBytes();
            fos.write(bytes);
            fos.flush();
            fos.close();

            if(!classFile.exists() || classFile.length() != bytes.length)
                throw new RuntimeException((classFile.exists() ? "exists" : "does not exist") +
                                           " ; length: " + classFile.length() + " ; should be: " +
                                           bytes.length);
        } catch(java.io.IOException e) {
            throw new RuntimeException("unknown io error " + e);
        }
    }

    sandmark.util.Log.message(0, "Decompiling by executing '" + cmdLine + "'");

    String output = null;

    try {
        output = sandmark.util.Misc.execute(cmdLine,sandmark.util.Misc.RETURN_STDOUT);
    } catch(Exception e) {
        sandmark.util.Log.message(0,"Execution failed");
    }

    return output;
}

private static sandmark.util.ConfigProperties sConfigProps;
public static sandmark.util.ConfigProperties getProperties(){
     if(sConfigProps == null) {
            String[][] props = {
                /*{"DECOMP_JarInput",
                 "",
                 "The jar-file to be decompiled.",
                 null,"F",
                 },*/
                {"Class",
                 "",
                 "The class file to be decompiled.",
                 null,"F",
                },
                {"Classpath","","Any external classes the file depends on", null, "F"},
                /*{"DECOMP_Decompiler",
                  "jDecompile # %",
                  "The decompiler command line. " +
                  "'%' is a placeholder for the (qualified) name of the class file. " +
                  "'#' is a placeholder for the path to the unpacked jarfile.",
                  null,"S",
                 },*/
            };
            sConfigProps = new sandmark.util.ConfigProperties
                (props,sandmark.Console.getConfigProperties());
        }
        return sConfigProps;
    }

    /**
     *  Get the HTML codes of the About page for Decompile
        @return html code for the about page
     */
    public static java.lang.String getAboutHTML(){
        return
            "<HTML><BODY>" +
            "<CENTER><B>List of Decompilers</B></CENTER>" +
            "</BODY></HTML>";
    }

    /**
     *  Get the URL of the Help page for Decompile
        @return url of the help page
     */
    public static java.lang.String getHelpURL(){
        return "sandmark/decompile/doc/help.html";
    }

    /*
     *  Describe what optimize is.
     */
    public static java.lang.String getOverview(){
        return "Decompile a jar file using an external decompiler, " +
               "such as SourceAgain from http://www.ahpah.com or Soot " +
               " from http://www.sable.mcgill.ca/soot/.  Use of this function " +
               " requires a special script to be located in your PATH.  " + 
               "See Help for details.  " +
               "Leave 'class' blank to decompile the entire jar-file. ";
    }
} // class Decompile





