package sandmark.gui;

class SMAlgHierTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private String mName;
    private String mHelpURL;
    private ConfigPropertyPanel mPropPanel;
    public SMAlgHierTreeNode(String name,String helpURL) {
	mName = name;
	mHelpURL = helpURL;
    }
    public String toString() {
	return mName;
    }
    public String helpURL() {
	return mHelpURL;
    }
}

