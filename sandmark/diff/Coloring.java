package sandmark.diff;

/** A class to store line numbers associated with "colors." 
 *  @author Zach Heidepriem
 */

public class Coloring {
    private java.util.Vector v;
    public static final int DEFAULT = 0;
    private String title;
    
    /** Construct a coloring
     *  @param size the number of lines this Coloring object will hold
     *  @param s the title of this Coloring object
     */
    public Coloring(int size, String s){;
	v = new java.util.Vector(size);
        title = s;
    }   

    /** @return the title of this object
     */
    public String getTitle(){
        return title;
    } 

   /** Add a line with default color to this coloring
    *  @param s the String to add
    */
    public void add(String s){
        add(s, DEFAULT);
    }
   
    /** Add a line to the end of this coloring
     *  @param s the String to add
     *  @param color the color associated with line
     */
    public void add(String s, int color){
	v.add(new Line(s, color));
    }

    /** Add a line to this coloring
     *  @param line the line number
     *  @param s the String to add
     *  @param color the color associated with line
     */
    public void add(int line, String s, int color){
	v.add(line, new Line(s, color));
    }

    /** Adds a line with default color 
     *  @param line the line number
     *  @param s the String to add
     */
    public void add(int line, String s){
	add(line, s, DEFAULT);
    }

   
    /** @param line the line number to get
     *  @return the color associated with line
     */ 
    public int getColor(int line){
	////System.out.println(line + " " + v.size());
	if(line >= v.size()){
	    //System.out.println("bad index, using default color");
	    return 0;
	}
	return ((Line)v.get(line)).getColor();	
    }
    
    /** @param line the line number to get
     *  @return the String associated with line
     */ 
    public String get(int line){
	return ((Line)v.get(line)).getString();
    }       

    /** @return a String representation of this Coloring
     */ 
    public String toString(){
	String s = "";
	for(int i = 0; i < v.size(); i++){
	    int color = ((Integer)v.get(i)).intValue();
	    s += i + ": " + color + "\n";
	}
	return s;
    }

    /** @return the capacity of this Coloring
     */  
    public int size(){ return v.size(); }

    private class Line {
	private String s;
	private int color;
	public Line(String str, int c){
	    s = str;
	    color = c;
	}
	public int getColor(){ return color; }
	public String getString(){ return s; }
    }
}

