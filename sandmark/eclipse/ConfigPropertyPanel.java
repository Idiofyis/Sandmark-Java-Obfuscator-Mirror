/*
 * Created on Mar 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.eclipse;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;

 /**
  * @see sandmark.gui.ConfigPropertyPanel
  */

 public class ConfigPropertyPanel extends Composite implements ModifyListener {
 	interface ChangeListener {
 		void changed();
 	}

     private static boolean DEBUG = false;
     private static final int MAX_VISIBLE_ROWS = 8;
     private java.util.HashSet mProperties;
     
     private sandmark.gui.CurrentApplicationTracker mTracker;
     private java.util.ArrayList mChangeListeners = new java.util.ArrayList();
     
     public ConfigPropertyPanel
        (org.eclipse.swt.widgets.Composite parent,
         sandmark.util.ConfigProperties configProps[],
         long phaseMask,sandmark.gui.CurrentApplicationTracker tracker) {
         super(parent,org.eclipse.swt.SWT.BORDER);

         mProperties = new java.util.HashSet();
         mTracker = tracker;
         org.eclipse.swt.layout.GridLayout layout = 
         	new org.eclipse.swt.layout.GridLayout();
         layout.numColumns = 3;
         layout.verticalSpacing = 0;
         setLayout(layout);
         
         for(int i = 0 ; i < configProps.length ; i++) {
         	System.out.println("cp " + configProps[i]);
             if(configProps[i] == null)
                 continue;
             for(java.util.Iterator propIt = configProps[i].properties();
             propIt.hasNext() ; ) {
                 
                 String propName = (String)propIt.next();
                 System.out.print("building propName...");
                 if((configProps[i].getPhases(propName) & phaseMask) == 0)
                     continue;
                 System.out.println("correct phase");
                 PropertyInfo info = PIFactory.createPI(configProps[i],
                                                        propName,this);
                 mProperties.add(info);
             }
         }
         
         Composite filler = new Composite(this,SWT.NONE);
         GridData fillerData = new GridData();
         fillerData.grabExcessVerticalSpace = true;
         filler.setLayoutData(fillerData);
     }
     
     public void modifyText(ModifyEvent e) {
     	System.out.println("changed text");
     	for(java.util.Iterator listeners = mChangeListeners.iterator() ; 
     	    listeners.hasNext() ; ) {
     		ChangeListener listener = (ChangeListener)listeners.next();
     		listener.changed();
     	}
     }
     
     public void addChangeListener(ChangeListener l) { mChangeListeners.add(l); }
     public void removeChangeListener(ChangeListener l) { mChangeListeners.remove(l); }
     
     sandmark.gui.CurrentApplicationTracker getTracker() { return mTracker; }
     
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
     
     private static final int TEXTFIELD = 0;
     private static final int COMBOBOX = 1;
     
 }

 abstract class PropertyInfo 
    implements sandmark.util.ConfigPropertyChangeListener {
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
         mProps.setProperty(mPropName,(String)getValue());
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
         
         org.eclipse.swt.widgets.Label label = 
            new org.eclipse.swt.widgets.Label
            (mCPP,org.eclipse.swt.SWT.CENTER);
         label.setText(getLabelText(mPropName));
         label.setToolTipText(mCPP.getToolTip(mProps,mPropName));

         org.eclipse.swt.widgets.Text textBox = getTextBox(mOrigValue);
         
         org.eclipse.swt.widgets.Button button = 
            new org.eclipse.swt.widgets.Button
            (mCPP,org.eclipse.swt.SWT.PUSH);
         button.setText(getButtonLabel());
         
         button.addSelectionListener(getListener());
     }
     protected abstract org.eclipse.swt.widgets.Text getTextBox
        (Object initValue);
     protected abstract String getButtonLabel();
     protected abstract org.eclipse.swt.events.SelectionListener getListener();
 }

 class FilePropertyInfo extends FieldAndButtonPropertyInfo {
     protected org.eclipse.swt.widgets.Text mFNB;
     FilePropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                      ConfigPropertyPanel cpp) {
         super(cp,propName,cpp);
     }
     protected org.eclipse.swt.widgets.Text getTextBox(Object initValue) {
         mFNB = new org.eclipse.swt.widgets.Text(mCPP,0);
         mFNB.setText(initValue == null ? "" : initValue.toString());
         return mFNB;
     }
     protected String getButtonLabel() { return "Browse"; }
     protected org.eclipse.swt.events.SelectionListener getListener() {
         return null;
     }
     public Object getValue() { 
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
     protected org.eclipse.swt.events.SelectionListener getListener() {
         return null;
     }
     protected String getFileCategory() { return "jar"; }
 }

 class BooleanPropertyInfo extends PropertyInfo {
     private org.eclipse.swt.widgets.Button mCheckBox;
     BooleanPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                         ConfigPropertyPanel cpp) {
         super(cp,propName,cpp);

         mCheckBox = new org.eclipse.swt.widgets.Button
            (mCPP,org.eclipse.swt.SWT.TOGGLE);
         mCheckBox.setText(getLabelText(mPropName));
         mCheckBox.setSelection
            (mOrigValue == null ? false : 
             ((Boolean)mOrigValue).booleanValue());
         mCheckBox.setToolTipText(mCPP.getToolTip(mProps,mPropName));
     }
     public Object getValue() { return new Boolean(mCheckBox.getSelection()); }
     public void propertyChanged
         (sandmark.util.ConfigProperties cp,String propName,
          Object oldValue,Object newValue) {
         Object localValue = getValue();
         boolean changed = (localValue == null ^ mOrigValue == null) ||
         (localValue != null && !localValue.equals(mOrigValue));
         if(updating && changed)
             return;
         
         mOrigValue = newValue;
         
         mCheckBox.setSelection(((Boolean)newValue).booleanValue());
     }
 }

 class StringPropertyInfo extends PropertyInfo {
    private abstract class Widget {
       abstract void setValue(Object o);
       abstract Object getValue();
       abstract org.eclipse.swt.widgets.Control getWidget();
    }
    private class TextField extends Widget {
       private org.eclipse.swt.widgets.Text mField;
       TextField(ConfigPropertyPanel parent) { 
          mField = new org.eclipse.swt.widgets.Text(parent,SWT.NONE);
          GridData data = new GridData(GridData.FILL_HORIZONTAL);
          data.grabExcessHorizontalSpace = true;
          data.verticalAlignment = GridData.VERTICAL_ALIGN_FILL;
          mField.setLayoutData(data);
          mField.addModifyListener(parent);
       }
       void setValue(Object o) {
       	if(mField.isDisposed())
       		return;
          mField.setText(o == null ? "" : o.toString()); 
       }
       Object getValue() { return mField.isDisposed() ? null : mField.getText(); }
       org.eclipse.swt.widgets.Control getWidget() { return mField; }
    }
    private class ComboBox extends Widget {
       private org.eclipse.swt.widgets.Combo mBox;
       java.util.List mValues;
       private boolean mEditable;
       ComboBox(org.eclipse.swt.widgets.Composite parent,
                java.util.List values,boolean editable) {
          int style = editable ? SWT.READ_ONLY : SWT.NONE;
          mBox = new org.eclipse.swt.widgets.Combo(parent,style);
          GridData data = new GridData(GridData.FILL_HORIZONTAL);
          data.grabExcessHorizontalSpace = true;
          mBox.setLayoutData(data);
          for(java.util.Iterator it = values.iterator() ; it.hasNext() ; )
             mBox.add(it.next().toString());
          mEditable = editable;
       }
       void setValue(Object o) {
          int index = mValues.indexOf(o);
          if(index == -1) {
             if(!mEditable)
                throw new Error("setting field to illegal value");
             mValues.add(o);
             mBox.add(o.toString());
             index = mValues.size();
          }
          mBox.select(index);
       }
       Object getValue() { return mValues.get(mBox.getSelectionIndex()); }
       org.eclipse.swt.widgets.Control getWidget() { return mBox; }
    }
     private Widget mWidget;
     StringPropertyInfo(sandmark.util.ConfigProperties cp,String propName,
                        ConfigPropertyPanel cpp) {
         super(cp,propName,cpp);
         
         System.out.println("building string property");
         
         org.eclipse.swt.widgets.Label label = 
            new org.eclipse.swt.widgets.Label(cpp,SWT.NONE);
         GridData data = new GridData();
         label.setLayoutData(data);
         label.setText(getLabelText(mPropName));
         label.setToolTipText(mCPP.getToolTip(mProps,mPropName));
      
         java.util.List choices = mProps.getChoices(mPropName);
         if(choices == null) {
            mWidget = new TextField(cpp);
         } else {
            mWidget = new ComboBox(cpp,choices,cp.getExclusive(propName));
         }
         mWidget.setValue(mOrigValue);
         
         org.eclipse.swt.widgets.Label comp =
         	new org.eclipse.swt.widgets.Label(cpp,SWT.NONE);
         GridData data2 = new GridData();
         comp.setLayoutData(data2);
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

 class PIFactory {
     private PIFactory() {}
     static PropertyInfo createPI(sandmark.util.ConfigProperties cp,String propName,
                                  ConfigPropertyPanel cpp) {
         int type = cp.getType(propName);
         switch(type) {
         case sandmark.util.ConfigProperties.TYPE_FILE:
             return new StringPropertyInfo(cp,propName,cpp);
         case sandmark.util.ConfigProperties.TYPE_JAR:
             return new StringPropertyInfo(cp,propName,cpp);
         case sandmark.util.ConfigProperties.TYPE_INTEGER:
             return new StringPropertyInfo(cp,propName,cpp);
         case sandmark.util.ConfigProperties.TYPE_DOUBLE:
             return new StringPropertyInfo(cp,propName,cpp);
         case sandmark.util.ConfigProperties.TYPE_STRING:
             return new StringPropertyInfo(cp,propName,cpp);
         case sandmark.util.ConfigProperties.TYPE_BOOLEAN:
             return new StringPropertyInfo(cp,propName,cpp);
         default:
             throw new RuntimeException("unsupported property type");
         }
     }
 }
