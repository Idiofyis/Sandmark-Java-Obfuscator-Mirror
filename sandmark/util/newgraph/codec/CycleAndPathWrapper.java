package sandmark.util.newgraph.codec;

public class CycleAndPathWrapper extends AbstractCodec implements WrapperCodec {
   private GraphCodec mCodec;
   public CycleAndPathWrapper() {
   }
   public CycleAndPathWrapper(GraphCodec wrappedCodec) {
      setWrappedCodec(wrappedCodec);
   }
   public void setWrappedCodec(GraphCodec wrappedCodec) {
      if(wrappedCodec == null)
	 throw new Error("wrapped codec cannot be null");
      if(mCodec != null)
	 throw new Error("can only call setWrappedCodec once");
      mCodec = wrappedCodec;
   }

   public int maxOutDegree() {
      if(mCodec != null)
	 return mCodec.maxOutDegree();

      java.util.Collection codecNames = 
	 sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	 (sandmark.util.classloading.IClassFinder.GRAPH_CODEC);
      int maxDegree = -1;
      for(java.util.Iterator names = codecNames.iterator() ; 
	  names.hasNext() ; ) {
	 String name = (String)names.next();
	 try {
	    GraphCodec codec = (GraphCodec)Class.forName(name).newInstance();
	    if(codec instanceof WrapperCodec)
	       continue;
	    int degree = codec.maxOutDegree();
	    if(degree > maxDegree)
	       maxDegree = degree;
	 } catch(Exception e) {
	 }
      }
      return maxDegree;
   }

   public sandmark.util.newgraph.Graph encode
      (java.math.BigInteger value,sandmark.util.newgraph.NodeFactory factory) {
      GraphCodec wrappedCodec = mCodec == null ? 
	 new ReduciblePermutationGraph() : mCodec;

      sandmark.util.newgraph.Graph g = wrappedCodec.encode(value,factory);
      //sandmark.util.newgraph.Graphs.dotInFile(g,value + ".orig.dot");
      int degree = 3;
      java.util.Hashtable nodeToCycle = new java.util.Hashtable();
      sandmark.util.newgraph.Graph cyclesAndPathGraph =
	 sandmark.util.newgraph.Graphs.createGraph(null,null);
      for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; ) {
	 Object node = nodes.next();
	 sandmark.util.newgraph.Graph cycle = createCycle(factory,degree);
	 nodeToCycle.put(node,new CycleState(cycle));
	 cyclesAndPathGraph = cyclesAndPathGraph.addAllNodes(cycle.nodes())
	    .addAllEdges(cycle.edges());
      }
      for(java.util.Iterator edges = g.edges() ; edges.hasNext() ; ) {
	 sandmark.util.newgraph.Edge edge = 
	    (sandmark.util.newgraph.Edge)edges.next();
	 CycleState source = (CycleState)nodeToCycle.get(edge.sourceNode());
	 CycleState sink = (CycleState)nodeToCycle.get(edge.sinkNode());
	 sandmark.util.newgraph.Graph path = createPath(factory,degree,edge);
	 sandmark.util.newgraph.Edge firstEdge,lastEdge;
	 {
	    Object firstSource = source.setNextSourceNode(),
	       firstSink = path.roots().next(),
	       lastSource = path.reverseRoots().next(),
	       lastSink = sink.setNextSinkNode();
	    try {
	       firstEdge = edge.clone(firstSource,firstSink);
	    } catch(CloneNotSupportedException e) {
	       firstEdge = 
		  new sandmark.util.newgraph.EdgeImpl(firstSource,firstSink);
	    }
	    try {
	       lastEdge = edge.clone(lastSource,lastSink);
	    } catch(CloneNotSupportedException e) {
	       lastEdge = 
		  new sandmark.util.newgraph.EdgeImpl(lastSource,lastSink);
	    }
	 }
	 cyclesAndPathGraph = 
	    cyclesAndPathGraph.addAllNodes(path.nodes()).addAllEdges(path.edges())
	    .addEdge(firstEdge).addEdge(lastEdge);
      }

