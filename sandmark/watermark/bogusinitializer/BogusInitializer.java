package sandmark.watermark.bogusinitializer;

/** Algorithm Implemented by : Ashok Venkatraj & RathnaPrabhu
 *   emails : ashok@cs.arizona.edu
 *            prabhu@cs.arizona.edu
 * This algorithm adds a constant
 *    "sm$len=x"
 *  where x is the number of bogus initialisers
 * to be embedded in the constant pool of a random class.
 * sm$len is the identifer we're looking
 * for during recognition and based on the value , we will
 * looking for that many bogus initialisers embedded
 *
 * Important : THE INPUT JAR FILE SHOULD NOT CONTAIN ANY INTERFACES
 * OTHERWISE, getInstructionList will return null
 */
public class BogusInitializer
   extends sandmark.watermark.StaticWatermarker {
   int bogus_ids_no;
   private static boolean DEBUG = false; 

   public sandmark.config.ModificationProperty[] getMutations()
   {
    	sandmark.config.ModificationProperty[] properties = {
         sandmark.config.ModificationProperty.I_CHANGE_CONSTANT_POOL,
         sandmark.config.ModificationProperty.I_ADD_METHOD_CODE};
      return properties; 
   }

   public sandmark.config.RequisiteProperty[] getPostprohibited(){
      sandmark.config.RequisiteProperty[] properties = {
         sandmark.config.ModificationProperty.I_CHANGE_CONSTANT_POOL,
         sandmark.config.ModificationProperty.I_REMOVE_METHOD_CODE,
         sandmark.config.ModificationProperty.I_MODIFY_METHOD_CODE};
      return properties;
   }

   public String getDescription()
   {
      return getShortName() + " is a StaticWatermarker which embeds "+
         "a numeric watermark by breaking it into 2-digit numbers "+
         "and adding them to the constant pool" ;
   }

   public String getAuthor()
   {
      return "Ashok Venkatraj & Rathnaprabhu Rajendran";
   }

   public String getAuthorEmail()
   {
      return "ashok@cs.arizona.edu prabhu@cs.arizona.edu";
   }

   /**
    *  Constructs a watermarker.
    */
   public BogusInitializer() {}


   /**
    *  Returns this watermarker's short name.
    */
   public String getShortName() {
      return "Add Initialization";
   }

   /**
    *  Returns this watermarker's long name.
    */
   public String getLongName() {
      return "Embed the number of Bogus Initializers in a string in the constant pool";
   }

   /*
    *  Get the HTML codes of the About page for BogusInitializer
    */
   public java.lang.String getAlgHTML(){
      return
         "<HTML><BODY>\n" +
         "BogusInitializer is a Static Watermarking algorithm which embeds "+
         "a numeric watermark by breaking it into 2-digit numbers "+
         "and adding them to the constant pool." +
         "<table>\n" +
         "<TR><TD>\n" +
         "   Authors: <a href=\"mailto:ashok@cs.arizona.edu\">Ashok Venkatraj</a> and <a href=\"mailto:prabhu@cs.arizona.edu\">Rathna Prabhu</a>\n" +
         "</TR></TD>\n" +
         "</table>\n" +
         "</BODY></HTML>\n";
   }

   /*
    *  Get the URL of the Help page for BogusInitializer
    */
   public java.lang.String getAlgURL(){
      return "sandmark/watermark/bogusinitializer/doc/help.html";
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
   public void embed(sandmark.watermark.StaticEmbedParameters params) throws 
      sandmark.watermark.WatermarkingException 
   {
      String watermark = 
         sandmark.util.StringInt.encode(params.watermark).toString();
      java.util.Iterator classes = params.app.classes();
 
      if (!classes.hasNext())
         throw new sandmark.watermark.WatermarkingException
            ("There must be at least one class to watermark.");
  
      sandmark.program.Class cg=null;
      boolean canembed=false;
      while(classes.hasNext()){
         cg = (sandmark.program.Class)classes.next();
         if(!cg.isInterface() && !cg.isAbstract()){
            canembed=true;
            break;
         }
      }
      
      if(!canembed){
         throw new sandmark.watermark.WatermarkingException
            ("No suitable class in application");
      }
      
      org.apache.bcel.generic.ConstantPoolGen cpg = cg.getConstantPool();

      int temp_len=watermark.length();
      int stringIndex = cpg.addString("sm$len=" + watermark.length());
      if((temp_len%2)==0)
         bogus_ids_no=temp_len/2;
      else{
         bogus_ids_no=(temp_len/2)+1;
         watermark=watermark+"0";
      }
      
      sandmark.program.Method[] methods = cg.getMethods();
      sandmark.program.Method mg = null;
      for (int i=0;mg==null && i<methods.length;i++){
         if (methods[i].getInstructionList()!=null)
            mg = methods[i];
      }

      org.apache.bcel.generic.InstructionList il = mg.getInstructionList();

      int local_index=mg.calcMaxLocals();
      for(int i=0;i<bogus_ids_no;i++){
         String str_value = watermark.substring((2*i),(2*i)+2);
         byte b = (byte)Integer.parseInt(str_value);
         il.insert(new org.apache.bcel.generic.ISTORE(local_index++));
         il.insert(new org.apache.bcel.generic.BIPUSH(b));
      }
      
      mg.setMaxLocals();
      mg.setMaxStack();
      mg.mark();
   }


   /*************************************************************************/
   /*                              Recognition                              */
   /*************************************************************************/

   /*
     Obtain the value of sm$len . Based on the value of sm$len , we
     use a loop to find out  the values of BIPUSH . Inherent advantage is
     that, we aren't relying on the names of added bogus variables,
     some compilers may strip out such information (ie) if
     watermark was 1056 ,then the code added during embedding would
     give us the following :

     0:    bipush            56
     2:    istore            %4
     4:    bipush            10
     6:    istore_3
     ..... The method's code
     .....

     We strip out the values in BIPUSH statements and reconstruct back
     the original watermark .
   */
   class Recognizer implements java.util.Iterator{
      java.util.Vector result = new java.util.Vector();
      int current = 0;

      public Recognizer(sandmark.watermark.StaticRecognizeParameters params) {
         generate(params);
      }

      public void generate(sandmark.watermark.StaticRecognizeParameters params) {
         java.util.Iterator classes = params.app.classes();

         while (classes.hasNext()){
            sandmark.program.Class cg =(sandmark.program.Class)classes.next();
            String className = cg.getFileName();
            
            if(cg.isInterface() || cg.isAbstract())
               continue;
            
            org.apache.bcel.generic.ConstantPoolGen cpg = cg.getConstantPool();
            org.apache.bcel.classfile.ConstantPool cp=cpg.getConstantPool();
            sandmark.program.Method[] methods=cg.getMethods();
            sandmark.program.Method mg=null;
            for (int i=0;mg==null && i<methods.length;i++){
               if (methods[i].getInstructionList()!=null)
                  mg = methods[i];
            }
            org.apache.bcel.generic.InstructionList il = mg.getInstructionList();
            org.apache.bcel.generic.Instruction[] instr = il.getInstructions();
            
            for (int i=0; i<cp.getLength(); i++){
               org.apache.bcel.classfile.Constant c = cpg.getConstant(i);
               if (!(c instanceof org.apache.bcel.classfile.ConstantUtf8))
                  continue;
               String smlen = ((org.apache.bcel.classfile.ConstantUtf8)c).getBytes();
               if (!smlen.startsWith("sm$len="))
                  continue;

               int var_count=Integer.parseInt(smlen.substring("sm$len=".length()));
               int no_of_vars=0;
               if(var_count%2==0)
                  no_of_vars=var_count/2;
               else
                  no_of_vars=(var_count/2)+1;
               
               String wm="";
               for(int j=0;j<no_of_vars;j++){
                  org.apache.bcel.generic.BIPUSH bip= 
                     (org.apache.bcel.generic.BIPUSH)instr[2*j];
                  int sub_value=bip.getValue().intValue();
                  String str = String.valueOf(sub_value);
                  while (str.length()<2){
                     str='0'+str;
                  }
                  wm = str + wm;
               }

               if(wm.length()==var_count){
                  result.add(sandmark.util.StringInt.decode
                             (new java.math.BigInteger(wm)));
               }
               else{
                  result.add(sandmark.util.StringInt.decode
                             (new java.math.BigInteger
                              (wm.substring(0,wm.length()-1))));
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

} // class BogusInitializer

