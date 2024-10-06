package sandmark.gui;

/**
 * A ConfigPropertyPanel lays out the properties of a ConfigProperties object
 * using widgets appropriate for the types of the properties.  A ConfigPropertyPanel
 * can be constructed with a phaseMask that will be used to determine whether a property
 * should be displayed.  If phaseMask & getPhases(property) != 0, property will be
 * included in the ConfigPropertyPanel.  By default, phaseMask == ~(0L), so all properties
 * are displayed.  A ConfigPropertyPanel observes changes to properties it displays,
 * so changes to properties are automatically reflected in the widget contents.  A
 * ConfigPropertyPanel reflects the contents of its widgets into the ConfigProperties
 * object only when updateProperties or updateProperty is called.
 * @author Andrew Huntwork <ash@cs.arizona.edu>
 */

public class ConfigPropertyPanel extends javax.swing.JScrollPane
    implements SandMarkGUIConstants {

    private static boolean DEBUG = false;
    private static final int MAX_VISIBLE_ROWS = 8;
    private java.util.HashSet mProperties;
    
    private int mRowNum;
    private javax.swing.JPanel mPanel;
    private java.awt.GridBagLayout mPanelLayout;
    private CurrentApplicationTracker mTracker;
    
    public ConfigPropertyPanel(sandmark.util.ConfigProperties configProps[],
                               long phaseMask,CurrentApplicationTracker tracker) {
        super(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
              javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mProperties = new java.util.HashSet();
        mTracker = tracker;

        setBackground(SAND_COLOR);
        setForeground(DARK_SAND_COLOR);
        
        mPanel = new javax.swing.JPanel() {
            public java.awt.Dimension getPreferredSize() {
                java.awt.Dimension panelPreferredSize = super.getPreferredSize();
                java.awt.Dimension viewSize = 
                    ConfigPropertyPanel.this.getViewport().getExtentSize();
                java.awt.Dimension rv = new java.awt.Dimension();
                rv.setSize
                (Math.min(panelPreferredSize.getWidth(),viewSize.getWidth()),
                        panelPreferredSize.getHeight());
                return rv;
            }
        };
        mPanelLayout = new java.awt.GridBagLayout();
        mPanel.setLayout(mPanelLayout);
        mPanel.setBackground(SAND_COLOR);
        mPanel.setForeground(DARK_SAND_COLOR);
        getViewport().setView(mPanel);
        
        for(int i = 0 ; i < configProps.length ; i++) {
            if(configProps[i] == null)
                continue;
            for(java.util.Iterator propIt = configProps[i].properties();
            propIt.hasNext() ; ) {
                
                if(mRowNum == MAX_VISIBLE_ROWS)
                    setPreferredSize(mPanel.getPreferredSize());
                
                String propName = (String)propIt.next();
                
                if((configProps[i].getPhases(propName) & phaseMask) == 0)
                    continue;
                
                PropertyInfo info = PIFactory.createPI(configProps[i],
                                                       propName,this);
                mProperties.add(info);
                mRowNum++;
            }
        }

        java.awt.Component box = javax.swing.Box.createGlue();
        java.awt.GridBagConstraints gbc = 
            new java.awt.GridBagConstraints();
        gbc.gridy = mRowNum;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        mPanelLayout.setConstraints(box,gbc);
        mPanel.add(box);
    }
    
    javax.swing.JPanel getPanel() { return mPanel; }
    java.awt.GridBagLayout getPanelLayout() { return mPanelLayout; }
    int getRowNum() { return mRowNum; }
    CurrentApplicationTracker getTracker() { return mTracker; }
    
    private void setUpdating(boolean updating) {
        for(java.util.Iterator pis = mProperties.iterator() ; 
            pis.hasNext() ; ) {
            PropertyInfo info = (PropertyInfo)pis.next();
            info.updating = updating;
        }
    }

    /**
       Atomically update all properties
    */
    public void updateProperties() {
        if(DEBUG)
            System.out.println("updating properties");
        setUpdating(true);
        for(java.util.Iterator pis = mProperties.iterator() ;
            pis.hasNext() ; ) {
            PropertyInfo pi = (PropertyInfo)pis.next();
            pi.updateProperty();
        }
        setUpdating(false);
    }
    
    protected String getToolTip(sandmark.util.ConfigProperties cp,
                                String propName) {
        String desc = cp.getDescription(propName);
        return propName + ": " + desc;
    }
}

class FileBrowseButtonEventListener implements java.awt.event.ActionListener {
    FilenameBox mBox;
    protected static java.io.File mCWD = new java.io.File(".");
    FileBrowseButtonEventListener(FilenameBox box) {
	mBox = box;
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
        javax.swing.JFileChooser fileChooser = 
            new javax.swing.JFileChooser(mCWD);
        fileChooser.setFileFilter(getFilter());
        int result = fileChooser.showOpenDialog(mBox);
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File chosen = 
                new java.io.File
                (fileChooser.getSelectedFile().getAbsolutePath());
            if(chosen.isDirectory())
                mCWD = chosen;
            else
                mCWD = chosen.getParentFile();
            mBox.setText(chosen.toString());
        }
    }
    ExtensionFileFilter getFilter() {
	return null;
    }
}
    
