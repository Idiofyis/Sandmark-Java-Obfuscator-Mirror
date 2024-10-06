package sandmark.gui;

public class QuickProtectPanel extends SkinPanel implements
   SandMarkGUIConstants,AlgorithmPanel,
   sandmark.util.ConfigPropertyChangeListener,SandMarkPanel {
   
   private class QuickProtectComboBox extends javax.swing.JComboBox 
      implements java.awt.event.ActionListener {
      QuickProtectComboBox() {
         setBackground(SAND_COLOR);
         java.util.Collection qps = 
            sandmark.util.classloading.ClassFinder.getClassesWithAncestor
            (sandmark.util.classloading.IClassFinder.QUICK_PROTECT);
         for(java.util.Iterator qpIt = qps.iterator(); qpIt.hasNext() ; ) {
            String qpClassName = (String)qpIt.next();
            try {
               Object qp = (sandmark.wizard.quickprotect.QuickProtect)
                  Class.forName(qpClassName).newInstance();
               this.addItem(qp);
            } catch(ClassNotFoundException e) {
               //not really a QuickProtect
            } catch(InstantiationException e) {
               //not really a QuickProtect
            } catch(IllegalAccessException e) {
               //not really a QuickProtect
            }
         }
         this.setSelectedIndex(0);
         QuickProtectPanel.this.setQuickProtect
            ((sandmark.wizard.quickprotect.QuickProtect)this.getSelectedItem());
         addActionListener(this);
      }
      
      public void actionPerformed(java.awt.event.ActionEvent e) {
         sandmark.wizard.quickprotect.QuickProtect qp =
            (sandmark.wizard.quickprotect.QuickProtect)getSelectedItem();
         QuickProtectPanel.this.setQuickProtect(qp);
      }
   }

   private sandmark.gui.SandMarkFrame myFrame;
   private sandmark.gui.ConfigPropertyPanel myCPP;
   private java.awt.GridBagConstraints myCPPConstraints;
   private javax.swing.JPanel mInsetPanel;

   //this will change with combo box
   private sandmark.wizard.quickprotect.QuickProtect quickProtect;

   private HelpButton mHelpButton;
   private GraphDisplayButton mGraphButton;

   private sandmark.util.ConfigProperties mConfigProps = new sandmark.util.ConfigProperties(
      new String[][] { 
         {"Output File","","The output jar-file.",null,"J","A",},
      },
      null);

   private static java.util.WeakHashMap sCPToName = new java.util.WeakHashMap();

   public QuickProtectPanel(sandmark.gui.SandMarkFrame frame) {
      myFrame = frame;

      sandmark.Console.getConfigProperties()
         .addPropertyChangeListener("Input File",this);

      buildInsetPanel();

      javax.swing.JLabel algorithmLabel = new javax.swing.JLabel(
         "Obfuscate and Watermark");
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

      javax.swing.JComboBox qpComboBox = new QuickProtectComboBox();
      add(qpComboBox,gbc);

      gbc.gridy = 1;
      gbc.gridx = 0;
      gbc.gridwidth = 4;
      gbc.weighty = 1.0;
      gbc.fill = java.awt.GridBagConstraints.BOTH;

      add(mInsetPanel,gbc);

   }

   public void buildInsetPanel() {

      mInsetPanel = new javax.swing.JPanel();
      mInsetPanel.setBorder(javax.swing.BorderFactory
         .createCompoundBorder(javax.swing.BorderFactory
            .createRaisedBevelBorder(),javax.swing.BorderFactory
            .createLoweredBevelBorder()));

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
               myCPP.updateProperties();
               if(getApplication() == null) {
                  sandmark.util.Log.message(0,"Please select a valid application");
                  return;
               }
               sandmark.gui.ObfDialog obd = new sandmark.gui.ObfDialog(myFrame,
                  getApplication());
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

      javax.swing.JButton protectButton = new javax.swing.JButton("Protect");
      protectButton.setBackground(SAND_COLOR);
      protectButton.setForeground(DARK_SAND_COLOR);
      protectButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
            myCPP.updateProperties();
            myFrame.setAllEnabled(false);
            sandmark.util.graph.graphview.GraphList.instance().clear();
            Thread obfuscationRunner = new Thread() {
               public void run() {
                  try {
                     myCPP.updateProperties();
                     sandmark.program.Application app =
                        getApplication();
                     if(app == null) {
                        sandmark.util.Log.message(0,"Invalid Application");
                        return;
                     }
                     sandmark.wizard.ObjectProvider op =
                        new sandmark.wizard.DefaultObjectProvider();
                     op.addObject(app);
                     sandmark.wizard.AlgorithmProvider ap =
                        new sandmark.wizard.DefaultAlgorithmProvider();
                     quickProtect.run(ap,op);
                     app.save(mConfigProps.getProperty("Output File"));
                  } catch(java.io.FileNotFoundException ex) {
                     ex.printStackTrace();
                     sandmark.util.Log.message(0,"Bad file name: " + ex);
                  } catch(Exception ex) {
                     ex.printStackTrace();
                     sandmark.util.Log.message(0,"Protection failed: " + ex);
                  } finally {
                     myFrame.setAllEnabled(true);
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

      layout.setConstraints(protectButton,gbc);
      mInsetPanel.add(protectButton);

      mGraphButton = new GraphDisplayButton(myFrame);
      mGraphButton.setVisible(false);
      gbc.gridx = 2;
      gbc.weightx = 0;
      gbc.anchor = java.awt.GridBagConstraints.SOUTHEAST;
      layout.setConstraints(mGraphButton,gbc);
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

      mHelpButton = new HelpButton("quick protect");
      gbc.gridx = 4;
      gbc.weightx = 0;
      layout.setConstraints(mHelpButton,gbc);
      mInsetPanel.add(mHelpButton);
   }
   
   void setQuickProtect(sandmark.wizard.quickprotect.QuickProtect qp) {
      quickProtect = qp;
      
      if(myCPP != null)
         mInsetPanel.remove(myCPP);

      sandmark.wizard.AlgorithmProvider ap = 
         new sandmark.wizard.DefaultAlgorithmProvider();
      qp.filter(ap);
      sandmark.Algorithm algs[] = ap.getAlgorithms();
      sandmark.util.ConfigProperties cps[] = new sandmark.util.ConfigProperties[algs.length + 2];
      cps[0] = sandmark.Console.getConfigProperties();
      cps[1] = mConfigProps;
      for(int i = 0; i < algs.length; i++) {
         cps[i + 2] = algs[i].getConfigProperties();
         if(algs[i].getConfigProperties() != null)
            sCPToName.put(algs[i].getConfigProperties(),algs[i].getShortName());
      }

      myCPP = new ConfigPropertyPanel(cps,
         sandmark.util.ConfigProperties.PHASE_STATIC_EMBED
            | sandmark.util.ConfigProperties.PHASE_OBFUSCATE,myFrame
            .getApplicationTracker()) {
         protected String getToolTip(sandmark.util.ConfigProperties cp,
                                     String propName) {
            String desc = cp.getDescription(propName);
            String algName = (String)sCPToName.get(cp);
            return propName + (algName == null ? "" : " (See " + algName + ") ")
               + ": " + desc;
         }
      };

      myCPPConstraints = new java.awt.GridBagConstraints();
      myCPPConstraints.gridy = 0;
      myCPPConstraints.gridx = 0;
      myCPPConstraints.gridwidth = 5;
      myCPPConstraints.weighty = 1.0;
      myCPPConstraints.insets = new java.awt.Insets(3,3,3,3);
      myCPPConstraints.fill = java.awt.GridBagConstraints.BOTH;
      myCPPConstraints.anchor = java.awt.GridBagConstraints.NORTH;
      ((java.awt.GridBagLayout)mInsetPanel.getLayout())
         .setConstraints(myCPP,myCPPConstraints);
      mInsetPanel.add(myCPP);
      this.validate();
   }

   public sandmark.program.Application getApplication() throws Exception {
      return myFrame.getCurrentApplication();
   }

   public ConfigPropertyPanel getCPP() {
      return myCPP;
   }

   public sandmark.Algorithm getCurrentAlgorithm() {
      return null;
   }

   public void setAlgorithm(sandmark.Algorithm a) {
   }

   public void propertyChanged(sandmark.util.ConfigProperties cp,
                               String propertyName,Object oldValue,
                               Object newValue) {
      java.io.File in = (java.io.File)newValue;
      if(in.exists()) {
         mConfigProps.setProperty("Output File",sandmark.Console
            .constructOutputFileName(in.toString(),"res"));
      }

   }

   public sandmark.gui.SandMarkFrame getFrame() {
      return myFrame;
   }

   public String getDescription() {
      return "Quick Protect will help you Obfuscate and Watermark "
         + "your program automatically.  The fields displayed above "
         + "configure all of the algorithms that Quick Protect may run.  "
         + "See help for the individual algorithms for more information "
         + "about specific configuration parameters.";
   }

   public static String getHelpURL() {
      return "sandmark/wizard/quickprotect/doc/help.html";
   }
}
