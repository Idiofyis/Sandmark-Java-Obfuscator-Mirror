package sandmark.util.splitint;

public class ScaledSplitter extends ValueSplitter {
    public static int EXPONENT_DIGITS = 4;
    public static int LENGTH_DIGITS = 4;
    public boolean orderMatters() { return false; }
    public java.math.BigInteger[] split
	(java.math.BigInteger value,int nparts) {
	String b10rep = value.toString();
	boolean isNeg = b10rep.startsWith("-");
	if(isNeg) {
	    b10rep = b10rep.substring(1);
	}
	int lengths[] = 
	    ValueSplitter.getRandomIntsWithSum
	    (b10rep.length(),nparts);
	String partStrs[] = 
	    ValueSplitter.getSubstringsWithLengths
	    (b10rep,lengths);
	for(int i = 0 ; i < nparts ; i++) {
	    String lengthStr = (new Integer
				(partStrs[i].length())).toString();
	    int missingDigits = EXPONENT_DIGITS - 
		lengthStr.length();
	    if(missingDigits > 0) {
		lengthStr = ValueSplitter.getZeroString
		    (missingDigits) + lengthStr;
	    }
	    partStrs[i] += lengthStr;
	}
	for(int i = 0 ; i < nparts ; i++) {
	    String indexStr = (new Integer(i)).toString();
	    int missingDigits = EXPONENT_DIGITS - 
		indexStr.length();
	    if(missingDigits > 0) {
		indexStr = ValueSplitter.getZeroString
		    (missingDigits) + indexStr;
	    }
	    partStrs[i] += indexStr;
	}
	if(isNeg)
	    partStrs[0] = "-" + partStrs[0];
	java.math.BigInteger parts[] = new java.math.BigInteger[nparts];
	for(int i = 0 ; i < nparts ; i++) {
	    parts[i] = new java.math.BigInteger(partStrs[i]);
	}
	return parts;
    }
    public java.math.BigInteger combine(java.math.BigInteger[] parts) 
	throws IllegalArgumentException {
	String b10reps[] = new String[parts.length];
	for(int i = 0 ; i < parts.length ; i++) {
	    b10reps[i] = parts[i].toString();
	    if(b10reps[i].length() < 1 + EXPONENT_DIGITS +
	       LENGTH_DIGITS)
		b10reps[i] = ValueSplitter.getZeroString
		    (1 + EXPONENT_DIGITS + LENGTH_DIGITS -
		     b10reps[i].length()) + b10reps[i];
	}
	int ndxs[] = new int[parts.length];
	int lengths[] = new int[parts.length];
	for(int i = 0 ; i < parts.length ; i++) {
	    String tmp = b10reps[i].substring
		(b10reps[i].length() - 4);
	    while(tmp.startsWith("0") && tmp.length() > 1)
		tmp = tmp.substring(1);
	    ndxs[i] = Integer.decode(tmp).intValue();
	    b10reps[i] = 
		b10reps[i].substring(0,b10reps[i].length() - 4);
	    tmp = b10reps[i].substring(b10reps[i].length() - 4);
	    while(tmp.startsWith("0") && tmp.length() > 1)
		tmp = tmp.substring(1);
	    lengths[i] = Integer.decode(tmp).intValue();
	    b10reps[i] =
		b10reps[i].substring(0,b10reps[i].length() - 4);
	}
	for(int i = 0 ; i < parts.length ; i++)
	    if(b10reps[i].length() < lengths[i])
		b10reps[i] = ValueSplitter.getZeroString
		    (lengths[i] - b10reps[i].length()) +
		    b10reps[i];
	String orderedPartStrs[] = new String[parts.length];
	for(int i = 0 ; i < parts.length ; i++)
	    orderedPartStrs[ndxs[i]] = b10reps[i];
	String combined = "";
	for(int i = 0 ; i < parts.length ; i++)
	    combined += b10reps[i];
	return new java.math.BigInteger(combined);
    }
    public static void main(String argv[]) {
	int count = Integer.decode(argv[0]).intValue();
	ScaledSplitter ss = new ScaledSplitter();
	ValueSplitter.testSplitter(ss,count,false);
    }
}

