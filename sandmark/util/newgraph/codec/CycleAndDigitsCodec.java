package sandmark.util.newgraph.codec;

public abstract class CycleAndDigitsCodec extends AbstractCodec 
   implements TypedEdgeCodec {

   protected abstract java.math.BigInteger decode(int digits[],int cycleLength);
   protected abstract int [] digits(java.math.BigInteger value,int cycleLength);
   protected abstract int cycleLength(java.math.BigInteger value);

   public int maxOutDegree() { return 2; }

   public sandmark.util.newgraph.Graph encode
      (java.math.BigInteger value,sandmark.util.newgraph.NodeFactory factory) {
      int cycleLength = cycleLength(value);
      assert cycleLength >= 2 : "someone should fix this case";
      int digits[] = digits(value,cycleLength);
      sandmark.util.newgraph.Graph g = 
	 createRootedCycleGraph(cycleLength,factory);
      Object root = g.roots().next();
      Object cycleStart = g.succs(root).next();
      Object nodes[] = getCycleInOrder(g,cycleStart,cycleLength);
      for(int i = 0 ; i < cycleLength ; i++) {
	 int digit = digits[i];
	 int position = i + digit;
	 if(position >= cycleLength)
	    position -= cycleLength;
	 g = g.addEdge(new sandmark.util.newgraph.TypedEdge
		       (nodes[i],nodes[position],1));
      }
      return g;
   }

   public java.math.BigInteger decode(sandmark.util.newgraph.Graph g) 
      throws DecodeFailure {
      int edgeTypes[] = checkEdgeTypes(g);
      checkEdges(g);
      int cycleType,digitType;
      try {
	 checkCycle(g,edgeTypes[0]);
	 cycleType = edgeTypes[0];
	 digitType = edgeTypes[1];
      } catch(DecodeFailure e) {
	 checkCycle(g,edgeTypes[1]);
	 cycleType = edgeTypes[1];
	 digitType = edgeTypes[0];
      }
      int nodeCount = g.nodeCount();
      int cycleLength = nodeCount - 1;
      java.util.ArrayList nodesInOrder = new java.util.ArrayList(nodeCount);
      {
	 Object node = g.roots().next();
	 for(int i = 0 ; i < nodeCount ; i++) {
	    nodesInOrder.add(node);
	    node = getSuccByType(g,node,cycleType);
	 }
      }
      int digits[] = new int[cycleLength];
      for(int i = 1 ; i < nodeCount ; i++) {
	 Object node = nodesInOrder.get(i);
	 Object digit = getSuccByType(g,node,digitType);
	 int offset = nodesInOrder.indexOf(digit) - i;
	 if(offset < 0)
	    offset += cycleLength;
	 digits[i - 1] = offset;
      }
      return decode(digits,cycleLength);
   }

   private  static Object getSuccByType
      (sandmark.util.newgraph.Graph g,Object node,int type) 
      throws DecodeFailure {
      for(java.util.Iterator outEdges = g.outEdges(node) ; 
	  outEdges.hasNext() ; ) {
	 sandmark.util.newgraph.TypedEdge te =
	    (sandmark.util.newgraph.TypedEdge)outEdges.next();
	 if(te.getType() == type)
	    return te.sinkNode();
      }
      throw new DecodeFailure
	 ("Graph validity check problem:  missing edge of type " + type);
   }

   private static int [] checkEdgeTypes(sandmark.util.newgraph.Graph g) 
      throws DecodeFailure {
      java.util.HashSet types = new java.util.HashSet();
      for(java.util.Iterator edges = g.edges() ; edges.hasNext() ; ) {
	 sandmark.util.newgraph.Edge edge = 
	    (sandmark.util.newgraph.Edge)edges.next();
	 if(!(edge instanceof sandmark.util.newgraph.TypedEdge))
	    throw new DecodeFailure("RadixGraph requires typed edges");
	 sandmark.util.newgraph.TypedEdge te = 
	    (sandmark.util.newgraph.TypedEdge)edge;
	 types.add(new Integer(te.getType()));
      }
      if(types.size() != 2)
	 throw new DecodeFailure("RadixGraph requires exactly 2 edge types");
      for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; ) {
	 Object node = nodes.next();
	 java.util.HashSet edgeTypes = new java.util.HashSet();
	 for(java.util.Iterator outEdges = g.outEdges(node) ; 
	     outEdges.hasNext() ; ) {
	    sandmark.util.newgraph.TypedEdge te =
	       (sandmark.util.newgraph.TypedEdge)outEdges.next();
	    Integer type = new Integer(te.getType());
	    if(edgeTypes.contains(type))
	       throw new DecodeFailure("RadixGraph requires outedges from " +
				       "a node to be of different types");
	    edgeTypes.add(type);
	 }
      }
      int edgeTypes[] = new int[2];
      java.util.Iterator typeIt = types.iterator();
      edgeTypes[0] = ((Integer)typeIt.next()).intValue();
      edgeTypes[1] = ((Integer)typeIt.next()).intValue();
      return edgeTypes;
   }

   private static sandmark.util.newgraph.Graph createRootedCycleGraph
      (int size,sandmark.util.newgraph.NodeFactory factory) {
      Object root = factory.createNode();
      Object first = factory.createNode(),
	 previous = first;
      sandmark.util.newgraph.Graph g = 
	 sandmark.util.newgraph.Graphs.createGraph
	 (null,null).addNode(root).addNode(first).addEdge
	 (new sandmark.util.newgraph.TypedEdge(root,first,0));
      for(int i = 2 ; i < size ; i++) {
	 Object node = factory.createNode();
	 g = g.addNode(node);
	 g = g.addEdge(new sandmark.util.newgraph.TypedEdge(previous,node,0));
	 previous = node;
      }
      Object last = factory.createNode();
      return g.addNode(last)
	 .addEdge(new sandmark.util.newgraph.TypedEdge(last,first,0))
	 .addEdge(new sandmark.util.newgraph.TypedEdge(previous,last,0));
   }

   private static Object [] getCycleInOrder
      (sandmark.util.newgraph.Graph cycleGraph,Object root,int cycleLength) {
      Object nodes[] = new Object[cycleLength];
      for(int i = 0 ; i < nodes.length ; i++) {
	 nodes[i] = root;
	 java.util.Iterator succs = cycleGraph.succs(root);
	 if(!succs.hasNext())
	    throw new Error("Not a cycle graph");
	 root = succs.next();
      }
      return nodes;
   }

   private static int count(java.util.Iterator it) {
      int cnt = 0;
      for( ; it.hasNext() ; cnt++,it.next())
	  ;
      return cnt;
   }

   static void checkEdges(sandmark.util.newgraph.Graph g) 
      throws DecodeFailure {
      boolean foundRoot = false;
      for(java.util.Iterator nodes = g.nodes() ; nodes.hasNext() ; ) {
	 Object node = nodes.next();
	 int outCnt = count(g.outEdges(node));
	 if(outCnt == 2)
	    continue;
	 if(outCnt == 1) {
	    int inCnt = count(g.inEdges(node));
	    if(!foundRoot && inCnt == 0) {
	       foundRoot = true;
	       continue;
	    }
	 }
	 throw new DecodeFailure("Node degree problems");
      }
      if(!foundRoot)
	  throw new DecodeFailure("Missing root");
   }

   static void checkCycle(sandmark.util.newgraph.Graph g,int cycleType)
      throws DecodeFailure {
      java.util.HashSet seen = new java.util.HashSet();
      Object cycleStart = g.succs(g.roots().next()).next();
      boolean sawStart = false;
      for(Object node = g.roots().next() ; !(sawStart && node.equals(cycleStart)) ; 
	  node = getSuccByType(g,node,cycleType)) {
	 if(seen.contains(node))
	    throw new DecodeFailure();
	 seen.add(node);
	 sawStart = sawStart || node.equals(cycleStart);
      }
      if(seen.size() != g.nodeCount())
	 throw new DecodeFailure();
   }

}

