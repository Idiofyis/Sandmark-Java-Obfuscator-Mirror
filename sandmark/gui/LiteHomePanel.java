package sandmark.gui;

public class LiteHomePanel extends SkinPanel implements SandMarkGUIConstants,
						    SandMarkPanel {
    private SandMarkFrame mFrame;

    public LiteHomePanel(SandMarkFrame frame) {
	mFrame = frame;

        javax.swing.JScrollPane insetPanel = new javax.swing.JScrollPane() {
		public java.awt.Dimension getPreferredSize() {
		    return this.getParent().getPreferredSize();
		}
	    };
        insetPanel.setBorder
	    (javax.swing.BorderFactory.createCompoundBorder
	     (javax.swing.BorderFactory.createRaisedBevelBorder(), 
	      javax.swing.BorderFactory.createLoweredBevelBorder()));
        insetPanel.setBackground(SAND_COLOR);

	java.lang.String startTags = 
            "<HTML>" +
	    "<BODY BGCOLOR = \"#E8D5BD\" TEXT = \"#7F7568\" LINK = \"#884400\" " +
	    "ALINK = \"#EE7700\" VLINK = \"#442200\">";
	java.lang.String endTags = "</BODY></HTML>";
        java.net.URL url = 
	    getClass().getClassLoader().getResource
	    (sandmark.gui.SandMarkGUIConstants.SMALL_LOGO_IMAGE);
	String logo = url.toString();	
	java.lang.String htmlText =
	    startTags + 
            "<CENTER><IMG SRC=\"" + logo + "\" ALT=\"SandMark\"><BR>" +
            "<FONT SIZE = 3>Christian Collberg " +
            "(<A HREF = \"mailto:collberg@cs.arizona.edu\">collberg@cs.arizona.edu</A>)" +
            "<BR><BR></FONT></CENTER>"+
            "<FONT SIZE = 4> SandMarkLite is a subset of the SandMark tool. " +
	    "SandMarkLite allows user to analyze java programs." +
            "<FONT SIZE = 3><ul>" +
            "   <li> <em>Diff</em> allows you to compare jar files. " +
            "   <li> <em>Statistics</em> computes Software Complexity Metrics. " +
            "   <li> <em>View</em> allows you to examine and search Java bytecode. " +
            "   <li> <em>Slice</em> allows you to slice local variable instructions in any method from a Jar file. " +
            "</ul>" +
            endTags;

	javax.swing.JEditorPane editorPane = new javax.swing.JEditorPane() {
		public boolean getScrollableTracksViewportWidth() {
		    return true;
		}
	    };
	editorPane.setEditable(false);
	editorPane.setContentType("text/html");
        //editorPane.setAlignmentY(java.awt.Component.LEFT_ALIGNMENT);
	editorPane.setText(htmlText);
        editorPane.setCaretPosition(0);
        editorPane.setBackground(SAND_COLOR);

	insetPanel.getViewport().setView(editorPane);

	setLayout(new java.awt.BorderLayout());
        add(insetPanel);
    }

    public java.awt.Dimension getPreferredSize() {
	if(getParent() == null)
	    return new java.awt.Dimension(0,0);

	javax.swing.JTabbedPane tabs = 
	    (javax.swing.JTabbedPane)getParent();
	int maxWidth = 0,maxHeight = 0;
	for(int i = 0 ; i < tabs.getTabCount() ; i++) {
	    if(tabs.getComponentAt(i) == this)
		continue;
	    java.awt.Dimension dim = tabs.getComponentAt(i).getPreferredSize();
	    if(dim.height > maxHeight)
		maxHeight = dim.height;
	    if(dim.width > maxWidth)
		maxWidth = dim.width;
	}
	return new java.awt.Dimension(maxWidth,maxHeight);
    }

    public String getDescription() {
	return "";
    }
    public SandMarkFrame getFrame() {
	return mFrame;
    }
}
