package sandmark.optimise;


public class Optimise  {

    public static void optimizeApplication(sandmark.program.Application app) 
        throws ClassNotFoundException {

        try {
            Class bloatMain = Class.forName("EDU.purdue.cs.bloat.optimize.Main");
            java.lang.reflect.Method bloatMainMain = bloatMain.getDeclaredMethod
                ("main",new Class[] { new String[0].getClass() });
            
            java.io.File tempClassDir = new sandmark.util.TempDir("smkOpt");
            java.util.Hashtable classNameToClassFile = new java.util.Hashtable();
            
            for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
                sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
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
            
                classNameToClassFile.put(clazz.getName(),classFile);
            }

            sandmark.program.Class classes[] = app.getClasses();
            for(int i = 0 ; i < classes.length ; i++)
                classes[i].delete();
            
            String args[] = new String[classNameToClassFile.keySet().size() + 4];
            String classpath = System.getProperty("java.class.path","");
            String bootclasspath = System.getProperty("sun.boot.class.path","");
            args[0] = "-classpath";
            args[1] = tempClassDir.getAbsolutePath();
            args[1] += (classpath.equals("") ? "" : (java.io.File.pathSeparatorChar + classpath));
            args[1] += (bootclasspath.equals("") ? "" : (java.io.File.pathSeparatorChar + bootclasspath));
            args[2] = "-preserve-debug";
            int i = 3;
            for(java.util.Iterator classNameIt = classNameToClassFile.keySet().iterator() ; 
                classNameIt.hasNext() ; i++) {
                String className = (String)classNameIt.next();
                args[i] = className;
            }
            args[args.length - 1] = tempClassDir.getAbsolutePath();
            
            bloatMainMain.invoke(null,new Object[] {args});
            
            for(java.util.Iterator classFileIt = classNameToClassFile.values().iterator() ;
                classFileIt.hasNext() ; ) {
                String classFileAbsolutePath = ((java.io.File)classFileIt.next()).getAbsolutePath();
                org.apache.bcel.classfile.JavaClass jc = 
                    new org.apache.bcel.classfile.ClassParser(classFileAbsolutePath).parse();
                new sandmark.program.LocalClass(app,jc);
            }
        } catch(NoSuchMethodException e) {
            throw new ClassNotFoundException();
        } catch(java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
	    sandmark.util.Log.message(0,"Please install BLOAT and try again");
        } catch(IllegalAccessException e) {
            throw new ClassNotFoundException();
        } catch(java.io.IOException e) {
	    throw new ClassNotFoundException();
	}
    }

    private static sandmark.util.ConfigProperties sConfigProps;
    public static sandmark.util.ConfigProperties getProperties(){
        if(sConfigProps == null) {
            String[][] props = {
                
                {"Output File",
                 "",
                 "The jar-file that has been optimized.",
                 null,"J",
                },
            };
            sConfigProps = new sandmark.util.ConfigProperties
                (props,sandmark.Console.getConfigProperties());
        }
        return sConfigProps;
    }

    /**
     *  Get the HTML codes of the About page for Optimise
        @return html code for the about page
     */
    public static java.lang.String getAboutHTML(){
        return
            "<HTML><BODY>" +
            "<CENTER><B>List of Optimisers</B></CENTER>" +
            "</BODY></HTML>";
    }

    /**
     *  Get the URL of the Help page for Optimise
        @return url of the help page
     */
    public static java.lang.String getHelpURL(){
        return "sandmark/optimise/doc/help.html";
    }

    /*
     *  Describe what optimize is.
     */
    public static java.lang.String getOverview(){
	return "Optimize a jar file using the BLOAT optimizer. ";
    }
} // class Optimise




