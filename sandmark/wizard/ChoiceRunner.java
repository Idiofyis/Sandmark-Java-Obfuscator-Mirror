/*
 * Created on Apr 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard;


/**
 * @author ash
 *
 * Runs Choice c
 */
public class ChoiceRunner {
   public void run(sandmark.wizard.modeling.Choice c) throws Exception {
      sandmark.Algorithm alg = c.getAlg();
      sandmark.program.Object obj = c.getTarget();
      boolean success = false;
      
      sandmark.util.Log.message(0,"running " + c + "...");

      try {
         if(alg instanceof sandmark.watermark.StaticWatermarker)
            ((sandmark.watermark.StaticWatermarker)alg).embed
            (sandmark.watermark.StaticWatermarker.getEmbedParams
                  ((sandmark.program.Application)obj));
         else if(alg instanceof sandmark.watermark.DynamicWatermarker)
            ((sandmark.watermark.DynamicWatermarker)alg).embed
            (sandmark.watermark.DynamicWatermarker.getEmbedParams
                  ((sandmark.program.Application)obj));
         else if(alg instanceof sandmark.obfuscate.ClassObfuscator)
            ((sandmark.obfuscate.ClassObfuscator)alg).apply
            ((sandmark.program.Class)obj);
         else if(alg instanceof sandmark.obfuscate.MethodObfuscator)
            ((sandmark.obfuscate.MethodObfuscator)alg).apply
            ((sandmark.program.Method)obj);
         else if(alg instanceof sandmark.obfuscate.AppObfuscator)
            ((sandmark.obfuscate.AppObfuscator)alg).apply
            ((sandmark.program.Application)obj);
         else
            assert false;
         success = true;
      } catch(Exception e) {
         sandmark.util.Log.message(0,"",e);
      }
      sandmark.util.Log.message(0,(success ? "success" : "failed"));
      
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         ChoiceRunListener l = (ChoiceRunListener)it.next();
         l.ranChoice(c);
      }
   }
   
   private java.util.HashSet mListeners = new java.util.HashSet();
   public void addRunListener(ChoiceRunListener r) { mListeners.add(r); }
   public void removeRunListener(ChoiceRunListener r) { mListeners.remove(r); }
}
