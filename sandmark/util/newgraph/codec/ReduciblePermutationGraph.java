package sandmark.util.newgraph.codec;

public class ReduciblePermutationGraph extends AbstractCodec {

   private class BackEdge implements Comparable {
      public int prev, curr;

      public BackEdge(int _prev, int _curr) {
	 prev = _prev;
	 curr = _curr;
      }

      public int compareTo(Object o) {
	 BackEdge e = (BackEdge)o;
	 return prev - e.prev;
      }

      public boolean equals(Object o) {
	 BackEdge e = (BackEdge)o;
	 return prev == e.prev;
      }
   }

   public int maxOutDegree() { return 2; }

   private int preambleCount = -1;
   public int lastPreambleNodeCount() {
      return preambleCount;
   }

   public sandmark.util.newgraph.Graph encode(java.math.BigInteger val,
					      sandmark.util.newgraph.NodeFactory factory) {
      java.util.Map p = encode(val, 1);

      sandmark.util.newgraph.Graph g = 
	 sandmark.util.newgraph.Graphs.createGraph(null, null);
      java.lang.Object root = factory.createNode(),
	  cycleRoot = factory.createNode();
      sandmark.util.newgraph.Edge tmp =
	 new sandmark.util.newgraph.EdgeImpl(root, cycleRoot);
      g = g.addEdge(tmp);
      
      java.lang.Object [] nodes = new java.lang.Object[p.size()];
      nodes[0] = factory.createNode();
      g = g.addEdge(cycleRoot, nodes[0]);
      for (int i = 1; i < nodes.length; i++) {
	 nodes[i] = factory.createNode();
	 g = g.addEdge(nodes[i-1], nodes[i]);
      }

      java.lang.Object sink = factory.createNode();
      g = g.addEdge(nodes[nodes.length-1], sink);

      int prev = ((Integer)p.get(new Integer(0))).intValue();
      if (prev == 0)
	 g = g.addEdge(cycleRoot, sink);
      else
	 g = g.addEdge(cycleRoot, nodes[prev]);
      for (int i = 1; i < p.size(); i++) {
	 int curr = ((Integer)p.get(new Integer(i))).intValue();
	 if (curr == prev+1)
	    g = g.addEdge(nodes[prev], sink);
	 else if (curr > prev)
	    g = g.addEdge(nodes[prev], nodes[curr]);
	 prev = curr;
      }
      g = g.addEdge(nodes[prev], cycleRoot);

      java.util.Vector backEdges = new java.util.Vector();
      sandmark.util.newgraph.Graph d = g.dominatorTree(root);
      prev = ((Integer)p.get(new Integer(0))).intValue();
      for (int i = 1; i < p.size(); i++) {
	 int curr = ((Integer)p.get(new Integer(i))).intValue();
	 if (curr < prev) {
	    if (d.reachable(nodes[curr], nodes[prev]))
	       g = g.addEdge(nodes[prev], nodes[curr]);
	    else
	       backEdges.add(new BackEdge(prev, curr));
	 }
	 prev = curr;
      }

      java.util.Collections.sort(backEdges);
      java.lang.Object [] extraRoots = new java.lang.Object[backEdges.size()];
      int max = -1;
      for (int i = 1; i < backEdges.size(); i++) {
	 BackEdge be = (BackEdge)backEdges.get(i);
	 int count = 0;
	 for (int j = 0; j < i; j++) {
	    BackEdge curr = (BackEdge)backEdges.get(j);
	    if (be.curr < curr.curr)
	       count++;
	 }
	 if (count > 0) {
	    count--;
	    if (count > max)
	       max = count;
	    for (int j = 0; j <= count; j++)
	       if (extraRoots[j] == null) {
		  extraRoots[j] = factory.createNode();
	       }
	    g = g.addEdge(nodes[be.prev], extraRoots[count]);
	 }
      }
      if (max >= 0) {
	 g = g.addEdge(root, extraRoots[max]);
	 for (int i = max; i > 0; i--)
	    g = g.addEdge(extraRoots[i], extraRoots[i-1]);
	 g = g.addEdge(extraRoots[0], cycleRoot);
	 g = g.removeEdge(tmp);
	 preambleCount = max;
      }
      else
	 preambleCount = 0;

      return g.consolidate();
   }

