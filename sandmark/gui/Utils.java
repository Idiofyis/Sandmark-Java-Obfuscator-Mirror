package sandmark.gui;

public class Utils
{
   private Utils() {}

   public static void centerOnParent(
      java.awt.Component parent,
      java.awt.Component child)
   {
      java.awt.Point     loc = parent.getLocation();
      java.awt.Dimension dim = parent.getSize();

      child.setLocation(
         loc.x + (dim.width  - child.getSize().width ) / 2,
         loc.y + (dim.height - child.getSize().height) / 2
      );
   }

   public static class LabeledSlider
      extends    javax.swing.JPanel
      implements javax.swing.event.ChangeListener,
                 sandmark.gui.SandMarkGUIConstants
   {
      private javax.swing.JSlider m_slider;
      private javax.swing.JLabel  m_valText;
      private ValueMapper         m_mapper;

      public static interface ValueMapper
      {
         public String map(int value);
      }

      public LabeledSlider()
         {this("Amount", "low", "high");}

      public LabeledSlider(String ttl, String low, String high)
         {this(ttl, low, high, null);}

      public LabeledSlider(String ttl, String low, String high,
                           int min, int max)
         {this(ttl, low, high, min, max, null);}

      public LabeledSlider(String ttl, String low, String high,
                           int min, int max, int val)
         {this(ttl, low, high, min, max, val, null);}

      public LabeledSlider(String ttl, String low, String high,
                           ValueMapper mapper)
      {
         m_slider = new javax.swing.JSlider();
         init(ttl, low, high, mapper);
      }

      public LabeledSlider(String ttl, String low, String high,
                           int min, int max, ValueMapper mapper)
      {
         m_slider = new javax.swing.JSlider(min, max);
         init(ttl, low, high, mapper);
      }

      public LabeledSlider(String ttl, String low, String high,
                           int min, int max, int val,
                           ValueMapper mapper)
      {
         m_slider = new javax.swing.JSlider(min, max, val);
         init(ttl, low, high, mapper);
      }

      private void init(String ttl, String low, String high,
                        ValueMapper mapper)
      {
         setLayout(new java.awt.BorderLayout());
         setOpaque(false);

         m_mapper  = mapper;
         m_valText = new javax.swing.JLabel();

         javax.swing.JPanel top, btm;
         top = mkPanel(mkLbl(ttl, true ), m_valText         );
         btm = mkPanel(mkLbl(low, false), mkLbl(high, false));

         add(top     , java.awt.BorderLayout.NORTH );
         add(m_slider, java.awt.BorderLayout.CENTER);
         add(btm     , java.awt.BorderLayout.SOUTH );

         m_valText.setForeground(java.awt.Color.black);
         m_valText.setFont(new java.awt.Font("Monospaced",
            java.awt.Font.PLAIN, 12));

         stateChanged(null);

         m_slider.addChangeListener(this);
         m_slider.setOpaque(false);

         java.awt.Dimension dim1, dim2, dim3, dim4;
         dim1 = top     .getPreferredSize();
         dim2 = m_slider.getPreferredSize();
         dim3 = btm     .getPreferredSize();
         dim4 = getMaximumSize();

         dim4.height = dim1.height + dim2.height + dim3.height;
         setMaximumSize(dim4);
      }

      private javax.swing.JPanel mkPanel(java.awt.Component left,
                                         java.awt.Component right)
      {
         javax.swing.JPanel panel = new javax.swing.JPanel();

         panel.setLayout(new java.awt.BorderLayout());
         panel.setOpaque(false);

         panel.add(left , java.awt.BorderLayout.WEST);
         panel.add(right, java.awt.BorderLayout.EAST);

         panel.add(javax.swing.Box.createHorizontalGlue(),
            java.awt.BorderLayout.CENTER);

         return panel;
      }

      private javax.swing.JLabel mkLbl(String text, boolean bold)
      {
         javax.swing.JLabel lbl = new javax.swing.JLabel(text);

         lbl.setFont(new java.awt.Font("Dialog",
            bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 12));

         if (bold) lbl.setForeground(DARK_SAND_COLOR);

         return lbl;
      }

      public void stateChanged(javax.swing.event.ChangeEvent e)
      {
         int val = m_slider.getValue();

         String txt = (m_mapper == null)
            ? ("" + val) : m_mapper.map(val);

         m_valText.setText(txt);
      }

      public javax.swing.JSlider getSlider()
         {return m_slider;}
   }
}
