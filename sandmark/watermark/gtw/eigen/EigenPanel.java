import javax.swing.*;
import java.awt.*;
import java.util.*;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;

public class EigenPanel extends JPanel {
    private GraphData model;

    public EigenPanel(GraphData _model) {
	model = _model;
	model.addListener(new RepaintingListener(this));
    }

    private static final int BORDER = 20;

    private Point getCoords(double x, double y, double maxx) {
	int newx = (int)((getWidth()-BORDER*2) * x / maxx) + BORDER;
	int newy = (int)((getHeight()-BORDER*2) * 0.5 * (2 - y)) + BORDER;
	return new Point(newx, newy);
    }

    private void drawTransformedLine(Graphics g,
				     double x1, double y1, 
				     double x2, double y2,
				     double maxx) {
	Point p1 = getCoords(x1, y1, maxx);
	Point p2 = getCoords(x2, y2, maxx);
	g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	g = g.create();

	EigenData data = model.getEigenData();
	DoubleMatrix1D lambda = data.e;
	double a = data.a;
	double b = data.b;

	double maxx = lambda.size() - 1;

	g.setColor(Color.BLACK);

	drawTransformedLine(g, 0, 0, maxx, 0, maxx);
	drawTransformedLine(g, 0, 0, 0, 2, maxx);
	for (int i = 1; i < lambda.size(); i++) {
	    Point p = getCoords(i, 0, maxx);
	    g.drawLine(p.x, p.y-3, p.x, p.y+3);
	}
	for (int i = 1; i <= 20; i++) {
	    Point p = getCoords(0, i * 0.1, maxx);
	    g.drawLine(p.x-3, p.y, p.x+3, p.y);
	}
	for (int i = 1; i <= 4; i++) {
	    Point p1 = getCoords(0, i * 0.5, maxx);
	    Point p2 = getCoords(maxx, i * 0.5, maxx);
	    g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	g.setColor(Color.BLUE);

	for (int i = 0; i < lambda.size(); i++) {
	    Point p = getCoords(i, lambda.get(i), maxx);
	    g.drawRect(p.x - 1, p.y - 1, 3, 3);
	}

	g.setColor(Color.RED);

	drawTransformedLine(g, 0, b, maxx, a * maxx + b, maxx);

	Point p = getCoords(lambda.size()/2, 0, maxx);
	g.drawString("diff. = " + data.sum, p.x, p.y - 30);
    }
}