   private class Decoder {
      private int [] list;
      private int [] preds;
      private int [] succs;
      private java.util.TreeSet available;
      private int cycleStart;

      public java.util.HashMap getPermutation() {
	 java.util.HashMap p = new java.util.HashMap();
	 for (int i = cycleStart + 1; i < list.length-1; i++)
	    p.put(new Integer(i-cycleStart-1), 
		  new Integer(list[i]-cycleStart-1));
	 return p;
      }

      public Decoder(sandmark.util.newgraph.Graph g,
		     java.lang.Object [] hamPathOrder,
		     java.util.HashMap hamPathInverse)
	 throws DecodeFailure {
	 list = new int[hamPathOrder.length];
	 preds = new int[hamPathOrder.length];
	 succs = new int[hamPathOrder.length];
	 available = new java.util.TreeSet();

	 cycleStart = -1;
	 for (int i = 0; cycleStart < 0; i++) {
            if(i >= hamPathOrder.length)
               throw new DecodeFailure();
	    java.util.Iterator edges = g.outEdges(hamPathOrder[i]);
	    int count = 0;
	    while (edges.hasNext()) {
	       edges.next();
	       count++;
	    }
	    if (count > 1)
	       cycleStart = i;
	 }
// 	 System.err.println("cycleStart = " + cycleStart);

	 for (int i = cycleStart + 1; i < hamPathOrder.length - 1; i++)
	    available.add(new Integer(i));
	 for (int i = 0; i < hamPathOrder.length; i++)
	    preds[i] = succs[i] = -1;

	 for (int i = 0; i <= cycleStart; i++)
	    list[i] = i;
	 for (int i = cycleStart + 1; i < list.length - 1; i++)
	    list[i] = -1;
	 list[list.length-1] = list.length-1;

	 java.util.Iterator edges = g.edges();
	 while (edges.hasNext()) {
	    sandmark.util.newgraph.Edge e =
	       (sandmark.util.newgraph.Edge)edges.next();
	    java.lang.Object source = e.sourceNode();
	    java.lang.Object sink = e.sinkNode();
	    int sourceIndex =
	       ((Integer)hamPathInverse.get(source)).intValue();
	    int sinkIndex =
	       ((Integer)hamPathInverse.get(sink)).intValue();
	    if (sinkIndex >= cycleStart) {
	       if (sinkIndex != sourceIndex + 1) {
		  if (sinkIndex == list.length - 1)
		     addSuccessor(sourceIndex, sourceIndex + 1, 
				  list, preds, succs, available);
		  else if (sinkIndex == cycleStart)
		     addSuccessor(sourceIndex, list.length - 1, 
				  list, preds, succs, available);
		  else
		     addSuccessor(sourceIndex, sinkIndex, 
				  list, preds, succs, available);
	       }
	    }
	 }

	 java.util.Vector dangling = new java.util.Vector(),
	    waiting = new java.util.Vector();
	 for (int i = cycleStart + 1; i < hamPathOrder.length - 1; i++) {
	    edges = g.inEdges(hamPathOrder[i]);
	    int count = 0;
	    while (edges.hasNext()) {
	       edges.next();
	       count++;
	    }
	    if (g.hasEdge(hamPathOrder[i-1], 
			  hamPathOrder[hamPathOrder.length-1]))
	       count++;
	    if (count == 1)
	       waiting.add(new Integer(i));

	    edges = g.outEdges(hamPathOrder[i]);
	    count = 0;
	    int pos = -1;
	    java.util.Iterator succs = g.succs(hamPathOrder[i]);
	    while (pos < 0 && succs.hasNext()) {
	       int destIndex =
		  ((Integer)hamPathInverse.get(succs.next())).intValue();
	       if (destIndex < cycleStart)
		  pos = cycleStart - destIndex;
	       else
		  count++;
	    }
	    if (count <= 1) {
	       if (pos < 0)
		  dangling.add(new Integer(i));
	       else {
		  int index = dangling.size() - pos;
		  dangling.insertElementAt(new Integer(i), index);
	       }
	    }

// 	    System.err.println("dangling = " + dangling);
// 	    System.err.println("waiting = " + waiting);
	 }

         if(waiting.size() < dangling.size())
            throw new DecodeFailure();

	 for (int i = 0; i < dangling.size(); i++) {
	    int source = 
	       ((Integer)dangling.get(i)).intValue();
	    int sink =
	       ((Integer)waiting.get(i)).intValue();
	    addSuccessor(source, sink,
			 list, preds, succs, available);
	 }
      }

