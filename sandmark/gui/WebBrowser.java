package sandmark.gui;

public class WebBrowser extends javax.swing.JFrame 
    implements java.awt.event.ActionListener, javax.swing.event.HyperlinkListener{

    public static java.net.URL DEFAULT_HOME_URL   = null;
    public static java.net.URL DEFAULT_SEARCH_URL = null;
    /* Make the URLs */
    static{
	try{
	    DEFAULT_HOME_URL   = new java.net.URL("http://www.cs.arizona.edu/");
	    DEFAULT_SEARCH_URL = new java.net.URL("http://www.yahoo.com/");
	}catch(java.net.MalformedURLException mfue){
	    System.err.println("Unable to make URLs: " + mfue);
	    System.exit(1);
	}
    }
	
    public WebBrowser(){
	this(DEFAULT_HOME_URL.toString());
    }
	
    public WebBrowser(java.lang.String home){
	this(home, DEFAULT_SEARCH_URL.toString(), null);
    }

    public WebBrowser(java.lang.String home, 
		      javax.swing.JFrame parent){
	this(home, DEFAULT_SEARCH_URL.toString(), parent);
    }
    
    public WebBrowser(java.lang.String home, 
		      java.lang.String search,
		      javax.swing.JFrame parent){
	super();
	setTitle("JWeb Browser");
	m_sHome   = home;
	m_sSearch = search;
	m_sParent = parent;
	
	/* Construct GUI components */
	setJMenuBar(createBar());
	java.awt.Container buttons = 
	    new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	m_jmBack    = addButton("Back",    buttons);
	m_jmForward = addButton("Forward", buttons);
	addButton("Refresh",  buttons);
	addButton("Home",    buttons);
	addButton("Search",  buttons);
	
	m_jcbLocation.setEditable(true);
	m_jep.setEditable(false);
	m_jep.addHyperlinkListener(this);
	
	/* event listeners */
	m_jcbLocation.setActionCommand("Location");
	m_jcbLocation.addActionListener(this);
	
	/* go to home page */
	addPage(home.toString());
	
	enableStuff();
	
	java.awt.Container north = new javax.swing.Box(javax.swing.BoxLayout.Y_AXIS);
	north.add(buttons);
	north.add(m_jcbLocation);
	
	javax.swing.JPanel south = new javax.swing.JPanel(new java.awt.BorderLayout());
	south.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	south.add(m_jlStatus);
	
	java.awt.Container contentPane = getContentPane();
	contentPane.add(north, java.awt.BorderLayout.NORTH);
	contentPane.add(new javax.swing.JScrollPane(m_jep), 
			java.awt.BorderLayout.CENTER);
	contentPane.add(south, java.awt.BorderLayout.SOUTH);
	
	setSize(new java.awt.Dimension(800, 600));
    }
    
    /* Processes button clicks and text field events in this frame. */
    public void actionPerformed(java.awt.event.ActionEvent event){
	java.lang.String command = event.getActionCommand().intern();
	if(command == "Home"){
	    trim(m_jcbLocation.getSelectedIndex());
	    addPage(m_sHome);
	}else if(command == "Search"){
	    trim(m_jcbLocation.getSelectedIndex());
	    addPage(m_sSearch);
	}else if (command == "Location"){
	    // trim(m_jcbLocation.getSelectedIndex());
	    goToPage((java.lang.String)m_jcbLocation.getSelectedItem());
	}else if (command == "Back"){
	    if(canGoBack()){
		m_jcbLocation.setSelectedIndex(m_jcbLocation.getSelectedIndex() + 1);
	    }
	}else if(command == "Forward"){
	    if(canGoForward()){
		m_jcbLocation.setSelectedIndex(m_jcbLocation.getSelectedIndex() - 1);
	    }
	}else if(command == "Refresh"){
	}else if(command == "About..."){
	    javax.swing.JOptionPane.showMessageDialog(this,
						      "JWeb Browser\n\n" +
						      "by Martin Stepp (stepp)",
						      "About JWeb Browser",
						      javax.swing.JOptionPane.INFORMATION_MESSAGE);
	}else if(command == "Exit"){
	    hide();
	}
	
	enableStuff();
    }
    
    /* Processes hyperlink clicks in this frame. */
    public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent event){
	javax.swing.event.HyperlinkEvent.EventType type = event.getEventType();
	if(type == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED){
	    if(event instanceof javax.swing.text.html.HTMLFrameHyperlinkEvent){
		/* an event in a page with frames; must be handled differently */
		javax.swing.text.html.HTMLFrameHyperlinkEvent frameEvent = 
		    (javax.swing.text.html.HTMLFrameHyperlinkEvent)event;
		javax.swing.text.html.HTMLDocument doc = 
		    (javax.swing.text.html.HTMLDocument)m_jep.getDocument();
		doc.processHTMLFrameHyperlinkEvent(frameEvent);
	    }else /* any other web page event */
		trim(m_jcbLocation.getSelectedIndex());
	    addPage(event.getURL().toString());
	}else if(type == javax.swing.event.HyperlinkEvent.EventType.ENTERED){
	    m_jlStatus.setText(event.getURL().toString());
	}else if(type == javax.swing.event.HyperlinkEvent.EventType.EXITED){
	    m_jlStatus.setText(" ");
	}
    }
    
    private javax.swing.JButton addButton(java.lang.String text, 
					  java.awt.Container c){
	javax.swing.ImageIcon i1 = 
	    new javax.swing.ImageIcon("images/" + text.toLowerCase() + "_bw.gif");
	javax.swing.ImageIcon i2 = 
	    new javax.swing.ImageIcon("images/" + text.toLowerCase() + ".gif");
	javax.swing.JButton jb = new JHoverButton(text, i1, i2);
	jb.addActionListener(this);
	c.add(jb);
	return jb;
    }
    
    private void addPage(java.lang.String text){
	// m_jcbLocation.removeActionListener(this);
	m_jcbLocation.insertItemAt(text, 0);
	m_jcbLocation.setSelectedItem(text);
	// m_jcbLocation.addActionListener(this);
    }
    
    private void trim(int ii){
	if (0 <= ii && ii < m_jcbLocation.getItemCount()){
	    m_jcbLocation.removeActionListener(this);
	    while(ii-- > 0){
		m_jcbLocation.removeItemAt(0);
	    }
	    m_jcbLocation.addActionListener(this);
	}
    }
    
    private boolean canGoBack() {
	int index = m_jcbLocation.getSelectedIndex();
	return 0 <= index && index < m_jcbLocation.getItemCount() - 1;
    }
    
    private boolean canGoForward() {
	int index = m_jcbLocation.getSelectedIndex();
	return 0 < index && index < m_jcbLocation.getItemCount();
    }
    
    private void enableStuff() {
	m_jmBack.setEnabled(canGoBack());
	m_jmForward.setEnabled(canGoForward());
    }
    
    /* (Try to) go to the page with the given textually represented URL */
    private boolean goToPage(java.lang.String text) {
	if(text != null){
	    java.net.URL page = null;
	    try{
		page = new java.net.URL(text);
	    }catch(java.net.MalformedURLException mfue){
		javax.swing.JOptionPane.showMessageDialog(this, 
							  "Invalid URL: " + text + "\n\n" + mfue, 
							  "Error", 
							  javax.swing.JOptionPane.ERROR_MESSAGE);
		return false;
	    }
	    try{
		m_jep.setPage(page);
		return true;
	    }catch(java.io.IOException ioe){
		javax.swing.JOptionPane.showMessageDialog(this, 
							  "I/O error while loading page: \n\n" + ioe,
							  "Error", 
							  javax.swing.JOptionPane.ERROR_MESSAGE);
	    }catch(java.lang.Exception e){
		javax.swing.JOptionPane.showMessageDialog(this, 
							  "Could not go to page: \n\n" + e, 
							  "Error", 
							  javax.swing.JOptionPane.ERROR_MESSAGE);
	    }
	}else{
	    // new RuntimeException().printStackTrace();
	    // System.out.println("goToPage(): null text");
	}
	return false;
    }
    
    /* Creates the menus */
    private javax.swing.JMenuBar createBar(){
	javax.swing.JMenu jmFile = new javax.swing.JMenu("File");  
	jmFile.setMnemonic('F');
	addItem(jmFile, "Exit", null, 'X', 
		javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 
						   java.awt.Event.ALT_MASK));
	javax.swing.JMenu jmHelp = new javax.swing.JMenu("Help");  
	jmHelp.setMnemonic('H');
	addItem(jmHelp, "About...", "help", 'A', 
		javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
	
	javax.swing.JMenuBar bar = new javax.swing.JMenuBar();
	bar.add(jmFile);
	bar.add(jmHelp);
	return bar;
    }
    
    /* Adds a new JMenuItem.  Convenience method. */
    private javax.swing.JMenuItem addItem(javax.swing.JMenu menu, 
					  java.lang.String text){
	return addItem(menu, text, -1);
    }
    
    /* Adds a new JMenuItem.  Convenience method. */
    private javax.swing.JMenuItem addItem(javax.swing.JMenu menu, 
					  java.lang.String text, 
					  int m){
	return addItem(menu, text, null, m);
    }
    
    /* Adds a new JMenuItem.  Convenience method. */
    private javax.swing.JMenuItem addItem(javax.swing.JMenu menu, 
					  java.lang.String text, 
					  java.lang.String icon, 
					  int m){
	return addItem(menu, text, icon, m, null);
    }
    
    /* Adds a new JMenuItem.  Convenience method. */
    private javax.swing.JMenuItem addItem(javax.swing.JMenu menu, 
					  java.lang.String text, 
					  java.lang.String icon, 
					  int m, 
					  javax.swing.KeyStroke accel){
	return setupItem(new javax.swing.JMenuItem(text), 
			 menu, icon, m, accel);
    }
    
    
    /* Adds a new JMenuItem.  Convenience method. */
    private javax.swing.JMenuItem addCheckItem(javax.swing.JMenu menu, 
					       java.lang.String text, 
					       java.lang.String icon, 
					       int m, boolean sel){
	return addCheckItem(menu, text, icon, m, sel, null);
    }
    
    /* Adds a new JMenuItem.  Convenience method. */
    private javax.swing.JMenuItem addCheckItem(javax.swing.JMenu menu, 
					       java.lang.String text, 
					       java.lang.String icon, 
					       int m, boolean sel, 
					       javax.swing.KeyStroke accel){
	return setupItem(new javax.swing.JCheckBoxMenuItem(text, sel), 
			 menu, icon, m, accel);
    }
    
    private javax.swing.JMenuItem setupItem(javax.swing.JMenuItem item, 
					    javax.swing.JMenu menu, 
					    java.lang.String icon, int m, 
					    javax.swing.KeyStroke accel){
	if(icon != null)
	    item.setIcon(new javax.swing.ImageIcon("images/" + icon + ".gif"));
	if(m >= 0)
	    item.setMnemonic(m);
	if(accel != null)
	    item.setAccelerator(accel);
	item.addActionListener(this);
	menu.add(item);
	return item;
    }

    private javax.swing.JFrame m_sParent;
    private javax.swing.JEditorPane m_jep = new javax.swing.JEditorPane();
    private javax.swing.JComboBox m_jcbLocation = new javax.swing.JComboBox();
    private javax.swing.JLabel m_jlStatus = new javax.swing.JLabel(" ");
    private javax.swing.JButton m_jmBack, m_jmForward;
    private java.lang.String m_sHome, m_sSearch;
}

