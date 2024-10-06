package sandmark.gui;

public interface SandMarkGUIConstants extends SMarkGUIConstants {
    public static final String ROOT_PATH = "sandmark/html/";

    //from AboutDialog
    public static final String LOGO_IMAGE = ROOT_PATH + "logo.jpg";
    public static final String SMALL_LOGO_IMAGE = ROOT_PATH + "logo-small.jpg";
    public static final java.awt.Color SAND_COLOR = new java.awt.Color(0xe8d5bd);
    public static final java.awt.Color DARK_SAND_COLOR = new java.awt.Color(0x7f7568);
    
    //from ObfuscateConfigDialog
    public static final int _12_CHARS_WIDE = 120;
    public static final java.awt.Dimension NS_PANEL_DIMS = new java.awt.Dimension(515, 50);
    public static final java.awt.Dimension W_PANEL_DIMS = new java.awt.Dimension(15, 400);

    //from StatDialog
    public static final java.awt.Dimension METHOD_DIALOG_DIMS = new java.awt.Dimension(633, 400);
    public static final java.awt.Dimension CLASS_DIALOG_DIMS = new java.awt.Dimension(631, 400);
    public static final java.awt.Dimension PACKAGE_DIALOG_DIMS = new java.awt.Dimension(378, 400);

    //from ObTableModel
    public static final int NO_OBFUSCATION = 0;
    public static final int HALF_OBFUSCATION = 1;
    public static final int FULL_OBFUSCATION = 2;

    //from SandMarkFrame
    public static final String TITLE = 
       "SandMark " + sandmark.Constants.longVersionString();
    public static final String SAND_IMAGE = ROOT_PATH + "sand.jpg";

    //from StatDialog, CodeDialog
    public static final int JDIALOG_OFFSET = 17;
    public static final int CLASS_STAT_DIALOG_WIDTH_0 = _12_CHARS_WIDE;
    public static final int CLASS_STAT_DIALOG_WIDTH_1 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_2 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_3 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_4 = 100;
    public static final int CLASS_STAT_DIALOG_WIDTH_5 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_6 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_7 = 100;
    public static final int CLASS_STAT_DIALOG_WIDTH_8 = 100;
    public static final int CLASS_STAT_DIALOG_WIDTH_9 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_10 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_11 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_12 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_13 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_14 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_15 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_16 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_17 = 70;
    public static final int CLASS_STAT_DIALOG_WIDTH_18 = 70;

    public static final int METHOD_STAT_DIALOG_WIDTH_0 = _12_CHARS_WIDE;
    public static final int METHOD_STAT_DIALOG_WIDTH_1 = 60;
    public static final int METHOD_STAT_DIALOG_WIDTH_2 = 60;
    public static final int METHOD_STAT_DIALOG_WIDTH_3 = 65;
    public static final int METHOD_STAT_DIALOG_WIDTH_4 = 100;
    public static final int METHOD_STAT_DIALOG_WIDTH_5 = 60;
    public static final int METHOD_STAT_DIALOG_WIDTH_6 = 70;

    public static final int PACKAGE_STAT_DIALOG_WIDTH_0 = 175;

    public static final int PACKAGE = 0;
}



