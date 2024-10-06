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
public class MethodViewPanel extends javax.swing.JTabbedPane 
   implements SandMarkGUIConstants,ViewPanel {
   private static boolean DEBUG = false;
   
   private static class GraphView {
      sandmark.util.graph.graphview.GraphPanel graphPanel;
      sandmark.util.graph.graphview.GraphZoomSlider graphSlider;
      javax.swing.JComponent container;
      GraphView(sandmark.util.graph.graphview.GraphZoomSlider gzs,
                javax.swing.JComponent comp) {
         graphSlider = gzs;
         container = comp;
      }
   }
   
   private sandmark.program.Method mMethod;
   private javax.swing.JPanel mSliderPanel;
   private GraphView mCurrentGraph;
   
   public MethodViewPanel(sandmark.program.Method method,
                          javax.swing.JPanel sliderPanel) {
      setBackground(SAND_COLOR);
      
      mMethod = method;
      mSliderPanel = sliderPanel;
      org.apache.bcel.generic.InstructionList il =
         method.getInstructionList();
      
      if(il == null)
         return;
      
      il.setPositions();
      
      javax.swing.JScrollPane tableScroll =
         new javax.swing.JScrollPane(getInstructionTable(method));
      tableScroll.setBackground(SAND_COLOR);
      javax.swing.JTable metricTable = 
         new javax.swing.JTable(new MetricTableModel(method));
      javax.swing.JTable statTable =
         new javax.swing.JTable(new StatTableModel(method));
      
      add("Instructions", tableScroll);
      add("CFG",createSplitPane());
      add("Interference Graph",createSplitPane());
      add("Statistics",new javax.swing.JScrollPane(statTable));
      add("Metrics",new javax.swing.JScrollPane(metricTable));
      add("Slicing",new MethodSliceGUI(method));
      
      addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent e) {
            int index = getSelectedIndex();
            tearDown();
            switch(index) {
            case 0:
            case 3:
            case 4:
            case 5:
               //No special handling for instruction,stats,metrics
               break;
            case 1:
               buildGraphView
                  (mMethod.getCFG().graph(),getCFGStyle(),
                   (javax.swing.JSplitPane)getSelectedComponent());
               break;
            case 2:
               buildGraphView
                  (mMethod.getIFG().graph(),getIFGStyle(),
                   (javax.swing.JSplitPane)getSelectedComponent());
               break;
            default:
               throw new Error("unhandled tab index " + index);
            }
         }
      });
   }
   
   public void tearDown() {
      if(mCurrentGraph != null) {
         mSliderPanel.remove(mCurrentGraph.graphSlider);
         mCurrentGraph.container.removeAll();
         mCurrentGraph = null;
      }
   }
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
   private javax.swing.JTable getInstructionTable(sandmark.program.Method method) {
      org.apache.bcel.generic.InstructionList il = method.getInstructionList();
      org.apache.bcel.classfile.ConstantPool cp =
         method.getConstantPool().getConstantPool();
      Object[] columnNames = { "Offset", "Opcode", "Params" };
      Object[][] rowData = new Object[il.size()][3];
      int instrNum = 0;
      for(org.apache.bcel.generic.InstructionHandle ih = il.getStart() ;
          ih != null ; ih = ih.getNext(),instrNum++) {
         org.apache.bcel.generic.Instruction instr = ih.getInstruction();
         rowData[instrNum][0] = new Integer(ih.getPosition());
         String instructionString = instr.toString();
         int openBracketIndex = instructionString.indexOf('[');
         int underscoreIndex = instructionString.indexOf('_');
         int closeParamIndex = instructionString.indexOf(')');
         int lastIndex;
         if(openBracketIndex == -1 && underscoreIndex == -1) lastIndex = instructionString.length();
         else if(openBracketIndex < underscoreIndex && openBracketIndex != -1) lastIndex = openBracketIndex;
         else if(underscoreIndex != -1) lastIndex = underscoreIndex;
         else lastIndex = openBracketIndex;
         if(instr instanceof org.apache.bcel.generic.IfInstruction ||
            instr instanceof org.apache.bcel.generic.LDC_W ||
            instr instanceof org.apache.bcel.generic.LDC2_W) 
            lastIndex = openBracketIndex;
         rowData[instrNum][1] = instr.toString().substring(0, lastIndex).trim();
         if(instr instanceof org.apache.bcel.generic.BranchInstruction)
            rowData[instrNum][2] = ((org.apache.bcel.generic.BranchInstruction)instr).getTarget().getPosition() + "";
         else if(underscoreIndex > -1 && openBracketIndex > -1 && !(instr instanceof org.apache.bcel.generic.LDC_W)
                  && !(instr instanceof org.apache.bcel.generic.LDC2_W))
            rowData[instrNum][2] = instructionString.substring(underscoreIndex + 1, openBracketIndex).trim();
         else if(closeParamIndex != instructionString.length()){
            String str = instructionString.substring(closeParamIndex + 1, instructionString.length()).trim();
            if (DEBUG)
               System.out.println(instr + ": str = " + str);
            if(str.equals("")) rowData[instrNum][2] = "";
            else if(instr instanceof org.apache.bcel.generic.BIPUSH ||
                     instr instanceof org.apache.bcel.generic.LocalVariableInstruction
                     || instr instanceof org.apache.bcel.generic.SIPUSH) rowData[instrNum][2] = str;
            else {
               int cpIndex;
               try{
                  cpIndex = Integer.parseInt(str);
                  rowData[instrNum][2] = cp.constantToString(cp.getConstant(cpIndex));
               } catch (Exception ex) { System.out.println("cant get index: " + str); rowData[instrNum][2] = str; }
            }
         }
         else rowData[instrNum][2] = "";
      }
      javax.swing.JTable instructionTable = 
         new javax.swing.JTable(rowData, columnNames);
      return instructionTable;
   }
   
   private static sandmark.util.newgraph.GraphStyle getCFGStyle() {
      return new sandmark.util.newgraph.EditableGraphStyle();
   }
   
   private static sandmark.util.newgraph.GraphStyle getIFGStyle() {
      return new sandmark.util.newgraph.EditableGraphStyle
      (sandmark.util.newgraph.EditableGraphStyle.BLACK,
       sandmark.util.newgraph.EditableGraphStyle.CIRCLE,
       sandmark.util.newgraph.EditableGraphStyle.SOLID,
       8, true,
       sandmark.util.newgraph.EditableGraphStyle.BLACK,
       sandmark.util.newgraph.EditableGraphStyle.SOLID,
       8, false);
   }
   
   private void buildGraphView(sandmark.util.newgraph.Graph graph,
                               sandmark.util.newgraph.GraphStyle style,
                               javax.swing.JSplitPane container) {
      // node Information area
      javax.swing.JTextArea textArea =
         new javax.swing.JTextArea("NODE INFORMATION");
      textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
      textArea.setEditable(false);
      javax.swing.JScrollPane textAreaScroll =
         new javax.swing.JScrollPane(textArea);
      
      sandmark.util.graph.graphview.GraphPanel graphPanel =
         new sandmark.util.graph.graphview.GraphPanel
         (graph, style.localize(graph), 
          sandmark.util.graph.graphview.GraphLayout.LAYERED_DRAWING_LAYOUT, 
          textArea);
      sandmark.util.graph.graphview.GraphZoomSlider slider =
         new sandmark.util.graph.graphview.GraphZoomSlider(graphPanel);
      slider.setBackground(SAND_COLOR);
      
      javax.swing.JScrollPane graphScrollPane =
         new javax.swing.JScrollPane(graphPanel);
      graphScrollPane.setBackground(SAND_COLOR);

      container.setTopComponent(graphScrollPane);
      container.setBottomComponent(textAreaScroll);
      
      java.awt.GridBagConstraints constraints = 
         new java.awt.GridBagConstraints();
      constraints.gridy = 3;
      constraints.gridx = 0;
      constraints.gridwidth = 2;
      constraints.weighty = 0.0;
      constraints.weightx = 1.0;
      constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      ((java.awt.GridBagLayout)mSliderPanel.getLayout()).
         setConstraints(slider,constraints);
      mSliderPanel.add(slider);
      
      mCurrentGraph = new GraphView(slider,graphScrollPane);
   }
   
   private static javax.swing.JSplitPane createSplitPane() {
      javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane
      	(javax.swing.JSplitPane.VERTICAL_SPLIT) {
         public java.awt.Dimension getPreferredSize() {
            return new java.awt.Dimension(0,0);
         }
      };
      splitPane.setOneTouchExpandable(true);
      splitPane.setResizeWeight(0.85);
      splitPane.setBackground(SAND_COLOR);
      return splitPane;
   }
}
