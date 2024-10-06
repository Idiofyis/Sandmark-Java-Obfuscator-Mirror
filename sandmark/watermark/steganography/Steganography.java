package sandmark.watermark.steganography;



/**
 * Given an application and a (watermark,key) pair, Steganography hides
 * (watermark,key) in a PNG image of the application. To prevent the image from
 * being removed, byte codes from some class of the application are also hidden
 * in the image. A class loader is installed in the application to load hidden
 * class from the image when the application is started.
 *
 * @author Srinivas Visvanathan
 *
 */


public class Steganography extends sandmark.watermark.StaticWatermarker
{
    private sandmark.program.Application app;
    private byte[] wmark;
    private long key;
    private String clsName;
    private String imgName;
    private String mainClsName;
    

    public void embed(sandmark.watermark.StaticEmbedParameters params)
	throws sandmark.watermark.WatermarkingException
    {
	this.app = params.app;

	//get watermark bytes
	String wmarkStr = params.watermark;
	if (wmarkStr == null)
	    throw new sandmark.watermark.WatermarkingException(
		    "No watermark specified");
	this.wmark = wmarkStr.getBytes();

	//get key
	String keyStr = params.key;
	if (keyStr == null)
	    throw new sandmark.watermark.WatermarkingException(
		    "No key specified");
	this.key = sandmark.util.StringInt.encode(keyStr).longValue();
	keyXOR(wmark,key);
	
	//select smallest class as class to hide
	clsName = selectSmallestClass(app);
	
	//use PNG with largest capacity for hiding
	imgName = selectLargestPNG(app);

	//identify main class, if present
	sandmark.program.Class mainCls = app.getMain();
	if (mainCls != null)
	    mainClsName = mainCls.getName();

	//embed stuff
	doEmbedding();
    }

    
    
    //does the actual embedding
    private void doEmbedding() throws sandmark.watermark.WatermarkingException
    {
	//read in png
	java.awt.image.BufferedImage png;
	try {
	    png = javax.imageio.ImageIO.read(
			new java.io.ByteArrayInputStream(
			    app.getFile(imgName).getBytes()));
	} catch (java.io.IOException e) {
	    throw new sandmark.watermark.WatermarkingException(
		    "Error occurred while reading " + imgName);
	}
	
	//make and hide payload
	byte[] payload = constructPayload(png);
	png = ImageHider.hide(png,payload);
	if (png == null)
	    throw new sandmark.watermark.WatermarkingException(
		    "Unable to hide payload in image !!");

	//save png back
	java.io.ByteArrayOutputStream baos =
	    new java.io.ByteArrayOutputStream();
	try {
	    javax.imageio.ImageIO.write(png,"png",baos);
	} catch (java.io.IOException e) {
	    throw new sandmark.watermark.WatermarkingException(
		    "Unable to write png to jar file");
	}
	app.getFile(imgName).setData(baos.toByteArray());

	//install loader to extract hidden class on startup
	installLoader();

	//change class extensions to ".cls" in the jar
	renameClassFiles();
    }


    //renames *.class to *.cls. Cheap hack to ensure that our loader
    //(StegLoader) is used to load all the class files in the application
    private void renameClassFiles() {
	java.util.Iterator it = app.classes();
	while (it.hasNext()) {
	    sandmark.program.Class cls = (sandmark.program.Class)it.next();
	    if (cls.getName().equals("sandmark.watermark.steganography.StegLoader")) continue;
	    new sandmark.program.File(
		    app,cls.getName().replace('.','/') + ".cls",cls.getBytes());
	    cls.delete();
	}
    }


    private static final int HDR_SIZE = 4;
    
    private byte[] constructPayload(java.awt.image.BufferedImage bi)
		throws sandmark.watermark.WatermarkingException
    {
	sandmark.program.Class cf = app.getClass(clsName);
	byte clsData[] = cf.getBytes();
	
	//ensure there's enough space to hide everything
	if (clsData.length + wmark.length + 2 * HDR_SIZE >
		ImageHider.getCapacity(bi))
	    throw new sandmark.watermark.WatermarkingException(
		"Not enough space in image to hide class file and watermark");
	
	//construct payload
	byte[] payload = new byte[clsData.length + wmark.length + 2 * HDR_SIZE];

	//encode class length
	payload[0] = (byte)(clsData.length & 0x000000FF);
	payload[1] = (byte)((clsData.length & 0x0000FF00) >> 8);
	payload[2] = (byte)((clsData.length & 0x00FF0000) >> 16);
	payload[3] = (byte)((clsData.length & 0xFF000000) >> 24);

	//next comes class data
	for (int i = 0; i < clsData.length; i++)
	    payload[HDR_SIZE + i] = clsData[i];

	//encode watermark length
	int ofs = HDR_SIZE + clsData.length;
	payload[ofs++] = (byte)(wmark.length & 0x000000FF);
	payload[ofs++] = (byte)((wmark.length & 0x0000FF00) >> 8);
	payload[ofs++] = (byte)((wmark.length & 0x00FF0000) >> 16);
	payload[ofs++] = (byte)((wmark.length & 0xFF000000) >> 24);

	//TODO: XOR with key
	//next comes watermark
	for (int i = 0; i < wmark.length; i++)
	    payload[ofs++] = wmark[i];

	//remove the class being hidden
	cf.delete();
	return payload;
    }

