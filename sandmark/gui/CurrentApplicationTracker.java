package sandmark.gui;

public class CurrentApplicationTracker {
    private sandmark.program.Application mCurrentApplication;
    public sandmark.program.Application getCurrentApplication() {
       if(mCurrentApplication != null && mCurrentApplication.getPath() == null)
          mCurrentApplication = null;
	if(mCurrentApplication != null && 
	   !mCurrentApplication.getPath().equals
	   (sandmark.Console.getConfigProperties().getValue
			     ("Input File")))
	   mCurrentApplication = null;
	java.io.File newValue = 
	   (java.io.File)sandmark.Console.getConfigProperties().getValue
	   ("Input File");
	if(mCurrentApplication == null && newValue != null &&
	   newValue.exists()) {
	    try {
		setApplication(new sandmark.program.Application(newValue));
	    } catch(Exception e) {
		sandmark.util.Log.message(0,"Couldn't open application: " + e);
	    }
	}
	return mCurrentApplication;
    }
    private void setApplication(sandmark.program.Application app) {
	//System.out.println("setting application to " + app);
	mCurrentApplication = app;
    }
}
