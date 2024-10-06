package sandmark.util;

/**
 *  This class represents a method.
 *  <P>
 *  All fields are public, but should be treated as read-only.
 */

public class FieldID implements java.io.Serializable {

/**
 * The name of the method that called Annotate.mark().
 */
    protected String name;

/**
 * The signature of the method that called Annotate.mark().
 */
   protected String signature;

/**
 * The name of the class the calling method was in.
 * This is the fully qualified class name, such as
 * <code>java.lang.Object</code>.
 */
   protected String className;

   public FieldID(
      String name,
      String signature,
      String className) {
      this.name = name;
      this.signature = signature;
      this.className = className;
   }

    /**
       Convienence constructor, creates a FieldID from a Field object.
    */
    public FieldID(sandmark.program.Field field)
    {
        this.name = field.getName();
        this.signature = field.getSignature();
        this.className = field.getEnclosingClass().getName();
    }

/**
 * Compare for equality.
 */
    public boolean equals(Object b) {
        FieldID a = (FieldID) b;
        return (name.equals(a.name)) &&
               (signature.equals(a.signature)) &&
               (className.equals(a.className));
    }

    public int hashCode() {
       return  name.hashCode()+
           signature.hashCode()+
           className.hashCode();
    }

/**
 * Return the name of this method.
 */
   public String getName() {
      return name;
   }

/**
 * Return the signature of this method.
 */
   public String getSignature() {
      return signature;
   }


/**
 * Return the fully qualified class name of this method.
 */
   public String getClassName() {
      return className;
   }

    public String toString () {
	return "FIELD[" + className + "." + name + "," + signature + "," + className + "]";
    }
} // class MethodID



