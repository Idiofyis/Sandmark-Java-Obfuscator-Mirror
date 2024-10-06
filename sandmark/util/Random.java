package sandmark.util;

public class Random extends java.util.Random {
    private static Random sRand = new Random();
    private Random() {
	super(0);
    }
    public static Random getRandom() {
        return sRand;
    }
}
