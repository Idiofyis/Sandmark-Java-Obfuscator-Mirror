package sandmark.gui.diff;

public class DiffOptionsFrame extends javax.swing.JDialog {

    private sandmark.diff.DiffOptions options;

    private javax.swing.JLabel minLabel, ignoreLabel;
    private javax.swing.JTextField filterTF, ignoreTF;
    private javax.swing.JCheckBox filterNamesCB, filterBodiesCB;
    private javax.swing.JRadioButton methodRB1, methodRB2;
    private javax.swing.JButton okButton;
    private boolean filterNames, filterBodies;
    private int objectCompare;

    public DiffOptionsFrame(java.awt.Frame f, sandmark.diff.DiffOptions o){
        super(f, "Diff Options");
        options = o;
        setSize(500,300);
        //setResizable(false);
        makeComponents();
        layoutComponents();   
        show();
    }

    private void makeComponents(){
        filterNames = options.getFilterNames();
        filterBodies = options.getFilterBodies();
        objectCompare = options.getObjectCompare();
        okButton = new javax.swing.JButton("OK");        
        okButton.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    saveAndExit();
                }});

        minLabel = new javax.swing.JLabel(
          "Show pairs of objects with similarity greather than or equal to %");
        filterTF = new javax.swing.JTextField(4);
        filterTF.setText(Double.toString(options.getFilter()*100));
      
        filterNamesCB = new javax.swing.JCheckBox("Filter out pairs of objects that " + 
                                           "have the same name",
                                           filterNames);
        filterBodiesCB = new javax.swing.JCheckBox("Filter out pairs of objects that " + 
                                           "appear to be identical",
                                           filterBodies);

        filterNamesCB.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    filterNames = !filterNames;
                }});
        filterBodiesCB.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    filterBodies = !filterBodies;
                }});

        ignoreLabel = new javax.swing.JLabel("Ignore metbods with number " + 
                                             "of instructions less than:");
        ignoreTF = new javax.swing.JTextField(4);
        ignoreTF.setText(""+options.getIgnoreLimit());        
       
        methodRB1 = new javax.swing.JRadioButton("Compare all pairs of objects",
              options.getObjectCompare() == sandmark.diff.DiffOptions.COMPARE_ALL_PAIRS);
        methodRB2 = new javax.swing.JRadioButton("Compare only objects with the same "+ 
                                                 "name",
              options.getObjectCompare() == sandmark.diff.DiffOptions.COMPARE_BY_NAME);
        methodRB1.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    objectCompare = sandmark.diff.DiffOptions.COMPARE_ALL_PAIRS;
                }});

        methodRB2.addActionListener(new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    objectCompare = sandmark.diff.DiffOptions.COMPARE_BY_NAME;
                }});
    }

    private void layoutComponents(){            
                
        java.awt.Dimension screen = 
            java.awt.Toolkit.getDefaultToolkit().getScreenSize();              	
        setLocation((int)(screen.getWidth()/2-getWidth()/2),
		    (int)(screen.getHeight()/2-getHeight()/2));
        
        java.awt.Container contentPane = getContentPane();
        java.awt.Image img = java.awt.Toolkit.getDefaultToolkit().getImage
            (getClass().getClassLoader().getResource(
              sandmark.gui.SandMarkGUIConstants.SAND_IMAGE));
     
        javax.swing.JPanel centerLeft = 
            new javax.swing.JPanel(new java.awt.GridLayout(0,1));
        centerLeft.add(minLabel);
        centerLeft.add(ignoreLabel);
      
  
        javax.swing.JPanel centerRight = 
            new javax.swing.JPanel(new java.awt.GridLayout(0,1));        
	centerRight.add(filterTF);  
        centerRight.add(ignoreTF);       
        
        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add(methodRB1);
        bg.add(methodRB2);

        javax.swing.JPanel bottom = new javax.swing.JPanel(new java.awt.GridLayout(0,1));
        bottom.add(filterNamesCB);
        bottom.add(filterBodiesCB);
        bottom.add(methodRB1);
        bottom.add(methodRB2);


        java.awt.FlowLayout flow = new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT);
        javax.swing.JPanel south = new javax.swing.JPanel(flow);        
        south.add(okButton);

        javax.swing.JPanel main = new javax.swing.JPanel(new java.awt.BorderLayout()); 
        javax.swing.JPanel center = new javax.swing.JPanel();
        center.add(centerLeft);
        center.add(centerRight);
       
        main.add(center, java.awt.BorderLayout.CENTER);         
        main.add(south, java.awt.BorderLayout.SOUTH);        
        main.add(bottom, java.awt.BorderLayout.NORTH);
        contentPane.add(main);
        sandmark.gui.diff.DiffFrame.setColors(contentPane);
    }
	
    private void saveAndExit(){        
        options.setFilterBodies(filterBodies);
        options.setFilterNames(filterNames);
        options.setObjectCompare(objectCompare);

        try{
	    double filter = Double.parseDouble(filterTF.getText())/100;	    
	    if(filter < 0 || filter > 1) 
                throw new NumberFormatException();	    
	    options.setFilter(filter);            
	}catch(NumberFormatException nfe){ 
	    javax.swing.JOptionPane.showMessageDialog(this,
                     "Filter must be between 0 and 100",
                     "Error",
                      javax.swing.JOptionPane.ERROR_MESSAGE); 
	    return;
	}

        try{
	    int ignoreLimit = Integer.parseInt(ignoreTF.getText());	    
	    if(ignoreLimit < 0) 
                throw new NumberFormatException();	    
	    options.setIgnoreLimit(ignoreLimit);            
	}catch(NumberFormatException nfe){ 
	    javax.swing.JOptionPane.showMessageDialog(this,
                     "Ignore limit must be an integer greater than or equal to 0",
                     "Error",
                      javax.swing.JOptionPane.ERROR_MESSAGE); 
	    return;
	}
        dispose();
    }
}
