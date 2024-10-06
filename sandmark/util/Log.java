package sandmark.util;

/**
 *  The sandmark.util.Log class handles logging for a SandMark application.
 *  Logging is considered a global aspect of the program, and all
 *  methods are static.
 *
 *  <P> Every message has a level associated with it.
 *  Only messages meeting a specified threshold are seen by the user.
 *
 *  <P>  Multiple log files can be specified, each with its own threshold.
 *  Each message is logged to every file with an accepting threshold.
 */

public class Log implements Runnable {

    static class Logger {
	java.io.PrintWriter logFile;
	int threshold;

	Logger ( java.io.PrintWriter logFile,
		 int threshold ) {
	    this.logFile = logFile;
	    this.threshold = threshold;
	}

	void log ( int level, String msg ) {
	    if (level <= threshold) {
		logFile.println(msg);
	    }
	}
    }
    
    static class Message {
    	String message;
    	int level;
    	Message(String m,int l) { message = m; level = l; }
    }

    public static final int USER_MESSAGES = 20;
    public static final int USER_ERRORS = 10;
    public static final int DEVELOPER_MESSAGES = -10;
    public static final int DEVELOPER_ERRORS = -20;
    public static final int INTERNAL_EXCEPTION = -50;

    private static java.util.ArrayList loggers = new java.util.ArrayList();
    private static java.util.LinkedList messages = new java.util.LinkedList();

/**
 *  Opens and registers an output file for logging.
 *
 *  @param fname	filename for output
 *  @param thresh	message threshold
 */
public static void addLog(String fname, int thresh) throws java.io.IOException {
	addLog(new java.io.PrintWriter(new java.io.FileWriter(fname), true), thresh);
}



/**
 *  Registers an output stream for logging.
 *
 *  @param o		output stream
 *  @param thresh	message threshold
 */
public static void addLog(java.io.OutputStream o, int thresh) {
    addLog(new java.io.OutputStreamWriter(o), thresh);
}



/**
 *  Registers an output writer for logging.
 *
 *  @param w		output stream
 *  @param thresh	message threshold
 */
public static void addLog(java.io.Writer w, int thresh) {
	Logger l = new Logger( new java.io.PrintWriter(w, true), thresh );
	synchronized(loggers) {
		loggers.add(l);
	}
}



/**
 *  Sends a message to the log files.
 *
 *  The message is sent to every log file that does not have a 
 *  threshold higher than the stated message level.
 *
 *  @param level	message level
 *  @param msg		message text
 */
public static void message(int level, String msg) {
	Message message = new Message(msg,level);
	synchronized(messages) {
		messages.add(message);
	}
	javax.swing.SwingUtilities.invokeLater(new Log());
}


/**
 *  Sends an exception message to the log files.
 *
 *  @param level	message level
 *  @param msg		message text
 */
public static void message(int level, String msg, java.lang.Throwable t) {
    message(level, msg + " [" + t.getMessage() + "]");
}

public void run() {
	java.util.List msgs;
	synchronized(messages) {
		msgs = (java.util.List)messages.clone();
		messages.clear();
	}
	synchronized(loggers) {
		for(java.util.Iterator loggerIt = loggers.iterator() ; 
			loggerIt.hasNext() ; ) {
			Logger l = (Logger)loggerIt.next();
			for(java.util.Iterator msgIt = msgs.iterator() ; msgIt.hasNext() ; ) {
				Message msg = (Message)msgIt.next();
				l.log(msg.level,msg.message);
			}
		}
	}
}

private Log() {}


} // class Log

