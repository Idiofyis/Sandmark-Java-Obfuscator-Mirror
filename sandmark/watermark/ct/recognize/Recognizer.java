package sandmark.watermark.ct.recognize;

/**
 *  The sandmark.watermark.ct.recognize.Recognizer class contains methods for
 *  recognizing dynamically built watermarks in a running program.
 */

public class Recognizer extends sandmark.util.exec.Overseer {
   sandmark.util.CircularBuffer buffer;
   static final int BUFFER_SIZE = 200;
   sandmark.util.ConfigProperties props;
   java.util.Iterator myWatermarks;

   class InitBreakpoint extends sandmark.util.exec.Breakpoint{
      public InitBreakpoint () {
         super("<init>",sandmark.util.exec.Breakpoint.standardExclude);
      }
      public void Action(sandmark.util.exec.MethodCallData data) {
         com.sun.jdi.ObjectReference ref = 
            (com.sun.jdi.ObjectReference)buffer.add(data.getObject());
         if(ref != null)
            ref.enableCollection();
      }
   }

   /**
    *  Runs a program and looks for watermarks.
    *
    *  @param props              global property list
    *
    *  <P> Reads properties:
    *  <BR> Input File: the watermarked program
    *  <BR> Class Path:          path to search for class-files
    *  <BR> Main Class:          main class, if not set in Jar file
    *  <BR> Arguments:   program arguments, if any
    *  <P>
    *  Call it like this:
    * <PRE>
    *  sandmark.watermark.ct.recognize.Recognizer recognizer = new sandmark.watermark.ct.recognize.Recognizer(props);
    *  recognizer.run();
    *  java.util.Iterator iter = recognizer.iterator();
    *  while (iter.hasNext()) {
    *      sandmark.watermark.ct.recognize.RecognizeData res = (sandmark.watermark.ct.recognize.RecognizeData) iter.next();
    *      System.out.println(res.toString());
    *  }
    *  recognizer.waitToComplete();
    * </PRE>
    * <P>
    * The RecognizeData-object that the recognizer returns 
    * contains information about which graph was found,
    * what watermark was extracted from this graph, and
    * which encoding was used. 
    */

   public Recognizer(sandmark.watermark.DynamicRecognizeParameters params,
                     sandmark.util.ConfigProperties props) {
      super(params.programCmdLine);
      this.props = props;
      buffer = new sandmark.util.CircularBuffer(BUFFER_SIZE);
      registerBreakpoint(new InitBreakpoint());
   }

   protected void onProgramExit(com.sun.jdi.VirtualMachine vm) {
      System.out.println("exited");
   }


   protected void onDisconnect(){
      if (myWatermarks==null){
         myWatermarks = new Watermarks(buffer,vm,props);      
         STOP();
      }
   }


   //----------------------------------------------------------
   static class Watermarks implements java.util.Iterator {
      sandmark.util.ConfigProperties mProps;
      sandmark.watermark.ct.recognize.RecognizeData nextObject;
      java.util.Iterator graphs;
      java.util.Iterator codecIt = new java.util.LinkedList().iterator();
      java.util.Iterator combinations = new java.util.LinkedList().iterator();
      static java.util.Set codecs = getCodecs();
       
      static {
         assert codecs.iterator().hasNext() : "class loading is broken";
      }
       
      DecodedGraph currentGraph;
      sandmark.util.newgraph.codec.GraphCodec currentCodec;
      int currentCombination[];
       
      Watermarks(sandmark.util.CircularBuffer buffer,
                 com.sun.jdi.VirtualMachine vm,
                 sandmark.util.ConfigProperties cp) {
         mProps = cp;
         graphs = new Graphs(buffer,vm);
         get();
      }
       
      private boolean getNextTriple() {
         if(!combinations.hasNext() && !codecIt.hasNext() && !graphs.hasNext())
            return false;
          
         while(!combinations.hasNext()) {
            if(!codecIt.hasNext()) {
               if(!graphs.hasNext())
                  break;
               currentGraph = (DecodedGraph)graphs.next();
               codecIt = codecs.iterator();
            }
            currentCodec = 
               (sandmark.util.newgraph.codec.GraphCodec)codecIt.next();
            combinations = new sandmark.util.Combinations
               (currentGraph.graph.maxOutDegree(),currentCodec.maxOutDegree());
         }
          
         if(!combinations.hasNext())
            return false;
          
         currentCombination = (int [])combinations.next();
         return true;
      }
       
      void get() {
         for(nextObject = null; nextObject == null ; ) {
            boolean hasNext = getNextTriple();
            if(!hasNext)
               return;
            java.util.HashSet keptEdges = new java.util.HashSet();
            for(int i = 0 ; i < currentCombination.length ; i++)
               keptEdges.add(new Integer(currentCombination[i]));
            sandmark.util.newgraph.Graph g = currentGraph.graph;
            for(java.util.Iterator edges = g.edges() ; edges.hasNext() ; ) {
               sandmark.util.newgraph.TypedEdge edge = 
                  (sandmark.util.newgraph.TypedEdge)edges.next();
               if(!keptEdges.contains(new Integer(edge.getType())))
                  g = g.removeEdge(edge);
            }
            try {
               nextObject = decode(currentGraph.root,g,currentCodec,mProps);
               break;
            } catch(sandmark.util.newgraph.codec.DecodeFailure d) {}
         }
      }
       
