/*
 * Created on Apr 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.wizard.evaluation;


/**
 * @author ash
 *
 * Interface for classes that listen for value of obfuscation level and
 * performance level update for sandmark objects being protected using
 * quick protect wizard.
 */
public interface EvaluationListener {
   void valueUpdated(sandmark.program.Object o,float obfLevel,float perfLevel);
}
