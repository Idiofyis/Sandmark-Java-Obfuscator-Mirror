package sandmark.util.graph.graphview;

public class GraphZoomSlider extends javax.swing.JPanel
    implements sandmark.gui.SandMarkGUIConstants {
    public GraphZoomSlider(sandmark.util.graph.graphview.GraphPanel panel) {
        mySlider = new javax.swing.JSlider(javax.swing.JSlider.HORIZONTAL,0,300,100);
	myGraphDisplayPanel = panel;
        mySlider.setBackground(SAND_COLOR);
        mySlider.setMajorTickSpacing(100);
        mySlider.setMinorTickSpacing(25);
        //mySlider.setPaintTicks(true);
        //mySlider.setPaintLabels(true);
 
        mySlider.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    javax.swing.JSlider source = (javax.swing.JSlider)e.getSource();
                    if (!source.getValueIsAdjusting()) {
                        double factor = source.getValue() / 100.0;
                        myGraphDisplayPanel.resizeGraph(factor);
                    }
                }
            });
	//setPreferredSize(new java.awt.Dimension(200, 100));
	setLayout(new java.awt.BorderLayout());
	setBackground(SAND_COLOR);
	add(mySlider, java.awt.BorderLayout.CENTER);
	add(new javax.swing.JLabel("Graph zoom", javax.swing.JLabel.CENTER), java.awt.BorderLayout.NORTH);
    }

    public void setGraphDisplayPanel(sandmark.util.graph.graphview.GraphPanel panel) {
	myGraphDisplayPanel = panel;
	reset();
    }

    public void reset() {
	mySlider.setValue(100);
    }

    private sandmark.util.graph.graphview.GraphPanel myGraphDisplayPanel;
    private javax.swing.JSlider mySlider;
}
