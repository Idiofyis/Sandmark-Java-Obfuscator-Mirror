package sandmark.watermark.steganography;


/**
 * Utility class that can be used to hide arbitrary data in images. Data hiding
 * is achieved by storing 2 bits of data in the lower 2 significant bits of the
 * alpha, red, green and blue channels of pixels. The class operates on the
 * generic BufferedImage (@see java.awt.image.BufferedImage) image object that's
 * provided as part of the standard Java library. However Steganography saves
 * and loads images from PNG files only since (a) its one of the formats supported
 * by the standard java library (others are JPEG and GIF; as of j2se 1.4) 
 * (b) PNG supports lossless compression (unlike JPEG, which is typically lossy)
 * (c) PNG does not impose any restrictions on the nature of the image (GIF
 * supports at most 256 colors and any image with more colors would be reduced
 * to 256 colors while saving). PNG also faithfully stores transparency (alpha)
 * info.
 */


public class ImageHider {

    private static final int HDR_SIZE = 4;

    /**
     * Tries to hide the given data bytes in the given image.
     *
     * @param bi image to hide data in
     * @param payload array of bytes to hide
     *
     * @return If the image has enough capacity, a new image with the data
     * hidden in it is returned. If there wasn't enough space, null is returned.
     * 
     */
    public static java.awt.image.BufferedImage
	hide(java.awt.image.BufferedImage bi,byte payload[])
    {
	int W = bi.getWidth(),H = bi.getHeight();
	
	//not enough space
	if (payload.length > W * H - HDR_SIZE)
	    return null;
	
	//get RGBA info
	int pixels[] = new int[W * H];
	bi.getRGB(0,0,W,H,pixels,0,W);

	//encode payload length
	pixels[0] = hideByte(pixels[0],(byte)(payload.length & 0x000000FF));
	pixels[1] = hideByte(pixels[1],(byte)((payload.length & 0x0000FF00) >> 8));
	pixels[2] = hideByte(pixels[2],(byte)((payload.length & 0x00FF0000) >> 16));
	pixels[3] = hideByte(pixels[3],(byte)((payload.length & 0xFF000000) >> 24));
	for (int i = 0; i < payload.length; i++) 
	    pixels[HDR_SIZE + i] = hideByte(pixels[HDR_SIZE + i],payload[i]);

	//construct result image
	java.awt.image.BufferedImage result =
	    new java.awt.image.BufferedImage(
		    W,H,java.awt.image.BufferedImage.TYPE_INT_ARGB);
	result.setRGB(0,0,W,H,pixels,0,W);
	return result;
    }

    /**
     * Tries to extract any data that may be hidden in the given image.
     * 
     * @param bi Image to recover data from
     * 
     * @return a byte array if a valid data payload was found in the image or
     *	    null if no valid data payload was found.
     */
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


    /* argb - ARGB value of a single pixel, 8 bits used per channel
     * b - byte to hide
     * 2 bits are hidden in each channel of the pixel by replacing the least
     * significant 2 bits with 2 bits of data
     * Returns the pixel with the data hidden in it
     */
    private static int hideByte(int argb,byte b) {
	argb = argb & 0xFFFFFFFC | (b & 0x03);
	argb = argb & 0xFFFFFCFF | (b & 0x0C) << 6;
	argb = argb & 0xFFFCFFFF | (b & 0x30) << 12;
	argb = argb & 0xFCFFFFFF | (b & 0xC0) << 18;
	return argb;
    }

    /* Extracts the byte hidden in a pixel by reading out the 2 bits hidden in
     * the ARGB channels
     */
    private static byte recoverByte(int argb) {
	byte result = 0;
	result |= (byte)(argb & 0x00000003);
	result |= (byte)((argb & 0x00000300) >> 6);
	result |= (byte)((argb & 0x00030000) >> 12);
	result |= (byte)((argb & 0x03000000) >> 18);
	return result;
    }

    /**
     * Returns the number of bytes of data that can be hidden in a given image.
     *
     * @param bi image
     * 
     * @return no. of bytes of data that can be hidden in bi 
     */
    public static int getCapacity(java.awt.image.BufferedImage bi) {
	return bi.getWidth() * bi.getHeight() - HDR_SIZE;
    }
}
