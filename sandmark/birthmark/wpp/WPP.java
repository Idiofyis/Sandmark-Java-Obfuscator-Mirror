package sandmark.birthmark.wpp;

public class WPP extends sandmark.birthmark.DynamicBirthmark{

   private boolean DEBUG = false;
   private static int IN = 0;
   private static int OUT = 1;

   public String getShortName(){
      return "WPP";
   }

   public String getLongName(){
      return "Determines if two applications are similar using whole program paths.";
   }

   public String getAlgHTML(){
      return "<HTML><BODY>" +
             "Whole Program Path birthmark" +
             "</BODY></HTML>";
   }

   public String getAuthor(){
      return "Ginger Myles";
   }

   public String getAuthorEmail(){
      return "mylesg@cs.arizona.edu";
   }

   public String getDescription(){
      return "computes a birthmark based on whole program paths.";
   }

   public String getAlgURL(){
      return "sandmark/birthmark/wpp/doc/help.html";
   }

   public sandmark.config.ModificationProperty[] getMutations(){
      sandmark.config.ModificationProperty[] properties = {};
      return properties;
   }

   public double calculate
      (sandmark.birthmark.DynamicBirthMarkParameters params) 
      throws Exception{
      Annotate anno = new Annotate(params.original);
      anno.annotate();
      anno.save(params.originalFile);
      startTracing(params.originalArgv);
      tracer.waitForExit();
      stopTracing();
      //System.out.println("stopped tracing");
      endTracing();
      //System.out.println("end tracing");

      int[] tracePoints = new int[annotationPoints.length];
      for(int i=0; i < annotationPoints.length; i++){
         tracePoints[i] = annotationPoints[i].hashCode();
      }
      //System.out.println("got hash codes");

      //runSequitur on trace 1
      RunSequitur seq = new RunSequitur(tracePoints);
      //System.out.println("starting sequitur");
      seq.runSequitur();
      if(DEBUG){
         System.out.println("Rules for app1");
         System.out.println(seq.getRules());
      }

      sandmark.util.newgraph.MutableGraph origDAG = buildDag(seq.getRules());

      if(DEBUG){
      System.out.println("the DAG");
      java.util.Iterator nodes = origDAG.nodes();
      while(nodes.hasNext()){
         DAGNode n = (DAGNode)nodes.next();
         System.out.println(n);
         java.util.Iterator outEdges = origDAG.outEdges(n);
         while(outEdges.hasNext()){
            DAGEdge e = (DAGEdge)outEdges.next();
            System.out.println(e);
         }
      }
      }

      //get a topological sort of the nodes in the graph
      java.util.ArrayList origSortedList = topoSort(origDAG.copy());
      if(DEBUG){
         System.out.println("topo sort");
         System.out.println(origSortedList);
      }

      anno = new Annotate(params.suspect);
      anno.annotate();
      anno.save(params.suspectFile);

      startTracing(params.suspectArgv);
      tracer.waitForExit();
      stopTracing();
      endTracing();

      tracePoints = new int[annotationPoints.length];
      for(int i=0; i < annotationPoints.length; i++){
         tracePoints[i] = annotationPoints[i].hashCode();
      }

      //runSequitur on trace 2
      seq = new RunSequitur(tracePoints);
      seq.runSequitur();
      if(DEBUG){
         System.out.println("Rules for app2");
         System.out.println(seq.getRules());
      }

      sandmark.util.newgraph.MutableGraph suspectDAG = buildDag(seq.getRules());

      if(DEBUG){
      System.out.println("the DAG");
      java.util.Iterator nodes = suspectDAG.nodes();
      while(nodes.hasNext()){
         DAGNode n = (DAGNode)nodes.next();
         System.out.println(n);
         java.util.Iterator outEdges = suspectDAG.outEdges(n);
         while(outEdges.hasNext()){
            DAGEdge e = (DAGEdge)outEdges.next();
            System.out.println(e);
         }
      }
      }

      java.util.ArrayList suspectSortedList = topoSort(suspectDAG.copy());
      if(DEBUG){
         System.out.println("topo sort");
         System.out.println(suspectSortedList);
      }

      java.util.HashMap vertexMapping = findSubgraphIsomorphism(origSortedList,
         suspectSortedList, origDAG.copy(), suspectDAG.copy());

      sandmark.util.newgraph.MutableGraph origDAGcopy = origDAG.copy();
      java.util.Set subgraphNodes = vertexMapping.keySet();
      java.util.Iterator origNodes = origDAG.nodes();
      while(origNodes.hasNext()){
         DAGNode n = (DAGNode)origNodes.next();
         if(!subgraphNodes.contains(n))
            origDAGcopy.removeNode(n);
      }

      int origNodeEdgeCount = getEdgeCount(origDAG) + origDAG.nodeCount();
      int subNodeEdgeCount = getEdgeCount(origDAGcopy) + origDAGcopy.nodeCount();

      if(DEBUG){
         System.out.println("orig count: " + origNodeEdgeCount);
         System.out.println("sub count: " + subNodeEdgeCount);
      }
      double similarity = ((double)subNodeEdgeCount / (double)origNodeEdgeCount)
         * 100;
      
      if(DEBUG){
         System.out.println("percent similarity: " + similarity);
         System.out.println("mapping size: " + vertexMapping.size());
         System.out.println(vertexMapping);
      }

      return similarity;

   }

