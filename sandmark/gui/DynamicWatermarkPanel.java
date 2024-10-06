package sandmark.gui;

public class DynamicWatermarkPanel extends SkinPanel 
    implements SandMarkGUIConstants,
	       sandmark.util.ConfigPropertyChangeListener,SandMarkPanel,
	       AlgorithmComboBox.DescriptionListener {

    private DynamicEmbedPanel mEmbedPanel;
    private DynamicTracePanel mTracePanel;
    private DynamicRecognizePanel mRecognizePanel;
    private javax.swing.JTabbedPane mInsetPanel;

    private AlgorithmComboBox mComboBox;

    private SandMarkFrame mFrame;

    public DynamicWatermarkPanel(SandMarkFrame frame) {
        mFrame = frame;

	sandmark.Console.getConfigProperties().addPropertyChangeListener
	    ("Input File",this);

        mInsetPanel = new javax.swing.JTabbedPane();
        mInsetPanel.setBackground(SAND_COLOR);
	mInsetPanel.setForeground(DARK_SAND_COLOR);

        //  Dynamic ComboBox
        mComboBox = new AlgorithmComboBox
            (this,sandmark.util.classloading.IClassFinder.DYN_WATERMARKER);

        mEmbedPanel = new DynamicEmbedPanel(mFrame,this);
        mTracePanel = new DynamicTracePanel(mFrame,this);
	mRecognizePanel = new DynamicRecognizePanel(mFrame,this);

        mInsetPanel.add(mTracePanel,"Trace");
        mInsetPanel.add(mEmbedPanel, "Embed");
        mInsetPanel.add(mRecognizePanel, "Recognize");

        mComboBox.addListener(mEmbedPanel);
        mComboBox.addListener(mTracePanel);
        mComboBox.addListener(mRecognizePanel);

        //  Static labels
        javax.swing.JLabel algorithmLabel = new javax.swing.JLabel("Algorithm:");
        algorithmLabel.setForeground(DARK_SAND_COLOR);

	java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
	setLayout(layout);

	java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
	gbc.gridx = gbc.gridy = 0;
	gbc.weightx = 1.0;
	gbc.insets = new java.awt.Insets(3,3,3,3);

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
        return mComboBox.getCurrentAlgorithm();
    }
    
    public void propertyChanged(sandmark.util.ConfigProperties cp,String propertyName,
                                Object oldValue,Object newValue) {
        java.io.File in = (java.io.File)newValue;
        if(in.exists()) {
            String fn = in.toString();
            sandmark.watermark.DynamicWatermarker.getProperties().setProperty
            ("Output File",
                    sandmark.Console.constructOutputFileName(fn,"wm"));
            sandmark.watermark.DynamicWatermarker.getProperties().setProperty
            ("Trace File",
                    fn.endsWith(".jar") ? 
                            fn.substring(0,fn.length() - 3) + "tra" :
                                fn + ".tra");
        }
    }

    private String mDescription = 
       sandmark.watermark.DynamicWatermarker.getOverview();
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

