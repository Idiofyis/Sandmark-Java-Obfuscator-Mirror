package sandmark.util.opaquepredicatelib;


/** Abstract superclass of all opaque predicate generator
 *  classes. In general, an opaque predicate will ensure that
 *  a 1 or 0 is on top of the stack upon reaching a specified
 *  InstructionHandle.
 */
abstract public class OpaquePredicateGenerator {
   public abstract boolean canInsertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,
       int valueType);

   /** Inserts an opaque predicate into the given method,
    *  so that a 1 or 0 will be on top of the stack just before
    *  the given InstructionHandle.
    */
   public abstract void insertPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,
       int valueType);


   /** This method attempts to insert an inter-procedural
    *  opaque predicate that will put a 1 or 0 on top of 
    *  the stack just before the given InstructionHandle of
    *  the given method. It computes the ApplicationCFG for 
    *  the method's application to find interprocedural dominator
    *  blocks, into which code can be added that will compute
    *  the opaque predicate. 
    *  NOTE: the default implementation of this method is to
    *  throw an UnsupportedOperationException. Any new OP class
    *  will have to explicitly override this method to add 
    *  interprocedural functionality.
    */
   public void insertInterproceduralPredicate
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle ih,
       int valueType){

      throw new UnsupportedOperationException("Interprocedural predicate not supported");
      // this is the default, specific OPs will have to override this
   }


   /** Returns blocks in order of dominance (i.e. block[0] will dominate all others).
    *  (this method filters out the sources and sinks)
    */
   protected final sandmark.analysis.controlflowgraph.BasicBlock[]
      findInterproceduralDominators
      (sandmark.program.Method method, 
       org.apache.bcel.generic.InstructionHandle insertBefore){

      sandmark.analysis.callgraph.ApplicationCFG appcfg = 
         new sandmark.analysis.callgraph.ApplicationCFG(method.getApplication());
      sandmark.analysis.controlflowgraph.MethodCFG cfg = method.getCFG();
      sandmark.analysis.controlflowgraph.BasicBlock bb = cfg.getBlock(insertBefore);
      
      Object MAGIC_NODE = new Object();
      for (java.util.Iterator rootiter=appcfg.roots();rootiter.hasNext();)
         appcfg.addEdge(MAGIC_NODE, rootiter.next());
      
      sandmark.util.newgraph.DomTree domtree = appcfg.dominatorTree(MAGIC_NODE);
      java.util.LinkedList domlist = new java.util.LinkedList();

      for (java.util.Iterator nodes=domtree.dominators(bb);nodes.hasNext();){
         Object next = nodes.next();
         if ((next instanceof sandmark.analysis.controlflowgraph.BasicBlock) &&
             next!=bb && !isInLoop(next, appcfg)){
            if (((sandmark.analysis.controlflowgraph.BasicBlock)next).getInstList().size()!=0)
               domlist.addFirst(next);
         }
      }

      return 
         (sandmark.analysis.controlflowgraph.BasicBlock[])
         domlist.toArray(new sandmark.analysis.controlflowgraph.BasicBlock[0]);
   }

   /** Same as above, but spreads the blocks out into 'howmany' spaces.
    */
   protected final sandmark.analysis.controlflowgraph.BasicBlock[]
      findInterproceduralDominators
      (sandmark.program.Method method,
       org.apache.bcel.generic.InstructionHandle insertBefore,
       int howmany){
      
      sandmark.analysis.controlflowgraph.BasicBlock[] domlist = 
         findInterproceduralDominators(method, insertBefore);
      if (domlist.length==0)
         return null;

      sandmark.analysis.controlflowgraph.BasicBlock[] blocks = 
         new sandmark.analysis.controlflowgraph.BasicBlock[howmany];

      java.util.Random random = sandmark.util.Random.getRandom();
      int[] indexes=new int[howmany];
      for (int i=0;i<howmany;i++)
         indexes[i] = random.nextInt(domlist.length);
      java.util.Arrays.sort(indexes);
      for (int i=0;i<howmany;i++)
         blocks[i] = domlist[indexes[i]];

      return blocks;
   }

   /** Determines if the given object in the given graph
    *  is in a directed loop.
    */
   private boolean isInLoop(Object source, sandmark.analysis.callgraph.ApplicationCFG appcfg){
      java.util.HashSet visited = new java.util.HashSet();
      java.util.LinkedList queue = new java.util.LinkedList();

      for (java.util.Iterator outedges=appcfg.outEdges(source);outedges.hasNext();){
         sandmark.util.newgraph.Edge edge = (sandmark.util.newgraph.Edge)outedges.next();
         if (edge.sinkNode()==source)
            return true;
         queue.add(edge.sinkNode());
      }

      while(!queue.isEmpty()){
         Object next = queue.removeFirst();
         if (visited.contains(next))
            continue;
         visited.add(next);
         for (java.util.Iterator outedges=appcfg.outEdges(next);outedges.hasNext();){
            Object sink = ((sandmark.util.newgraph.Edge)outedges.next()).sinkNode();
            if (sink==source)
               return true;
            queue.add(sink);
         }
      }
      return false;
   }
}
