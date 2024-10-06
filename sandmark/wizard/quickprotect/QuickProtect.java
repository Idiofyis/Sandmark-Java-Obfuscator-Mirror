package sandmark.wizard.quickprotect;

/**
   Quick Protect algorithms are designed to be simple ways to combine
   sandmark.Algorithms for the Quick Protect pane on the GUI.
   @author Kelly Heffner
   @since 3.4.0 2003-12-10
   
   Classes that extend QuickProtect contain a run method that takes an 
   AlgorithmProvider and ObjectProvider.  The AlgorithmProvider supplies 
   the list of sandmark protection algorithms and the ObjectProvider 
   supplies the sandmark objects to protect (i.e. applications, classes,
   methods).
*/
public interface QuickProtect{

    /**
        The run method typically utilizes three classes.  A Model, a Strategy
	and a Evaluator.  The Strategy has a reference to a Model and a
	Evaluator.  The Strategy then chooses how to protect the objects from
	the ObjectProvider using algorithms from the AlgorithmProvider and 
	choosing from the choices provided by the model it is using.  This
	is done in the step() method of the Strategy, which is typically called
	in the run method of the QuickProtect class.  The Model also has a
	reference to the Evaluator.   
     */
    public void run(sandmark.wizard.AlgorithmProvider ap,
                    sandmark.wizard.ObjectProvider op) throws Exception;
    public void filter(sandmark.wizard.AlgorithmProvider ap);
    public void filter(sandmark.wizard.ObjectProvider op);
    public String toString();
}
