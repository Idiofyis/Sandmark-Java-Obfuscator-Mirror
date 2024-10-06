package sandmark.gui;

public class VSplitPanel extends javax.swing.JSplitPane
    implements sandmark.gui.SandMarkGUIConstants {
   private javax.swing.JSplitPane mSplitPane;

    public VSplitPanel(sandmark.program.Application app) {
       super(javax.swing.JSplitPane.VERTICAL_SPLIT);
       setBackground(SAND_COLOR);

        final AppTree classSortTree = new AppTree
           (app,AppTree.SHOW_APPS|AppTree.SHOW_CLASSES|AppTree.SHOW_METHODS,
            javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
        final AppTree methodSortTree = new AppTree
           (app,AppTree.SHOW_APPS|AppTree.SHOW_METHODS,
            javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        classSortTree.selectNode(app);
        methodSortTree.selectNode(app);
        
        final SortPanel classSortPanel = buildSortPanel
           (classSortTree,sandmark.newstatistics.Stats.getClassMetrics());
        final SortPanel methodSortPanel = buildSortPanel
           (methodSortTree,sandmark.newstatistics.Stats.getMethodMetrics());
        
        new ClassSorter(classSortTree,classSortPanel.metrics);
        new ClassSorter(classSortTree,classSortPanel.ops);
        new MethodSorter(methodSortTree,methodSortPanel.metrics);
        new MethodSorter(methodSortTree,methodSortPanel.ops);

        final javax.swing.JTabbedPane tabbedPane = 
           new javax.swing.JTabbedPane();
        tabbedPane.setBackground(SAND_COLOR);
        tabbedPane.addTab("Class Sort",classSortPanel.panel);
        tabbedPane.addTab("Method Sort",methodSortPanel.panel);
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener(){
           public void stateChanged(javax.swing.event.ChangeEvent evt){
              updateViewPanel(tabbedPane.getSelectedIndex() == 0 ? 
                              classSortTree : methodSortTree,
                              (javax.swing.JPanel)
                              tabbedPane.getSelectedComponent());
                }
            });
        
        classSortTree.addTreeSelectionListener
           (new javax.swing.event.TreeSelectionListener() {
              public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
                 javax.swing.JTree tree = (javax.swing.JTree)e.getSource();
                 updateViewPanel(tree,classSortPanel.panel);
              }
           });
        
        methodSortTree.addTreeSelectionListener
           (new javax.swing.event.TreeSelectionListener() {
              public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
                 javax.swing.JTree tree = (javax.swing.JTree)e.getSource();
                 updateViewPanel(tree,methodSortPanel.panel);
              }
           });
        
        //LogPane must be constructed before APpViewPanel because AVP
        //issues a warning.  see bug 185
        LogPane logPane = new LogPane();
        AppViewPanel appViewPanel = new AppViewPanel(app,classSortPanel.panel);
        
        mSplitPane = new javax.swing.JSplitPane
           (javax.swing.JSplitPane.HORIZONTAL_SPLIT,tabbedPane, appViewPanel);
        mSplitPane.setOneTouchExpandable(true);
        mSplitPane.setBackground(SAND_COLOR);

        this.setTopComponent(mSplitPane);
        this.setBottomComponent(logPane);
    }
    
    private static class SortPanel {
       javax.swing.JPanel panel;
       javax.swing.JComboBox metrics;
       javax.swing.JComboBox ops;
       SortPanel(javax.swing.JPanel p,javax.swing.JComboBox m,
                 javax.swing.JComboBox o) 
       { panel = p ; metrics = m ; ops = o; }
    }
    
    private SortPanel buildSortPanel
       (AppTree tree,sandmark.metric.Metric[] metrics) {
       java.awt.GridBagLayout layout = new java.awt.GridBagLayout(); 
       javax.swing.JPanel panel = 
          new javax.swing.JPanel(layout);
       panel.setBackground(SAND_COLOR);
       javax.swing.JComponent classTree = 
          new javax.swing.JScrollPane(tree);
       java.awt.GridBagConstraints constraints = 
          new java.awt.GridBagConstraints();
       constraints.gridx = 0;
       constraints.gridy = 0;
       constraints.gridwidth = 2;
       constraints.weighty = 1.0;
       constraints.weightx = 1.0;
       constraints.anchor = java.awt.GridBagConstraints.CENTER;
       constraints.fill = java.awt.GridBagConstraints.BOTH;
       layout.setConstraints(classTree,constraints);
       panel.add(classTree);

       javax.swing.JLabel metricsLabel = new javax.swing.JLabel("Metrics");
       constraints.gridy = 1;
       constraints.gridwidth = 1;
       constraints.weighty = 0.0;
       constraints.anchor = java.awt.GridBagConstraints.EAST;
       constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
       layout.setConstraints(metricsLabel,constraints);
       panel.add(metricsLabel);
       
       javax.swing.JComboBox comboBox =
          new javax.swing.JComboBox(metrics);
       comboBox.setForeground(DARK_SAND_COLOR);
       comboBox.setBackground(SAND_COLOR);
       constraints.gridx = 1;
       layout.setConstraints(comboBox,constraints);
       panel.add(comboBox);
       
       javax.swing.JLabel opcodeLabel = new javax.swing.JLabel("Opcodes");
       constraints.gridy = 2;
       constraints.gridx = 0;
       layout.setConstraints(opcodeLabel,constraints);
       panel.add(opcodeLabel);

       javax.swing.JComboBox opsComboBox = new javax.swing.JComboBox
          (org.apache.bcel.Constants.OPCODE_NAMES);
       opsComboBox.setForeground(DARK_SAND_COLOR);
       opsComboBox.setBackground(SAND_COLOR);
       constraints.gridx = 1;
       layout.setConstraints(opsComboBox,constraints);
       panel.add(opsComboBox);
       
       return new SortPanel(panel,comboBox,opsComboBox);
    }

    public static javax.swing.JPanel getSandMarkViewPanel(SandMarkFrame smf) {
        return new SandMarkViewPanel(smf);
    }
    private void updateViewPanel(javax.swing.JTree tree,
                                 javax.swing.JPanel selectedTab) {
       javax.swing.tree.TreePath path = tree.getSelectionPath();
       if(path == null)
          return;
       
       sandmark.program.Object object =
          (sandmark.program.Object)path.getLastPathComponent();
       
       ViewPanel newView;
       if(object instanceof sandmark.program.Application)
          newView =
             new AppViewPanel((sandmark.program.Application)object,
                   				selectedTab);
       else if(object instanceof sandmark.program.Class)
          newView = 
             new ClassViewPanel((sandmark.program.Class)object);
       else if(object instanceof sandmark.program.Method)
          newView = 
             new MethodViewPanel((sandmark.program.Method)object,
                                 selectedTab);
       else
          throw new Error("unknown object type " + object.getClass());
       
       ViewPanel oldView = (ViewPanel)mSplitPane.getBottomComponent();
       Object viewState = oldView.saveViewState();
       oldView.tearDown();
       newView.restoreViewState(viewState);
       
       mSplitPane.setBottomComponent((javax.swing.JComponent)newView);
    }
}

