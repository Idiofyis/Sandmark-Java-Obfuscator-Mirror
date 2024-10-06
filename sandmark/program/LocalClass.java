package sandmark.program;

/**
 * Represents a class found in the program jar file, or synthesized.
 *
 * <P> Modification methods in this class automatically call the
 * {@link sandmark.program.Object#mark() mark} method
 * to register their changes.
 *
 * @see sandmark.program.Class
 */
public class LocalClass extends Class {

   /**
    * Constructs a new LocalClass and adds it to an application.
    * The arguments mimic a BCEL JavaClass constructor.
    */
   public LocalClass(sandmark.program.Application parent,
                     String class_name, String super_class_name, String file_name,
                     int access_flags, String[] interfaces) {

      super(parent, new org.apache.bcel.generic.ClassGen(class_name,
                                                         super_class_name, 
                                                         file_name, 
                                                         access_flags, 
                                                         interfaces).getJavaClass(),null);
   }

   /**
    * Constructs a LocalClass and adds it to an application.
    * The class is read from an InputStream containing a classfile.
    */
   public LocalClass(sandmark.program.Application parent,
                     java.io.InputStream istream, String fname)
      throws java.lang.Exception {

      super(parent,
            new org.apache.bcel.classfile.ClassParser(istream, fname).parse(),null);

      for (java.util.Iterator miter=methods();miter.hasNext();){
         ((Method)miter.next()).fixLDC_WBug();
      }
   }


   /**
    * Constructs a LocalClass from a BCEL JavaClass
    * and adds it to an application.
    */
   public LocalClass(sandmark.program.Application parent,
                     org.apache.bcel.classfile.JavaClass jclass) {

      this(parent, jclass, null);
   }
   
   public LocalClass(sandmark.program.Application parent,
                     org.apache.bcel.classfile.JavaClass jclass,
                     sandmark.program.Class original) {
      super(parent, jclass, original);
   }


   /**
    *  Constructs a LocalCPG for this class.
    */
   /*package*/ sandmark.program.ConstantPoolGen makeCPG
      (sandmark.program.Class c, org.apache.bcel.classfile.ConstantPool g) {
      return new LocalCPG(c, g);
   }


   /**
    *  Constructs a LocalField for this class.
    */
   /*package*/ sandmark.program.Field makeField
      (sandmark.program.Class c, org.apache.bcel.classfile.Field f) {
      return new LocalField(c, f);
   }


   /**
    *  Constructs a LocalMethod for this class.
    */
   /*package*/ sandmark.program.Method makeMethod
      (sandmark.program.Class c, org.apache.bcel.classfile.Method m) {
      return new LocalMethod(c, m);
   }
}

