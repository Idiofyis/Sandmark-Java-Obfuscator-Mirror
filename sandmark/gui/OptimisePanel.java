

package sandmark.gui;

public class OptimisePanel extends SkinPanel
    implements SandMarkGUIConstants,AlgorithmPanel,
               sandmark.util.ConfigPropertyChangeListener,SandMarkPanel,
               AlgorithmComboBox.DescriptionListener {

    private javax.swing.JPanel mInsetPanel;

    private SandMarkFrame mFrame;

    private ConfigPropertyPanel mCPP;
    private java.awt.GridBagConstraints mCPPConstraints;

    private AlgorithmComboBox mComboBox;

    private HelpButton mHelpButton;
    private GraphDisplayButton mGraphButton;
    
    private sandmark.util.ConfigProperties mConfigProps =
        new sandmark.util.ConfigProperties(new String [][] {
            {"Output File","","The output jar-file.",
                null,"J","OPT",
            },
        },null);

    public OptimisePanel(SandMarkFrame frame) {
        sandmark.Console.getConfigProperties().addPropertyChangeListener
            ("Input File",this);

        mFrame = frame;

	// System.out.println("]]] " + sandmark.util.classloading.IClassFinder.GEN_OPTIMIZER);
        mComboBox = new AlgorithmComboBox
            (this,sandmark.util.classloading.IClassFinder.GEN_OPTIMIZER);
        mComboBox.addListener(this);

        buildInsetPanel();
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
        setAlgorithm(getCurrentAlgorithm());
    }

    public void buildInsetPanel() {

        mInsetPanel = new javax.swing.JPanel();
        mInsetPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder
                               (javax.swing.BorderFactory.createRaisedBevelBorder(),
                                javax.swing.BorderFactory.createLoweredBevelBorder()));
        mInsetPanel.setBackground(SAND_COLOR);

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        mInsetPanel.setLayout(layout);

        javax.swing.JButton configureButton = new javax.swing.JButton("Configure");
        configureButton.setBackground(SAND_COLOR);
        configureButton.setForeground(DARK_SAND_COLOR);
        configureButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ((javax.swing.JButton)e.getSource()).setEnabled(false);
                    try {
                        mCPP.updateProperties();
                        if(getApplication() == null) {
                            sandmark.util.Log.message(0,"Invalid application");
                            return;
                        }
                        sandmark.gui.ObfDialog obd = new sandmark.gui.ObfDialog
                            (mFrame,getApplication());
                        obd.show();
                    } catch(java.io.IOException ex) {
                        sandmark.util.Log.message(0,"Bad file name: " + ex);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                        sandmark.util.Log.message(0,"Unexpected error: " + ex);
                    } finally {
                        ((javax.swing.JButton)e.getSource()).setEnabled(true);
                    }
                }
            });


        javax.swing.JButton optimizeButton = new javax.swing.JButton("Optimize");
        optimizeButton.setBackground(SAND_COLOR);
        optimizeButton.setForeground(DARK_SAND_COLOR);
        optimizeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    mFrame.setAllEnabled(false);
                    sandmark.util.graph.graphview.GraphList.instance().clear();
                    Thread optimizationRunner = new Thread() {
                            public void run() {
                                try {
                                    mCPP.updateProperties();
                                    if(getApplication() == null) {
                                        sandmark.util.Log.message(0,"Invalid Application");
                                        return;
                                    }
                                    sandmark.optimise.Optimizer.runOptimization
                                        (getApplication(),mComboBox.getCurrentAlgorithm());
                                    getApplication().save(mConfigProps.getProperty("Output File"));
                                } catch(java.io.FileNotFoundException ex) {
                                    ex.printStackTrace();
                                    sandmark.util.Log.message(0,"Bad file name: " + ex);
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                    sandmark.util.Log.message(0,"Optimization failed: " + ex);
                                } finally {
                                    mFrame.setAllEnabled(true);
                                }
                            }
                        };
                    optimizationRunner.start();
                }
            });

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(3,3,3,3);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;

        layout.setConstraints(optimizeButton,gbc);
        mInsetPanel.add(optimizeButton);

        mGraphButton = new GraphDisplayButton(mFrame);
        mGraphButton.setVisible(false);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        layout.setConstraints(mGraphButton, gbc);
        mInsetPanel.add(mGraphButton);

        gbc.gridx = 3;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        layout.setConstraints(configureButton,gbc);
        mInsetPanel.add(configureButton);

        java.awt.Component lowerMiddleBox = javax.swing.Box.createGlue();
        gbc.gridx = 1;
        gbc.weightx = 1;
        layout.setConstraints(lowerMiddleBox,gbc);
        mInsetPanel.add(lowerMiddleBox);

        mHelpButton = new HelpButton(getCurrentAlgorithm().getShortName());
        gbc.gridx = 4;
        gbc.weightx = 0;
        layout.setConstraints(mHelpButton,gbc);
        mInsetPanel.add(mHelpButton);
    }

    public void setAlgorithm(sandmark.Algorithm alg) {
        if(mCPP != null) {
            mCPP.updateProperties();
            mInsetPanel.remove(mCPP);
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
        mCPP = new ConfigPropertyPanel( 
                new sandmark.util.ConfigProperties[] {
                        sandmark.Console.getConfigProperties(),
                        mConfigProps,    
                        alg.getConfigProperties()
                },sandmark.util.ConfigProperties.PHASE_OPTIMIZE,
                mFrame.getApplicationTracker());
        ((java.awt.GridBagLayout)mInsetPanel.getLayout()).setConstraints(mCPP,mCPPConstraints);
        mInsetPanel.add(mCPP);

        if(mHelpButton != null)
            mHelpButton.setHelpKey(alg.getShortName());
    }

    public ConfigPropertyPanel getCPP() {
        return mCPP;
    }

    public void propertyChanged(sandmark.util.ConfigProperties cp,String propertyName,
                                Object oldValue,Object newValue) {
        java.io.File in = (java.io.File)newValue;
        if(in.exists())
            mConfigProps.setProperty
            ("Output File",
             sandmark.Console.constructOutputFileName(in.toString(),"opt"));
    }

    public sandmark.program.Application getApplication() throws Exception {
        return mFrame.getCurrentApplication();
    }

    public sandmark.Algorithm getCurrentAlgorithm() {
        return mComboBox.getCurrentAlgorithm();
    }

    private String mDescription = getOverview();
    public String getDescription() {
        return mDescription;
    }

    public SandMarkFrame getFrame() {
        return mFrame;
    }
    public void showDescription() {
       sandmark.Algorithm cur = getCurrentAlgorithm();
       mFrame.setDescription(mDescription);
    }
    public void showTransientDescription(String description) {
       mFrame.setDescription(description);
    }
    public void algorithmChanged(sandmark.Algorithm alg) {
       mDescription = alg.getDescription();
    }

    /**
     *  Get the HTML codes of the About page for Optimise
        @return HTML code for the about page
     */
    public static java.lang.String getAboutHTML(){
        return
            "<HTML><BODY>" +
            "<CENTER><B>List of Optimizers</B></CENTER>" +
            "</BODY></HTML>";
    }

    /**
     *  Get the URL of the Help page for Optimise
        @return url for the help page
     */
    public static java.lang.String getHelpURL(){
        return "sandmark/optimise/doc/optimise.html";
    }

    /*
     *  Describe what optimization is.
     */
    public static java.lang.String getOverview(){
    return "This pane allows you to choose between a number " +
               "of different optimization algorithms to apply to " +
               "your program.";
    }
}

