package sandmark.gui;

public class HelpDialog extends javax.swing.JFrame
    implements java.awt.event.ActionListener, javax.swing.event.TreeSelectionListener,
    javax.swing.event.HyperlinkListener, sandmark.gui.SandMarkGUIConstants {

    private SMAlgHierTreePane myTree;
    private javax.swing.JSplitPane splitPane;
    private final java.awt.Dimension windowSize = new java.awt.Dimension(1000, 700);
    private final java.awt.Dimension minimumSize1 = new java.awt.Dimension(150, 400);
    private final java.awt.Dimension minimumSize2 = new java.awt.Dimension(350, 400);
    private javax.swing.JEditorPane editorPane = null;

    public HelpDialog(){
	setTitle("Help");
	setSize(windowSize);
	setResizable(true);
	setDefaultCloseOperation(HIDE_ON_CLOSE);

	splitPane =  new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
	splitPane.setDividerLocation(200);
	splitPane.setPreferredSize(windowSize);
	splitPane.setContinuousLayout(true);

	/* construct the tree pane */
	myTree = new SMAlgHierTreePane();
	myTree.addTreeSelectionListener(this);
	javax.swing.JScrollPane sp1 = new javax.swing.JScrollPane(myTree);
	sp1.setMinimumSize(minimumSize1);
	splitPane.setTopComponent(sp1);
	/* construct an empty editor pane to start */
	editorPane = new javax.swing.JEditorPane();
	editorPane.setEditable(false);
	editorPane.addHyperlinkListener(this);
	javax.swing.JScrollPane sp2 = new javax.swing.JScrollPane(editorPane);
	sp2.setMinimumSize(minimumSize2);
	splitPane.setBottomComponent(sp2);

	java.awt.Container contentPane = getContentPane();
	contentPane.setLayout(new java.awt.BorderLayout());
	contentPane.add(splitPane, java.awt.BorderLayout.CENTER);
	pack();
    }

    public void valueChanged(javax.swing.event.TreeSelectionEvent e){
	SMAlgHierTreeNode selectedNode =
	    (SMAlgHierTreeNode)myTree.getLastSelectedPathComponent();
	if(selectedNode != null) {
	    displayPage(selectedNode.helpURL()); 
	}
    }

    public void actionPerformed(java.awt.event.ActionEvent e){
    }
    
    public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {
       gotoLink(e);
    }
    
    private void gotoLink(javax.swing.event.HyperlinkEvent e) {
       if(e.getEventType() != 
          javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)
          return;
       java.net.URL clicked = e.getURL();
       displayPage(clicked);
    }
    
    private void displayPage(java.lang.String page) {
       java.net.URL url = page == null ? null :
          getClass().getClassLoader().getResource(page);
       displayPage(url);
    }
    
    private void displayPage(java.net.URL url) {
       try {
          editorPane.setPage(url);
       } catch(java.io.IOException e) {
          try {
             editorPane.setPage
             (getClass().getClassLoader().getResource
                   ("sandmark/html/error.html"));
          } catch(java.io.IOException ex) {
             throw new RuntimeException();
          }
       }	
    }
    public void showHelpFor(String helpKey) {
	myTree.selectNode(helpKey);
    }
}