class SandMarkViewPanel extends SkinPanel
    implements java.awt.event.ActionListener,SandMarkPanel {
    private SandMarkFrame mySandMarkFrame;

    private ConfigPropertyPanel mCPP;

    public SandMarkViewPanel(SandMarkFrame frame){
        mySandMarkFrame = frame;

        javax.swing.JPanel insetPanel = new javax.swing.JPanel();

        insetPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder
                                 (javax.swing.BorderFactory.createRaisedBevelBorder(),
                                  javax.swing.BorderFactory.createLoweredBevelBorder()));
        insetPanel.setBackground(SandMarkFrame.SAND_COLOR);

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        insetPanel.setLayout(layout);

        mCPP = new ConfigPropertyPanel(new sandmark.util.ConfigProperties[] {
                sandmark.Console.getConfigProperties(),
        },sandmark.util.ConfigProperties.PHASE_ALL,
        mySandMarkFrame.getApplicationTracker());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(3,3,3,3);
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.anchor = java.awt.GridBagConstraints.NORTH;
        layout.setConstraints(mCPP,gbc);
        insetPanel.add(mCPP);

        javax.swing.JButton viewButton = new javax.swing.JButton("View");
        viewButton.addActionListener(this);
        viewButton.setBackground(SandMarkFrame.SAND_COLOR);
        viewButton.setForeground(SandMarkFrame.DARK_SAND_COLOR);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.weightx = 0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        layout.setConstraints(viewButton,gbc);
        insetPanel.add(viewButton);
        
        HelpButton help = new HelpButton("view");
        gbc.gridx = 1;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        layout.setConstraints(help,gbc);
        insetPanel.add(help);

        setLayout(new java.awt.BorderLayout());
        this.add(insetPanel);
    }

    public void actionPerformed(java.awt.event.ActionEvent ev) {
        mCPP.updateProperties();
        sandmark.program.Application app = mySandMarkFrame.getCurrentApplication();
	if(app == null) {
	    sandmark.util.Log.message(0,"Invalid input file");
	    return;
	}
        javax.swing.JFrame frame = new javax.swing.JFrame("View JarFile");
        VSplitPanel vs = new VSplitPanel(app);
        frame.getContentPane().add(vs );
        frame.pack();
        frame.show();
    }

    public String getDescription() {
        return sandmark.view.View.getOverview();
    }

    public SandMarkFrame getFrame() {
        return mySandMarkFrame;
    }
}
