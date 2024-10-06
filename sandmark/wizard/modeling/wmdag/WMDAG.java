package sandmark.wizard.modeling.wmdag;

public class WMDAG implements sandmark.wizard.modeling.Model,
                              sandmark.wizard.ChoiceRunListener {

   sandmark.util.newgraph.MutableGraph myGraph = 
      new sandmark.util.newgraph.MutableGraph();
   sandmark.Algorithm[] seq;
   int curpos;
   sandmark.program.Application app;

   public void init(sandmark.wizard.evaluation.Evaluator e,
                    sandmark.wizard.ChoiceRunner r,
                    sandmark.wizard.ObjectProvider op,
                    sandmark.wizard.AlgorithmProvider ap){
      sandmark.Algorithm algs[] = ap.getAlgorithms();
      filter(op);
      this.app = (sandmark.program.Application)op.next();
      WMDAGNode root = new WMDAGNode(null,0,new java.util.ArrayList());
      myGraph.addNode(root);
      java.util.Hashtable postProhibs = buildPostprohibAlgs(algs);
      construct(root,algs,postProhibs);
      constructGraph(algs);
      seq = (sandmark.Algorithm [])
         findLongestPath
         (new java.util.ArrayList(),new java.util.HashSet(),0).toArray
         (new sandmark.Algorithm[0]);
   }
   
   private void constructGraph(sandmark.Algorithm algs[]) {
      java.util.Hashtable modPropToAlgs = new java.util.Hashtable();
      java.util.Hashtable classToAlg = new java.util.Hashtable();
      for(int i = 0 ; i < algs.length ; i++) {
         sandmark.config.ModificationProperty props[] = algs[i].getMutations();
         for(int j = 0 ; props != null && j < props.length ; j++) {
            java.util.List modList = (java.util.List)modPropToAlgs.get(props[j]);
            if(modList == null) {
               modList = new java.util.ArrayList();
               modPropToAlgs.put(props[j],modList);
            }
            classToAlg.put(algs[i].getClass(),algs[i]);
            modList.add(algs[i]);
         }
         myGraph.addNode(algs[i]);
      }
      
      for(int i = 0 ; i < algs.length ; i++) {
         sandmark.config.RequisiteProperty props[] = 
            algs[i].getPostprohibited();
         for(int j = 0 ; props != null && j < props.length ; j++) {
            if(props[j] instanceof sandmark.config.AlgorithmProperty) {
               sandmark.config.AlgorithmProperty ap =
                  (sandmark.config.AlgorithmProperty)props[j];
               sandmark.Algorithm prohibAlg = 
                  (sandmark.Algorithm)classToAlg.get(ap.getAlgorithm());
               if(prohibAlg != null)
                  myGraph.addEdge(algs[i],prohibAlg);
            } else if(props[j] instanceof 
                      sandmark.config.ModificationProperty) {
               java.util.List providers = 
                  (java.util.List)modPropToAlgs.get(props[j]);
               if(providers != null)
                  for(java.util.Iterator it = providers.iterator() ; 
                      it.hasNext() ; )
                     myGraph.addEdge(algs[i],it.next());
            } else
               assert false;
         }
      }  
   }
   
   private java.util.ArrayList findLongestPath(java.util.ArrayList path,
                                               java.util.HashSet postProhibs,
                                               int longestPathLength) {
      java.util.ArrayList longestPath = (java.util.ArrayList)path.clone();
      RECURSE: for(java.util.Iterator nodes = myGraph.nodes() ; 
                   nodes.hasNext() ; ) {
         Object node = nodes.next();
         if(path.contains(node))
            continue;
         for(java.util.Iterator pathIt = path.iterator() ; 
             pathIt.hasNext() ; ) {
            Object pathElem = pathIt.next();
            if(myGraph.hasEdge(pathElem,node))
               continue RECURSE;
         }
         java.util.HashSet nodePostProhibs = 
            (java.util.HashSet)postProhibs.clone();
         for(java.util.Iterator succs = myGraph.succs(node) ; 
             succs.hasNext() ; )
            nodePostProhibs.add(succs.next());
         if(longestPathLength >= path.size() + myGraph.nodeCount() - 
            nodePostProhibs.size())
            continue;
         java.util.ArrayList tmpPath = (java.util.ArrayList)path.clone();
         tmpPath.add(node);
         java.util.ArrayList longPath = 
            findLongestPath(tmpPath,nodePostProhibs,longestPath.size());
         if(longPath.size() > longestPath.size())
            longestPath = longPath;
      }
      return longestPath;
   }
   
   private java.util.HashSet mListeners = new java.util.HashSet();
   public void addModelChangeListener
      (sandmark.wizard.modeling.ModelChangeListener l) { mListeners.add(l); }
   public void removeModelChangeListener
      (sandmark.wizard.modeling.ModelChangeListener l) { mListeners.remove(l); }
   
   public boolean hasChoices() { return curpos != seq.length; }
   public boolean isTerminationPoint() { return true; }
   public int getChoiceCount() { return hasChoices() ? 1 : 0; }
   public int getTerminationChoiceCount() { return getChoiceCount(); }
   public sandmark.wizard.modeling.Choice getChoiceAt(int i) 
   { return new sandmark.wizard.modeling.Choice(seq[curpos],app); }
   public sandmark.wizard.modeling.Choice getTerminationChoiceAt(int i)
   { return getChoiceAt(i); }
   public sandmark.wizard.modeling.Choice [] getChoicesAt(int m,int n)
   { return new sandmark.wizard.modeling.Choice[] { getChoiceAt(0) }; }
   public sandmark.wizard.modeling.Choice [] getTerminationChoicesAt(int m,int n)
   { return getChoicesAt(m,n); }
   public void ranChoice(sandmark.wizard.modeling.Choice c) { 
      assert c.getAlg() == seq[curpos]; 
      curpos++;
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         sandmark.wizard.modeling.ModelChangeListener l =
            (sandmark.wizard.modeling.ModelChangeListener)it.next();
         l.modelChanged();
      }
   }
   
   public java.util.Iterator choices() { 
      return new sandmark.util.SingleObjectIterator(getChoiceAt(0));
   }
   public java.util.Iterator terminationChoices() { return choices(); }
   public sandmark.util.newgraph.Graph getVisualization() { return myGraph.graph(); }
   public sandmark.Algorithm [] getSequence() { return seq; }

   public void filter(sandmark.wizard.AlgorithmProvider ap) {}
   public void filter(sandmark.wizard.ObjectProvider op) {
      while(op.hasNext())
         if(!(op.next() instanceof sandmark.program.Application))
            op.remove();
      op.reset();
   }
   

   private void findLongestSeq(sandmark.Algorithm algs[]){
      //java.util.Iterator leaves = myGraph.reverseRoots();
      java.util.Iterator leaves = myGraph.nodes();
      //System.out.println("num nodes in graph: " + myGraph.nodeCount());
      sandmark.wizard.modeling.wmdag.WMDAGNode longestLeaf = null;
      int longestPath = 0;
      while(leaves.hasNext()){
         sandmark.wizard.modeling.wmdag.WMDAGNode leaf =
            (sandmark.wizard.modeling.wmdag.WMDAGNode)leaves.next();
         //System.out.println("leaf: " + leaf);
         if(leaf.getLevel() > longestPath){
            longestLeaf = leaf;
            longestPath = leaf.getLevel();
         }
      }

      seq = new sandmark.Algorithm[longestPath];
      sandmark.wizard.modeling.wmdag.WMDAGNode node = longestLeaf;
      for(int i = longestPath -1; i >= 0; i--){
         seq[i] = node.getAlg();
         java.util.Iterator parents = myGraph.preds(node);
         if(parents.hasNext())
            node = (sandmark.wizard.modeling.wmdag.WMDAGNode)parents.next();
      }
   }
 
   private void construct
      (sandmark.wizard.modeling.wmdag.WMDAGNode currentNode,
       sandmark.Algorithm algs[],java.util.Hashtable postProhibs) {
      java.util.ArrayList possAlgs = 
         findPossAlgs(currentNode.getPostprohibits(),algs);
      if(possAlgs.size() == 0)
         return;
      
         int currentLevel = currentNode.getLevel() + 1;
         for(int i=0; i < possAlgs.size(); i++){
            java.util.ArrayList newProhibits = new java.util.ArrayList();
            newProhibits.addAll(currentNode.getPostprohibits());
            java.util.HashSet newPostProhibits = 
               (java.util.HashSet)postProhibs.get(possAlgs.get(i));
            newProhibits.addAll(newPostProhibits);

            if(currentNode.getPostprohibits().size() != newProhibits.size()) {

               if(possAlgs.get(i) != currentNode.getAlg()){
                  sandmark.wizard.modeling.wmdag.WMDAGNode newNode = new
                     sandmark.wizard.modeling.wmdag.WMDAGNode(
                     (sandmark.Algorithm)possAlgs.get(i),
                     currentLevel, newProhibits);
                  myGraph.addNode(newNode);
                  myGraph.addEdge(currentNode, newNode);
                  System.out.println("added edge " + currentNode.getAlg() + " " +
                     currentNode.getLevel() + ", "
                     + newNode.getAlg() + " " + newNode.getLevel() + " " + myGraph.nodeCount());
                  construct(newNode,algs,postProhibs);
               }
            }
         }
   }

   private java.util.ArrayList findPossAlgs
      (java.util.ArrayList postProhibs,sandmark.Algorithm algs[]){
      java.util.ArrayList possAlgs = 
         new java.util.ArrayList(java.util.Arrays.asList(algs));
      possAlgs.removeAll(postProhibs);
      return possAlgs;
   }
   
   private java.util.Hashtable buildPostprohibInfo(sandmark.Algorithm algs[]) {
      java.util.Hashtable ppi = new java.util.Hashtable();
      for(int i = 0 ; i < algs.length ; i++) {
         sandmark.config.ModificationProperty props[] =
            algs[i].getMutations();
         for(int j = 0 ; props != null && j < props.length ; j++) {
            java.util.HashSet providers = (java.util.HashSet)ppi.get(props[j]);
            if(providers == null) {
               providers = new java.util.HashSet();
               ppi.put(props[j],providers);
            }
            providers.add(algs[i]);
         }
      }
      return ppi;
   }
   
   private java.util.Hashtable buildClassToAlgs(sandmark.Algorithm algs[]) {
      java.util.Hashtable ht = new java.util.Hashtable();
      for(int i = 0 ; i < algs.length ; i++) {
         java.util.HashSet a = (java.util.HashSet)ht.get(algs[i].getClass());
         if(a == null) {
            a = new java.util.HashSet();
            ht.put(algs[i].getClass(),a);
         }
         a.add(algs[i]);
      }
      return ht;
   }
   
   private java.util.Hashtable buildPostprohibAlgs(sandmark.Algorithm algs[]) {
      java.util.Hashtable ppa = new java.util.Hashtable();
      java.util.Hashtable ppi = buildPostprohibInfo(algs);
      java.util.Hashtable classToAlgs = buildClassToAlgs(algs);
      for(int i = 0 ; i < algs.length ; i++) {
         java.util.HashSet postProhib = new java.util.HashSet();
         postProhib.add(algs[i]);
         sandmark.config.RequisiteProperty props[] = algs[i].getPostprohibited();
         for(int j = 0 ; props != null && j < props.length ; j++) {
            if(props[j] instanceof sandmark.config.ModificationProperty) {
               java.util.HashSet p = (java.util.HashSet)ppi.get(props[j]);
               if(p != null)
                  postProhib.addAll(p);
            } else if(props[j] instanceof sandmark.config.AlgorithmProperty) {
               Class alg = 
                  ((sandmark.config.AlgorithmProperty)props[j]).getAlgorithm();
               java.util.HashSet hs = (java.util.HashSet)classToAlgs.get(alg);
               if(hs != null)
                  postProhib.addAll(hs);
            } else assert false;
         }
         ppa.put(algs[i],postProhib);
      }
      return ppa;
   }
}