      private void addSuccessor(int from, int to, 
				int [] list, int [] preds, int [] succs,
				java.util.SortedSet available) 
	 throws DecodeFailure {
	 for (int i = cycleStart; i < list.length-2; i++)
	    if (list[i] == from) {
// 	       System.err.println("inserting " + to + " in " + (i+1));
	       insert(i+1, to, list, preds, succs, available);
	       return;
	    }

	 for (int i = cycleStart + 2; i < list.length; i++)
	    if (list[i] == to) {
// 	       System.err.println("inserting " + from + " in " + (i-1));
	       insert(i-1, from, list, preds, succs, available);
	       return;
	    }

	 available.remove(new Integer(to));
	 preds[to] = from;
	 succs[from] = to;
      }

      private boolean insert(int index, int value,
			     int [] list, int [] preds, int [] succs,
			     java.util.SortedSet available) 
	 throws DecodeFailure {
	 if (list[index] == -1) {
	    available.remove(new Integer(value));
	    list[index] = value;
	    if (preds[value] != -1) {
	       int tmp = preds[value];
	       preds[value] = -1;
	       if (!insert(index-1, tmp, list, preds, succs, available))
		  throw new DecodeFailure("something bad");
	    }
	    if (succs[value] != -1) {
	       int tmp = succs[value];
	       succs[value] = -1;
	       if (!insert(index+1, tmp, list, preds, succs, available))
		  throw new DecodeFailure("something bad");
	    }
	    insert(value, index, list, preds, succs, available);
	    return true;
	 }
	 else if (list[index] == value)
	    return true;
	 else
	    return false;
      }
   }

   public java.math.BigInteger decode(sandmark.util.newgraph.Graph g)
      throws DecodeFailure {
      g = g.removeMultipleEdges().consolidate();

      // check outdegree
      if (g.maxOutDegree() > 2)
	  throw new DecodeFailure("not a permutation graph: " + 
 				 "out degree too large");
      if (g.nodeCount() < 4)
          throw new DecodeFailure("not a permutation graph: not enough nodes");

      // find root
      java.util.Iterator iter = g.roots();
      
      if(!iter.hasNext())
	  throw new DecodeFailure("not a permutation graph: no roots");

      java.lang.Object root = iter.next();
      if (iter.hasNext())
	 throw new DecodeFailure("not a permutation graph: multiple roots");

      // check that the graph is reducible
      sandmark.util.newgraph.Graph d =
	 g.dominatorTree(root);
      if (!sandmark.util.newgraph.Graphs.reducible(g, root, d))
	 throw new DecodeFailure("graph is irreducible");

      // look for hamiltonian path in acyclic component
      sandmark.util.newgraph.Graph h = g;
      java.util.Iterator edgeIter = h.edges();
      while (edgeIter.hasNext()) {
	 sandmark.util.newgraph.Edge e =
	    (sandmark.util.newgraph.Edge)edgeIter.next();
	 if (d.reachable(e.sinkNode(), e.sourceNode()))
	    h = h.removeEdge(e);
      }
      java.util.List hp = h.acyclicHamiltonianPath(root);
      if (hp == null) {
	 throw new DecodeFailure("no Hamiltonian path found");
      }
      java.util.HashMap orderInverse = new java.util.HashMap();
      java.lang.Object hamPathOrder[] = hp.toArray();
      for (int i = 0; i < hamPathOrder.length; i++) {
	 orderInverse.put(hamPathOrder[i], new Integer(i));
      }

      // find permutation
      Decoder decoder = new Decoder(g, hamPathOrder, orderInverse);
      java.util.Map p = decoder.getPermutation();

//       System.err.println(p);
      
      return decodePermutation(p);
   }   

