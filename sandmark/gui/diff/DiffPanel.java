package sandmark.gui.diff;

public class DiffPanel extends sandmark.gui.SkinPanel
    implements sandmark.gui.SandMarkGUIConstants,sandmark.gui.SandMarkPanel {
    private sandmark.gui.SandMarkFrame mFrame;

    private sandmark.gui.ConfigPropertyPanel mCPP;
    private sandmark.util.ConfigProperties mConfigProps;

    public DiffPanel(sandmark.gui.SandMarkFrame frame) {
	mFrame = frame;

	javax.swing.JPanel insetPanel = new javax.swing.JPanel();
	insetPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder
				 (javax.swing.BorderFactory.createRaisedBevelBorder(), 
				  javax.swing.BorderFactory.createLoweredBevelBorder()));
	insetPanel.setBackground(SAND_COLOR);

	java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
	insetPanel.setLayout(layout);

	setLayout(new java.awt.BorderLayout());
	add(insetPanel);
	
	mConfigProps = new sandmark.util.ConfigProperties
	    (new String[][] { 
		{ "First Jar File","","A Jar File",null,"J","A",},
		{ "Second Jar File","","Another Jar File",null,"J","A",},},null);

	mCPP = new sandmark.gui.ConfigPropertyPanel(new sandmark.util.ConfigProperties[] {
	        mConfigProps
	},sandmark.util.ConfigProperties.PHASE_ALL,
	mFrame.getApplicationTracker());

	java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
	gbc.fill = java.awt.GridBagConstraints.BOTH;
	gbc.gridy = 0;
	gbc.gridx = 0;
	gbc.weighty = 1;
	gbc.weightx = 1;
	gbc.gridwidth = 2;
	gbc.insets = new java.awt.Insets(3,3,3,3);
	layout.setConstraints(mCPP,gbc);
	insetPanel.add(mCPP);

	javax.swing.JButton diffButton = new javax.swing.JButton("Diff");
	diffButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    mCPP.updateProperties();
		    String jar1 = mConfigProps.getProperty("First Jar File");
		    String jar2 = mConfigProps.getProperty("Second Jar File");
		    sandmark.gui.diff.DiffFrame df = 
			new sandmark.gui.diff.DiffFrame(jar1,jar2);
		    df.show();
		}
	    });
	diffButton.setBackground(SAND_COLOR);
	diffButton.setForeground(DARK_SAND_COLOR);

	gbc.fill = java.awt.GridBagConstraints.NONE;
	gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
	gbc.weightx = 0;
	gbc.weighty = 0;
	gbc.gridy = 1;
	gbc.gridwidth = 1;
	layout.setConstraints(diffButton,gbc);
	insetPanel.add(diffButton);
	
	sandmark.gui.HelpButton help = new sandmark.gui.HelpButton("diff");
	gbc.gridx = 1;
	gbc.anchor = java.awt.GridBagConstraints.SOUTHEAST;
	layout.setConstraints(help,gbc);
	insetPanel.add(help);
    }

    public String getDescription() {
	return sandmark.gui.diff.DiffFrame.getOverview();
    }

    public sandmark.gui.SandMarkFrame getFrame() {
	return mFrame;
    }
}
