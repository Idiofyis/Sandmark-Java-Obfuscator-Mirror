package sandmark.wizard.modeling.dfa;

public class DFA extends sandmark.util.newgraph.MutableGraph
{
   private static final boolean DEBUG = true;
    protected DFANode myStartState;

    //mapping of application object A to an arraylist of application objects in A's range
    protected java.util.HashMap rangeMap;

   //used for dfa intersection
    private DFA(java.util.HashMap rangeMap){ this.rangeMap = rangeMap; }

    /**
       Constructs common structure between every dfa type.
       Initializes the set of states to be the powerset of the set of all
       application objects that are a target of alg1.
       @param alg1 the algorithm that is involved in the dependency
       @param accept initial value of every state's accept field
    */
    protected DFA(java.util.HashMap rangeMap,
                  sandmark.Algorithm alg1, sandmark.config.RequisiteProperty prop,
                         sandmark.Algorithm [] allAlgs,
                         sandmark.program.Object[] allObjects, boolean accept)
    {
       this.rangeMap = rangeMap;
       
        //The set of states is composed of the powerset of all application objects that are
        //a target of alg1
        java.util.ArrayList targetSet = new java.util.ArrayList();
        for(int i = 0; i < allObjects.length; i++){
            if(isTargetOf(allObjects[i],alg1))
               targetSet.add(allObjects[i]);
        }
	
        //build the nodes
        java.util.HashMap stateMap = new java.util.HashMap();
        java.util.ArrayList[]powerset = getPowerset(targetSet.toArray());
        for(int i = 0; i < powerset.length; i++){
            String label = powerset[i].toString();
	    DFANode state = new DFANode(label, accept);
            addNode(state);
            stateMap.put(state, powerset[i]);
            if(powerset[i].size() == 0)
                myStartState = state;
        }

        java.util.HashMap rtMap = getNodeRangeMap(allObjects,targetSet);

        //build the edges///////////////////////////////////////////////////////
        Object[][]alphabet = getAlphabet(allAlgs, allObjects);

        DFANode states[] = new DFANode[nodeCount()];
        {
           int i = 0;
           for(java.util.Iterator stateIt = nodes() ; stateIt.hasNext() ; i++){
              DFANode state = (DFANode)stateIt.next();
              states[i] = state;
           }
        }
        for(int i = 0 ; i < states.length ; i++) {
           DFANode state = states[i];
            for(int c = 0; c < alphabet.length; c++){

                sandmark.Algorithm a = (sandmark.Algorithm)alphabet[c][0];
                sandmark.program.Object t = (sandmark.program.Object)alphabet[c][1];

                java.util.ArrayList s_q = (java.util.ArrayList)stateMap.get(state);

                java.util.ArrayList r_t = (java.util.ArrayList)rtMap.get(t);

                if(((IntermediateDFA)this).isLoopEdge(alg1, a, t, prop, s_q, r_t)){
                    //then \lambda(q, (a,t))= q
                    DFAEdge edge = new DFAEdge(state, state, a, t);
                    addEdge(edge);
                }

                for(int j = 0 ; j < states.length ; j++){
                   DFANode statePrime = states[j];
                    if(state == statePrime) continue;

                    java.util.ArrayList s_qp = (java.util.ArrayList)stateMap.get(statePrime);

                    if(((IntermediateDFA)this).isNonLoopEdge(alg1, a, t, prop, s_q, s_qp, r_t)){

                        DFAEdge edge = new DFAEdge(state,statePrime,a, t);
                        addEdge(edge);
                    }
                }
            }
        }
    }

