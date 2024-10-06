package sandmark.program;



/**
 * Represents a method within a class or interface found on the CLASSPATH.
 * Such methods are immutable;
 * modification attempts produce a java.lang.UnsupportedOperationException.
 *
 * @see sandmark.program.Method
 */

public class LibraryMethod extends sandmark.program.Method {



   /**
    * Constructs a LibraryMethod from a BCEL Method and adds it to a class.
    */
   /*package*/ LibraryMethod(sandmark.program.Class parent,
      org.apache.bcel.classfile.Method method) {
      super(parent, method);
      setImmutable();
   }



}