    private static final String STEGLOADER_PATH =
	"/sandmark/watermark/steganography/StegLoader.class";

    //adds the class loader (available at STEGLOADER_PATH) to the application.
    private void installLoader() 
	throws sandmark.watermark.WatermarkingException {
	//get steg loader class	
	org.apache.bcel.classfile.JavaClass jc;
	try {
	    org.apache.bcel.classfile.ClassParser cp =
		new org.apache.bcel.classfile.ClassParser(
			getClass().getResourceAsStream(STEGLOADER_PATH),
			STEGLOADER_PATH);
	    jc = cp.parse();
	} catch (Exception e) {
	    throw new sandmark.watermark.WatermarkingException(
		    "Unable to access: " + STEGLOADER_PATH);
	}
	
	//fix static init code
	jc = fixStaticInit(jc);
	
	//add stegloader to the jar and set it as main class
	sandmark.program.LocalClass lc = 
	    new sandmark.program.LocalClass(app,jc);
	app.setMain(lc);
    }

    //add initializations of clsName, mainClsName and imgName in the static
    //initialization code of jc
    private org.apache.bcel.classfile.JavaClass
	fixStaticInit(org.apache.bcel.classfile.JavaClass jc) {

	org.apache.bcel.generic.ClassGen cg =
	    new org.apache.bcel.generic.ClassGen(jc);
	org.apache.bcel.generic.ConstantPoolGen cpg =
	    cg.getConstantPool();
	org.apache.bcel.classfile.Method meth =
	    cg.containsMethod("<clinit>","()V");
	org.apache.bcel.generic.MethodGen mg =
	    new org.apache.bcel.generic.MethodGen(
		    meth,cg.getClassName(),cpg);
	org.apache.bcel.generic.InstructionFactory iF =
	    new org.apache.bcel.generic.InstructionFactory(cg);
	org.apache.bcel.generic.InstructionList il = mg.getInstructionList();

	//delete all instructions
	il.dispose();
	
	//add initialization for clsName
	il.append(iF.createConstant(clsName));
	il.append(iF.createPutStatic(cg.getClassName(),"clsName",
		    org.apache.bcel.generic.Type.STRING));

	//add initialization for imgName
	il.append(iF.createConstant(imgName));
	il.append(iF.createPutStatic(cg.getClassName(),"imgName",
		    org.apache.bcel.generic.Type.STRING));

	//add initialization for mainClsName
	if (mainClsName != null) {
	    il.append(iF.createConstant(mainClsName));
	    il.append(iF.createPutStatic(cg.getClassName(),"mainClsName",
		    org.apache.bcel.generic.Type.STRING));
	}
	il.append(org.apache.bcel.generic.InstructionFactory.createReturn
	    (org.apache.bcel.generic.Type.VOID));

	mg.removeLineNumbers();
	mg.removeExceptions();
	mg.setMaxStack();
	cg.replaceMethod(meth,mg.getMethod());
	return cg.getJavaClass();
    }


    //looks through the files of the app and returns name of PNG with largest
    //capacity. Throws an exception of no PNGs are found
    private static String selectLargestPNG(sandmark.program.Application app) 
	throws sandmark.watermark.WatermarkingException
    {
	sandmark.program.File largestPNG = null;
	int largestSize = -1;
	
	java.util.Iterator it = app.files();
	while (it.hasNext()) {
	    sandmark.program.File f = (sandmark.program.File)it.next();
	    if (!f.getJarName().toLowerCase().endsWith(".png"))
		continue;

	    java.awt.image.BufferedImage bi = null;
	    try {
		bi = javax.imageio.ImageIO.read(
			new java.io.ByteArrayInputStream(
			    f.getBytes()));
	    } catch (java.io.IOException e) {
		//bad PNG, skip it
		continue;
	    }
	    
	    if (largestPNG == null) {
		largestPNG = f;
		largestSize = ImageHider.getCapacity(bi);
		continue;
	    }
	    int size = ImageHider.getCapacity(bi);
	    if (size > largestSize) {
		largestPNG = f;
		largestSize = size;
	    }
	}

	if (largestPNG == null)
	    throw new sandmark.watermark.WatermarkingException(
		    "The application must contain at least one valid PNG image");

	return largestPNG.getJarName();
    }


