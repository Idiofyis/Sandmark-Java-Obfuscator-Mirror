package sandmark.gui;

public class      FilenameBox
       extends    javax.swing.JComboBox
       implements java.awt.event.ItemListener,SandMarkGUIConstants
{
   private static java.util.Set m_inst;
   private static java.util.Map m_hash;
   private        FileList      m_hist;

   private static class Item
   {
      public Item(String s, String l)
         { shrt = s; lng = l; }

      public String toString()
         { return shrt; }

      String shrt;
      String lng;
   }

   private static class   FileList
                  extends java.util.ArrayList
   {
      public void add(String name)
      {
         String shrt = name, lng = name;
         try
         {
            java.io.File f = new java.io.File(name);
            if (f.exists())
            {
               shrt = f.getName();
               lng  = f.getCanonicalPath();
            }
         }
         catch (java.io.IOException e) {
             return;
    }

         for (int i = 0; i < size();)
         {
            Item it = (Item) get(i);
            if (it.lng.equals(lng)) remove(i);
            else i++;
         }

         add(0, new Item(shrt, lng));
      }
   }

   static
   {
      m_inst = new java.util.HashSet();
      m_hash = new java.util.HashMap();
      
      FileList jars = new FileList();
      String pwdJars[] = 
         new java.io.File(".").list(new java.io.FilenameFilter() {
            public boolean accept(java.io.File dir,String name) {
               return name.endsWith(".jar");
            }
         });
      
      for(int i = 0 ; i < pwdJars.length ; i++)
         jars.add(pwdJars[i]);
      
      m_hash.put("jar",jars);
   }

   public FilenameBox(Object type)
   {
      // m_instances keeps track of all existing FilenameBox objects,
      // so that they can be notified whenever any list changes
      // unfortunately this defeats garbage collection... oh well

      m_inst.add(this);

      m_hist = (FileList) m_hash.get(type);
      if (m_hist == null)
         m_hash.put(type, m_hist = new FileList());
      else updateList();

      setEditable(true);
      setSelectedItem("");
      addItemListener(this);
      setBackground(SAND_COLOR);
   }

   public void use()
   {
      String str = getSelectedItem().toString();
      if (!str.equals(""))
      {
         m_hist.add(str);

         java.util.Iterator i = m_inst.iterator();
         while (i.hasNext())
            ((FilenameBox) i.next()).updateList();
      }
   }

   private void updateList()
   {
      Object o = getSelectedItem();

      removeAllItems();
      for (int i = 0; i < m_hist.size(); i++)
         addItem(m_hist.get(i));

      setSelectedItem(o);
   }

   public void itemStateChanged(java.awt.event.ItemEvent e)
   {
      Object o = getSelectedItem();
      if (o instanceof Item)
         setSelectedItem(((Item) o).lng);
   }

   public String getText()
      {return getSelectedItem().toString();}

   public void setText(String s)
      {setSelectedItem(s);}
}