class JarBrowseButtonEventListener extends FileBrowseButtonEventListener {
    JarBrowseButtonEventListener(FilenameBox box) {
	super(box);
    }
    ExtensionFileFilter getFilter() {
	return new ExtensionFileFilter("jar", "Jar Files (*.jar)");
    }
}

abstract class PropertyInfo implements sandmark.util.ConfigPropertyChangeListener,
                                       SandMarkGUIConstants {
    boolean updating = false;
    protected Object mOrigValue;
    protected sandmark.util.ConfigProperties mProps;
    protected String mPropName;
    protected ConfigPropertyPanel mCPP;
    PropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                 ConfigPropertyPanel cpp) {
        cp.addPropertyChangeListener(propName,this);
        mOrigValue = cp.getValue(propName);     
        mProps = cp;
        mPropName = propName;
        mCPP = cpp;
    }
    void updateProperty() {
        mProps.setValue(mPropName,getValue());
    }
    protected static String getLabelText(String propName) {
        String nicePropName = propName;

       if(nicePropName.length() > 25)
            nicePropName = nicePropName.substring(0,22) + "...";

        return nicePropName;
    }
    abstract Object getValue();
}

abstract class FieldAndButtonPropertyInfo extends PropertyInfo {
    FieldAndButtonPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                               ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
        
        java.awt.GridBagConstraints fnbConstraints =
            new java.awt.GridBagConstraints();
        fnbConstraints.gridy = mCPP.getRowNum();
        fnbConstraints.insets = new java.awt.Insets(5,5,5,5);
        
        javax.swing.JComponent textBox = getTextBox(mOrigValue);
        
        javax.swing.JLabel label = new javax.swing.JLabel(getLabelText(mPropName));
        label.setLabelFor(textBox);
        label.setOpaque(false);
        label.setForeground(DARK_SAND_COLOR);
        label.setToolTipText(mCPP.getToolTip(mProps,mPropName));
        fnbConstraints.gridx = 0;
        fnbConstraints.anchor = java.awt.GridBagConstraints.EAST;
        mCPP.getPanelLayout().setConstraints(label,fnbConstraints);
        mCPP.getPanel().add(label);
        
        //fileNameBox.setBackground(SAND_COLOR);
        fnbConstraints.gridx = 1;
        fnbConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        fnbConstraints.weightx = 1;
        fnbConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mCPP.getPanelLayout().setConstraints(textBox,fnbConstraints);
        mCPP.getPanel().add(textBox);
        
        javax.swing.JButton button = new javax.swing.JButton(getButtonLabel());
        button.setOpaque(false);
        button.setForeground(DARK_SAND_COLOR);
        fnbConstraints.gridx = 2;
        fnbConstraints.anchor = java.awt.GridBagConstraints.WEST;
        fnbConstraints.weightx = 0;
        fnbConstraints.fill = java.awt.GridBagConstraints.NONE;
        mCPP.getPanelLayout().setConstraints(button,fnbConstraints);
        mCPP.getPanel().add(button);
        
        button.addActionListener(getListener());
    }
    protected abstract javax.swing.JComponent getTextBox(Object initValue);
    protected abstract String getButtonLabel();
    protected abstract java.awt.event.ActionListener getListener();
}

