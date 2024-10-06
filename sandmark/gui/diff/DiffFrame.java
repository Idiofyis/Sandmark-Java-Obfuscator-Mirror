package sandmark.gui.diff;

/** A frame for diffing jar files and viewing resulting pair of methods
 *  side by side.
 *  @author Zach Heidepriem
 */

public class DiffFrame extends javax.swing.JFrame{

    private static boolean DEBUG = false;
    private int HPAD = 10, VPAD = 115;
    private javax.swing.JButton diffButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JButton optionsButton;
  
    private sandmark.diff.DiffOptions options;

    private javax.swing.JScrollPane leftScrollPane;
    private javax.swing.JScrollPane rightScrollPane;
    private sandmark.gui.diff.ResultsPane resultsPane;   
    private BytecodeEP leftEP;
    private BytecodeEP rightEP;
 
    private javax.swing.JLabel messageLabel;

    private java.io.File leftFile, rightFile;
    private String leftFilename, rightFilename;
    
    private java.util.Vector algorithms;
    private sandmark.diff.DiffAlgorithm algorithm;
    private javax.swing.JComboBox algorithmComboBox;

    private sandmark.program.Application app1, app2;
    private int numResults;
   
    private javax.swing.JProgressBar progressBar;

    /** Create and show a DiffFrame
     *  @param a the name of the first jar file to diff
     *  @param b the name of the second jar file to diff
     */
    public DiffFrame(String a, String b){      
        super("Bytecode Diff");        	   
	if(a == null || b == null || a == "" || b == ""){
	    error("Please select two files.");
	    hide();
	    dispose();	     
	    return;
	}
	leftFilename = a;
	rightFilename = b;
	leftFile = new java.io.File(a);
	rightFile = new java.io.File(b);
	try{
            app1 = new sandmark.program.Application(leftFilename);
	}catch(Exception e){ 
	    error( "Could not load " + leftFilename);
	    hide();
	    dispose();	     
	    return;    
	}
	try{
            app2 = new sandmark.program.Application(rightFilename);
	}catch(Exception e){ 
	    error("Could not load " + rightFilename);
	    hide();
	    dispose();	     
	    return;	
	}	     
        options = new sandmark.diff.DiffOptions();
        algorithms = new java.util.Vector();	
     
	algorithms.add(
           new sandmark.diff.methoddiff.TrivialDiffAlgorithm(app1, app2, options,true)); 
        algorithms.add(
            new sandmark.diff.methoddiff.TrivialDiffAlgorithm(app1, app2, options));
        algorithms.add(
            new sandmark.diff.methoddiff.BakerAlgorithm(app1, app2, options));
        algorithms.add(
           new sandmark.diff.methoddiff.DMDiffAlgorithm(app1, app2, options));  
        algorithms.add(
           new sandmark.diff.methoddiff.CFGDiff(app1, app2, options));
        algorithms.add(
           new sandmark.diff.classdiff.ConstPoolDiff(app1, app2, options));
        
        makeComponents();
        layoutComponents();
	//pack();		
    }

