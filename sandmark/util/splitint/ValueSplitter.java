package sandmark.util.splitint;

public abstract class ValueSplitter {
    public abstract java.math.BigInteger[] split
	(java.math.BigInteger value,int nparts)
	throws IllegalArgumentException;
    public abstract java.math.BigInteger combine
	(java.math.BigInteger[] parts)
	throws IllegalArgumentException;
    public abstract boolean orderMatters();
    public static int[] getRandomIntsWithSum(int sum,int count) {
        return getRandomIntsWithSum(sum,count,new java.util.Random());
    }
    public static int[] getRandomIntsWithSum(int sum,int count,java.util.Random rnd) {
	if(sum < count)
	    throw new RuntimeException("Can't split a string of " +
				       "length " + sum + " into " +
				       count + " parts");
	int ints[] = new int[count];
	for(int i = 0 ; i < count ; i++) {
	    int max = sum - count + i + 1;
	    ints[i] = 1 + (((rnd.nextInt() % max) + max) % max);
	    sum -= ints[i];
	}
	ints[((rnd.nextInt() % count) + count) % count] +=
	    sum;
	return ints;
    }
    public static String[] getSubstringsWithLengths(String str,
						    int[] lengths) {
	String partStrs[] = new String[lengths.length];
	for(int i = 0 ; i < lengths.length ; i++) {
	    partStrs[i] = str.substring(0,lengths[i]);
	    str = str.substring(lengths[i]);
	}
	return partStrs;
    }
    public static String getZeroString(int nzeros) {
	String zeros = "";
	for(int i = 0 ; i < nzeros ; i++) {
	    zeros += "0";
	}
	return zeros;
    }
    public static void testSplitter(ValueSplitter vs,int reps,
				    boolean debug) {
	java.util.Random rnd = sandmark.util.Random.getRandom(); //new java.util.Random();
	for(int i = 0 ; i < reps ; i++) {
	    int nparts = 0;
	    while(nparts == 0)
		nparts = ((rnd.nextInt() % 10) + 10) % 10;
	    long number = rnd.nextLong();
	    java.math.BigInteger bi = 
		java.math.BigInteger.valueOf(number);
	    java.math.BigInteger parts[] = vs.split(bi,nparts);
	    java.math.BigInteger combined = vs.combine(parts);
	    if(!bi.equals(combined) || debug) {
		System.out.println("splitting " + bi + " into " + nparts +
				   " parts gives:");
		for(int j = 0 ; j < parts.length ; j++) {
		    System.out.println("|" + parts[j] + "|");
		}
		System.out.println("which recombine to " + combined);
		if(!bi.equals(combined))
		   System.out.println("which is WRONG");
		System.out.println();
	    } else {
		System.out.println(bi + " with " + nparts + " ok");
	    }
	}
    }
}

