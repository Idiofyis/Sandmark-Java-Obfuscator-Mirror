import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GraphEigenvalues extends JFrame {
    GraphEigenvalues(int vertices) {
	setSize(640, 480);
	setTitle("k-partite graph eigenvalues");
	setDefaultCloseOperation(EXIT_ON_CLOSE);

	JTabbedPane tabs = new JTabbedPane();
	getContentPane().add(tabs, BorderLayout.CENTER);

	SumData sums = new SumData();

	GraphData model = new RandomGraphData(vertices, 0.4, 
					      sums.getListener());
	tabs.addTab("Random graph", new GraphPanel(model));
	tabs.addTab("Random graph eigenvalues", new EigenPanel(model));

	model = new KPartiteGraphData(3, vertices, 0.6,
				      sums.getListener());
	tabs.addTab("Tripartite graph", new GraphPanel(model));
	tabs.addTab("Tripartite graph eigenvalues", new EigenPanel(model));

	tabs.addTab("Differences", new SumsPanel(sums));
    }

    public static void main(String [] argv) {
	r = sandmark.util.Random.getRandom();

	GraphEigenvalues f = new GraphEigenvalues(50);
	f.show();
    }

    public static Random r;
}