      return cyclesAndPathGraph;
   }

   public java.math.BigInteger decode(sandmark.util.newgraph.Graph g) 
      throws DecodeFailure {
      
      java.util.HashSet replacedNodes = new java.util.HashSet();
      //Repeatedly replace the shortest cycle with a single node.
      //Delete every edge involving 2 cycle nodes, and exchange the
      //cycle node for the replacement node in every edge where one
      //node is in the cycle
      for(java.util.List shortCycles = findShortCycles(g,replacedNodes) ; 
      	 !shortCycles.isEmpty() ; 
      	 shortCycles = findShortCycles(g,replacedNodes)) {
         for(java.util.Iterator cycles = shortCycles.iterator() ; 
         	 cycles.hasNext() ; ) {
            Object cycle[] = (Object [])cycles.next();
            Object replacement = new Object();
            replacedNodes.add(replacement);
            g = g.addNode(replacement);
            for(int j = 0 ; j < cycle.length ; j++) {
               for(java.util.Iterator in = g.inEdges(cycle[j]) ;
               in.hasNext() ; ) {
                  sandmark.util.newgraph.Edge edge =
                     (sandmark.util.newgraph.Edge)in.next();
                  if(!edge.sourceNode().equals(replacement)) {
                     sandmark.util.newgraph.Edge newEdge;
                     try {
                        newEdge = edge.clone(edge.sourceNode(),replacement);
                     } catch(CloneNotSupportedException e) {
                        newEdge = new sandmark.util.newgraph.EdgeImpl
                        (edge.sourceNode(),replacement);
                     }
                     g = g.addEdge(newEdge);
                  }
               }
               for(java.util.Iterator in = g.outEdges(cycle[j]) ;
               in.hasNext() ; ) {
                  sandmark.util.newgraph.Edge edge =
                     (sandmark.util.newgraph.Edge)in.next();
                  if(!edge.sinkNode().equals(replacement)) {
                     sandmark.util.newgraph.Edge newEdge;
                     try {
                        newEdge = edge.clone(replacement,edge.sinkNode());
                     } catch(CloneNotSupportedException e) {
                        newEdge = new sandmark.util.newgraph.EdgeImpl
                        (replacement,edge.sinkNode());
                     }
                     g = g.addEdge(newEdge);
                  }
               }
               g = g.removeNode(cycle[j]);
            }
         }
      }
      
      for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; ) {
         Object node = nodes.next();
         do {
            if(replacedNodes.contains(node) || !g.hasNode(node))
               break;
            int in = g.inDegree(node),out = g.outDegree(node);
            if(in + out == 0) {
               g = g.removeNode(node);
               break;
            } else if(in + out == 1) {
               Object n = node;
               node = in == 1 ? g.preds(node).next() : g.succs(node).next();
               g = g.removeNode(n);
            } else
               break;
         } while(true);
      }
      
      //Replace every edge from a replacement node to a non-replacement
      //node with an edge from the replacement node to the first 
      //replacement node reachable from the non-replacement node.
      for(java.util.Iterator nodes = replacedNodes.iterator() ; 
      nodes.hasNext() ; ) {
         Object node = nodes.next();
         java.util.HashSet succs = new java.util.HashSet();
         for(java.util.Iterator outEdges = g.outEdges(node) ; 
         outEdges.hasNext() ; ) {
            sandmark.util.newgraph.Edge outEdge = 
               (sandmark.util.newgraph.Edge)outEdges.next();
            if(succs.contains(outEdge.sinkNode()))
               continue;
            succs.add(outEdge.sinkNode());
            Object succ = outEdge.sinkNode();
            for(java.util.Iterator reachables = 
               g.removeUnreachable(succ).breadthFirst(succ) ; 
            reachables.hasNext() ; ) {
               Object reachable = reachables.next();
               if(replacedNodes.contains(reachable)) {
                  sandmark.util.newgraph.Edge newEdge;
                  try {
                     newEdge = outEdge.clone(node,reachable);
                  } catch(CloneNotSupportedException e) {
                     newEdge = 
                        new sandmark.util.newgraph.EdgeImpl(node,reachable);
                  }
                  g = g.addEdge(newEdge);
                  break;
               }
            }
         }
      }
      
      //Remove all non-replacement nodes
      for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; ) {
         Object node = nodes.next();
         if(!replacedNodes.contains(node))
            g = g.removeNode(node);
      }
      
      //sandmark.util.newgraph.Graphs.dotInFile(g,"unwrapped.dot");
      
      if(mCodec != null)
         return mCodec.decode(g);
      
      java.util.Collection codecNames = 
         sandmark.util.classloading.ClassFinder.getClassesWithAncestor
         (sandmark.util.classloading.IClassFinder.GRAPH_CODEC);
      for(java.util.Iterator names = codecNames.iterator() ; 
      names.hasNext() ; ) {
         String name = (String)names.next();
         try {
            GraphCodec gc = (GraphCodec)Class.forName(name).newInstance();
            if(gc.getClass() != getClass())
               return gc.decode(g);
         } catch(Exception e) {
         }
      }
      throw new DecodeFailure("Could not decode using any available decoder");
   }
   
   private static java.util.List findShortCycles
      (sandmark.util.newgraph.Graph g,java.util.Set replaced) {
      java.util.Set nodeSet = new java.util.HashSet();
      for(java.util.Iterator nodeIt = g.nodes() ; nodeIt.hasNext() ; ) {
         Object node = nodeIt.next();
         if(!replaced.contains(node))
            nodeSet.add(node);
      }
      Object nodes[] = nodeSet.toArray(new Object[0]);
      int INFINITY = INFINITY(nodes.length);
      int d[][] = doFloyd(g,nodes);
      boolean handled[] = new boolean[nodes.length];
      for(int i = 0 ; i < handled.length ; i++)
         handled[i] = false;
      java.util.List paths = new java.util.LinkedList();
      java.util.Set usedNodes = new java.util.HashSet();
      /*We want to repeatedly:  find the node involved in the shortest cycle,
       * and handle it by:  finding the nodes that form the cycle, and accepting
       * the cycle if no nodes in the cycle are also part of another cycle
       * found in a previous iteration.
       */
      while(true) {
         int shortestCycleNode = -1;
         for(int i = 0 ; i < nodes.length ; i++)
            if(!handled[i] && 
               ((shortestCycleNode == -1 && d[i][i] < INFINITY) || 
               (shortestCycleNode != -1 && 
                d[i][i] < d[shortestCycleNode][shortestCycleNode])))
               shortestCycleNode = i;
         if(shortestCycleNode == -1)
            break;
         handled[shortestCycleNode] = true;
         int cycleLength = d[shortestCycleNode][shortestCycleNode];
         Object path[] = new Object[cycleLength];
         int curNode = shortestCycleNode;
         boolean reusesNodes = false;
         do {
            int i;
            /* Since shortestCycleNode is in a cycle of length cycleLength,
             * there must be a node n with a path from shortestCycleNode to n
             * with length cycleLength - 1 and a path from n to sCN with length
             * 1.  And we find all the intermediate nodes in the same fashion.
             * We're not interested in paths that contain nodes that are already
             * in smaller cycles, so if this path reuses a node, forget about it.
             */
            for(i = 0 ; i < nodes.length ; i++)
               if(d[i][curNode] == 1 && 
                  ((i == shortestCycleNode && cycleLength == 1) ||
                   d[shortestCycleNode][i] == cycleLength - 1))
                  break;
            reusesNodes = usedNodes.contains(nodes[i]);
            if(reusesNodes)
               break;
            path[cycleLength - 1] = nodes[i];
            cycleLength--;
            curNode = i;
         } while(curNode != shortestCycleNode);
         path[0] = nodes[shortestCycleNode];
         if(!reusesNodes) {
            for(int i = 0 ; i < path.length ; i++)
               usedNodes.add(path[i]);
            paths.add(path);
         }
      }
      return paths;
   }
   
   private static int INFINITY(int n) { return n < 5 ? 1000 : (2 * n); }
   
   private static int[][] doFloyd(sandmark.util.newgraph.Graph g,Object nodes[]) {
      int INFINITY = INFINITY(nodes.length);
      int d[][] = new int[nodes.length][nodes.length];
      for(int i = 0 ; i < nodes.length ; i++)
         for(int j = 0 ; j < nodes.length ; j++)
            d[i][j] = g.hasEdge(nodes[i],nodes[j]) ? 1 : INFINITY;
      for(int k = 0 ; k < nodes.length ; k++) {
         int dNew[][] = new int[nodes.length][nodes.length];
         for(int i = 0 ; i < nodes.length ; i++)
            for(int j = 0 ; j < nodes.length ; j++) {
               int oldVal = d[i][j],newVal = d[i][k] + d[k][j];
               dNew[i][j] = newVal < oldVal ? newVal : oldVal;
            }
         d = dNew;
      }
      return d;
   }

   private static sandmark.util.newgraph.Graph createPath
      (sandmark.util.newgraph.NodeFactory factory,int nodeCount,
       sandmark.util.newgraph.Edge template) {
      Object previous = factory.createNode();
      sandmark.util.newgraph.Graph g = 
	 sandmark.util.newgraph.Graphs.createGraph
	 (null,null).addNode(previous);
      for(int i = 1 ; i < nodeCount ; i++) {
	 Object node = factory.createNode();
	 sandmark.util.newgraph.Edge newEdge;
	 try {
	    newEdge = template.clone(previous,node);
	 } catch(CloneNotSupportedException e) {
	    newEdge = new sandmark.util.newgraph.EdgeImpl(previous,node);
	 }
	 g = g.addNode(node).addEdge(newEdge);
	 previous = node;
      }
      return g;
   }

   private static sandmark.util.newgraph.Graph createCycle
      (sandmark.util.newgraph.NodeFactory factory,int length) {
      Object first = factory.createNode(),
	 previous = first;
      sandmark.util.newgraph.Graph g = 
	 sandmark.util.newgraph.Graphs.createGraph
	 (null,null).addNode(first);
      for(int i = 2 ; i < length ; i++) {
	 Object node = factory.createNode();
	 g = g.addNode(node).addEdge(previous,node);
	 previous = node;
      }
      Object last = factory.createNode();
      g = g.addNode(last).addEdge(last,first).addEdge(previous,last);
      return g;
   }

   private static class CycleState {
      sandmark.util.newgraph.Graph g;
      Object nextSinkNode;
      Object nextSourceNode;
      CycleState(sandmark.util.newgraph.Graph g) 
      { this.g = g; nextSinkNode = nextSourceNode = g.nodes().next(); }
      Object setNextSinkNode() { 
	 Object n = nextSinkNode;
	 nextSinkNode = g.succs(n).next();
	 return n;
      }
      Object setNextSourceNode() { 
	 Object n = nextSourceNode;
	 nextSourceNode = g.succs(n).next();
	 return n;
      }
   }

   public static void main(String argv[]) throws Exception {
      new CycleAndPathWrapper(new RadixGraph()).test(argv);
   }
}
