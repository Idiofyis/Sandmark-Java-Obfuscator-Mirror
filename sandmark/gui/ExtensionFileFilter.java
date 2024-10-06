package sandmark.gui;

/*
	Special file filter used in JFileDialog boxes.
	It filters out all files which are not directories or 
	do not end in ("." + ext) as defined by the constructor.
*/
public class ExtensionFileFilter extends javax.swing.filechooser.FileFilter{
	private String extension;
	private String description;
	
	// Constructor:	"ext" is the extension, without the '.' in it.
	//				"des" is the description of the type of files 
	//				that the extension describes
	public ExtensionFileFilter(String ext, String des){
		extension = ext;
		description = des;
	}
	
	// method used by JFileDialogs to see if a file should be displayed
	public boolean accept(java.io.File f){
		String name = f.getName();
		return (name.endsWith('.'+extension) || f.isDirectory());
	}
	
	// accessor method
	public String getDescription(){
		return description;
	}
}

