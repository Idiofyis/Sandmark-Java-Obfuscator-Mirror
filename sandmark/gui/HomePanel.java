package sandmark.gui;

public class HomePanel extends SkinPanel implements SandMarkGUIConstants,
						    SandMarkPanel {
    private SandMarkFrame mFrame;

    public HomePanel(SandMarkFrame frame) {
	mFrame = frame;

        javax.swing.JScrollPane insetPanel = new javax.swing.JScrollPane() {
		public java.awt.Dimension getPreferredSize() {
		    return getParent().getPreferredSize();
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
            "<FONT SIZE = 4> SandMark is a tool to watermark, obfuscate, " +
            "and tamper-proof Java class files." +
            "<FONT SIZE = 3><ul>" +
            "   <li> <P><em> Dynamic Watermark</em> will embed a copyright notice or " +
            "        customer identification number into the runtime structures " +
            "        of a program. " +
            "   <li> <em>Static Watermark</em> embeds a mark into the Java bytecode itself. " +
            "   <li> <em>Obfuscate</em> rearranges code to make it harder to understand. " +
            "   <li> <em>Optimize</em> runs the <a href=\"http://www.cs.purdue.edu/s3/projects/bloat\"> BLOAT </a> optimizer, a dynamic inliner, or a static inliner. " +
            "   <li> <em>Diff</em> compares the bytecodes of two jar-files for similarity." +
            "   <li> <em>View</em> allows you to examine and search Java bytecode. " +
            "   <li> <em>Decompile</em> allows you to decompile the classes in a Jar file. " +
            "   <li> <em>Quick Protect</em> will help you Obfuscate and" +
            "           Watermark your program automatically." +
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
	return "Welcome to SandMark!";
    }
    public SandMarkFrame getFrame() {
	return mFrame;
    }
}
