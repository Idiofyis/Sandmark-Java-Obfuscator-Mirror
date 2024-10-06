/*
 * Created on Apr 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.quickprotect;

/**
 * @author ash
 *
 */
public class AllAlgsOnce implements QuickProtect {

   /* (non-Javadoc)
    * @see sandmark.wizard.quickprotect.QuickProtect#run(sandmark.program.Application)
    */
   public void run(sandmark.wizard.AlgorithmProvider ap,
                   sandmark.wizard.ObjectProvider op) throws Exception {
      sandmark.wizard.evaluation.Evaluator e =
         new sandmark.wizard.evaluation.FixedChange(0.0f);
      sandmark.wizard.modeling.Model m =
         new sandmark.wizard.modeling.lazydfa.LazyDFAModel();
	 //new sandmark.wizard.modeling.dfa.DFAModel();
      sandmark.wizard.decision.Strategy s =
         new sandmark.wizard.decision.AllAlgsOnce();
      sandmark.wizard.ChoiceRunner r = new sandmark.wizard.ChoiceRunner();
      e.init(m,r);
      m.init(e,r,op,ap);
      s.init(m,e,r);
      
      while(s.step())
         System.out.println(m.getVisualization().nodeCount());
   }
   
   public void filter(sandmark.wizard.AlgorithmProvider ap) {}
   public void filter(sandmark.wizard.ObjectProvider op) {}
   
   public String toString() { return "Each Alg Once"; }
}
