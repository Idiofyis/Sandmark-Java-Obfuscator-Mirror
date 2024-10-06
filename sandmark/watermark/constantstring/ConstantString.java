package sandmark.watermark.constantstring;

/**
 * This algorithm adds a constant
 *    "sm$watermark=WATERMARK"
 * to the constant pool of a random class.
 * "WATERMARK" is the actual watermark value,
 * sm$watermark is the identifer we're looking
 * for during recognition.
 */

public class ConstantString
   extends sandmark.watermark.StaticWatermarker {

    /**
     *  Constructs a watermarker.
     */
    public ConstantString() {}


    /**
     *  Returns this watermarker's short name.
     */
    public String getShortName() {
        return "String Constant";
    }

    /**
     *  Returns this watermarker's long name.
     */
    public String getLongName() {
        return "Embed a watermark in a string in the constant pool";
    }

    public String getAuthor(){
        return "Christian Collberg";
    }

    public String getAuthorEmail(){
        return "collberg@cs.arizona.edu";
    }

    public String getDescription(){
        return "Embed a watermark in a string in the constant pool";
    }

    public sandmark.config.ModificationProperty[] getMutations(){
       sandmark.config.ModificationProperty[] properties = {
          sandmark.config.ModificationProperty.I_CHANGE_CONSTANT_POOL};
       return properties; 
    }

    public sandmark.config.RequisiteProperty[] getPostprohibited(){
       sandmark.config.RequisiteProperty[] properties = {
          sandmark.config.ModificationProperty.I_CHANGE_CONSTANT_POOL};
       return properties; 
    }

    /*
     *  Get the properties of ConstantString algorithm
     */
    private sandmark.util.ConfigProperties mConfigProps;
    public sandmark.util.ConfigProperties getConfigProperties(){
        if(mConfigProps == null) {
            String[][] props = {
                {"SWM_ConstantString_Ident",
                 "sm$watermark",
                 "The prefix of the string in which we hide the watermark.",
                 null, "S", "N",
                },
            };
            mConfigProps = new sandmark.util.ConfigProperties(props,null);
        }
        return mConfigProps;
    }

    /*
     *  Get the HTML codes of the About page for ConstantString
     */
    public java.lang.String getAlgHTML(){
        return
            "<HTML><BODY>" +
            "ConstantString is a very simple static watermarking " +
            "algorithm, to be used as a simple proof of concept. The" +
            " watermark is embedded in a string in the constant pool." +
            "<TABLE>" +
            "<TR><TD>" +
            "Author: <a href=\"mailto:collberg@cs.arizona.edu\">Christian Collberg</a>\n" +
            "</TD></TR>" +
            "</TABLE>" +
            "</BODY></HTML>";
    }

    /*
     *  Get the URL of the Help page for ConstantString
     */
    public java.lang.String getAlgURL(){
        return "sandmark/watermark/constantstring/doc/help.html";
    }

/*************************************************************************/
/*                               Embedding                               */
/*************************************************************************/


/* Embed a watermark value into the program. The props argument
 * holds at least the following properties:
 *  <UL>
 *     <LI> Watermark: The watermark value to be embedded.
 *  </UL>
 */
public void embed(sandmark.watermark.StaticEmbedParameters params)
    throws sandmark.watermark.WatermarkingException {
   // throw new java.lang.UnsupportedOperationException("unimplemented");
   String watermark = params.watermark;

   java.util.Iterator classes = params.app.classes();
   if (!classes.hasNext())
       throw new sandmark.watermark.WatermarkingException("There must be at least one class to watermark.");

   sandmark.program.Class aclass = (sandmark.program.Class) classes.next();
   org.apache.bcel.generic.ConstantPoolGen cp = aclass.getConstantPool();
   String IDENTIFIER = getConfigProperties().getProperty("SWM_ConstantString_Ident");
   cp.addString(IDENTIFIER + "=" + watermark);
}


/*************************************************************************/
/*                              Recognition                              */
/*************************************************************************/

/* An iterator which generates the watermarks
 * found in the program. We simply walk through
 * every class looking for a constant of the
 * form
 *    "sm$watermark=WATERMARK"
 * We store the WATERMARK bit in a Vector result.
 * The iterator will then generate the elements
 * from this vector one at a time. (This is, obviously,
 * the wrong thing to do, but writing an iterator that
 * actually generates one element at a time in Java is
 * painful.)
 */
class Recognizer implements java.util.Iterator {
    java.util.Vector result = new java.util.Vector();
    int current = 0;

    public Recognizer(sandmark.watermark.StaticRecognizeParameters params) {
       generate(params);
    }

    public void generate(sandmark.watermark.StaticRecognizeParameters params) {
       sandmark.program.Application app = params.app;
       String IDENTIFIER = getConfigProperties().getProperty("SWM_ConstantString_Ident");
       java.util.Iterator classes = app.classes();
       while (classes.hasNext()) {
         sandmark.program.Class aclass = (sandmark.program.Class) classes.next();
         org.apache.bcel.generic.ConstantPoolGen cpg = aclass.getConstantPool();
         org.apache.bcel.classfile.ConstantPool cp = cpg.getConstantPool();
         for (int i=0; i<cp.getLength(); i++) {
             org.apache.bcel.classfile.Constant c = cp.getConstant(i);
             if (c instanceof org.apache.bcel.classfile.ConstantString) {
                 org.apache.bcel.classfile.ConstantString s =
                    (org.apache.bcel.classfile.ConstantString) c;
                 String v = (String)s.getConstantValue(cp);
                 if (v.startsWith(IDENTIFIER)) {
                    String w = v.substring(IDENTIFIER.length()+1);
                    result.add(w);
                 }
             }
          }
       }
    }

    public boolean hasNext() {
       return current < result.size();
    }

    public java.lang.Object next() {
       return result.get(current++);
    }

    public void remove() {}
}



/* Return an iterator which generates the watermarks
 * found in the program. The props argument
 * holds at least the following properties:
 *  <UL>
 *     <LI> Input File: The name of the file to be watermarked.
 *  </UL>
 */
public java.util.Iterator recognize(sandmark.watermark.StaticRecognizeParameters params)
      throws sandmark.watermark.WatermarkingException {
    return new Recognizer(params);
}


} // class ConstantString

