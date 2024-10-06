/*
 * Author: Albert L. M. Ting <alt@artisan.com>
 *
 * Released into the public domain.
 *
 * $Revision: 1.1 $
 * $Id: MultiLineToolTipUI.java,v 1.1 2003/07/23 23:37:17 ash Exp $
 */

package sandmark.gui;

/**
 * To add multiline tooltip support to your swing applications, just add this
 * static call to your main method.  Note, you only need to do this once, even
 * if you change LookAndFeel as the UIManager knows not to overwrite the user
 * defaults.  Moreover, it uses the current L&F foreground/background colors
 * <p><pre>
 *        MultiLineToolTipUI.initialize();
 * </pre><p>
 * @author Albert L. M. Ting
 */
public class MultiLineToolTipUI extends javax.swing.plaf.ToolTipUI {
    static MultiLineToolTipUI SINGLETON = new MultiLineToolTipUI();
    static boolean DISPLAY_ACCELERATOR=true;
    static int TIP_LINE_LENGTH = 40;

    int m_inset = 3;
    int m_accelerator_offset = 15;

    private MultiLineToolTipUI() {
    }

    public static void initialize() {
	// don't hardcode the class name, fetch it dynamically.  This way we can
	// obfuscate.
	String key = "ToolTipUI";
	Class cls = SINGLETON.getClass();
	String name = cls.getName();
	javax.swing.UIManager.put(key,name);
	javax.swing.UIManager.put(name,cls);	// needed for 1.2
    }

    public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent c) {
	return SINGLETON;
    }

    public void installUI(javax.swing.JComponent c) {
	javax.swing.LookAndFeel.installColorsAndFont(c,
						     "ToolTip.background",
						     "ToolTip.foreground",
						     "ToolTip.font");
	javax.swing.LookAndFeel.installBorder(c, "ToolTip.border");
    }

    public void uninstallUI(javax.swing.JComponent c) {
	javax.swing.LookAndFeel.uninstallBorder(c);
    }

    public static void setDisplayAcceleratorKey(boolean val) {
	DISPLAY_ACCELERATOR=val;
    }

    public java.awt.Dimension getPreferredSize(javax.swing.JComponent c) {
	java.awt.Font font = c.getFont();
	java.awt.FontMetrics fontMetrics = java.awt.Toolkit.getDefaultToolkit().getFontMetrics(font);
	int fontHeight = fontMetrics.getHeight();
	String tipText = ((javax.swing.JToolTip)c).getTipText();

	if (tipText == null) tipText = "";

	String lines[] = breakupLines(tipText);
	int num_lines = lines.length;
	java.awt.Dimension dimension;
	int width,height,onewidth;

	height = num_lines * fontHeight;
	width = 0;
	for (int i=0; i<num_lines; i++) {
	    onewidth = fontMetrics.stringWidth(lines[i]);
	    if (DISPLAY_ACCELERATOR && i == num_lines - 1) {
		String keyText = getAcceleratorString((javax.swing.JToolTip)c);
		if (!keyText.equals("")) {
		    onewidth += fontMetrics.stringWidth(keyText) + m_accelerator_offset;
		}
	    }
	    width = Math.max(width,onewidth);
	}
	return new java.awt.Dimension(width+m_inset*2,height+m_inset*2);
    }

    public java.awt.Dimension getMinimumSize(javax.swing.JComponent c) {
	return getPreferredSize(c);
    }

    public java.awt.Dimension getMaximumSize(javax.swing.JComponent c) {
	return getPreferredSize(c);
    }

    public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
	java.awt.Font font = c.getFont();
	java.awt.FontMetrics fontMetrics =
	    java.awt.Toolkit.getDefaultToolkit().getFontMetrics(font);
	java.awt.Dimension dimension = c.getSize();
	int fontHeight = fontMetrics.getHeight();
	int fontAscent = fontMetrics.getAscent();
	String tipText = ((javax.swing.JToolTip)c).getTipText();
	String lines[] = breakupLines(tipText);
	int num_lines = lines.length;
	int height;
	int i;

	g.setColor(c.getBackground());
	g.fillRect(0, 0, dimension.width, dimension.height);
	g.setColor(c.getForeground());
	for (i=0, height=2+fontAscent; i<num_lines; i++, height+=fontHeight) {
	    g.drawString(lines[i], m_inset, height);
	    if (DISPLAY_ACCELERATOR && i == num_lines - 1) {
		String keyText = getAcceleratorString((javax.swing.JToolTip)c);
		if (!keyText.equals("")) {
		    java.awt.Font smallFont =
			new java.awt.Font(font.getName(), font.getStyle(), font.getSize() - 2 );
		    g.setFont(smallFont);
		    g.drawString(keyText,
				 fontMetrics.stringWidth(lines[i])+m_accelerator_offset,
				 height);
		}
	    }
	}
    }

    public String getAcceleratorString(javax.swing.JToolTip tip) {
	javax.swing.JComponent comp = tip.getComponent();
	if (comp == null) {
	    return "";
	}
	javax.swing.KeyStroke[] keys =comp.getRegisteredKeyStrokes();
	String controlKeyStr = "";
	javax.swing.KeyStroke postTip = javax.swing.KeyStroke.getKeyStroke
	    (java.awt.event.KeyEvent.VK_F1,java.awt.Event.CTRL_MASK);

	for (int i = 0; i < keys.length; i++) {
	    if (postTip.equals(keys[i])) {
		// ignore, associated with ToolTipManager postTip action, in
		// swing1.1beta3 and onward
		continue;
	    }
	    char c = (char)keys[i].getKeyCode();
	    int mod = keys[i].getModifiers();
	    if ( mod == java.awt.event.InputEvent.CTRL_MASK ) {
		controlKeyStr = "Ctrl+"+(char)keys[i].getKeyCode();
		break;
	    } else if (mod == java.awt.event.InputEvent.ALT_MASK) {
		controlKeyStr = "Alt+"+(char)keys[i].getKeyCode();
		break;
	    } 
	}
	return controlKeyStr;
    }  

    private static String LINE_SEPARATOR = " ";
    private static int LINE_SEPARATOR_LEN = LINE_SEPARATOR.length();
                                                                                                                                                                                                       
    /**
     * StringTokenizer(text,"\n") really does a "\n+" which is not what we want.
     * We also want it be based on the line.separator property.  So create our
     * own version.  We first attempt to divide by line.separator, then by "\n".
     * Ideally, we'd prefer to just break up by line.separator, but we need to
     * handle text that was defined in a properties file, with embedded \n.
     */
    public static String[] breakupLines(String text) {
	int len = text.length();
	if (len == 0) {
	    return new String[] {""};
	} else {
	    java.util.Vector data = new java.util.Vector(10);
	    int start=0;
	    int i=0;
	    while (i<len) {
		if (text.startsWith(LINE_SEPARATOR,i) && 
		    i - start > TIP_LINE_LENGTH) {
		    data.addElement(text.substring(start,i));
		    start=i+LINE_SEPARATOR_LEN;
		    i=start;
		} else if (text.charAt(i) == '\n') {
		    data.addElement(text.substring(start,i));
		    start=i+1;
		    i=start;
		} else {
		    i++;
		}
	    }
	    if (start != len) {
		data.addElement(text.substring(start));
	    }
	    int numlines = data.size();
	    String lines[] = new String[numlines];
	    data.copyInto(lines);
	    return lines;
	}
    }
}

