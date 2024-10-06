package sandmark.gui;

public class HelpButton extends javax.swing.JButton 
    implements java.awt.event.ActionListener,SandMarkGUIConstants {
    String mHelpKey;
    public HelpButton(String helpKey) {
    	this(helpKey,"Help");
    }
    public HelpButton(String helpKey,String buttonTitle) {
	super(buttonTitle);

        setBackground(SAND_COLOR);
        setForeground(DARK_SAND_COLOR);

	setHelpKey(helpKey);
	addActionListener(this);
    }
    public void setHelpKey(String helpKey) {
	mHelpKey = helpKey;
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
	HelpDialog hd = new HelpDialog();
	hd.showHelpFor(mHelpKey);
	hd.show();
    }
}
