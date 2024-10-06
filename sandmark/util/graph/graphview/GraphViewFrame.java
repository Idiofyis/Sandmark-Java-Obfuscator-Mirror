// GraphViewFrame.java

package sandmark.util.graph.graphview;

/**
 * This is a frame for visualizing graphs.  The user needs to create an object
 * of type {@link GraphList}, that contains graphs that can later be drawn.
 * The list will also contain styles and names of the graphs.
 *
 * @author Andrzej
 */
public class GraphViewFrame extends javax.swing.JFrame
    implements sandmark.gui.SandMarkGUIConstants, java.awt.event.ActionListener {

    /**
     * Creates a new frame with a list of graphs.
     * 
     * @param parent the parent window of this frame
     * @param graphList list of graphs that can be drawn
     */
    public GraphViewFrame(javax.swing.JFrame parent, GraphList graphList) {
        super();
        setTitle("Graph Display");
        setResizable(true);
        setSize(WINDOW_SIZE);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        myGraphList = graphList;

        // node Information area
        textArea = new javax.swing.JTextArea("NODE INFORMATION");
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
        textArea.setEditable(false);
	textArea.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createRaisedBevelBorder(),
									  javax.swing.BorderFactory.createLoweredBevelBorder()));
	textArea.setBackground(SAND_COLOR);
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(textArea);
        sp.setPreferredSize(TEXT_AREA_SIZE);

        // user controls
        createUserControls();

        // graph display area
        myGraphDisplayPanel = new GraphPanel(myGraph, myGraphStyle, myLayoutStyle, textArea);
	myGraphDisplayPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createRaisedBevelBorder(),
										     javax.swing.BorderFactory.createLoweredBevelBorder()));
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(myGraphDisplayPanel);
        scrollPane.setPreferredSize(PANEL_SIZE);

	mySlider.setGraphDisplayPanel(myGraphDisplayPanel);
        
        java.awt.Container contentPane = getContentPane();
        contentPane.setLayout(new java.awt.BorderLayout());
        //contentPane.add(scrollPane, java.awt.BorderLayout.CENTER);
        //contentPane.add(sp, java.awt.BorderLayout.SOUTH);

	myUserControls.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createRaisedBevelBorder(), javax.swing.BorderFactory.createLoweredBevelBorder()));

        //contentPane.add(myUserControls, java.awt.BorderLayout.WEST);
	javax.swing.JSplitPane splitPane
	  = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT,
				       myUserControls, scrollPane);
	splitPane.setOneTouchExpandable(true);
	
	javax.swing.JSplitPane mainSplitPane
	  = new javax.swing.JSplitPane(javax.swing.JSplitPane.VERTICAL_SPLIT,
				       splitPane, sp);
	mainSplitPane.setOneTouchExpandable(true);
	mainSplitPane.setResizeWeight(0.8);

	contentPane.add(mainSplitPane);

        pack();

	if (parent != null) {
	    java.awt.Point parentLoc = parent.getLocation();
	    //java.awt.Dimension parentDim = parent.getSize();
	    //setLocation(parentLoc.x + (parentDim.width - getSize().width) / 2,
	    //		parentLoc.y + (parentDim.height - getSize().height) / 2);
	    setLocation(parentLoc.x + 50, parentLoc.y + 50);
	}
    }

    /**
     * Creates a new frame with a list of graphs.
     * 
     * @param graphList list of graphs that can be drawn
     */
    public GraphViewFrame(GraphList graphList) {
	this(null, graphList);
    }

    // creates controls
    private void createUserControls() {
        myUserControls = new javax.swing.JPanel();
        myUserControls.setPreferredSize(CONTROLS_PANEL_SIZE);
        myUserControls.setBackground(SAND_COLOR);
        myUserControls.setLayout(new java.awt.BorderLayout());

        // list of graphs
        javax.swing.JPanel northLabelPanel = new javax.swing.JPanel();
        northLabelPanel.setPreferredSize(new java.awt.Dimension(50, 30));
        northLabelPanel.setBackground(SAND_COLOR);
        javax.swing.JLabel northLabel = new javax.swing.JLabel("Graphs");
        northLabelPanel.add(northLabel);
        myUserControls.add(northLabelPanel, java.awt.BorderLayout.NORTH);

        java.lang.Object[] graphs = myGraphList.getGraphNames();
        javax.swing.JList graphList = new javax.swing.JList(graphs);
        graphList.setBackground(SAND_COLOR);
        graphList.setSelectedIndex(0);
        myGraph = myGraphList.getGraph(0);
	myGraphStyle = myGraphList.getStyle(0);
        graphList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                    int graphIndex = ((javax.swing.JList)e.getSource()).getSelectedIndex();
                    myGraph = myGraphList.getGraph(graphIndex);
                    myGraphStyle = myGraphList.getStyle(graphIndex);
                }
            });
        javax.swing.JScrollPane graphListScroll = new javax.swing.JScrollPane(graphList);
        myUserControls.add(graphListScroll, java.awt.BorderLayout.CENTER);


        // south controls
        javax.swing.JLabel layoutType = new javax.swing.JLabel("Layout Type");

        // comboBox with layout styles
        java.lang.String[] layoutStyles = {"Layered Drawing", "Force Directed", "Random"};
        //java.lang.String[] layoutStyles = {"Layered Drawing", "Force Directed", "Tree Layout", "Class Hierarchy", "Random"};
        javax.swing.JComboBox graphLayoutStyle = new javax.swing.JComboBox(layoutStyles);
        myLayoutStyle = GraphLayout.LAYERED_DRAWING_LAYOUT;
        graphLayoutStyle.setBackground(SAND_COLOR);
        graphLayoutStyle.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    java.lang.String style = 
                        (java.lang.String)((javax.swing.JComboBox)e.getSource()).getSelectedItem();
                    if (style.equals("Layered Drawing"))
                        myLayoutStyle = GraphLayout.LAYERED_DRAWING_LAYOUT;
                    else if (style.equals("Force Directed"))
                        myLayoutStyle = GraphLayout.SPRING_EMBEDDER_LAYOUT;
                    else if (style.equals("Tree Layout"))
                        myLayoutStyle = GraphLayout.TREE_LAYOUT;
                    else if (style.equals("Class Hierarchy"))
                        myLayoutStyle = GraphLayout.HIERARCHY_TREE_LAYOUT;
                    else if (style.equals("Random"))
                        myLayoutStyle = GraphLayout.SIMPLE_GRAPH_LAYOUT;
                }
            });

        javax.swing.JPanel southPanel = new javax.swing.JPanel();

        southPanel.setPreferredSize(new java.awt.Dimension(150, 200));
        southPanel.setBackground(SAND_COLOR);
        //southPanel.setLayout(new java.awt.GridLayout(6, 1));
	java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
	java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
	southPanel.setLayout(gridbag);

        //southPanel.add(new javax.swing.JLabel());
	gc.insets = new java.awt.Insets(10, 0, 5, 0);
        gc.gridx = 0;
	gc.gridy = 0;
	gridbag.setConstraints(layoutType, gc);
	southPanel.add(layoutType);

	gc.insets = new java.awt.Insets(5, 0, 10, 0);
        gc.gridx = 0;
	gc.gridy = 1;
	gridbag.setConstraints(graphLayoutStyle, gc);
        southPanel.add(graphLayoutStyle);

	mySlider = new GraphZoomSlider(myGraphDisplayPanel);
	gc.insets = new java.awt.Insets(10, 0, 10, 0);
	gc.gridx = 0;
	gc.gridy = 2;
	gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridbag.setConstraints(mySlider, gc);
	southPanel.add(mySlider);

        // button to draw a graph
        javax.swing.JButton drawGraphButton = new javax.swing.JButton("Draw Graph");
        drawGraphButton.setBackground(SAND_COLOR);
        drawGraphButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    myGraphDisplayPanel.setLayout(myLayoutStyle, myGraph, myGraphStyle);
		    mySlider.reset();
                }
            });
        javax.swing.JPanel drawGraphButtonPanel = new javax.swing.JPanel();
        drawGraphButtonPanel.setBackground(SAND_COLOR);
        //drawGraphButtonPanel.setPreferredSize(new java.awt.Dimension(100, 40));
	drawGraphButtonPanel.add(drawGraphButton);
        
	gc.insets = new java.awt.Insets(10, 0, 10, 0);
	gc.gridx = 0;
	gc.gridy = 3;
	gridbag.setConstraints(drawGraphButtonPanel, gc);
	southPanel.add(drawGraphButtonPanel);

        myUserControls.add(southPanel, java.awt.BorderLayout.SOUTH);
	
	createMenuBar();
    }

    // creates the menu bar for this window
    private void createMenuBar() {
	javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();

        javax.swing.JMenu fileMenu = new javax.swing.JMenu("File");
        fileMenu.setBackground(SAND_COLOR);
        fileMenu.setMnemonic('F');
 
        mySaveGraphItem = new javax.swing.JMenuItem("Save Graph");
        mySaveGraphItem.setBackground(SAND_COLOR);
        mySaveGraphItem.setMnemonic('s');
        fileMenu.add(mySaveGraphItem);
        mySaveGraphItem.addActionListener(this);

        myExitItem = new javax.swing.JMenuItem("Exit");
        myExitItem.setBackground(SAND_COLOR);
        myExitItem.setMnemonic('x');
        fileMenu.add(myExitItem);
        myExitItem.addActionListener(this);
 
        javax.swing.JMenu helpMenu = new javax.swing.JMenu("Help");
        helpMenu.setBackground(SAND_COLOR);
        helpMenu.setMnemonic('H');
 
        myAboutItem = new javax.swing.JMenuItem("About");
        myAboutItem.setBackground(SAND_COLOR);
        myAboutItem.setMnemonic('A');
        helpMenu.add(myAboutItem);
        myAboutItem.addActionListener(this);
 
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        menuBar.setBackground(SAND_COLOR);
	this.setJMenuBar(menuBar);
    }

    /**
     * Invoked when an action occurs in this frame.
     *
     * @param e event that occured
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
	java.lang.Object source = e.getSource();
	if (source == mySaveGraphItem) {
	    javax.swing.JFileChooser fc = new javax.swing.JFileChooser(".");
	    fc.setFileFilter(new sandmark.gui.ExtensionFileFilter("dot", "Dot Files (*.dot)"));
	    int result = fc.showSaveDialog(this);
	    if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
		sandmark.util.newgraph.Graphs.dotInFile(myGraph, myGraphStyle,
							fc.getSelectedFile().getAbsolutePath());
	    }
	} else if (source == myExitItem) {
	    setVisible(false);
	    dispose();
	} else if (source == myAboutItem) {
	    javax.swing.JOptionPane.showMessageDialog(this, "Graph Visualization for SandMark\n\u00A92003");
	}
    }

    private static final java.awt.Dimension WINDOW_SIZE = new java.awt.Dimension(700, 500);
    private static final java.awt.Dimension PANEL_SIZE = new java.awt.Dimension(600, 420);
    private static final java.awt.Dimension TEXT_AREA_SIZE = new java.awt.Dimension(700, 80);
    private static final java.awt.Dimension CONTROLS_PANEL_SIZE = new java.awt.Dimension(100, 500);

    private sandmark.util.newgraph.Graph myGraph;
    private sandmark.util.newgraph.Style myGraphStyle;
    private GraphList myGraphList;
    private int myLayoutStyle;
    private GraphPanel myGraphDisplayPanel;
    private javax.swing.JTextArea textArea;
    private javax.swing.JPanel myUserControls;
    private javax.swing.JMenuItem mySaveGraphItem;
    private javax.swing.JMenuItem myAboutItem;
    private javax.swing.JMenuItem myExitItem;
    private GraphZoomSlider mySlider;
}