class JHoverButton extends javax.swing.JButton 
    implements java.awt.event.MouseListener{

    public JHoverButton(){
	super();
	init();
    }
    
    public JHoverButton(javax.swing.Action a){
	super(a);
	init();
    }
    
    public JHoverButton(javax.swing.Icon icon){
	super(icon);
	init();
    }
    
    public JHoverButton(java.lang.String text){
	super(text);
	init();
    }
    
    public JHoverButton(java.lang.String text, 
			javax.swing.Icon icon){
	super(text, icon);
	init();
    }
    
    public JHoverButton(javax.swing.Icon offIcon, 
			javax.swing.Icon onIcon){
	super(offIcon);
	myOffIcon = offIcon;
	myOnIcon  = onIcon;
	init();
    }
    
    public JHoverButton(java.lang.String text, 
			javax.swing.Icon offIcon, 
			javax.swing.Icon onIcon){
	super(text, offIcon);
	myOffIcon = offIcon;
	myOnIcon  = onIcon;
	init();
    }
    
    public JHoverButton(javax.swing.Icon icon, 
			java.lang.String actionCommand){
	super(icon);
	setActionCommand(actionCommand);
	init();
    }
    
    public JHoverButton(javax.swing.Icon offIcon, 
			javax.swing.Icon onIcon, 
			java.lang.String actionCommand){
	super(offIcon);
	myOffIcon = offIcon;
	myOnIcon  = onIcon;
	setActionCommand(actionCommand);
	init();
    }
    
    public void setEnabled(boolean b){
	/* make sure hover doesn't take effect 
	   stay on for a disabled button
	*/
	if(!b){
	    setBorderPainted(false);
	    if(myOffIcon != null)
		setIcon(myOffIcon);
	}
	super.setEnabled(b);
    }
    
    public void mouseClicked(java.awt.event.MouseEvent e){}
    public void mousePressed(java.awt.event.MouseEvent e) {}
    public void mouseReleased(java.awt.event.MouseEvent e){}
    public void mouseEntered(java.awt.event.MouseEvent e){
	if(isEnabled()){
	    /* show border ("hover" effect) when mouse is over */
	    setBorderPainted(true);
	    if(myOnIcon != null)
		setIcon(myOnIcon);
	}
    }
    public void mouseExited(java.awt.event.MouseEvent e){
	if(isEnabled()){
	    /* turn off "hover" effect when mouse leaves */
	    setBorderPainted(false);
	    if(myOffIcon != null)
		setIcon(myOffIcon);
	}
    }
    
    private void init() {
	setBorderPainted(false);
	setFocusPainted(false);
	addMouseListener(this);
    }

    private javax.swing.Icon myOffIcon = null;
    private javax.swing.Icon myOnIcon  = null;
}

