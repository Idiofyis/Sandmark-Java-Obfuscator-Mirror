package sandmark.wizard.modeling.lazydfa;

public class LazyDFA
{
    private LazyDFANode myStartState;
    private java.util.ArrayList myStates;
    
    private sandmark.wizard.AlgorithmProvider mAlgProvider;
    sandmark.wizard.ObjectProvider mObjProvider;

    private java.util.HashMap myRangeMap;

    public static boolean DEBUG = false;

    public LazyDFA(sandmark.wizard.AlgorithmProvider ap,
                   sandmark.wizard.ObjectProvider op)
    {       
       mAlgProvider = ap;
       mObjProvider = op;
       myRangeMap = buildRangeMap(op);

        myStartState = new LazyDFANode(myRangeMap);
        myStates = new java.util.ArrayList();
    }

    public LazyDFANode getStartState()
    {
        return myStartState;
    }

    public LazyDFANode getDestination
        (LazyDFAEdge edge)
    {
        //the destination node does not change since its based
        //on properties
        if(edge.getLazilyComputedSink() != null){
            if(DEBUG) System.out.println("ALREADY KNOW SINK");
            return edge.getLazilyComputedSink();
        }

        //otherwise, we need to compute it :D
        LazyDFANode temp =
            new LazyDFANode(myRangeMap,edge.getSource());

        sandmark.Algorithm alg = edge.getAlg();
        sandmark.program.Object targ = edge.getTarget();

        sandmark.config.ModificationProperty[] ranProps = alg.getMutations();
        if(ranProps != null){
            for(int i = 0; i < ranProps.length; i++){
                temp.addRan(ranProps[i], targ);
                temp.removeReq(ranProps[i], targ);
            }
        }
        temp.addRan(new sandmark.config.AlgorithmProperty(alg), targ);
        temp.removeReq(new sandmark.config.AlgorithmProperty(alg), targ);

        sandmark.config.RequisiteProperty[] prohibProps = alg.getPostprohibited();
        if(prohibProps != null){
            for(int i = 0; i < prohibProps.length; i++){
                temp.addProhib(prohibProps[i], targ);
            }
        }

        sandmark.config.RequisiteProperty[] reqProps = alg.getPostrequisites();
        if(reqProps != null){
            for(int i = 0; i < reqProps.length; i++){
                temp.addReq(reqProps[i], targ);
            }
        }

        int index = myStates.indexOf(temp);
        if(index >= 0)
            return (LazyDFANode)myStates.get(index);
        else{
            myStates.add(temp);
        }

        if(DEBUG) System.out.println("&&&Number of states: " + myStates.size());

        edge.setLazilyComputedSink(temp);
        return temp;
    }

    public void buildEdges(LazyDFANode source)
    {
        java.util.List tempEdgeList = getEdgeList(source);

        if(!source.isAccept() && tempEdgeList.isEmpty())
            throw new RuntimeException("I got stuck in a nonaccepting state; help!");

        //degenerate case, we stripped out our edges to the accept state
        // if(!source.isAccept() && deadCount == tempEdgeList.size() &&
        //   source.getCurrentEdgeSet() == null){
        //    source.setEdgeSet((LazyDFAEdge[])
        //                      tempEdgeList.toArray
        //                      (new LazyDFAEdge[tempEdgeList.size()]));
        //        return;
        //}

        source.setEdgeSet((LazyDFAEdge[])
                        tempEdgeList.toArray(new LazyDFAEdge
                                             [tempEdgeList.size()]));
    }
    
    private java.util.List getEdgeList(LazyDFANode source) {
       //this edge list may already be computed - if so, we know that this
       //edge set is a *superset* of the edges that can come out now
       //(properties are still the same, only weights change) so skip
       //the reevaluation of the edges
       if(source.getCurrentEdgeSet() != null)
           return new java.util.ArrayList(java.util.Arrays.asList(source.getCurrentEdgeSet()));
       
       //now go through each element of the alphabet and determine if it
       //can be on an edge out of this node. for all (T,x)
       java.util.ArrayList edgeList = new java.util.ArrayList();
       
       sandmark.Algorithm algs[] = mAlgProvider.getAlgorithms();
       sandmark.program.Object[] objects = mObjProvider.getObjects();
       
       for(int i = 0; i < algs.length; i++) {
          for(int j = 0 ; j < objects.length ; j++) {
             sandmark.Algorithm alg = algs[i];
             sandmark.program.Object targ = objects[j];
             if(!sandmark.wizard.modeling.dfa.DFA.isTargetOf(objects[j],algs[i]))
                continue;
          
          //if T has a prereq property p, that property must be fulfilled
          //ie that x is in the ran list mapping keyed on p
          sandmark.config.RequisiteProperty[] prereqs = alg.getPrerequisites();
          if(prereqs != null){
             boolean nix = false;
             for(int p = 0; p < prereqs.length; p++){
                if(!source.ranListContains(prereqs[p], targ)){
                   nix = true;
                   break;
                }
             }
             if(nix) continue;
          }
          
          //if T has a preprohibit property p, then that property must be
          //unfulfilled ie that x is not in the ran list mapping keyed on p
          sandmark.config.RequisiteProperty[] preprohibs = alg.getPreprohibited();
          if(preprohibs != null){
             boolean nix = false;
             for(int p = 0; p < preprohibs.length; p++){
                if(source.ranListContains(preprohibs[p], targ)){
                   nix = true;
                   break;
                }
             }
             if(nix) continue;
          }
          
          //no postprohibitions have banned T, ie for all mutation properties
          //p that T has, x cannot be in the prohibit list keyed on p
          sandmark.config.ModificationProperty[] mutations = alg.getMutations();
          if(mutations != null){
             boolean nix = false;
             for(int p = 0; p < mutations.length; p++){
                if(source.prohibListContains(mutations[p], targ)){
                   nix = true;
                   break;
                }
             }
             if(nix) continue;
          }
          //dont forget to check the algorithm property for alg!
          if(source.prohibListContains
                (new sandmark.config.AlgorithmProperty(alg), targ))
             continue;
          
          /******HERE ADD CAVEAT ABOUT POSTREQ'S!!!!!*/
          
          //otherwise good to go!
          edgeList.add(new LazyDFAEdge
                (alg, targ, source));
          
          }
       }

       return edgeList;
    }

    protected static java.util.HashMap buildRangeMap
        (sandmark.wizard.ObjectProvider op)
    {
        java.util.HashMap rangeMap = new java.util.HashMap();
        sandmark.program.Object objects[] = op.getObjects();
        for(int i = 0 ; i < objects.length ; i++) {
            java.util.ArrayList temp = new java.util.ArrayList();

            //for each object o, if t is a subobject of o or vice versa
            //then o is in r(t).
            for(int j = 0; j < objects.length; j++){
                if(objects[i] == objects[j] ||
                   sandmark.wizard.modeling.dfa.DFA.isSubobjectOf
                      (objects[i],objects[j]) || 
                   sandmark.wizard.modeling.dfa.DFA.isSubobjectOf
                      (objects[j],objects[i]))
                    temp.add(objects[j]);
            }
            rangeMap.put(objects[i], temp);
        }
        return rangeMap;
    }

    /*package*/ static java.util.ArrayList getRange
        (java.util.HashMap rangeMap,sandmark.program.Object obj)
    {
        return (java.util.ArrayList)rangeMap.get(obj);
    }

}