   /**
    * Returns the number of permutations on n elements of order 1 or 2.
    *
    * @param n the number of elements to permute
    * @return the number of possible permutations of order 1 or 2
    */
   private static java.math.BigInteger numPerms(int n) {
      java.math.BigInteger current = java.math.BigInteger.ONE;
      java.math.BigInteger sum = current;

      for (int k = 1; 2*k <= n; k++) {
	 java.math.BigInteger numFactor = 
	    java.math.BigInteger.valueOf((n-2*k+2)*(n-2*k+1));
	 java.math.BigInteger demFactor = java.math.BigInteger.valueOf(2*k);
	 current = current.multiply(numFactor);
	 current = current.divide(demFactor);
	 sum = sum.add(current);
      }

      return sum;
   }

   /**
    * Returns the number of permutations on n elements of order 1 or
    * 2 consisting of exactly k disjoint 2-cycles.
    *
    * @param n the number of elements to permute
    * @param k the number of disjoint 2-cycles in the permutations
    * @return the number of possible permutations of order 1 or 2
    */
   private static java.math.BigInteger numPerms(int n, int k) {
      java.math.BigInteger current = java.math.BigInteger.ONE;
      
      for (int i = 1; i <= k; i++) {
	 java.math.BigInteger numFactor =
	    java.math.BigInteger.valueOf((n-2*i+2)*(n-2*i+1));
	 java.math.BigInteger demFactor = java.math.BigInteger.valueOf(2*i);
	 current = current.multiply(numFactor);
	 current = current.divide(demFactor);
      }

      return current;
   }

   private static java.util.Map getPermutation(java.math.BigInteger value, 
					       int n, int k) {
      java.math.BigInteger product = java.math.BigInteger.ONE;
      for (int i = 1; i <= k; i++)
	 product = product.multiply(java.math.BigInteger.valueOf(2*i-1));

      java.math.BigInteger [] split = value.divideAndRemainder(product);

      java.util.Collection tempcoll = sandmark.util.Math.getCombination(split[0], n, 2*k);
      if (tempcoll==null)
         tempcoll=new java.util.ArrayList();

      java.util.List cycleElements =
         new java.util.ArrayList(tempcoll);
          

      java.util.Map cycles = getCycles(split[1], k);

      java.util.Map result = new java.util.HashMap();
      for (int i = 0; i < 2*k; i++) {
	 Integer ii = new Integer(i);
	 Object output = 
	    cycleElements.get(((Integer)cycles.get(ii)).intValue());
	 result.put(cycleElements.get(i), output);
      }
      for (int i = 0; i < n; i++) {
	 Integer input = new Integer(i);
	 if (!result.containsKey(input))
	    result.put(input, input);
      }
	
      return result;
   }

   private static java.util.Map getCycles(java.math.BigInteger value,
					  int numCycles) {
      java.util.List remaining = new java.util.LinkedList();
      for (int i = 0; i < 2*numCycles; i++)
	 remaining.add(new Integer(i));

      int [] cycles = new int[2*numCycles];
      for (int i = 0; i < numCycles; i++) {
	 cycles[2*i] = ((Integer)remaining.remove(0)).intValue();
	 java.math.BigInteger base =
	    java.math.BigInteger.valueOf(2*(numCycles-i)-1);
	 java.math.BigInteger [] split = value.divideAndRemainder(base);
	 value = split[0];
	 cycles[2*i+1] = 
	    ((Integer)remaining.remove(split[1].intValue())).intValue();
      }

      java.util.Map result = new java.util.HashMap();
      for (int i = 0; i < numCycles; i++) {
	 result.put(new Integer(cycles[2*i]), new Integer(cycles[2*i+1]));
	 result.put(new Integer(cycles[2*i+1]), new Integer(cycles[2*i]));
      }

      return result;
   }

