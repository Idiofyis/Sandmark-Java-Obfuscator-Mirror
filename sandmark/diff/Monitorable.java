package sandmark.diff;

/** An interface for providing status updates while a
 *  DiffAlgorithm is running.
 *  @author Zach Heidepriem
 */

public interface Monitorable {   
    /**@return An arbitrary value that represents the expected 
     * running time for the task.
     */
    public int getTaskLength();
    
    /**@return A number between 0 and getTaskLength()
     * that represents how much of the task has been completed.
     *  
     */
    public int getCurrent();    

    /** Stop this from running
     */
    public abstract void stop();

}

