package sandmark.gui;

/*
 author: Armand Navabi
 date: June 16, 2003
 MethodSliceGUI.java : This class provides a graphical user interface for
 slicing tools found in sandmark.analysis.slicingtools.
 */

public class MethodSliceGUI extends javax.swing.JPanel 
implements java.awt.event.ActionListener,SandMarkGUIConstants
{
   private javax.swing.JButton forwardSlice, backwardSlice;
   private sandmark.program.Method mMethod;
   private javax.swing.JRadioButton staticSlicer, dynamicSlicer;
   private SliceRenderer myCellRenderer;
   private javax.swing.JList mInstructionList;
   
   public MethodSliceGUI(sandmark.program.Method method) {
      mMethod = method;
      initGUI();
   }
   
   private void displaySlice(java.util.ArrayList slice) {
      myCellRenderer.setSliceInstrs(slice);
      mInstructionList.repaint();
   }
   
   public void actionPerformed(java.awt.event.ActionEvent e) {
      Object source = e.getSource();
      org.apache.bcel.generic.InstructionHandle ih =
         (org.apache.bcel.generic.InstructionHandle)
         mInstructionList.getSelectedValue();
      if(ih == null || 
         !(ih.getInstruction() instanceof 
           org.apache.bcel.generic.LocalVariableInstruction)) {
         sandmark.util.Log.message
         	(0,"Please select a Local Variable Instruction");
         return;
      }
      if(source == forwardSlice) {
         sandmark.analysis.slicingtools.ForwardMethodSlice fms =
            new sandmark.analysis.slicingtools.ForwardMethodSlice
            (mMethod, ih, staticSlicer.isSelected());
         java.util.ArrayList slice = fms.getSlice();
         displaySlice(slice);
         sandmark.util.Log.message(0,"ForwardSlice on instruction: " + ih);
      } else if(source == backwardSlice) {
         sandmark.analysis.slicingtools.BackwardMethodSlice bms =
            new sandmark.analysis.slicingtools.BackwardMethodSlice
            (mMethod, ih, staticSlicer.isSelected());
         java.util.ArrayList slice = bms.getSlice();
         displaySlice(slice);
         sandmark.util.Log.message(0,"BackwardSlice on instruction: " + ih);
      }
   }
   
   private void initGUI(){
      myCellRenderer = new SliceRenderer();
      org.apache.bcel.generic.InstructionList il =
         mMethod.getInstructionList();
      mInstructionList =
         new javax.swing.JList
         (il == null ? new Object[0] : il.getInstructionHandles());
      mInstructionList.setCellRenderer(myCellRenderer);
      javax.swing.JScrollPane instrPane = 
         new javax.swing.JScrollPane(mInstructionList);
      instrPane.setBackground(SAND_COLOR);
      
      forwardSlice = new javax.swing.JButton("Forward Slice");
      forwardSlice.setBackground(SandMarkFrame.SAND_COLOR);
      forwardSlice.addActionListener(this);
      
      backwardSlice = new javax.swing.JButton("Backward Slice");
      backwardSlice.setBackground(SandMarkFrame.SAND_COLOR);
      backwardSlice.addActionListener(this);
      
      staticSlicer = new javax.swing.JRadioButton("Static Slicer");
      staticSlicer.setBackground(SandMarkFrame.SAND_COLOR);
      staticSlicer.setSelected(true);
      dynamicSlicer = new javax.swing.JRadioButton("Dynamic Slicer");
      dynamicSlicer.setBackground(SandMarkFrame.SAND_COLOR);
      javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
      group.add(staticSlicer);
      group.add(dynamicSlicer);
      
      javax.swing.JPanel radioButtonPanel = new javax.swing.JPanel();
      radioButtonPanel.setBackground(SandMarkFrame.SAND_COLOR);
      radioButtonPanel.add(staticSlicer);
      radioButtonPanel.add(dynamicSlicer);
      
      javax.swing.JPanel actionButtonPanel = new javax.swing.JPanel();
      actionButtonPanel.setBackground(SandMarkFrame.SAND_COLOR);
      actionButtonPanel.add(forwardSlice);
      actionButtonPanel.add(backwardSlice);
      
      javax.swing.JPanel buttonPanel = 
         new javax.swing.JPanel(new java.awt.GridLayout(2,1));
      buttonPanel.setBackground(SAND_COLOR);
      buttonPanel.add(radioButtonPanel);
      buttonPanel.add(actionButtonPanel);
      
      setBackground(SAND_COLOR);
      setLayout(new java.awt.BorderLayout());
      setBackground(SAND_COLOR);
      add(instrPane, java.awt.BorderLayout.CENTER);
      add(buttonPanel, java.awt.BorderLayout.SOUTH);
   }
}

class SliceRenderer extends javax.swing.JLabel 
	implements javax.swing.ListCellRenderer {
   private static final int NOT_SELECTED = 0,NOT_IN_SLICE = 0,
      SELECTED = 1,IN_SLICE = 1;
   private static java.awt.Color sBGColors[][] = 
      new java.awt.Color[2][2];
   {
      sBGColors[NOT_SELECTED][NOT_IN_SLICE] = java.awt.Color.white;
      sBGColors[NOT_SELECTED][IN_SLICE] = java.awt.Color.red;
      sBGColors[SELECTED][NOT_IN_SLICE] = java.awt.Color.blue;
      sBGColors[SELECTED][IN_SLICE] = java.awt.Color.blue;
   }
   private static java.awt.Color sFGColors[]  =
      new java.awt.Color[2];
   {
      sFGColors[NOT_IN_SLICE] = java.awt.Color.black;
      sFGColors[IN_SLICE] = java.awt.Color.white;
   }
   private java.util.List mSliceInstrs;
   public SliceRenderer() { 
      setOpaque(true);
   }
   public void setSliceInstrs(java.util.List instrs) {
      mSliceInstrs = instrs;
   }
   public java.awt.Component getListCellRendererComponent
   	(javax.swing.JList list, Object value, int index, 
       boolean isSelected, boolean cellHasFocus) {
      setText(value.toString());
      int selected = isSelected ? SELECTED : NOT_SELECTED;
      int inSlice = 
         mSliceInstrs != null && mSliceInstrs.contains(value) ?
            		  IN_SLICE : NOT_IN_SLICE; 
      setBackground(sBGColors[selected][inSlice]);
      setForeground(sFGColors[inSlice]);
      return this;
   }
}

