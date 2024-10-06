package sandmark.gui;

public class SplashPanel extends javax.swing.JPanel
{
  public SplashPanel()
  {
    setLayout(null);
    setBackground(SandMarkGUIConstants.DARK_SAND_COLOR);
    try {
      img = java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource(SandMarkGUIConstants.LOGO_IMAGE));
      java.awt.MediaTracker med = new java.awt.MediaTracker(this);
      med.addImage(img, 0);
      med.waitForAll(10000);
    }
    catch (Exception ex)
    {
      displayable = false;
    }
    
    if ( img == null )
	return;

    setPreferredSize(new java.awt.Dimension(img.getWidth(null), img.getHeight(null)));
    setMinimumSize(new java.awt.Dimension(img.getWidth(null), img.getHeight(null)));
    setSize(new java.awt.Dimension(img.getWidth(null), img.getHeight(null)));
    displayable = true;
  }

  public void paintComponent(java.awt.Graphics g)
  {
    super.paintComponent(g);
    if (img != null) {
      g.drawImage(img, 0, 0, this);
    }
  }

  private java.awt.Image img;
  private boolean displayable = false;
}