      public boolean hasNext() {
         return nextObject != null;
      }
       
      public Object next() throws java.util.NoSuchElementException {
         if (nextObject == null)
            throw new java.util.NoSuchElementException();
         RecognizeData n = nextObject;
         get();
         n.addToGraphViewer();
         return n;
      }
       
      public void remove() {
         throw new UnsupportedOperationException();
      }
       
      private static java.util.Set getCodecs() {
         java.util.Collection codecNames = 
            sandmark.util.classloading.ClassFinder.getClassesWithAncestor
            (sandmark.util.classloading.IClassFinder.GRAPH_CODEC);
         java.util.HashSet codecSet = new java.util.HashSet();
         for(java.util.Iterator codecIt = codecNames.iterator() ; 
             codecIt.hasNext() ; ) {
            try { 
               Class codec = Class.forName((String)codecIt.next());
               codecSet.add(codec.newInstance()); 
            } catch(Exception e) {}
         }
         return codecSet;
      }
       
      /**
       * Attempts to decode the graph theGraph into a watermark
       * using the graph decoder 'decoder', and mapping kidnames
       * according to 'kidmap'. If decoding doesn't work (for
       * whatever reason), an exception is thrown.
       */
      private static sandmark.watermark.ct.recognize.RecognizeData decode 
         (Long root,sandmark.util.newgraph.Graph g,
          sandmark.util.newgraph.codec.GraphCodec gc,
          sandmark.util.ConfigProperties cp) 
         throws sandmark.util.newgraph.codec.DecodeFailure {
         java.math.BigInteger value = gc.decode(g);
         String wm_String = 
            cp.getProperty("Numeric Watermark").equals("true") ?
            value.toString() : sandmark.util.StringInt.decode(value);
         return new RecognizeData(gc,g,value,wm_String,root);
      }
   }

   /*
    * Returns a sequence of 'sandmark.watermark.ct.recognize.RecognizeData' 
    * objects. These represent the watermarks that were
    * extracted from the heap. 
    */
   public java.util.Iterator watermarks() {
      return myWatermarks;
   }


   //-------------------------------------------------------
   static class DecodedGraph {
      private sandmark.util.newgraph.Graph graph;
      private Long root;
      public DecodedGraph
         (sandmark.util.newgraph.Graph graph,Long root) {
         this.graph = graph;
         this.root = root;
      }
   }

   /*
    * Returns a sequence of 'DecodedGraph' objects describing
    * graphs that were extracted and decoded from the heap.
    * The graphs are generated in the reverse order 
    * of root-node allocation, i.e. the graph whose root
    * node is youngest is generated first.
    */
   static class Graphs implements java.util.Iterator {
      DecodedGraph nextObject;
      java.util.HashSet seen;
      java.util.Iterator rootIt;
      sandmark.util.newgraph.Graph graph;
      
      public Graphs (sandmark.util.CircularBuffer buffer,
                     com.sun.jdi.VirtualMachine vm) {
         seen = new java.util.HashSet(BUFFER_SIZE*2);
         java.util.List rootObjs = new java.util.ArrayList();
         java.util.List rootNodes = new java.util.ArrayList();
         for(java.util.Iterator it = buffer.iterator() ; it.hasNext() ; ) {
            com.sun.jdi.ObjectReference root = 
               (com.sun.jdi.ObjectReference)it.next();

            if(root != null && !root.isCollected()) {
               rootObjs.add(root);
               rootNodes.add(new Long(root.uniqueID()));
            }
         }
         graph = Heap2Graph.unpack(vm,rootObjs);
         sandmark.util.graph.graphview.GraphList.instance().add(graph,"heap");
         rootIt = rootNodes.iterator();
      }
      
      void get() {
         if (nextObject != null) return;
         while (rootIt.hasNext() && (nextObject == null)) {
            Long node = (Long)rootIt.next();
            seen.add(node);
            if(!graph.hasNode(node))
               throw new Error("graph must contain roots: " + node);
            sandmark.util.newgraph.Graph g = graph.removeUnreachable(node);
            nextObject = new DecodedGraph(g,node);
         }
      }
      
      public boolean hasNext() {
         get();
         return nextObject != null;
      }
      
      public Object next() throws java.util.NoSuchElementException {
         get();
         if (nextObject == null)
            throw new java.util.NoSuchElementException();
         DecodedGraph n = nextObject;
         nextObject = null;
         return n;
      }
      
      public void remove() {}
   }

} // class Recognizer


