/*
 * Created on Mar 5, 2004
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
public class MethodSorter implements java.awt.event.ActionListener {
   private AppTree mTree;
   private javax.swing.JComboBox mSelector;
   public MethodSorter(AppTree tree,javax.swing.JComboBox metricSelector) {
      mTree = tree;
      mSelector = metricSelector;
      metricSelector.addActionListener(this);
   }
   public void actionPerformed(java.awt.event.ActionEvent e) {
      Object item = mSelector.getSelectedItem();
      java.util.Comparator comp;
      if(item instanceof String)
         comp = new ClassSorter.ReverseComparator
         	(new sandmark.metric.MethodOpcodeComparator((String)item));
      else
         comp = new ClassSorter.ReverseComparator
         	(new sandmark.metric.MethodComparator
             ((sandmark.metric.MethodMetric)item));
      mTree.sortMethods(comp);
   }

}
