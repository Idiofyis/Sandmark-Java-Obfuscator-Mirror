package sandmark.watermark.ct.trace;

/**
 *  The <CODE>Annotator</Code> class contains static methods that are called
 *  to identify insertion points for watermarking code.  First, annotation
 *  calls are added to the subject program, and the program is run to
 *  produce a log.  Then, the watermarker marks the program by replacing
 *  the annotation calls with generated code.
 * <P>
 * NOTE: this class <b>must</b> be compiled with with -g
 * in order for the local variable tables to be intact.
 * If not, we can't get stack-frame information for the
 * mark() calls during tracing.
 * <P>
 *  13-dec-2000/cc
 *  30-sep-1999/gmt
 *  10-feb-2000/gmt
 *
 */

public class Annotator {
 
   static String VALUE = "";

   /*
    * This is the method we set a breakpoint on during tracing.
    * When execution reaches this point sandmark.watermark.ct.trace.VALUE 
    * contains whatever data was passed to the last mark() call.
    * If no data was passed then VALUE="----".
    * If a string was passed then VALUE= "\"the string\"".
    * If a long was passed then VALUE= "the value".
    * 
    * MARK() doesn't have a sm$stackID variable. The reason is that
    * we set the breakpoint at the beginning of MARK at which time
    * sm$stackID wouldn't exist anyway. It doesn't matter, we don't
    * need a unique stack frame ID for MARK anyway.
    */
   public static void MARK(){
   }

   // Marks an unparameterized annotation point.
   public static void sm$mark() {
      long sm$stackID = sandmark.watermark.ct.trace.Annotator.stackFrameNumber++;
      VALUE = "----";
      MARK();
   }

   // Marks an annotation point controlled by a string value.
   public static void sm$mark(String s) {
      long sm$stackID = sandmark.watermark.ct.trace.Annotator.stackFrameNumber++;
      VALUE = "\"" + s + "\"";
      MARK();
   }

   // Marks an annotation point controlled by an integral value.
   public static void sm$mark(long v) {
      long sm$stackID = sandmark.watermark.ct.trace.Annotator.stackFrameNumber++;
      VALUE = Long.toString(v);
      MARK();
   }

   public static long stackFrameNumber=0;
   public void clear() {
      stackFrameNumber=0;
   }

} // class Annotator

