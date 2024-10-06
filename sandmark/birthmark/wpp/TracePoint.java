package sandmark.birthmark.wpp;

/**
 *  This class represents the location of an annotation 
 * point in the code.
 *  <P>
 *  All fields are public, but should be treated as read-only.
 */

public class TracePoint implements java.io.Serializable {

/**
 * The source location of a Annotate.mark(Arg) call.
 */   
   public sandmark.util.ByteCodeLocation location;


   public TracePoint(
      sandmark.util.ByteCodeLocation location) {
      this.location = location;
   }
   
   
/**
 * Compare for equality.
 * @param b object to compare to
 */ 
    public boolean equals(Object b) {
        TracePoint a = (TracePoint) b;
	return (location.equals(a.location));
    }

    public int hashCode() {
       return  location.hashCode();
    }

/**
 * Format the data in an easy to parse form.
 */  
    public String toString () {
      return "TRACEPT[" + location.toString() + "]";
    }
/**
 * Format the data for easy reading.
 */  
    public static String toString (
      TracePoint[] pts) {
       String S = "";
       for(int i=0; i<pts.length; i++) {
          S += pts[i].toString() + "\n";
       }
       return S;
    }


public static void write(
   String traceFileName, 
   TracePoint[] tracePoints) throws Exception {
       java.io.FileOutputStream fos =  new java.io.FileOutputStream(traceFileName);
       java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
       oos.writeObject(tracePoints);
       oos.flush();
       fos.close();
}

public static TracePoint[] read(
   String traceFileName) throws Exception { 
    java.io.FileInputStream fis =  new java.io.FileInputStream(traceFileName);
    java.io.ObjectInputStream oos = new java.io.ObjectInputStream(fis);
    TracePoint[] tracePoints= (TracePoint[])oos.readObject();
    fis.close();
    return tracePoints;
}

} // class TracePoint

