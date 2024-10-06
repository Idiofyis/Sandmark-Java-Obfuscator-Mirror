package sandmark.wizard.modeling.set;

public class SetModel implements sandmark.wizard.modeling.Model,
                                 sandmark.wizard.ChoiceRunListener,
                                 sandmark.program.ObjectMemberChangeListener
{

    /** sandmark.program.Object ==> sandmark.wizard.modeling.set.CandidateSet */
    private java.util.HashMap objectToSet = new java.util.HashMap();

    private java.util.ArrayList mObjects = new java.util.ArrayList();
    private ChoiceList mChoiceList;
    private sandmark.wizard.AlgorithmProvider mAlgProvider;
    
    /**
       Initializes a SetModel given a list of algorithms and an application.
       allAlgs is a list of{@link sandmark.Algorithm} objects (which is
       cloned anywhere it would be stored).
       app is the root sandmark.program.Object for what objects
       you want to run algorithms on (normally a sandmark.program.Application.
    */
    public void init(sandmark.wizard.evaluation.Evaluator e,
                     sandmark.wizard.ChoiceRunner r,
                     sandmark.wizard.ObjectProvider op,
                     sandmark.wizard.AlgorithmProvider ap){
       mAlgProvider = ap;
       registerMemberChangeListener(op);
       initObjects(new java.util.HashMap(), op, ap);
    }
    
    public void deletingObject(sandmark.program.Object parent,
                               sandmark.program.Object child) {
       objectToSet.remove(child);
       mObjects.remove(child);
       mChoiceList = null;
       for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
          sandmark.wizard.modeling.ModelChangeListener listener =
             (sandmark.wizard.modeling.ModelChangeListener)it.next();
          listener.modelChanged();
       }
    }
    
    public void addedObject(sandmark.program.Object parent,
                            sandmark.program.Object child) {
       initObject(new java.util.HashMap(),child,mAlgProvider.getAlgorithms());
       mChoiceList = null;
       for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
          sandmark.wizard.modeling.ModelChangeListener listener =
             (sandmark.wizard.modeling.ModelChangeListener)it.next();
          listener.modelChanged();
       }
    }
    
    public void copiedObject(sandmark.program.Object parent,
                             sandmark.program.Object orig,
                             sandmark.program.Object copy) {
       initObject(new java.util.HashMap(),copy,mAlgProvider.getAlgorithms());
       mChoiceList = null;
       for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
          sandmark.wizard.modeling.ModelChangeListener listener =
             (sandmark.wizard.modeling.ModelChangeListener)it.next();
          listener.modelChanged();
       }
    }
    
    private void registerMemberChangeListener(sandmark.wizard.ObjectProvider op) {
       while(op.hasNext()) {
          sandmark.program.Object o = 
             ((sandmark.program.Object)op.next()).getParent();
          if(o != null)
             o.addObjectMemberChangeListener(this);
       }
       op.reset();
    }
    
    private class ChoiceList implements java.util.Iterator {
       private java.util.Iterator mObjectIterator;
       private java.util.ArrayList mChoices = new java.util.ArrayList();
       private SetModelIterator mCurrentIterator;
       ChoiceList() { mObjectIterator = mObjects.iterator(); }
       public boolean hasNext() { 
          setCurrentIterator();
          return mCurrentIterator != null && mCurrentIterator.hasNext();
       }
       public Object next() {
          setCurrentIterator();
          return mCurrentIterator.next();
       }
       public void remove() { throw new UnsupportedOperationException(); }
       private void setCurrentIterator() {
          if(mCurrentIterator != null && mCurrentIterator.hasNext())
             return;
          for(mCurrentIterator = null ; mCurrentIterator == null && 
              mObjectIterator.hasNext() ; ) {
             sandmark.program.Object object = 
                (sandmark.program.Object)mObjectIterator.next();
             CandidateSet cs = (CandidateSet)objectToSet.get(object);
             cs = getCurrentIterationPool(cs);
             if(cs.numAlgorithms() > 0) {
                mCurrentIterator = new SetModelIterator(cs,object);
                if(!mCurrentIterator.hasNext())
                   mCurrentIterator = null;
             }
          }
       }
       int getChoiceCount() {
          while(hasNext())
             mChoices.add(next());
          return mChoices.size();
       }
       sandmark.wizard.modeling.Choice getChoiceAt(int i) {
          while(mChoices.size() < i) {
             setCurrentIterator();
             if(mCurrentIterator == null || !mCurrentIterator.hasNext())
                break;
             mChoices.add(mCurrentIterator.next());
          }
          return (sandmark.wizard.modeling.Choice)mChoices.get(i);
       }
       sandmark.wizard.modeling.Choice [] getChoicesAt(int m,int n) {
          getChoiceAt(n);
          sandmark.wizard.modeling.Choice choices[] = 
             new sandmark.wizard.modeling.Choice[n - m + 1];
          for(int i = 0 ; i < choices.length ; i++)
             choices[i] = (sandmark.wizard.modeling.Choice)mChoices.get(i + m);
          return choices;
       }
    }
    
    private void buildChoiceList() {
       if(mChoiceList == null)
          mChoiceList = new ChoiceList();
    }
 
    public java.util.Iterator choices() {
       buildChoiceList();
       return mChoiceList;
    }

    public java.util.Iterator terminationChoices(){
       return choices();
    }

    public sandmark.util.newgraph.Graph getVisualization(){
        return null;
    }
    
    public int getChoiceCount() {
       buildChoiceList();
       return mChoiceList.getChoiceCount();
    }
    
    public boolean hasChoices() {
       return getChoiceCount() > 0;
    }
    
    public int getTerminationChoiceCount() { return getChoiceCount(); }
    public sandmark.wizard.modeling.Choice getTerminationChoiceAt(int i)
    { return getChoiceAt(i); }
    public sandmark.wizard.modeling.Choice [] getTerminationChoicesAt(int m,int n)
    { return getChoicesAt(m,n); }
    public boolean isTerminationPoint() { return true; }
    
    public sandmark.wizard.modeling.Choice getChoiceAt(int i) {
       buildChoiceList();
       return mChoiceList.getChoiceAt(i);
    }
    
    public sandmark.wizard.modeling.Choice [] getChoicesAt(int m,int n) {
       buildChoiceList();
       return mChoiceList.getChoicesAt(m,n);
    }
    
    private java.util.HashSet mListeners = new java.util.HashSet();
    public void addModelChangeListener
       (sandmark.wizard.modeling.ModelChangeListener l) { mListeners.add(l); }
    public void removeModelChangeListener
       (sandmark.wizard.modeling.ModelChangeListener l) { mListeners.remove(l); }
    
    public void ranChoice(sandmark.wizard.modeling.Choice c) {
       computeNewCandidates(c.getAlg(),c.getTarget());
       mChoiceList = null;
       for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
          sandmark.wizard.modeling.ModelChangeListener l =
             (sandmark.wizard.modeling.ModelChangeListener)it.next();
          l.modelChanged();
       }
    }
   
    
    private void initObject(java.util.HashMap hints,
                              sandmark.program.Object obj,
                              sandmark.Algorithm allAlgs[]) {

       java.util.List algs = (java.util.List)hints.get(obj.getClass());
       if(algs == null){
           algs = sandmark.wizard.modeling.Util.getAlgsForTarget
               (obj, allAlgs);
           hints.put(obj.getClass(), algs);
       }

       sandmark.program.UserObjectConstraints constraints =
          obj.getUserConstraints();

       if(constraints.obfuscationLevel >0 && algs.size() > 0){
           objectToSet.put(obj,
                           new sandmark.wizard.modeling.set.CandidateSet
                           (algs, constraints));
           mObjects.add(obj);
       }
       
    }

    private void initObjects(java.util.HashMap hints,
                               sandmark.wizard.ObjectProvider op,
                               sandmark.wizard.AlgorithmProvider ap){
       sandmark.Algorithm algs[] = ap.getAlgorithms();
       while(op.hasNext())
          initObject(hints,(sandmark.program.Object)op.next(),algs);
    }

    private static void removeFromCandidateSet
        (java.util.Collection toRemove,
         sandmark.wizard.modeling.set.CandidateSet candidateSet)
    {
        candidateSet.removeAlgorithms(toRemove);

        //two rules for removal:
        //  i.  once a property has been removed from the candidate pool
        //      all algorithms that have that property as a prereq
        //      (and the prereq has not been filled) must be removed
        //      as well
        //  ii. once a property has been removed from the candidate pool
        //              all algorithms that postreq that property must
        //      be removed as well

        //get the list of properties that have been affected (the mutations
        //for all the algorithms being removed)
        java.util.Set propsToRemove = new java.util.LinkedHashSet();
        java.util.Iterator removeItr = toRemove.iterator();

        while(removeItr.hasNext())
            propsToRemove.addAll
                (sandmark.wizard.modeling.Util.getMutationProps
                 ((sandmark.Algorithm)removeItr.next()));

        //for each prop, see if it was the last of its kind
        java.util.List extinct = new java.util.LinkedList();
        java.util.Iterator propItr = propsToRemove.iterator();

        while(propItr.hasNext()){
            sandmark.config.RequisiteProperty prop =
                (sandmark.config.RequisiteProperty)propItr.next();

            java.util.ArrayList algsForProp =
                sandmark.wizard.modeling.Util.getAlgsForProp(prop);
            java.util.Iterator algItr = candidateSet.getAlgorithms();
            boolean contained = false;

            //check each algorithm to see if it has that property
            while(algItr.hasNext()) {
                if(algsForProp.contains(algItr.next())){
                    contained = true;
                    //we just need one
                    break;
                }
            }
            if(!contained)
                extinct.add(prop);
        }

        if(extinct.isEmpty())
            return;

        java.util.Set affectedRemoves = new java.util.LinkedHashSet();

        //now we have extinct, the list of properties that have been
        //completely removed from the list

        java.util.Iterator extinctItr = extinct.iterator();
        while(extinctItr.hasNext()){
            sandmark.config.RequisiteProperty dead =
                (sandmark.config.RequisiteProperty)extinctItr.next();

            //rule i. unfilled prereqs
            if(!candidateSet.hasRun(dead)){
                affectedRemoves.addAll
                    (sandmark.wizard.modeling.Util.getPrerequisiters(dead));
            }

            //rule ii.
            affectedRemoves.addAll
                (sandmark.wizard.modeling.Util.getPostrequisiters(dead));
        }

        if(!affectedRemoves.isEmpty())
            removeFromCandidateSet(affectedRemoves, candidateSet);

    }


    /**
       Performs the dependency computations on the candidate sets necessary
       when an algorithm is run.
       @param candidate the algorithm run
       @param target the object that the target was run on
     */
    public void computeNewCandidates(sandmark.Algorithm candidate, sandmark.program.Object target) {

        sandmark.wizard.modeling.set.CandidateSet candidateSet =
            (sandmark.wizard.modeling.set.CandidateSet)objectToSet.get(target);


        //adjust the main candidate pool accordingly
        java.util.ArrayList mutations =
            sandmark.wizard.modeling.Util.getMutationProps(candidate);

        //add all properties for this algorithm to the run props list
        candidateSet.run(mutations);

        //add all post requirements for this algorithm to the post
        //req list
        candidateSet.addPostreqs(candidate.getPostrequisites());

        //add all post suggestions for this algorithm to the post
        //suggest list
        candidateSet.addPostsuggs(candidate.getPostsuggestions());

        //once an allgorithm has been run, all algorithms that
        //preprohibit it must be removed from the candidate pool
        removeFromCandidateSet(sandmark.wizard.modeling.Util.getXers
                               (candidate,
                                sandmark.wizard.modeling.Util.PRE_PROHIBIT),
                               candidateSet);

        //the above line replaced:
        //while(propItr.hasNext()){
        //    sandmark.config.RequisiteProperty prop =
        //        (sandmark.config.RequisiteProperty)propItr.next();
        //    java.util.ArrayList toRemove = getPreprohibitors(prop);
        //    removeFromAlgList(toRemove, candidatePool, runProperties);
        //}


        //once an algorithm has been run, all algorithms that it
        //postprohibits must be removed from the candidate pool
        removeFromCandidateSet
            (sandmark.wizard.modeling.Util.getXed
             (candidate, sandmark.wizard.modeling.Util.POST_PROHIBIT),
             candidateSet);

        //the above line replaced:
        //sandmark.config.RequisiteProperty [] postProhibited =
        //    candidate.getPostprohibited();
        //if(postProhibited != null){
        //    for(int p = 0; p < postProhibited.length; p++){
        //       java.util.ArrayList toRemove =
        //            getAlgsForProp(postProhibited[p]);
        //       removeFromAlgList(toRemove, candidatePool, runProperties);
        //  }
        //}
    }


    /**
       Narrows the list of algorithm candidates for an object
       to a list of algorithms that can be run on this iteration.
       @param candidateSet the object to narrow the candidates for
    */
    public sandmark.wizard.modeling.set.CandidateSet getCurrentIterationPool
        (sandmark.wizard.modeling.set.CandidateSet candidateSet) {

        sandmark.wizard.modeling.set.CandidateSet currentSet =
            (sandmark.wizard.modeling.set.CandidateSet)candidateSet.clone();

        //rules for determining what can be run in the current
        //iteration:
        // i.  Anything that post-prohibits a req'd property
        //     should be removed from the candidate list
        // ii. Anything with unfufilled prereq's should be
        //     removed from the candidate list
        // iii.For all req'd properties, remove any algorithm
        //     that would be pre-prohibited by all alg's that
        //     fufill that property.


        // i.  Anything that post-prohibits a req'd property
        //     should be removed from the candidate list
        java.util.ArrayList toRemove = new java.util.ArrayList();
        for(java.util.Iterator itr = currentSet.postReqs();
            itr.hasNext() ;
            ){
            sandmark.config.RequisiteProperty prop =
                (sandmark.config.RequisiteProperty)itr.next();
            toRemove.addAll
                (sandmark.wizard.modeling.Util.getPostprohibitors(prop));
        }
        removeFromCandidateSet(toRemove, currentSet);


        // ii. Anything with unfufilled prereq's should be
        //     removed from the candidate list
        toRemove = new java.util.ArrayList();
        for( java.util.Iterator itr = currentSet.getAlgorithms();
             itr.hasNext();
             ) {
            sandmark.Algorithm alg = (sandmark.Algorithm)itr.next();
            sandmark.config.RequisiteProperty []preq = alg.getPrerequisites();
            if(preq != null)
                for(int p = 0; p < preq.length; p++){
                    if(!candidateSet.hasRun(preq[p])){
                        toRemove.add(alg);
                        break; //just need one
                    }
                }
        }
        removeFromCandidateSet(toRemove, currentSet);


        // iii.For all req'd properties, remove any algorithm
        //     that would be pre-prohibited by all alg's that
        //     fufill that property.

        //for all req'd properties p, if t_p pre-prohibits x
        //for all t_p, remove x

        for(java.util.Iterator itr = currentSet.postReqs();
            itr.hasNext();
            ) {
            sandmark.config.RequisiteProperty p =
                (sandmark.config.RequisiteProperty)itr.next();

            toRemove = currentSet.getAlgorithmList();
            for(java.util.Iterator algItr =
                    sandmark.wizard.modeling.Util.getAlgsForProp(p).iterator();
                algItr.hasNext();
                ) {
                sandmark.Algorithm t_p = (sandmark.Algorithm)algItr.next();
                java.util.ArrayList prehibited =
                    sandmark.wizard.modeling.Util.getXed
                    (t_p, sandmark.wizard.modeling.Util.PRE_PROHIBIT);

                //take the set intersection of toRemove and
                //the pre-prohibit list for this algorithm
                //this will remove any algorithm that is not
                //prehibited by the possible req filler
                toRemove = sandmark.wizard.modeling.Util.setIntersect
                    (prehibited, toRemove);
            }
            removeFromCandidateSet(toRemove, currentSet);
        }

        // iv. if all alg's that fufill a required property
        //     are removed - backtrack?
        if(!candidateSet.verify())
            throw new sandmark.wizard.modeling.CorruptStateException
                ("Program may be invalid, please restart executive " +
                 "with a different seed.");

        return currentSet;
    }

    private class SetModelIterator implements java.util.Iterator{
        private sandmark.program.Object myTarg;
        private java.util.Iterator algItr;

        public SetModelIterator(sandmark.wizard.modeling.set.CandidateSet algs,
                                sandmark.program.Object targ){
            myTarg = targ;
            algItr = algs.getAlgorithms();
        }

        public boolean hasNext(){
            return algItr.hasNext();
        }

        public Object next(){
            if(!hasNext())
                throw new java.util.NoSuchElementException();

            return new sandmark.wizard.modeling.Choice
                ((sandmark.Algorithm)algItr.next(), myTarg);
        }

        public void remove(){
            throw new java.lang.UnsupportedOperationException();
        }

    }

    public void filter(sandmark.wizard.AlgorithmProvider ap) {}
    public void filter(sandmark.wizard.ObjectProvider op) {}

}


