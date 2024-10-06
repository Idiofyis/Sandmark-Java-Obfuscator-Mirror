// ObfDialog.java

package sandmark.gui;

/**
   The Obfuscation dialog pops up when the user clicks the Configure
   button in the Obfuscate panel.  This dialog lets the user select
   specific obfuscation algorithms and obfuscation levels for
   individual methods, classes, or the entire jar file.

   @author Steven Kobes
 */

public class      ObfDialog
       extends    javax.swing.JDialog
       implements java.awt.event.ActionListener,
                  javax.swing.event.TreeSelectionListener,
                  javax.swing.event.ChangeListener,
                  SandMarkGUIConstants
{
   // named constants

   private static final String TITLE = "Obfuscation";

   private static final java.awt.Dimension WINDOW_SIZE
      = new java.awt.Dimension(700, 300);

   private static final int MIN_LEFT_PANE_WIDTH  = 100;
   private static final int MIN_RIGHT_PANE_WIDTH = 270;
   private static final int DIVIDER_LOCATION     = 400;
   private static final int MIN_HEIGHT_FOR_ADV   = 305;
   private static final int PREF_HEIGHT_FOR_ADV  = 430;
   private static final int CHECK_BOX_WIDTH      = 25 ;

   // GUI widgets

   private javax.swing.JSplitPane  m_splitPane;
   private javax.swing.JTree       m_tree;
   private javax.swing.JLabel      m_lblTitle;
   private Utils.LabeledSlider     m_obfSlider;
   private javax.swing.JCheckBox   m_chkThread;
   private javax.swing.JCheckBox   m_chkReflect;
   private Utils.LabeledSlider     m_perfSlider;
   private javax.swing.JCheckBox   m_advCheck;
   private javax.swing.JScrollPane m_obfList;
   private javax.swing.JTable      m_table;
   private javax.swing.JButton     m_btnOK;
   private javax.swing.JButton     m_btnCancel;
   private ObfListModel            m_obfModel;
   
   private java.util.Hashtable mCachedConstraints = new java.util.Hashtable();

   /**
      Constructs a new ObfDialog with the given parent and jar file.
      @param parent The parent frame.
      @param app    The application we're configuring
    */
   public ObfDialog(javax.swing.JFrame parent, final sandmark.program.Application app)
   {
      super(parent, true);

      setTitle(TITLE);
      setSize(WINDOW_SIZE);
      setResizable(true);
      setDefaultCloseOperation(HIDE_ON_CLOSE);
      setBackground(DARK_SAND_COLOR);

      java.awt.Container con = getContentPane();

      con.setBackground(SAND_COLOR);
      con.setLayout(new java.awt.BorderLayout());
      con.add(mkSplitPane(app), java.awt.BorderLayout.CENTER);


      this.addWindowListener(new java.awt.event.WindowAdapter(){
              public void windowDeactivated(java.awt.event.WindowEvent we){
                  try{
                      app.saveUserConstraints();
                  }catch(java.io.IOException ioe){
                      javax.swing.JOptionPane.showMessageDialog(null, ioe);
                  }
              }
          });

      update();
      Utils.centerOnParent(parent, this);
   }

   // the split pane contains everything on the dialog
   private javax.swing.JSplitPane mkSplitPane(sandmark.program.Application app)
   {
      m_splitPane = new javax.swing.JSplitPane(
         javax.swing.JSplitPane.HORIZONTAL_SPLIT);

      m_splitPane.setDividerLocation(DIVIDER_LOCATION);
      m_splitPane.setContinuousLayout(true);
      m_splitPane.setOpaque(false);

      m_splitPane.setTopComponent(mkLeft(app));
      m_splitPane.setBottomComponent(mkRight());

      return m_splitPane;
   }

   // an ObfTree occupies the left side of the split pane
   private javax.swing.JScrollPane mkLeft(sandmark.program.Application app)
   {
      m_tree = new AppTree
          (app,AppTree.SHOW_APPS|AppTree.SHOW_CLASSES|AppTree.SHOW_METHODS,
           javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);

      javax.swing.JScrollPane sp
         = new javax.swing.JScrollPane(m_tree);

      sp.setMinimumSize(
         new java.awt.Dimension(MIN_LEFT_PANE_WIDTH, 0));

      m_tree.addTreeSelectionListener(this);

      return sp;
   }

   // the right side of the split pane is a box in a panel
   private javax.swing.JPanel mkRight()
   {
      javax.swing.JPanel panel = new javax.swing.JPanel();

      panel.setLayout(new java.awt.BorderLayout());
      panel.setOpaque(false);

      panel.add(strut(false, 10), java.awt.BorderLayout.WEST  );
      panel.add(strut(false, 10), java.awt.BorderLayout.EAST  );
      panel.add(mkBox(),          java.awt.BorderLayout.CENTER);

      panel.setMinimumSize(
         new java.awt.Dimension(MIN_RIGHT_PANE_WIDTH, 0));

      return panel;
   }

   // this box holds all the widgets in the right pane
   private javax.swing.Box mkBox()
   {
      javax.swing.Box box = javax.swing.Box.createVerticalBox();

      box.add(strut(true, 5)); box.add(mkLabel     ());
      box.add(strut(true, 5)); box.add(mkRule      ());
      box.add(strut(true, 5)); box.add(mkObfSlider ());
      box.add(strut(true, 3)); box.add(mkCheckboxes());
      box.add(strut(true, 3)); box.add(mkPerfSlider());
      box.add(strut(true, 3)); box.add(mkAdvCheck  ());

      box.add(mkObfList());
      box.add(javax.swing.Box.createVerticalGlue());

      box.add(strut(true, 5)); box.add(mkButtons());
      box.add(strut(true, 5));

      return box;
   }

   // top right corner -- tells the user what object is being edited
   private javax.swing.JLabel mkLabel()
   {
      m_lblTitle = new javax.swing.JLabel("[no target selected] ");

      java.awt.Font font = new java.awt.Font(
         "Monospaced", java.awt.Font.ITALIC | java.awt.Font.BOLD, 14);

      m_lblTitle.setFont(font);
      m_lblTitle.setForeground(java.awt.Color.black);

      return m_lblTitle;
   }

   // little decorative horizontal rule is done by a bordered panel
   private javax.swing.JPanel mkRule()
   {
      javax.swing.JPanel panel = new javax.swing.JPanel();

      int infinity = Integer.MAX_VALUE;
      panel.setMinimumSize  (new java.awt.Dimension(4       , 4));
      panel.setPreferredSize(new java.awt.Dimension(100     , 4));
      panel.setMaximumSize  (new java.awt.Dimension(infinity, 4));

      int type = javax.swing.border.BevelBorder.LOWERED;
      panel.setBorder(new javax.swing.border.BevelBorder(type));

      return panel;
   }

   // the "obfuscation" slider
   private Utils.LabeledSlider mkObfSlider()
   {
      class obfMapper implements Utils.LabeledSlider.ValueMapper
      {
         public String map(int val)
         {
            if (val == 0) return "none";
            else if (val < 20) return "very light";
            else if (val < 40) return "light";
            else if (val < 60) return "moderate";
            else if (val < 80) return "heavy";
            else if (val < 100) return "very heavy";
            else return "maximum";
         }
      }

      m_obfSlider = new Utils.LabeledSlider(
         "Obfuscation Level", "none", "heavy", new obfMapper());

      m_obfSlider.setAlignmentX(0.0f);
      m_obfSlider.getSlider().addChangeListener(this);
      return m_obfSlider;
   }

   // the "multithreaded" and "reflection" checkboxes lie in a panel
   private javax.swing.JPanel mkCheckboxes()
   {
      javax.swing.JPanel panel = new javax.swing.JPanel();

      panel.setLayout(new java.awt.BorderLayout());
      panel.setAlignmentX(0.0f);
      panel.setOpaque(false);

      m_chkThread  = new javax.swing.JCheckBox("Multithreaded"  );
      m_chkReflect = new javax.swing.JCheckBox("Uses Reflection");

      m_chkThread .setOpaque(false);
      m_chkReflect.setOpaque(false);

      m_chkThread .addActionListener(this);
      m_chkReflect.addActionListener(this);

      java.awt.Component glue
         = javax.swing.Box.createHorizontalGlue();

      panel.add(m_chkThread , java.awt.BorderLayout.WEST  );
      panel.add(glue,         java.awt.BorderLayout.CENTER);
      panel.add(m_chkReflect, java.awt.BorderLayout.EAST  );

      java.awt.Dimension dim = panel.getMaximumSize();
      dim.height = m_chkThread.getPreferredSize().height;
      panel.setMaximumSize(dim);

      return panel;
   }

   // the "performance critical" slider
   private Utils.LabeledSlider mkPerfSlider()
   {
      class perfMapper implements Utils.LabeledSlider.ValueMapper
      {
         public String map(int val)
            {return "" + (val / 10);}
      }

      m_perfSlider = new Utils.LabeledSlider(
         "Performance Critical", "low", "high", new perfMapper());

      m_perfSlider.setAlignmentX(0.0f);
      m_perfSlider.getSlider().addChangeListener(this);
      return m_perfSlider;
   }

   // the "advanced options" checkbox
   private javax.swing.JCheckBox mkAdvCheck()
   {
      m_advCheck = new javax.swing.JCheckBox("Advanced Options");

      m_advCheck.setOpaque(false);
      m_advCheck.addActionListener(this);

      return m_advCheck;
   }

   // the list of specific obfuscations is a table in a scroll pane
   private javax.swing.JScrollPane mkObfList()
   {
      m_obfList = new javax.swing.JScrollPane(mkTable());

      m_obfList.setAlignmentX(0.0f);
      m_obfList.getViewport().setBackground(DARK_SAND_COLOR);
      m_obfList.setVisible(false);

      return m_obfList;
   }

   // and here's the table (ObfListModel is an inner class)
   private javax.swing.JTable mkTable()
   {
      m_obfModel = new ObfListModel(null,null);
      m_table = new javax.swing.JTable(m_obfModel);
      m_table.setTableHeader(null);
      m_table.setRowSelectionAllowed(false);

      fixCheckBoxColWidth();
      return m_table;
   }

   // for some reason this guy needs to get called on every update()
   private void fixCheckBoxColWidth()
   {
      javax.swing.table.TableColumn col
         = m_table.getColumnModel().getColumn(0);

      col.setMinWidth      (CHECK_BOX_WIDTH);
      col.setMaxWidth      (CHECK_BOX_WIDTH);
      col.setPreferredWidth(CHECK_BOX_WIDTH);
   }

   // the OK and Cancel buttons at the bottom lie in their own panel
   private javax.swing.JPanel mkButtons()
   {
      javax.swing.JPanel panel = new javax.swing.JPanel();

      panel.setOpaque(false);
      panel.setAlignmentX(0.0f);

      m_btnOK     = new javax.swing.JButton("OK");
      m_btnCancel = new javax.swing.JButton("Cancel");

      m_btnOK    .addActionListener(this);
      m_btnCancel.addActionListener(this);

      panel.add(m_btnOK);
      panel.add(m_btnCancel);

      getRootPane().setDefaultButton(m_btnOK);

      java.awt.Dimension dim = panel.getMaximumSize();
      dim.height = panel.getPreferredSize().height;
      panel.setMaximumSize(dim);

      return panel;
   }

   // a strut is a fixed-size spacer component for a box or a panel
   private java.awt.Component strut(boolean vertical, int size)
   {
      return vertical
         ? javax.swing.Box.createVerticalStrut(size)
         : javax.swing.Box.createHorizontalStrut(size);
   }

   // table model for the list of obfuscations
   private class   ObfListModel
           extends javax.swing.table.AbstractTableModel
   {

      private String applicableAlgShortNames[];
       private sandmark.program.UserObjectConstraints constraints;
       ObfListModel(sandmark.program.Object o,
                    sandmark.program.UserObjectConstraints c) {
          applicableAlgShortNames = o == null ? null : findApplicableAlgs(o);
          constraints = c;
       }
      // one row for every obfuscation
      public int getRowCount() {
          return applicableAlgShortNames == null ? 
                 0 : applicableAlgShortNames.length;
      }

      // two columns: one for a check box and one for the name
      public int getColumnCount()
         {return 2;}

      // check boxes are Booleans, names are Strings
      public Class getColumnClass(int c)
       {return c == 0 ? Boolean.class : String.class;}

      public Object getValueAt(int row, int c)
      {
          if (c == 1)
              return applicableAlgShortNames[row];
          else
              return new Boolean
                  (constraints.isAlgoOn
                   (applicableAlgShortNames[row]));
      }

      public void setValueAt(Object val, int row, int col)
      {
          constraints.setAlgoOn
              (applicableAlgShortNames[row], ((Boolean) val).booleanValue());
          fireTableCellUpdated(row, col);
      }

      // only column zero (the checkboxes) is editable
      public boolean isCellEditable(int row, int col)
         {return col == 0;}

      // called by update()... new table data
      public void fireChange()
      {
         fireTableStructureChanged();
         fireTableDataChanged();
      }
       private String [] findApplicableAlgs(sandmark.program.Object object) {
           int algType;
           if(object instanceof sandmark.program.Application)
               algType = sandmark.util.classloading.IClassFinder.APP_ALGORITHM;
           else if(object instanceof sandmark.program.Class)
               algType = sandmark.util.classloading.IClassFinder.CLASS_ALGORITHM;
           else if(object instanceof sandmark.program.Method)
               algType = sandmark.util.classloading.IClassFinder.METHOD_ALGORITHM;
           else
               throw new RuntimeException("unkown object type");
           String names[] = (String[])
               sandmark.util.classloading.ClassFinder.getClassesWithAncestor(algType).toArray
               (new String[0]);
           for(int i = 0 ; i < names.length ; i++)
               names[i] =
                   sandmark.util.classloading.ClassFinder.getClassShortname
                   (names[i]);
           return names;
       }
   }

   /**
      Handle user manipulation of JButtons and JCheckBoxes.
      @param evt The event parameters.
    */

   public void actionPerformed(java.awt.event.ActionEvent evt)
   {
      Object src = evt.getSource();

           if (src == m_btnCancel ) hide();
      else if (src == m_btnOK     ) onOK();
      else if (src == m_advCheck  ) onAdvClick();
      else if (src == m_chkReflect) refl();
      else if (src == m_chkThread ) thread();
   }

   // called when the "reflection" checkbox changes
   private void refl()
   {
       sandmark.program.UserObjectConstraints constraints =
           (sandmark.program.UserObjectConstraints)mCachedConstraints.get
            (m_tree.getSelectionPath().getLastPathComponent());
       constraints.reflection = m_chkReflect.isSelected();
   }

   // called when the "multithreaded" checkbox changes
   private void thread()
   {
      sandmark.program.UserObjectConstraints constraints =
         (sandmark.program.UserObjectConstraints)mCachedConstraints.get
          (m_tree.getSelectionPath().getLastPathComponent());
       constraints.multithreaded = m_chkThread.isSelected();
   }

   // called when OK is clicked
   private void onOK()
   {
      for(java.util.Iterator objects = mCachedConstraints.keySet().iterator() ;
          objects.hasNext() ; ) {
         sandmark.program.Object object = 
            (sandmark.program.Object)objects.next();
         sandmark.program.UserObjectConstraints localConstraints =
            (sandmark.program.UserObjectConstraints)mCachedConstraints.get
            (object);
         object.getUserConstraints().copyFrom(localConstraints);
      }
      hide();
   }

   // called when "advanced" is clicked
   private void onAdvClick()
   {
      boolean visible = m_advCheck.isSelected();
      m_obfList.setVisible(visible);

      // if the window isn't big enough, make it bigger
      java.awt.Dimension dim = getSize();
      if (visible && dim.height < MIN_HEIGHT_FOR_ADV)
      {
         dim.height = PREF_HEIGHT_FOR_ADV;
         setSize(dim);
      }

      // rearrange and repaint the window
      invalidate();
      validate();
   }

   /**
      Called when a slider is slid.
      @param evt The event parameters.
    */
   public void stateChanged(javax.swing.event.ChangeEvent evt)
   {
      Object src = evt.getSource();
      sandmark.program.UserObjectConstraints constraints =
         (sandmark.program.UserObjectConstraints)mCachedConstraints.get
          (m_tree.getSelectionPath().getLastPathComponent());

      if (src == m_obfSlider.getSlider())
         constraints.obfuscationLevel =
            m_obfSlider.getSlider().getValue() / 100.0f;

      else if (src == m_perfSlider.getSlider())
          constraints.performanceCritical =
            m_perfSlider.getSlider().getValue() / 100.0f;
   }

   /**
      Called when a new object in the tree is selected.
      @param evt The event parameters.
    */
   public void valueChanged
      (javax.swing.event.TreeSelectionEvent evt)
   {
      javax.swing.tree.TreePath path = m_tree.getSelectionPath();
      sandmark.program.Object object = path == null ? null :
         (sandmark.program.Object)path.getLastPathComponent();
      sandmark.program.UserObjectConstraints constraints =
         object == null ? null : 
         (sandmark.program.UserObjectConstraints)mCachedConstraints.get(object);
      if(constraints == null && object != null) {
         constraints = object.getUserConstraints();
         mCachedConstraints.put(object,constraints);
      }
      m_table.setModel(m_obfModel = new ObfListModel(object,constraints));
      update();
   }

   // update the widgets in the right pane of the
   // dialog with info about a newly selected object

   private void update()
   {
      // the path through the tree from root to current selection
      javax.swing.tree.TreePath path = m_tree.getSelectionPath();

      if (path == null) { // nothing selected
         if(m_obfModel != null)
            m_obfModel.fireChange();

         m_obfSlider .setVisible(false);
         m_chkThread .setVisible(false);
         m_chkReflect.setVisible(false);
         m_perfSlider.setVisible(false);
         m_advCheck  .setVisible(false);
         m_obfList   .setVisible(false);

         m_lblTitle.setText("[no target selected]");
      } else {
         sandmark.program.Object selectedObject =
            (sandmark.program.Object)path.getLastPathComponent();
         sandmark.program.UserObjectConstraints constraints =
            (sandmark.program.UserObjectConstraints)
            mCachedConstraints.get(selectedObject);

         m_obfModel.fireChange(); // update obfuscation list

         m_obfSlider .setVisible(true);
         m_chkThread .setVisible(true);
         m_chkReflect.setVisible(true);
         m_perfSlider.setVisible(true);
         m_advCheck  .setVisible(true);
         m_obfList   .setVisible(m_advCheck.isSelected());

         m_obfSlider.getSlider().setValue(
            (int) (constraints.obfuscationLevel * 100.0));

         m_chkThread .setSelected(constraints.multithreaded);
         m_chkReflect.setSelected(constraints.reflection);

         m_perfSlider.getSlider().setValue(
            (int) (constraints.performanceCritical * 100.0));

         // if you don't put a space at the end the last letter
         // gets slightly cut off because of the italic font :(

         m_lblTitle.setText(selectedObject.toString());

         // for some reason JTable likes to reset the column widths
         // every time the model changes, so this needs to be called
         // on each update()

         fixCheckBoxColWidth();
      }
   }
}

