package sandmark.gui;

public class DynamicRecognizeButton extends javax.swing.JButton {
    private AlgorithmPanel mPanel;
    private NextWMButton mButton;
    private SandMarkFrame mFrame;
    public DynamicRecognizeButton(AlgorithmPanel panel,NextWMButton button,
				  SandMarkFrame frame) {
        super("Recognize");

        mPanel = panel;
        mButton = button;
	mFrame = frame;

        addActionListener(new java.awt.event.ActionListener() {
		private boolean recognitionInProgress = false;
                public void actionPerformed(java.awt.event.ActionEvent e) {
		    mFrame.setAllEnabled(false);
                    sandmark.util.graph.graphview.GraphList.instance().clear();
		    Thread recognizeThread = new Thread() {
			    public void run() {
				try {
				    mPanel.getCPP().updateProperties();
				    sandmark.watermark.DynamicWatermarker dwm =
					(sandmark.watermark.DynamicWatermarker)mPanel.getCurrentAlgorithm();
				    if(recognitionInProgress) {
					try {
					    mButton.setIter(dwm.watermarks());
					    dwm.stopRecognition();
					    recognitionInProgress = false;
					    setText("Recognize");
					} catch(sandmark.util.exec.TracingException ex) {
					    sandmark.util.Log.message(0,"Unexpected failure: " + ex);
					} catch(Exception ex) {
					    System.out.println("missed an exception");
					    ex.printStackTrace();
					}
				    } else {
					try {
					    if(mPanel.getApplication() == null)
						throw new java.io.FileNotFoundException();
					    dwm.startRecognition
					    (sandmark.watermark.DynamicWatermarker.getRecognizeParams
					            (mFrame.getCurrentApplication()));
					    recognitionInProgress = true;
					    setText("Done");
					} catch(ClassNotFoundException e) {
					   sandmark.util.Log.message(0,"Please specify a Main Class");
					} catch(java.io.FileNotFoundException ex) {
					    sandmark.util.Log.message(0,"File not found: " + ex);
					} catch(sandmark.util.exec.TracingException ex) {
					    ex.printStackTrace();
					    sandmark.util.Log.message(0,"Unexpected failure: " + ex);
					} catch(Exception ex) {
					    ex.printStackTrace();
					    sandmark.util.Log.message(0,"Unexpected failure: " + ex);
					}
				    }
				} finally {
				    mFrame.setAllEnabled(true);
				}
			    }
			};
		    recognizeThread.start();
		}
            });
    }
}


