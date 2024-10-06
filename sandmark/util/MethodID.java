package sandmark.util;

/**
 *  This class represents a method.
 *  <P>
 *  All fields are public, but should be treated as read-only.
 */

public class MethodID implements java.io.Serializable {

/**
 * The name of the method that called Annotate.mark().
 */
    private String name;

/**
 * The signature of the method that called Annotate.mark().
 */
   private String signature;

/**
 * The name of the class the calling method was in.
 * This is the fully qualified class name, such as
 * <code>java.lang.Object</code>.
 */
   private String className;

   public MethodID(
      String name,
      String signature,
      String className) {
      this.name = name;
      this.signature = signature;
      this.className = className;
   }

    /**
       Convienence constructor, creates a MethodID from a BCEL
       <a href="http://bcel.sourceforge.net/doc/de/fub/bytecode/generic/MethodGen.html">
       MethodGen</a> object.
       @param method a MethodGen to build a MethodID from
    */
    public MethodID(org.apache.bcel.generic.MethodGen method)
    {
        this.name = method.getName();
        this.signature = method.getSignature();
        this.className = method.getClassName();
    }

    /**
       Convienence constructor, creates a MethodID from a BCEL
       <a href="http://bcel.sourceforge.net/doc/de/fub/bytecode/generic/MethodGen.html">
       MethodGen</a> object.
       @param method a MethodGen to build a MethodID from
    */
    public MethodID(sandmark.program.Method method)
    {
        this.name = method.getName();
        this.signature = method.getSignature();
        this.className = method.getClassName();
    }

/**
 * Compare for equality.
 */
    public boolean equals(Object b) {
        MethodID a = (MethodID) b;
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
 * Format the data in an easy to parse form.
 */
    public String toString () {
      return "METHOD[" + className + "." + name + "," + signature + "," + className + "]";
    }

/**
 * Format the data in a compact form.
 */
    public String toStringShortFormat () {
      return name + "," + signature + "," + className;
    }

/**
 * Format the data in a format suitable for dot.
 */
    public String toStringDotFormat () {
      return "{{" + name + "|" + signature + "}|{" + className + "}}";
    }

/**
 * Format the data in a format suitable for dot.
 */
    public String toStringShortDotFormat() {
      return "{"  +
                "{" + name + "|" + signature + "}" +
                "|" + className +
             "}";
    }

/**
 * Parse a line of tracing data, as formated by 'toString()'.
 */
    /*   public static MethodID parse(String input) {
        java.util.StringTokenizer tok = new java.util.StringTokenizer(input);
        String name = tok.nextToken();
        String signature = tok.nextToken();
        String className = tok.nextToken();
        return new MethodID(name, signature, className);
    }
    */

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
} // class MethodID



