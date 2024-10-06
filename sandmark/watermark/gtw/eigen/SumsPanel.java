import javax.swing.*;
import java.awt.*;

public class SumsPanel extends JPanel {
    private SumData model;

    public SumsPanel(SumData _model) {
	model = _model;
	model.addListener(new RepaintingListener(this));
    }

    private static final int BORDER = 20;

    private int getCoord(double x) {
	return (int)(((x+1)/2) * (getWidth()-2*BORDER)) + BORDER;
    }

    private void drawSet(Graphics g, Color c, int set, int y) {
	g.setColor(Color.BLACK);
	g.drawLine(getCoord(-1), y, getCoord(1), y);
	for (int i = -10; i <= 10; i++) {
	    int x = getCoord(i * 0.1);
	    g.drawLine(x, y+7, x, y-7);
	}
	g.drawLine(getCoord(0), y+20, getCoord(0), y-20);
	g.setColor(c);
	int elements = model.setSize(set);
	for (int i = 0; i < elements; i++) {
	    double s = model.setElement(set, i);
	    int x = getCoord(s);
	    g.drawLine(x-2, y, x+2, y);
	    g.drawLine(x, y-2, x, y+2);
	}
    }

    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	g = g.create();

	if (model.numSets() >= 1) {
	    drawSet(g, Color.BLUE, 0, getHeight() / 3);
	    if (model.numSets() >= 2) {
		drawSet(g, Color.GREEN, 1, getHeight() * 2 / 3);
	    }
	}
    }
}

