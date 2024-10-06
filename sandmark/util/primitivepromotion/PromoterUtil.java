package sandmark.util.primitivepromotion;



/**
 * Useful routines for the promoter classes 
 * 
 * @author Srinivas Visvanathan
 *
 */
public class PromoterUtil
{
    /**
     * Returns the wrapper type for a given primitive type. Returns null if
     * primitiveType is not a primitive type.
     */
    public static org.apache.bcel.generic.Type
	getWrapperType(org.apache.bcel.generic.Type primitiveType)
    {
	switch (primitiveType.getType()) {
	    case org.apache.bcel.Constants.T_INT:
		return new org.apache.bcel.generic.ObjectType("java.lang.Integer");
		
	    case org.apache.bcel.Constants.T_BOOLEAN:
		return new org.apache.bcel.generic.ObjectType("java.lang.Boolean");
		
	    case org.apache.bcel.Constants.T_BYTE:
		return new org.apache.bcel.generic.ObjectType("java.lang.Byte");
		
	    case org.apache.bcel.Constants.T_FLOAT:
		return new org.apache.bcel.generic.ObjectType("java.lang.Float");
		
	    case org.apache.bcel.Constants.T_CHAR:
		return new org.apache.bcel.generic.ObjectType("java.lang.Character");
		
	    case org.apache.bcel.Constants.T_SHORT:
		return new org.apache.bcel.generic.ObjectType("java.lang.Short");
		
	    case org.apache.bcel.Constants.T_LONG:
		return new org.apache.bcel.generic.ObjectType("java.lang.Long");
		
	    case org.apache.bcel.Constants.T_DOUBLE:
		return new org.apache.bcel.generic.ObjectType("java.lang.Double");
	};

	return null;
    }


    /**
     * Returns the name of the method that can be used to get the primitive
     * value from its wrapper type. Returns null if primitiveType is not a
     * primitive type
     */
    public static String getValueMethodName(org.apache.bcel.generic.Type primitiveType)
    {
	switch (primitiveType.getType()) {
	    case org.apache.bcel.Constants.T_INT:
		return new String("intValue");
		    
	    case org.apache.bcel.Constants.T_BOOLEAN:
		return new String("booleanValue");
		
	    case org.apache.bcel.Constants.T_BYTE:
		return new String("byteValue");

	    case org.apache.bcel.Constants.T_FLOAT:
		return new String("floatValue");
		
	    case org.apache.bcel.Constants.T_CHAR:
		return new String("charValue");
		
	    case org.apache.bcel.Constants.T_SHORT:
		return new String("shortValue");
		
	    case org.apache.bcel.Constants.T_LONG:
		return new String("longValue");
		
	    case org.apache.bcel.Constants.T_DOUBLE:
		return new String("doubleValue");
	};

	return null;
    }
}
