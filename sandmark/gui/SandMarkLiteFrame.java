package sandmark.gui;

public class SandMarkLiteFrame extends  SandMarkFrame
    implements sandmark.gui.SandMarkGUIConstants {

    public SandMarkLiteFrame() {
    	setTitle("SandMarkLite V1.0");
    }
    protected void addTabs(javax.swing.JTabbedPane tabPane) {
    	tabPane.add("Home", new LiteHomePanel(this));
    	tabPane.add("Diff", new sandmark.gui.diff.DiffPanel(this));
    	tabPane.add("View", VSplitPanel.getSandMarkViewPanel(this));
    }
    public static void main(String argv[]) throws Exception {
    	start(SandMarkLiteFrame.class);
    }
}
