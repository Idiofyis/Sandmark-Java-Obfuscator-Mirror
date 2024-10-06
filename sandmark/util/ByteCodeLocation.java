package sandmark.util;

/**
 * This class represents the location of a bytecode instruction
 * in the code.
 *  <P>
 *  All fields are public, but should be treated as read-only.
 */

public class ByteCodeLocation  implements java.io.Serializable {
   public static final long MISSING_long = -1;

/**
 * The method in which the bytecode instruction resides.
 */  
   private sandmark.util.MethodID method = null;

/**
 * The line-number of the bytecode instruction. (May be ==-1 if there
 * was no line-number information present.)
 */  
   private long lineNumber = MISSING_long;


/**
 * The bytecode offset where the call was made.
 */  
   private long codeIndex = MISSING_long;

   public ByteCodeLocation(
      sandmark.util.MethodID method,
      long lineNumber,
      long codeIndex) {
      this.method = method;
      this.lineNumber = lineNumber;
      this.codeIndex  = codeIndex;
   }
   
   public sandmark.util.MethodID getMethod() { return method; }
   public long getCodeIndex() { return codeIndex; }
   public long getLineNumber() { return lineNumber; }
   
/**
 * Compare for equality.
 */ 
    public boolean equals(Object b) {
        ByteCodeLocation a = (ByteCodeLocation) b;
	return (method.equals(a.method)) &&
	       (codeIndex==a.codeIndex) &&
	       (lineNumber==a.lineNumber);
    }

    public int hashCode() {
       return  method.hashCode()+
           (int)(codeIndex+lineNumber);
    }

/**
 * Format the data in an easy to parse form.
 */  
    public String toString () {
      return "LOCATION[" + method.toString() + ", LINE=" + lineNumber + ", BC=" + codeIndex + "]";
    }

/**
 * Format the data in a compact form.
 */  
    public String toStringShortFormat () {
      return method.toStringShortFormat() + ",LINE=" + lineNumber + ",BC=" + codeIndex;
    }

/**
 * Format the data in a format suitable for dot.
 */  
    public String toStringDotFormat () {
      return "{" + method.toStringDotFormat() + "|{LINE=" + lineNumber + "|BC=" + codeIndex + "}}";
    }

/**
 * Format the data in a format suitable for dot.
 */  
    public String toStringShortDotFormat () {
      return "{" + 
               method.toStringShortDotFormat() + 
               "|LN=" + lineNumber + 
               "}";
    }

/**
 * Parse a line of tracing data, as formated by 'toString()'.
 */  
    /*    public static ByteCodeLocation parse(String input) {
	java.util.StringTokenizer tok = new java.util.StringTokenizer(input);
        String name = tok.nextToken();
        String callerSignature = tok.nextToken();
        String sourceName = tok.nextToken();
        int lineNumber = java.lang.Integer.parseInt(tok.nextToken());
        int codeIndex = java.lang.Integer.parseInt(tok.nextToken());
        sandmark.util.MethodID method = new sandmark.util.MethodID(name, callerSignature, sourceName);
        return new ByteCodeLocation(method, lineNumber, codeIndex);
    }
    */
} // class ByteCodeLocation

