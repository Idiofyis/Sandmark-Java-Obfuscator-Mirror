package sandmark.watermark.ct.embed;
public class PrepareTrace {


//----------------------------------------------------------------
/**
 *  Distributes a set of code fragments among insertion points.
 *  Produces a list of lists, with one sublist for each insertion point.
 *
 *  @param flist	code fragment list
 *  @param n		number of insertion points
 *
 * This method needs to be more random. It seems like the first
 * trace point is always picked, for example.
 */
static sandmark.util.MethodID[][] distribute(sandmark.util.MethodID[] flist, int n) {
    int nfrags = flist.length;
    //System.out.println("NFRAGS=" + nfrags);
    int taken = 0;
    sandmark.util.MethodID[][] result = new sandmark.util.MethodID[n][];
    for (int i = 0; i < n; i++) {
    	int leave = (int)((float)nfrags * ((float)(n - i - 1)) / n);
	int take = nfrags - leave - taken;
	sandmark.util.MethodID[] sublist = new sandmark.util.MethodID[take];
	for (int j = 0; j < take; j++) {
	    sublist[j] = flist[taken++];
	}
	result[i] = sublist;
    }
    return result;
}

static class TraceLocation {
   public sandmark.watermark.ct.trace.TracePoint tracePoint;
   public int kind;
   public TraceLocation(
      sandmark.watermark.ct.trace.TracePoint tracePoint,
      int kind) {
       this.tracePoint = tracePoint;
       this.kind = kind;
   }
}
/**
   The LOCATION/VALUE flags are determined by looking
   at all the traces that come from the same location.

   If there's just one trace at a location, it's
   LOCATION based. If there's more than one, it's
   VALUE based. But if there are any duplicate values
   at a location, the location is discarded and not used
   as a trace point.

   Any location that has multiple identical values should
   be discarded. All values at a location should be unique
   or else that location should be discarded.

<OL>
   <LI> Insert all trace points into a table indexed by
    sandmark.util.ByteCodeLocation. Each entry is a list of the
    trace points generated at that location.

   'table' is a Hashtable that maps
          the source location of a trace event (sandmark.util.ByteCodeLocation)
           to a list of the
      trace events that happened at that location (sandmark.watermark.ct.trace.TracePoint).

   <LI> Walk 'table' and determine for each source location whether
      <OL>
         <LI> there are multiple identical trace values at this
             location (in which case it should be discarded), or
         <LI> there is exactly one trace point at this
	     location (in which case it's a LOCATION trace point), or
         <LI> there are multiple unique values at this source
             location (in which case it's a VALUE trace point).
      </OL>

   <LI> Return an array of those tracepoints which have been
    determined to be unique.
</OL>
   @param traceData the code points that were touched during tracing.
*/
static TraceLocation[] uniquify(
   sandmark.watermark.ct.trace.TracePoint[] traceData) {
   java.util.Hashtable table = new java.util.Hashtable(traceData.length*2);
   for(int i=0; i<traceData.length; i++) {
       if (!table.containsKey(traceData[i].location))
          table.put(traceData[i].location, new java.util.LinkedList());
       java.util.LinkedList elements =
          (java.util.LinkedList) table.get(traceData[i].location);
       elements.add(traceData[i]);
   }
   final int DISCARD = 2;

   java.util.Hashtable keep = new java.util.Hashtable();

   java.util.Enumeration enum = table.keys();
   while (enum.hasMoreElements()){
       sandmark.util.ByteCodeLocation location = (sandmark.util.ByteCodeLocation) enum.nextElement();
       java.util.LinkedList elmts = (java.util.LinkedList) table.get(location);
       int kind = -1;
       if (elmts.size() == 1)
	   kind = sandmark.watermark.ct.embed.EmbedData.LOCATION;
       else {
          java.util.HashSet seen = new java.util.HashSet();
          java.util.Iterator listElmts = elmts.iterator();
          kind = sandmark.watermark.ct.embed.EmbedData.VALUE;
          while (listElmts.hasNext()) {
	      sandmark.watermark.ct.trace.TracePoint tp = (sandmark.watermark.ct.trace.TracePoint)listElmts.next();
              if (seen.contains(tp.value)) {
		  kind = DISCARD;
                  break;
	      }
	      seen.add(tp.value);
          }
       }
       if (kind != DISCARD) {
          java.util.Iterator keepElmts = elmts.iterator();
          while (keepElmts.hasNext()) {
	      sandmark.watermark.ct.trace.TracePoint tracep = (sandmark.watermark.ct.trace.TracePoint)keepElmts.next();
 	      keep.put(tracep, new TraceLocation(tracep,kind));
          }
      }
   }

   TraceLocation[] New = new TraceLocation[keep.size()];
   int k=0;
   for(int i=0; i<traceData.length; i++){
       if (keep.containsKey(traceData[i]))
	   New[k++] = (TraceLocation)keep.get(traceData[i]);
   }
   return New;
}


/**
 * Allocate the code fragments among the trace points.
 * Returns an array of EmbedData objects, each of
 * which maps a TracePoint object to a list of method
 * names. These methods should be inserted at the
 * point in the code that TracePoint references.
 *
 *  @param traceData	annotation points hit during tracing
 *  @param methods      array of methods to insert
 */
public static sandmark.watermark.ct.embed.EmbedData[] allocate(
   sandmark.watermark.ct.trace.TracePoint[] traceData,
   sandmark.util.MethodID[] methods) throws Exception {
	/*for(int i=0;i<traceData.length;i++)
	System.out.println("trace points "+i+"="+traceData[i]);
	System.out.println("trace pointsssssssssssssssssssssssssssss ");*/
   //		System.out.println(sandmark.watermark.ct.trace.TracePoint.toString(traceData));

   //	for(int i=0;i<methods.length;i++)
   //	System.out.println("Method points="+i+" "+methods[i]);

   TraceLocation[] traceLocations = uniquify(traceData);

  if (traceLocations.length < 1)
       throw new Exception("Not enough unique annotation points found during tracing.");

sandmark.util.MethodID[][] d = distribute(methods, traceLocations.length);
//  sandmark.util.MethodID[][] d = mydistribute(methods, traceLocations.length);

   int validPoints = 0;
   for(int i=0; i<d.length; i++)
       if (d[i].length > 0)
          validPoints++;
   sandmark.watermark.ct.embed.EmbedData[] e = new sandmark.watermark.ct.embed.EmbedData[validPoints];
   int k = 0;
   for(int i=0; i<d.length; i++)
       if (d[i].length > 0)
	   e[k++] = new sandmark.watermark.ct.embed.EmbedData(
                        traceLocations[i].tracePoint,d[i],traceLocations[i].kind);
   //	System.out.println("Embedding......");
   //	for(int i=0;i<validPoints;i++)
   //	{
   //		System.out.println("i="+i+"="+e[i]);
   //	}

   return e;
}



//-------------------------------------------------------------
/**
 * Run a test of this class.
 */
static void test (
   String header,
   sandmark.watermark.ct.trace.TracePoint[] tracePoints,
   sandmark.util.MethodID[] creators) throws Exception {
    System.out.println(header);
    sandmark.watermark.ct.embed.EmbedData[] embedData =
       sandmark.watermark.ct.embed.PrepareTrace.allocate(tracePoints, creators);
    for(int i=0; i<embedData.length; i++)
	System.out.println(embedData[i].toString());
}

/**
 * Construct a trace point.
 * @param value            the value at the trace point
 * @param callerName       the name of the caller
 * @param callerLocation   the name of the caller location
 * @param bytecodeLoc      the botecode location
 * @param lineNumber       the line number
 */
static sandmark.watermark.ct.trace.TracePoint TP(
   String value,
   String callerName,
   String callerLocation,
   int bytecodeLoc,
   int lineNumber) {
    sandmark.util.MethodID m = new sandmark.util.MethodID(callerName, "()V", callerLocation);
      return new sandmark.watermark.ct.trace.TracePoint(value,
         new sandmark.util.ByteCodeLocation(m, bytecodeLoc, lineNumber));
}

/**
 * Test this class.
 */
public static void main (String[] args) throws Exception {
    sandmark.util.ConfigProperties props =
        new sandmark.watermark.ct.CT().getConfigProperties();
    sandmark.util.MethodID[] creators4 = {
       new sandmark.util.MethodID("create1","",""),
       new sandmark.util.MethodID("create2","",""),
       new sandmark.util.MethodID("create3","",""),
       new sandmark.util.MethodID("create4","","")
    };
    sandmark.util.MethodID[] creators5 = {
       new sandmark.util.MethodID("create1","",""),
       new sandmark.util.MethodID("create2","",""),
       new sandmark.util.MethodID("create3","",""),
       new sandmark.util.MethodID("create4","",""),
       new sandmark.util.MethodID("create5","","")
    };

   sandmark.watermark.ct.trace.TracePoint[] tracePoints1 = new sandmark.watermark.ct.trace.TracePoint[3];
   tracePoints1[0] = TP("2", "caller1", "caller1Source", 2, 22);
   tracePoints1[1] = TP("5", "caller2", "caller2Source", 5, 44);
   tracePoints1[2] = TP("9", "caller3", "caller3Source", 9, 77);
   test("TEST 1: #tracePoints<#methods:",tracePoints1,creators4);

   sandmark.watermark.ct.trace.TracePoint[] tracePoints2 = new sandmark.watermark.ct.trace.TracePoint[6];
   tracePoints2[0] = TP("2", "caller1", "caller1Source", 2, 22);
   tracePoints2[1] = TP("5", "caller2", "caller2Source", 5, 44);
   tracePoints2[2] = TP("9", "caller3", "caller3Source", 9, 77);
   tracePoints2[3] = TP("1", "caller4", "caller4Source", 11, 123);
   tracePoints2[4] = TP("3", "caller5", "caller5Source", 66, 456);
   tracePoints2[5] = TP("6", "caller6", "caller6Source", 99, 777);
   test("\nTEST 2: #tracePoints>#methods:", tracePoints2,creators4);

   sandmark.watermark.ct.trace.TracePoint[] tracePoints3 = new sandmark.watermark.ct.trace.TracePoint[8];
   tracePoints3[0] = TP("2", "caller1", "caller1Source", 2, 22);
   tracePoints3[1] = TP("5", "caller2", "caller2Source", 5, 44);
   tracePoints3[2] = TP("9", "caller3", "caller3Source", 9, 77);
   tracePoints3[3] = TP("9", "caller3", "caller3Source", 9, 77);
   tracePoints3[4] = TP("9", "caller3", "caller3Source", 9, 77);
   tracePoints3[5] = TP("1", "caller4", "caller4Source", 11, 123);
   tracePoints3[6] = TP("1", "caller4", "caller4Source", 11, 123);
   tracePoints3[7] = TP("1", "caller4", "caller4Source", 11, 123);
   test("\nTEST 3: non-unique points", tracePoints3,creators4);

   sandmark.watermark.ct.trace.TracePoint[] tracePoints4 = new sandmark.watermark.ct.trace.TracePoint[4];
   tracePoints4[0] = TP("9", "caller2", "caller2Source", 5, 44);
   tracePoints4[1] = TP("9", "caller3", "caller3Source", 9, 77);
   tracePoints4[2] = TP("8", "caller3", "caller3Source", 9, 77);
   tracePoints4[3] = TP("7", "caller3", "caller3Source", 9, 77);
   test("\nTEST 4: unique points", tracePoints4,creators4);

   sandmark.watermark.ct.trace.TracePoint[] tracePoints5 = new sandmark.watermark.ct.trace.TracePoint[8];
   tracePoints5[0] = TP("INIT", "init", "TTTApplication", 0, 40);
   tracePoints5[1] = TP("----", "init", "TTTApplication", 104, 55);
   tracePoints5[2] = TP("1", "move", "TTTApplication", 36, 98);
   tracePoints5[3] = TP("0", "sm$mark", "TTTApplication", 49, 131);
   tracePoints5[4] = TP("2", "move", "TTTApplication", 36, 98);
   tracePoints5[5] = TP("4", "sm$mark", "TTTApplication", 49, 131);
   tracePoints5[6] = TP("1", "move", "TTTApplication", 36, 98);
   tracePoints5[7] = TP("8", "sm$mark", "TTTApplication", 49, 131);
   props.setProperty("Subgraph Count", "5");
   test( "\nTEST 5: non-unique points", tracePoints5,creators5);

}

}