   private static java.math.BigInteger decodeCycles(int [] cycles) {
      java.util.List remaining = new java.util.LinkedList();
      for (int i = 0; i < cycles.length; i++)
	 remaining.add(new Integer(i));
      java.math.BigInteger base = java.math.BigInteger.ONE,
	 sum = java.math.BigInteger.ZERO;

      for (int i = 0; i < cycles.length/2; i++) {
	 if (((Integer)remaining.get(0)).intValue() != cycles[2*i])
	    throw new RuntimeException("invalid cycles");
	 remaining.remove(0);
	 int index = remaining.indexOf(new Integer(cycles[2*i+1]));
	 if (index == -1)
	    throw new RuntimeException("invalid cycles");
	 sum = sum.add(base.multiply(java.math.BigInteger.valueOf(index)));
	 base = base.multiply(java.math.BigInteger.valueOf(remaining.size()));
	 remaining.remove(index);
      }

      return sum;
   }

   private static java.math.BigInteger decodePermutation(java.util.Map p) 
      throws DecodeFailure {
      int n = p.size();
      java.util.Map cycleElements = new java.util.HashMap();

      for (int i = 0; i < n; i++) {
	 Object pi = p.get(new Integer(i));
	 if (pi == null || !(pi instanceof Integer))
	    throw new DecodeFailure("not a permutation");

	 if (((Integer)pi).intValue() != i)
	    cycleElements.put(new Integer(i), 
			      new Integer(cycleElements.size()));
	 
	 Object ppi = p.get(pi);
	 if (ppi == null || !(ppi instanceof Integer))
	    throw new DecodeFailure("not a permutation");
	 if (((Integer)ppi).intValue() != i)
	    throw new DecodeFailure("permutation is of order greater than 2");
      }

      int k = cycleElements.size()/2;

      java.math.BigInteger combValue = 
	 sandmark.util.Math.decodeCombination(cycleElements.keySet(), n);

      int [] cycles = new int[2*k];
      int current = 0;
      for (int i = 0; i < n; i++) {
	 Integer ii = new Integer(i);
	 Integer output = (Integer)p.get(ii);
	 if (i < output.intValue()) {
	    cycles[current++] = ((Integer)cycleElements.get(ii)).intValue();
	    cycles[current++] = 
	       ((Integer)cycleElements.get(output)).intValue();
	 }
      }

      java.math.BigInteger permValue = decodeCycles(cycles);

      java.math.BigInteger base = java.math.BigInteger.ONE;
      for (int i = 1; i <= k; i++)
	 base = base.multiply(java.math.BigInteger.valueOf(2*i-1));

      java.math.BigInteger offset = java.math.BigInteger.ZERO;
      for (int i = 1; i < n; i++)
	 offset = offset.add(numPerms(i));
      for (int i = 0; i < k; i++)
	 offset = offset.add(numPerms(n, i));

      return combValue.multiply(base).add(permValue).add(offset);
   }

   private static java.util.Map encode(java.math.BigInteger value, int n) {
      java.math.BigInteger perms = numPerms(n);
      if (value.compareTo(perms) >= 0)
	 return encode(value.subtract(perms), n+1);
      else
	 return encode(value, n, 0);
   }

   private static java.util.Map encode(java.math.BigInteger value, 
				       int n, int k) {
      java.math.BigInteger perms = numPerms(n, k);
      if (value.compareTo(perms) >= 0)
	 return encode(value.subtract(perms), n, k+1);
      else
	 return getPermutation(value, n, k);
   }

   public static void main(String [] argv) throws Exception {
      new ReduciblePermutationGraph().test(argv);
   }
}

