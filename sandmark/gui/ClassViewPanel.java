/*
 * Created on Mar 4, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.gui;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ClassViewPanel extends javax.swing.JTabbedPane 
   implements SandMarkGUIConstants,ViewPanel {
   public ClassViewPanel(sandmark.program.Class clazz) {
      setBackground(SAND_COLOR);
      
      Object[][] cpRowData, fieldRowData;

      org.apache.bcel.generic.ConstantPoolGen cpg =
          clazz.getConstantPool();

      Object[] cpColumnNames = { "Index", "Constant" };
      cpRowData = new Object[cpg.getSize()][2];

      for(int c = 0 ; c < cpg.getSize() ; c++) {
          org.apache.bcel.classfile.Constant C = cpg.getConstant(c);
          if (C != null) {
              cpRowData[c][0] = c + "";
              cpRowData[c][1] = C.toString();
          }
      }

      Object[] fieldColumnNames = { "Name", "Signature" };
      sandmark.program.Field fields[] = clazz.getFields();
      fieldRowData = new Object[fields.length][2];

      for(int i = 0 ; i < fields.length ; i++) {
          fieldRowData[i][0] = fields[i].getName();
          fieldRowData[i][1] = fields[i].getSignature();
      }

      javax.swing.JTable cpTable = new javax.swing.JTable(cpRowData, cpColumnNames);
      javax.swing.JScrollPane cpScroll = new javax.swing.JScrollPane(cpTable);
      cpScroll.setBackground(SAND_COLOR);
      add("Constant Pool", cpScroll);
      javax.swing.JTable fieldTable = new javax.swing.JTable(fieldRowData, fieldColumnNames);
      javax.swing.JScrollPane fieldScroll =
         new javax.swing.JScrollPane(fieldTable);
      fieldScroll.setBackground(SAND_COLOR);
      add("Fields", fieldScroll);
      javax.swing.JTable statTable = 
         new javax.swing.JTable(new StatTableModel(clazz));
      javax.swing.JScrollPane statScroll = 
         new javax.swing.JScrollPane(statTable);
      statScroll.setBackground(SAND_COLOR);
      add("Statistics",statScroll);
      javax.swing.JTable metricTable =
         new javax.swing.JTable(new MetricTableModel(clazz));
      javax.swing.JScrollPane metricScroll =
         new javax.swing.JScrollPane(metricTable);
      metricScroll.setBackground(SAND_COLOR);
      add("Metrics",metricScroll);
      setBackground(SAND_COLOR);
   }
   public void tearDown() {}
   public Object saveViewState() {
      return new Object[] { getClass(),new Integer(getSelectedIndex()) };
   }
   public void restoreViewState(Object v) {
      Object viewState[] = (Object [])v;
      Class c = (Class)viewState[0];
      if(c != getClass())
         return;
      
      Integer i = (Integer)viewState[1];
      setSelectedIndex(i.intValue());
   }
}
