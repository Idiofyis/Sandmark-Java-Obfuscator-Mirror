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
public class StaticWatermark implements QuickProtect {

   /* (non-Javadoc)
    * @see sandmark.wizard.quickprotect.QuickProtect#run(sandmark.wizard.AlgorithmProvider, sandmark.wizard.ObjectProvider)
    */
   public void run(sandmark.wizard.AlgorithmProvider ap, 
                   sandmark.wizard.ObjectProvider op) throws Exception {
     	filter(ap);
      sandmark.wizard.modeling.Model m =
         new sandmark.wizard.modeling.lazydfa.LazyDFAModel();
      sandmark.wizard.evaluation.Evaluator e =
         new sandmark.wizard.evaluation.FixedChange(0.05f);
      sandmark.wizard.decision.Strategy s =
         new sandmark.wizard.decision.random.Random();
      sandmark.wizard.ChoiceRunner r = new sandmark.wizard.ChoiceRunner();
      m.init(e,r,op,ap);
      e.init(m,r);
      s.init(m,e,r);
      
      while(s.step())
         ;
   }

   public void filter(sandmark.wizard.AlgorithmProvider ap) {
      while(ap.hasNext())
         if(!(ap.next() instanceof sandmark.watermark.StaticWatermarker))
            ap.remove();
      ap.reset();
   }
   public void filter(sandmark.wizard.ObjectProvider op) {}
   public String toString() { return "Static Watermarking Loop"; }
}