    /**
     * Creates a DFA for algs and objects by finding dependencies of algorithms.
     */
    public static DFA createDFA(sandmark.Algorithm [] algs, 
				sandmark.program.Object [] objects)
    {
     	//get range of all objects 
        java.util.HashMap rangeMap = buildRangeMap(objects);

        int restrictionCount = 0;

        DFA model = null;//getUniversalDFA(algs, objects); //if there were no restrictions, this would be it!

        //for each algorithm find postprohibits, preprohibits, prerequisites and postprohibits
	//build DFA by intersecting restriction DFA's.
	for(int a1 = 0; a1 < algs.length; a1++){
            if(DEBUG){
                System.out.println("algorithm " + algs[a1] + "'s dependencies are being processed");
                System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
            }
            sandmark.config.RequisiteProperty[] postprohibs = algs[a1].getPostprohibited();
            if(postprohibs != null){
                for(int p = 0; p < postprohibs.length; p++){
                    if(DEBUG){
                        System.out.println("Constructing postprohibit: " + postprohibs[p]);
                    }
                    DFA restriction = new PostprohibitDFA(rangeMap, algs[a1], postprohibs[p], algs, objects);
		    if(model == null)
                        model = restriction;
                    else
                        model = intersect(restriction, model);
                    restrictionCount++;
                }
            }

            sandmark.config.RequisiteProperty[] preprohibs = algs[a1].getPreprohibited();
            if(preprohibs != null){
                for(int p = 0; p < preprohibs.length; p++){
                    if(DEBUG){
                        System.out.println("Constructing preprohibit");
                    }
                    DFA restriction  = new PreprohibitDFA(rangeMap, algs[a1], preprohibs[p], algs, objects);
		    if(model == null)
                        model = restriction;
                    else
                        model = intersect(restriction, model);
                    restrictionCount++;
                }
            }

            sandmark.config.RequisiteProperty[] prerequisites = algs[a1].getPrerequisites();
            if(prerequisites != null){
                for(int p = 0; p < prerequisites.length; p++){
                    if(DEBUG){
                        System.out.println("Constructing prerequisite");
                    }
                    DFA restriction  = new PrereqDFA(rangeMap, algs[a1], prerequisites[p], algs, objects);
                    if(model == null)
                        model = restriction;
                    else
                        model = intersect(restriction, model);
                    restrictionCount++;
                }
            }

            sandmark.config.RequisiteProperty[] postrequisites = algs[a1].getPostrequisites();
            if(postrequisites != null){
                for(int p = 0; p < postrequisites.length; p++){
                    if(DEBUG){
                        System.out.println("Constructing postrequisite");
                    }
                    DFA restriction  = new PostreqDFA(rangeMap, algs[a1], postrequisites[p], algs, objects);
                    if(model == null)
                        model = restriction;
                    else
                        model = intersect(restriction, model);
                    restrictionCount++;
                }
            }

        }
        System.out.println("Intersections performed:" + restrictionCount);
	model.trim();
	return model;
    }

    private void trim()
    {
        trimUnreachable();
        trimDeadStates();
    }

    public DFANode getStartState()
    {
        return myStartState;
    }

    public String toDot(){
    
        String graphName = "DFA";

        String retVal = "digraph " + graphName + " {\n";
        retVal +=  "graph [page=\"7.5,10\",\n  margin=0, \n ratio=auto, \n pagedir=TL\n];";

        java.util.ArrayList tempList = new java.util.ArrayList();
        for(java.util.Iterator states = nodes() ; states.hasNext() ; ) {
            DFANode node = (DFANode)states.next();
            retVal += "\"" + node.getLabel() + "\"" + " [ shape=" + (node.isAccept()?"doubleoctagon":"ellipse") /*+ ", label= " + "\"" + node.getLabel()*/ + "];\n";
            tempList.add(node);
        }
	
        java.util.HashMap edge2label = new java.util.HashMap();

        for(java.util.Iterator edges = edges() ; edges.hasNext() ; ){
            DFAEdge edge = (DFAEdge)edges.next();
            if(!tempList.contains(edge.getSource()))continue;
            String source = "\"" + edge.getSource().getLabel() + "\"";
            String dest = "\"" + edge.getDestination().getLabel() + "\"";
            String key = source +" -> " + dest;
            String label = (String)edge2label.get(key);
            if(label == null){
                //if(edge.getSource() == edge.getDestination())
                //    label = "";
                //else
                    label = "(" + edge.getAlg().getShortName() + ", " + edge.getTarget() + ")";
            }
            else {//if(edge.getSource() != edge.getDestination()){
                    label += "\\n (" + edge.getAlg().getShortName() + ", " + edge.getTarget() + ")";
            }

            edge2label.put(key, label);
        }
	
	if(DEBUG) System.out.print("label entries start");
        java.util.Iterator edgeItr = edge2label.entrySet().iterator();
        while(edgeItr.hasNext()){
            java.util.Map.Entry edge = (java.util.Map.Entry)edgeItr.next();
            retVal += "             " + edge.getKey().toString() + "[ label=\"" + edge.getValue() + "\"];\n";
        }
	if(DEBUG) System.out.print("label entries done");

        retVal += "}";
        return retVal;
    }

    public void dotInFile(String filename){
        try{
            new java.io.PrintStream(new java.io.FileOutputStream(filename)).println(toDot());
        }catch(java.io.IOException ioe){
            System.out.println("Error printing graph to dot file " + filename + ":" + ioe);
        }
    }