      sandmark.birthmark.wpp.Tracer tracer = null;

   public void startTracing(String argv[])
      throws sandmark.util.exec.TracingException {

      tracer = new sandmark.birthmark.wpp.Tracer(argv);
      tracer.run();

   }

   sandmark.birthmark.wpp.TracePoint annotationPoints[];
   public void endTracing() {

      annotationPoints =
         (sandmark.birthmark.wpp.TracePoint[])
         tracer.getTracePoints().toArray(
         new sandmark.birthmark.wpp.TracePoint[0]);
   }

   public void stopTracing() {
      tracer.STOP();
   }

   private sandmark.util.newgraph.MutableGraph buildDag(String rules){

      //parse the rules
      String[] setOfRules = rules.split("\\n");
      java.util.ArrayList splitRules = new java.util.ArrayList();
      for(int i=0; i < setOfRules.length; i++){
         String[] splitRule = setOfRules[i].split(" ");
         splitRules.add(splitRule);
      }

      //construct the DAG
      //first add all of the nodes to the graph
      sandmark.util.newgraph.MutableGraph dag = 
         new sandmark.util.newgraph.MutableGraph();
      for(int i=0; i < splitRules.size(); i++){
         String[] rule = (String[])splitRules.get(i);
         DAGNode node = new DAGNode(rule[0]);
         dag.addNode(node);
      }

      //now we need to add each edge
      for(int i=0; i < splitRules.size(); i++){
         String[] rule = (String[])splitRules.get(i);
         DAGNode sourceNode = new DAGNode(rule[0]);
         //int numEdges = 0;
         for(int j=2; j < rule.length; j++){
            if(rule[j].startsWith("R")){
               //numEdges++;
               DAGNode sinkNode = new DAGNode(rule[j]);
               if(dag.hasEdge(sourceNode, sinkNode)){
                  java.util.Iterator outEdges = dag.outEdges(sourceNode);
                  while(outEdges.hasNext()){
                     DAGEdge e = (DAGEdge)outEdges.next();
                     if(((DAGNode)e.sinkNode()).equals(sinkNode)){
                        e.increaseEdgeCount();
                        break;
                     }
                  }
               }else{
                  DAGEdge edge = new DAGEdge(sourceNode, sinkNode);
                  dag.addEdge(edge);
               }
            }
         }
      }
      return dag;
   }

   private java.util.ArrayList topoSort(sandmark.util.newgraph.MutableGraph graph){
      java.util.LinkedList ll = new java.util.LinkedList();
      java.util.ArrayList sortedList = new java.util.ArrayList();

      java.util.Iterator roots = graph.roots();
      while(roots.hasNext())
         ll.addLast(roots.next());

      while(ll.size() > 0){
         DAGNode n = (DAGNode)ll.removeFirst();
         sortedList.add(n);
         java.util.Iterator succs = graph.succs(n);
         while(succs.hasNext()){
            DAGNode s = (DAGNode)succs.next();
            graph.removeEdge(n,s);
         }
         graph.removeNode(n);
         roots = graph.roots();
         //System.out.println("new roots");
         java.util.TreeSet newNodesToAdd = new java.util.TreeSet(new DAGNode());
         while(roots.hasNext()){
            DAGNode r = (DAGNode)roots.next();
            if(!ll.contains(r)){
               newNodesToAdd.add(r);
               //System.out.println("root: " + r);
            }
         }
         java.util.Iterator newNodes = newNodesToAdd.iterator();
         while(newNodes.hasNext())
            ll.addLast(newNodes.next());
      }
      return sortedList;
   }

