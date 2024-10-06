import javax.swing.JComponent;
import cern.colt.matrix.DoubleMatrix1D;

public class RepaintingListener implements BasicListener, GraphListener {
    private JComponent c;
    private long lastRepaint;

    public RepaintingListener(JComponent _c) {
	c = _c;
	// There will be a repaint when the component is first shown.
	lastRepaint = System.currentTimeMillis();
    }

    private void action() {
	long time = System.currentTimeMillis();
	if (time - lastRepaint > 1000) {
	    c.repaint(0, 0, c.getWidth(), c.getHeight());
	    lastRepaint = time;
	}
    }

    public void somethingChanged() {
	action();
    }

    public void graphChanged(Graph g, DoubleMatrix1D e,
			     double a, double b, double sum) {
	action();
    }
}

