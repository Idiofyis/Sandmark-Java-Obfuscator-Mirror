package sandmark.util;

/**
 *  A TempDir object represents a temporary directory in the filesystem.
 */
public class TempDir extends java.io.File {

    /**
     *  Constructs a TempDir object and a corresponding directory in the
     *  temporary area specified by the java.io.tmpdir system property.
     *  The directory name is formed by appending random digits to the
     *  supplied prefix.
     */
    public TempDir(String prefix) throws java.io.IOException {
        super(System.getProperty("java.io.tmpdir", "/tmp"),
              prefix + Integer.toString((int)(100000 * java.lang.Math.random())));
        if(!mkdirs())
            throw new java.io.IOException("cannot create " + getPath());
    }
    
    /**
     *  Removes the directory and its contents.
     */
    protected void finalize() throws Throwable {
        delete();
        super.finalize();
    }
    
    /**
     *  Removes the directory and its contents.
     */
    public boolean delete() {
        java.util.ArrayList files = 
            new java.util.ArrayList();
        files.add(this);

        for(int i = 0 ; i < files.size() ; i++) {
            java.io.File f = (java.io.File)files.get(i);
            java.io.File children[] = f.listFiles();
            if(children != null && children.length != 0)
                files.addAll(java.util.Arrays.asList(children));
        }

        for(int i = files.size() - 1 ; i >= 0 ; i--)
            ((java.io.File)files.get(i)).delete();

        return super.delete();
    }
} // class TempDir

