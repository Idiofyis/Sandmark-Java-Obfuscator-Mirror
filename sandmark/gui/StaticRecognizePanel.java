package sandmark.gui;

public class StaticRecognizePanel extends javax.swing.JPanel 
    implements SandMarkGUIConstants,AlgorithmPanel {
    private SandMarkFrame mFrame;
    private StaticWatermarkPanel mPanel;

    private ConfigPropertyPanel mCPP;
    private java.awt.GridBagConstraints mCPPConstraints;

    private HelpButton mHelpButton;
    private GraphDisplayButton mGraphButton;

    public StaticRecognizePanel(SandMarkFrame frame,StaticWatermarkPanel panel) {
        mFrame = frame;
        mPanel = panel;

        setBackground(SAND_COLOR);

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        setLayout(layout);

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(3,3,3,3);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = java.awt.GridBagConstraints.NONE;

        javax.swing.JComboBox wmList = new javax.swing.JComboBox();
        wmList.setBackground(SAND_COLOR);
        wmList.setForeground(DARK_SAND_COLOR);

        NextWMButton wmButton = new NextWMButton(wmList);
        wmButton.setBackground(SAND_COLOR);
        wmButton.setForeground(DARK_SAND_COLOR);

        RecognizeButton recognizeButton = new RecognizeButton(this,wmButton);
        recognizeButton.setBackground(SAND_COLOR);
        recognizeButton.setForeground(DARK_SAND_COLOR);

        gbc.gridwidth = 1;
        gbc.weighty = 0.0;
        gbc.gridy = 2;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        layout.setConstraints(recognizeButton,gbc);
        add(recognizeButton);

        gbc.gridx = 1;
        gbc.weightx = 1;
	gbc.gridwidth = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        layout.setConstraints(wmList,gbc);
        add(wmList);

        gbc.gridx = 2;
        gbc.weightx = 0;
        layout.setConstraints(wmButton,gbc);
        add(wmButton);

	mGraphButton = new GraphDisplayButton(mFrame);
	mGraphButton.setVisible(false);
	gbc.gridx = 3;
	layout.setConstraints(mGraphButton, gbc);
	add(mGraphButton);

	mHelpButton = new HelpButton(getCurrentAlgorithm().getShortName());
	gbc.gridx = 4;
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
            mCPPConstraints.gridwidth = 5;
	    mCPPConstraints.weighty = 1.0;
            mCPPConstraints.insets = new java.awt.Insets(3,3,3,3);
            mCPPConstraints.fill = java.awt.GridBagConstraints.BOTH;
            mCPPConstraints.anchor = java.awt.GridBagConstraints.NORTH;
            
        }
        mCPP = new ConfigPropertyPanel
            (new sandmark.util.ConfigProperties[] {
                    sandmark.watermark.StaticWatermarker.getProperties(),
                    alg.getConfigProperties(),
            },sandmark.util.ConfigProperties.PHASE_STATIC_RECOGNIZE,
            mFrame.getApplicationTracker());
        ((java.awt.GridBagLayout)getLayout()).setConstraints
            (mCPP,mCPPConstraints);
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

