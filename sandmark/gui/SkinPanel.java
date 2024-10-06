package sandmark.gui;

/*
  This is just a panel that accepts an Image to use as a skin.
  If the image is null or cannot be loaded in 10 seconds, the
  panel will ignore it and have no skin. All other behavior is the same.
*/

public class SkinPanel extends javax.swing.JPanel implements SandMarkGUIConstants {
    private java.awt.Image image;
    private int imgWidth, imgHeight;
    
    public SkinPanel()
    {
	setLayout(null);
	setBackground(new java.awt.Color(0xe8d5bd));
        image = 
            java.awt.Toolkit.getDefaultToolkit().getImage
            (getClass().getClassLoader().getResource(SAND_IMAGE));
	if (image != null){
	    java.awt.MediaTracker med = new java.awt.MediaTracker(this);
	    med.addImage(image, 0);
	    try{
		med.waitForAll(10000);
	    }catch(Exception ex){
                throw new RuntimeException(ex);
	    }
	    while((imgWidth = image.getWidth(null)) == -1 || 
		  (imgHeight = image.getHeight(null)) == -1);
	}
    }
    
    public void paintComponent(java.awt.Graphics g)
    {
	super.paintComponent(g);
	if (image != null){
	    java.awt.Dimension d = getSize();
	    for (int x = 0; x < d.width; x += imgWidth)
		for (int y = 0; y < d.height; y += imgHeight)
		    g.drawImage(image, x, y, this);
	}
    }
}

