package sandmark.gui;

public class DecompilePanel extends SkinPanel
    implements SandMarkGUIConstants,SandMarkPanel {
    private SandMarkFrame mFrame;
    private ConfigPropertyPanel mCPP;

    public DecompilePanel(SandMarkFrame frame) {
        mFrame = frame;

        javax.swing.JPanel DInsetPanel = new javax.swing.JPanel();
        DInsetPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder
                               (javax.swing.BorderFactory.createRaisedBevelBorder(),
                                javax.swing.BorderFactory.createLoweredBevelBorder()));
        DInsetPanel.setBackground(SAND_COLOR);

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        DInsetPanel.setLayout(layout);

        javax.swing.JButton DButton = new javax.swing.JButton("Decompile");
        DButton.setBackground(SAND_COLOR);
        DButton.setForeground(DARK_SAND_COLOR);
        DButton.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    mCPP.updateProperties();
                    sandmark.program.Application app =
                        mFrame.getCurrentApplication();
                    String classname = sandmark.decompile.Decompile.
			getProperties().getProperty("Class");
                    String classpath = sandmark.decompile.Decompile.
			getProperties().getProperty("Classpath");
                    if(app == null) {
                        sandmark.util.Log.message(0,"Invalid application");
                        return;
                    }
                    String result =
                        sandmark.decompile.Decompile.decompile(app,classname,classpath);
                    new sandmark.gui.DecompileDialog(mFrame, result).show();
                }
            });

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();

        mCPP = new ConfigPropertyPanel
            ( new sandmark.util.ConfigProperties[] { 
                    sandmark.decompile.Decompile.getProperties() 
              },sandmark.util.ConfigProperties.PHASE_ALL,
              mFrame.getApplicationTracker());
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.insets = new java.awt.Insets(3,3,3,3);
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.anchor = java.awt.GridBagConstraints.NORTHEAST;

        layout.setConstraints(mCPP,gbc);
        DInsetPanel.add(mCPP);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;

        layout.setConstraints(DButton,gbc);
        DInsetPanel.add(DButton);
        
        HelpButton help = new HelpButton("decompile");
        gbc.gridx = 0;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        layout.setConstraints(help,gbc);
        DInsetPanel.add(help);

        setLayout(new java.awt.BorderLayout());
        add(DInsetPanel);
    }

    public String getDescription() {
        return sandmark.decompile.Decompile.getOverview();
    }

    public SandMarkFrame getFrame() {
        return mFrame;
    }
}

