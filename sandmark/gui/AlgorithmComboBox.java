package sandmark.gui;

public class AlgorithmComboBox extends javax.swing.JComboBox 
	implements SandMarkGUIConstants,javax.swing.event.PopupMenuListener {
   public interface DescriptionListener {
      void showTransientDescription(String description);
      void showDescription();
      void algorithmChanged(sandmark.Algorithm alg);
   }
   
   private java.util.ArrayList listeners;
   private sandmark.Algorithm lastSelection;
   private DescriptionListener mListener; 
   public AlgorithmComboBox(DescriptionListener listener,int algType) {
      mListener = listener;
      setRenderer(new MyComboBoxRenderer());
      
      listeners = new java.util.ArrayList();
      
      String algClassNames[] = 
         (String[])
         sandmark.util.classloading.ClassFinder.getClassesWithAncestor
         (algType).toArray(new String[0]);
      java.util.Arrays.sort(algClassNames,new java.util.Comparator() {
         public int compare(Object o1,Object o2) {
            return sandmark.util.classloading.ClassFinder.
               getClassShortname((String)o1).compareTo
               (sandmark.util.classloading.ClassFinder.
               getClassShortname((String)o2));
         }
      });
      java.awt.FontMetrics fm = getFontMetrics(getFont());
      int maxWidth = fm.stringWidth("   ");
      for(int i = 0; i < algClassNames.length; i++) {
         try {
            sandmark.Algorithm alg = 
               (sandmark.Algorithm)Class.forName
               (algClassNames[i]).newInstance();
            addItem(alg);
            int thisWidth = fm.stringWidth(alg.toString());
            if(thisWidth > maxWidth)
               maxWidth = thisWidth;
         } catch(ClassNotFoundException e) {
            //I guess it really isn't a class, so don't let the user select it
         } catch(InstantiationException e) {
            //I guess it really isn't a class, so don't let the user select it
         } catch(IllegalAccessException e) {
            //I guess it really isn't a class, so don't let the user select it
         }
      }
      maxWidth += 50;
      
      setForeground(DARK_SAND_COLOR);
      setBackground(SAND_COLOR);
      setSize(maxWidth, ALGORITHM_LABEL_Y + 5);
      addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
            sandmark.Algorithm alg = (sandmark.Algorithm)getSelectedItem();
            if(alg == lastSelection)
               return;
            lastSelection = alg;
            for(java.util.Iterator listenerIt = listeners.iterator() ;
            	 listenerIt.hasNext() ; ) {
               AlgorithmPanel ap = (AlgorithmPanel)listenerIt.next();
               ap.setAlgorithm(alg);
            }
            mListener.algorithmChanged(alg);
         }
      });
      this.addPopupMenuListener(this);
   }
   public sandmark.Algorithm getCurrentAlgorithm() {
      return (sandmark.Algorithm)getSelectedItem();
   }
   public void addListener(AlgorithmPanel panel) {
      listeners.add(panel);
   }
   public void removeListener(AlgorithmPanel panel) {
      listeners.remove(panel);
   }
   public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
   public void popupMenuWillBecomeVisible
   	(javax.swing.event.PopupMenuEvent e) {}
   public void popupMenuWillBecomeInvisible
   	(javax.swing.event.PopupMenuEvent e) {
      mListener.showDescription();
   }

   private class MyComboBoxRenderer extends javax.swing.plaf.basic.BasicComboBoxRenderer {
      public java.awt.Component getListCellRendererComponent
      (javax.swing.JList list,Object value,int index,boolean isSelected, 
       boolean cellHasFocus) {
         java.awt.Component c = super.getListCellRendererComponent
         (list,value,index,isSelected,cellHasFocus);
         if(index == list.getSelectedIndex())
            mListener.showTransientDescription
            (((sandmark.Algorithm)value).getDescription());
         return c;
      }  
   }
}
