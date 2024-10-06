package sandmark.gui;

public class SandMarkFrame extends javax.swing.JFrame
    implements sandmark.gui.SandMarkGUIConstants {

    private javax.swing.JTabbedPane tabs;

    private javax.swing.JTextArea logText;
    private javax.swing.JTextArea descText;

    private javax.swing.JMenuBar menuBar;

    private CurrentApplicationTracker mApplicationTracker;

    static {
        MultiLineToolTipUI.initialize(); // See comment in MultiLineToolTipUI
        javax.swing.ToolTipManager.sharedInstance().setInitialDelay(0);
    }

    public SandMarkFrame() {
        super(TITLE);

        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        menuBar = new SandMarkMenuBar(this);
        setJMenuBar(menuBar);
        javax.swing.JScrollPane logPane = new LogPane();
        sandmark.util.Log.addLog(System.out,0);

        descText = new javax.swing.JTextArea() {
                public java.awt.Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
        descText.setEditable(false);
        descText.setRows(4);
        descText.setColumns(60);
        descText.setLineWrap(true);
        descText.setWrapStyleWord(true);
        descText.setMargin(new java.awt.Insets(3,3,3,3));
        javax.swing.JScrollPane descPane = new javax.swing.JScrollPane(descText);

        mApplicationTracker = new CurrentApplicationTracker();

        tabs = new javax.swing.JTabbedPane(javax.swing.SwingConstants.TOP);
        tabs.setBackground(SAND_COLOR);
        tabs.setForeground(DARK_SAND_COLOR);
        addTabs(tabs);

        tabs.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                   showDescription();
                    sandmark.util.graph.graphview.GraphList.instance().clear();
                }
            });
        tabs.setSelectedIndex(0);

        javax.swing.JSplitPane tabsAndDescPane = new javax.swing.JSplitPane
            (javax.swing.JSplitPane.VERTICAL_SPLIT);

        tabsAndDescPane.setTopComponent(tabs);
        tabsAndDescPane.setBottomComponent(descPane);
        tabsAndDescPane.setOneTouchExpandable(true);
        tabsAndDescPane.setBackground(SAND_COLOR);

        javax.swing.JSplitPane splitAndLogPane = new javax.swing.JSplitPane
            (javax.swing.JSplitPane.VERTICAL_SPLIT);

        splitAndLogPane.setTopComponent(tabsAndDescPane);
        splitAndLogPane.setBottomComponent(logPane);
        splitAndLogPane.setOneTouchExpandable(true);
        splitAndLogPane.setBackground(SAND_COLOR);

        setContentPane(splitAndLogPane);
        pack();

        showDescription();
    }
    
    protected void addTabs(javax.swing.JTabbedPane tabpane) {
       tabpane.add("Home", new HomePanel(this));
       tabpane.add("Dynamic Watermark", new DynamicWatermarkPanel(this));
       tabpane.add("Static Watermark", new StaticWatermarkPanel(this));
       tabpane.add("Obfuscate", new ObfuscatePanel(this));
       tabpane.add("Optimize", new OptimisePanel(this));
       tabpane.add("Diff", new sandmark.gui.diff.DiffPanel(this));
       tabpane.add("View", VSplitPanel.getSandMarkViewPanel(this));
       tabpane.add("Decompile", new DecompilePanel(this));
       tabpane.add("Quick Protect", new QuickProtectPanel(this));
       tabpane.add("Static Birthmark", new StaticBirthmarkPanel(this));
       tabpane.add("Dynamic Birthmark", new DynamicBirthmarkPanel(this));
    }
    private void showDescription() {
       SandMarkPanel panel = getDisplayedPanel();
       setDescription(panel == null ? "" : panel.getDescription());
    }
    public void setDescription(String description) {
        descText.setText(description);
        descText.setCaretPosition(0);
    }
    public void addLogEntry(String str){
        logText.append(str + "\n");
        logText.setCaretPosition(logText.getText().length());
    }

    void setTabsEnabled(boolean enabled) {
        for(int i = 0 ; i < tabs.getTabCount() ; i++)
            tabs.setEnabledAt(i,enabled);
    }
    public sandmark.program.Application getCurrentApplication() {
        return mApplicationTracker.getCurrentApplication();
    }
    public CurrentApplicationTracker getApplicationTracker() {
        return mApplicationTracker;
    }
    public SandMarkPanel getDisplayedPanel() {
        return (SandMarkPanel)tabs.getSelectedComponent();
    }

    public javax.swing.JTabbedPane getTabs () {
        return tabs;
    }

    public void setAllEnabled(boolean enabled) {
        java.util.ArrayList bfsQueue = new java.util.ArrayList();
        bfsQueue.add(this);

        while(bfsQueue.size() != 0) {
            java.awt.Component comp = (java.awt.Component)bfsQueue.remove(0);
            comp.setEnabled(enabled);
            if(comp instanceof java.awt.Container)
                bfsQueue.addAll(java.util.Arrays.asList
                                (((java.awt.Container)comp).getComponents()));
        }
    }
    
    protected static void start(Class frameType) throws Exception {
    	String errors = null;
    	try {
    		sandmark.Console.sanityCheck();
    	} catch(UnsupportedOperationException e) {
    		errors = e.getMessage();
    	}
        //  this creates a splash image that is displayed until the GUI is loaded
        //  there is one more line at the bottom to set the window invisible
        javax.swing.JWindow jw = new javax.swing.JWindow();
        jw.getContentPane().add(new sandmark.gui.SplashPanel());
        jw.pack();
        jw.setLocation((int)((java.awt.Toolkit.getDefaultToolkit()
                              .getScreenSize().getWidth() - jw.getWidth()) / 2),
                       (int)((java.awt.Toolkit.getDefaultToolkit().
                              getScreenSize().getHeight() - jw.getHeight()) / 2));
        jw.setVisible(true);

        SandMarkFrame smFrame = ((SandMarkFrame)frameType.newInstance());
        smFrame.setVisible(true);
        if(errors != null) {
           String message =
              "The following SandMark dependencies are " +
              " unsatisfied.  You should exit SandMark, " +
              "satisfy them, and restart SandMark.\n\n" + errors;
           String title = "Unsatisfied SandMark Dependencies";
           final javax.swing.JDialog dialog = 
              new javax.swing.JDialog(smFrame,title,true);
           dialog.getContentPane().setBackground(SAND_COLOR);
           javax.swing.JTextArea text = new javax.swing.JTextArea();
           text.setColumns(40);
           text.setLineWrap(true);
           text.setWrapStyleWord(true);
           text.setText(message);
           text.setEditable(false);
           text.setRows(text.getText().length()/40);
           text.setBackground(SAND_COLOR);
           java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
           dialog.getContentPane().setLayout(layout);
           java.awt.GridBagConstraints constraints =
              new java.awt.GridBagConstraints();
           constraints.anchor = java.awt.GridBagConstraints.CENTER;
           constraints.fill = java.awt.GridBagConstraints.BOTH;
           constraints.gridx = 0;
           constraints.gridy = 0;
           constraints.weighty = 1.0;
           constraints.gridwidth = 2;
           layout.setConstraints(text,constraints);
           dialog.getContentPane().add(text);
           constraints.fill = java.awt.GridBagConstraints.NONE;
           constraints.weighty = 0.0;
           constraints.gridy = 1;
           constraints.gridwidth = 1;
           constraints.weightx = 1.0;
           constraints.anchor = java.awt.GridBagConstraints.EAST;
           java.awt.event.ActionListener closer = 
              new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                 dialog.setVisible(false);
              }
           };
           HelpButton help = new HelpButton("running SandMark","Details");
           help.setBackground(SAND_COLOR);
           help.addActionListener(closer);
           layout.setConstraints(help,constraints);
           dialog.getContentPane().add(help);
           javax.swing.JButton cancel = new javax.swing.JButton("Cancel");
           cancel.setBackground(SAND_COLOR);
           cancel.setForeground(DARK_SAND_COLOR);
           cancel.addActionListener(closer);
           constraints.gridx = 1;
           constraints.anchor = java.awt.GridBagConstraints.WEST;
           layout.setConstraints(cancel,constraints);
           dialog.getContentPane().add(cancel);
           dialog.pack();
           dialog.setVisible(true);
        } 

        //  sets the splash panel invisible
        jw.setVisible(false);
    }

    /**
     * Execution begins here for interactive runs of SandMark.
     @param   args
    */
    public static void main(String args[]) throws Exception {
    	start(SandMarkFrame.class);
    }

}
