package sandmark.program;



/**
 * Represents a complete Java program.  An Application object is the
 * root of a tree encompassing Classes and all their subcomponents,
 * plus other jar file entries such as images or sound clips.
 * The jar file manifest is part of the application itself and is
 * not a node in the tree.
 *
 * <P> For more background, see the {@link sandmark.program} package overview.
 *
 * <P>Here is a simple example that reads a jar file and prints the classes:
 * <PRE>
 *    sandmark.program.Application app =
 *       new sandmark.program.Application("program.jar");
 *    java.util.Iterator it = a.classes();
 *    while (it.hasNext()) {
 *       sandmark.program.Class cls = (sandmark.program.Class) it.next();
 *       System.out.println("class " + cls.getName());
 *    }
 * </PRE>
 *
 * <P>This example renames the main class and writes a new jar file:
 * <PRE>
 *    sandmark.program.Application app =
 *       new sandmark.program.Application("old.jar");
 *    sandmark.program.Class c = app.getMain();
 *    c.setName("Start");
 *    app.setMain(c);
 *    app.save("new.jar");
 * </PRE>
 *
 * @author Gregg Townsend and Kelly Heffner
 */

public class Application extends sandmark.program.Object {

   private java.util.jar.Manifest manifest;    // attribute manifest
   private java.io.File mMostRecentPath;
   private java.util.ArrayList mPathChangeListeners;


   /**
    * Creates an empty application.
    */
   public Application() {
      manifest = new java.util.jar.Manifest();
      setName("Application");
      setApplication(this);
      mPathChangeListeners = new java.util.ArrayList();
   }



   /**
    * Creates an application from a given jar file or class file.
    *
    * <P> If a jar file has an index file (META-INF/INDEX.LIST),
    * it is discarded because this code does not know how to
    * maintain and update it.  An index can be recreated later
    * by running <CODE>"jar -i file.jar"</CODE>.
    *
    * <P> If a jar file has a manifest file (META-INF/MANIFEST.MF),
    * it is used to initialize the application's manifest, but
    * it does not appear as a member file within the application.
    *
    * <P> The <CODE>MAIN_CLASS</CODE> attribute is set if
    * specified by the manifest or if exactly one
    * <CODE>public static void main(String[])</CODE> method
    * is found among all the classes loaded.
    *
    * @param filename the name of a jar file or class file
    */
   public Application(String filename) throws java.lang.Exception {
      this(new java.io.File(filename));
   }



   /**
    * Creates an application from a given jar file or class file.
    *
    * <P> If a jar file has an index file (META-INF/INDEX.LIST),
    * it is discarded because this code does not know how to
    * maintain and update it.  An index can be recreated later
    * by running <CODE>"jar -i file.jar"</CODE>.
    *
    * <P> If a jar file has a manifest file (META-INF/MANIFEST.MF),
    * it is used to initialize the application's manifest, but
    * it does not appear as a member file within the application.
    *
    * <P> The <CODE>MAIN_CLASS</CODE> attribute is set if
    * specified by the manifest or if exactly one
    * <CODE>public static void main(String[])</CODE> method
    * is found among all the classes loaded.
    *
    * @param file a jar file or class file
    */
   public Application(java.io.File file) throws java.lang.Exception {

      this();
      if (! file.exists()) {
         throw new java.io.FileNotFoundException(file.toString());
      }
      try {
         loadJarFile(file);
      } catch (java.util.zip.ZipException e) {
         manifest = new java.util.jar.Manifest();
         java.io.InputStream instream = 
            new java.io.BufferedInputStream
            (new java.io.FileInputStream(file));
         new sandmark.program.LocalClass(this, instream, file.getName());
         instream.close();
      }

      String s = file.getName();
      int n = s.indexOf('.');
      if (n > 0) {
         setName(s.substring(0, n));
      } else {
         setName(s);                    // set App name
      }
      if (getMain() == null) {
         findMain();                    // set main class
      }

      //load user configuration settings if they exist
      loadUserConstraints();
   }



