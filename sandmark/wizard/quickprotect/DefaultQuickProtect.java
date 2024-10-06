/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.quickprotect;


/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DefaultQuickProtect implements QuickProtect {

   public void run(sandmark.wizard.AlgorithmProvider ap, 
                   sandmark.wizard.ObjectProvider op) throws Exception {
      sandmark.wizard.modeling.Model m = 
         new sandmark.wizard.modeling.lazydfa.LazyDFAModel();
	 //new sandmark.wizard.modeling.dfa.DFAModel();
      sandmark.wizard.evaluation.Evaluator e =
         new sandmark.wizard.evaluation.FixedChange(0.25f);
      sandmark.wizard.decision.Strategy s =
         new sandmark.wizard.decision.priority.MaxPriorityStrategy();
      sandmark.wizard.ChoiceRunner r = new sandmark.wizard.ChoiceRunner();
      m.init(e,r,op,ap);
      e.init(m,r);
      s.init(m,e,r);
      
      while(s.step())
         ;
   }

   public void filter(sandmark.wizard.AlgorithmProvider ap) {}
   public void filter(sandmark.wizard.ObjectProvider op) {}
   public String toString() { return "Obfuscation Executive"; }
}
