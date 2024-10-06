/*
 * Created on May 6, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.quickprotect;

import sandmark.wizard.AlgorithmProvider;
import sandmark.wizard.ObjectProvider;


/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CustomQuickProtect implements QuickProtect {

   private sandmark.wizard.modeling.Model mModel;
   private sandmark.wizard.decision.Strategy mStrategy;
   private sandmark.wizard.evaluation.Evaluator mEvaluator;
   private sandmark.wizard.ChoiceRunner mRunner =
      new sandmark.wizard.ChoiceRunner();
   
   public void setModel(sandmark.wizard.modeling.Model model) {
      assert mModel == null;
      mModel = model;
   }
   
   public void setDecision(sandmark.wizard.decision.Strategy decision) {
      assert mStrategy == null;
      mStrategy = decision;
   }
   
   public void setEvaluator(sandmark.wizard.evaluation.Evaluator eval) {
      assert mEvaluator == null;
      mEvaluator = eval;
   }
   
   /* (non-Javadoc)
    * @see sandmark.wizard.quickprotect.QuickProtect#run(sandmark.wizard.AlgorithmProvider, sandmark.wizard.ObjectProvider)
    */
   public void run(AlgorithmProvider ap, ObjectProvider op) throws Exception {
      assert mModel != null && mStrategy != null && mEvaluator != null;
      
      mModel.init(mEvaluator,mRunner,op,ap);
      mEvaluator.init(mModel,mRunner);
      mStrategy.init(mModel,mEvaluator,mRunner);
      
      while(mStrategy.step())
         ;
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.quickprotect.QuickProtect#filter(sandmark.wizard.AlgorithmProvider)
    */
   public void filter(AlgorithmProvider ap) {
      if(mModel != null)
         mModel.filter(ap);
   }

   /* (non-Javadoc)
    * @see sandmark.wizard.quickprotect.QuickProtect#filter(sandmark.wizard.ObjectProvider)
    */
   public void filter(ObjectProvider op) {
      if(mModel != null)
         mModel.filter(op);
   }

   public String toString() { return "Custom Quick Protect"; }
}
