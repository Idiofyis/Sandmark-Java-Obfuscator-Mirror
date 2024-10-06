package sandmark.gui;

public class SandMarkMenuBar extends javax.swing.JMenuBar implements sandmark.gui.SandMarkGUIConstants {
    private SandMarkFrame mFrame;

    public SandMarkMenuBar(SandMarkFrame frame) {
	mFrame = frame;

        javax.swing.JMenu fileMenu = new javax.swing.JMenu("File");
        fileMenu.setForeground(DARK_SAND_COLOR);
        fileMenu.setBackground(SAND_COLOR);
        fileMenu.setMnemonic('F');

        javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem("Exit");
        exitItem.setForeground(DARK_SAND_COLOR);
        exitItem.setBackground(SAND_COLOR);
        exitItem.setMnemonic('x');
        fileMenu.add(exitItem);
        exitItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            });

        javax.swing.JMenu helpMenu = new javax.swing.JMenu("Help");
        helpMenu.setForeground(DARK_SAND_COLOR);
        helpMenu.setBackground(SAND_COLOR);
        helpMenu.setMnemonic('H');

        javax.swing.JMenuItem aboutItem = new javax.swing.JMenuItem("About");
        aboutItem.setForeground(DARK_SAND_COLOR);
        aboutItem.setBackground(SAND_COLOR);
        aboutItem.setMnemonic('A');
        helpMenu.add(aboutItem);
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    AboutDialog ad = new AboutDialog(mFrame);
                    ad.show();
                }
            });

        javax.swing.JMenuItem helpItem = new javax.swing.JMenuItem("Help");
        helpItem.setForeground(DARK_SAND_COLOR);
        helpItem.setBackground(SAND_COLOR);
        helpItem.setMnemonic('H');
        helpMenu.add(helpItem);
        helpItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    HelpDialog hd = new HelpDialog();
                    hd.showHelpFor("SandMark");
                    hd.show();
                }
            });

        javax.swing.JMenuItem licenseItem = new javax.swing.JMenuItem("License");
        licenseItem.setForeground(DARK_SAND_COLOR);
        licenseItem.setBackground(SAND_COLOR);
        licenseItem.setMnemonic('L');
        helpMenu.add(licenseItem);
        licenseItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    CopyrightDialog cd = new CopyrightDialog(mFrame);
                    cd.show();
                }
            });

        add(fileMenu);
        add(helpMenu);
        setForeground(DARK_SAND_COLOR);
        setBackground(SAND_COLOR);
    }
}

