/*
 * Created on Apr 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.modeling;


/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface Model {
   void init(sandmark.wizard.evaluation.Evaluator e,
             sandmark.wizard.ChoiceRunner r,
             sandmark.wizard.ObjectProvider op,
             sandmark.wizard.AlgorithmProvider ap);
   int getChoiceCount();
   Choice getChoiceAt(int i);
   Choice [] getChoicesAt(int m,int n);
   java.util.Iterator choices();
   int getTerminationChoiceCount();
   Choice getTerminationChoiceAt(int i);
   Choice [] getTerminationChoicesAt(int m,int n);
   java.util.Iterator terminationChoices();
   void addModelChangeListener(ModelChangeListener m);
   void removeModelChangeListener(ModelChangeListener m);
   boolean isTerminationPoint();
   boolean hasChoices();
   sandmark.util.newgraph.Graph getVisualization();
   void filter(sandmark.wizard.ObjectProvider ap);
   void filter(sandmark.wizard.AlgorithmProvider op);
}