   private java.util.HashMap findSubgraphIsomorphism(
      java.util.ArrayList origList, java.util.ArrayList suspectList,
      sandmark.util.newgraph.MutableGraph origDAG,
      sandmark.util.newgraph.MutableGraph suspectDAG){

      int origIndex = 0;
      int suspectIndex = 0;

      java.util.HashMap vertexMapping = new java.util.HashMap();

      while(origIndex < origList.size() && suspectIndex < suspectList.size()){
         DAGNode orig = (DAGNode)origList.get(origIndex);
         DAGNode suspect = (DAGNode)suspectList.get(suspectIndex);
         int origInDeg = getDegree(orig, origDAG, IN);
         int origOutDeg = getDegree(orig, origDAG, OUT);
         int suspectInDeg = getDegree(suspect, suspectDAG, IN);
         int suspectOutDeg = getDegree(suspect, suspectDAG, OUT);

         if(origInDeg <= suspectInDeg && 
            origDAG.inDegree(orig) <= suspectDAG.inDegree(suspect) &&
            origOutDeg <= suspectOutDeg &&
            origDAG.outDegree(orig) <= suspectDAG.outDegree(suspect)){
            boolean add = true;
            java.util.Iterator preds = origDAG.preds(orig);
            while(preds.hasNext()){
               DAGNode p = (DAGNode)preds.next();
               DAGNode suspectP = (DAGNode)vertexMapping.get(p);
               if(suspectP == null || !suspectDAG.hasEdge(suspectP, suspect))
                  add = false;
               else{
                  int origEdgeCount = 0;
                  int suspectEdgeCount = 0;
                  java.util.Iterator origEdges = origDAG.inEdges(orig);
                  java.util.Iterator suspectEdges = suspectDAG.inEdges(suspect);
                  while(origEdges.hasNext()){
                     DAGEdge oe = (DAGEdge)origEdges.next();
                     if((oe.sourceNode()).equals(p))
                        origEdgeCount = oe.getEdgeCount();
                  }
                  while(suspectEdges.hasNext()){
                     DAGEdge se = (DAGEdge)suspectEdges.next();
                     if((se.sourceNode()).equals(suspectP))
                        suspectEdgeCount = se.getEdgeCount();
                  }
                  if(origEdgeCount > suspectEdgeCount)
                     add = false;
               }
            }
            if(add){
               vertexMapping.put(orig, suspect);
               origIndex++;
            }
            suspectIndex++;
         }else{
            if(origList.size() < suspectList.size())
               suspectIndex++;
            else if(origList.size() > suspectList.size())
               origIndex++;
            else{
               origIndex++;
               suspectIndex++;
            }
         }
      }
      return vertexMapping;
   }

   private int getDegree(DAGNode n, 
      sandmark.util.newgraph.MutableGraph graph,
      int direction){
      
      int degree = 0;
      java.util.Iterator edges;
      if(direction == IN)
         edges = graph.inEdges(n);
      else
         edges = graph.outEdges(n);
      while(edges.hasNext()){
         DAGEdge e = (DAGEdge)edges.next();
         degree += e.getEdgeCount();
      }
      return degree;
   }

   private int getEdgeCount(sandmark.util.newgraph.MutableGraph graph){
      int count = 0;
      java.util.Iterator edges = graph.edges();
      while(edges.hasNext()){
         DAGEdge e = (DAGEdge)edges.next();
         count += e.getEdgeCount();
      }
      return count;
   }

   

   public static void main(String[] args){
      String file1 = args[0];
      String file2 = args[1];
      String mainClass = args[2];

      //java.util.Properties props = new java.util.Properties();
      sandmark.util.ConfigProperties props =
         sandmark.birthmark.DynamicBirthMarkParameters.createConfigProperties();
      props.setProperty("Suspect File", file2);
      props.setProperty("Main Class", mainClass);
      props.setProperty("Class Path",
"/cs/wmark/mylesg/smextern3/BCEL.jar:/cs/wmark/mylesg/smextern3/bloat-1.0.jar:/cs/wmark/mylesg/smark3/sandmark.jar");
      props.setProperty("Arguments",
         "/cs/wmark/mylesg/smtest3/tests/kaffe/wc/benchmark.txt");
      //props.setProperty("Arguments",
      //       "_213_javac");
      //     "40 < /cs/wmark/mylesg/smtest3/tests/hard/decode/cipher.txt");

      try{
         sandmark.program.Application app1 = new sandmark.program.Application(file1);
         WPP wpp = new WPP();
         wpp.calculate
            (sandmark.birthmark.DynamicBirthMarkParameters.buildParameters
             (props,app1));
      }catch(Exception e){
         e.printStackTrace();
         System.out.println("couldn't create app object");
      }
   }
}


