/*
 * Created on Apr 14, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.modeling.lazydfa;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LazyDFAModel implements sandmark.wizard.modeling.Model,
                                     sandmark.wizard.ChoiceRunListener,
                                     sandmark.program.ObjectMemberChangeListener {
   private LazyDFA mDFA;
   private LazyDFANode mCurrentNode;
   public void init(sandmark.wizard.evaluation.Evaluator e,
                    sandmark.wizard.ChoiceRunner r,
                    sandmark.wizard.ObjectProvider op,
                    sandmark.wizard.AlgorithmProvider ap) {
      registerMemberChangeListener(op);
      mDFA = new LazyDFA(ap,op);
      mCurrentNode = mDFA.getStartState();
      if(mCurrentNode.getOutgoingEdges(mDFA).length == 0)
         throw new RuntimeException();
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
   
   public void addedObject(sandmark.program.Object parent,
                           sandmark.program.Object child) {
      mDFA.mObjProvider.addObject(child);
      mCurrentNode.setEdgeSet(null);
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         sandmark.wizard.modeling.ModelChangeListener listener =
            (sandmark.wizard.modeling.ModelChangeListener)it.next();
         listener.modelChanged();
      }
   }
   public void copiedObject(sandmark.program.Object parent,
                            sandmark.program.Object orig,
                            sandmark.program.Object child) {
      mDFA.mObjProvider.addObject(child);
      mCurrentNode.setEdgeSet(null);
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         sandmark.wizard.modeling.ModelChangeListener listener =
            (sandmark.wizard.modeling.ModelChangeListener)it.next();
         listener.modelChanged();
      }
   }
   public void deletingObject(sandmark.program.Object parent,
                              sandmark.program.Object child) {
      mDFA.mObjProvider.removeObject(child);
      mCurrentNode.setEdgeSet(null);
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         sandmark.wizard.modeling.ModelChangeListener listener =
            (sandmark.wizard.modeling.ModelChangeListener)it.next();
         listener.modelChanged();
      }
   }
   
   public boolean isTerminationPoint() { return mCurrentNode.isAccept(); }
   public boolean hasChoices() { return getChoiceCount() != 0; }
   
   public int getChoiceCount() { return mCurrentNode.getOutgoingEdges(mDFA).length; }
   public sandmark.wizard.modeling.Choice getChoiceAt(int i) {
      return new EdgeChoice(mCurrentNode.getOutgoingEdges(mDFA)[i]);
   }
   
   public sandmark.wizard.modeling.Choice [] getChoicesAt(int m,int n) {
      EdgeChoice choices[] = new EdgeChoice[n - m + 1];
      for(int i = 0 ; i < choices.length ; i++)
         choices[i] = new EdgeChoice(mCurrentNode.getOutgoingEdges(mDFA)[i + m]);
      return choices;
   }
   
   public int getTerminationChoiceCount() { return getChoiceCount(); }
   public sandmark.wizard.modeling.Choice getTerminationChoiceAt(int i) {
      return getChoiceAt(i);
   }
   public sandmark.wizard.modeling.Choice [] getTerminationChoicesAt(int m,int n) {
      return getChoicesAt(m,n);
   }
   
   public java.util.Iterator choices() { 
      return new DFAEdgeIterator(mCurrentNode.getOutgoingEdges(mDFA));
   }
   public java.util.Iterator terminationChoices() { return choices(); }
   
   private java.util.HashSet mListeners = new java.util.HashSet();
   public void addModelChangeListener
      (sandmark.wizard.modeling.ModelChangeListener l) { mListeners.add(l); }
   public void removeModelChangeListener
      (sandmark.wizard.modeling.ModelChangeListener l) { mListeners.remove(l); }
   
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
      LazyDFAEdge mEdge;
      EdgeChoice(LazyDFAEdge edge) {
         super(edge.getAlg(),edge.getTarget());
         mEdge = edge;
      }
   }
   private static class DFAEdgeIterator implements java.util.Iterator {
      private LazyDFAEdge mEdges[];
      private int i;
      DFAEdgeIterator(LazyDFAEdge edges[]) {
         mEdges = edges;
         i = 0;
      }
      public boolean hasNext() { return mEdges != null && i != mEdges.length; }
      public Object next() { 
         LazyDFAEdge next = mEdges[i++];
         return new EdgeChoice(next);
      }
      public void remove() { throw new UnsupportedOperationException(); }
   }
   public java.util.Iterator getChoices() {
      LazyDFAEdge edges[] = mCurrentNode.getOutgoingEdges(mDFA);
      return new DFAEdgeIterator(edges);
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.modeling.Strategy#update(sandmark.wizard.modeling.Choice)
    */
   public void ranChoice(sandmark.wizard.modeling.Choice choice) {
      EdgeChoice ec = (EdgeChoice)choice;
      assert ec.mEdge.getSource() == mCurrentNode;
      mCurrentNode = mDFA.getDestination(ec.mEdge);
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         sandmark.wizard.modeling.ModelChangeListener l =
            (sandmark.wizard.modeling.ModelChangeListener)it.next();
         l.modelChanged();
      }
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.modeling.Strategy#getVisualization()
    */
   public sandmark.util.newgraph.Graph getVisualization() {
      return null;
   }

   public void filter(sandmark.wizard.AlgorithmProvider ap) {}
   public void filter(sandmark.wizard.ObjectProvider op) {}

}
