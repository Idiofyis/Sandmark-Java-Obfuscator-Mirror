package sandmark.watermark.mondenwmark;

/**
 * MondenWmark Watermarking
        @author         Armand Navabi
        @version        1.0
 */

public class MondenWmark
   extends sandmark.watermark.StaticWatermarker {

    public String getShortName() { return "Monden"; }
    public String getAuthor(){ return "Armand Navabi"; }
    public String getAuthorEmail(){ return "navabia@cs.arizona.edu"; }
    public String getDescription(){
        return "Implements the watermarking technique described in A" +
            " Practical Method for Watermarking Java Programs by A. Monden, H. " +
            " Iida, K. Matsumoto, K. Inoue, and K. Torii." +
            " The watermark is embeded by replacing instruction in a dummy" +
            " method, which is added to the" +
            " application.";
    }
    public sandmark.config.ModificationProperty [] getMutations() { return null; }

    public String getLongName() {
        return "Implements watermarking technique described by Akito Monden.";
    }

    public java.lang.String getAlgHTML(){
        return
            "<HTML><BODY>" +
            "MondenWmark implements the watermarking technique described in A" +
            " Practical Method for Watermarking Java Programs by A. Monden, H. " +
            " Iida, K. Matsumoto, K. Inoue, and K. Torii." +
            " The watermark is embeded by replacing instruction in a dummy" +
            " method, which is added to the" +
            " application." +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href =\"mailto:navabia@cs.arizona.edu\">Armand Navabi</a>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }
    public java.lang.String getAlgURL() {
        return "sandmark/watermark/mondenwmark/doc/help.html";
    }

/*************************************************************************/
/*                               Embedding                               */
/*************************************************************************/
    private int numInstructions(sandmark.program.Application app) {
	sandmark.program.Class[] classes = app.getClasses();
	sandmark.program.Method[] methods;
	int result = 0;
	for(int i = 0; i < classes.length; i++) {
	    methods = classes[i].getMethods();
	    for(int j = 0; j < methods.length; j++) {
		if(methods[j].getInstructionList() != null)
		    result += methods[j].getInstructionList().size();
	    }
	}
	return result;
    }

    //For testing and evaluation (script) purposes
    public static void main(String[] args) {
	if(args.length != 2) {
	    System.out.println("usage: 2 args");
	    return;
	}
	String appStr = args[0];
	String propsStr = args[1];

	try {
	    sandmark.program.Application app = 
		new sandmark.program.Application(appStr);
	    MondenWmark m = new MondenWmark();
	    sandmark.watermark.StaticWatermarker.getProperties().setProperty("Watermark",propsStr);
	    m.embed(sandmark.watermark.StaticWatermarker.getEmbedParams(app));
	    System.out.println("done");
	} catch (Exception e) { 
	    System.out.println("error: " + e);
	    e.printStackTrace();
	}
    }

    public static final String PREFIX = "m"; //prefix to prevent false watermarks
    public String watermarkTest;

    public void embed(sandmark.watermark.StaticEmbedParameters params) 
        throws sandmark.watermark.WatermarkingException {

	watermarkTest = params.watermark;

	int instrsAdded = 0;
	System.out.println("total num instrs: " + numInstructions(params.app));

	String watermark = params.watermark + " ";
	
	watermark = PREFIX + watermark;
	String watermarkCode = sandmark.util.StringInt.encode(watermark).toString(2);
	int length = watermarkCode.length();
	while(length % 2 != 0){
	    watermark += "*";
	    watermarkCode = sandmark.util.StringInt.encode(watermark).toString(2);
	    length = watermarkCode.length();
	}
	String lengthCode = new java.math.BigInteger(length + "").toString(2);
	while(lengthCode.length() < 12) lengthCode = "0" + lengthCode;

	watermarkCode = lengthCode + watermarkCode;
	//while((watermarkCode.length() % 3) != 0) watermarkCode += "1";  //because we use 3 bit codes
	String key = params.key;
	java.util.Iterator classes = params.app.classes();
	String randKey = makeKey(key);
	//System.out.println("Random Key: " + randKey);
	java.util.Hashtable codeTable = buildCodeTable(randKey);

	if(!classes.hasNext())
	    throw new sandmark.watermark.WatermarkingException("There must at least one class to watermark.");

	//Embed watermark in method with the most number of arithmetic ops.

        int methodMaxOpCount = -1;
        sandmark.program.Method maxOpMethod = null;
	sandmark.program.Class methodClass = null;
	while(classes.hasNext()){
            sandmark.program.Class clazz = (sandmark.program.Class) classes.next();
            for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
                sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
		if(method.getName().equals("<init>")) continue;
		else {
		    int space = getEncodeSize(method);
		    if(space > methodMaxOpCount){
			methodMaxOpCount = space;
			maxOpMethod = method;
			methodClass = clazz;
		    }
		}
            }
	}


	System.out.println("Copied Method size: " + maxOpMethod.getInstructionList().size());
	sandmark.program.Method methodCopy = maxOpMethod.copy();

	//Add instructions so that mg can encode the given watermark
	instrsAdded = addInstrs(methodCopy, watermarkCode.length());

	//instrsAdded += methodCopy.getInstructionList().size();

	//Now change the name of the dummyMethod, and add it so it becomes part of the class
	methodCopy.setMaxStack();
	methodCopy.setMaxLocals();

	org.apache.bcel.generic.InstructionHandle[] ihs = 
	    methodCopy.getInstructionList().getInstructionHandles();

	//System.out.println("WATERMARK CODE ENCODING: " + watermarkCode + " length of " + watermarkCode.length());

	//encode watermarkCode
	int endSub, startSub = 0;
	int codeLength = watermarkCode.length();
	String tableKey;
	int numericalValue;
	short[] ops = {96, 100, 104, 108, 112, 126, 128, 130};
	short opCode;
	for(int i = 0; i < ihs.length; i++){
	    if(startSub < codeLength) {
		org.apache.bcel.generic.Instruction instr = ihs[i].getInstruction();

		if(isNumericalInstr(instr)) {
		    endSub = Math.min(codeLength, startSub + 8);
		    numericalValue = new java.math.BigInteger(watermarkCode.substring(startSub, endSub), 2).intValue() - 128;
		    //System.out.println(watermarkCode.substring(startSub, endSub) + ": " + numericalValue);
		    if(instr instanceof org.apache.bcel.generic.IINC) 
			((org.apache.bcel.generic.IINC)instr).setIncrement(numericalValue);
		    else if(instr instanceof org.apache.bcel.generic.BIPUSH) {
			//System.out.println("numerical value: " + numericalValue);
			ihs[i].setInstruction(new org.apache.bcel.generic.BIPUSH(new Byte(numericalValue + "").byteValue()));
		    }
		    startSub += 8; 
		}
		else if(isIArithmeticInstr(instr)) {
		    endSub = Math.min(codeLength, startSub + 3);
		    tableKey = watermarkCode.substring(startSub, endSub);
		    //System.out.println("tableKey: " + tableKey);
		    while(tableKey.length() != 3) tableKey = tableKey + "1";
		    opCode = ((Short)codeTable.get(tableKey)).shortValue();
		    //System.out.println("encoding opcode: " + tableKey);
		    if(opCode == ops[0]) 
			ihs[i].setInstruction(new org.apache.bcel.generic.IADD());
		    else if(opCode == ops[1]) 
			ihs[i].setInstruction(new org.apache.bcel.generic.ISUB());
		    else if(opCode == ops[2]) 
			ihs[i].setInstruction(new org.apache.bcel.generic.IMUL());
		    else if(opCode == ops[3]) 
			ihs[i].setInstruction(new org.apache.bcel.generic.IDIV());
		    else if(opCode == ops[4]) 
			ihs[i].setInstruction(new org.apache.bcel.generic.IREM());
		    else if(opCode == ops[5]) 
			ihs[i].setInstruction(new org.apache.bcel.generic.IAND());
		    else if(opCode == ops[6]) 
			ihs[i].setInstruction(new org.apache.bcel.generic.IOR());
		    else if(opCode == ops[7]) 
			ihs[i].setInstruction(new org.apache.bcel.generic.IXOR());
		    startSub += 3;
		}
	    }
	}

	methodCopy.removeLocalVariables();

	//System.out.println("Instruction Size: " + methodCopy.getInstructionList().size());

	sandmark.program.Method falseCallMethod = null;
	boolean found = false;
	for(java.util.Iterator methodIt = methodClass.methods() ; 
	    methodIt.hasNext() && !found; ) {
	    falseCallMethod = (sandmark.program.Method)methodIt.next();
	    if(!falseCallMethod.getName().equals("<init>")) found = true;
	}

	if(found) {
	    org.apache.bcel.generic.InstructionList newIL = 
		new org.apache.bcel.generic.InstructionList();
	    newIL.append(new org.apache.bcel.generic.ICONST(1));
	    newIL.append(new org.apache.bcel.generic.IFNE
			 (falseCallMethod.getInstructionList().getStart()));
	    org.apache.bcel.generic.InstructionFactory factory =
		new org.apache.bcel.generic.InstructionFactory
		(falseCallMethod.getConstantPool());

	    if(!methodCopy.isStatic()) {
		newIL.append(new org.apache.bcel.generic.ACONST_NULL());
		newIL.append(factory.createCheckCast
			     (methodCopy.getEnclosingClass().getType()));
	    }
	    
	    org.apache.bcel.generic.Type[] argTypes = methodCopy.getArgumentTypes();
	    for(int i = 0; i < argTypes.length; i++)
		newIL.append(org.apache.bcel.generic.InstructionFactory.createNull(argTypes[i]));
	    newIL.append
		(factory.createInvoke
		 (methodCopy.getEnclosingClass().getName(),methodCopy.getName(),
		  methodCopy.getReturnType(),methodCopy.getArgumentTypes(),
		  methodCopy.isStatic() ? org.apache.bcel.Constants.INVOKESTATIC :
		  org.apache.bcel.Constants.INVOKEVIRTUAL));
	    if(!methodCopy.getReturnType().equals(org.apache.bcel.generic.Type.VOID))
		newIL.append(org.apache.bcel.generic.InstructionFactory.createPop(methodCopy.getReturnType().getSize()));
	    falseCallMethod.getInstructionList().insert
		(falseCallMethod.getInstructionList().getStart(),newIL);
	}
	
	//instrsHandle = il.getInstructionHandles();
	//for(int i = 4; i > 0; i--) System.out.println(instrsHandle[instrsHandle.length - i]);	    
       
	System.out.println("total num instrs added: " + instrsAdded);
    }

    private String makeKey (String key) {
	return makeKey(key == null ? new String("").hashCode() : key.hashCode());
    }

    private String makeKey(int randomSeed) {
	java.util.Random rand = new java.util.Random(new Integer(randomSeed).longValue());
	int[] randomArray = new int[8];
	char[] keyArray = {'a', 'r', 's', 'n', 'm', 'o', 'd', 'x'};
	
	//get random ordering
	for(int i = 0; i < 8; i++) randomArray[i] = rand.nextInt();
	
	//now sort key array
	int i = 0;
	int j = 1;
	int temp;
	char tempChar;
	
	while( j < randomArray.length) {
	    while(j > 0 && randomArray[j] < randomArray[j-1]) {
		temp = randomArray[j];
		randomArray[j] = randomArray[j-1];
		randomArray[j-1] = temp;
		
		tempChar = keyArray[j];
		keyArray[j] = keyArray[j-1];
		keyArray[j-1] = tempChar;
		
		j--;
	    }
	    i ++;
	    j = i + 1;
	}
	
	//for(int k = 0; k < 8; k++) System.out.println(randomArray[k]);
	
	return new String(keyArray);
    }
    
    private int getOpIndex(char c){
	switch (c){
	case 'a': return 0;
	case 's': return 1;
	case 'm': return 2;
	case 'd': return 3;
	case 'r': return 4;
	case 'n': return 5;
	case 'o': return 6;
	case 'x': return 7;
	default: return -1;
	}
    }

    private java.util.Hashtable buildCodeTable(String key){
	java.util.Hashtable codeTable = new java.util.Hashtable();
	short[] ops = {96, 100, 104, 108, 112, 126, 128, 130};
	Short[] opcodes = {new Short(ops[0]), new Short(ops[1]), new Short(ops[2]), new Short(ops[3]),
			   new Short(ops[4]), new Short(ops[5]), new Short(ops[6]), new Short(ops[7])};

	codeTable.put("000", opcodes[getOpIndex(key.charAt(0))]);
	codeTable.put("001", opcodes[getOpIndex(key.charAt(1))]);
	codeTable.put("010", opcodes[getOpIndex(key.charAt(2))]);
	codeTable.put("100", opcodes[getOpIndex(key.charAt(3))]);
	codeTable.put("011", opcodes[getOpIndex(key.charAt(4))]);
	codeTable.put("101", opcodes[getOpIndex(key.charAt(5))]);
	codeTable.put("110", opcodes[getOpIndex(key.charAt(6))]);
	codeTable.put("111", opcodes[getOpIndex(key.charAt(7))]);
	return codeTable;
    }

    private int addInstrs(sandmark.program.Method method, int wmarkLength) {
	int instrsAdded = 0;
	org.apache.bcel.generic.InstructionList il = method.getInstructionList();
	//System.out.println("METHOD: " + method);
	if(il == null) {
	    il = new org.apache.bcel.generic.InstructionList();
	    method.setInstructionList(il);
	}
	//org.apache.bcel.generic.InstructionHandle[] ihs = il.getInstructionHandles();
	//int moreOps = (wmarkLength - (getNumOps(method) * 3)) / 3;
	int spaceNeeded = wmarkLength - getEncodeSize(method);
	if(spaceNeeded < 1) return 0;
	//make sure there are at least on instruction to encode
	il.insert(il.getStart(),new org.apache.bcel.generic.ISTORE(43));
	il.insert(il.getStart(),new org.apache.bcel.generic.IADD());
	int byteValue = (int)(sandmark.util.Random.getRandom().nextDouble() * 50);
	il.insert(il.getStart(), new org.apache.bcel.generic.BIPUSH(new Byte(byteValue + "").byteValue()));
	byteValue = (int)(sandmark.util.Random.getRandom().nextDouble() * 50);
	il.insert(il.getStart(), new org.apache.bcel.generic.BIPUSH(new Byte(byteValue + "").byteValue()));
	instrsAdded += 4;
	spaceNeeded -= 19;
	org.apache.bcel.generic.InstructionHandle[] ihs = il.getInstructionHandles();
	for(int i = 0; i < ihs.length; i++){
	    if(spaceNeeded > 0 && !(ihs[i].getInstruction() instanceof org.apache.bcel.generic.IINC) &&
	       (isIArithmeticInstr(ihs[i].getInstruction()) || isNumericalInstr(ihs[i].getInstruction()))) {
		while(spaceNeeded > 0){
		    byteValue = (int)(sandmark.util.Random.getRandom().nextDouble() * 50);
		    il.append(ihs[i],new org.apache.bcel.generic.IADD());
		    il.append(ihs[i],new org.apache.bcel.generic.BIPUSH(new Byte(byteValue + "").byteValue()));
		    instrsAdded += 2;
		    spaceNeeded -= 11;
		}
	    }
	}
	method.getInstructionList().setPositions(true);
	return instrsAdded;
    }

    private int getEncodeSize(sandmark.program.Method method) {
	org.apache.bcel.generic.InstructionList il = method.getInstructionList();
	if(il == null) return 0;
	org.apache.bcel.generic.Instruction[] instructions = il.getInstructions();
	
	int total = 0;
	for(int i = 0; i < instructions.length; i++) {
	    if (isIArithmeticInstr(instructions[i])) total += 3;
	    else if(isNumericalInstr(instructions[i])) total += 8;
	}

	return total;
    }

    private boolean isIArithmeticInstr(org.apache.bcel.generic.Instruction instr) {
	short[] opcodes = {96, 100, 104, 108, 112, 126, 128, 130};
	java.util.ArrayList replaceable = new java.util.ArrayList();
	for(int i = 0; i < opcodes.length; i++) replaceable.add(new Short(opcodes[i]));
	if(replaceable.contains(new Short(instr.getOpcode()))) return true;
	return false;
    }

    private boolean isNumericalInstr(org.apache.bcel.generic.Instruction instr) {
	if(instr instanceof org.apache.bcel.generic.BIPUSH ||
	   instr instanceof org.apache.bcel.generic.IINC) return true;
	else return false;
    }

    public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
	throws sandmark.watermark.WatermarkingException {
	String key = params.key;
	String randKey = makeKey(key);
	//System.out.println("Random Key: " + randKey);
	java.util.Hashtable codeTable = buildDeCodeTable(randKey);
	sandmark.program.Application app = params.app;

        java.util.ArrayList wms = new java.util.ArrayList();
        for(java.util.Iterator classIt = app.classes() ; classIt.hasNext() ; ) {
            sandmark.program.Class clazz = (sandmark.program.Class)classIt.next();
            for(java.util.Iterator methodIt = clazz.methods() ; methodIt.hasNext() ; ) {
                sandmark.program.Method method = (sandmark.program.Method)methodIt.next();
                String currentWMark = "";
                org.apache.bcel.generic.InstructionList il = method.getInstructionList();
		if(il == null) continue; //if not instruction list, go to next method.
		org.apache.bcel.generic.Instruction[] instructions = il.getInstructions();
                for(int j = 0; j < instructions.length; j++){
		    //System.out.println("Instruction: " + instructions[j]);
                    if(isIArithmeticInstr(instructions[j]))
                        currentWMark += (String)codeTable.get(new Short(instructions[j].getOpcode()));
		    else if(isNumericalInstr(instructions[j])){
			if(instructions[j] instanceof org.apache.bcel.generic.IINC) {
			    int numericalValue = ((org.apache.bcel.generic.IINC)instructions[j]).getIncrement() + 128;
			    String code = new java.math.BigInteger(numericalValue + "").toString(2);
			    while(code.length() < 8) code = "0" + code;
			    currentWMark += code;
			}
			else if(instructions[j] instanceof org.apache.bcel.generic.BIPUSH) {
			    int numericalValue = ((org.apache.bcel.generic.BIPUSH)instructions[j]).getValue().intValue() + 128;
			    String code = new java.math.BigInteger(numericalValue + "").toString(2);
			    while(code.length() < 8) code = "0" + code;
			    currentWMark += code;
			}
		    }
		    //System.out.println(currentWMark);
		}
		//System.out.println("current wmark: " + currentWMark + " length: " + currentWMark.length());
		String decodedWmark = getWmarkValue(currentWMark);
		//System.out.println("decoded wmark: " + decodedWmark);
		if(!(decodedWmark.equals("")) && decodedWmark.startsWith(PREFIX)) {
		    String recognizedWmark = decodedWmark.substring(1).trim();
		    //if(recognizedWmark.equals(watermarkTest)) System.out.println("WATERMARK RECOGNIZED!");
		    //else System.out.println(recognizedWmark + " does not equal " + watermarkTest);
		    wms.add(recognizedWmark);
		}
            }
        }

        return wms.iterator();
    }

    private String getWmarkValue(String wmarkCode){
	if(!(wmarkCode.length() > 12)) return "";
	String wmarkLengthStrCode = wmarkCode.substring(0, 12);
	java.math.BigInteger bInteger = new java.math.BigInteger(wmarkLengthStrCode, 2);
	String wmarkLengthStr = bInteger + "";
	int wmarkLength;
	try { wmarkLength = Integer.parseInt(wmarkLengthStr); }
	catch(Exception e) { return ""; }
	int endIndex = Math.min(wmarkLength + 12, wmarkCode.length());
	String bareWMark = wmarkCode.substring(12, endIndex);
	String wmark;
	try { wmark = sandmark.util.StringInt.decode(new java.math.BigInteger(bareWMark, 2)); }
	catch (NumberFormatException e) { wmark = ""; }
	return wmark;
    }

    private java.util.Hashtable buildDeCodeTable(String key){
	java.util.Hashtable codeTable = new java.util.Hashtable();
	short[] ops = {96, 100, 104, 108, 112, 126, 128, 130};
	Short[] opcodes = {new Short(ops[0]), new Short(ops[1]), new Short(ops[2]), new Short(ops[3]),
			   new Short(ops[4]), new Short(ops[5]), new Short(ops[6]), new Short(ops[7])};

	codeTable.put(opcodes[getOpIndex(key.charAt(0))], "000");
	codeTable.put(opcodes[getOpIndex(key.charAt(1))], "001");
	codeTable.put(opcodes[getOpIndex(key.charAt(2))], "010");
	codeTable.put(opcodes[getOpIndex(key.charAt(3))], "100");
	codeTable.put(opcodes[getOpIndex(key.charAt(4))], "011");
	codeTable.put(opcodes[getOpIndex(key.charAt(5))], "101");
	codeTable.put(opcodes[getOpIndex(key.charAt(6))], "110");
	codeTable.put(opcodes[getOpIndex(key.charAt(7))], "111");
	return codeTable;
    }

}
