package sandmark.gui;

public class NextWMButton extends javax.swing.JButton {
   private java.util.Iterator mWMIter;
   private javax.swing.JComboBox mWMList;
   NextWMButton(javax.swing.JComboBox wmList) {
      super("Get More");
      mWMList = wmList;
      setEnabled(false);
      addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
            addNextItem();
         }
      });
   }
   void setIter(java.util.Iterator wmIter) {
      if(wmIter == null)
         wmIter = new java.util.LinkedList().iterator();
      mWMList.removeAllItems();
      mWMIter = wmIter;
      addNextItem();
   }
   private void addNextItem() {
      if(mWMIter.hasNext()) {
         mWMList.addItem(mWMIter.next());
         mWMList.setSelectedIndex(mWMList.getItemCount() - 1);
      }
      setEnabled(mWMIter.hasNext());
   }
}
