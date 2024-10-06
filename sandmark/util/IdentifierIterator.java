package sandmark.util;

/**
   IdentifierIterator provides a straightforward interface for generating
   new java identifiers for methods, classes, etc.  Identifiers are returned
   in lexicographic order (basically).

   @author Kelly Heffner
 */
public class IdentifierIterator implements java.util.Iterator
{
    private static final char MAX_VALUE = 'z';
    private static final char MIN_VALUE = '$';

    private String namePrefix = "";
    private char nameSuffix = MIN_VALUE;

    /**
       Checks to see if there are more identifiers available.  This will always
       return true.
       @return true
    */
    public boolean hasNext()
    {
	return true;
    }
    
    /**
       Not implemented.
    */
    public void remove()
    {
	throw new UnsupportedOperationException
	    ("It is impossible to remove from this iterator!");
    }
    
    /**
       Returns the next unique identifier.
       @return an identifier that is unique from any other identifier
       returned from this instance of the iterator so far
    */
    public Object next()
    {
	String retVal = namePrefix + nameSuffix;
	
	if(namePrefix.length() == 0){
	    
	    do{
		nameSuffix += 1;
	    }while( !Character.isJavaIdentifierStart(nameSuffix) &&
		    nameSuffix <= MAX_VALUE);
	}
	else{
	    do{
		nameSuffix += 1;
	    }while(	!Character.isJavaIdentifierPart(nameSuffix) &&
			nameSuffix <= MAX_VALUE);
	    
	}

	//now move to the next letter
	if(nameSuffix > MAX_VALUE){
	    if(namePrefix.length() == 0)
		namePrefix = "" + MIN_VALUE;
	    else{
		char lastChar = namePrefix.charAt(namePrefix.length()-1);
		do{
		    lastChar += 1;
		}while(!Character.isJavaIdentifierPart(lastChar) &&
		       lastChar <= MAX_VALUE);
		if(lastChar >= MAX_VALUE)
		    namePrefix += MIN_VALUE;
		else
		    namePrefix = namePrefix.substring(0,
						      namePrefix.length()-1) + lastChar;
		
	    }
	    nameSuffix = MIN_VALUE;
	}
	return retVal;
    }
    
    /**
       Constructs a new identifier iterator.  Reset an IdentifierIterator by
       reconstructing it.
    */
    public IdentifierIterator()
    {
	namePrefix = "";
	nameSuffix = MIN_VALUE;
    }


}

