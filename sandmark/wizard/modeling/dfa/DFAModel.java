/*
 * Created on Apr 14, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.modeling.dfa;

/**
 * @author ash
	
	The DFAModel uses a deterministic finite automata to model the protection
	of the sandmark objects, with the start state being the unprotected
	program and each edge representing a Choice representing algorithms to
	run next.  At each step of the Model tells the Strategy the Choices
	and the Strategy then runs a Choice (using a ChoiceRunner object).
 */
public class DFAModel implements sandmark.wizard.modeling.Model,
                                 sandmark.wizard.ChoiceRunListener,
                                 sandmark.program.ObjectMemberChangeListener {
   private DFA mDFA;
   private DFANode mCurrentNode;
   private ChoiceList mCurrentChoices;
   private EdgeChoice mTerminationChoice;
   
   
   /**
    * Deletes object from DFA.
    */   
   public void deletingObject(sandmark.program.Object parent,
                           sandmark.program.Object child) {
      //Delete all edges connected to the object to delete
      for(java.util.Iterator edges = mDFA.edges() ; edges.hasNext() ; ) {
         DFAEdge edge = (DFAEdge)edges.next();
         if(edge.getTarget() == child)
            mDFA.removeEdge(edge);
      }
      mDFA.removeUnreachable(mCurrentNode);
      mCurrentChoices = null;
      //Tell listeners that model has changed.
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         sandmark.wizard.modeling.ModelChangeListener listener =
            (sandmark.wizard.modeling.ModelChangeListener)it.next();
         listener.modelChanged();
      }
   }
   
   public void addedObject(sandmark.program.Object parent,
                           sandmark.program.Object child) {}
   
   public void copiedObject(sandmark.program.Object parent,
                            sandmark.program.Object orig,
                            sandmark.program.Object copy) {}
   
   /**
    * Initializes DFAModel and registers listeners.
    * Builds a DFA model based on the objects and algorithms
    * from the ObjectProvider and AlgorithmProveder.
    */   
   public void init(sandmark.wizard.evaluation.Evaluator e,
                    sandmark.wizard.ChoiceRunner r,
                    sandmark.wizard.ObjectProvider op,
                    sandmark.wizard.AlgorithmProvider ap) {
      registerMemberChangeListener(op);
      sandmark.program.Object objects[] = (sandmark.program.Object [])
         getObjects(op).toArray(new sandmark.program.Object[0]);
      mDFA = DFA.createDFA(ap.getAlgorithms(),objects);
      mCurrentNode = mDFA.getStartState();
      r.addRunListener(this);
   }
   
   /**
    * Registers listeners for objects in ObjectProvider
    */
   private void registerMemberChangeListener(sandmark.wizard.ObjectProvider op) {
      while(op.hasNext()) {
         sandmark.program.Object o = 
            ((sandmark.program.Object)op.next()).getParent();
         if(o != null)
            o.addObjectMemberChangeListener(this);
      }
      op.reset();
   }
   
   private java.util.HashSet mListeners = new java.util.HashSet();
   
   /**
    * Adds a model change listener to the list of listeners
    */
   public void addModelChangeListener
      (sandmark.wizard.modeling.ModelChangeListener l) { mListeners.add(l); }
   /**
    * Removes a model change listener to the list of listeners
    */   
   public void removeModelChangeListener
      (sandmark.wizard.modeling.ModelChangeListener l) { mListeners.remove(l); }
      
   /**
    * Called after choice has been run. Changes DFA current state to
    * reflect model after choice has been run.  Also fires a model
    * changed for ModelChangeListeners.
    */
   public void ranChoice(sandmark.wizard.modeling.Choice c) {
      EdgeChoice ec = (EdgeChoice)c;
      mCurrentChoices = null;
      mTerminationChoice = null;
      mCurrentNode = ec.mEdge.getDestination();
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         sandmark.wizard.modeling.ModelChangeListener l =
            (sandmark.wizard.modeling.ModelChangeListener)it.next();
         l.modelChanged();
      }
   }
   
   /**
    * Returns if model is at a termination point (i.e. current node in DFA
    * is an accept state).
    */   
   public boolean isTerminationPoint() { return mCurrentNode.isAccept(); }
   
   /**
    * Returns a sandmark.util.SingleObjectIterator with the EdgeChoice that
    * is a termination choice (i.e. leads to accept state in DFA).  The
    * termination choice is found with a BFS starting at the current node
    * in the DFA.
    */        
   public java.util.Iterator terminationChoices() {
      findTerminationChoice();
      return new sandmark.util.SingleObjectIterator(mTerminationChoice);
   }
   
   /**
    * Returns 0 if the current node is an accept state or 1 otherwise. 
    */     
   public int getTerminationChoiceCount() {
      return mCurrentNode.isAccept() ? 0 : 1;
   }
   
   /**
    * Returns the choice that leads to an accept state.
    * Choice is found using BFS search starting at current node in DFA
    * leading to accept state.
    */
   public sandmark.wizard.modeling.Choice getTerminationChoiceAt(int i) { 
      findTerminationChoice();
      return mTerminationChoice;
   }
   
   /**
    * Returns the choice that leads to an accept state in a Choice[] of length 1.
    * Choice is found using BFS search starting at current node in DFA
    * leading to accept state.
    */   
   public sandmark.wizard.modeling.Choice [] getTerminationChoicesAt(int m,int n) {
      findTerminationChoice();
      return new sandmark.wizard.modeling.Choice[] { mTerminationChoice };
   }
   
   /**
    * Returns whether or not there are any choices from current node of DFA.
    */
   public boolean hasChoices() { return getChoiceCount() != 0; }
   
   /**
    * Returns the number of choices from the current state in the DFA.
    */
   public int getChoiceCount() {
      buildChoiceList();
      return mCurrentChoices.getChoiceCount();
   }
   
   /**
    * Returns the ith choice from the choices reachable from current state in DFA.
    */
   public sandmark.wizard.modeling.Choice getChoiceAt(int i) {
      buildChoiceList();
      return mCurrentChoices.getChoiceAt(i);
   }
   
   /**
    * Returns a list of choice from the mth choice to the nth choice from the choices 
    * reachable from current state in DFA.
    */   
   public sandmark.wizard.modeling.Choice [] getChoicesAt(int m,int n) {
      buildChoiceList();
      return mCurrentChoices.getChoicesAt(m,n);
   }
   
   /**
    * Returns Iterator of choices reachable from current state in DFA.
    */   
   public java.util.Iterator choices() { 
      buildChoiceList();
      return mCurrentChoices;
   }
   
   /**
    * Returns ArrayList of objects provided by ObjectProvider.
    */      
   protected java.util.ArrayList getObjects(sandmark.wizard.ObjectProvider object) {
      java.util.ArrayList objects = new java.util.ArrayList();
      while(object.hasNext())
         objects.add(object.next());
      return objects;
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.modeling.Strategy#getChoices()
    */
   private static class EdgeChoice extends sandmark.wizard.modeling.Choice {
      DFAEdge mEdge;
      EdgeChoice(DFAEdge edge) {
         super(edge.getAlg(),edge.getTarget());
         mEdge = edge;
      }
   }
   private static class ChoiceList implements java.util.Iterator {
      private java.util.Iterator mEdgeIterator;
      private java.util.ArrayList mChoices = new java.util.ArrayList();
      private int next = 0;
      ChoiceList(java.util.Iterator edgeIterator) {
         mEdgeIterator = edgeIterator;
      }
      int getChoiceCount() {
         while(mEdgeIterator.hasNext())
            mChoices.add(new EdgeChoice((DFAEdge)mEdgeIterator.next()));
         return mChoices.size();
      }
      EdgeChoice getChoiceAt(int i) { 
         while(mChoices.size() <= i && mEdgeIterator.hasNext())
            mChoices.add(new EdgeChoice((DFAEdge)mEdgeIterator.next()));
         return (EdgeChoice)mChoices.get(i);
      }
      EdgeChoice [] getChoicesAt(int m,int n) {
         getChoiceAt(n);
         EdgeChoice [] choices = new EdgeChoice[n - m + 1];
         for(int i = 0 ; i < choices.length ; i++)
            choices[i] = (EdgeChoice)mChoices.get(i + m);
         return choices;
      }
      public boolean hasNext() { 
         return next != mChoices.size() || mEdgeIterator.hasNext(); 
      }
      public Object next() {
         return getChoiceAt(next++);
      }
      public void remove() { throw new UnsupportedOperationException(); }
   }
   
   private void buildChoiceList() {
      if(mCurrentChoices == null) {
      	  java.util.ArrayList choiceList = new java.util.ArrayList();
	  java.util.Iterator nodes = mDFA.succs(mCurrentNode);
	  DFAEdge edge = null;
	  while(nodes.hasNext()) {
		DFANode n = (DFANode)nodes.next();
		for(java.util.Iterator edges = mDFA.edges();
		    edges.hasNext();) {
		    edge = (DFAEdge)edges.next();
		    if(edge.sourceNode() == mCurrentNode)
		       choiceList.add(edge);
		}
	  }
	  mCurrentChoices =  new ChoiceList(choiceList.iterator());
      }
   }

   /** Finds termination choice reachable form current state in DFA.
    *  Uses a breadth first search, starting at current state, which can
    *  not be an accept state, to find the EdgeChoice that is the
    *  edge taken to the accept state.
    */
   private void findTerminationChoice() {
      if(mTerminationChoice != null)
         return;
      
      if(mCurrentNode.isAccept())
         return;
      
      java.util.Hashtable parents = new java.util.Hashtable();
      Object accept = null;
      for(java.util.Iterator bfs = mDFA.breadthFirst(mCurrentNode) ; 
          accept == null && bfs.hasNext() ; ) {
         DFANode state = (DFANode)bfs.next();
         
         //this only affects the case where state == mCurrentNode
         Object parent = state;
         
         for(java.util.Iterator preds = mDFA.preds(state) ; 
             parent == null && preds.hasNext() ; ) {
            Object pred = preds.next();
            if(parents.containsKey(pred))
               parent = pred;
         }
         parents.put(state,parent);
         if(state.isAccept())
            accept = state;
      }
      
      if(accept == null)
         throw new sandmark.wizard.modeling.CorruptStateException();
      
      for(Object p ; (p = parents.get(accept)) != mCurrentNode ; accept = p)
         ;
      
      DFAEdge edge = (DFAEdge)mDFA.getFirstEdge(mCurrentNode,accept);
      assert edge != null;
      mTerminationChoice = new EdgeChoice(edge);
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.modeling.Strategy#getVisualization()
    */
   public sandmark.util.newgraph.Graph getVisualization() {
      return mDFA.graph();
   }
   
   public DFANode getCurrentNode() { return mCurrentNode; }

   public void filter(sandmark.wizard.AlgorithmProvider ap) {}
   public void filter(sandmark.wizard.ObjectProvider op) {}
}
