/*
 * Created on Mar 9, 2004
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
public class AppViewPanel extends javax.swing.JTabbedPane 
   implements SandMarkGUIConstants,ViewPanel {
   
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
   private GraphView mCurrentGraph;
   private javax.swing.JPanel mSliderPanel;
   
   AppViewPanel(final sandmark.program.Application app,
         		 javax.swing.JPanel sliderPanel) {
      sandmark.util.Log.message
      	(0,"BEWARE:  Building the Application CFG may take hours");
      setBackground(SAND_COLOR);
      mSliderPanel = sliderPanel;
      
      javax.swing.JTable metricTable = 
         new javax.swing.JTable(new MetricTableModel(app));
      javax.swing.JScrollPane scrollPane = 
         new javax.swing.JScrollPane(metricTable);
      scrollPane.setBackground(SAND_COLOR);
      add("Metrics",scrollPane);
      add("Inheritance Graph",createSplitPane());
      add("Call Graph",createSplitPane());
      add("Application CFG",createSplitPane());
      addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent e) {
            int index = getSelectedIndex();
            tearDown();
            switch(index) {
            case 0:
               //No special handling for metrics
               break;
            case 1:
               buildGraphView
                  (app.getHierarchy().graph(),
                   new sandmark.util.newgraph.EditableGraphStyle(),
                   (javax.swing.JSplitPane)getSelectedComponent());
               break;
            case 2:
               sandmark.util.Log.message(0,"This is very slow and may use up your RAM");
               try {
                  buildGraphView
                  	(new sandmark.analysis.callgraph.CallGraph(app).graph(),
                      new sandmark.util.newgraph.EditableGraphStyle(),
                      (javax.swing.JSplitPane)getSelectedComponent());
               } catch(sandmark.analysis.classhierarchy.ClassHierarchyException ex) {
                  sandmark.util.Log.message(0,"unable to build call graph: " + 
                        						  ex.getMessage());
               }
               break;
            case 3:
               sandmark.util.Log.message(0,"This is very slow and may use up your RAM");
               buildGraphView
               	(new sandmark.analysis.callgraph.ApplicationCFG(app).graph(),
               	 new sandmark.util.newgraph.EditableGraphStyle(),
               	 (javax.swing.JSplitPane)getSelectedComponent());
               break;
            default:
               throw new Error("unhandled tab index " + index);
            }
         }
      });
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
}
