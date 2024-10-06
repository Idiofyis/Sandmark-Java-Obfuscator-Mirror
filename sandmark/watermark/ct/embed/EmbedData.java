package sandmark.watermark.ct.embed;

/**
 *  All fields are public, but should be treated as read-only.
 */

public class EmbedData {
/**
 * The annotation trace point, representing a source location
 * and the mark() value encountered at that location.
 */
    public sandmark.watermark.ct.trace.TracePoint tracePoint;

/**
 * The methods in Watermark.java that should be inserted 
 * at this point.
 */
   public sandmark.util.MethodID[] methods;

/**
 * Whether this trace point is location or value based.
 */
    public static final int LOCATION = 0;
    public static final int VALUE = 1;
    public int kind;

/**
 * Construct an new annotation trace point, representing 
 * a source location and the mark() value encountered at 
 * that location.
 * @param tracePoint  the source code location
 * @param methods     the methods to be inserted at this point
 * @param kind        whether this trace point is location or value based
 */
   public EmbedData(
      sandmark.watermark.ct.trace.TracePoint tracePoint,
      sandmark.util.MethodID[] methods,
      int kind) {
      this.tracePoint = tracePoint;
      this.methods  = methods;
      this.kind  = kind;
   }

/**
 * Format the data in an easy to parse form.
 */  
    public String toString () {
        String K = (kind==LOCATION)?"LOCATION":"VALUE   ";
	String S = K + "\t" + tracePoint.toString() + "\t:";
        for(int i=0; i< methods.length; i++)
           S += "\t" + methods[i];
        return S;
    }

} // class EmbedData

