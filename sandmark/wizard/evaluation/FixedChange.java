package sandmark.wizard.evaluation;

public class FixedChange 
   implements sandmark.wizard.evaluation.Evaluator,
              sandmark.wizard.ChoiceRunListener {
   private static final float OBF_START = 0.0f;
   private static final float PERF_START = 1.0f;
   private float change;
   private java.util.Hashtable mObjectToRunCount = new java.util.Hashtable();
   private java.util.HashSet mListeners = new java.util.HashSet();
   public FixedChange(float f) { change = f; }
   public FixedChange() { this(0.25f); }
   public void init(sandmark.wizard.modeling.Model m,
                    sandmark.wizard.ChoiceRunner r) {
      r.addRunListener(this);
   }
   public void addEvaluationListener(EvaluationListener l) { mListeners.add(l); }
   public void removeEvaluationListener(EvaluationListener l) { mListeners.remove(l); }
   public float evaluatePerformanceLevel(sandmark.program.Object o) { return PERF_START; }
   public float evaluateObfuscationLevel(sandmark.program.Object o) {
      Integer runCountWrapper = (Integer)mObjectToRunCount.get(o);
      int runCount = runCountWrapper == null ? 0 : runCountWrapper.intValue();
      return OBF_START + runCount * change;
   }
   public void ranChoice(sandmark.wizard.modeling.Choice c) {
      Integer runCountWrapper = (Integer)mObjectToRunCount.get(c.getTarget());
      int runCount = runCountWrapper == null ? 0 : runCountWrapper.intValue();
      runCount++;
      mObjectToRunCount.put(c.getTarget(),new Integer(runCount));
      for(java.util.Iterator it = mListeners.iterator() ; it.hasNext() ; ) {
         EvaluationListener listener = (EvaluationListener)it.next();
         listener.valueUpdated(c.getTarget(),OBF_START + runCount * change,PERF_START);
      }
   }
}
