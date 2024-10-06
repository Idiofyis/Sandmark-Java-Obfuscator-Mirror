package sandmark.util;

/**
 *  The sandmark.util.StringInt class encodes strings as BigIntegers.
 *  Strings having the form of non-negative integers are encoded with
 *  minimal overhead.
 */

public class StringInt {



/**
 *  Encodes a string to produce a BigInteger.
 */
public static java.math.BigInteger encode(String s) {
    byte[] b1 = s.getBytes();
    int len = b1.length;
    byte[] b2 = new byte[len + 1];
    System.arraycopy(b1, 0, b2, 1, b1.length);
    b2[0] = (byte) (2 - (b2[len] & 1));
    b2[len] |= 1;
    return new java.math.BigInteger(b2);
}



/**
 *  Decodes a BigInteger to produce the corresponding string.
 */
public static String decode(java.math.BigInteger n) {
    byte[] b = n.toByteArray();
    int len = b.length - 1;
    b[len] = (byte) ((b[len] & 0xFE) | (b[0] & 1));
    return new String(b, 1, len);
}



//------------------------------------------------------------

public static void main(String args[]) {
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
    System.out.println("++++++++++++ Testing util.StringInt +++++++++++++++");
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
    String[] data = {
	"0", "1", "2", "3", "4", "21701", "6972593", "601709000061803",
	"31415926535897932384626433832795028841971693993751",
	"x", "y", "gmt", "VHD 755", "078-05-1120", "splendiferous", 
	"", "\0", "\0\0\0", "\0a", "b\0", "-", " ", "-0", "-1", "00",
	"While I nodded, nearly napping, suddenly there came a tapping",
    };
   
    for (int i = 0; i < data.length; i++) {
	String s1 = data[i];
	java.math.BigInteger n = encode(s1);
	String s2 = decode(n);
	System.out.println(s1.equals(s2) ? "okay:" : "ERROR:");
	System.out.println("   s1 = " + s1.replace('\0', '%'));
	System.out.println("   s2 = " + s2.replace('\0', '%'));
	System.out.println("   n  = " + n.toString());
    }
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
}



} // class StringInt

