package sandmark.watermark.hattrick;


/*
 ** Program: HatTrick.java
 ** Authors: Danny Mandel and Anna Segurson
 ** Purpose: Implements algorithm 3.9 for CSc 620's Project 1
 */

public class HatTrick extends sandmark.watermark.StaticWatermarker {
   private static final boolean DEBUG = false;
   private static final String SECRET_NAME = "yzerman";

   private static java.util.Hashtable classHash;
   private static java.util.Hashtable numHash;

   /*
    * Returns the base-10 digit mapped to the passed-in signature
    * Each signature maps to a single base-10 digit.
    */
   public static java.math.BigInteger getDigitFromSig(String signature) {
      return (java.math.BigInteger) classHash.get(signature);
   }

   static {
      classHash = new java.util.Hashtable();
      numHash = new java.util.Hashtable();
      classHash.put("Ljava/util/GregorianCalendar;",
            new java.math.BigInteger("0"));
      numHash.put(new java.math.BigInteger("0"), "Ljava/util/GregorianCalendar;");
      classHash.put("Ljava/lang/Thread;", new java.math.BigInteger("1"));
      numHash.put(new java.math.BigInteger("1"), "Ljava/lang/Thread;");
      classHash.put("Ljava/util/Vector;", new java.math.BigInteger("2"));
      numHash.put(new java.math.BigInteger("2"), "Ljava/util/Vector;");
      classHash.put("Ljava/util/Stack;", new java.math.BigInteger("3"));
      numHash.put(new java.math.BigInteger("3"), "Ljava/util/Stack;");
      classHash.put("Ljava/util/Date;", new java.math.BigInteger("4"));
      numHash.put(new java.math.BigInteger("4"), "Ljava/util/Date;");
      // changed java.io.InputStream to java.lang.Byte
      classHash.put("Ljava/lang/Byte;", new java.math.BigInteger("5"));
      numHash.put(new java.math.BigInteger("5"), "Ljava/lang/Byte;");
      // changed java.io.ObjectInputStream to java.lang.Number
      classHash.put("Ljava/lang/Number;", new java.math.BigInteger("6"));
      numHash.put(new java.math.BigInteger("6"), "Ljava/lang/Number;");
      classHash.put("Ljava/lang/Math;", new java.math.BigInteger("7"));
      numHash.put(new java.math.BigInteger("7"), "Ljava/lang/Math;");
      // changed java.io.ObjectOutputStream to java.lang.StrictMath
      classHash.put("Ljava/lang/StrictMath;", new java.math.BigInteger("8"));
      numHash.put(new java.math.BigInteger("8"), "Ljava/lang/StrictMath;");
      classHash.put("Ljava/lang/String;", new java.math.BigInteger("9"));
      numHash.put(new java.math.BigInteger("9"), "Ljava/lang/String;");
   }

   /*
    **  Returns this watermarker's short name.
    */
   public String getShortName() {
      return "Register Types";
   }

   /*
    **  Returns this watermarker's long name.
    */
   public String getLongName() {
      return "Embed a watermark in the types of local variables in a method.";
   }

   public String getDescription() {
      return          "HatTrick is a way of encoding watermarks based on "
            + "special local variables that encode a message based on the "
            + " locals' types. Each type maps to a base-10 digit that encodes "
            + "a numerical watermark.";
   }

   public String getAuthor() {
      return "Anna Segurson and Danny Mandel";
   }

   public String getAuthorEmail() {
      return "segurson@cs.arizona.edu and dmandel@cs.arizona.edu";
   }

   public sandmark.config.ModificationProperty[] getMutations() {
      return null;
   }

   /*
    **   Get the HTML codes of the About page.
    */
   public java.lang.String getAlgHTML() {
      return
            "<HTML><BODY>\n"
            + "HatTrick is a way of encoding watermarks based on\n"
            + " special local variables that encode a message based on the "
            + "locals' types.  Each type maps to a base-10 digit that encodes "
            + "a numerical watermark." + "<table>\n" + "<TR><TD>\n"
            + "   Authors: <a href=\"mailto:dmandel@cs.arizona.edu\">Danny"
            + " Mandel</a> and <a href=\"mailto:segurson@cs.arizona.edu\">Anna"
            + "\nSegurson" + "</TD></TR>\n" + "</table>\n" + "</BODY></HTML>\n";
   }

   /*
    **  Get the URL of the Help page
    */

    public java.lang.String getAlgURL(){
	return "sandmark/watermark/hattrick/doc/help.html";
    }


