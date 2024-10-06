package sandmark.watermark.ct.embed;
/**
 *  This class is used to distribute a set of code fragments among insertion points.
 * Allocate the code fragments among the trace points.
 * Returns an array of EmbedData objects, each of
 * which maps a TracePoint object to a list of method
 * names. These methods should be inserted at the
 * point in the code that TracePoint references.
 *
 */
public class Distribute{

private static final boolean Debug=false;
sandmark.util.ConfigProperties props;
sandmark.watermark.ct.trace.TracePoint[] traceData;  //contains initial trace data
sandmark.watermark.ct.trace.TracePoint[] newTraceData; //trace data after call to uniquify
TraceLocation[] traceLocations;
sandmark.program.Application app;
sandmark.watermark.ct.trace.callforest.Forest callForest;
sandmark.newstatistics.Stats nstatistics;
sandmark.analysis.classhierarchy.ClassHierarchy classHierarchy;
sandmark.util.MethodID[] allMeths;
sandmark.watermark.ct.embed.EmbedData[] embedData;
sandmark.watermark.ct.trace.callforest.Node DomNode;  //The node where the calls to storage creator
													//should be embedded

/**
 *
 *  @param props         global property list
 *  @param traceData     a vector of annotation points that
 *                       were hit during tracing
 *  @param inApp		 the application to be watermarked.
 *  @param creators      array of methods to insert
 *
 */

public Distribute (
        sandmark.util.ConfigProperties props,
   sandmark.watermark.ct.trace.TracePoint[] traceData,
   sandmark.program.Application inApp,
	sandmark.util.MethodID[] creators
   ) throws Exception {

   this.props = props;
   this.traceData = traceData;
   app = inApp;
   classHierarchy= app.getHierarchy();
   this.nstatistics = app.getStatistics();
   ClassHierarchy ch=new ClassHierarchy(classHierarchy);

	if(Debug)
	System.out.println(sandmark.watermark.ct.trace.TracePoint.toString(traceData));

	//call the uniquify method
	traceLocations=uniquify(traceData);
	newTraceData=new sandmark.watermark.ct.trace.TracePoint[traceLocations.length];
	for(int i=0;i<traceLocations.length;i++)
		newTraceData[i]=traceLocations[i].tracePoint;

	if(Debug)
	System.out.println(sandmark.watermark.ct.trace.TracePoint.toString(newTraceData));

	//callForest= new sandmark.watermark.ct.trace.callforest.Forest(newTraceData, classHierarchy, nstatistics, props);
	callForest= new sandmark.watermark.ct.trace.callforest.Forest(newTraceData, ch, nstatistics, props);

	embedData=allocate(creators);
}

public sandmark.watermark.ct.embed.EmbedData[] findEmbedding()
{
	return embedData;
}



/*Returns the new trace data after call to uniquify
 */
sandmark.watermark.ct.trace.TracePoint[] getNewTraceData()
{
 return newTraceData;

}

/*Gets the callforest
*/
sandmark.watermark.ct.trace.callforest.Forest getCallForest()
{
 return callForest;
}

/*Returns the node where the calls to storage creator should be embedded
 */

sandmark.watermark.ct.trace.callforest.Node getStorageNode()
{
 return DomNode;
}

/* List of methods for which we have to modify the parameter list. This methods appear in the path
 **from the DomNode to the node where the CreateGraph() method was called.
 */
sandmark.util.MethodID[] allMethods()
{
 return allMeths;
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
TraceLocation[] uniquify(
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
 *  @param methods      array of methods to insert
 */

public sandmark.watermark.ct.embed.EmbedData[] allocate(
    sandmark.util.MethodID[] methods) throws Exception {
	if(Debug)
	{	for(int i=0;i<newTraceData.length;i++)
			System.out.println("trace points "+i+"="+newTraceData[i]);

		for(int i=0;i<methods.length;i++)
			System.out.println("Method points="+i+" "+methods[i]);
	}

  if (traceLocations.length < 1)
       throw new Exception("Not enough unique annotation points found during tracing.");

   //sandmark.util.MethodID[][] d = distribute(methods, traceLocations.length);

 sandmark.util.MethodID[][] d = getDistribution(methods);

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
	if(Debug)
	{
		System.out.println("My Embedding......");
		for(int i=0;i<validPoints;i++)
		{
			System.out.println("i="+i+"="+e[i]);
		}
	}
   return e;

}
//----------------------------------------------------------------
//----------------------------------------------------------------
/**
 *  Distributes a set of code fragments among insertion points.
 *  Produces a list of lists, with one sublist for each insertion point.
 *
 *  @param flist	code fragment list
 */

sandmark.util.MethodID[][] getDistribution(sandmark.util.MethodID[] methods)
{	long threadid;
	long frameid;
	sandmark.watermark.ct.trace.callforest.Forest newCallForest;
	sandmark.watermark.ct.trace.TracePoint[] tp=
			new sandmark.watermark.ct.trace.TracePoint[newTraceData.length];

	// The commented code adds a newStackFrame namely sm$mark on top of existing frames for each tracepoint
	// This is required if sm$mark frame is not already there

	if(Debug)
	{	System.out.println("initial Tracepoint"+sandmark.watermark.ct.trace.TracePoint.toString(newTraceData));
	}
	/*for(int i=0; i<newTraceData.length; i++){
		threadid=newTraceData[i].stack[0].threadID;
		frameid=newTraceData[i].stack[0].frameID+1;
		sandmark.util.StackFrame st[]=new sandmark.util.StackFrame[newTraceData[i].stack.length+1];
		st[0]=Distribute.mkFrame("sm$mark","()V","sandmark.watermark.ct.trace.Annotator",45,4,threadid,frameid);
		for(int j=0;j<newTraceData[i].stack.length;j++)
		{
			st[j+1]=newTraceData[i].stack[j];
		}
		tp[i]=new sandmark.watermark.ct.trace.TracePoint(newTraceData[i].value,newTraceData[i].location,st);
	}

	ClassHierarchy ch=new ClassHierarchy(classHierarchy);

	newCallForest= new sandmark.watermark.ct.trace.callforest.Forest(tp, ch, nstatistics, props);*/

	tp=newTraceData;
	newCallForest=callForest;

		if(Debug)
		{	String[] S = newCallForest.toDot();
			for(int i=0; i<S.length; i++)
			{ try{
				 sandmark.util.Misc.writeToFile("temp" + i + ".dot", S[i]);
				System.out.println("written"+i);
				}catch(Exception e){}
			}
		}

	// This is used to get the list of insertion points
	sandmark.watermark.ct.embed.InsertionPoints ipoints=new sandmark.watermark.ct.embed.InsertionPoints(methods.length,newCallForest);

	java.util.ArrayList nlist=ipoints.getInsertionPoints();
	DomNode=ipoints.getDomNode();
	allMeths=ipoints.getAllMethods();


	 if(Debug)
	 	System.out.println("Nlist Size="+nlist.size());

		int factor=methods.length/nlist.size();
		int taken = 0;
		int n=traceLocations.length;
	    sandmark.util.MethodID[][] result = new sandmark.util.MethodID[n][];
	    int count=0;
	    int take=0;
	    int nfrags=methods.length;

	  for (int i = 0; i < n; i++)
	  {
	    boolean found=false;
	    for(int j=0;j< nlist.size();j++)
	     { sandmark.util.StackFrame p=((sandmark.watermark.ct.trace.callforest.Node)nlist.get(j)).getFrame();
	       int k;
	    	for(k=0;k< tp[i].stack.length;k++)
	        {
				if(tp[i].stack[k].equals(p))
				{ count++;
				  if(count==nlist.size())
				  		take=nfrags-taken;
				  else
				  	take=factor;
				  	found=true;
				  	break;
			    }
		    }
  			if(found)
				break;


	  	 }


			if(found)
			{

				sandmark.util.MethodID[] sublist = new sandmark.util.MethodID[take];

				for (int j = 0; j < take; j++)
				{
					sublist[j] = methods[taken++];
				}
				result[i] = sublist;

			}
			else
			{
				sandmark.util.MethodID[] sublist = new sandmark.util.MethodID[0];
				result[i] = sublist;
			}
	 }
	    if(taken!=nfrags)
	    {
			 System.out.println(" Unknown Error");
			 System.exit(0);

	    }
    return result;


}

public static class ClassHierarchy extends sandmark.analysis.classhierarchy.ClassHierarchy{
	sandmark.analysis.classhierarchy.ClassHierarchy origClassHierarchy;
   public ClassHierarchy(sandmark.analysis.classhierarchy.ClassHierarchy c)
   { origClassHierarchy=c;

   }

   public boolean methodRenameOK (
      sandmark.util.MethodID origMethod,
      sandmark.util.MethodID newMethod) {
      if (origMethod.getName().equals("sm$mark"))
      {
		  return true;
      }
      else
      {
		try{
		  return origClassHierarchy.methodRenameOK(origMethod,newMethod);
		}catch(Exception e){ System.out.println(e); }
		return false;
	  }
   }
}


/*
static sandmark.util.StackFrame mkFrame(
   String name,
   String sig,
   String className,
   long lineNumber,
   long codeIndex,
   long threadID,
   long frameID) {
      return new sandmark.util.StackFrame(
         new sandmark.util.ByteCodeLocation(
            new sandmark.util.MethodID(name, sig, className),
            lineNumber,
            codeIndex
         ),
         threadID, frameID
      );
}
*/

}