/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.decision;

import sandmark.wizard.ChoiceRunner;
import sandmark.wizard.evaluation.Evaluator;
import sandmark.wizard.modeling.Model;


/**
 * @author ash
 *
 * AllAlgsOnce is a Strategery that runs each algorithm provided by the 
 * AlgorithmProvider one time by iterating over algorithms.
 */
public class AllAlgsOnce implements Strategy {
   private sandmark.wizard.modeling.Model mModel;
   private sandmark.wizard.ChoiceRunner mRunner;
   private java.util.HashSet mUsedAlgs = new java.util.HashSet();
   /* (non-Javadoc)
    * @see sandmark.wizard.decision.Strategy#step()
    */
   public boolean step() throws Exception {      
      for(java.util.Iterator choices = mModel.choices() ; choices != null && 
          choices.hasNext() ; ) {
         sandmark.wizard.modeling.Choice c = (sandmark.wizard.modeling.Choice)
            choices.next();
         if(mUsedAlgs.contains(c.getAlg().getClass()))
            continue;
         mRunner.run(c);
         mUsedAlgs.add(c.getAlg().getClass());
         return true;
      }
      return false;
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.decision.Strategy#init(sandmark.wizard.modeling.Model, sandmark.wizard.evaluation.Evaluator, sandmark.wizard.ChoiceRunner)
    */
   public void init(Model m, Evaluator e, ChoiceRunner r) {
      mModel = m ; mRunner = r;
   }

}
