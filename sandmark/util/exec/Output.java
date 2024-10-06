package sandmark.util.exec;

public class Output {
   private java.lang.Process process = null;
   private int outputCompleteCount = 0;

   private void dumpStream(
                           java.io.InputStream stream) throws java.io.IOException {
      java.io.PrintStream outStream = System.out;
      java.io.BufferedReader in = 
         new java.io.BufferedReader(new java.io.InputStreamReader(stream));
      String line;
      while ((line = in.readLine()) != null) {
         outStream.println(line);
      }
   }

   public Output(java.lang.Process process) {
      this.process = process;
      displayRemoteOutput(process.getErrorStream());
      displayRemoteOutput(process.getInputStream());
   }

   /**	
    *	Create a Thread that will retrieve and display any output.
    *	Needs to be high priority, else debugger may exit before
    *	it can be displayed.
    */
   private void displayRemoteOutput(final java.io.InputStream stream) {
      java.lang.Thread thr = new java.lang.Thread("output reader") { 
            public void run() {
               try {
                  dumpStream(stream);
               } catch (java.io.IOException ex) {
                  System.err.println("Failed reading output of child java interpreter.");
                  System.exit(0);
               } finally {
                  notifyOutputComplete();
               }
            }
         };
      thr.setPriority(Thread.MAX_PRIORITY-1);
      thr.start();
   }

   synchronized void notifyOutputComplete() {
      outputCompleteCount++;
      notifyAll();
   }

   public synchronized void waitOutputComplete() {
      // Wait for stderr and stdout
      if (process != null) {
         while (outputCompleteCount < 2) {
            try {wait();} catch (java.lang.InterruptedException e) {
               sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
            }
         }
      }
   }

}

