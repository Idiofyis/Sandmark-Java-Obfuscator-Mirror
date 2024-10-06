package sandmark.watermark.steganography;

/*
 * Class loader that is installed in the watermarked application by
 * Steganography.
 */

class StegLoader extends java.lang.ClassLoader {

    private static String clsName;	//name of hidden class
    private static String mainClsName;	//name of main class
    private static String imgName;	//name of image in which class was hidden

    private static final int HDR_SIZE = 4;
    
     
    //the code for <clinit> will be replaced by Steganography with valid
    //initializations
    static {
	clsName = null;
	mainClsName = null;
	imgName = null;
    } 
    

    public java.lang.Class findClass(String name) 
	throws java.lang.ClassNotFoundException 
    {
	System.out.println("findClass: clsName: " + clsName + "; name = " + name);
	byte[] b;
	if (clsName.equals(name))
	    b = loadClassFromImage(name);
	else
	    b = loadClassFromCls(name);
	if (b == null)
	    throw new ClassNotFoundException(name);
	return defineClass(name,b,0,b.length);
    }

    private byte[] loadClassFromCls(String name)
	throws java.lang.ClassNotFoundException {
	System.out.println("loading class from: " + name.replace('.','/') + ".cls");
	java.io.InputStream in = getResourceAsStream(name.replace('.','/') + ".cls");
	if (in == null)
	    throw new ClassNotFoundException(name);
	java.io.ByteArrayOutputStream baos =
	    new java.io.ByteArrayOutputStream();
	try {
	    for (int i = in.read(); i != -1; i = in.read())
		baos.write(i);

	} catch (java.io.IOException e) {
	    throw new ClassNotFoundException(name);
	}
	return baos.toByteArray();
    }

    private byte[] loadClassFromImage(String name) 
	throws java.lang.ClassNotFoundException
    {
	System.out.println("loading class from: " + imgName);
	//read in png
	java.awt.image.BufferedImage bi;
	try {
	    bi = javax.imageio.ImageIO.read(
		    ClassLoader.getSystemResourceAsStream(imgName));
	} catch (Exception e) {
	    throw new ClassNotFoundException(name);
	}

	//extract hidden payload if any
	byte[] payload = recover(bi);
	if (payload == null)
	    return null;
	
	//extract class length
	int clsLen = (payload[0] & 0xFF)
		| (payload[1] & 0xFF) << 8
		| (payload[2] & 0xFF) << 16
		| (payload[3] & 0xFF) << 24;
	if (clsLen < 0 || clsLen + 2 *HDR_SIZE > payload.length)
	    return null;
	
	//extract class data
	byte clsData[] = new byte[clsLen];
	for (int i = 0; i < clsLen; i++)
	    clsData[i] = payload[HDR_SIZE + i];

	return clsData;
    }
    

    public static byte[] recover(java.awt.image.BufferedImage bi) {
	int W = bi.getWidth(),H = bi.getHeight();
	
	//get RGBA info
	int pixels[] = new int[W * H];
	bi.getRGB(0,0,W,H,pixels,0,W);

	//read payload size field and check if its valid
	int size = (recoverByte(pixels[0]) & 0xFF)
		| (recoverByte(pixels[1]) & 0xFF) << 8
		| (recoverByte(pixels[2]) & 0xFF) << 16
		| (recoverByte(pixels[3]) & 0xFF) << 24;
	if (size > W * H - HDR_SIZE || size < 0)
	    return null;

	//read out hidden stuff
	byte payload[] = new byte[size];
	for (int i = 0; i < size; i++)
	    payload[i] = recoverByte(pixels[HDR_SIZE + i]);

	return payload;
    }


    private static byte recoverByte(int argb) {
	byte result = 0;
	result |= (byte)(argb & 0x00000003);
	result |= (byte)((argb & 0x00000300) >> 6);
	result |= (byte)((argb & 0x00030000) >> 12);
	result |= (byte)((argb & 0x03000000) >> 18);
	return result;
    }


    public static void main(String args[]) throws Exception {

	System.out.println("in steg loader main");
	System.out.println("mainClsName = " + mainClsName);
	System.out.println("clsName = " + clsName);
	System.out.println("imgName = " + imgName);
	if (mainClsName == null) {
	    System.err.println("This jar is not runnable.");
	    System.exit(1);
	}

	StegLoader sl = new StegLoader();
	java.lang.Class cls = sl.loadClass(mainClsName,true);
	System.out.println("loaded class containing main: " + mainClsName);
	java.lang.reflect.Method meth = 
	    cls.getMethod("main",new Class[] { (new String[0]).getClass() });
	System.out.println("about to invoke main");
	meth.invoke(null,new Object[] { args });
    }
    
}