   /**
    *  Loads the contents of a jar file into this application.
    */
   private void loadJarFile(java.io.File file) throws java.lang.Exception {

      java.util.jar.JarFile jf = new java.util.jar.JarFile(file);
      manifest = jf.getManifest();
      if (manifest == null) {
         manifest = new java.util.jar.Manifest();
      } else {
         rmsigs(manifest);              // clean up manifest
      }

      for (java.util.Enumeration e = jf.entries(); e.hasMoreElements(); ) {
         java.util.jar.JarEntry je = (java.util.jar.JarEntry) e.nextElement();
         if (! je.isDirectory()) {
            String fname = je.getName();
            java.io.InputStream instream =
               new java.io.BufferedInputStream(jf.getInputStream(je));
            if (fname.endsWith(".class")) {
               new sandmark.program.LocalClass(this, instream, fname);
            } else if (! unwanted(fname)) {
               new sandmark.program.File(this, fname, instream);
            }
            instream.close();
         }
      }

      setPath(file);
   }


   /**
    *  Returns true if the given filename matches a META-INF file
    *  that we want to discard instead of reading in.
    */
   private static boolean unwanted(String name) {
      name = name.toUpperCase();                // make tests case insensitive
      return name.startsWith("META-INF/") && (
         name.equals(java.util.jar.JarFile.MANIFEST_NAME)  // old manifest
         || name.equals("META-INF/INDEX.LIST")  // jar index
         || name.endsWith(".SF")                // class signatures
         || name.endsWith(".DSA")               // manifest signature
         || name.endsWith(".RSA")               // alternate manifest signature
         );
   }



   /**
    *  Cleans the manifest of digital signatures for class files
    *  (which become invalid when SandMark modifies the classes).
    */
   private static void rmsigs(java.util.jar.Manifest manifest) {
      java.util.Iterator it = manifest.getEntries().keySet().iterator();
      while (it.hasNext()) {
         String entry = (String) it.next();
         if (entry.endsWith(".class")) {
            java.util.jar.Attributes att = manifest.getAttributes(entry);
            java.util.Iterator it2 = att.keySet().iterator();
            while (it2.hasNext()) {
               java.util.jar.Attributes.Name key =
                  (java.util.jar.Attributes.Name) it2.next();
               String kname = key.toString();
               if (kname.indexOf("-Digest-") > 0
                     || kname.endsWith("-Digest")
                     || kname.equals("Digest-Algorithms")
                     || kname.equals("Magic")) {
                  it2.remove();
               }
            }
            if (att.size() == 0) {
               it.remove();
            }
         }
      }
   }



   /**
    *  Sets the <CODE>MAIN_CLASS</CODE> attribute if there is
    *  exactly one <CODE>public static void main(String[])</CODE>
    *  method in the loaded classes.
    */
   private void findMain() {
      sandmark.program.Class mainclass = null;
      int nmains = 0;
      for (java.util.Iterator it = classes(); it.hasNext(); ) {
         sandmark.program.Class c = (sandmark.program.Class) it.next();
         sandmark.program.Method m =
            c.getMethod("main", "([Ljava/lang/String;)V");
         if (m != null && m.isMain()) {
            nmains++;
            mainclass = c;
            break;
         }
      }
      if (nmains == 1) {
         setMain(mainclass);
      }
   }



   /**
    * Writes this application to the specified file as a jar file.
    * A manifest file (META-INF/MANIFEST.MF) is included unless it
    * would be empty.  A user configuration file is written.
    *
    * @param filename the name of the file
    */
   public void save(String filename) throws java.io.IOException {
      save(new java.io.File(filename));
   }

   public void save(java.io.File file) throws java.io.IOException {
      save(new java.io.FileOutputStream(file));
      setPath(file);
      saveUserConstraints();
   }

