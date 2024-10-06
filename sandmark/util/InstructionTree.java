package sandmark.util;

public class InstructionTree
{
    private class BTN
    {
	public BTN left;
	public BTN right;
	public Comparable data;
	public int numAccess;
	
	public BTN( Comparable data )
	{
	    this.left = null;
	    this.right = null;
	    this.data = data;
	    numAccess = 1;
	}
	
	public BTN( Comparable data, BTN left, BTN right )
	{
	    this.left = left;
	    this.right = right;
	    this.data = data;
	    numAccess = 1;
	}
    }
    
    private BTN root;
    
    public InstructionTree()
    {
	root = null;
    }
    
    public void add( Comparable data )
    {
	root = this.add(root, data);
    }
    
    private BTN add( BTN curr, Comparable newElem )
    {
	if(curr == null)
	    curr = new BTN(newElem);
	else if(newElem.compareTo(curr.data) < 0)
	    curr.left = this.add(curr.left, newElem);
	else if(newElem.compareTo(curr.data) > 0)
	    curr.right = this.add(curr.right, newElem);
	else
	    curr.numAccess++;
	return curr;
    }
    
    public String toString()
    {
	return this.buildString( root );
    }
    
    //This method is the helper method that recursively builds the
    //String representation of the Thesaurus.
    private String buildString( BTN curr )
    {
	String result = "";
	
	if( curr != null )
	{
	    result += buildString(  curr.left );
	    String time = (curr.numAccess == 1)? "time":"times";
	    result += lineUp(curr.data + " : " + curr.numAccess + " " + time + ".\n");
	    result += buildString( curr.right );
	}
	return result;
    }

    private String lineUp(String x)
    {
	String result = "";
	for(int i = 0 ; i < 30 - x.indexOf(':'); i++)
	    result += " ";
	return result + x;
    }
    
    public static void main(String[] args)
    {
	InstructionTree it = new InstructionTree();
	it.add("C");
	it.add("S");
	it.add("A");
	it.add("V");
	it.add("B");
	it.add("S");
	it.add("S");
	System.out.println(it);
    }
}

