package sandmark.gui;

public class DynamicEmbedPanel extends javax.swing.JPanel 
    implements SandMarkGUIConstants,AlgorithmPanel {
    private SandMarkFrame mFrame;
    private DynamicWatermarkPanel mPanel;

    private ConfigPropertyPanel mCPP;
    private java.awt.GridBagConstraints mCPPConstraints;

    private HelpButton mHelpButton;
    private GraphDisplayButton mGraphButton;

    public DynamicEmbedPanel(SandMarkFrame frame,DynamicWatermarkPanel panel) {
        mFrame = frame;
        mPanel = panel;

        setBackground(SAND_COLOR);

        javax.swing.JButton embedButton = new javax.swing.JButton("Embed");
        embedButton.setBackground(SAND_COLOR);
        embedButton.setForeground(DARK_SAND_COLOR);
        embedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                mFrame.setAllEnabled(false);
                sandmark.util.graph.graphview.GraphList.instance().clear();
                Thread embedThread = new Thread() {
                    public void run() {
                        try {
                            mCPP.updateProperties();
                            if(getApplication() == null)
                                throw new java.io.FileNotFoundException();
                            sandmark.watermark.DynamicWatermarker dwm = 
                                (sandmark.watermark.DynamicWatermarker)
                            mPanel.getCurrentAlgorithm();
                            dwm.embed(sandmark.watermark.DynamicWatermarker.getEmbedParams(getApplication()));
                            getApplication().save
                            (sandmark.watermark.DynamicWatermarker
                                    .getProperties().getProperty("Output File"));
                        } catch(java.io.FileNotFoundException ex) {
                            sandmark.util.Log.message(0,"Bad file name: " + ex);
                        } catch(Exception ex) {
                            ex.printStackTrace();
                            sandmark.util.Log.message(0,"Watermarking failed: " + ex);
                        } finally {
                            mFrame.setAllEnabled(true);
                        }
                    }
                };
                embedThread.start();
            }
        });

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        setLayout(layout);

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(3,3,3,3);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = java.awt.GridBagConstraints.NONE;

        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.gridy = 2;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        layout.setConstraints(embedButton,gbc);
        add(embedButton);

        java.awt.Component lowerMiddleBox = javax.swing.Box.createGlue();
        gbc.gridx = 1;
        gbc.weightx = 1;
	gbc.gridwidth = 1;
        layout.setConstraints(lowerMiddleBox,gbc);
        add(lowerMiddleBox);

        mGraphButton = new GraphDisplayButton(mFrame);
        mGraphButton.setVisible(false);
        gbc.gridx = 2;
        gbc.weightx = 0;
        layout.setConstraints(mGraphButton, gbc);
        add(mGraphButton);

	mHelpButton = new HelpButton(getCurrentAlgorithm().getShortName());
	gbc.gridx = 3;
	gbc.weightx = 0;
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
            mCPPConstraints.gridwidth = 4;
	    mCPPConstraints.weighty = 1.0;
            mCPPConstraints.insets = new java.awt.Insets(3,3,3,3);
            mCPPConstraints.fill = java.awt.GridBagConstraints.BOTH;
            mCPPConstraints.anchor = java.awt.GridBagConstraints.NORTH;
            
        }
        mCPP = new ConfigPropertyPanel
            (new sandmark.util.ConfigProperties[] {
                    sandmark.watermark.DynamicWatermarker.getProperties(),
                    alg.getConfigProperties(),
            },sandmark.util.ConfigProperties.PHASE_DYNAMIC_EMBED,
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