class FilePropertyInfo extends FieldAndButtonPropertyInfo {
    protected FilenameBox mFNB;
    FilePropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                     ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
    }
    protected javax.swing.JComponent getTextBox(Object initValue) {
        mFNB = new FilenameBox(getFileCategory());
        mFNB.setText(initValue == null ? "" : initValue.toString());
        return mFNB;
    }
    protected String getButtonLabel() { return "Browse"; }
    protected java.awt.event.ActionListener getListener() {
        return new FileBrowseButtonEventListener(mFNB);
    }
    protected String getFileCategory() { return "file"; }
    public Object getValue() { 
        mFNB.use();
        return mFNB.getText().equals("") ? null :new java.io.File(mFNB.getText());
    }
    public void propertyChanged
        (sandmark.util.ConfigProperties cp,String propName,
         Object oldValue,Object newValue) {
        Object localValue = getValue();
        boolean changed = (localValue == null ^ mOrigValue == null) ||
            (localValue != null && !localValue.equals(mOrigValue));
        if(updating && changed) {
            //System.out.println("vetoing change of " + propName + ": localValue: " + 
            //                    localValue + " ; mOrigValue: " + mOrigValue);
            return;
        }
        
        mOrigValue = newValue;
        
        mFNB.setText(newValue == null ? "" : newValue.toString());
    }
}

class JarPropertyInfo extends FilePropertyInfo {
    JarPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                    ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
    }
    protected java.awt.event.ActionListener getListener() {
        return new JarBrowseButtonEventListener(mFNB);
    }
    protected String getFileCategory() { return "jar"; }
}

class BooleanPropertyInfo extends PropertyInfo {
    private javax.swing.JCheckBox mCheckBox;
    BooleanPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                        ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
        
        java.awt.GridBagConstraints fnbConstraints =
            new java.awt.GridBagConstraints();
        fnbConstraints.gridy = mCPP.getRowNum();
        fnbConstraints.insets = new java.awt.Insets(5,5,5,5);

        mCheckBox =
            new javax.swing.JCheckBox
                (getLabelText(mPropName),
                mOrigValue == null ? false : 
                ((Boolean)mOrigValue).booleanValue());
        mCheckBox.setOpaque(false);
        mCheckBox.setForeground(DARK_SAND_COLOR);
        mCheckBox.setToolTipText(mCPP.getToolTip(mProps,mPropName));
        fnbConstraints.gridx = 1;
        fnbConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        fnbConstraints.weightx = 1;
        mCPP.getPanelLayout().setConstraints(mCheckBox,fnbConstraints);
        mCPP.getPanel().add(mCheckBox);
    }
    public Object getValue() { return new Boolean(mCheckBox.isSelected()); }
    public void propertyChanged
        (sandmark.util.ConfigProperties cp,String propName,
         Object oldValue,Object newValue) {
        Object localValue = getValue();
        boolean changed = (localValue == null ^ mOrigValue == null) ||
        (localValue != null && !localValue.equals(mOrigValue));
        if(updating && changed)
            return;
        
        mOrigValue = newValue;
        
        mCheckBox.setSelected(((Boolean)newValue).booleanValue());
    }
}

class StringPropertyInfo extends PropertyInfo {
   private abstract class Widget {
      abstract void setValue(Object o);
      abstract Object getValue();
      abstract javax.swing.JComponent getWidget();
   }
   private class TextField extends Widget {
      private javax.swing.JTextField mField;
      TextField(javax.swing.JTextField field) { mField = field; }
      void setValue(Object o) { 
         mField.setText(o == null ? "" : o.toString()); 
      }
      Object getValue() { return mField.getText(); }
      javax.swing.JComponent getWidget() { return mField; }
   }
   private class ComboBox extends Widget {
      private javax.swing.JComboBox mBox;
      private boolean mEditable;
      ComboBox(javax.swing.JComboBox box,boolean editable) { 
         mBox = box;
         mEditable = editable;
      }
      void setValue(Object o) {
         mBox.setSelectedItem(o);
         if(!o.equals(mBox.getSelectedItem())) {
            if(!mEditable)
               throw new Error("trying to set property to invalid value");
            mBox.addItem(o);
            mBox.setSelectedItem(o);
         }            
      }
      Object getValue() { return mBox.getSelectedItem(); }
      javax.swing.JComponent getWidget() { return mBox; }
   }
    private Widget mWidget;
    StringPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                       ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
        
        java.awt.GridBagConstraints fnbConstraints =
            new java.awt.GridBagConstraints();
        fnbConstraints.gridy = mCPP.getRowNum();
        fnbConstraints.insets = new java.awt.Insets(5,5,5,5);
        
