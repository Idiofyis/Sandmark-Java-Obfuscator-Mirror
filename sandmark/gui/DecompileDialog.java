// DecompileDialog.java

package sandmark.gui;

/**
   This dialog pops up when the user clicks on Decompile button
   in the Decompile pane.  It shows the source code of the decompiled
   jar file.

   @author Andrzej Pawlowski
 */
public class DecompileDialog extends javax.swing.JFrame 
    implements sandmark.gui.SandMarkGUIConstants {
    
    public DecompileDialog(javax.swing.JFrame parent, String text){
	super();
	setTitle("Decompilation");
	setResizable(true);
	setSize(WINDOW_SIZE);
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	setBackground(SAND_COLOR);

	// textArea displays the decompiled file
	textArea = new javax.swing.JTextArea(text);
	textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
	textArea.setEditable(false);
   //Christian does not want the background color behind the code to be sand
   //color.
	//textArea.setBackground(SAND_COLOR);
	javax.swing.JScrollPane scrollPane = 
	    new javax.swing.JScrollPane(textArea);
	scrollPane.setPreferredSize(WINDOW_SIZE);
	
	// buttonPanel containing the close button
	javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
	buttonPanel.setBackground(SAND_COLOR);
	closeButton = new javax.swing.JButton("Close");
	closeButton.addActionListener(
            new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent event) {
		    setVisible(false);
		    dispose();
		}
	    });
	closeButton.setForeground(DARK_SAND_COLOR);
	closeButton.setBackground(SAND_COLOR);
	buttonPanel.add(closeButton);
	
	java.awt.Container contentPane = getContentPane();
        //	contentPane.setBackground(SAND_COLOR);
	contentPane.setLayout(new java.awt.BorderLayout());
	contentPane.add(scrollPane, java.awt.BorderLayout.CENTER);
	contentPane.add(buttonPanel, java.awt.BorderLayout.SOUTH);

	pack();
	java.awt.Point parentLoc = parent.getLocation();
	java.awt.Dimension parentDim = parent.getSize();
	setLocation(parentLoc.x + (parentDim.width - getSize().width) / 2, 
		    parentLoc.y + (parentDim.height - getSize().height) / 2);
    }
    
    private static final java.awt.Dimension WINDOW_SIZE = new java.awt.Dimension(600, 400);

    private javax.swing.JTextArea textArea;
    private javax.swing.JButton closeButton;
}

