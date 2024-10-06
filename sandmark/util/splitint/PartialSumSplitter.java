package sandmark.util.splitint;

public class PartialSumSplitter extends ValueSplitter {
    public PartialSumSplitter(java.util.Random rnd) {
       // We don't use randomness.
       this();
    }
   
    public PartialSumSplitter() {

    }

    public java.math.BigInteger[] split(java.math.BigInteger value, 
					int nparts) {
	if (nparts < 2)
	    throw new IllegalArgumentException("minimum two parts");
	if (value.compareTo(java.math.BigInteger.ZERO) < 0)
	    throw new IllegalArgumentException("cannot encode " + 
					       "negative values");

	int totalBits = value.bitLength();
	if (totalBits == 0)
	    totalBits++;
	java.math.BigInteger [] parts = new java.math.BigInteger[nparts];
	int partBits = totalBits / (nparts-1);
	if (totalBits % (nparts-1) != 0)
	    partBits++;
	int shift = 0;
	for (int i = 1; i < nparts; i++) {
	    parts[i] = value.shiftRight(shift).and(mask(partBits));
	    shift += partBits;
	}

	java.math.BigInteger sum = 
	    java.math.BigInteger.valueOf(partBits-1);
	parts[0] = sum;
	for (int i = 1; i < nparts; i++) {
	    parts[i] = sum = sum.add(parts[i]);
	}

	return parts;
    }

    public java.math.BigInteger combine(java.math.BigInteger [] parts) {
	if(parts.length < 2)
	    throw new IllegalArgumentException("minimum two parts");
	java.util.Arrays.sort(parts);
	int partBits = parts[0].intValue()+1;

	if (partBits < 1)
	    throw new IllegalArgumentException("negative number " + 
					       "of bits per part");
	if (parts[1].compareTo(java.math.BigInteger.ZERO) < 0)
	    throw new IllegalArgumentException("negative parts");

	java.math.BigInteger value = java.math.BigInteger.ZERO;
	for (int i = 1; i < parts.length; i++) {
	    java.math.BigInteger part = parts[i].subtract(parts[i-1]);
	    value = value.add(part.shiftLeft((i-1)*partBits));
	}

	return value;
    }

    public boolean orderMatters() {
	return false;
    }

    private static java.math.BigInteger mask(int bits) {
	java.math.BigInteger tmp = java.math.BigInteger.ONE.shiftLeft(bits);
	return tmp.subtract(java.math.BigInteger.ONE);
    }

    public static void main(String [] argv) {
	ValueSplitter s = new PartialSumSplitter();

	for (int n = 0; n < 1024; n++) {
	    java.math.BigInteger value = 
		java.math.BigInteger.valueOf(n);
	    java.math.BigInteger [] parts = null;
	    for (int nparts = 2; nparts <= value.bitLength()+2; nparts++) {
		parts = s.split(value, nparts++);
		java.util.Arrays.sort(parts);
		System.out.print("n = " + n + ", parts =");
		for (int i = 0; i < parts.length; i++)
		    System.out.print(" " + parts[i]);
		System.out.println();
		if (!value.equals(s.combine(parts))) {
		    System.err.println("uhoh!  n = " + n);
		    System.exit(-1);
		}
	    }
	}
    }
}