   /**
    * Writes this application to the specified output stream as a jar file.
    * A manifest file (META-INF/MANIFEST.MF) is included unless it
    * would be empty.
    *
    * @param ostream the output stream
    */
   public void save(java.io.OutputStream ostream) throws java.io.IOException {

      java.util.jar.JarOutputStream jstream;
      java.util.jar.Attributes atts = manifest.getMainAttributes();
      if (atts.size() == 0 && manifest.getEntries().size() == 0) {
         // manifest is empty, so don't write it
         jstream = new java.util.jar.JarOutputStream(ostream);
      } else {
         // include manifest in jar file
         if (atts.getValue(java.util.jar.Attributes.Name.MANIFEST_VERSION)
             == null) {
            // must have this or it won't write the Main-Class attribute!
            atts.put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
         }
         jstream = new java.util.jar.JarOutputStream(ostream, manifest);
      }
            
      // BCEL's JavaClass.dump insists on closing its output stream;
      // override close() to avoid trouble with the multi-file jar output.
      java.io.OutputStream bstream = new java.io.BufferedOutputStream(jstream) {
            public void close() throws java.io.IOException {
               flush();
            }
         };

      // sort list of members for a sensibly-ordered output jarfile
      sandmark.program.Object[] members = getMembers();
      java.util.Arrays.sort(members);

      // write the jar file, excluding library objects
      for (int i = 0; i < members.length; i++) {
         sandmark.program.JarElement elem =
            (sandmark.program.JarElement) members[i];
         String fname = elem.getJarName();
         jstream.putNextEntry(new java.util.jar.JarEntry(fname));
         elem.save(bstream);
         bstream.flush();
      }
      jstream.close();

      setPath(null);
   }



   /**
    * Frees resources and renders this application object invalid.
    */
   public void close() {
      mark();                        // flush cache
   }



   /**
    * Calls {@link sandmark.program.Application#close() close}.
    */
   public void finalize() {
      close();
   }



   /**
    * Gets the manifest of this application.
    *
    * @return the program manifest
    */
   public java.util.jar.Manifest getManifest() {
      return manifest;
   }



   /**
    * Gets the <CODE>MAIN_CLASS</CODE> attribute of this application.
    *
    * @return the class containing the main method,
    * or <code>null</code> if no main class is set
    */
   public sandmark.program.Class getMain() {
      java.util.jar.Attributes att = manifest.getMainAttributes();
      if (att == null) {
         return null;
      }
      String classname =
         (String) att.get(java.util.jar.Attributes.Name.MAIN_CLASS);
      if (classname == null) {
         return null;
      } else {
         return getClass(classname);
      }
   }



   /**
    * Sets the <CODE>MAIN_CLASS</CODE> attribute for this application.
    * If the argument is <CODE>null</CODE>, the attribute is cleared.
    * The application's <CODE>mark</CODE> method is called.
    *
    * @param c the class containing the main method for the
    * application
    */
   public void setMain(sandmark.program.Class c) {
      if (c == null) {
         setMain((String) null);
      } else {
         setMain(c.getName());
      }
      mark();
   }



   /**
    * Sets the <CODE>MAIN_CLASS</CODE> attribute for this application.
    * If the argument is <CODE>null</CODE>, the attribute is cleared.
    * The application's <CODE>mark</CODE> method is called.
    *
    * @param classname the name of the class containing the main
    * method for this application
    */
   public void setMain(String classname) {
      java.util.jar.Attributes atts = manifest.getMainAttributes();
      if (classname == null) {
         atts.remove(java.util.jar.Attributes.Name.MAIN_CLASS);
      } else {
         atts.put(java.util.jar.Attributes.Name.MAIN_CLASS, classname);
      }
      mark();
   }