    /**
     * For each object o, if t is a subobject of o or vice versa then o is in r(t)
     */
    protected static java.util.HashMap buildRangeMap(sandmark.program.Object [] objects)
    {
       java.util.HashMap rangeMap = new java.util.HashMap();
        for(int i = 0; i < objects.length; i++){
           sandmark.program.Object t = objects[i];
            java.util.ArrayList temp = new java.util.ArrayList();

            //for each object o, if t is a subobject of o or vice versa
            //then o is in r(t).
            for(int j = 0; j < objects.length; j++){
               sandmark.program.Object s = objects[j];
                if(t == s || isSubobjectOf(s,t) || 
                   isSubobjectOf(t,s))
                    temp.add(s);
            }
            rangeMap.put(t, temp);
        }
        return rangeMap;
    }
    
    public static boolean isSubobjectOf(sandmark.program.Object child,
                                        sandmark.program.Object parent) {
       for( ; child.getParent() != null && child != parent ; 
           child = child.getParent())
          ;
       return parent == child;
    }

    /**
       Returns a list of tuples (algorithm, target) of valid algorithm
       to target pairs.
    */
    protected static Object[][] getAlphabet(sandmark.Algorithm [] allAlgs,
                                            sandmark.program.Object[] allObjs)
    {
        java.util.ArrayList retVal = new java.util.ArrayList();
        for(int a = 0; a < allAlgs.length; a++){
            for(int o = 0; o < allObjs.length; o++){
                if(isTargetOf(allObjs[o],allAlgs[a]))
                    retVal.add(new Object[]{allAlgs[a], allObjs[o]});
            }
        }
        return (Object[][])retVal.toArray(new Object[][]{{}});
    }
    
    public static boolean isTargetOf(sandmark.program.Object obj,
                                      sandmark.Algorithm alg) {
       assert obj instanceof sandmark.program.Application ||
          obj instanceof sandmark.program.Class ||
          obj instanceof sandmark.program.Method;
       
       return (obj instanceof sandmark.program.Application &&
               alg instanceof sandmark.AppAlgorithm) ||
              (obj instanceof sandmark.program.Class &&
               alg instanceof sandmark.ClassAlgorithm) ||
              (obj instanceof sandmark.program.Method &&
               alg instanceof sandmark.MethodAlgorithm);
    }
    
    /**
       Returns an array of arrays, that is the powerset of a given set.
       @param set the given set
       @return the P(set)
    */
    protected static java.util.ArrayList[] getPowerset(Object [] set)
    {
        //hopefully a faster way to do this:
        // - enumerate all of the binary numbers from 0 to 2^n
        // - each number denotes a subset of set, create that
        //   array and add it to the powerset

        int numPowersets = (int)Math.pow(2, set.length);
        //System.out.println("Generating powerset of size: " + numPowersets);
        java.util.ArrayList[] retVal = new java.util.ArrayList[numPowersets];
        //System.out.println("Array created");

        for(int i = 0; i < numPowersets; i++){
            String binary = Integer.toBinaryString(i);

            retVal[i] = new java.util.ArrayList();
            for(int j = binary.length()-1; j >= 0; j--)
                if(binary.charAt(j) == '1')
                    retVal[i].add(set[j+set.length-binary.length()]);

        }
        return retVal;
    }

    //remove all useless states from the graph
    protected void trimUnreachable()
    {
        java.util.ArrayList start = new java.util.ArrayList();
        start.add(myStartState);
        trim(getBFSPathFrom(start, true));
    }

    protected void trimDeadStates()
    {
        java.util.ArrayList start = new java.util.ArrayList();
        for(java.util.Iterator states = nodes() ; states.hasNext() ; ) {
           DFANode state = (DFANode)states.next();
           if(state.isAccept())
               start.add(state);  
        }
        trim(getBFSPathFrom(start, false));
    }

    //trims the dfa to the dfa containing only those nodes
    private void trim(java.util.ArrayList newNodes)
    {
       inducedSubgraph(newNodes.iterator());
    }

    private java.util.ArrayList getBFSPathFrom(java.util.ArrayList startNodes, boolean forward)
    {
        java.util.ArrayList reachables = new java.util.ArrayList();
        java.util.ArrayList queue = new java.util.ArrayList();

        reachables.addAll(startNodes);
        queue.addAll(startNodes);
        while(!queue.isEmpty()){
            DFANode curr = (DFANode)queue.remove(0);
            java.util.Iterator edges = 
               forward ? outEdges(curr) : inEdges(curr);
            while(edges.hasNext()) {
               DFAEdge edge = (DFAEdge)edges.next();
                DFANode target = forward ? edge.getSource() : 
                                           edge.getDestination();
                if(!reachables.contains(target)){
                    reachables.add(target);
                    queue.add(target);
                }
            }
        }
        return reachables;
    }

