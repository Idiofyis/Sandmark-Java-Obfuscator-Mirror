package sandmark.gui.diff;

/** A ScrollPane for displaying an array of sandmark.diff.Results
 *  @author Zach Heidepriem
 */

public class ResultsPane extends javax.swing.JScrollPane {
    javax.swing.JPanel main;
    DiffFrame frame;
    sandmark.diff.DiffOptions options;

    /** Make a sandmark.diff.ResultsPane 
     *  @param df the DiffFrame to add this to. DiffFrames must provide
     *  <code>public void color(sandmark.diff.Result r)</code>
     */
    public ResultsPane(sandmark.gui.diff.DiffFrame df, 
                                     sandmark.diff.DiffOptions o){       
        super(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
              javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        options = o;
	frame = df;
        main = new sandmark.gui.SkinPanel(); 		  
	setViewportView(main);
    }
    
    /** Update the results of this pane
     *  @param results the results to put in the pane
     */
    public void setResults(sandmark.diff.Result[] results){			
	main.removeAll();
	main.setLayout(new java.awt.GridLayout(0, 1));	
	if(results == null){
	    javax.swing.JTextArea j = new javax.swing.JTextArea(
                "Select an algorithm from the combo box and press" +
                " the diff button");
                                                    
            j.setPreferredSize(new java.awt.Dimension(getWidth()-50,
                                                      getHeight()));
            j.setLineWrap(true);
            j.setWrapStyleWord(true);
            main.add(j);
            return;
	}

	for(int i = 0; i < results.length; i++){           
	    final sandmark.diff.Result result = results[i];         
            double d = results[i].getSimilarity() * 100;	  
	    d = Math.round(d*10.0)/10.0;
           
	    String s = Double.toString(d) + "%";	 
       
	    javax.swing.JPanel panel = new sandmark.gui.SkinPanel();
            panel.setLayout(new java.awt.BorderLayout());
	    javax.swing.border.TitledBorder tb = new javax.swing.border.TitledBorder(s);
	    tb.setTitleFont(new java.awt.Font(null, java.awt.Font.BOLD, 16));
	    tb.setTitleColor(sandmark.gui.SandMarkGUIConstants.DARK_SAND_COLOR);
	    panel.setBorder(tb);	    
	    /*
            String strings[] = { results[i].getMethod1().getClassName(),
				 results[i].getMethod1().getName(),
				 results[i].getMethod2().getClassName(),
                                 results[i].getMethod2().getName() };
	    
	    int index = strings[0].lastIndexOf(".");
	    String a = strings[0].substring(index+1);
	    index = strings[2].lastIndexOf(".");
	    String b = strings[2].substring(index+1);
	    */	 
	    javax.swing.JTextArea ta = new javax.swing.JTextArea(2, 10);
	    ta.setBackground(null);
	    ta.setFont(new java.awt.Font(null, java.awt.Font.PLAIN, 10));
	    ta.setEditable(false);
	    
            String s1 = "", s2 = "";
            if(results[i].getObject1().getParent() != null)
                s1 += results[i].getObject1().getParent().getName() + ".";
            if(results[i].getObject2().getParent() != null)
                s2 += results[i].getObject2().getParent().getName() + ".";


            ta.append(s1 + results[i].getObject1().getName() + "\n" +
		      s2 + results[i].getObject2().getName());       
         
   	    javax.swing.JButton button = new javax.swing.JButton("View");
            button.setForeground(sandmark.gui.SandMarkGUIConstants.DARK_SAND_COLOR);
            button.setBackground(sandmark.gui.SandMarkGUIConstants.SAND_COLOR);

	    button.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e){
		    frame.color(result);
		}});

	    javax.swing.JPanel bPanel = new sandmark.gui.SkinPanel();
            bPanel.setLayout(new java.awt.BorderLayout());	    
            bPanel.add(button, java.awt.BorderLayout.WEST);   

	    panel.add(ta, java.awt.BorderLayout.CENTER);
	    panel.add(bPanel, java.awt.BorderLayout.SOUTH);
	    main.add(panel);	   	   
	}      
        if(main.getComponentCount() == 0){
            main.add(new javax.swing.JLabel("No results to display."));
            return;
        }
        scrollToTop();
    }    
    
    public void scrollToTop(){      
        if(getViewport() != null)
            getViewport().setViewPosition(new java.awt.Point(0,0));
    }

    public void validate(){
        super.validate();
        scrollToTop();
    }
}

