package sandmark.gui;

public class StaticWatermarkPanel extends SkinPanel 
    implements SandMarkGUIConstants,
	       sandmark.util.ConfigPropertyChangeListener,SandMarkPanel,
	       AlgorithmComboBox.DescriptionListener {

    private javax.swing.JTabbedPane mInsetPanel;
    private StaticEmbedPanel mEmbedPanel;
    private StaticRecognizePanel mRecognizePanel;

    private SandMarkFrame mFrame;

    private AlgorithmComboBox mComboBox;

    public StaticWatermarkPanel(SandMarkFrame frame) {
        mFrame = frame;

	sandmark.Console.getConfigProperties().addPropertyChangeListener
	    ("Input File",this);

        mInsetPanel = new javax.swing.JTabbedPane();
        //mInsetPanel.setBorder
	//(javax.swing.BorderFactory.createCompoundBorder
	//(javax.swing.BorderFactory.createRaisedBevelBorder(), 
	//javax.swing.BorderFactory.createLoweredBevelBorder()));
        mInsetPanel.setBackground(SAND_COLOR);
	mInsetPanel.setForeground(DARK_SAND_COLOR);

        //  Static ComboBox
        mComboBox = new AlgorithmComboBox
	    (this,sandmark.util.classloading.IClassFinder.STAT_WATERMARKER);

        mEmbedPanel = new StaticEmbedPanel(frame,this);
        mRecognizePanel = new StaticRecognizePanel(frame,this);

        mInsetPanel.add(mEmbedPanel, "Embed");
        mInsetPanel.add(mRecognizePanel, "Recognize");

	mInsetPanel.setSelectedIndex(0);

        mComboBox.addListener(mEmbedPanel);
        mComboBox.addListener(mRecognizePanel);

        //  Static labels
        javax.swing.JLabel algorithmLabel = new javax.swing.JLabel("Algorithm:");
        algorithmLabel.setForeground(DARK_SAND_COLOR);

	java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
	setLayout(layout);

	java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
	gbc.insets = new java.awt.Insets(3,3,3,3);
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weightx = 1.0;

	add(javax.swing.Box.createGlue(),gbc);

	gbc.gridx = 1;
	gbc.weightx = 0;
	
	add(algorithmLabel,gbc);

	gbc.gridx = 2;

	add(mComboBox,gbc);

	gbc.gridy = 1;
	gbc.gridx = 0;
	gbc.gridwidth = 3;
	gbc.weighty = 1.0;
	gbc.fill = java.awt.GridBagConstraints.BOTH;

	add(mInsetPanel,gbc);
    }
    protected sandmark.Algorithm getCurrentAlgorithm() {
	return mComboBox == null ? null : mComboBox.getCurrentAlgorithm();
    }
    
    public void propertyChanged(sandmark.util.ConfigProperties cp,String propertyName,
                                Object oldValue,Object newValue) {
        java.io.File in = (java.io.File)newValue;
        if(in.exists())
            sandmark.watermark.StaticWatermarker.getProperties().setProperty
            ("Output File",
             sandmark.Console.constructOutputFileName(in.toString(),"wm"));
    }

    private String mDescription = 
       sandmark.watermark.StaticWatermarker.getOverview();
    public String getDescription() {
	return mDescription;
    }

    public SandMarkFrame getFrame() {
	return mFrame;
    }
    public void showDescription() {
       mFrame.setDescription(mDescription);
    }
    public void showTransientDescription(String description) {
       mFrame.setDescription(description);
    }
    public void algorithmChanged(sandmark.Algorithm alg) {
       mDescription = alg.getDescription();
    }
}

