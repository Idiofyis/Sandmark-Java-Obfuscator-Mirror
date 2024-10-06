package sandmark.gui;

public class AboutDialog extends javax.swing.JFrame 
    implements java.awt.event.ActionListener, javax.swing.event.HyperlinkListener, 
    sandmark.gui.SandMarkGUIConstants{
    
    public AboutDialog(javax.swing.JFrame parent){
	super();
	setTitle("About " + SandMarkGUIConstants.TITLE);
	setResizable(true);
	setSize(windowSize);
	setDefaultCloseOperation(HIDE_ON_CLOSE);
	setBackground(SAND_COLOR);

	editorPane = new javax.swing.JEditorPane();
	editorPane.setEditable(false);
	editorPane.addHyperlinkListener(this);
	editorPane.setContentType("text/html");
	java.lang.String htmlText = buildHTMLText();
	editorPane.setText(htmlText);
        editorPane.setCaretPosition(0);
	javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(editorPane);
	scrollPane.setPreferredSize(windowSize);
	
	javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
	buttonPanel.setBackground(SAND_COLOR);
	okButton = new javax.swing.JButton("OK");
	okButton.addActionListener(this);
	okButton.setForeground(DARK_SAND_COLOR);
	okButton.setBackground(SAND_COLOR);
	buttonPanel.add(okButton);
	
	java.awt.Container contentPane = getContentPane();
	contentPane.setBackground(SAND_COLOR);
	contentPane.setLayout(new java.awt.BorderLayout());
	contentPane.add(scrollPane, java.awt.BorderLayout.CENTER);
	contentPane.add(buttonPanel, java.awt.BorderLayout.SOUTH);

	pack();
	java.awt.Point parentLoc = parent.getLocation();
	java.awt.Dimension parentDim = parent.getSize();
	setLocation(parentLoc.x + (parentDim.width - getSize().width) / 2, 
		    parentLoc.y + (parentDim.height - getSize().height) / 2);
    }
    
    
    public void actionPerformed(java.awt.event.ActionEvent e){
	java.lang.Object source = e.getSource();
	if (source == okButton)
	    hide();
    }

    public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e){
	try{gotoLink(e);}
	catch(java.lang.Exception ex){
        //sandmark.util.Log.message( sandmark.util.Log.INTERNAL_EXCEPTION, "Exception caught and ignored:" + ex );
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

    private java.lang.String buildHTMLText(){
	java.lang.String startTags = "<HTML><HEAD><TITLE>" + "About " + SandMarkGUIConstants.TITLE+ "</TITLE></HEAD>" +
	    "<BODY BGCOLOR = \"#E8D5BD\" TEXT = \"#7F7568\" LINK = \"#884400\" " +
	    "ALINK = \"#EE7700\" VLINK = \"#442200\">";
	java.lang.String endTags = "</BODY></HTML>";
	java.lang.String line = "<BR><HR WIDTH = \"100%\" SIZE = 3>";
	java.lang.String result =
	    startTags + 
	    cutTags(getAboutHTML()) + line + 
	     "<CENTER><B>List of Watermarking Algorithms</B></CENTER>" + line; 
       String[] wmAlgNames = (String[])
          sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	       (sandmark.util.classloading.IClassFinder.GEN_WATERMARKER).toArray
	       (new String[0]);
      for(int i=0; i < wmAlgNames.length; i++){
         try{
            sandmark.Algorithm alg = 
		         (sandmark.Algorithm)Class.forName(wmAlgNames[i]).newInstance();
		      result += cutTags(alg.getAlgHTML()) + line;
	      } catch(Exception e) {}
      }
      
      result += "<CENTER><B>List of Obfuscation Algorithms</B></CENTER>" + line;
      String[] obfAlgNames = (String[])
         sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	       (sandmark.util.classloading.IClassFinder.GEN_OBFUSCATOR).toArray
	       (new String[0]);
      for(int i=0; i < obfAlgNames.length; i++){
         try{
            sandmark.Algorithm alg = 
		         (sandmark.Algorithm)Class.forName(obfAlgNames[i]).newInstance();
		      result += cutTags(alg.getAlgHTML()) + line;
	      } catch(Exception e) {}
      }

      result += "<CENTER><B>List of Optimization Algorithms</B></CENTER>" + line;
      String[] optAlgNames = (String[])
         sandmark.util.classloading.ClassFinder.getClassesWithAncestor
	       (sandmark.util.classloading.IClassFinder.GEN_OPTIMIZER).toArray
	       (new String[0]);
      for(int i=0; i < optAlgNames.length; i++){
         try{
            sandmark.Algorithm alg = 
		         (sandmark.Algorithm)Class.forName(optAlgNames[i]).newInstance();
		      result += cutTags(alg.getAlgHTML()) + line;
	      } catch(Exception e) {}
      }

	return result;
    }

    private java.lang.String cutTags(java.lang.String text){
	if(text == null)
	    return "";
	text = text.trim();
	int start = text.indexOf("<BODY");
	start = text.indexOf(">", start) + 1;
	int end = text.indexOf("</BODY>");
	if(start >= text.length() || start == -1 || end == -1){
	    java.lang.System.err.println("Syntax error in HTML codes!");
	    return "";
	}
	//System.out.println("Successful");
	return text.substring(start, end);
    }

    /**
     *  Get the HTML codes of the about page for SandMark
   @return HTML code for the about page
     */
    public java.lang.String getAboutHTML(){
        String logo = getClass().getClassLoader().getResource
	    (SandMarkGUIConstants.LOGO_IMAGE).toString();
	return
	    "<HTML><BODY><CENTER><IMG SRC=\"" + logo + "\" ALT = \"SandMark\"><BR>" +
	    "<FONT SIZE = 5>" + "About " + SandMarkGUIConstants.TITLE + "</FONT><BR><BR>" +
	    "<FONT SIZE = 4> A tool to watermark, obfuscate, and tamper-proof Java class files.<BR><BR>" +
	    "Principal designers:</FONT><BR>" +
	    "<FONT SIZE = 3>Christian Collberg " +
	    "(<A HREF = \"mailto:collberg@cs.arizona.edu\">collberg@cs.arizona.edu</A>)<BR>" +
	    "Gregg Townsend " +
	    "(<A HREF = \"mailto:gmt@cs.arizona.edu\">gmt@cs.arizona.edu</A>)<BR>" +
	    "Kelly Heffner " +
	    "(<A HREF = \"mailto:kheffner@cs.arizona.edu\">kheffner@cs.arizona.edu</A>)<BR>" +
	    "Andrew Huntwork " +
	    "(<A HREF = \"mailto:ash@cs.arizona.edu\">ash@cs.arizona.edu</A>)<BR>" +
	    "Jasvir Nagra " +
	    "(<A HREF = \"mailto:jas@cs.auckland.ac.nz\">jas@cs.auckland.ac.nz</A>)<BR>" +
	    "Ginger Myles " +
	    "(<A HREF = \"mailto:mylesg@cs.arizona.edu\">mylesg@cs.arizona.edu</A>)<BR><BR></FONT>" +
	    "<FONT SIZE = 4> Contact: <A HREF = \"mailto:sandmark-users@listserv.arizona.edu\">sandmark-users@listserv.arizona.edu</A></FONT><BR><BR>" +
	    "<FONT SIZE = 4> Based on:</FONT>" +
	    "</CENTER>" +
	    "<ul>" +
	    "   <li> Christian Collberg, Clark Thomborson," +
	    "        <a href=\"http://www.cs.auckland.ac.nz/~cthombor/Pubs/01027797a.pdf\">" +
	    "           Watermarking, Tamper-Proofing, and Obfuscation - Tools for Software Protectionhristian Collberg, Clark Thomborson," +
	    "        </a>," +
	    "        IEEE Transactions on Software Engineering 28:8, 735-746, August 2002" +
	    "   <li> Christian Collberg, Clark Thomborson," +
	    "      <a href=\"http://www.cs.arizona.edu/~collberg/Research/Publications/CollbergThomborson99a/index.html\"> " + 
	    "           Software Watermarking - Models and Dynamic Embeddings" +
	    "        </a>," +
	    "        ACM POPL'99." +
	    "   <li> Christian Collberg, Clark Thomborson, Douglas Low," +
	    "       <a href=\"http://www.cs.arizona.edu/~collberg/Research/Publications/CollbergThomborsonLow98a/index.html\">" +
	    "          Manufacturing Cheap, Resilient, and Stealthy Opaque Constructs " +
	    "       </a>," +
	    "       ACM POPL'98." +
	    "   <li> Christian Collberg, Clark Thomborson, Douglas Low," +
	    "        <a href=\"http://www.cs.arizona.edu/~collberg/Research/Publications/CollbergThomborsonLow97d\">" +
	    "            Breaking Abstractions and Unstructuring Data Structures " +
	    "        </a>," +
	    "        IEEE ICCL'98." +
	    "</ul>" +
       "<CENTER>" +
       "<FONT SIZE = 4> Additional papers:</FONT>" +
	    "</CENTER>" +
	    "<ul>" +
       "   <li>Ginger Myles and Christian Collberg, " +
       "       Software Watermarking via Opaque Predicates: Implementation," +
       "       Analysis, and Attacks," +
       "       In ICECR-7, July 10-13, 2004." +
       "   <li>C. Collberg, E. Carter, S. Debray, A. Huntwork, C. Linn, M. Stepp," +
       "       Dynamic Path-Based Software Watermarking," +
       "       PLDI 2004." +
       "   <li>Ginger Myles, " +
       "       <a href=\"http://www.acm.org/crossroads/xrds10-3/watermarking.html\">" +
       "       Using Software Watermarking to Discourage Piracy," +
       "       ACM Crossroads, Spring 2004." +
       "   <li>Ginger Myles and Christian Collberg, " +
       "       Software Watermarking Through Register Allocation: Implementation," +
       "       Analysis, and Attacks. In 6th International Conference on" +
       "       Information Security and Cryptology, 2003"+
       "   <li>Christian Collberg, Ginger Myles, Andrew Huntwork, " +
       "       Sandmark -- A Tool for Software Protection Research, " +
       "       IEEE, Security and Privacy, Vol. 1, Num. 4, July/August 2003." +
	    "</BODY></HTML>";
    }

    private final java.awt.Dimension windowSize = new java.awt.Dimension(400, 400);
    private final java.awt.Dimension paneSize = new java.awt.Dimension(400, 200);
    private javax.swing.JEditorPane editorPane = null;
    private javax.swing.JButton okButton;
}

