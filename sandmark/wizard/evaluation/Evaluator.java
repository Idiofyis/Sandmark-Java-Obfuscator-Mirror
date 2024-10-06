package sandmark.wizard.evaluation;

/**
 * Class that encapsulates evaluation for obfuscation level and 
 * performance level for sandmark objects.
 */
public interface Evaluator{
   void addEvaluationListener(EvaluationListener e);
   void removeEvaluationListener(EvaluationListener e);
   float evaluateObfuscationLevel(sandmark.program.Object o);
   float evaluatePerformanceLevel(sandmark.program.Object o);
   void init(sandmark.wizard.modeling.Model m,sandmark.wizard.ChoiceRunner r);
}
