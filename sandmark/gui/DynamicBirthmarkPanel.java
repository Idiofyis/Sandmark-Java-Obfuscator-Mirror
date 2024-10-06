package sandmark.gui;

public class DynamicBirthmarkPanel extends SkinPanel
   implements SandMarkGUIConstants, AlgorithmPanel,
              sandmark.util.ConfigPropertyChangeListener, SandMarkPanel,
              AlgorithmComboBox.DescriptionListener {

   private javax.swing.JPanel mInsetPanel;
   private javax.swing.JTextField percentSim;

   private SandMarkFrame mFrame;

   private ConfigPropertyPanel mCPP;
   private java.awt.GridBagConstraints mCPPConstraints;

   private AlgorithmComboBox mComboBox;

   private HelpButton mHelpButton;

   private sandmark.util.ConfigProperties mConfigProps =
      sandmark.birthmark.DynamicBirthMarkParameters.createConfigProperties();


   public DynamicBirthmarkPanel(SandMarkFrame frame){
      sandmark.Console.getConfigProperties().addPropertyChangeListener(
         "Input File", this);
      
      mFrame = frame;

      mComboBox = new AlgorithmComboBox(this,
         sandmark.util.classloading.IClassFinder.DYN_BIRTHMARK);
      mComboBox.addListener(this);

      buildInsetPanel();

      mComboBox.setSelectedIndex(0);

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

   public void buildInsetPanel(){

      mInsetPanel = new javax.swing.JPanel();
      mInsetPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder
                               (javax.swing.BorderFactory.createRaisedBevelBorder(),
                                javax.swing.BorderFactory.createLoweredBevelBorder()));
      mInsetPanel.setBackground(SAND_COLOR);

      java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
      mInsetPanel.setLayout(layout);

/*
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
*/

      javax.swing.JButton obfuscateButton = new javax.swing.JButton("Calculate");
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
                     if(getApplication() == null) {
                        sandmark.util.Log.message(0,"Invalid Application");
                        return;
                     }
                     sandmark.birthmark.DynamicBirthmark dbm =
                        (sandmark.birthmark.DynamicBirthmark)mComboBox.getCurrentAlgorithm();
                        sandmark.program.Application app1 = getApplication();
                        sandmark.birthmark.DynamicBirthMarkParameters params =
                           sandmark.birthmark.DynamicBirthMarkParameters.buildParameters
                           (mConfigProps,app1);
                        double percent = dbm.calculate(params);
                        percentSim.setText((new Double(percent)).toString());
                     //}
                     
                                    //sandmark.obfuscate.Obfuscator.runObfuscation
                                    //    (getApplication(),mComboBox.getCurrentAlgorithm());
                                    //getApplication().save(mConfigProps.getProperty("Output File"));
                  } catch(java.io.FileNotFoundException ex) {
                     ex.printStackTrace();
                     sandmark.util.Log.message(0,"Bad file name: " + ex);
                  } catch(Exception ex) {
                     ex.printStackTrace();
                     sandmark.util.Log.message(0,"Birthmark calculation failed: " + ex);
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

/*
      javax.swing.JLabel percentLabel = new javax.swing.JLabel(
         "Percent Similarity:");
      percentLabel.setForeground(DARK_SAND_COLOR);
      gbc.gridx = 1;
      gbc.weightx = 1;
      gbc.gridwidth = 1;
      gbc.fill = java.awt.GridBagConstraints.BOTH;
      layout.setConstraints(percentLabel,gbc);
      mInsetPanel.add(percentLabel);
*/
      //javax.swing.JTextField 
      percentSim = new javax.swing.JTextField();
      percentSim.addActionListener(new java.awt.event.ActionListener(){
         public void actionPerformed(java.awt.event.ActionEvent e){
         }
      });
      percentSim.setEditable(false);
      percentSim.setBackground(java.awt.Color.WHITE);
      gbc.gridx = 1;
      gbc.weightx = 1;
      gbc.gridwidth = 1;
      gbc.fill = java.awt.GridBagConstraints.BOTH;
      //gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
      layout.setConstraints(percentSim,gbc);
      mInsetPanel.add(percentSim);

/*
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
*/

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

      setAlgorithm(getCurrentAlgorithm());
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
      //mCPP = new ConfigPropertyPanel(alg.getConfigProperties());
      //((java.awt.GridBagLayout)mInsetPanel.getLayout()).setConstraints(mCPP,mCPPConstraints);
      sandmark.util.ConfigProperties extraProps = null;
      //if(alg instanceof sandmark.birthmark.DynamicBirthmark){
      //   System.out.println("extra props");
      //   extraProps = mClassConfigProps;
      //}
      mCPP = new ConfigPropertyPanel(
         new sandmark.util.ConfigProperties[]{
            sandmark.Console.getConfigProperties(),
            mConfigProps,extraProps,
            alg.getConfigProperties()
         },sandmark.util.ConfigProperties.PHASE_DYNAMIC_BIRTHMARK,
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
                               Object oldValue, Object newValue) {

      //String inputFile = sandmark.Console.getConfigProperties().getProperty
      //      ("Input File");
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

    public static java.lang.String getOverview(){
    return "This pane allows you to choose between a number " +
               "of different dynamic birthmark algorithms to test for program similarity."; 
    }


}

