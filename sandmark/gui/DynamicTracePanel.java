package sandmark.gui;

public class DynamicTracePanel extends javax.swing.JPanel 
    implements SandMarkGUIConstants,AlgorithmPanel {
    private SandMarkFrame mFrame;
    private DynamicWatermarkPanel mPanel;

    private ConfigPropertyPanel mCPP;
    private java.awt.GridBagConstraints mCPPConstraints;

    private HelpButton mHelpButton;
    private javax.swing.JButton mTraceButton;

    public DynamicTracePanel(SandMarkFrame frame,DynamicWatermarkPanel panel) {
        mFrame = frame;
        mPanel = panel;

        setBackground(SAND_COLOR);

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        setLayout(layout);

        mTraceButton = new javax.swing.JButton("Start");
        mTraceButton.setBackground(SAND_COLOR);
        mTraceButton.setForeground(DARK_SAND_COLOR);
        mTraceButton.addActionListener(new java.awt.event.ActionListener() {
            private boolean traceInProgress = false;
            public void actionPerformed(java.awt.event.ActionEvent e) {
                mFrame.setAllEnabled(false);
                Thread traceThread = new Thread() {
                    public void run() {
                        try {
                            sandmark.watermark.DynamicWatermarker dwm =
                                (sandmark.watermark.DynamicWatermarker)getCurrentAlgorithm();
                            mCPP.updateProperties();
                            if(getApplication() == null)
                                throw new java.io.FileNotFoundException();
                            if(traceInProgress) {
                                dwm.endTracing();
                                dwm.stopTracing();
                                traceInProgress = false;
                                mTraceButton.setText("Start");
                            } else {
                                dwm.startTracing
                                (sandmark.watermark.DynamicWatermarker.getTraceParams
                                        (getApplication()));
                                traceInProgress = true;
                                mTraceButton.setText("Done");
                            }
                        } catch(ClassNotFoundException e) {
                           sandmark.util.Log.message(0,"Please specify a Main Class");
                        } catch(java.io.FileNotFoundException ex) {
                            sandmark.util.Log.message(0,"File not found: " + ex);
                        } catch(sandmark.util.exec.TracingException ex) {
                            sandmark.util.Log.message(0,"Tracing failed: " + ex);
                        } catch(Exception ex) {
                            ex.printStackTrace();
                            sandmark.util.Log.message(0,"Unexpected failure: " + ex);
                        } finally {
                            mFrame.setAllEnabled(true);
                        }
                    }
                };
                traceThread.start();
            }
        });

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(3,3,3,3);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = java.awt.GridBagConstraints.NONE;

        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.gridy = 1;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        layout.setConstraints(mTraceButton,gbc);
        add(mTraceButton);

        java.awt.Component lowerRightBox = javax.swing.Box.createGlue();
        gbc.gridx = 1;
        gbc.weightx = 1;
	gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        layout.setConstraints(lowerRightBox,gbc);
        add(lowerRightBox);

	mHelpButton = new HelpButton(getCurrentAlgorithm().getShortName());
	gbc.gridx = 2;
	gbc.weightx = 0;
	gbc.fill = java.awt.GridBagConstraints.NONE;
	layout.setConstraints(mHelpButton,gbc);
	add(mHelpButton);

	setAlgorithm(getCurrentAlgorithm());
    }

    public void setAlgorithm(sandmark.Algorithm alg) {
        if(mCPP != null) {
	    mCPP.updateProperties();
            remove(mCPP);
	}
        if(mCPPConstraints == null) {
            mCPPConstraints = new java.awt.GridBagConstraints();
            mCPPConstraints.gridy = 0;
            mCPPConstraints.gridx = 0;
            mCPPConstraints.gridwidth = 3;
	    mCPPConstraints.weighty = 1.0;
            mCPPConstraints.insets = new java.awt.Insets(3,3,3,3);
            mCPPConstraints.fill = java.awt.GridBagConstraints.BOTH;
            mCPPConstraints.anchor = java.awt.GridBagConstraints.NORTH;
            
        }
        mCPP = new ConfigPropertyPanel
            (new sandmark.util.ConfigProperties[] {
                    sandmark.watermark.DynamicWatermarker.getProperties(),
                    alg.getConfigProperties(),
            },sandmark.util.ConfigProperties.PHASE_DYNAMIC_TRACE,
            mFrame.getApplicationTracker());
        ((java.awt.GridBagLayout)getLayout()).setConstraints(mCPP,mCPPConstraints);
        add(mCPP);

	if(mHelpButton != null)
	    mHelpButton.setHelpKey(alg.getShortName());
    }

    public ConfigPropertyPanel getCPP() {
        return mCPP;
    }

    public sandmark.Algorithm getCurrentAlgorithm() {
        return mPanel.getCurrentAlgorithm();
    }

    public sandmark.program.Application getApplication() throws Exception {
	return mFrame.getCurrentApplication();
    }
}