    private String getClassFromSig(String signature) {
	String returnString = "";

	if (signature.startsWith("L")) {
	    signature = signature.substring(1);
	}
	if (signature.endsWith(";")) {
	    signature = signature.substring(0, signature.length() - 1);
	}
	for (int i = 0; i < signature.length(); i++) {
	    if (signature.charAt(i) != '/') {
		returnString += signature.charAt(i);
	    } else {
		returnString += ".";
	    }
	}

	return returnString;
    }

    /*************************************************************************/
    /*                               Embedding                               */
    /*************************************************************************/


    
    public void embed(sandmark.watermark.StaticEmbedParameters params)
	throws sandmark.watermark.WatermarkingException {
	
	String watermark, fieldName, currentSig;
	String currentClassName;
	java.math.BigInteger wmBigInteger;
	java.util.Vector digitHolder;
	sandmark.program.Class origClass = null;
	sandmark.program.Method currMethod= null;
	org.apache.bcel.generic.InstructionList il, newIl;
	org.apache.bcel.generic.ConstantPoolGen cpg;
	org.apache.bcel.generic.LocalVariableGen currentLocalVar;
	int field_acc_flags;

	
	watermark = params.watermark;
	
	sandmark.util.Log.message(0,"[Hattrick] watermark started!\n");

        java.util.Hashtable methodCounts = new java.util.Hashtable();
        for(java.util.Iterator classes = params.app.classes() ; classes.hasNext() ; ) {
           sandmark.program.Class clazz = 
              (sandmark.program.Class)classes.next();
           for(java.util.Iterator methods = clazz.methods() ; 
               methods.hasNext() ; ) {
              sandmark.program.Method method = 
                 (sandmark.program.Method)methods.next();
              Integer i = (Integer)methodCounts.get(method.getName());
              if(i == null)
                 methodCounts.put(method.getName(),new Integer(1));
              else
                 methodCounts.put(method.getName(),
                                  new Integer(i.intValue() + 1));
           }
        }

	sandmark.program.Class[] classes = params.app.getClasses();
	sandmark.program.Method[] methods;
	int i = 0;
	while(i < classes.length){
	    origClass = classes[i];
	    if(!origClass.isAbstract()){
		methods = origClass.getMethods();
		for(i = 0 ; i < methods.length; i++){
		    currMethod = methods[i];
		    if (currMethod.getInstructionList() != null &&
			methodCounts.get(currMethod.getName()).equals(new Integer(1))) {
			break;
		    }
		    currMethod = null;
		}
		if(currMethod!=null)
		    break;
		methods = null;
	    }
	    origClass = null;
	    i++;
	}

	if(origClass == null ){
	    sandmark.util.Log.message(0,"[Hattrick] Need atleast one non-abstract class to watermark");
	    return;
	}

        currMethod.removeLocalVariables();

	// Break down the watermark value into digits
	wmBigInteger = sandmark.util.StringInt.encode(watermark);
	digitHolder = new java.util.Vector();
	while (!wmBigInteger.equals(java.math.BigInteger.ZERO)) {
	    digitHolder.add(wmBigInteger.mod(new java.math.BigInteger("10")));
	    wmBigInteger = wmBigInteger.divide(new java.math.BigInteger("10"));
	}


      il = currMethod.getInstructionList();

      newIl = new org.apache.bcel.generic.InstructionList();


      cpg = origClass.getConstantPool();
      cpg.addClass("java.lang.Number");
      cpg.addClass("java.lang.Object");
	
      for (i = 0; i < digitHolder.size(); i++) {
         currentSig = (String) numHash.get(digitHolder.elementAt(i));
         currentClassName = getClassFromSig(currentSig);
         if(DEBUG)
            System.out.println(i + " " + digitHolder.elementAt(i) + " " + currentSig);
         currentLocalVar = currMethod.addLocalVariable
               (SECRET_NAME + "$" + i,
               org.apache.bcel.generic.Type.getType(currentSig), null, null);
         newIl.append(new org.apache.bcel.generic.NEW
               (cpg.addClass(currentClassName)));
         newIl.append(new org.apache.bcel.generic.DUP());
         newIl.append(new org.apache.bcel.generic.INVOKESPECIAL
               (cpg.addMethodref(getClassFromSig(currentSig), "<init>", "()V")));
         newIl.append(new org.apache.bcel.generic.ASTORE
               (currentLocalVar.getIndex()));
      }
      // changed il.insert(newIl) to il.append(newIl)
      il.append(newIl);
      currMethod.mark();

      /*
       ** Add a "public static final String" field to the class to mark which
       ** method the watermark is in
       */
      field_acc_flags = org.apache.bcel.Constants.ACC_PUBLIC
            | org.apache.bcel.Constants.ACC_FINAL
            | org.apache.bcel.Constants.ACC_STATIC;
      fieldName = "hat" + currMethod.getName() + "Trick";

      new sandmark.program.LocalField(origClass,
            field_acc_flags, org.apache.bcel.generic.Type.STRING, fieldName);

      	
      origClass.mark();    
   }


