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
public class ClassSorter implements java.awt.event.ActionListener {
   static class ReverseComparator implements java.util.Comparator {
      private java.util.Comparator mComp;
      ReverseComparator(java.util.Comparator comp) { mComp = comp; }
      public int compare(Object o1,Object o2) { return -mComp.compare(o1,o2); }
   }
   private AppTree mTree;
   private javax.swing.JComboBox mSelector;
   public ClassSorter(AppTree tree,javax.swing.JComboBox metricSelector) {
      mTree = tree;
      mSelector = metricSelector;
      metricSelector.addActionListener(this);
   }
   public void actionPerformed(java.awt.event.ActionEvent e) {
      Object item = mSelector.getSelectedItem();
      java.util.Comparator comp;
      if(item instanceof String)
         comp = new ReverseComparator
         	(new sandmark.metric.ClassOpcodeComparator((String)item));
      else
         comp = new ReverseComparator
         	(new sandmark.metric.ClassComparator
             ((sandmark.metric.ClassMetric)item));
      mTree.sortClasses(comp);
   }
}