   /**
    * Returns the specified class, which may be in this Application,
    * in one of the jar specified in this Application's Manifest
    * in the Class-Path variable, or in the CLASSPATH
    */
   public sandmark.program.Class findClass(String name) {
      {
         sandmark.program.Class cls = getClass(name);
         if(cls != null)
            return cls;
      }

      if(mMostRecentPath != null && mMostRecentPath.exists()) {
         java.io.File pwd = mMostRecentPath.getParentFile();
         String classFileName = name.replace('.','/') + ".class";
         String manifestCP = manifest.getMainAttributes().getValue
            (java.util.jar.Attributes.Name.CLASS_PATH);
         String paths[] = manifestCP == null ? new String[0] : 
            manifestCP.split("/\\s+/");
         for(int i = 0 ; i < paths.length ; i++) {
            java.io.File cpFile = new java.io.File(pwd,paths[i]);
            if(!cpFile.exists())
               continue;
            if(cpFile.isDirectory()) {
               java.io.File classFile = new java.io.File(cpFile,classFileName);
               if(!classFile.exists())
                  continue;
               try {
                  java.io.FileInputStream fis = 
                     new java.io.FileInputStream(classFile);
                  Class cls = new LibraryClass
                     (new org.apache.bcel.classfile.ClassParser
                      (fis,classFile.toString()).parse());
                  fis.close();
                  return cls;
               } catch(java.io.FileNotFoundException e) {
                  //Doesn't exist, so keep going
               } catch(java.io.IOException e) {
                  //It's screwed up, so keep going
               }
            } else {
               try {
                  java.io.FileInputStream fis = 
                     new java.io.FileInputStream(cpFile);
                  java.util.jar.JarInputStream jis = 
                     new java.util.jar.JarInputStream(fis);
                  java.util.zip.ZipEntry classZE = null;
                  for( ; jis.available() == 1 && 
                          (classZE = jis.getNextEntry()) != null ; classZE = null)
                     if(classZE.getName().equals(classFileName))
                        break;
                  if(classZE != null) {
                     Class cls = new LibraryClass
                        (new org.apache.bcel.classfile.ClassParser
                         (jis,classFileName).parse());
                     jis.close();
                     return cls;
                  }
               } catch(java.io.IOException e) {
                  //It's screwed up, so keep going
               }
            }
         }
      }
      
      return LibraryClass.find(name);
   }



   /**
    * Returns the specified class.
    *
    * @param name the fully qualified name of the desired class
    * (for example <code>java.lang.String</code>
    * @return the Class object for the desired class
    */
   public sandmark.program.Class getClass(String name) {
      sandmark.program.Object o = getMember(name);
      if (o != null && o instanceof sandmark.program.Class) {
         return (sandmark.program.Class) o;
      } else {
         return null;
      }
   }



   /**
    * Returns a list of all the classes in this application.
    *
    * @return an array of Class objects containing each class in the
    * application
    */
   public sandmark.program.Class[] getClasses() {
      return (sandmark.program.Class[])
         sandmark.util.Misc.buildArray(
                                       classes(), new sandmark.program.Class[0]);
   }



   /**
    * Returns an iterator over all the classes in this application.
    *
    * @return an iterator of <code>sandmark.program.Class</code> objects
    * containing each class in this application
    */
   public java.util.Iterator classes() {
      return sandmark.util.Misc.instanceFilter(
                                               members(), sandmark.program.Class.class);
   }



   /**
    * Returns a single file associated with this application.
    *
    * @param name the name of a member of the jarfile
    * @return a file abstraction for the specified component
    */
   public sandmark.program.File getFile(String name) {
      sandmark.program.Object o = getMember(name);
      if (o != null && o instanceof sandmark.program.File) {
         return (sandmark.program.File) o;
      } else {
         return null;
      }
   }



   /**
    * Returns a list of all the files associated with this application,
    * excluding class files and the manifest.
    *
    * @return an array of sandmark.program.File objects
    */
   public sandmark.program.File[] getFiles() {
      return (sandmark.program.File[])
         sandmark.util.Misc.buildArray(
                                       files(), new sandmark.program.File[0]);
   }