        java.util.List choices = mProps.getChoices(mPropName);
        if(choices == null) {
           mWidget = new TextField(new javax.swing.JTextField());
        } else {
           Object choiceObjs[] = choices.toArray(new Object[0]);
           javax.swing.JComboBox box = new javax.swing.JComboBox(choiceObjs);
           box.setSelectedIndex(0);
           box.setBackground(java.awt.Color.WHITE);
           boolean exclusive = mProps.getExclusive(mPropName);
           box.setEditable(!exclusive);
           mWidget = new ComboBox(box,exclusive);
        }
        mWidget.setValue(mOrigValue);
        
        javax.swing.JLabel label = new javax.swing.JLabel(getLabelText(mPropName));
        label.setLabelFor(mWidget.getWidget());
        label.setOpaque(false);
        label.setForeground(DARK_SAND_COLOR);
        label.setToolTipText(mCPP.getToolTip(mProps,mPropName));
        fnbConstraints.gridx = 0;
        fnbConstraints.anchor = java.awt.GridBagConstraints.EAST;
        mCPP.getPanelLayout().setConstraints(label,fnbConstraints);
        mCPP.getPanel().add(label);
        
        fnbConstraints.gridx = 1;
        fnbConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        fnbConstraints.weightx = 1;
        fnbConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mCPP.getPanelLayout().setConstraints
        	(mWidget.getWidget(),fnbConstraints);
        mCPP.getPanel().add(mWidget.getWidget());
    }
    public Object getValue() { return mWidget.getValue(); }
    public void propertyChanged
        (sandmark.util.ConfigProperties cp,String propName,
         Object oldValue,Object newValue) {
        Object localValue = getValue();
        boolean changed = (localValue == null ^ mOrigValue == null) ||
        (localValue != null && !localValue.equals(mOrigValue));
        if(updating && changed)
            return;
        
        mOrigValue = newValue;
        mWidget.setValue(newValue);
    }
}

class IntegerPropertyInfo extends StringPropertyInfo {
    IntegerPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                        ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
    }
    public Object getValue() {
        String value = (String)super.getValue();
        try { return new Integer(value); }
        catch(NumberFormatException e) { return null; }
    }
}

class DoublePropertyInfo extends StringPropertyInfo {
    DoublePropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                       ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
    }
    public Object getValue() {
        String value = (String)super.getValue();
        try { return new Double(value); }
        catch(NumberFormatException e) { return null; }
    }
}

abstract class ProgramObjectPropertyInfo extends FieldAndButtonPropertyInfo {
    javax.swing.JTextField mTextField;
    javax.swing.JTree mObjectTree;
    javax.swing.JFrame mFrame;
    ConfigPropertyPanel mContainer;
    ProgramObjectPropertyInfo
        (sandmark.util.ConfigProperties cp,String propName,
         ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
        
        mContainer = cpp;
    }
    public void propertyChanged(sandmark.util.ConfigProperties cp,String propName,
                                Object oldValue,Object newValue) {
        if(!updating)
            throw new java.util.ConcurrentModificationException
            ("one user of this prop only");
    }
    public java.awt.event.ActionListener getListener() {
        return new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                mContainer.updateProperties();
                sandmark.program.Application app = 
                    mCPP.getTracker().getCurrentApplication();
                if(app == null) {
                    sandmark.util.Log.message(0,"Please select a jar");
                    return;
                }
                
                mFrame = new javax.swing.JFrame();
                mObjectTree = new AppTree
                    (app,getObjectVisibilityMask(),
                     javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
                javax.swing.JScrollPane treePane = 
                    new javax.swing.JScrollPane(mObjectTree);
                javax.swing.JButton ok = new javax.swing.JButton("OK");
                ok.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        javax.swing.tree.TreePath objectNodes[] =
                            mObjectTree.getSelectionPaths();
                        java.util.ArrayList objects = new java.util.ArrayList();
                        for(int i = 0 ; objectNodes != null && i < objectNodes.length ; i++) {
                            Object node = objectNodes[i].getLastPathComponent();
                            objects.add(node);
                        }
                        setSelectedObjects(objects);
                        mFrame.hide();
                    }
                });
                javax.swing.JButton cancel = new javax.swing.JButton("Cancel");
                cancel.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        mFrame.hide();
                    }
                });
                mFrame.getContentPane().add(treePane,java.awt.BorderLayout.NORTH);
                mFrame.getContentPane().add(ok,java.awt.BorderLayout.WEST);
                mFrame.getContentPane().add(cancel,java.awt.BorderLayout.EAST);
                mFrame.pack();
                mFrame.show();
            }
        };
    }
    protected abstract void setSelectedObjects(java.util.List selectedObjects);
    protected abstract int getObjectVisibilityMask();
}

