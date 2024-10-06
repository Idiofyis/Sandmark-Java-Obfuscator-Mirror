package sandmark.program;



/**
 * Represents a class found on the CLASSPATH.
 * Such classes are created lazily and automatically as needed,
 * and are immutable.
 * Modification attempts produce a java.lang.UnsupportedOperationException.
 *
 * @see sandmark.program.Class
 */
public class LibraryClass extends sandmark.program.Class {

   //  Caches library classes for reuse, since they're immutable.

   private static java.util.Hashtable classtab = new java.util.Hashtable();


   /**
    * Finds a class on $CLASSPATH and returns a LibraryClass object.
    * Such classes are not considered part of the application,
    * and are immutable.
    * Returns null if the class cannot be found or loaded.
    */
   public static sandmark.program.LibraryClass find(String classname) {
      sandmark.program.LibraryClass c =
         (sandmark.program.LibraryClass) classtab.get(classname);
      if (c != null) {
         return c;
      }

      org.apache.bcel.classfile.JavaClass jclass =
         org.apache.bcel.Repository.lookupClass(classname);
      if(jclass == null)
         return null;

      c = new sandmark.program.LibraryClass(jclass);

      for (java.util.Iterator miter=c.methods();miter.hasNext();){
         ((Method)miter.next()).fixLDC_WBug();
      }

      classtab.put(classname, c);
      return c;
   }


   /**
    * Constructs a LibraryClass from a BCEL JavaClass.
    * The new class is not part of any application.
    */
   LibraryClass(org.apache.bcel.classfile.JavaClass jclass) {
      super(null, jclass, null);
      setImmutable();
   }



   /**
    *  Constructs a LibraryCPG for this class.
    */
   /*package*/ sandmark.program.ConstantPoolGen makeCPG
      (sandmark.program.Class c, org.apache.bcel.classfile.ConstantPool g) {
      return new LibraryCPG(c, g);
   }


   /**
    *  Constructs a LibraryField for this class.
    */
   /*package*/ sandmark.program.Field makeField
      (sandmark.program.Class c, org.apache.bcel.classfile.Field f) {
      return new LibraryField(c, f);
   }

   /**
    *  Constructs a LibraryMethod for this class.
    */
   /*package*/ sandmark.program.Method makeMethod
      (sandmark.program.Class c, org.apache.bcel.classfile.Method m) {
      return new LibraryMethod(c, m);
   }
}