    private void makeComponents(){
	//int tfsize = 14; 
	progressBar = new javax.swing.JProgressBar();
	progressBar.setStringPainted(true);

       
	diffButton = new javax.swing.JButton("Diff");     
	diffButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e){
                    algorithm = (sandmark.diff.DiffAlgorithm)
                        algorithmComboBox.getSelectedItem();
                    diff();
                    resultsPane.repaint();
		}});
	stopButton = new javax.swing.JButton("Stop");       
	stopButton.setEnabled(false);
    
        optionsButton = new javax.swing.JButton("Options...");
        final java.awt.Frame frame = this;
        optionsButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e){
                    new sandmark.gui.diff.DiffOptionsFrame(frame, options);
                }});
	
        algorithm = (sandmark.diff.DiffAlgorithm)algorithms.get(0);          
               	
	algorithmComboBox = new javax.swing.JComboBox(algorithms);       
	algorithmComboBox.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e){		  
                    sandmark.diff.DiffAlgorithm alg = (sandmark.diff.DiffAlgorithm)
                        algorithmComboBox.getSelectedItem();
		    messageLabel.setText(alg.getDescription());	
		}});
	messageLabel = new javax.swing.JLabel(algorithm.getDescription());       
	if(algorithm instanceof sandmark.diff.Monitorable) progressBar.setString(null);
	else { progressBar.setString("Unavailable"); }		
    }
 
    private void layoutComponents(){
         setSize(800,600);
        //setResizable(false);
        java.awt.Container contentPane = getContentPane();
        
        javax.swing.JPanel southPanel = new sandmark.gui.SkinPanel();  
        javax.swing.JPanel sePanel = new sandmark.gui.SkinPanel();
        
        java.awt.FlowLayout flow = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
        java.awt.FlowLayout flow2 = new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT);
        sePanel.setLayout(flow);              
	
	messageLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder()); 
            	
        sePanel.add(algorithmComboBox);
	sePanel.add(progressBar); 	
        sePanel.add(diffButton);
	sePanel.add(stopButton);
	     
        //javax.swing.JPanel leftPanel = new javax.swing.JPanel(flow);
        javax.swing.JPanel optionsPanel = new sandmark.gui.SkinPanel();
        optionsPanel.setLayout(flow2);
        optionsPanel.add(optionsButton);
   
        javax.swing.JPanel middle = 
            new javax.swing.JPanel(new java.awt.BorderLayout());
        //leftPanel.add(sePanel);
     
        middle.add(sePanel, java.awt.BorderLayout.CENTER);
        middle.add(optionsPanel, java.awt.BorderLayout.EAST);
        //sePanel.add(optionsButton);

        southPanel.setLayout(new java.awt.GridLayout(2,1));
	southPanel.add(messageLabel);
	southPanel.add(middle);

        java.awt.Dimension paneDim = new java.awt.Dimension(3*getWidth()/8-HPAD, 
            getHeight()-southPanel.getHeight()-VPAD);
        leftScrollPane = new javax.swing.JScrollPane();
        //leftScrollPane.setPreferredSize(paneDim);  
        rightScrollPane = new javax.swing.JScrollPane();
        //rightScrollPane.setPreferredSize(paneDim);	          

        resultsPane = new sandmark.gui.diff.ResultsPane(this, options);
        resultsPane.setResults(null);
        resultsPane.setPreferredSize(new java.awt.Dimension(getWidth()/4-HPAD,
                                                   (int)paneDim.getHeight()));
     
        javax.swing.JPanel main = new javax.swing.JPanel();
        main.setLayout(new java.awt.GridLayout(0,1));
        javax.swing.JSplitPane split1 = 
            new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, true,
                                       leftScrollPane, rightScrollPane);
        javax.swing.JSplitPane split2 = 
            new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT,true,
                                       resultsPane, split1);    
   
        split1.setDividerLocation(3*getWidth()/8-HPAD);
        split1.setOneTouchExpandable(true);
        split2.setOneTouchExpandable(true);
        main.add(split2);
       
	contentPane.add(southPanel, java.awt.BorderLayout.SOUTH); 
        contentPane.add(main, java.awt.BorderLayout.CENTER);      

        setColors(contentPane);                  
       
        rightScrollPane.getViewport().setBackground(java.awt.Color.white);
        leftScrollPane.getViewport().setBackground(java.awt.Color.white);
       
	java.awt.Dimension screen = 
            java.awt.Toolkit.getDefaultToolkit().getScreenSize();              	
        setLocation((int)(screen.getWidth()/2-getWidth()/2),
		    (int)(screen.getHeight()/2-getHeight()/2));       
    }   

    //Recursively set fg/bg     
    public static void setColors(java.awt.Container container){
        for(int i = 0; i < container.getComponents().length; i++){                
            java.awt.Component c = container.getComponents()[i];
            if(c instanceof java.awt.Container)
                setColors((java.awt.Container)c);               
            c.setForeground(sandmark.gui.SandMarkGUIConstants.DARK_SAND_COLOR);
            c.setBackground(sandmark.gui.SandMarkGUIConstants.SAND_COLOR);       
        }
    }

    private static sandmark.util.ConfigProperties sConfigProps;
    public static sandmark.util.ConfigProperties getProperties(){
        if(sConfigProps == null) {
            String[][] props = {};
            sConfigProps = new sandmark.util.ConfigProperties
                (props,sandmark.Console.getConfigProperties());
        }
        return sConfigProps;
    }

    /**
     *  Get the HTML codes of the About page for Diff
     @return html code for the about page
    */
    public static java.lang.String getAboutHTML(){
        return
            "<HTML><BODY>" +
            "<CENTER><B></B></CENTER>" +
            "</BODY></HTML>";
    }

    /**
     *  Get the URL of the Help page for Decompile
     @return url of the help page
    */
    public static java.lang.String getHelpURL(){
        return "sandmark/diff/doc/help.html";
    }

    /*
     *  Describe what diffing is.
     */
    public static java.lang.String getOverview(){
	return "Compare the bytecodes of two jar-files for " +
            "similarity. ";
    }

    /** Create and show a DiffFrame
     *  @param args the first two arguments should contain the names of the files to diff 
     */
    public static void main(String[] args){
	DiffFrame f;
	if(args.length < 2){
            args = new String[]{"zachtest.jar", "zachtest_wm.jar"};     
	}
        f = new DiffFrame(args[0],args[1]);
        f.setDefaultCloseOperation(EXIT_ON_CLOSE); //for testing
        f.show();	
    }

    public static void error(String s){
	javax.swing.JOptionPane.showMessageDialog(null, s, "Error", 
                                                  javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /** Shows a sandmark.diff.Result in this DiffFrame
     *  @param r sandmark.diff.Result to show in this DiffFrame 
     */
    public void color(sandmark.diff.Result r){
	sandmark.diff.Coloring[] c = algorithm.color(r);	

	leftEP = new BytecodeEP(c[0]);
	rightEP = new BytecodeEP(c[1]);			

        java.awt.FlowLayout fl = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);   
        javax.swing.JPanel pnl = new javax.swing.JPanel(fl);            
        javax.swing.JPanel pnl2 = new javax.swing.JPanel(fl);
        pnl.setBackground(java.awt.Color.white);
        pnl2.setBackground(java.awt.Color.white);
        
	javax.swing.border.TitledBorder tb = 
           new javax.swing.border.TitledBorder(leftFile.getName());
	tb.setTitleFont(new java.awt.Font(null, java.awt.Font.PLAIN, 14));	
	pnl.setBorder(tb);	

	tb = new javax.swing.border.TitledBorder(rightFile.getName());
	tb.setTitleFont(new java.awt.Font(null, java.awt.Font.PLAIN, 14));	
	pnl2.setBorder(tb);	
              
        //leftEP.setSize(400, 400);
        //rightEP.setSize(400,400);
        pnl.add(leftEP);
        pnl2.add(rightEP);

	leftScrollPane.setViewportView(pnl);
	rightScrollPane.setViewportView(pnl2);	
    }

    private void diff(){
	if(leftScrollPane != null){
	    leftScrollPane.setViewportView(null);
	    rightScrollPane.setViewportView(null);
	}
	messageLabel.setText("Using " + algorithm.getName() + "...");		    	
	final Thread thread = new Thread(algorithm);
	    	  
        final sandmark.diff.Monitorable m = (sandmark.diff.Monitorable)algorithm;
        thread.start();		
        progressBar.setIndeterminate(true);       
        int length = m.getTaskLength();
        //make sure we have time to calculate length
        while(length < 0){
            try{
                Thread.sleep(10);		
                length = m.getTaskLength();
            }catch(InterruptedException ie){ ie.printStackTrace(); }
        }
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(length);	
        timer = new javax.swing.Timer(50, 
                   new java.awt.event.ActionListener() {
                       public void actionPerformed(java.awt.event.ActionEvent e){
                           progressBar.setValue(m.getCurrent());    
                           if(!thread.isAlive()){
                               diffButton.setEnabled(true);
                               stopButton.setEnabled(false);		
                               timer.stop();		
                               showResults();                                  
                               messageLabel.setText(messageLabel.getText()+
                                                    "done. " +
                                                    "Found " + numResults +
                                                    " results.");
                           }
                       }});
        timer.start();
        diffButton.setEnabled(false);
        stopButton.setEnabled(true);	
        stopButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e){
                    timer.stop();
                    algorithm.stop();
                    progressBar.setValue(progressBar.getMinimum());
                    diffButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    messageLabel.setText("Diff canceled.");
                }});		    
    }
    
    private javax.swing.Timer timer;
    
    private void showResults(){		
	sandmark.diff.Result[] results = algorithm.getResults();
        resultsPane.setResults(results);       
        numResults = results.length;
    }       
    
    //An editor pane to show a sandmark.diff.Coloring
    private class BytecodeEP extends javax.swing.JList {       		
	sandmark.diff.Coloring coloring;
	
        public BytecodeEP(sandmark.diff.Coloring c){	    
	    coloring = c;        
            java.util.Vector instrs = new java.util.Vector();
            for(int i = 0; i < coloring.size(); i++)
                instrs.add(coloring.get(i).toString());
            
            setCellRenderer(new MyCellRenderer());
            setListData(instrs);	
	}

        
        private class MyCellRenderer extends javax.swing.JLabel
            implements javax.swing.ListCellRenderer {           
          
            public java.awt.Component getListCellRendererComponent(javax.swing.JList list, 
                                                                   Object value, int index, 
                                                                   boolean isSelected, 
                                                                   boolean cellHasFocus) {
                setText(value.toString());
                int color = coloring.getColor(index);
                this.setOpaque(true);               
                this.setBackground(intToColor(color));
                if(this.getBackground().equals(java.awt.Color.WHITE))
                    this.setForeground(java.awt.Color.BLACK);
                else
                    this.setForeground(java.awt.Color.WHITE);
                return this;
            }
        }

        public java.awt.Color intToColor(int color){
            if(color == 0)
                return java.awt.Color.WHITE;
            java.awt.Color[] colors = new java.awt.Color[]{                
                java.awt.Color.BLUE, 
                java.awt.Color.RED,               
                java.awt.Color.GREEN.darker().darker(),
                java.awt.Color.MAGENTA.darker().darker(),
                java.awt.Color.LIGHT_GRAY,
                java.awt.Color.ORANGE,
                java.awt.Color.BLUE.darker().darker(),
                java.awt.Color.PINK.darker().darker(),
                java.awt.Color.BLACK,
                java.awt.Color.YELLOW.darker().darker().darker(),
                java.awt.Color.DARK_GRAY.brighter().brighter()                
               };
            return colors[color%colors.length];
        }
    } 	
}

 

