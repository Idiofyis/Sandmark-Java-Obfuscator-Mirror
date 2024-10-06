package sandmark.wizard.decision;

/**
 * Interface that encapsulates the code that makes decisions of what
 * choices to run, in what order, and runs choices. 
 */
public interface Strategy{
   /**
    * Called multiple times to run algorithms.  Returns whether Strategy
    * has finished or not.
    */
   boolean step() throws Exception;
   void init(sandmark.wizard.modeling.Model m,
             sandmark.wizard.evaluation.Evaluator e,
             sandmark.wizard.ChoiceRunner r);
}