   /*************************************************************************/

   /* Recognition                              */

   /*************************************************************************/

   /* An iterator which generates the watermarks
    **  found in the program.
    */

    class Recognizer implements java.util.Iterator {
	java.util.Vector result = new java.util.Vector();
	int current = 0;

	public Recognizer(sandmark.watermark.StaticRecognizeParameters params) {
	    generate(params);
	}

	public void generate(sandmark.watermark.StaticRecognizeParameters params) {
	    java.util.Iterator classes= null;
	    String waterMark = "";
	    sandmark.program.Class clazz=null;
	    sandmark.program.Field[] fields=null;
	    sandmark.program.Method[] methods=null;
	    sandmark.program.Method wmMeth = null;
	    int f, m, wmPos;
	    String fieldName, methName = "", wmPosString;
	    org.apache.bcel.generic.LocalVariableGen lv;
	    java.math.BigInteger wmInteger, currentBigInteger, currentTenPower, ten;


	    wmInteger = new java.math.BigInteger("0");
	    ten = new java.math.BigInteger("10");
	    classes = params.app.classes();

	    /*
	    ** Find the class and the method where the bogus variables are
	    ** using the special field marker
	    */
	    while (classes.hasNext()) {
	       clazz = (sandmark.program.Class)classes.next();
	       fields = clazz.getFields();
	       for(f = 0; f < fields.length; f++) {
	          if (fields[f].getSignature().equals("Ljava/lang/String;")) {
	             fieldName = fields[f].getName();
	             if (fieldName.startsWith("hat") && fieldName.endsWith("Trick")) {
	                // found the method, extract the name
	                methName = fieldName.substring(3,fieldName.length() - 5);
	                f = fields.length;
	             }
	          }
	       }
	       
	       methods = clazz.getMethods();
	       for (m = 0; m < methods.length; m++) {
	          if (methods[m].getName().equals(methName)) {
	             wmMeth = methods[m];
	             m = methods.length;
	          }
	       }
	    }
	    /*
	     ** If this class had the method containing the watermark then
	     ** extract the method's local variable table and recover WM
	     */
	    if (wmMeth != null) {
	       org.apache.bcel.generic.LocalVariableGen lvgs[] =
	          wmMeth.getLocalVariables();
	       if (lvgs != null) {
	          for(int i = 0 ; i < lvgs.length ; i++) {
	             lv = lvgs[i];
	             if (lv != null && (lv.getName().length() > SECRET_NAME.length()) &&
	                   (lv.getName().startsWith(SECRET_NAME))) {
	                wmPosString = lv.getName().substring(8);
	                wmPos = Integer.valueOf(wmPosString).intValue();
	                currentBigInteger = getDigitFromSig(lv.getType().getSignature());
	                if(DEBUG)
	                   System.out.println(wmPosString + " " + wmPos + " " + currentBigInteger + " " + lv.getType().getSignature());
	                //System.out.println("currentBigInteger: " + currentBigInteger);
	                currentTenPower = ten.pow(wmPos);
	                wmInteger = wmInteger.add
	                (currentBigInteger.multiply(currentTenPower));
	                //System.out.println("current wmInteger: " + wmInteger);
	             }
	          }
	          waterMark = sandmark.util.StringInt.decode(wmInteger);
	       }
	    }
	    result.add(waterMark);
	}

	public boolean hasNext() {
	    return (current < result.size());
	}

	public java.lang.Object next() {
	    return result.get(current++);
	}

      public void remove() {}
   }

   /*
    ** Return an iterator which generates the watermarks
    ** found in the program. The props argument
    ** holds at least the following properties:
    **  <UL>
    **     <LI> Input File: The name of the file to be watermarked.
    **  </UL>
    */
   public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
      throws sandmark.watermark.WatermarkingException {
      return new Recognizer(params);
   }

} // class HatTrick
