package sandmark.util.splitint;

public class AdditiveSplitter extends ValueSplitter {
    private java.util.Random mRnd;
    public AdditiveSplitter(java.util.Random rnd) { mRnd = rnd; }
    public boolean orderMatters() { return false; }
    public java.math.BigInteger[] split(java.math.BigInteger value,
					int nparts) {
	String b10rep = value.toString();
	boolean isNeg = b10rep.startsWith("-");
	if(isNeg) {
	    b10rep = b10rep.substring(1);
	}
	String partStrs[] = new String[nparts];
	{
	    int lengths[] = 
		ValueSplitter.getRandomIntsWithSum
		(b10rep.length(),
		 b10rep.length() < nparts ? b10rep.length() : nparts,mRnd);
	    String partStrsTmp[] = 
		ValueSplitter.getSubstringsWithLengths(b10rep,lengths);
	    int i = 0;
	    for(; i < partStrs.length - partStrsTmp.length ; i++)
		partStrs[i] = "0";
	    for(int j = i ; j < partStrs.length ; j++)
		partStrs[j] = partStrsTmp[j - i];
	}
	for(int i = nparts - 2 ; i >= 0 ; i--) {
	    partStrs[i] += ValueSplitter.getZeroString
		(partStrs[i + 1].length());
	}
	for(int i = 0 ; i < nparts ; i++) {
	    if(isNeg) {
		partStrs[i] = "-" + partStrs[i];
	    }
	}
	java.math.BigInteger parts[] = new java.math.BigInteger[nparts];
	for(int i = 0 ; i < nparts ; i++) {
	    parts[i] = new java.math.BigInteger(partStrs[i]);
	}
	return parts;
    }
    public java.math.BigInteger combine(java.math.BigInteger[] parts) 
	throws IllegalArgumentException {
	java.math.BigInteger sum = java.math.BigInteger.ZERO;
	for(int i = 0 ; i < parts.length ; i++) {
	    sum = sum.add(parts[i]);
	}
	return sum;
    }
    public static void main(String argv[]) {
	int count = Integer.decode(argv[0]).intValue();
	AdditiveSplitter as = new AdditiveSplitter(new java.util.Random());
	ValueSplitter.testSplitter(as,count,false);
    }
}
    