    protected java.util.HashMap getNodeRangeMap
        (sandmark.program.Object [] allObjs,java.util.ArrayList targetSet)
    {
        java.util.HashMap rtMap = new java.util.HashMap();
        for(int i = 0; i < allObjs.length; i++){

            java.util.ArrayList r_t = new java.util.ArrayList();
            java.util.ArrayList range_t =  (java.util.ArrayList)rangeMap.get(allObjs[i]);

            for(int b = 0; b < range_t.size(); b++){
                if(targetSet.contains(range_t.get(b)))
                    r_t.add(range_t.get(b));
            }
            rtMap.put(allObjs[i], r_t);
        }
        return rtMap;
    }

    protected  boolean hasProp(sandmark.Algorithm a,
                               sandmark.config.RequisiteProperty prop)
    {
        sandmark.config.ModificationProperty[] mutationList = a.getMutations();
        if(mutationList == null)return false;

        //if a either has the property p, or is the alg for prop p
        return (prop instanceof sandmark.config.AlgorithmProperty)?
            ((sandmark.config.AlgorithmProperty)prop).getAlgorithm().equals(a):
            java.util.Arrays.asList(mutationList).contains(prop);
    }


    /**
        Takes two DFA's and returns the DFA that is the intersection of the
        two languages.
        */
    private static DFA intersect(DFA d1, DFA d2)
    {       
       //nested hashtable first is keyed on state from dfa1,
       //which is mapped to a hashtable keyed on state from dfa2,
       //which is mapped to the new state for s1xs2
       java.util.HashMap state1map =
          new java.util.HashMap((int)(d1.nodeCount() * 1.35));
       
       //new set of states are d1.myStates x d2.myStates
       assert d1.rangeMap == d2.rangeMap;
       DFA result = new DFA(d1.rangeMap);
       
       for(java.util.Iterator states1 = d1.nodes() ; states1.hasNext() ; ){
          DFANode state1 = (DFANode)states1.next();
          java.util.HashMap state2map =
             new java.util.HashMap((int)(d2.nodeCount() * 1.35));
          state1map.put(state1, state2map);
          for(java.util.Iterator states2 = d2.nodes() ; states2.hasNext() ; ){
             DFANode state2 = (DFANode)states2.next();
             
             DFANode newNode =
                new DFANode(
                      state1.getLabel() + " x " + state2.getLabel(),
                      state1.isAccept() && state2.isAccept()
                );
             
             result.addNode(newNode);
             state2map.put(state2, newNode);
          }
       }
       
       //now fill in the edge table
       for(java.util.Iterator states1 = d1.nodes() ; states1.hasNext() ; ){
          DFANode state1 = (DFANode)states1.next();
          DFAEdge [] edges1 = getEdgeArray(d1.outEdges(state1));
          for(java.util.Iterator states2 = d2.nodes() ; states2.hasNext() ; ){
             DFANode state2 = (DFANode)states2.next();
             java.util.HashMap edges2 = getCharMap(getEdgeArray(d2.outEdges(state2)));
             for(int e1 = 0; e1 < edges1.length; e1++){
                DFANode dest1 =
                   edges1[e1].getDestination();
                DFAEdge e2 =
                   (DFAEdge)edges2.get(edges1[e1].getCharKey());
                if(e2 != null){
                   DFANode dest2 =
                      e2.getDestination();
                   //put an edge from state1xstate2 to dest1xdest2
                   DFANode source =
                      (DFANode)
                   ((java.util.HashMap)state1map.get(state1)).get(state2);
                   
                   DFANode dest =
                      (DFANode)
                   ((java.util.HashMap)state1map.get(dest1)).get(dest2);
                   
                   //change edge weight here
                   DFAEdge newEdge =
                      new DFAEdge
                      (source, dest, edges1[e1].getAlg(), edges1[e1].getTarget());
                   result.addEdge(newEdge);
                }
             }
          }
       }
       result.myStartState = (DFANode)
       ((java.util.HashMap)state1map.get(d1.myStartState)).get(d2.myStartState);
       
       
       result.trimUnreachable();
       result.trimDeadStates();
       return result;
       
    }
    
    private static DFAEdge [] getEdgeArray(java.util.Iterator edges) {
       java.util.ArrayList edgeList = new java.util.ArrayList();
       while(edges.hasNext())
          edgeList.add(edges.next());
       return (DFAEdge [])edgeList.toArray(new DFAEdge[0]);
    }

        private static java.util.HashMap getCharMap(DFAEdge [] edges)
        {
                java.util.HashMap retVal = new java.util.HashMap();
                for(int i = 0; i < edges.length; i++)
                {
                        retVal.put(edges[i].getCharKey(), edges[i]);
                }
                return retVal;
        }
}

