import javax.swing.*;
import java.awt.*;
import cern.colt.matrix.DoubleMatrix1D;

public class GraphPanel extends JPanel {
    private GraphData model;

    public GraphPanel(GraphData _model) {
	model = _model;
	model.addListener(new RepaintingListener(this));
    }

    protected void paintComponent(Graphics gr) {
	super.paintComponent(gr);
	gr = gr.create();
	
	Graph g = model.getGraph();

	// compute angle per vertex
	double theta = 2 * Math.PI / g.numVertices();

	// compute radius
	double radius = (Math.min(getHeight(), getWidth()) - 30) / 2;
	
	// set color to black
	gr.setColor(Color.BLACK);

	// compute center
	int x0 = getWidth() / 2;
	int y0 = getHeight() / 2;

	// draw vertices
	for (int i = 0; i < g.numVertices(); i++) {
	    int x = (int)(x0 + radius * Math.cos(theta * i));
	    int y = (int)(y0 + radius * Math.sin(theta * i));
	    gr.fillOval(x - 3, y - 3, 6, 6);
	}

	// draw edges
	for (int i = 0; i < g.numVertices() - 1; i++)
	    for (int j = i + 1; j < g.numVertices(); j++)
		if (g.containsEdge(i, j)) {
		    int x1 = (int)(x0 + radius * Math.cos(theta * i));
		    int y1 = (int)(y0 + radius * Math.sin(theta * i));
		    int x2 = (int)(x0 + radius * Math.cos(theta * j));
		    int y2 = (int)(y0 + radius * Math.sin(theta * j));
		    gr.drawLine(x1, y1, x2, y2);
		}
    }
}

