package sandmark.gui;

public class RecognizeButton extends javax.swing.JButton {
    private AlgorithmPanel mPanel;
    private NextWMButton mButton;
    public RecognizeButton(AlgorithmPanel panel,NextWMButton button) {
        super("Recognize");

        mPanel = panel;
        mButton = button;

        addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    mPanel.getCPP().updateProperties();
		    sandmark.util.graph.graphview.GraphList.instance().clear();
                    try {
                        sandmark.program.Application app = mPanel.getApplication();
                        if(mPanel.getApplication() == null)
                            throw new java.io.FileNotFoundException();
                        sandmark.watermark.StaticRecognizeParameters params = 
                            sandmark.watermark.StaticWatermarker.getRecognizeParams(app);
                        mButton.setIter
                            (sandmark.watermark.StaticRecognize.runRecognition
                             (mPanel.getCurrentAlgorithm(),params));
		    } catch(java.io.FileNotFoundException ex) {
			sandmark.util.Log.message(0,"File not found: " + ex);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                        sandmark.util.Log.message(0,"Unexpected failure: " + ex);
                    }
                }
            });
    }
}