class ClassPropertyInfo extends ProgramObjectPropertyInfo {
    sandmark.program.Class mSelectedClasses[];
    ClassPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                      ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
    }
    protected int getObjectVisibilityMask() { 
       return AppTree.SHOW_APPS|AppTree.SHOW_CLASSES; 
    }
    public Object getValue() { return mSelectedClasses; }
    public javax.swing.JComponent getTextBox(Object initValue) {
        mTextField = new javax.swing.JTextField();
        sandmark.program.Class classes[] = (sandmark.program.Class [])initValue;
        mSelectedClasses = classes;
        setText();
        return mTextField;
    }
    public String getButtonLabel() { return "Select Classes"; }
    protected void setSelectedObjects(java.util.List selectedObjects) {
        mSelectedClasses = (sandmark.program.Class [])
            selectedObjects.toArray(new sandmark.program.Class[0]);
        setText();
    }
    protected void setText() {
        String text = "";
        for(int i = 0 ; mSelectedClasses!= null && 
            i < mSelectedClasses.length ; i++)
            text += (i == 0 ? "" : ", ") + mSelectedClasses[i].getName();
        if(text.equals(""))
            text = "[All Classes]";
        mTextField.setText(text );
    }
}

class MethodPropertyInfo extends ProgramObjectPropertyInfo {
    sandmark.program.Method mSelectedMethods[];
    MethodPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                      ConfigPropertyPanel cpp) {
        super(cp,propName,cpp);
    }
    public Object getValue() { return mSelectedMethods; }
    public void propertyChanged(sandmark.util.ConfigProperties cp,String propName,
                                Object oldValue,Object newValue) {
        
    }
    public javax.swing.JComponent getTextBox(Object initValue) {
        mTextField = new javax.swing.JTextField();
        mSelectedMethods = (sandmark.program.Method [])initValue;
        setText();
        return mTextField;
    }
    public String getButtonLabel() { return "Select Methods"; }
    protected void setSelectedObjects(java.util.List selectedObjects) {
        mSelectedMethods = (sandmark.program.Method [])
            selectedObjects.toArray(new sandmark.program.Method[0]);
        setText();
    }
    protected int getObjectVisibilityMask() { 
       return AppTree.SHOW_APPS|AppTree.SHOW_CLASSES|AppTree.SHOW_METHODS;
    }
    protected void setText() {
        String text = "";
        for(int i = 0 ; mSelectedMethods != null && 
            i < mSelectedMethods.length ; i++)
            text += (i == 0 ? "" : ", ") + mSelectedMethods[i].getName() +
                mSelectedMethods[i].getSignature();
        if(text.equals(""))
            text = "[All Methods]";
        mTextField.setText(text );
    }
}

class PIFactory {
    private PIFactory() {}
    static PropertyInfo createPI(sandmark.util.ConfigProperties cp,String propName,
                                 ConfigPropertyPanel cpp) {
        int type = cp.getType(propName);
        switch(type) {
        case sandmark.util.ConfigProperties.TYPE_FILE:
            return new FilePropertyInfo(cp,propName,cpp);
        case sandmark.util.ConfigProperties.TYPE_JAR:
            return new JarPropertyInfo(cp,propName,cpp);
        case sandmark.util.ConfigProperties.TYPE_INTEGER:
            return new IntegerPropertyInfo(cp,propName,cpp);
        case sandmark.util.ConfigProperties.TYPE_DOUBLE:
            return new DoublePropertyInfo(cp,propName,cpp);
        case sandmark.util.ConfigProperties.TYPE_STRING:
            return new StringPropertyInfo(cp,propName,cpp);
        case sandmark.util.ConfigProperties.TYPE_BOOLEAN:
            return new BooleanPropertyInfo(cp,propName,cpp);
        case sandmark.util.ConfigProperties.TYPE_CLASS:
            return new ClassPropertyInfo(cp,propName,cpp);
        case sandmark.util.ConfigProperties.TYPE_METHOD:
            return new MethodPropertyInfo(cp,propName,cpp);
        default:
            throw new RuntimeException("unsupported property type");
        }
    }
}