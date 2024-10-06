package sandmark.watermark.ct.trace;

/**
 *  This class represents the location of an annotation 
 * point in the code.
 *  <P>
 *  All fields are public, but should be treated as read-only.
 */

public class TracePoint implements java.io.Serializable {

/**
 * The argument (if any) found in a Annotate.mark(Arg)
 * call.
 */
   public String value;

/**
 * The source location of a Annotate.mark(Arg) call.
 */   
   public sandmark.util.ByteCodeLocation location;


/**
 * The call stack at this annotation point.
 */  
    public sandmark.util.StackFrame[] stack;

   public TracePoint(
      String value,
      sandmark.util.ByteCodeLocation location) {
      this.value = value;
      this.location = location;
      this.stack = null;
   }
   
   public TracePoint(
      String value,
      sandmark.util.ByteCodeLocation location,
      sandmark.util.StackFrame[] stack) {
      this.value = value;
      this.location = location;
      this.stack = stack;
   }
   
/**
 * Compare for equality.
 * @param b object to compare to
 */ 
    public boolean equals(Object b) {
        TracePoint a = (TracePoint) b;
	return (value.equals(a.value)) &&
               (location.equals(a.location));
    }

    public int hashCode() {
       return  value.hashCode() + location.hashCode();
    }

/**
 * Format the data in an easy to parse form.
 */  
    public String toString () {
      return "TRACEPT[" + value + "," + location.toString() + "]";
    }
/**
 * Format the data for easy reading.
 */  
    public static String toString (TracePoint[] pts) {
       java.lang.StringBuffer S = new java.lang.StringBuffer(100000);

       for(int i=0; i<pts.length; i++) {
          S.append(pts[i].toString() + "\n");
          for(int j=0; j<pts[i].stack.length; j++)
             S.append("   " + pts[i].stack[j].toString() + "\n");
          S.append('\n');
       }
       return S.toString();
    }


/**
 * Parse a line of tracing data, as formated by 'toString()'.
 */  
    /*
    public static TracePoint parse(String input) {
	java.util.StringTokenizer tok = new java.util.StringTokenizer(input);
        String value = tok.nextToken();
        String remaining = "";
	while(tok.hasMoreTokens())
	    remaining += "\t" + tok.nextToken();
        sandmark.util.ByteCodeLocation location = sandmark.util.ByteCodeLocation.parse(remaining);
        return new TracePoint(value, location);
    }
    */

/**
 * Read an array of trace point data from a file.
 */  
    /*   public static sandmark.watermark.ct.trace.TracePoint[] read (
      String traceFile) throws Exception {
      java.io.BufferedReader in =
          new java.io.BufferedReader(new java.io.FileReader(traceFile));

      java.util.Vector vec = new java.util.Vector(500);
      String line;
      while ((line = in.readLine()) != null) {
         sandmark.watermark.ct.trace.TracePoint d = sandmark.watermark.ct.trace.TracePoint.parse(line);
         vec.add(d);
      }

      sandmark.watermark.ct.trace.TracePoint[] arr =
         (sandmark.watermark.ct.trace.TracePoint[])vec.toArray(new sandmark.watermark.ct.trace.TracePoint[0]);

      return arr;
}
    */
/**
 * Write an array of trace point data to file.
 */  
    /*   public static void write(
       String traceFileName, 
       TracePoint[] tracePoints) throws Exception {
       java.io.PrintWriter writer = new java.io.PrintWriter(
          new java.io.FileWriter(traceFileName));
       for(int i=0; i<tracePoints.length; i++)
         writer.println(tracePoints[i].toString());
       writer.close();
    }
    */

public static void write(
   java.io.File traceFile, 
   TracePoint[] tracePoints) throws Exception {
       java.io.FileOutputStream fos =  new java.io.FileOutputStream(traceFile);
       java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
       oos.writeObject(tracePoints);
       oos.flush();
       fos.close();
}

public static TracePoint[] read(
   java.io.File traceFile) throws Exception { 
    java.io.FileInputStream fis =  new java.io.FileInputStream(traceFile);
    java.io.ObjectInputStream oos = new java.io.ObjectInputStream(fis);
    TracePoint[] tracePoints= (TracePoint[])oos.readObject();
    fis.close();
    return tracePoints;
}

} // class TracePoint