    //looks through the classes of the app and returns the name of the class
    //with smallest size. Throws an exception of no classes are found
    private static String selectSmallestClass(sandmark.program.Application app)
	throws sandmark.watermark.WatermarkingException
    {
	sandmark.program.Class smallestCls = null;
	int smallestSize = -1;

	java.util.Iterator it = app.classes();
	while (it.hasNext()) {
	    sandmark.program.Class cls = (sandmark.program.Class)it.next();
	    if (smallestCls == null) {
		smallestCls = cls;
		smallestSize = cls.getBytes().length;
		continue;
	    }
	    int size = cls.getBytes().length;
	    if (size < smallestSize) {
		smallestCls = cls;
		smallestSize = size;
	    }
	}
	
	if (smallestCls == null)
	    throw new sandmark.watermark.WatermarkingException(
		    "Application must contain at least one class.");

	return smallestCls.getName();
    }


    public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
	throws sandmark.watermark.WatermarkingException
    {
	return new StegIterator(params);
    }


    //XOR the bytes of buff with bytes of key
    private static void keyXOR(byte[] buff,long key) {
	byte xorMask[] = new byte[8];	
	long mask = 0xFF;
	for (int i = 0; i < 8; i++, mask <<= 8)
	    xorMask[i] = (byte)((key & mask) >> (i << 3));
	for (int i = 0, j = 0; i < buff.length; i++, j = (j + 1) % 8)
	    buff[i] ^= xorMask[j];
    }
    

    public String getAlgHTML() {
	return "<HTML><BODY> Steganograph is a static watermarking algorithm which" +
          " hides a watermark in PNG images of the application." +
          " To prevent the PNG from being removed/modified, a class of the" +
          " application is also hidden in the image." +
          "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href=\"mailto:srini@cs.arizona.edu\">Srinivas" +
            " Visvanathan</a>" +
            "</TR></TD>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }

    public String getAlgURL() {
	return "sandmark/watermark/steganography/doc/help.html";
    }

    public String getAuthor() {
	return "Srinivas Visvanathan";
    }

    public String getAuthorEmail() {
	return "srini@cs.arizona.edu";
    }

    public String getDescription() {
	return "Steganograph hides watermarks in PNG images of the application.";
    }

    public String getLongName() {
	return "Steganography";
    }

    public sandmark.config.ModificationProperty[] getMutations() {
	return null;
    }
    
    public String getShortName() {
	return "Steganography";
    }



//Instance of this class is returned by recognize
class StegIterator implements java.util.Iterator
{
    private long key;
    java.util.List wmarks;
    java.util.Iterator curr;

    public StegIterator(sandmark.watermark.StaticRecognizeParameters params) 
	throws sandmark.watermark.WatermarkingException {
	key = sandmark.util.StringInt.encode(params.key).longValue();
	wmarks = new java.util.LinkedList();
	
	//scan thru PNG's of app, and extract any watermarks they contain into a
	//list
	java.util.Iterator it = params.app.files();
	while (it.hasNext()) {
	    sandmark.program.File f = (sandmark.program.File)it.next();
	    if (!f.getJarName().toLowerCase().endsWith(".png"))
		continue;
	    String wmark = getWatermark(f,key);
	    if (wmark != null)
		wmarks.add(wmark);
	}
	curr = wmarks.iterator();
    }

    public boolean hasNext() {
	return curr.hasNext();
    }

    public Object next() {
	return curr.next();
    }

    public void remove() {
	//does nothing
    }

    private String getWatermark(sandmark.program.File pngFile,long key) 
	throws sandmark.watermark.WatermarkingException {
	//read in the png
	java.awt.image.BufferedImage bi;
	try {
	    bi = javax.imageio.ImageIO.read(//new java.io.File("out.png"));
		    new java.io.ByteArrayInputStream(
			pngFile.getBytes()));
	} catch (java.io.IOException e) {
	    throw new sandmark.watermark.WatermarkingException(
		    "Error occurred while reading " + pngFile.getJarName());
	}

	//extract hidden payload if any
	byte[] payload = ImageHider.recover(bi);
	if (payload == null)
	    return null;
	
	//extract class length
	int clsLen = (payload[0] & 0xFF)
		| (payload[1] & 0xFF) << 8
		| (payload[2] & 0xFF) << 16
		| (payload[3] & 0xFF) << 24;
	if (clsLen < 0 || clsLen + 2 *HDR_SIZE > payload.length)
	    return null;

	//get watermark length
	int ofs = clsLen + HDR_SIZE;
	int wmarkLen = (payload[ofs] & 0xFF)
		    | (payload[ofs + 1] & 0xFF) << 8
		    | (payload[ofs + 2] & 0xFF) << 16
		    | (payload[ofs + 3] & 0xFF) << 24;
	if (ofs < 0 || ofs + HDR_SIZE + wmarkLen > payload.length)
	    return null;

	//extract watermark
	byte[] watermark = new byte[wmarkLen];
	for (int j = 0; j < wmarkLen; j++)
	    watermark[j] = payload[ofs + HDR_SIZE + j];
	keyXOR(watermark,key);
	return new String(watermark);
    }

}   //class StegIterator

}   //class Steganography
