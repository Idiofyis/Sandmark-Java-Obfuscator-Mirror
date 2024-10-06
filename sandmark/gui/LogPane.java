/*
 * Created on Mar 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.gui;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

public class LogPane extends javax.swing.JScrollPane {
   private static int MAX_BUFFER_LENGTH = 10000;
	private static class LogWriter extends java.io.Writer {
		private javax.swing.JTextArea mArea;
		LogWriter(javax.swing.JTextArea area) { 
			mArea = area; 
			sandmark.util.Log.addLog(this,0);
		}
	    public void write(String str) {
	       String text = mArea.getText() + str + "\n";
	       int length = text.length();
	       if(length > MAX_BUFFER_LENGTH)
	          text = text.substring(length - MAX_BUFFER_LENGTH);
	        mArea.setText(text);
	        mArea.setCaretPosition(text.length());
	    }
	    public void write(String str,int offset,int length) {
	       write(str.substring(offset,offset + length));
	    }
	    public void write(char buf[],int offset,int length) {
	       write(new String(buf,offset,length));
	    }
	    public void close() {}
	    public void flush() {}
	}
	public LogPane() { 
		javax.swing.JTextArea area = new javax.swing.JTextArea();
        area.setEditable(false);
        area.setRows(4);
        area.setColumns(60);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new java.awt.Insets(3,3,3,3));
        setViewportView(area);
        new LogWriter(area);
	}
}
