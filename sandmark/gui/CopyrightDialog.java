package sandmark.gui;

public class CopyrightDialog extends javax.swing.JFrame
    implements java.awt.event.ActionListener, javax.swing.event.HyperlinkListener, 
    sandmark.gui.SandMarkGUIConstants
{
    public CopyrightDialog(javax.swing.JFrame parent){
	super();
	setTitle(SandMarkGUIConstants.TITLE + " Copyright");
	setSize(windowSize);
	setResizable(true);
	setDefaultCloseOperation(HIDE_ON_CLOSE);

	editorPane = new javax.swing.JEditorPane();
	editorPane.setEditable(false);
	editorPane.addHyperlinkListener(this);
	java.net.URL url = getClass().getClassLoader().getResource(COPYRIGHT_PAGE);
	if(url == null)
	    url = getClass().getClassLoader().getResource("sandmark/html/error.html");
	try{
	    editorPane.setPage(url);
	}catch(java.io.IOException e){
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + e );
    }

	javax.swing.JScrollPane sp = new javax.swing.JScrollPane(editorPane);
	sp.setPreferredSize(windowSize);
	
	java.awt.Container contentPane = getContentPane();
	contentPane.setLayout(new java.awt.BorderLayout());
	contentPane.add(sp, java.awt.BorderLayout.CENTER);
	pack();
	java.awt.Point parentLoc = parent.getLocation();
	java.awt.Dimension parentDim = parent.getSize();
	setLocation(parentLoc.x + (parentDim.width-getSize().width)/2, 
		    parentLoc.y + (parentDim.height-getSize().height)/2);
    }

    public void actionPerformed(java.awt.event.ActionEvent e){
    }

    public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e){
	try{gotoLink(e);}
	catch(java.lang.Exception ex){
        sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + ex );
    }
    }

    private void gotoLink(javax.swing.event.HyperlinkEvent e) 
	throws java.io.IOException{
	java.net.URL clicked = e.getURL();
	if(e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED){
	    try{
		javax.swing.JFrame browser = new WebBrowser(clicked.toString(), this);
		browser.show();
	    }catch(java.lang.Exception ex){
		throw new java.io.IOException("Page requested not found!");
	    }
	}
    }

    private final java.awt.Dimension windowSize = new java.awt.Dimension(700, 400);
    private javax.swing.JEditorPane editorPane = null;
    private final java.lang.String COPYRIGHT_PAGE = "sandmark/html/copyright.html";
}

