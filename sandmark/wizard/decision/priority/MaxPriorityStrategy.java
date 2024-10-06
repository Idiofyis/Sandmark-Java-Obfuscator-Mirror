/*
 * Created on Apr 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.decision.priority;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MaxPriorityStrategy 
   implements sandmark.wizard.decision.Strategy, 
              sandmark.wizard.evaluation.EvaluationListener,
              sandmark.wizard.modeling.ModelChangeListener {
   protected static final float DELTA = .00001f;
   protected static class ChoiceWrapper extends sandmark.wizard.modeling.Choice {
      public float value;
      ChoiceWrapper(sandmark.wizard.modeling.Choice c,float v) {
         super(c.getAlg(),c.getTarget());
         value = v;
      }
      public String toString() {
         return super.toString() + " weight: " + value;
      }
   }   
   protected java.util.ArrayList mChoicePQ = new java.util.ArrayList();
   private sandmark.wizard.evaluation.Evaluator mEvaluator;
   protected sandmark.wizard.modeling.Model mModel;
   protected sandmark.wizard.ChoiceRunner mRunner;
   
   private static java.util.Comparator mPQComparator =
      new java.util.Comparator() {
      public int compare(Object o1,Object o2) {
         ChoiceWrapper c1 = (ChoiceWrapper)o1;
         ChoiceWrapper c2 = (ChoiceWrapper)o2;
         float diff = c1.value - c2.value;
         float absDiff = diff < 0.0f ? -diff : diff;
         if(absDiff < DELTA)
            return 0;
         return diff < 0.0f ? 1 : -1; //we want high priority near the front
      }
   };
   
   public void valueUpdated(sandmark.program.Object object,
                            float obfLevel,float perfLevel) {
      boolean changed = false;
      for(java.util.Iterator it = mChoicePQ.iterator() ; it.hasNext() ; ) {
         ChoiceWrapper wrapper = (ChoiceWrapper)it.next();
         if(wrapper.getTarget() == object) {
            wrapper.value = getWeightForChoice(wrapper,obfLevel,perfLevel);
            changed = true;
         }
      }
      if(changed)
         java.util.Collections.sort(mChoicePQ,mPQComparator);
   }
   
   private static float getWeightForChoice
      (sandmark.wizard.modeling.Choice c,float obfLevel,float perfLevel) {
      float crit = c.getTarget().getUserConstraints().performanceCritical;
      float potency = 1; //XXXash: need to get alg potency here
      return potency * (Math.max(0, 1 - (crit))) * perfLevel * (1 - obfLevel);
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.decision.Strategy#step()
    */
   public boolean step() throws Exception {
      ChoiceWrapper cw = (ChoiceWrapper)mChoicePQ.get(0);
      if(cw.value < DELTA)
         return false;
      
      mRunner.run(cw);
      return true;
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.decision.Strategy#init(sandmark.wizard.modeling.Model, sandmark.wizard.evaluation.Evaluator, sandmark.wizard.ChoiceRunner)
    */
   public void init(sandmark.wizard.modeling.Model m, 
                    sandmark.wizard.evaluation.Evaluator e, 
                    sandmark.wizard.ChoiceRunner r) {
      mModel = m;
      mEvaluator = e;
      mRunner = r;
      m.addModelChangeListener(this);
      e.addEvaluationListener(this);
      modelChanged();
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.modeling.ModelChangeListener#modelChanged()
    */
   public void modelChanged() {
      mChoicePQ.clear();
      sandmark.wizard.modeling.Choice choices[] = 
         mModel.getChoicesAt(0,mModel.getChoiceCount() - 1);
      choicesAdded(choices);
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.modeling.ModelChangeListener#choicesAdded(sandmark.wizard.modeling.Choice[])
    */
   public void choicesAdded(sandmark.wizard.modeling.Choice[] choices) {
      for(int i = 0 ; i < choices.length ; i++) {
         float obfLevel = 
            mEvaluator.evaluateObfuscationLevel(choices[i].getTarget());
         float perfLevel =
            mEvaluator.evaluatePerformanceLevel(choices[i].getTarget());
         float value = getWeightForChoice(choices[i],obfLevel,perfLevel);
         ChoiceWrapper cw = new ChoiceWrapper(choices[i],value);
         mChoicePQ.add(cw);
      }
      java.util.Collections.sort(mChoicePQ,mPQComparator);
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.modeling.ModelChangeListener#choicesRemoved(sandmark.wizard.modeling.Choice[])
    */
   public void choicesRemoved(sandmark.wizard.modeling.Choice[] choices) {
      java.util.List removedChoices = java.util.Arrays.asList(choices);
      mChoicePQ.removeAll(removedChoices);
   }

}