   /**
    * Returns an iterator over all the files associated with this application,
    * excluding class files and the manifest.
    *
    * @return an iterator of sandmark.program.File objects
    */
   public java.util.Iterator files() {
      return sandmark.util.Misc.instanceFilter(
                                               members(), sandmark.program.File.class);
   }



   /**
    * Returns the class hierarchy containing all the classes in the
    * application.  The hierarchy is cached so that repeated calls
    * to <code>getHierarchy</code> return the same object as
    * long as this application and its components are not modified.
    *
    * @return the class hierarchy information for this application
    */
   public sandmark.analysis.classhierarchy.ClassHierarchy getHierarchy(){ // (String jarfile) {
      sandmark.analysis.classhierarchy.ClassHierarchy ch =
         (sandmark.analysis.classhierarchy.ClassHierarchy) retrieve("HIERARCHY");
      if (ch == null) {
         ch = new sandmark.analysis.classhierarchy.ClassHierarchy(this);
         cache("HIERARCHY", ch);
      }

      return ch;
   }



   /**
    * Returns the Stats object for this application,
    * constructing one if necessary.
    */
   public sandmark.newstatistics.Stats getStatistics() {
      sandmark.newstatistics.Stats stats =
         (sandmark.newstatistics.Stats) retrieve("STATISTICS");
      if (stats == null) {
         stats = new sandmark.newstatistics.Stats(this);
         cache("STATISTICS", stats);
      }
      return stats;
   }

   public java.io.File getMostRecentPath() {
      return mMostRecentPath;
   }


   public static void main(String args[])
   {
      try{
         sandmark.program.Application app = new sandmark.program.Application(new String("test.jar"));
         sandmark.analysis.classhierarchy.ClassHierarchy ch1 =app.getHierarchy();
         System.out.println(ch1.toString());
      }catch(Exception e)
         {
            e.printStackTrace();
         }
   }

   private void setPath(java.io.File file) {
      boolean shouldNotify =
         (file == null && mMostRecentPath != null) ||
         (file != null && !file.equals(mMostRecentPath));
      mMostRecentPath = file;
      if(shouldNotify)
         for(java.util.Iterator it = mPathChangeListeners.iterator() ;
             it.hasNext() ; )
            ((PathChangeListener)it.next()).pathChanged(file);
   }

   public java.io.File getPath() {
      return mMostRecentPath;
   }

   public void addPathChangeListener(PathChangeListener listener) {
      if(!mPathChangeListeners.contains(listener))
         mPathChangeListeners.add(listener);
   }

   public void removePathChangeListener(PathChangeListener listener) {
      mPathChangeListeners.remove(listener);
   }

   public String getCanonicalName()
   {
      //there is no real name for the jar file from inside the program

      return "";
   }

   /**
      Saves the user configuration settings to disk, if the application exists
      on disk.
      @return true if the application existed on disk and config was written
   */
   public boolean saveUserConstraints() throws java.io.IOException{
      java.io.File configFile = this.getPath();
      if(configFile != null){
         java.io.File parent = configFile.getParentFile();
         configFile = new java.io.File(parent, getName() + ".smconfig");
         java.io.FileOutputStream out = new java.io.FileOutputStream(configFile);
         sandmark.program.UserObjectConstraints.writeUserConstraints(out, this);
         out.close();
         return true;
      }

      return false;
   }

   private boolean loadUserConstraints() throws java.lang.Exception{
      java.io.File configFile = this.getPath();
      if(configFile != null){
         java.io.File parent = configFile.getParentFile();
         configFile = new java.io.File(parent, getName() + ".smconfig");
         if(configFile.exists()){
            sandmark.program.UserObjectConstraints.readUserConstraints
               (new java.io.FileInputStream(configFile), this);
            return true;
         }
      }
      return false;
   }
    
   public void delete() {
      onDelete();
      setImmutable();
   }
}
