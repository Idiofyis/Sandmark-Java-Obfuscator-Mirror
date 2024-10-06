package sandmark.gui;

public class ObfuscatePanel extends SkinPanel
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
                null,"J","O",
            },
        },null);
    
    private sandmark.util.ConfigProperties mMethodConfigProps =
        new sandmark.util.ConfigProperties(new String[][] {
            {"Methods","","The methods to obfuscate.",null,"M","O",},  
        },null);
    
    private sandmark.util.ConfigProperties mClassConfigProps =
        new sandmark.util.ConfigProperties(new String[][] {
            {"Classes","","The classes to obfuscate.",null,"C","O",},  
        },null);

    public ObfuscatePanel(SandMarkFrame frame) {
        sandmark.Console.getConfigProperties().addPropertyChangeListener
            ("Input File",this);

        mFrame = frame;

        mComboBox = new AlgorithmComboBox
            (this,sandmark.util.classloading.IClassFinder.GEN_OBFUSCATOR);
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


        javax.swing.JButton obfuscateButton = new javax.swing.JButton("Obfuscate");
        obfuscateButton.setBackground(SAND_COLOR);
        obfuscateButton.setForeground(DARK_SAND_COLOR);
        obfuscateButton.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent e) {
              mFrame.setAllEnabled(false);
              sandmark.util.graph.graphview.GraphList.instance().clear();
              Thread obfuscationRunner = new Thread() {
                 public void run() {
                    try {
                       mCPP.updateProperties();
                       sandmark.program.Application app = getApplication();
                       if(app == null) {
                          sandmark.util.Log.message(0,"Invalid Application");
                          return;
                       }
                       sandmark.Algorithm alg = mComboBox.getCurrentAlgorithm();
                       if(alg instanceof sandmark.obfuscate.AppObfuscator)
                          ((sandmark.obfuscate.AppObfuscator)alg).apply(app);
                       else if(alg instanceof sandmark.obfuscate.ClassObfuscator) {
                          sandmark.program.Class classes[] = (sandmark.program.Class [])
                             mClassConfigProps.getValue("Classes");
                          if(classes == null)
                             for(java.util.Iterator classIt = app.classes() ; 
                                 classIt.hasNext() ; )
                                ((sandmark.obfuscate.ClassObfuscator)alg).apply
                                   ((sandmark.program.Class)classIt.next());
                          else
                             for(int i = 0 ; i < classes.length ; i++)
                                ((sandmark.obfuscate.ClassObfuscator)alg).apply(classes[i]);
                       } else if(alg instanceof sandmark.obfuscate.MethodObfuscator) {
                          sandmark.program.Method methods[] = (sandmark.program.Method [])
                             mMethodConfigProps.getValue("Methods");
                          if(methods == null)
                             for(java.util.Iterator methodIt = 
                                 new sandmark.program.util.AllMethods(app); methodIt.hasNext() ; )
                                ((sandmark.obfuscate.MethodObfuscator)alg).apply
                                   ((sandmark.program.Method)methodIt.next());
                          else
                             for(int i = 0 ; i < methods.length ; i++)
                                ((sandmark.obfuscate.MethodObfuscator)alg).apply(methods[i]);
                       } else
                          throw new Error("Unknown obfuscation type " + alg.getClass());
                       getApplication().save(mConfigProps.getProperty("Output File"));
                    } catch(java.io.FileNotFoundException ex) {
                       ex.printStackTrace();
                       sandmark.util.Log.message(0,"Bad file name: " + ex);
                    } catch(Exception ex) {
                       ex.printStackTrace();
                       sandmark.util.Log.message(0,"Obfuscation failed: " + ex);
                    } finally {
                       mFrame.setAllEnabled(true);
                    }
                 }
              };
              obfuscationRunner.start();
           }
        });

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(3,3,3,3);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;

        layout.setConstraints(obfuscateButton,gbc);
        mInsetPanel.add(obfuscateButton);

        mGraphButton = new GraphDisplayButton(mFrame);
        mGraphButton.setVisible(false);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        layout.setConstraints(mGraphButton, gbc);
        mInsetPanel.add(mGraphButton);

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
        sandmark.util.ConfigProperties extraProps = null;
        if(alg instanceof sandmark.obfuscate.MethodObfuscator)
            extraProps = mMethodConfigProps;
        else if(alg instanceof sandmark.obfuscate.ClassObfuscator)
            extraProps = mClassConfigProps;
        mCPP = new ConfigPropertyPanel( 
                new sandmark.util.ConfigProperties[] {
                        sandmark.Console.getConfigProperties(),
                        mConfigProps,extraProps,
                        alg.getConfigProperties()
                },sandmark.util.ConfigProperties.PHASE_OBFUSCATE,
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
             sandmark.Console.constructOutputFileName(in.toString(),"obf"));
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
       mFrame.setDescription(mDescription);
    }
    public void showTransientDescription(String description) {
       mFrame.setDescription(description);
    }
    public void algorithmChanged(sandmark.Algorithm alg) {
       mDescription = alg.getDescription();
    }

    /**
     *  Get the HTML codes of the About page for Obfuscate
        @return HTML code for the about page
     */
    public static java.lang.String getAboutHTML(){
        return
            "<HTML><BODY>" +
            "<CENTER><B>List of Obfuscators</B></CENTER>" +
            "</BODY></HTML>";
    }

    /**
     *  Get the URL of the Help page for Obfuscate
        @return url for the help page
     */
    public static java.lang.String getHelpURL(){
        return "sandmark/obfuscate/doc/obfuscate.html";
    }

    /*
     *  Describe what obfuscation is.
     */
    public static java.lang.String getOverview(){
    return "This pane allows you to choose between a number " +
               "of different obfuscation algorithms to apply to " +
               "your program.";
    }
}

