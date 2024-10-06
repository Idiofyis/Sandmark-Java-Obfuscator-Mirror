package sandmark.gui;

public class GraphDisplayButton extends javax.swing.JButton 
    implements java.awt.event.ActionListener,SandMarkGUIConstants,
               java.util.Observer {

    public GraphDisplayButton(javax.swing.JFrame frame) {
	super("Graphs");
	
	myFrame = frame;

        setBackground(SAND_COLOR);
        setForeground(DARK_SAND_COLOR);

	addActionListener(this);
	sandmark.util.graph.graphview.GraphList.instance().addObserver(this);
    }

    public void update(java.util.Observable o, Object arg) {
	if (arg.equals("add"))
	    setVisible(true);
	else if (arg.equals("clear"))
	    setVisible(false);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
	new sandmark.util.graph.graphview.GraphViewFrame(myFrame, sandmark.util.graph.graphview.GraphList.instance()).show();
    }

    private javax.swing.JFrame myFrame;
}
