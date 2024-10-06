package sandmark.optimise.bloat;

public class BLOAT extends sandmark.optimise.AppOptimizer {
    public void apply(sandmark.program.Application app) throws Exception {
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

    public String getShortName() {
        return "BLOAT";
    }
    public String getLongName() {
        return "Run the BLOAT optimizer from Purdue";
    }
    public String getAlgHTML() {
        return
            "<HTML><BODY>" +
            "BLOAT Optimizer\n" +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <A HREF =\"mailto:ash@cs.arizona.edu\">Andrew Huntwork</A>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }
    public String getAlgURL() {
        return "sandmark/optimise/bloat/doc/help.html";
    }

    public String getAuthor(){
        return "Andrew Huntwork";
    }

    public String getAuthorEmail(){
        return "ash@cs.arizona.edu";
    }

    public String getDescription(){
        return "Run the BLOAT optimizer from Purdue.";
    }
    public String[] getReferences() {
        return new String[] {};
    }
    public sandmark.config.ModificationProperty[] getMutations() {
        return new sandmark.config.ModificationProperty[]{
            sandmark.config.ModificationProperty.I_ADD_METHOD_CODE,
            sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE,
            sandmark.config.ModificationProperty.I_PUBLICIZE_FIELDS,
            sandmark.config.ModificationProperty.I_PUBLICIZE_METHODS,
        };
    }
}

